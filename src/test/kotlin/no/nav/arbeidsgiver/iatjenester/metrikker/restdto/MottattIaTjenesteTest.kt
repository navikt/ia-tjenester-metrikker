package no.nav.arbeidsgiver.iatjenester.metrikker.restdto

import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.Næringskode5Siffer
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker.Fylke
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker.InstitusjonellSektorkode
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker.Kommune
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker.OverordnetEnhet
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker.Underenhet
import no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll.Orgnr
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import java.time.ZonedDateTime.now

internal class MottattIaTjenesteTest {

    @Test
    fun getInnloggetMottattIaTjenesteMedVirksomhetGrunndata_returnerer_objektet_som_skal_persisteresi_DB() {

        val innloggetIaTjeneste = InnloggetMottattIaTjeneste(
            UNDERENHET_ORGNR,
            AltinnRettighet.ARBEIDSGIVERS_OPPFØLGINGSPLAN_FOR_SYKMELDTE,
            TypeIATjeneste.DIGITAL_IA_TJENESTE,
            Kilde.SAMTALESTØTTE,
            TODAY
        )
        val underenhet = Underenhet(
            Orgnr(UNDERENHET_ORGNR),
            "Test bedrift",
            Næringskode5Siffer(
                "88911",
                "Barnehager"
            ),
            Orgnr(OVERORDNET_ENHET_ORGNR),
            Kommune(
                "3005",
                "DRAMMEN"
            ),
            Fylke(
                "03",
                "Viken"
            ),
            15
        )
        val overordnetEnhet = OverordnetEnhet(
            Orgnr(OVERORDNET_ENHET_ORGNR),
            "Test overordnet enhet",
            Næringskode5Siffer(
                "88912",
                "Barneparker og dagmammaer"
            ),
            InstitusjonellSektorkode("2100", "Private aksjeselskaper mv."),
            156
        )

        val (orgnr,
            næringKode5Siffer,
            type,
            kilde,
            tjenesteMottakkelsesdato,
            antallAnsatte,
            næringskode5SifferBeskrivelse,
            næring2SifferBeskrivelse,
            SSBSektorKode,
            SSBSektorKodeBeskrivelse,
            fylke,
            kommunenummer,
            kommune) = getInnloggetMottattIaTjenesteMedVirksomhetGrunndata(
            innloggetIaTjeneste = innloggetIaTjeneste,
            underenhet = underenhet,
            overordnetEnhet = overordnetEnhet
        )

        assertThat(orgnr).isEqualTo(UNDERENHET_ORGNR)
        assertThat(næringKode5Siffer).isEqualTo("88911")
        assertThat(type).isEqualTo(TypeIATjeneste.DIGITAL_IA_TJENESTE)
        assertThat(kilde).isEqualTo(Kilde.SAMTALESTØTTE)
        assertThat(tjenesteMottakkelsesdato).isEqualTo(TODAY)
        assertThat(antallAnsatte).isEqualTo(15)
        assertThat(næringskode5SifferBeskrivelse).isEqualTo("Barnehager")
        assertThat(næring2SifferBeskrivelse).isEqualTo("Sosiale omsorgstjenester uten botilbud")
        assertThat(SSBSektorKode).isEqualTo("2100")
        assertThat(SSBSektorKodeBeskrivelse).isEqualTo("Private aksjeselskaper mv.")
        assertThat(fylke).isEqualTo("Viken")
        assertThat(kommunenummer).isEqualTo("3005")
        assertThat(kommune).isEqualTo("DRAMMEN")
    }

    companion object {
        val OVERORDNET_ENHET_ORGNR: String = "999999999"
        val UNDERENHET_ORGNR: String = "888888888"
        val TODAY: ZonedDateTime = now()
    }
}