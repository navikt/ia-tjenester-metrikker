package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker

import no.nav.arbeidsgiver.iatjenester.metrikker.domene.Kilde
import no.nav.arbeidsgiver.iatjenester.metrikker.repository.IaTjenesterMetrikkerRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate.now

internal class MottattIaTjenesteUtilsTest {

    @Test
    fun `getMetadata() returnerer riktig bransje basert på 5 siffer kode`() {

        val metrikkForBarnehage = IaTjenesterMetrikkerRepository.MottattInnloggetIaTjenesteMetrikk(
            "999999999",
            Kilde.SYKEFRAVÆRSSTATISTIKK,
            IaTjenesterMetrikkerRepository.Næringskode5Siffer("88911", "Barnehager"),
            "Helse- og sosialtjenester  ",
            "0576",
            "Oslo",
            now().atStartOfDay()
        )
        val metrikkForTransport =
            IaTjenesterMetrikkerRepository.MottattInnloggetIaTjenesteMetrikk(
                "989898989",
                Kilde.SAMTALESTØTTE,
                IaTjenesterMetrikkerRepository.Næringskode5Siffer("49311", "Rutebiltransport i by- og forstadsområde"),
                "Landtransport og rørtransport",
                "0576",
                "Oslo",
                now().atStartOfDay()
            )


        Assertions.assertThat(metrikkForBarnehage.getMetadata().bransje).isEqualTo(ArbeidstilsynetBransje.BARNEHAGER)
        Assertions.assertThat(metrikkForTransport.getMetadata().bransje).isEqualTo(ArbeidstilsynetBransje.TRANSPORT)
    }

    @Test
    fun `getMetadata() returnerer riktig bransje basert på 2 siffer kode`() {
        val metrikkForNæringsmiddelIndustri = IaTjenesterMetrikkerRepository.MottattInnloggetIaTjenesteMetrikk(
            "999999999",
            Kilde.SAMTALESTØTTE,
            IaTjenesterMetrikkerRepository.Næringskode5Siffer("10810", "Produksjon av sukker"),
            "Produksjon av nærings- og nytelsesmidler",
            "0576",
            "Oslo",
            now().atStartOfDay()
        )
        val metrikkForAnlegg = IaTjenesterMetrikkerRepository.MottattInnloggetIaTjenesteMetrikk(
            "989898989",
            Kilde.SAMTALESTØTTE,
            IaTjenesterMetrikkerRepository.Næringskode5Siffer("42210", "Bygging av vann- og kloakkanlegg"),
            "Anleggsvirksomhet",
            "0576",
            "Oslo",
            now().atStartOfDay()
        )

        Assertions.assertThat(metrikkForNæringsmiddelIndustri.getMetadata().bransje)
            .isEqualTo(ArbeidstilsynetBransje.NÆRINGSMIDDELINDUSTRI)
        Assertions.assertThat(metrikkForAnlegg.getMetadata().bransje)
            .isEqualTo(ArbeidstilsynetBransje.ANLEGG)
    }

    @Test
    fun `getMetadata() returnerer ANDRE_BRANSJER dersom bedriften ikke er i bransjeprogrammet (Arbeidstilsynet bransje)`() {
        val metrikk = IaTjenesterMetrikkerRepository.MottattInnloggetIaTjenesteMetrikk(
            "999999999",
            Kilde.SAMTALESTØTTE,
            IaTjenesterMetrikkerRepository.Næringskode5Siffer("45512", "Detaljhandel med biler"),
            "Varehandel, reparasjon av motorvogner",
            "0576",
            "Oslo",
            now().atStartOfDay()
        )

        Assertions.assertThat(metrikk.getMetadata().bransje)
            .isEqualTo(ArbeidstilsynetBransje.ANDRE_BRANSJER)
    }
}
