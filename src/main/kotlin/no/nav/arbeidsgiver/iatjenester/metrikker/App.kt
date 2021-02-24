package no.nav.arbeidsgiver.iatjenester.metrikker

import io.github.cdimascio.dotenv.dotenv
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration
import org.springframework.boot.runApplication

private val environment = dotenv { ignoreIfMissing = true }

@SpringBootApplication(exclude = [FlywayAutoConfiguration::class])
class App

fun main(args: Array<String>) {
    runApplication<App>(*args)
}
