package no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll

import arrow.core.Either
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnrettigheterProxyKlient
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.ServiceCode
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.ServiceEdition
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.Subject
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.TokenXToken
import no.nav.arbeidsgiver.iatjenester.metrikker.config.AltinnServiceKey
import no.nav.arbeidsgiver.iatjenester.metrikker.config.TilgangskontrollConfig
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.AltinnRettighet
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log
import org.springframework.stereotype.Component

@Component
class TilgangskontrollService(
    private val klient: AltinnrettigheterProxyKlient,
    private val tilgangsconfig: TilgangskontrollConfig,
    private val tilgangskontrollUtils: TilgangskontrollUtils,
    private val tokendingsService: TokenxService,
) {

    val tilgangskontrollServiceLogger = log("TilgangskontrollService")

    fun hentInnloggetBruker(): Either<Exception, InnloggetBruker> {
        return hentInnloggetBrukerFraAltinn()
    }

    fun hentInnloggetBrukerFraAltinn(): Either<Exception, InnloggetBruker> {

        try {
            val innloggetSelvbetjeningBruker: InnloggetBruker =
                tilgangskontrollUtils.hentInnloggetBruker()


            val tokendingsToken =
                tokendingsService.exchangeTokenToAltinnProxy(tilgangskontrollUtils.hentJwtToken())

            innloggetSelvbetjeningBruker.organisasjoner =
                klient.hentOrganisasjoner(
                    TokenXToken(tokendingsToken.tokenAsString),
                    Subject(innloggetSelvbetjeningBruker.fnr.asString()),
                    false
                ).map {
                    AltinnOrganisasjon(
                        it.name,
                        it.parentOrganizationNumber,
                        it.organizationNumber,
                        it.organizationForm,
                        it.status!!,
                        it.type
                    )
                }
            return Either.Right(innloggetSelvbetjeningBruker)
        } catch (exception: Exception) {
            return Either.Left(exception)
        }
    }


    companion object {

        fun sjekkTilgangTilOrgnr(
            orgnr: Orgnr,
            bruker: InnloggetBruker
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
