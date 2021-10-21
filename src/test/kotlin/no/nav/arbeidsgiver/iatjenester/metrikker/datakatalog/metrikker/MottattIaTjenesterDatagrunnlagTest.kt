package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker

import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.Næring
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.Næring.ArbeidstilsynetBransje
import no.nav.arbeidsgiver.iatjenester.metrikker.repository.IaTjenesterMetrikkerRepository.MottattInnloggetIaTjenesteMetrikk
import no.nav.arbeidsgiver.iatjenester.metrikker.repository.IaTjenesterMetrikkerRepository.MottattUinnloggetIaTjenesteMetrikk
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.Kilde
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month

internal class MottattIaTjenesterDatagrunnlagTest {

    private val _1_JANUAR_2021 = LocalDate.of(2021, Month.JANUARY, 1)
    private val _21_JUNI_2021 = LocalDate.of(2021, Month.JUNE, 21)
    private val _1_MAI = LocalDate.of(2021, Month.MAY, 1)
    private val _5_JUNI = LocalDate.of(2021, Month.JUNE, 5)


    val anleggsvirksomhet = Næring("42210", "Bygging av vann- og kloakkanlegg", "Anleggsvirksomhet")
    val barnehage = Næring("88911", "Barnehager", "Helse- og sosialtjenester")
    val detaljhandelMedBiler = Næring("45512", "Detaljhandel med biler", "Varehandel, reparasjon av motorvogner")


    private fun dummyInnloggetMetrikk(
        orgnr: String = "99999999",
        kilde: Kilde = Kilde.SYKEFRAVÆRSSTATISTIKK,
        næring: Næring = barnehage,
        kommunenummer: String = "0301",
        kommune: String = "Oslo",
        fylke: String = "Oslo",
        tidspunkt: LocalDateTime = _1_MAI.atStartOfDay()
    ) = MottattInnloggetIaTjenesteMetrikk(orgnr, kilde, næring, kommunenummer, kommune, fylke, tidspunkt)

    @Test
    fun `beregn antall innlogget metrikker per måned summerer antall mottatt metrikker per måned`() {
        val datagrunnlag = MottattIaTjenesterDatagrunnlag(
            innloggetMetrikker = emptyList(),
            uinnloggetMetrikker = emptyList(),
            fraDato = _1_JANUAR_2021,
            tilDato = _21_JUNI_2021
        )
        val resultat =
            datagrunnlag.beregnAntallMetrikkerPerMåned(
                listOf(Month.FEBRUARY, Month.MARCH, Month.APRIL),
                mapOf(
                    LocalDate.of(2021, Month.FEBRUARY, 5) to 15,
                    LocalDate.of(2021, Month.FEBRUARY, 6) to 9,
                    LocalDate.of(2021, Month.MARCH, 5) to 5,
                    LocalDate.of(2021, Month.APRIL, 5) to 44
                )
            )

        Assertions.assertThat(resultat.keys.size).isEqualTo(3)
        Assertions.assertThat(resultat[Month.FEBRUARY]).isEqualTo(24)
        Assertions.assertThat(resultat[Month.MARCH]).isEqualTo(5)
        Assertions.assertThat(resultat[Month.APRIL]).isEqualTo(44)
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
        Assertions.assertThat(resultat[BransjePerKilde(Kilde.SYKEFRAVÆRSSTATISTIKK, ArbeidstilsynetBransje.BARNEHAGER)])
            .isEqualTo(2)
    }

    @Test
    fun `beregn antall metrikker per bransje, untatt ANDRE_BRANSJER med 0 for de bransjene uten metrikk`() {

        val innloggetMetrikkerTest = listOf(
            dummyInnloggetMetrikk(),
            dummyInnloggetMetrikk(kilde = Kilde.SAMTALESTØTTE, tidspunkt = _5_JUNI.atStartOfDay()),
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
                tidspunkt = _5_JUNI.atStartOfDay()
            )
        )

