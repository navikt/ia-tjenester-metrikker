package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker.MottattIaTjenesterDatagrunnlag
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker.MottattIaTjenesterStatistikk
import no.nav.arbeidsgiver.iatjenester.metrikker.repository.IaTjenesterMetrikkerRepository
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDate.now
import java.time.Month
import java.time.temporal.ChronoUnit


@Component
class DatakatalogStatistikk(
    private val iaTjenesterMetrikkerRepository: IaTjenesterMetrikkerRepository,
    private val datakatalogKlient: DatakatalogKlient
) : Runnable {

    private val fraDato = LocalDate.of(2021, 1, 1)
    private val dagensDato = now()

    override fun run() {
        byggOgSendDatapakke(false)
    }

    internal fun byggOgSendDatapakke(erDebugAktivert: Boolean = false) {
        log.info("Starter jobb som sender statistikk til datakatalogen")
        log.info("Skal sende statistikk for målinger til og med ${dagensDato}")
        byggDatapakke().also {
            if (erDebugAktivert) {
                log("DatakatalogStatistikk").info(
                    "Sender følgende datapakke '${jacksonObjectMapper().writeValueAsString(it)}'"
                )
            }
            datakatalogKlient.sendDatapakke(it)
        }
        log.info("Har gjennomført jobb som sender statistikk til datakatalogen")
    }

    private fun datapakke(views: List<View>): Datapakke =
        Datapakke(
            title = "Digitale IA-tjenester",
            type = "datapackage",
            description = "Mottatte digitale ia-tjenester-metrikker",
            views = views,
            name = "ia-tjenester-metrikker-statistikk",
            uri = "",
            url = "",
            team = "Team IA"
        )

    private fun byggDatapakke(): Datapakke = (Pair(
        iaTjenesterMetrikkerRepository.hentUinnloggetMetrikker(fraDato),
        iaTjenesterMetrikkerRepository.hentInnloggetMetrikker(fraDato)
    ))
        .let { (uinnloggedeMetrikker, innloggedeMetrikker) ->
            MottattIaTjenesterStatistikk(
                MottattIaTjenesterDatagrunnlag(
                    innloggedeMetrikker,
                    uinnloggedeMetrikker,
                    fraDato,
                    dagensDato
                )
            ).let {
                datapakke(it.views())
            }
        }
}

infix fun LocalDate.til(tilDato: LocalDate): List<Month> {
    val førstDagIHverMåned: List<LocalDate> = ChronoUnit.MONTHS.between(this, tilDato)
        .let { antallMåneder ->
            (0..antallMåneder).map { this.plusMonths(it) }
        }
    return førstDagIHverMåned.map { it.month }
}
