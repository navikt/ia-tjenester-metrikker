package no.nav.arbeidsgiver.iatjenester.metrikker.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "tilgangskontroll.ia-service-i-altinn")
data class TilgangskontrollConfigProperties(
    var serviceCode: String,
    var serviceEdition: String
)
