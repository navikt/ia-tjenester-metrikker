package no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtToken
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class TilgangskontrollUtils @Autowired constructor(
    private val contextHolder: TokenValidationContextHolder,
) {

    fun hentJwtToken(): JwtToken =
        contextHolder.tokenValidationContext.firstValidToken.orElseThrow {
            TilgangskontrollException(
                "Finner ikke jwt token"
            )
        }

    fun hentInnloggetBruker(): InnloggetBruker {
        val fnr = contextHolder
            .tokenValidationContext
            .anyValidClaims
            .orElseThrow {
                TilgangskontrollException("Kan ikke hente innlogget bruker. Finner ikke claims.")
            }.getStringClaim("pid")

        return InnloggetBruker(Fnr(fnr));
    }
}