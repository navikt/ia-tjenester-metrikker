package no.nav.arbeidsgiver.iatjenester.metrikker

import io.github.cdimascio.dotenv.dotenv
import io.javalin.Javalin
import no.nav.arbeidsgiver.iatjenester.metrikker.config.DBConfig
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.FlywayMigration
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.Liveness
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log
import java.io.Closeable
import java.lang.RuntimeException


private val environment = dotenv { ignoreIfMissing = true }

private val webServer = Javalin.create().apply {
    config.defaultContentType = "application/json"
    routes {
        get("/internal/isAlive") { if (Liveness.isAlive) it.status(200) else it.status(500) }
        get("/internal/isReady") { it.status(200) }
    }
}

fun start() {
    // TODO
    // getDataSource() --->
    // migrate
    // ...
    webServer.start(8222)

}


fun main() {
    /*properties (DB config, Auth config, ...) i parameter til startApplikasjon()
    i startApplikasjon()
    -> create Hikari Datasource
    -> run Flyway.Migrate()
    -> gi datasource som parameter til f.eks MetrikkerRepository()
    -> startWebServer()*/
    try {
        val testmiljø = when (environment["NAIS_CLUSTER_NAME"]) {
            "local" -> {"sthg"}
            "dev-gcp" -> {"sthg else"}
            else -> throw RuntimeException("Ukjent miljø")
        }

        // TODO: Fix me
        /*
        val dataSource = DBConfig(
            environment["DATABASE_URL"],
            environment["DATABASE_USERNAME"],
            environment["DATABASE_PASSWORD"]
        ).getDataSource()


        FlywayMigration(dataSource).setupOgMigrer()*/
        start(/* Mulig vi trenger å sende Datasource til applikasjon for å kunne skrive i DB*/)
    } catch (exception: Exception) {
        log("main()").error("Noe galt skjedde – oppstart er stoppet", exception)
    }
}
