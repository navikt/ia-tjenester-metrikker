package no.nav.arbeidsgiver.iatjenester.metrikker

import no.nav.arbeidsgiver.iatjenester.metrikker.config.DBConfigProperties
import no.nav.arbeidsgiver.iatjenester.metrikker.config.OutboundKafkaProperties
import no.nav.arbeidsgiver.iatjenester.metrikker.config.SecurityConfig
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Profile


@Profile("local")
@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class, FlywayAutoConfiguration::class])
@EnableConfigurationProperties(
    value = [
        DBConfigProperties::class,
        OutboundKafkaProperties::class,
        SecurityConfig::class
    ]
)
@EnableJwtTokenValidation
@EnableMockOAuth2Server
class LokalApp

fun main(args: Array<String>) {

    log("main()").info("Starter ia-tjenester-metrikker applikasjon -- LOKALT --")
    runApplication<LokalApp>(*args)
}
