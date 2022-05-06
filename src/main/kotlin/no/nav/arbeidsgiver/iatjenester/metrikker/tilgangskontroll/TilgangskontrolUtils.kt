package no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll

import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log
import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.security.token.support.core.jwt.JwtTokenClaims
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import java.util.*
import java.util.function.Function
import java.util.function.Supplier
import java.util.stream.Stream

@Component
class TilgangskontrollUtils @Autowired constructor(
    private val contextHolder: TokenValidationContextHolder,
    private val environment: Environment
) {


    fun hentInnloggetJwtToken(): JwtToken = VALID_ISSUERS.stream()
        .map { issuer: String ->
            getJwtTokenFor(
                contextHolder.tokenValidationContext,
                issuer
            )
        }
        .flatMap { obj: Optional<JwtToken> -> obj.stream() }
        .findFirst()
        .orElseThrow<RuntimeException> {
            TilgangskontrollException(
                String.format(
                    "Finner ikke gyldig jwt token"
                )
            )
        }

    fun hentInnloggetBruker(): InnloggetBruker {
        val context = contextHolder.tokenValidationContext
        val claimsForIssuerSelvbetjening: Optional<JwtTokenClaims> = getClaimsFor(context, ISSUER_SELVBETJENING)

        if (claimsForIssuerSelvbetjening.isPresent) {
            TilgangskontrollUtils.log.debug("Claims kommer fra issuer Selvbetjening (loginservice)")
            return InnloggetBruker(
                Fnr(getFnrFraClaims(claimsForIssuerSelvbetjening.get()))
            )
        }
        val claimsForIssuerTokenX: Optional<JwtTokenClaims> = getClaimsFor(context, ISSUER_TOKENX)

        if (claimsForIssuerTokenX.isPresent) {
            TilgangskontrollUtils.log.debug("Claims kommer fra issuer TokenX")
            val fnrString: String = getTokenXFnr(claimsForIssuerTokenX.get())
            return InnloggetBruker(
                Fnr(fnrString)
            )
        }
        throw TilgangskontrollException(
            String.format(
                "Kan ikke hente innlogget bruker. Finner ikke claims for issuer '%s' eller '%s'",
                ISSUER_SELVBETJENING,
                ISSUER_TOKENX
            )
        )
    }

    private fun getFnrFraClaims(claimsForIssuerSelvbetjening: JwtTokenClaims): String {
        var fnrFromClaim = ""

        if (claimsForIssuerSelvbetjening.getStringClaim("pid") != null) {
            TilgangskontrollUtils.log.debug("Fnr hentet fra claims 'pid'")
            fnrFromClaim = claimsForIssuerSelvbetjening.getStringClaim("pid")
        } else if (claimsForIssuerSelvbetjening.getStringClaim("sub") != null) {
            TilgangskontrollUtils.log.debug("Fnr hentet fra claims 'sub' skal snart fases ut")
            fnrFromClaim = claimsForIssuerSelvbetjening.getStringClaim("sub")
        }
        return fnrFromClaim
    }

    private fun getClaimsFor(context: TokenValidationContext, issuer: String): Optional<JwtTokenClaims> {
        return if (context.hasTokenFor(issuer)) {
            Optional.of(context.getClaims(issuer))
        } else {
            Optional.empty()
        }
    }

    private fun getTokenXFnr(claims: JwtTokenClaims): String {
        /* NOTE: This is not validation of original issuer. We trust TokenX to only issue
         * tokens from trustworthy sources. The purpose is simply to differentiate different
         * original issuers to extract the fnr. */
        val idp = claims.getStringClaim("idp")

        if (idp.matches("^https://oidc.*difi.*\\.no/idporten-oidc-provider/$".toRegex())) {
            return claims.getStringClaim("pid")
        }

        if (idp.matches("^https://nav(no|test)b2c\\.b2clogin\\.com/.*$".toRegex())) {
            return getFnrFraClaims(claims)
        }

        if (idp.matches("https://fakedings.dev-gcp.nais.io/fake/idporten".toRegex())
            && Arrays.stream(environment.activeProfiles).noneMatch { profile: String -> profile == "prod" }
        ) {
            return claims.getStringClaim("pid")
        }

        throw TilgangskontrollException("Ukjent idp fra tokendings")
    }

    private fun getJwtTokenFor(context: TokenValidationContext, issuer: String): Optional<JwtToken> {
        return Optional.ofNullable(context.getJwtToken(issuer))
    }


    companion object {
        const val ISSUER_TOKENX = "tokenx"
        const val ISSUER_SELVBETJENING = "selvbetjening"
        val VALID_ISSUERS: Set<String> = setOf(ISSUER_TOKENX, ISSUER_SELVBETJENING)
    }
}
