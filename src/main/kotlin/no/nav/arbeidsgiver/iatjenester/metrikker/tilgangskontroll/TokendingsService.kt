package no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll

import com.nimbusds.jose.JOSEObjectType.JWT
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import no.nav.arbeidsgiver.iatjenester.metrikker.config.TokenXConfigProperties
import no.nav.security.token.support.core.jwt.JwtToken
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import java.time.Instant.now
import java.util.Date
import java.util.UUID


internal const val PARAMS_GRANT_TYPE = "grant_type"
internal const val GRANT_TYPE = "urn:ietf:params:oauth:grant-type:token-exchange"
internal const val PARAMS_SUBJECT_TOKEN_TYPE = "subject_token_type"
internal const val SUBJECT_TOKEN_TYPE = "urn:ietf:params:oauth:token-type:jwt"
internal const val PARAMS_SUBJECT_TOKEN = "subject_token"
internal const val PARAMS_AUDIENCE = "audience"
internal const val PARAMS_CLIENT_ASSERTION = "client_assertion"
internal const val PARAMS_CLIENT_ASSERTION_TYPE = "client_assertion_type"
internal const val CLIENT_ASSERTION_TYPE = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer"
internal const val BEARER = "Bearer"

@Component
class TokendingsService(val tokenXConfig: TokenXConfigProperties) {

    fun exchangeTokenToAltinnProxy(subjectToken: JwtToken): JwtToken {
        val assertionToken: String = clientAssertion(
            tokenXConfig.tokendingsUrl,
            tokenXConfig.tokenEndpoint,
            RSAKey.parse(tokenXConfig.privateJwk)
        )

        val request = OAuth2TokenExchangeRequest(
            clientAssertion = assertionToken,
            subjectToken = subjectToken.tokenAsString,
            audience = tokenXConfig.altinnRettigheterProxyAudience
        )

        val response = RestTemplate().tokenExchange(tokenXConfig.tokendingsUrl, request)

        return JwtToken(response.body?.access_token)
    }
}

fun RestTemplate.tokenExchange(
    tokendingsUrl: String,
    request: OAuth2TokenExchangeRequest
): ResponseEntity<TokenExchangeResponse> {

    val contentTypeHeader =
        HttpHeaders().apply { contentType = MediaType.APPLICATION_FORM_URLENCODED }

    return this.postForEntity(
        tokendingsUrl,
        request.asParameters(),
        contentTypeHeader,
        TokenExchangeResponse::class
    )
}

fun OAuth2TokenExchangeRequest.asParameters(): Map<String, String> {
    return mapOf(
        PARAMS_CLIENT_ASSERTION to this.clientAssertion,
        PARAMS_CLIENT_ASSERTION_TYPE to this.clientAssertionType,
        PARAMS_SUBJECT_TOKEN to this.subjectToken,
        PARAMS_SUBJECT_TOKEN_TYPE to this.subjectTokenType,
        PARAMS_GRANT_TYPE to this.grantType,
        PARAMS_AUDIENCE to this.audience,
    )
}


data class OAuth2TokenExchangeRequest(
    val clientAssertion: String,
    val subjectToken: String,
    val audience: String,
    val subjectTokenType: String = SUBJECT_TOKEN_TYPE,
    val clientAssertionType: String = CLIENT_ASSERTION_TYPE,
    val grantType: String = GRANT_TYPE
)

fun clientAssertion(clientId: String, audience: String, rsaKey: RSAKey): String {
    val now = Date.from(now())
    val inSixtySeconds = Date.from(now().plusSeconds(60))
    val randomUUID = UUID.randomUUID().toString()

    return JWTClaimsSet.Builder()
        .issuer(clientId)
        .subject(clientId)
        .audience(audience)
        .issueTime(now)
        .expirationTime(inSixtySeconds)
        .jwtID(randomUUID)
        .notBeforeTime(now)
        .build()
        .sign(rsaKey)
        .serialize()
}

internal fun JWTClaimsSet.sign(rsaKey: RSAKey): SignedJWT =
    SignedJWT(
        JWSHeader.Builder(JWSAlgorithm.RS256)
            .keyID(rsaKey.keyID)
            .type(JWT)
            .build(),
        this
    ).apply { sign(RSASSASigner(rsaKey.toPrivateKey())) }

data class TokenExchangeResponse(
    val access_token: String,
    val issued_token_type: String,
    val token_type: String,
    val expires_in: String,
)
