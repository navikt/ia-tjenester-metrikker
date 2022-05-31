package no.nav.arbeidsgiver.iatjenester.metrikker.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "tokenx")
/*
 * For forklaring av disse verdiene, se NAIS-dokumentasjonen (https://doc.nais.io/security/auth/tokenx)
 */
data class TokenXConfigProperties(
    val clientId: String,
    val privateJwk: String,
    val tokenEndpoint: String,
    val altinnRettigheterProxyAudience: String,
) {
    init {
        require(clientId.toHaveCorrectFormat()) { "'clientId' må være på format cluster:namespace:app" }
        require(tokenEndpoint.toBeValidUrl()) { "'tokenEndpoint' må være en gyldig URL" }
        require(privateJwk.toContainCorrectClaims()) { "'privateJwk' må inneholde nødvendige claims" }
        require(altinnRettigheterProxyAudience.toHaveCorrectFormat()) { "'audience' må være på format cluster:namespace:app" }
    }
}

internal fun String.toBeValidUrl(): Boolean {
    // TODO: Implement a proper URL validator
    return this.startsWith("http")
}

internal fun String.toContainCorrectClaims() = this.contains("kty") && this.contains("kid")

internal fun String.toHaveCorrectFormat() = this.matches(Regex("^.+:.+:.+$"))
