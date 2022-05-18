package no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll

import com.nimbusds.jose.JOSEObjectType.JWT
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import no.nav.arbeidsgiver.iatjenester.metrikker.config.TokenXConfigProperties
import org.springframework.stereotype.Component
import java.time.Instant.now
import java.util.Date
import java.util.UUID


@Component
class TokendingsService(val tokenXConfig: TokenXConfigProperties) {
}

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


//    fun exchangeTokenToAltinnProxy(token: JwtToken): JwtToken {
//
//    }