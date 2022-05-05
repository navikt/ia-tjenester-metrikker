package no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll

import arrow.core.Either
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnrettigheterProxyKlient
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.SelvbetjeningToken
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.ServiceCode
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.ServiceEdition
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.Subject
import no.nav.arbeidsgiver.iatjenester.metrikker.config.AltinnServiceKey
import no.nav.arbeidsgiver.iatjenester.metrikker.config.TilgangskontrollConfig
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.AltinnRettighet
import org.springframework.stereotype.Component

@Component
class TilgangskontrollService(
    private val klient: AltinnrettigheterProxyKlient,
    private val tilgangsconfig: TilgangskontrollConfig,
    private val tilgangskontrollUtils: TilgangskontrollUtils
) {

    fun hentInnloggetBruker(altinnRettighet: AltinnRettighet): Either<Exception, InnloggetBruker> {
        when (altinnRettighet) {
            AltinnRettighet.SYKEFRAVÆRSSTATISTIKK_FOR_VIRKSOMHETER ->
                return hentInnloggetBruker(AltinnServiceKey.IA)
            AltinnRettighet.ARBEIDSGIVERS_OPPFØLGINGSPLAN_FOR_SYKMELDTE ->
                return hentInnloggetBruker(AltinnServiceKey.OPPFOLGINGSPLAN)
        }
    }

    fun hentInnloggetBruker(serviceKey: AltinnServiceKey): Either<Exception, InnloggetBruker> {

        try {
            val innloggetSelvbetjeningBruker: InnloggetBruker = tilgangskontrollUtils.hentInnloggetSelvbetjeningBruker()

            val currentAltinnServiceConfig = tilgangsconfig.altinnServices.getValue(serviceKey)

            innloggetSelvbetjeningBruker.organisasjoner =
                klient.hentOrganisasjoner(
                    SelvbetjeningToken(tilgangskontrollUtils.selvbetjeningToken.tokenAsString),
                    Subject(innloggetSelvbetjeningBruker.fnr.asString()),
                    ServiceCode(currentAltinnServiceConfig.serviceCode),
                    ServiceEdition(currentAltinnServiceConfig.serviceEdition),
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
