package no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll

import arrow.core.Either
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnrettigheterProxyKlient
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.SelvbetjeningToken
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.ServiceCode
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.ServiceEdition
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.Subject
import no.nav.arbeidsgiver.iatjenester.metrikker.config.TilgangskontrollConfigProperties
import org.springframework.stereotype.Component

enum class AltinnServiceId(val value: String) {
    IA_SERVICE("ia-service-i-altinn"),
    OPPFPLAN("oppfolgingsplan-service-i-altinn")
}

@Component
class TilgangskontrollService(
    private val klient: AltinnrettigheterProxyKlient,
    private val tilgangsconfig: TilgangskontrollConfigProperties,
    private val tilgangskontrollUtils: TilgangskontrollUtils
) {

    fun hentInnloggetBruker(altinnServiceId: AltinnServiceId): Either<TilgangskontrollException, InnloggetBruker> {

        if (tilgangskontrollUtils.erInnloggetSelvbetjeningBruker() as Boolean) {
            val innloggetSelvbetjeningBruker: InnloggetBruker = tilgangskontrollUtils.hentInnloggetSelvbetjeningBruker()

            innloggetSelvbetjeningBruker.organisasjoner =
                klient.hentOrganisasjoner(
                    SelvbetjeningToken(tilgangskontrollUtils.selvbetjeningToken.tokenAsString),
                    Subject(innloggetSelvbetjeningBruker.fnr.asString()),
                    ServiceCode(tilgangsconfig.altinntjenester[altinnServiceId.value]!!.serviceCode),
                    ServiceEdition(tilgangsconfig.altinntjenester[altinnServiceId.value]!!.serviceEdition),
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
        } else {
            return Either.Left(TilgangskontrollException("Innlogget bruker er ikke selvbetjeningsbruker"))
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