package no.nav.arbeidsgiver.iatjenester.metrikker.restdto

import no.nav.arbeidsgiver.iatjenester.metrikker.domene.Fylke
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.InstitusjonellSektorkode
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.Næringskode5Siffer
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.OverordnetEnhet
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.Underenhet
import no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll.Orgnr
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class MottattIaTjenesteTest {

    @Test
    fun getInnloggetMottattIaTjenesteMedVirksomhetGrunndata_returnerer_objektet_som_skal_persisteresi_DB() {

        val innloggetIaTjeneste = InnloggetMottattIaTjeneste(
            UNDERENHET_ORGNR,
            TypeIATjeneste.DIGITAL_IA_TJENESTE,
            Kilde.SAMTALESTØTTE,
        )
        val underenhet = Underenhet(
            orgnr = Orgnr(UNDERENHET_ORGNR),
            navn = "Test bedrift",
            næringskode = Næringskode5Siffer(
                "88911",
                "Barnehager"
            ),
            overordnetEnhetOrgnr = Orgnr(OVERORDNET_ENHET_ORGNR),
            kommune = "DRAMMEN",
            kommunenummer = "3005",
            fylke = Fylke.VIKEN,
            antallAnsatte = 15
        )
        val overordnetEnhet = OverordnetEnhet(
            orgnr = Orgnr(OVERORDNET_ENHET_ORGNR),
            navn = "Test overordnet enhet",
            næringskode = Næringskode5Siffer(
                "88912",
                "Barneparker og dagmammaer"
            ),
            institusjonellSektorkode = InstitusjonellSektorkode(
                "2100",
                "Private aksjeselskaper mv."
            ),
            antallAnsatte = 156
        )

        val (orgnr,
            næringKode5Siffer,
            type,
            kilde,
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
        const val OVERORDNET_ENHET_ORGNR: String = "999999999"
        const val UNDERENHET_ORGNR: String = "888888888"
    }
}
