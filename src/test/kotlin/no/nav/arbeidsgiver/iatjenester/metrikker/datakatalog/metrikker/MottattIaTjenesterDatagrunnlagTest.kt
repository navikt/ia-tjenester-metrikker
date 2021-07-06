package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker

import no.nav.arbeidsgiver.iatjenester.metrikker.repository.IaTjenesterMetrikkerRepository.MottattIaTjenesteMetrikk
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDate.now
import java.time.Month

internal class MottattIaTjenesterDatagrunnlagTest {

    @Test
    fun `beregn antall innlogget metrikker per måned summerer antall mottatt metrikker per måned`() {
        val datagrunnlag = MottattIaTjenesterDatagrunnlag(
            innloggetMetrikker = emptyList(),
            uinnloggetMetrikker = emptyList()
        ) { now() }

        val resultat =
            datagrunnlag.beregnAntallMetrikkerPerMåned(
                listOf(Month.FEBRUARY, Month.MARCH, Month.APRIL),
                mapOf(
                    LocalDate.of(2021, 2, 5) to 15,
                    LocalDate.of(2021, 2, 6) to 9,
                    LocalDate.of(2021, 3, 5) to 5,
                    LocalDate.of(2021, 4, 5) to 44
                )
            )

        Assertions.assertThat(resultat.keys.size).isEqualTo(3)
        Assertions.assertThat(resultat[Month.FEBRUARY]).isEqualTo(24)
        Assertions.assertThat(resultat[Month.MARCH]).isEqualTo(5)
        Assertions.assertThat(resultat[Month.APRIL]).isEqualTo(44)
    }

    @Test
    fun `beregn antall innlogget metrikker per dag`() {
        val vilkårligDato = LocalDate.of(2021, 5, 5)

        val innloggetMetrikkerTest = listOf(
            MottattIaTjenesteMetrikk(true, "999999999", vilkårligDato.atStartOfDay()),
            MottattIaTjenesteMetrikk(true, "999999999", vilkårligDato.atStartOfDay().plusHours(4)),
            MottattIaTjenesteMetrikk(true, "888888888", vilkårligDato.atStartOfDay().plusHours(4)),
            MottattIaTjenesteMetrikk(true, "999999999", vilkårligDato.atStartOfDay().plusDays(6)),
        )

        val datagrunnlag = MottattIaTjenesterDatagrunnlag(
            innloggetMetrikker = innloggetMetrikkerTest,
            uinnloggetMetrikker = emptyList(),
        ) {
            LocalDate.of(
                2021,
                5,
                6
            )
        }

        val resultat =
            datagrunnlag.beregnAntallMetrikkerPerDag(innloggetMetrikkerTest)

        Assertions.assertThat(resultat.keys.size).isEqualTo(2)
        Assertions.assertThat(resultat.get(vilkårligDato.plusDays(6))).isEqualTo(1)
        Assertions.assertThat(resultat.get(vilkårligDato)).isEqualTo(2)
    }
}