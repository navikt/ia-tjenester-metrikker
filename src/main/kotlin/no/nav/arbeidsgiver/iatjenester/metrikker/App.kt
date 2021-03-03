package no.nav.arbeidsgiver.iatjenester.metrikker

import no.nav.arbeidsgiver.iatjenester.metrikker.config.OutboundKafkaProperties
import no.nav.arbeidsgiver.iatjenester.metrikker.config.DBConfigProperties
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log
import no.nav.security.spring.oidc.api.EnableOIDCTokenValidation
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.runApplication


@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class, FlywayAutoConfiguration::class])
@EnableConfigurationProperties(value = [DBConfigProperties::class, OutboundKafkaProperties::class])
@EnableOIDCTokenValidation(
    ignore = ["springfox.documentation.swagger.web.ApiResourceController", "org.springframework"]
)
class App

fun main(args: Array<String>) {

    log("main()").info("Starter ia-tjenester-metrikker applikasjon")
    runApplication<App>(*args)
}
