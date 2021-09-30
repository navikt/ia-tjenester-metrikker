package no.nav.arbeidsgiver.iatjenester.metrikker.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

enum class AltinnService(val id: String) {
    IA("ia"),
    OPPFOLGINGSPLAN("oppfolgingsplan")
}

@ConstructorBinding
@ConfigurationProperties(prefix = "tilgangskontroll")
data class TilgangskontrollConfigProperties(
    var altinnServices: Map<String, ServiceIAltinnKonfig>
)

data class ServiceIAltinnKonfig(val serviceCode: String, val serviceEdition: String)
