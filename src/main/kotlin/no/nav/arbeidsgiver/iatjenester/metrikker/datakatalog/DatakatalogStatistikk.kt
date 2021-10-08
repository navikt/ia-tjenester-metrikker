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

    private val fraDato: LocalDate = startDato()
    private var tilDato: LocalDate = dagensDato()

    fun startDato(): LocalDate {
        return LocalDate.of(2021, Month.MARCH, 17)
    }

    fun dagensDato(): LocalDate {
        return now()
    }

    override fun run() {
        byggOgSendDatapakke(false)
    }

    internal fun byggOgSendDatapakke(erDebugAktivert: Boolean = false) {
        tilDato = dagensDato()
        log.info("Starter jobb som sender statistikk til datakatalogen")
        log.info("Skal sende statistikk for målinger til og med ${tilDato}")

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

    private fun opprettDatapakke(views: List<View>): Datapakke {
        val description = """
            __Statistikken på denne siden viser antall digitale IA-tjenester fra følgende tjenester:__
            - Samtalestøtte til arbeidsgiver (krever ikke innlogging) 
            - Sykefraværsstatistikk til arbeidsgiver (krever innlogging)
            
            __En digital IA-tjeneste telles når en bruker har benyttet seg av innholdet.__
            
            Som hovedregel betyr dette at brukeren har *klikket på noe*, *skrevet noe* eller *åpnet noe* på siden. 
            Det er ikke tilstrekkelig å brukeren kun har besøkt forsiden. 
        
            Unntak:
            - Hvis brukeren klikker på link til Samtalestøtten inne fra Sykefraværsstatistikk, så vil dette *ikke* telle 
            som en levert IA-tjeneste fra Sykefraværsstatistikk. 
            """.trimIndent()

        return Datapakke(
            title = "Digitale IA-tjenester",
            type = "datapackage",
            description = description,
            views = views,
            name = "ia-tjenester-metrikker-statistikk",
            uri = "",
            url = "",
            team = "Team IA"
        )
    }

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
                    tilDato
                )
            ).let {
                opprettDatapakke(it.opprettViews())
            }
        }
}

infix fun LocalDate.månederTil(tilDato: LocalDate): List<Month> {
    val startDato: LocalDate = this.withDayOfMonth(1)
    val alleFørsteDagIHverMåned: List<LocalDate> = ChronoUnit.MONTHS.between(startDato, tilDato)
        .let { antallMåneder ->
            (0..antallMåneder).map { this.plusMonths(it) }
        }
    return alleFørsteDagIHverMåned.map { it.month }
}
