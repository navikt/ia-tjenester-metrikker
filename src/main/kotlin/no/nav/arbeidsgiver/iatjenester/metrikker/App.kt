package no.nav.arbeidsgiver.iatjenester.metrikker

import io.github.cdimascio.dotenv.dotenv
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

private val environment = dotenv { ignoreIfMissing = true }

@SpringBootApplication
class App
fun main(args: Array<String>) {
    runApplication<App>(*args)
}
