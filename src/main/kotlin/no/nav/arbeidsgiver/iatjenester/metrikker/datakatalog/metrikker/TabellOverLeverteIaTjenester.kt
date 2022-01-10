package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker

import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.DatapakkeTabellBuilder
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.TabellHeader
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.månederOgÅrTil
import no.nav.arbeidsgiver.iatjenester.metrikker.repository.IaTjenesterMetrikkerRepository.MottattIaTjenesteMetrikk
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.IaTjenesteTilgjengelighet
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.IaTjenesteTilgjengelighet.INNLOGGET
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.IaTjenesteTilgjengelighet.UINNLOGGET
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

    private val tabelldata: List<Tabellcelle> =
        listOf(
            datagrunnlag.leverteInnloggedeIatjenester.map { Tabellcelle(it, INNLOGGET) },
            datagrunnlag.uinnloggetMetrikker.map { Tabellcelle(it, UINNLOGGET) }
        ).flatten()

    private val tabellcellerIDatagrunnlaget = tabelldata.groupingBy { it }

    private val allePermutasjonerAvTabellceller =
        (tabellFraDato månederOgÅrTil tabellTilDato).flatMap { tidspunkt ->
            listOf(
                TypeLevertTjeneste(SYKEFRAVÆRSSTATISTIKK, INNLOGGET),
                TypeLevertTjeneste(SAMTALESTØTTE, INNLOGGET),
                TypeLevertTjeneste(SAMTALESTØTTE, UINNLOGGET)
            )
                .map { kilde -> Tabellcelle(kilde, tidspunkt.år, tidspunkt.måned) }
        }

    private val alleTabellcellerSortert =
        allePermutasjonerAvTabellceller.sortedWith(
            compareBy(
                { it.måned },
                { it.typeLevertTjeneste })
        )

    val ontallLeverteIaTjenester = tellOppAntallLeverteIaTjenester()

    private fun tellOppAntallLeverteIaTjenester(): Map<Tabellcelle, Int> {
        return alleTabellcellerSortert.associateWith { 0 } + tabellcellerIDatagrunnlaget.eachCount()
    }

    private fun leverteIaTjenesterPerMånedPerApp(): List<List<Any>> =
        ontallLeverteIaTjenester
            .toList()
            .groupBy { it.first.måned }
            .mapValues { (_, antallLeverteTjenesterPerMåned) -> antallLeverteTjenesterPerMåned.map { it.second } }
            .map { (måned, antallTjenesterDenMåneden) ->
                listOf(måned.tilNorskTekstformat()) + antallTjenesterDenMåneden
            }

    private fun summerLeverteTjenesterPerAppPerÅr() =
        listOf("totalt") +
                ontallLeverteIaTjenester
                    .toList()
                    .groupBy { it.first.år to it.first.typeLevertTjeneste }
                    .map { (_, antallLeverteTjenester) -> antallLeverteTjenester.sumOf { it.second } }

    data class TypeLevertTjeneste(
        val kilde: Kilde,
        val tilgjenglighet: IaTjenesteTilgjengelighet,
    ) : Comparable<TypeLevertTjeneste> {
        override fun compareTo(other: TypeLevertTjeneste): Int = when {
            kilde < other.kilde -> -1
            kilde > other.kilde -> 1
            tilgjenglighet < other.tilgjenglighet -> -1
            tilgjenglighet > other.tilgjenglighet -> 1
            else -> 0
        }
    }

    data class Tabellcelle(
        val typeLevertTjeneste: TypeLevertTjeneste,
        val år: Int,
        val måned: Month,
    ) {
        constructor(
            mottattTjeneste: MottattIaTjenesteMetrikk,
            tilgjengelighet: IaTjenesteTilgjengelighet,
        ) : this(
            TypeLevertTjeneste(mottattTjeneste.kilde, tilgjengelighet),
            mottattTjeneste.tidspunkt.year,
            mottattTjeneste.tidspunkt.month
        )
    }

}