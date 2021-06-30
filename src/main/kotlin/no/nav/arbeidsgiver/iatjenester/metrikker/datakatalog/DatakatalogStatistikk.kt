package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog

import kscience.plotly.Plot
import kscience.plotly.bar
import kscience.plotly.layout
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker.MottattIaTjenesterDatagrunnlag
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker.MottattIaTjenesterStatistikk
import no.nav.arbeidsgiver.iatjenester.metrikker.repository.IaTjenesterMetrikkerRepository
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log
import java.time.LocalDate
import java.time.temporal.ChronoUnit


class DatakatalogStatistikk(
    private val iaTjenesterMetrikkerRepository: IaTjenesterMetrikkerRepository, private val datakatalogKlient: DatakatalogKlient,
    private val dagensDato: () -> LocalDate
) : Runnable {

    private val målingerStartet = LocalDate.of(2021, 6, 1)

    override fun run() {
        log.info("Starter jobb som sender statistikk til datakatalogen")
        log.info("Skal sende statistikk for målinger til og med ${dagensDato}")
        plotlydataOgDataPakke().also { (plotly, datapakke) ->
            datakatalogKlient.sendDatapakke(datapakke)
            datakatalogKlient.sendPlotlyFilTilDatavarehus(plotly)
        }

        log.info("Har gjennomført jobb som sender statistikk til datakatalogen")
    }

    private fun datapakke(views: List<View>) =
        Datapakke(
            title = "IA-tjenester metrikker",
            description = "Vise mottatt ia-tjenester",
            resources = emptyList(),
            views = views
        )

    private fun plotlydataOgDataPakke() : Pair<List<Pair<String, String>>, Datapakke> = (
            iaTjenesterMetrikkerRepository.hentUinnloggetMetrikker(målingerStartet) to
                    iaTjenesterMetrikkerRepository.hentInnloggetMetrikker(målingerStartet))
        .let { (uinnloggedeMetrikker, innloggedeMetrikker) ->
            listOf(
                MottattIaTjenesterStatistikk(MottattIaTjenesterDatagrunnlag(innloggedeMetrikker, uinnloggedeMetrikker, dagensDato))
            ).let {
                it.flatMap(DatakatalogData::plotlyFiler) to it.flatMap(DatakatalogData::views).let(this::datapakke)
            }
        }

    private fun plotlydataOgDataPakke2() : Pair<List<Pair<String, String>>, Datapakke> = (
            iaTjenesterMetrikkerRepository.hentUinnloggetMetrikker(målingerStartet) to
                    iaTjenesterMetrikkerRepository.hentInnloggetMetrikker(målingerStartet))
        .let { (uinnloggedeMetrikker, innloggedeMetrikker) ->
            listOf(
                MottattIaTjenesterStatistikk(MottattIaTjenesterDatagrunnlag(innloggedeMetrikker, uinnloggedeMetrikker, dagensDato))
            ).let {
                val generertDatapakke: Datapakke = it.flatMap(DatakatalogData::views).let(this::datapakke)
                val pair: Pair<List<Pair<String, String>>, Datapakke> =
                    it.flatMap(DatakatalogData::plotlyFiler) to generertDatapakke
                pair
            }
        }

    private fun plotlydataOgDataPakke3() : Datapakke = (
            iaTjenesterMetrikkerRepository.hentUinnloggetMetrikker(målingerStartet) to
                    iaTjenesterMetrikkerRepository.hentInnloggetMetrikker(målingerStartet))
        .let { (uinnloggedeMetrikker, innloggedeMetrikker) ->
            listOf(
                MottattIaTjenesterStatistikk(MottattIaTjenesterDatagrunnlag(innloggedeMetrikker, uinnloggedeMetrikker, dagensDato))
            ).let {
                val flatMap: List<View> = it.flatMap(DatakatalogData::views)
                val genererteDatapakke: Datapakke = flatMap.let(this::datapakke)
                genererteDatapakke
            }
        }

}

fun Plot.getLayout(yTekst: String) {
    layout {
        bargap = 0.1
        title {
            text = ""
            font {
                size = 20
            }
        }
        xaxis {
            title {
                text = "Dato"
                font {
                    size = 16
                }
            }
        }
        yaxis {
            title {
                text = yTekst
                font {
                    size = 16
                }
            }
        }
    }
}

fun Plot.lagBar(description: String, datoer: List<LocalDate>, hentVerdi: (LocalDate) -> Int) = bar {
    x.strings = datoer.map { it.toString() }
    y.numbers = datoer.map { hentVerdi(it) }
    name = description
}

infix fun LocalDate.til(tilDato: LocalDate) = ChronoUnit.DAYS.between(this, tilDato)
    .let { antallDager ->
        (0..antallDager).map { this.plusDays(it) }
    }
