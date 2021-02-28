package no.nav.arbeidsgiver.iatjenester.metrikker

import io.github.cdimascio.dotenv.dotenv
import no.nav.arbeidsgiver.iatjenester.metrikker.config.OutboundKafkaProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

private val environment = dotenv { ignoreIfMissing = true }

@EnableConfigurationProperties(value = [OutboundKafkaProperties::class])
@SpringBootApplication
class App
fun main(args: Array<String>) {
    runApplication<App>(*args)
}
