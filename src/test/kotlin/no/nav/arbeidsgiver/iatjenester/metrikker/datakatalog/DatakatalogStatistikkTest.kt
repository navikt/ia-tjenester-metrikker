package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Month


internal class DatakatalogStatistikkTest {

    @Test
    fun `Antall måneder mellom to datoer`() {
        val fraDato = LocalDate.of(2021, 1, 1)
        val tilDato = LocalDate.of(2021, 4, 1)

        Assertions.assertThat((fraDato til fraDato)).isEqualTo(listOf(Month.JANUARY))
        Assertions.assertThat((tilDato til tilDato)).isEqualTo(listOf(Month.APRIL))
        Assertions.assertThat((fraDato til tilDato))
            .isEqualTo(listOf(Month.JANUARY, Month.FEBRUARY, Month.MARCH, Month.APRIL))
    }
}

