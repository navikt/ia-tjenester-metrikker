package no.nav.arbeidsgiver.iatjenester.metrikker.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "tokenx")
data class TokenXConfigProperties(
    val tokendingsUrl: String = "Tokendings URL (WELL_KNOWN_URL)",
    val clientId: String = "cluster:namespace:ia-tjenester-metrikker",
    val altinnRettigheterProxyAudience: String = "cluster:namespace:altinn-rettigheter-proxy"
) {
    init {
        require(tokendingsUrl.startsWith("http")) { "Ingen URL uten http, takk!" }
        require(clientId.matches(Regex("^.+:.+:.+$"))) { "$this.clientId er ikke på gyldig format" }
        require(altinnRettigheterProxyAudience.matches(Regex("^.+:.+:.+$"))) { "$this.altinnRettigheterProxyAudience er ikke på gyldig format" }
    }
}
