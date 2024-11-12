package no.nav.arbeidsgiver.iatjenester.metrikker.domene

import no.nav.arbeidsgiver.iatjenester.metrikker.domene.Næring.ArbeidsmiljøportalenBransje.ANDRE_BRANSJER
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.Næring.ArbeidsmiljøportalenBransje.ANLEGG
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.Næring.ArbeidsmiljøportalenBransje.BARNEHAGER
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.Næring.ArbeidsmiljøportalenBransje.NÆRINGSMIDDELINDUSTRI
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.Næring.ArbeidsmiljøportalenBransje.TRANSPORT
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class NæringTest {
    @Test
    fun `getArbeidstilsynetBransje returner riktig bransje basert på 5-sifret kode`() {
        val næringForBarnehage = Næring("88911", "Barnehager", "Helse- og sosialtjenester")
        val næringForTransport =
            Næring("49311", "Rutebiltransport i by- og forstadsområde", "Landtransport og rørtransport")

        Assertions.assertThat(næringForBarnehage.getArbeidstilsynetBransje()).isEqualTo(BARNEHAGER)
        Assertions.assertThat(næringForTransport.getArbeidstilsynetBransje()).isEqualTo(TRANSPORT)
    }

    @Test
    fun `getArbeidstilsynetBransje returnerer riktig bransje basert på 2-sifret kode`() {
        val næringForNæringsmiddelIndustri =
            Næring("10810", "Produksjon av sukker", "Produksjon av nærings- og nytelsesmidler")

        val næringForAnlegg = Næring("42210", "Bygging av vann- og kloakkanlegg", "Anleggsvirksomhet")

        Assertions.assertThat(næringForNæringsmiddelIndustri.getArbeidstilsynetBransje())
            .isEqualTo(NÆRINGSMIDDELINDUSTRI)
        Assertions.assertThat(næringForAnlegg.getArbeidstilsynetBransje()).isEqualTo(ANLEGG)
    }

    @Test
    fun `getArbeidstilsynetBransje returnerer ANDRE_BRANSJER dersom bedriften ikke er i bransjeprogrammet`() {
        val annenNæring = Næring("45512", "Detaljhandel med biler", "Varehandel, reparasjon av motorvogner")

        Assertions.assertThat(annenNæring.getArbeidstilsynetBransje()).isEqualTo(ANDRE_BRANSJER)
    }

    @Test
    fun `getNæringskode2Siffer mapper femsifret kode til tosifret`() {
        val dummyNæring = Næring("12345", "xxx", "yyy")

        Assertions.assertThat(dummyNæring.getKode2siffer()).isEqualTo("12")
    }
}
