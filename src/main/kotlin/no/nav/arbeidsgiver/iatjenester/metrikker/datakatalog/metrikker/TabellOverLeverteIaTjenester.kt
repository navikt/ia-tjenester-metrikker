package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker

import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.DatapakkeTabellBuilder
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.TabellHeader
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.månederOgÅrTil
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.Kilde
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.Kilde.SAMTALESTØTTE
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.Kilde.SYKEFRAVÆRSSTATISTIKK
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.tilNorskTekstformat
import java.time.LocalDate
import java.time.Month

class TabellOverLeverteIaTjenester(private val datagrunnlag: MottattIaTjenesterDatagrunnlag) {

    private val tabellFraDato = LocalDate.of(2021, 1, 1)
    private val tabellTilDato = LocalDate.of(2022, 12, 31)

    fun lagTabellOverLeverteIaTjenester() =
        DatapakkeTabellBuilder(
            headere = listOf(
                TabellHeader(""),
                TabellHeader("Sykefraværsstatistikk", colspan = 2),
                TabellHeader("Samtalestøtte (innlogget)", colspan = 2),
                TabellHeader("Samtalestøtte (uinnlogget)", colspan = 2)
            )
        )
            .leggTilRad(listOf("", "2021", "2022", "2021", "2022", "2021", "2022"), uthevet = true)
            .leggTilRader(leverteIaTjenesterPerMånedPerApp())
            .leggTilRad(summerLeverteTjenesterPerAppPerÅr(), uthevet = true)
            .build()

    private fun tabelldataAntallLeverteIaTjenester(): Map<Grupperingsverdier, Int> {

        val kombinasjonerIDatagrunnlaget = datagrunnlag.innloggetMetrikker.groupingBy {
            Grupperingsverdier(
                kilde = it.kilde,
                år = it.tidspunkt.year,
                måned = it.tidspunkt.month,
            )
        }

        val alleKombinasjonerITabell =
            (tabellFraDato månederOgÅrTil tabellTilDato).flatMap { tidspunkt ->
                listOf(SYKEFRAVÆRSSTATISTIKK, SAMTALESTØTTE)
                    .map { kilde -> Grupperingsverdier(kilde, tidspunkt.år, tidspunkt.måned) }
            }.sortedWith(compareBy({ it.måned }, { it.kilde }))

        return alleKombinasjonerITabell.associateWith { 0 } + kombinasjonerIDatagrunnlaget.eachCount()
    }

    private fun leverteIaTjenesterPerMånedPerApp(): List<List<Any>> = listOf(
        tabelldataAntallLeverteIaTjenester()
            .toList()
            .groupBy { it.first.måned }
            .mapValues { (_, antallLeverteTjenesterPerMåned) -> antallLeverteTjenesterPerMåned.map { it.second } }
            .map { (måned, antallTjenesterDenMåneden) ->
                listOf(måned.tilNorskTekstformat()) + antallTjenesterDenMåneden
            }
    )

    private fun summerLeverteTjenesterPerAppPerÅr() =
        listOf("totalt") +
                tabelldataAntallLeverteIaTjenester()
                    .toList()
                    .groupBy { it.first.år to it.first.kilde }
                    .map { (_, antallLeverteTjenester) -> antallLeverteTjenester.sumOf { it.second } }

    data class Grupperingsverdier(val kilde: Kilde, val år: Int, val måned: Month)
}