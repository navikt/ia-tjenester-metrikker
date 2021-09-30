package no.nav.arbeidsgiver.iatjenester.metrikker.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

enum class AltinnServiceKey(val id: String) {
    IA("ia"),
    OPPFOLGINGSPLAN("oppfolgingsplan")
}

@ConstructorBinding
@ConfigurationProperties(prefix = "tilgangskontroll")
data class TilgangskontrollConfig(
    var altinnServices: Map<AltinnServiceKey, AltinnServiceConfig>
)

data class AltinnServiceConfig(val serviceCode: String, val serviceEdition: String)
