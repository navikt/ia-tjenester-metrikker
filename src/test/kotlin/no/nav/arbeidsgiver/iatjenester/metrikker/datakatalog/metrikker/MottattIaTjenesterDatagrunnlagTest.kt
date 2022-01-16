package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker

import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.Næring.ArbeidsmiljøportalenBransje
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog._1_JANUAR_2021
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog._1_MAI_2021
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog._21_JUNI_2021
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog._5_JUNI_2021
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.anleggsvirksomhet
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.detaljhandelMedBiler
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.dummyInnloggetMetrikk
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.dummyUinnloggetMetrikk
import no.nav.arbeidsgiver.iatjenester.metrikker.repository.IaTjenesterMetrikkerRepository.MottattUinnloggetIaTjenesteMetrikk
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.IaTjenesteTilgjengelighet
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.Kilde
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Month

internal class MottattIaTjenesterDatagrunnlagTest {

    @Test
    fun `beregnAntallMetrikkerPerMåned skal returnere riktig liste av måneder`() {
        val datagrunnlag = MottattIaTjenesterDatagrunnlag(
            innloggetMetrikker = listOf(
                dummyInnloggetMetrikk(),
                dummyInnloggetMetrikk(tidspunkt = _1_MAI_2021.atStartOfDay()),
                dummyInnloggetMetrikk(tidspunkt = _21_JUNI_2021.atStartOfDay()),
            ),
            uinnloggetMetrikker = emptyList(),
            fraDato = _1_JANUAR_2021,
            tilDato = _21_JUNI_2021
        )
        val resultat = datagrunnlag.beregnAntallMetrikkerPerMånedPerApp(
            Kilde.SYKEFRAVÆRSSTATISTIKK,
            IaTjenesteTilgjengelighet.INNLOGGET
        )
        Assertions.assertThat(resultat[Month.MAY]).isEqualTo(1)
        Assertions.assertThat(resultat[Month.JUNE]).isEqualTo(1)
        Assertions.assertThat(resultat[Month.JANUARY]).isEqualTo(0)

    }

    @Test
    fun `beregnAntallMetrikkerPerMåned skal returnere riktig liste for ønskede app`() {
        val datagrunnlag = MottattIaTjenesterDatagrunnlag(
            listOf(
                dummyInnloggetMetrikk(kilde = Kilde.SAMTALESTØTTE),
                dummyInnloggetMetrikk(tidspunkt = _1_MAI_2021.atStartOfDay()),
                dummyInnloggetMetrikk(
                    kilde = Kilde.SAMTALESTØTTE,
                    tidspunkt = _21_JUNI_2021.atStartOfDay()
                ),
            ), emptyList(), _1_JANUAR_2021, _21_JUNI_2021
        )
        val resultat = datagrunnlag.beregnAntallMetrikkerPerMånedPerApp(
            Kilde.SYKEFRAVÆRSSTATISTIKK,
            IaTjenesteTilgjengelighet.INNLOGGET
        )
        Assertions.assertThat(resultat[Month.MAY]).isEqualTo(1)
        Assertions.assertThat(resultat[Month.JUNE]).isEqualTo(0)
    }

    @Test
    fun `beregn totalt innlogget metrikker per App returnere 0 hvis listen er empty`() {
        val datagrunnlag = MottattIaTjenesterDatagrunnlag(
            innloggetMetrikker = emptyList(),
            uinnloggetMetrikker = emptyList(),
            fraDato = _1_JANUAR_2021,
            tilDato = _21_JUNI_2021
        )

        Assertions.assertThat(
            datagrunnlag.totalInnloggetMetrikkerPerApp(Kilde.SAMTALESTØTTE)
        ).isEqualTo(0)
        Assertions.assertThat(
            datagrunnlag.totalInnloggetMetrikkerPerApp(Kilde.SYKEFRAVÆRSSTATISTIKK)
        ).isEqualTo(0)
    }

    @Test
    fun `beregn totalt innlogget metrikker per App returnere riktige tall`() {
        val datagrunnlag = MottattIaTjenesterDatagrunnlag(
            innloggetMetrikker = listOf(dummyInnloggetMetrikk()),
            uinnloggetMetrikker = emptyList(),
            fraDato = _1_JANUAR_2021,
            tilDato = _21_JUNI_2021
        )

        Assertions.assertThat(datagrunnlag.totalInnloggetMetrikkerPerApp(Kilde.SAMTALESTØTTE))
            .isEqualTo(0)
        Assertions.assertThat(datagrunnlag.totalInnloggetMetrikkerPerApp(Kilde.SYKEFRAVÆRSSTATISTIKK))
            .isEqualTo(1)
    }

