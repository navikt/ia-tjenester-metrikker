package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker

import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog._1_JANUAR_2021
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog._5_JUNI_2021
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.dummyInnloggetMetrikk
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.dummyUinnloggetMetrikk
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker.TabellOverLeverteIaTjenester.Tabellcelle
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.IaTjenesteTilgjengelighet.INNLOGGET
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.IaTjenesteTilgjengelighet.UINNLOGGET
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class TabellOverLeverteIaTjenesterTest {

    @Test
    fun `regnUtAntallLeverteTjenesterPerMånedOgÅr regner ut en verdi for hver tjenestetype hver måned over to år`() {
        val datagrunnlag = MottattIaTjenesterDatagrunnlag(
            innloggetMetrikker = emptyList(),
            uinnloggetMetrikker = emptyList(),
            fraDato = _1_JANUAR_2021,
            tilDato = _1_JANUAR_2021
        )

        val tabellceller =
            TabellOverLeverteIaTjenester(datagrunnlag).regnUtAntallLeverteTjenesterPerMånedOgÅr()

        // Tre apptyper (Samtalestøtte innlogget, Samtalestøtte uinnlogget, Sykefrværsstatistikk)
        // ganger 24 måneder = 72 tabellverdier
        Assertions.assertThat(tabellceller.size).isEqualTo(72)
    }

    @Test
    fun `regnUtAntallLeverteTjenesterPerMånedOgÅr mapper til riktig tabellcelle`() {
        val datagrunnlag = MottattIaTjenesterDatagrunnlag(
            innloggetMetrikker = listOf(
                dummyInnloggetMetrikk(),
                dummyInnloggetMetrikk(orgnr = "88888888")
            ),
            uinnloggetMetrikker = listOf(dummyUinnloggetMetrikk()),
            fraDato = _1_JANUAR_2021,
            tilDato = _5_JUNI_2021
        )

        val tabellceller =
            TabellOverLeverteIaTjenester(datagrunnlag).regnUtAntallLeverteTjenesterPerMånedOgÅr()

        val antallInnloggedeMetrikkerFørsteMai2021 =
            tabellceller[Tabellcelle(dummyInnloggetMetrikk(), INNLOGGET)]
        val antallUinnloggedeMetrikkerFørsteMai2021 =
            tabellceller[Tabellcelle(dummyUinnloggetMetrikk(), UINNLOGGET)]
        val antallInnloggedeMetrikkerFørsteJanuar2022 =
            tabellceller[Tabellcelle(
                dummyUinnloggetMetrikk(tidspunkt = _1_JANUAR_2021.atStartOfDay()),
                INNLOGGET
            )]

        Assertions.assertThat(antallInnloggedeMetrikkerFørsteMai2021).isEqualTo(2)
        Assertions.assertThat(antallUinnloggedeMetrikkerFørsteMai2021).isEqualTo(1)
        Assertions.assertThat(antallInnloggedeMetrikkerFørsteJanuar2022).isEqualTo(0)
    }

    @Test
    fun `regnUtAntallLeverteTjenesterPerMånedOgÅr teller ikke med duplikater innenfor samme dag`() {
        val datagrunnlag = MottattIaTjenesterDatagrunnlag(
            innloggetMetrikker = listOf(
                dummyInnloggetMetrikk(),
                dummyInnloggetMetrikk(),
                dummyInnloggetMetrikk(orgnr = "88888888")
            ),
            uinnloggetMetrikker = emptyList(),
            fraDato = _1_JANUAR_2021,
            tilDato = _5_JUNI_2021
        )

        val tabellceller =
            TabellOverLeverteIaTjenester(datagrunnlag).regnUtAntallLeverteTjenesterPerMånedOgÅr()

        val antallInnloggedeMetrikkerFørsteMai2021 =
            tabellceller[Tabellcelle(dummyInnloggetMetrikk(), INNLOGGET)]

        Assertions.assertThat(antallInnloggedeMetrikkerFørsteMai2021).isEqualTo(2)
    }
}