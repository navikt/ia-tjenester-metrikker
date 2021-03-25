package no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.security.token.support.core.jwt.JwtTokenClaims
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*

@Component
class TilgangskontrollUtils @Autowired constructor(private val contextHolder: TokenValidationContextHolder) {

    fun erInnloggetSelvbetjeningBruker(): Any? {
        return hentClaim(ISSUER_SELVBETJENING, "sub")
            .map<Any> { fnrString: String? ->
                Fnr.erGyldigFnr(
                    fnrString!!
                )
            }
            .orElse(false)
    }

    fun hentInnloggetSelvbetjeningBruker(): InnloggetBruker {
        val fnr = hentClaim(ISSUER_SELVBETJENING, "sub")
            .orElseThrow<RuntimeException> { TilgangskontrollException("Finner ikke fodselsnummer til bruker.") }
        return InnloggetBruker(Fnr(fnr))
    }

    private fun hentClaim(issuer: String, claim: String): Optional<String> {
        val claims = hentClaimSet(issuer)
        return claims.map { jwtClaims: JwtTokenClaims ->
            jwtClaims[claim].toString()
        }
    }

    private fun hentClaimSet(issuer: String): Optional<JwtTokenClaims> {
        return Optional.ofNullable(contextHolder.tokenValidationContext.getClaims(issuer))
    }

    val selvbetjeningToken: JwtToken
        get() = contextHolder.tokenValidationContext.getJwtToken(ISSUER_SELVBETJENING)

    companion object {
        const val ISSUER_SELVBETJENING = "selvbetjening"
    }
}