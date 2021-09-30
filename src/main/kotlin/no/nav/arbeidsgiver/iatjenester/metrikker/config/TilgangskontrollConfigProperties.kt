package no.nav.arbeidsgiver.iatjenester.metrikker.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

enum class AltinnServiceId(val value: String) {
    IA_SERVICE("ia-service-i-altinn"),
    OPPFPLAN("oppfolgingsplan-service-i-altinn")
}

@ConstructorBinding
@ConfigurationProperties(prefix = "tilgangskontroll")
data class TilgangskontrollConfigProperties(
    var altinntjenester: Map<String, ServiceIAltinnKonfig>
)

data class ServiceIAltinnKonfig(val serviceCode: String, val serviceEdition: String)
