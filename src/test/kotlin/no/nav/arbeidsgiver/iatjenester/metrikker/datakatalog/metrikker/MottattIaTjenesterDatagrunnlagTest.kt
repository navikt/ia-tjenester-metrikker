package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker

import no.nav.arbeidsgiver.iatjenester.metrikker.repository.IaTjenesterMetrikkerRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertTrue

internal class MottattIaTjenesterDatagrunnlagTest {

    @Test
    fun `test test`() {
        assertTrue(true)
    }

    @Test
    fun `beregn antall innlogget metrikker per dag`() {
        val vilkårligDato = LocalDate.of(2021, 5, 5)

        val innloggetMetrikkerTest = listOf(
            IaTjenesterMetrikkerRepository.MottattIaTjenesteMetrikk(true, "999999999", vilkårligDato.atStartOfDay()),
            IaTjenesterMetrikkerRepository.MottattIaTjenesteMetrikk(true, "999999999", vilkårligDato.atStartOfDay().plusHours(4)),
            IaTjenesterMetrikkerRepository.MottattIaTjenesteMetrikk(true, "888888888", vilkårligDato.atStartOfDay().plusHours(4)),
            IaTjenesterMetrikkerRepository.MottattIaTjenesteMetrikk(true, "999999999", vilkårligDato.atStartOfDay().plusDays(6)),
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
            datagrunnlag.beregnAntallMetrikkerPerDag(innloggetMetrikkerTest, true)

        Assertions.assertThat(resultat.keys.size).isEqualTo(2)
        Assertions.assertThat(resultat.get(vilkårligDato.plusDays(6))).isEqualTo(1)
       // Assertions.assertThat(resultat.get(vilkårligDato)).isEqualTo(2) TODO: filterer på unik orgnr
    }
}