package no.nav.arbeidsgiver.iatjenester.metrikker

import no.nav.arbeidsgiver.iatjenester.metrikker.config.AltinnConfigProperties
import no.nav.arbeidsgiver.iatjenester.metrikker.config.DBConfigProperties
import no.nav.arbeidsgiver.iatjenester.metrikker.config.DatakatalogConfigProperties
import no.nav.arbeidsgiver.iatjenester.metrikker.config.FlywayConfigProperties
import no.nav.arbeidsgiver.iatjenester.metrikker.config.OutboundKafkaProperties
import no.nav.arbeidsgiver.iatjenester.metrikker.config.SecurityConfig
import no.nav.arbeidsgiver.iatjenester.metrikker.config.TilgangskontrollConfig
import no.nav.arbeidsgiver.iatjenester.metrikker.config.TokenXConfigProperties
import no.nav.arbeidsgiver.iatjenester.metrikker.enhetsregisteret.EnhetsregisteretProperties
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication


@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class, FlywayAutoConfiguration::class])
@EnableConfigurationProperties(
    value = [
        DBConfigProperties::class,
        FlywayConfigProperties::class,
        OutboundKafkaProperties::class,
        SecurityConfig::class,
        AltinnConfigProperties::class,
        TilgangskontrollConfig::class,
        DatakatalogConfigProperties::class,
        EnhetsregisteretProperties::class,
        TokenXConfigProperties::class,
    ]
)
class App

fun main(args: Array<String>) {

    log("main()").info("Starter ia-tjenester-metrikker applikasjon")
    runApplication<App>(*args)
}