        val datagrunnlag = MottattIaTjenesterDatagrunnlag(
            innloggetMetrikker = innloggetMetrikkerTest,
            uinnloggetMetrikker = emptyList(),
            fraDato = _1_JANUAR_2021,
            tilDato = _21_JUNI_2021
        )

        val resultat: MottatteIaTjenesterPerBransjeOgKilde = datagrunnlag.mottatteIaTjenesterInnloggetPerBransjeOgKilde

        Assertions.assertThat(resultat[BransjePerKilde(Kilde.SYKEFRAVÆRSSTATISTIKK, ArbeidstilsynetBransje.BARNEHAGER)])
            .isEqualTo(2)
        Assertions.assertThat(resultat[BransjePerKilde(Kilde.SAMTALESTØTTE, ArbeidstilsynetBransje.BARNEHAGER)])
            .isEqualTo(1)
        Assertions.assertThat(resultat[BransjePerKilde(Kilde.SAMTALESTØTTE, ArbeidstilsynetBransje.ANLEGG)])
            .isEqualTo(1)
        Assertions.assertThat(resultat[BransjePerKilde(Kilde.SAMTALESTØTTE, ArbeidstilsynetBransje.ANDRE_BRANSJER)])
            .isNull()
    }

    @Test
    fun `beregn antall innlogget metrikker per dag for innlogget ia-tjenester`() {

        val innloggetMetrikkerTest = listOf(
            dummyInnloggetMetrikk(),
            dummyInnloggetMetrikk(tidspunkt = _1_MAI.atStartOfDay().plusHours(4)),
            dummyInnloggetMetrikk(tidspunkt = _1_MAI.atStartOfDay().plusHours(4), orgnr = "88888888"),
            dummyInnloggetMetrikk(tidspunkt = _5_JUNI.atStartOfDay()),
        )
        val datagrunnlag = MottattIaTjenesterDatagrunnlag(
            innloggetMetrikker = innloggetMetrikkerTest,
            uinnloggetMetrikker = emptyList(),
            fraDato = _1_JANUAR_2021,
            tilDato = _21_JUNI_2021
        )

        val resultat: Map<Month, Int> = datagrunnlag.antallInnloggetMetrikkerPerMåned

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
            MottattUinnloggetIaTjenesteMetrikk(Kilde.SAMTALESTØTTE, _1_MAI.atStartOfDay()),
            MottattUinnloggetIaTjenesteMetrikk(Kilde.SAMTALESTØTTE, _1_MAI.atStartOfDay().plusHours(4)),
            MottattUinnloggetIaTjenesteMetrikk(Kilde.SAMTALESTØTTE, _1_MAI.atStartOfDay().plusHours(4)),
            MottattUinnloggetIaTjenesteMetrikk(Kilde.SAMTALESTØTTE, _5_JUNI.atStartOfDay()),
        )
        val datagrunnlag = MottattIaTjenesterDatagrunnlag(
            innloggetMetrikker = emptyList(),
            uinnloggetMetrikker = uinnloggetMetrikkerTest,
            _1_JANUAR_2021,
            _21_JUNI_2021
        )

        val resultat: Map<Month, Int> = datagrunnlag.antallUinnloggetMetrikkerPerMåned

        Assertions.assertThat(resultat.keys.size).isEqualTo(6)
        Assertions.assertThat(resultat[Month.JANUARY]).isEqualTo(0)
        Assertions.assertThat(resultat[Month.FEBRUARY]).isEqualTo(0)
        Assertions.assertThat(resultat[Month.MARCH]).isEqualTo(0)
        Assertions.assertThat(resultat[Month.APRIL]).isEqualTo(0)
        Assertions.assertThat(resultat[Month.MAY]).isEqualTo(3)
        Assertions.assertThat(resultat[Month.JUNE]).isEqualTo(1)
    }
}