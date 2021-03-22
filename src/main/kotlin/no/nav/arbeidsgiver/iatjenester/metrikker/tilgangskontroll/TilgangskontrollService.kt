package no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll

import org.springframework.stereotype.Component


@Component
class TilgangskontrollService(
    private val klient: AltinnClient,
    private val tilgangskontrollUtils: TilgangskontrollUtils
) {
    private val serviceCode: String = "3403"
    private val serviceEdition: String = "1"

    fun hentInnloggetBruker(): InnloggetBruker {

        return if (tilgangskontrollUtils.erInnloggetSelvbetjeningBruker() as Boolean) {
            val innloggetSelvbetjeningBruker: InnloggetBruker = tilgangskontrollUtils.hentInnloggetSelvbetjeningBruker()

            innloggetSelvbetjeningBruker.organisasjoner =
                klient.hentOrganisasjonerBasertPaRettigheter(
                    innloggetSelvbetjeningBruker.fnr.asString(),
                    serviceCode,
                    serviceEdition,
                    tilgangskontrollUtils.selvbetjeningToken.tokenAsString
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