    @Test
    fun `beregn totalt innlogget metrikker per App returnere 0 hvis vi ikke har innloggetmetrikker`() {
        val datagrunnlag = MottattIaTjenesterDatagrunnlag(
            innloggetMetrikker = emptyList(),
            uinnloggetMetrikker = listOf(dummyUinnloggetMetrikk()),
            fraDato = _1_JANUAR_2021,
            tilDato = _21_JUNI_2021
        )

        Assertions.assertThat(datagrunnlag.totalInnloggetMetrikkerPerApp(Kilde.SAMTALESTØTTE))
            .isEqualTo(0)
        Assertions.assertThat(datagrunnlag.totalInnloggetMetrikkerPerApp(Kilde.SYKEFRAVÆRSSTATISTIKK))
            .isEqualTo(0)
    }


    @Test
    fun `beregn antall metrikker per bransje med hensyn på duplikater`() {
        val innloggetMetrikkerTest = listOf(
            dummyInnloggetMetrikk(),
            dummyInnloggetMetrikk(),
            dummyInnloggetMetrikk(orgnr = "888888888")
        )
        val datagrunnlag = MottattIaTjenesterDatagrunnlag(
            innloggetMetrikker = innloggetMetrikkerTest,
            uinnloggetMetrikker = emptyList(),
            fraDato = _1_JANUAR_2021,
            tilDato = _21_JUNI_2021
        )

        val resultat = datagrunnlag.mottatteIaTjenesterInnloggetPerBransjeOgKilde

        Assertions.assertThat(resultat.keys.filter { it.first == Kilde.SAMTALESTØTTE }.size)
            .isEqualTo(datagrunnlag.bransjeListe.size)
        Assertions.assertThat(resultat.keys.filter { it.first == Kilde.SYKEFRAVÆRSSTATISTIKK }.size)
            .isEqualTo(datagrunnlag.bransjeListe.size)
        Assertions.assertThat(
            resultat[BransjeOgKilde(
                Kilde.SYKEFRAVÆRSSTATISTIKK,
                ArbeidsmiljøportalenBransje.BARNEHAGER
            )]
        )
            .isEqualTo(2)
    }

    @Test
    fun `beregn antall metrikker per bransje, untatt ANDRE_BRANSJER med 0 for de bransjene uten metrikk`() {

        val innloggetMetrikkerTest = listOf(
            dummyInnloggetMetrikk(),
            dummyInnloggetMetrikk(
                kilde = Kilde.SAMTALESTØTTE,
                tidspunkt = _5_JUNI_2021.atStartOfDay()
            ),
            dummyInnloggetMetrikk(orgnr = "98888888"),
            dummyInnloggetMetrikk(
                orgnr = "97777777",
                kilde = Kilde.SAMTALESTØTTE,
                næring = anleggsvirksomhet,
                tidspunkt = LocalDate.now().atStartOfDay()
            ),
            dummyInnloggetMetrikk(
                orgnr = "96666666",
                kilde = Kilde.SAMTALESTØTTE,
                næring = detaljhandelMedBiler,
                tidspunkt = _5_JUNI_2021.atStartOfDay()
            )
        )

        val datagrunnlag = MottattIaTjenesterDatagrunnlag(
            innloggetMetrikker = innloggetMetrikkerTest,
            uinnloggetMetrikker = emptyList(),
            fraDato = _1_JANUAR_2021,
            tilDato = _21_JUNI_2021
        )

        val resultat: Map<BransjeOgKilde, Int> =
            datagrunnlag.mottatteIaTjenesterInnloggetPerBransjeOgKilde

        Assertions.assertThat(
            resultat[BransjeOgKilde(
                Kilde.SYKEFRAVÆRSSTATISTIKK,
                ArbeidsmiljøportalenBransje.BARNEHAGER
            )]
        )
            .isEqualTo(2)
        Assertions.assertThat(
            resultat[BransjeOgKilde(
                Kilde.SAMTALESTØTTE,
                ArbeidsmiljøportalenBransje.BARNEHAGER
            )]
        )
            .isEqualTo(1)
        Assertions.assertThat(
            resultat[BransjeOgKilde(
                Kilde.SAMTALESTØTTE,
                ArbeidsmiljøportalenBransje.ANLEGG
            )]
        )
            .isEqualTo(1)
        Assertions.assertThat(
            resultat[BransjeOgKilde(
                Kilde.SAMTALESTØTTE,
                ArbeidsmiljøportalenBransje.ANDRE_BRANSJER
            )]
        )
            .isNull()
    }

