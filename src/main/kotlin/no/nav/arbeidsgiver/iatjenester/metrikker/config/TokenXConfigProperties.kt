package no.nav.arbeidsgiver.iatjenester.metrikker.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "tokenx")
data class TokenXConfigProperties(
    val tokendingsUrl: String = "Tokendings URL (WELL_KNOWN_URL)",
    val clientId: String = "Client ID that uniquely identifies the application in TokenX. It has the format ´cluster:namespace:altinn-rettigheter-proxy`",
    val privateJwk: String = "Private JWK containing an RSA key belonging to your client. Used to sign client assertions during client authentication.",
    val tokenEndpoint: String = "Token endpoint, eg. `https://tokendings.dev-gcp.nais.io/token`",
    val altinnRettigheterProxyAudience: String = "",
) {
    init {
        require(tokendingsUrl.toBeValidUrl()) { "Ikke en gyldig URL" }
        require(clientId.matches(Regex("^.+:.+:.+$"))) { "$this.clientId er ikke på gyldig format" }
        require(tokenEndpoint.toBeValidUrl()) { "Ikke en gyldig URL" }
        require(privateJwk.toContainCorrectClaims())
        require(altinnRettigheterProxyAudience.matches(Regex("^.+:.+:.+$"))) { "$this.altinnRettigheterProxyAudience er ikke på gyldig format" }
    }
}

fun String.toBeValidUrl(): Boolean {
    // TODO: Implement a proper validator
    return this.startsWith("http")
}

fun String.toContainCorrectClaims(): Boolean {
    return this.contains("kty") && this.contains("kid")
}