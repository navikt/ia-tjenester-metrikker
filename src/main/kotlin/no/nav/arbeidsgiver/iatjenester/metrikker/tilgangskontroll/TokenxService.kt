package no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll

import com.nimbusds.jose.JOSEObjectType.JWT
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import no.nav.arbeidsgiver.iatjenester.metrikker.config.TokenXConfigProperties
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log
import no.nav.security.token.support.core.jwt.JwtToken
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import java.time.Instant.now
import java.util.Date
import java.util.UUID

@Component
class TokenxService(
    val tokenXConfig: TokenXConfigProperties,
) {
    fun exchangeTokenToAltinnProxy(subjectToken: JwtToken): JwtToken = with(tokenXConfig) {

        val clientAssertionToken = clientAssertion(
            clientId = clientId,
            audience = tokenEndpoint,
            rsaKey = RSAKey.parse(privateJwk)
        )

        val tokenExchangeRequest = OAuth2TokenExchangeRequest(
            clientAssertion = clientAssertionToken,
            subjectToken = subjectToken.tokenAsString,
            audience = altinnRettigheterProxyAudience
        )

        val response = tokenExchange(tokenEndpoint, tokenExchangeRequest)
        return JwtToken(response.body?.access_token)
            .also { log.info("Token exchange completed; returned access_token has length of ${it.tokenAsString.length}") }
    }

    internal fun clientAssertion(clientId: String, audience: String, rsaKey: RSAKey): String {
        log.info("Performing client assertion with clientId:$clientId and audience:$audience")

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
            .sign(with = rsaKey)
            .serialize()
    }

    internal fun tokenExchange(
        tokenEndpoint: String,
        tokenExchangeRequest: OAuth2TokenExchangeRequest,
    ): ResponseEntity<TokenExchangeResponse> {
        log.info("Performing token exchange for audience:${tokenExchangeRequest.audience}")

        return RestTemplate().postForEntity(
            tokenEndpoint,
            tokenExchangeRequest.asHttpEntity(),
            TokenExchangeResponse::class
        )
    }
}

internal fun OAuth2TokenExchangeRequest.asHttpEntity() = HttpEntity<MultiValueMap<String, String>>(
    this.asParameterMap(),
    HttpHeaders().apply { contentType = MediaType.APPLICATION_FORM_URLENCODED }
)

internal const val PARAMS_GRANT_TYPE = "grant_type"
internal const val PARAMS_SUBJECT_TOKEN_TYPE = "subject_token_type"
internal const val PARAMS_SUBJECT_TOKEN = "subject_token"
internal const val PARAMS_AUDIENCE = "audience"
internal const val PARAMS_CLIENT_ASSERTION = "client_assertion"
internal const val PARAMS_CLIENT_ASSERTION_TYPE = "client_assertion_type"

internal fun OAuth2TokenExchangeRequest.asParameterMap() =
    LinkedMultiValueMap<String, String>().apply {
        add(PARAMS_CLIENT_ASSERTION, clientAssertion)
        add(PARAMS_CLIENT_ASSERTION_TYPE, clientAssertionType)
        add(PARAMS_SUBJECT_TOKEN, subjectToken)
        add(PARAMS_SUBJECT_TOKEN_TYPE, subjectTokenType)
        add(PARAMS_GRANT_TYPE, grantType)
        add(PARAMS_AUDIENCE, audience)
    }

internal const val CLIENT_ASSERTION_TYPE = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer"
internal const val GRANT_TYPE = "urn:ietf:params:oauth:grant-type:token-exchange"
internal const val SUBJECT_TOKEN_TYPE = "urn:ietf:params:oauth:token-type:jwt"

internal data class OAuth2TokenExchangeRequest(
    val clientAssertion: String,
    val subjectToken: String,
    val audience: String,
    val subjectTokenType: String = SUBJECT_TOKEN_TYPE,
    val clientAssertionType: String = CLIENT_ASSERTION_TYPE,
    val grantType: String = GRANT_TYPE,
)

internal fun JWTClaimsSet.sign(with: RSAKey): SignedJWT =
    SignedJWT(
        JWSHeader.Builder(JWSAlgorithm.RS256)
            .keyID(with.keyID)
            .type(JWT)
            .build(),
        this
    ).apply { sign(RSASSASigner(with.toPrivateKey())) }

internal data class TokenExchangeResponse(
    val access_token: String,
    val issued_token_type: String,
    val token_type: String,
    val expires_in: String,
)