    @Test
    fun `beregn antall innlogget metrikker per dag for innlogget ia-tjenester`() {

        val innloggetMetrikkerTest = listOf(
            dummyInnloggetMetrikk(),
            dummyInnloggetMetrikk(tidspunkt = _1_MAI_2021.atStartOfDay().plusHours(4)),
            dummyInnloggetMetrikk(
                tidspunkt = _1_MAI_2021.atStartOfDay().plusHours(4),
                orgnr = "88888888"
            ),
            dummyInnloggetMetrikk(tidspunkt = _5_JUNI_2021.atStartOfDay()),
        )
        val datagrunnlag = MottattIaTjenesterDatagrunnlag(
            innloggetMetrikker = innloggetMetrikkerTest,
            uinnloggetMetrikker = emptyList(),
            fraDato = _1_JANUAR_2021,
            tilDato = _21_JUNI_2021
        )

        val resultat: Map<Month, Int> = datagrunnlag.beregnAntallMetrikkerPerMånedPerApp(
            Kilde.SYKEFRAVÆRSSTATISTIKK,
            IaTjenesteTilgjengelighet.INNLOGGET
        )

        Assertions.assertThat(resultat.keys.size).isEqualTo(6)
        Assertions.assertThat(resultat[Month.JANUARY]).isEqualTo(0)
        Assertions.assertThat(resultat[Month.FEBRUARY]).isEqualTo(0)
        Assertions.assertThat(resultat[Month.MARCH]).isEqualTo(0)
        Assertions.assertThat(resultat[Month.APRIL]).isEqualTo(0)
        Assertions.assertThat(resultat[Month.MAY]).isEqualTo(2)
        Assertions.assertThat(resultat[Month.JUNE]).isEqualTo(1)
    }

    @Test
    fun `beregn antall uinnlogget metrikker per dag for uinnlogget ia-tjenester`() {
        val uinnloggetMetrikkerTest = listOf(
            MottattUinnloggetIaTjenesteMetrikk(Kilde.SAMTALESTØTTE, _1_MAI_2021.atStartOfDay()),
            MottattUinnloggetIaTjenesteMetrikk(
                Kilde.SAMTALESTØTTE,
                _1_MAI_2021.atStartOfDay().plusHours(4)
            ),
            MottattUinnloggetIaTjenesteMetrikk(
                Kilde.SAMTALESTØTTE,
                _1_MAI_2021.atStartOfDay().plusHours(4)
            ),
            MottattUinnloggetIaTjenesteMetrikk(Kilde.SAMTALESTØTTE, _5_JUNI_2021.atStartOfDay()),
        )
        val datagrunnlag = MottattIaTjenesterDatagrunnlag(
            innloggetMetrikker = emptyList(),
            uinnloggetMetrikker = uinnloggetMetrikkerTest,
            _1_JANUAR_2021,
            _21_JUNI_2021
        )

        val resultat: Map<Month, Int> = datagrunnlag.beregnAntallMetrikkerPerMånedPerApp(
            Kilde.SAMTALESTØTTE,
            IaTjenesteTilgjengelighet.UINNLOGGET
        )

        Assertions.assertThat(resultat.keys.size).isEqualTo(6)
        Assertions.assertThat(resultat[Month.JANUARY]).isEqualTo(0)
        Assertions.assertThat(resultat[Month.FEBRUARY]).isEqualTo(0)
        Assertions.assertThat(resultat[Month.MARCH]).isEqualTo(0)
        Assertions.assertThat(resultat[Month.APRIL]).isEqualTo(0)
        Assertions.assertThat(resultat[Month.MAY]).isEqualTo(3)
        Assertions.assertThat(resultat[Month.JUNE]).isEqualTo(1)
    }
}
