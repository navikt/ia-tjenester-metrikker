package no.nav.arbeidsgiver.iatjenester.metrikker

import io.github.cdimascio.dotenv.dotenv
import io.javalin.Javalin
import no.nav.arbeidsgiver.iatjenester.metrikker.config.DBConfig
import no.nav.arbeidsgiver.iatjenester.metrikker.config.DatabaseCredentials
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.FlywayMigration
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.Liveness
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log


private val environment = dotenv { ignoreIfMissing = true }
private val erSQLInstanseKlarTilBruk = false

private val webServer = Javalin.create().apply {
    config.defaultContentType = "application/json"
    routes {
        get("/internal/isAlive") { if (Liveness.isAlive) it.status(200) else it.status(500) }
        get("/internal/isReady") { it.status(200) }
    }
}

fun start() {
    webServer.start(8222)
}

fun main() {
    try {
        val driverClassName = when (environment["NAIS_CLUSTER_NAME"]) {
            "local" -> {
                "org.h2.Driver"
            }
            "dev-gcp" -> {
                "org.postgresql.Driver"
            }
            else -> throw RuntimeException("Ukjent miljø")
        }


        if (erSQLInstanseKlarTilBruk) {
            val password = if (environment["DATABASE_PASSWORD"].isEmpty())
                "empty" else environment["DATABASE_PASSWORD"].substring(0, 8)
            log("main()").info("DATABASE_USERNAME=" + environment["DATABASE_USERNAME"])
            log("main()").info("DATABASE_Passord=$password*****")

            val dataSource = DBConfig(
                DatabaseCredentials(
                    environment["NAIS_CLUSTER_NAME"],
                    environment["DATABASE_HOST"],
                    environment["DATABASE_PORT"],
                    environment["DATABASE_DATABASE"]
                ).getUrl(),
                environment["DATABASE_USERNAME"],
                environment["DATABASE_PASSWORD"],
                driverClassName
            ).getDataSource()

            FlywayMigration(dataSource).setupOgMigrer("local" == environment["NAIS_CLUSTER_NAME"])
        }

        start()
    } catch (exception: Exception) {
        log("main()").error("Det har skjedd en feil ved oppstarting av ia-tjenester-metrikker", exception)
    }
}
