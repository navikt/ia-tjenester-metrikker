package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Month


internal class DatakatalogStatistikkTest {

    @Test
    fun `Antall måneder mellom to datoer`() {
        val fraDato = LocalDate.of(2021, 1, 1)
        val tilDato = LocalDate.of(2021, 4, 1)

        assertThat((fraDato månederTil fraDato)).isEqualTo(listOf(Month.JANUARY))
        assertThat((tilDato månederTil tilDato)).isEqualTo(listOf(Month.APRIL))
        assertThat((fraDato månederTil tilDato))
            .isEqualTo(listOf(Month.JANUARY, Month.FEBRUARY, Month.MARCH, Month.APRIL))
    }

    @Test
    fun `Antall måneder mellom to datoer, når første dag ikke er 1`() {
        val fraDato = LocalDate.of(2021, 1, 17)
        val tilDato = LocalDate.of(2021, 4, 1)

        assertThat((fraDato månederTil fraDato)).isEqualTo(listOf(Month.JANUARY))
        assertThat((tilDato månederTil tilDato)).isEqualTo(listOf(Month.APRIL))
        assertThat((fraDato månederTil tilDato))
            .isEqualTo(listOf(Month.JANUARY, Month.FEBRUARY, Month.MARCH, Month.APRIL))
    }


    @Test
    fun `Antall måneder mellom to datoer, når første dag ikke er 1, hverken på fra- eller tilDato`() {
        val fraDato = LocalDate.of(2021, 1, 17)
        val tilDato = LocalDate.of(2021, 4, 30)

        assertThat((fraDato månederTil fraDato)).isEqualTo(listOf(Month.JANUARY))
        assertThat((tilDato månederTil tilDato)).isEqualTo(listOf(Month.APRIL))
        assertThat((fraDato månederTil tilDato))
            .isEqualTo(listOf(Month.JANUARY, Month.FEBRUARY, Month.MARCH, Month.APRIL))
    }

    @Test
    fun `Antall måneder mellom to datoer spenner seg over flere år`() {
        val fraDato = LocalDate.of(2021, 1, 17)
        val tilDato = LocalDate.of(2022, 1, 30)

        assertThat((fraDato månederTil tilDato).size).isEqualTo(13)
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

