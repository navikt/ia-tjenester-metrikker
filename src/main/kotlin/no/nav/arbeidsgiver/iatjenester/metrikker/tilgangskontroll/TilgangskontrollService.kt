package no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll

import arrow.core.Either
import no.nav.arbeidsgiver.iatjenester.metrikker.altinn.AltinnTilgangerKlient
import org.springframework.stereotype.Component

@Component
class TilgangskontrollService(
    private val altinnTilgangerKlient: AltinnTilgangerKlient,
    private val tilgangskontrollUtils: TilgangskontrollUtils,
    private val tokendingsService: TokenxService,
) {
    fun hentInnloggetBruker(): Either<Exception, InnloggetBruker> = hentInnloggetBrukerFraAltinn()

    fun hentInnloggetBrukerFraAltinn(): Either<Exception, InnloggetBruker> {
        try {
            val innloggetSelvbetjeningBruker: InnloggetBruker =
                tilgangskontrollUtils.hentInnloggetBruker()
            val tokendingsToken =
                tokendingsService.exchangeTokenToAltinnTilganger(tilgangskontrollUtils.hentJwtToken())

            val altinnOrganisasjoner = altinnTilgangerKlient.hentAltinnOrganisasjoner(tokendingsToken)
            innloggetSelvbetjeningBruker.organisasjoner = altinnOrganisasjoner
            return Either.Right(innloggetSelvbetjeningBruker)
        } catch (exception: Exception) {
            return Either.Left(exception)
        }
    }

    companion object {
        fun sjekkTilgangTilOrgnr(
            orgnr: Orgnr,
            bruker: InnloggetBruker,
        ): Either<TilgangskontrollException, InnloggetBruker> {
            val harTilgang = bruker.harTilgang(orgnr)
            return if (harTilgang) {
                Either.Right(bruker)
            } else {
                Either.Left(TilgangskontrollException("Har ikke tilgang til IA tjenester for denne bedriften"))
            }
        }
    }
}
