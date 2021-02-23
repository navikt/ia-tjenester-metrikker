package no.nav.arbeidsgiver.iatjenester.metrikker

import io.github.cdimascio.dotenv.dotenv
import io.javalin.Javalin
import no.nav.arbeidsgiver.iatjenester.metrikker.config.DBConfig
import no.nav.arbeidsgiver.iatjenester.metrikker.config.DatabaseCredentials
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.FlywayMigration
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.Liveness
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log


private val environment = dotenv { ignoreIfMissing = true }

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

        start(/* Mulig vi trenger å sende Datasource til applikasjon for å kunne skrive i DB*/)
    } catch (exception: Exception) {
        log("main()").error("Det har skjedd en feil ved oppstarting av ia-tjenester-metrikker", exception)
    }
}
