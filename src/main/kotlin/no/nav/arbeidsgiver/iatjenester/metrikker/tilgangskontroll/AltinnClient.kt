package no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll

import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnrettigheterProxyKlient
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.*
import org.springframework.stereotype.Component

@Component
class AltinnClient(private val klient: AltinnrettigheterProxyKlient) {

    fun hentOrganisasjonerBasertPaRettigheter(
        fnr: String,
        serviceKode: String,
        serviceEdition: String,
        selvbetjeningToken: String
    ): List<AltinnReportee> =
        klient.hentOrganisasjoner(
            SelvbetjeningToken(selvbetjeningToken),
            Subject(fnr),
            ServiceCode(serviceKode),
            ServiceEdition(serviceEdition),
            false
        )
}

