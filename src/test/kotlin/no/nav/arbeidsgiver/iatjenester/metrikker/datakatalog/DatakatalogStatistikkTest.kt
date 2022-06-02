package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Month
import java.time.Month.FEBRUARY


internal class DatakatalogStatistikkTest {

    private val januar2021 = MånedOgÅr(2021, Month.JANUARY)
    private val februar2021 = MånedOgÅr(2021, FEBRUARY)
    private val mars2021 = MånedOgÅr(2021, Month.MARCH)

    @Test
    fun `Antall måneder mellom to datoer`() {
        val fraDato = LocalDate.of(2021, 1, 1)
        val tilDato = LocalDate.of(2021, 3, 1)

        assertThat((fraDato månederOgÅrTil fraDato)).isEqualTo(listOf(januar2021))
        assertThat((fraDato månederOgÅrTil tilDato))
            .isEqualTo(listOf(januar2021, februar2021, mars2021))
    }

    @Test
    fun `tilDato før fraDato gir feilmelding`() {
        val fraDato = LocalDate.of(2021, 3, 1)
        val tilDato = LocalDate.of(2021, 1, 17)

        // DENNE BØR GI FEILMELDING
    }


    @Test
    fun `Antall måneder mellom to datoer, når første dag ikke er 1`() {
        val fraDato = LocalDate.of(2021, 1, 17)
        val tilDato = LocalDate.of(2021, 4, 1)

        assertThat((fraDato månederOgÅrTil fraDato)).isEqualTo(listOf(Month.JANUARY))
        assertThat((tilDato månederOgÅrTil tilDato)).isEqualTo(listOf(Month.APRIL))
        assertThat((fraDato månederOgÅrTil tilDato))
            .isEqualTo(listOf(Month.JANUARY, Month.FEBRUARY, Month.MARCH, Month.APRIL))
    }


    @Test
    fun `Antall måneder mellom to datoer, når første dag ikke er 1, hverken på fra- eller tilDato`() {
        val fraDato = LocalDate.of(2021, 1, 17)
        val tilDato = LocalDate.of(2021, 4, 30)

        assertThat((fraDato månederOgÅrTil fraDato)).isEqualTo(listOf(Month.JANUARY))
        assertThat((tilDato månederOgÅrTil tilDato)).isEqualTo(listOf(Month.APRIL))
        assertThat((fraDato månederOgÅrTil tilDato))
            .isEqualTo(listOf(Month.JANUARY, Month.FEBRUARY, Month.MARCH, Month.APRIL))
    }

    @Test
    fun `Antall måneder mellom to datoer spenner seg over flere år`() {
        val fraDato = LocalDate.of(2021, 1, 17)
        val tilDato = LocalDate.of(2022, 1, 30)

        assertThat((fraDato månederOgÅrTil tilDato).size).isEqualTo(13)
    }

    @Test
    fun `dager 1`() {
        val fraDato = LocalDate.of(2021, 1, 2)
        val tilDato = LocalDate.of(2021, 1, 3)

        assertThat(fraDato dagerTil tilDato).isEqualTo(listOf(fraDato, tilDato))
    }

    @Test
    fun `dager 2`() {
        val fraDato = LocalDate.of(2021, 1, 28)
        val tilDato = LocalDate.of(2021, 2, 2)

        assertThat((fraDato dagerTil tilDato).size).isEqualTo(6)
    }
}

