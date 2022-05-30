package no.nav.arbeidsgiver.iatjenester.metrikker.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "tokenx")
/*
 * For forklaring av disse verdiene, se NAIS-dokumentasjonen (https://doc.nais.io/security/auth/tokenx)
 */
data class TokenXConfigProperties(
    val tokendingsUrl: String,
    val clientId: String,
    val privateJwk: String,
    val tokenEndpoint: String,
    val altinnRettigheterProxyAudience: String,
) {
    init {
        require(tokendingsUrl.toBeValidUrl()) { "tokendingsUrl er ikke en gyldig URL" }
        require(clientId.toHaveCorrectFormat()) { "clientId er ikke på gyldig format" }
        require(tokenEndpoint.toBeValidUrl()) { "tokenEndpoint er ikke en gyldig URL" }
        require(privateJwk.toContainCorrectClaims()) { "privateJwk inneholder ikke nødvendige claims" }
        require(altinnRettigheterProxyAudience.toHaveCorrectFormat()) { "Audience er ikke på gyldig format" }
    }
}

internal fun String.toBeValidUrl(): Boolean {
    // TODO: Implement a proper URL validator
    return this.startsWith("http")
}

internal fun String.toContainCorrectClaims(): Boolean {
    return this.contains("kty") && this.contains("kid")
}

internal fun String.toHaveCorrectFormat(): Boolean = this.matches(Regex("^.+:.+:.+$"))