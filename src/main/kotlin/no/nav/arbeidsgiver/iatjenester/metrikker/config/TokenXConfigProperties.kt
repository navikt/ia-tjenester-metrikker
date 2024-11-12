package no.nav.arbeidsgiver.iatjenester.metrikker.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "tokenx-service")
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
        require(tokenEndpoint.toBeAValidTokenUrl()) { "'tokenEndpoint' er ikke en gyldig URL" }
        require(privateJwk.toContainCorrectClaims()) { "'privateJwk' må inneholde nødvendige claims" }
        require(altinnRettigheterProxyAudience.toHaveCorrectFormat()) { "'audience' må være på format cluster:namespace:app" }
    }
}

internal fun String.toBeAValidTokenUrl(): Boolean = this.startsWith("http") && this.endsWith("/token")

internal fun String.toContainCorrectClaims() = this.contains("kty") && this.contains("kid")

internal fun String.toHaveCorrectFormat() = this.matches(Regex("^.+:.+:.+$"))
