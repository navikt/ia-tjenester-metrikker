package no.nav.arbeidsgiver.iatjenester.metrikker.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "tokenx")
data class TokenXConfigProperties(
    val tokendingsUrl: String = "Tokendings URL (WELL_KNOWN_URL)",
    val clientId: String = "Client ID (<cluster>:<metadata.namespace>:<metadata.name>)"
) {
    init {
        require(tokendingsUrl.startsWith("http")) { "Ingen URL uten http, takk!" }
        require(clientId.matches(Regex("^.+:.+:.+$"))) { "$this.clientId er ikke en gyldig clientId" }
    }
}
