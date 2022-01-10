package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker

import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.DatapakkeTabellBuilder
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.TabellHeader
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.månederOgÅrTil
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.IaTjenesteTilgjengelighet.INNLOGGET
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.IaTjenesteTilgjengelighet.UINNLOGGET
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.Kilde.SAMTALESTØTTE
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.Kilde.SYKEFRAVÆRSSTATISTIKK
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.TypeLevertTjeneste
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

    private fun TabellverdierAntallLeverteIaTjenester(): Map<Tabellcelle, Int> {
        val tabelldata: List<Tabellcelle> =
            listOf(
                datagrunnlag.leverteInnloggedeIatjenester.map {
                    Tabellcelle(
                        typeLevertTjeneste = TypeLevertTjeneste(it.kilde, INNLOGGET),
                        år = it.tidspunkt.year,
                        måned = it.tidspunkt.month
                    )
                },
                datagrunnlag.uinnloggetMetrikker.map {
                    Tabellcelle(
                        typeLevertTjeneste = TypeLevertTjeneste(it.kilde, UINNLOGGET),
                        år = it.tidspunkt.year,
                        måned = it.tidspunkt.month
                    )
                }).flatten()

        val tabellverdierIDatagrunnlaget = tabelldata.groupingBy { it }

        val alleTabellceller =
            (tabellFraDato månederOgÅrTil tabellTilDato).flatMap { tidspunkt ->
                listOf(
                    TypeLevertTjeneste(SYKEFRAVÆRSSTATISTIKK, INNLOGGET),
                    TypeLevertTjeneste(SAMTALESTØTTE, INNLOGGET),
                    TypeLevertTjeneste(SAMTALESTØTTE, UINNLOGGET)
                )
                    .map { kilde -> Tabellcelle(kilde, tidspunkt.år, tidspunkt.måned) }
            }.sortedWith(compareBy({ it.måned }, { it.typeLevertTjeneste.kilde }))

        return alleTabellceller.associateWith { 0 } + tabellverdierIDatagrunnlaget.eachCount()
    }

    private fun leverteIaTjenesterPerMånedPerApp(): List<List<Any>> =
        TabellverdierAntallLeverteIaTjenester()
            .toList()
            .groupBy { it.first.måned }
            .mapValues { (_, antallLeverteTjenesterPerMåned) -> antallLeverteTjenesterPerMåned.map { it.second } }
            .map { (måned, antallTjenesterDenMåneden) ->
                listOf(måned.tilNorskTekstformat()) + antallTjenesterDenMåneden
            }


    private fun summerLeverteTjenesterPerAppPerÅr() =
        listOf("totalt") +
                TabellverdierAntallLeverteIaTjenester()
                    .toList()
                    .groupBy { it.first.år to it.first.typeLevertTjeneste }
                    .map { (_, antallLeverteTjenester) -> antallLeverteTjenester.sumOf { it.second } }

    data class Tabellcelle(
        val typeLevertTjeneste: TypeLevertTjeneste,
        val år: Int,
        val måned: Month,
    )
}