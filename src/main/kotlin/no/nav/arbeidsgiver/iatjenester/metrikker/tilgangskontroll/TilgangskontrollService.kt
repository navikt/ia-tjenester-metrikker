package no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll

import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnrettigheterProxyKlient
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.SelvbetjeningToken
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.ServiceCode
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.ServiceEdition
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.Subject
import no.nav.arbeidsgiver.iatjenester.metrikker.config.IaServiceIAltinnKonfig
import org.springframework.stereotype.Component


@Component
class TilgangskontrollService(
    private val klient: AltinnrettigheterProxyKlient,
    private val iaServiceIAltinnKonfig: IaServiceIAltinnKonfig,
    private val tilgangskontrollUtils: TilgangskontrollUtils
) {

    fun hentInnloggetBruker(): InnloggetBruker {

        return if (tilgangskontrollUtils.erInnloggetSelvbetjeningBruker() as Boolean) {
            val innloggetSelvbetjeningBruker: InnloggetBruker = tilgangskontrollUtils.hentInnloggetSelvbetjeningBruker()

            innloggetSelvbetjeningBruker.organisasjoner =
                klient.hentOrganisasjoner(
                    SelvbetjeningToken(tilgangskontrollUtils.selvbetjeningToken.tokenAsString),
                    Subject(innloggetSelvbetjeningBruker.fnr.asString()),
                    ServiceCode(iaServiceIAltinnKonfig.serviceCode),
                    ServiceEdition(iaServiceIAltinnKonfig.serviceEdition),
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
            innloggetSelvbetjeningBruker
        } else {
            throw TilgangskontrollException("Innlogget bruker er ikke selvbetjeningsbruker")
        }
    }


    companion object {

        fun sjekkTilgangTilOrgnr(
            orgnr: Orgnr,
            bruker: InnloggetBruker
        ) {
            val harTilgang = bruker.harTilgang(orgnr)
            if (!harTilgang) {
                throw TilgangskontrollException("Har ikke tilgang til IA tjenester for denne bedriften.")
            }
        }
    }
}