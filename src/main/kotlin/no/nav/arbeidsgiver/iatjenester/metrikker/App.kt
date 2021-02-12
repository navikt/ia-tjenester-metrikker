package no.nav.arbeidsgiver.iatjenester.metrikker

import io.javalin.Javalin
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.Liveness
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log
import java.io.Closeable

class App(
) : Closeable {

    private val webServer = Javalin.create().apply {
        config.defaultContentType = "application/json"
        routes {
            get("/internal/isAlive") { if (Liveness.isAlive) it.status(200) else it.status(500) }
            get("/internal/isReady") { it.status(200) }
        }
    }

    fun start() = try {
        webServer.start(8222)


    } catch (exception: Exception) {
        close()
        throw exception
    }


    override fun close() {
        webServer.stop()
    }
}

fun main() {
    try {
        App(

        ).start()

    } catch (exception: Exception) {
        log("main()").error("Noe galt skjedde – indekseringen er stoppet", exception)
    }
}
