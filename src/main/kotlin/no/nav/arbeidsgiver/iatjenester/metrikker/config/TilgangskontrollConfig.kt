package no.nav.arbeidsgiver.iatjenester.metrikker.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TilgangskontrollConfig(private val tilgangskontrollConfigProperties: TilgangskontrollConfigProperties) {

    @Bean fun altinnIaServiceKonfig(): IaServiceIAltinnKonfig {
        return IaServiceIAltinnKonfig(
            tilgangskontrollConfigProperties.serviceCode,
            tilgangskontrollConfigProperties.serviceEdition
        )
    }
}

data class IaServiceIAltinnKonfig (val serviceCode: String, val serviceEdition: String)
