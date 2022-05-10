package no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class TilgangskontrollUtils @Autowired constructor(
    private val contextHolder: TokenValidationContextHolder,
) {

    fun hentJwtToken() = contextHolder.tokenValidationContext.firstValidToken.orElseThrow {
        TilgangskontrollException(
            "Finner ikke jwt token"
        )
    }

    fun hentInnloggetBruker(): InnloggetBruker {
        val tokenClaims = contextHolder
            .tokenValidationContext
            .anyValidClaims
            .orElseThrow {
                TilgangskontrollException("Kan ikke hente innlogget bruker. Finner ikke claims.")
            };

        val fnrString = if (tokenClaims.containsClaim("pid", "*")) {
            tokenClaims.getStringClaim("pid")
        } else {
            tokenClaims.getStringClaim("sub")
        };

        return InnloggetBruker(Fnr(fnrString));
    }
}