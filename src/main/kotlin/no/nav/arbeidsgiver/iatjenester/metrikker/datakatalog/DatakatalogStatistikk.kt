package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog

import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker.MottattIaTjenesterDatagrunnlag
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker.MottattIaTjenesterStatistikk
import no.nav.arbeidsgiver.iatjenester.metrikker.repository.IaTjenesterMetrikkerRepository
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log
import java.time.LocalDate
import java.time.Month
import java.time.temporal.ChronoUnit


class DatakatalogStatistikk(
    private val iaTjenesterMetrikkerRepository: IaTjenesterMetrikkerRepository,
    private val datakatalogKlient: DatakatalogKlient,
    private val dagensDato: () -> LocalDate
) : Runnable {

    private val målingerStartet = LocalDate.of(2021, 6, 1)

    override fun run() {
        log.info("Starter jobb som sender statistikk til datakatalogen")
        log.info("Skal sende statistikk for målinger til og med ${dagensDato}")
        byggDatapakke().also {
            datakatalogKlient.sendDatapakke(it)
        }
        log.info("Har gjennomført jobb som sender statistikk til datakatalogen")
    }

    private fun datapakke(views: List<View>): Datapakke =
        Datapakke(
            title = "IA-tjenester metrikker",
            type = "",
            description = "Vise mottatt ia-tjenester",
            views = views,
            name = "",
            uri = "",
            url = "",
            team = ""
        )

    private fun byggDatapakke(): Datapakke = (
            iaTjenesterMetrikkerRepository.hentUinnloggetMetrikker(målingerStartet) to
                    iaTjenesterMetrikkerRepository.hentInnloggetMetrikker(målingerStartet))
        .let { (uinnloggedeMetrikker, innloggedeMetrikker) ->
            val listOf: List<MottattIaTjenesterStatistikk> = listOf(
                MottattIaTjenesterStatistikk(
                    MottattIaTjenesterDatagrunnlag( // HER er datagrunnlaget
                        innloggedeMetrikker,
                        uinnloggedeMetrikker,
                        dagensDato
                    )
                )
            )
            listOf.let {
                val flatMap: List<View> = it.flatMap(DatakatalogData::views)
                datapakke(flatMap)
            }
        }
}

infix fun LocalDate.til(tilDato: LocalDate): List<LocalDate> = ChronoUnit.DAYS.between(this, tilDato)
    .let { antallDager ->
        (0..antallDager).map { this.plusDays(it) }
    }

infix fun Month.til(tilMonth: Month): List<Month> =
    listOf(this.value, tilMonth.value).map { value -> Month.of(value) }


