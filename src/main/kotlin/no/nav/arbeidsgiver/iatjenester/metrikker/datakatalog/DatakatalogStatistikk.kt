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
import java.util.stream.Collectors


@Component
class DatakatalogStatistikk(
    private val iaTjenesterMetrikkerRepository: IaTjenesterMetrikkerRepository,
    private val datakatalogKlient: DatakatalogKlient
) : Runnable {

    private var fraDato: LocalDate = startDato()
    private var tilDato: LocalDate = dagensDato()

    fun startDato(): LocalDate {
        return dagensDato().minusMonths(12).withDayOfMonth(1)
    }

    fun dagensDato(): LocalDate {
        return now()
    }

    override fun run() {
        byggOgSendDatapakke(false)
    }

    internal fun byggOgSendDatapakke(erDebugAktivert: Boolean = false) {
        fraDato = startDato()
        tilDato = dagensDato()
        log.info("Starter jobb som sender statistikk til datakatalogen")
        log.info("Skal sende statistikk for målinger til og med $tilDato")

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
            **Statistikken på denne siden viser antall digitale IA-tjenester fra følgende tjenester:**
            - Samtalestøtte til arbeidsgiver
            - Sykefraværsstatistikk til arbeidsgiver
            
            Dataregistreringen startet mars 2021. 
            
            **En digital IA-tjeneste telles når en bruker har benyttet seg av innholdet i tjenesten.**
            
            Som hovedregel betyr dette at brukeren har *klikket på noe*, *skrevet noe* eller *åpnet noe* på siden. 
            Det er ikke tilstrekkelig at brukeren kun har besøkt forsiden.
            
            **Vi har hatt en feil på telling av digitale IA-tjenester i perioden 19. mai til 1. juli 2022.**
            **Det er ca 1000 digitale IA-tjenester som ikke er kommet med i statistikken.**
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

infix fun LocalDate.dagerTil(tilDato: LocalDate): List<LocalDate> =
    this.datesUntil(tilDato.plusDays(1)).collect(Collectors.toList())

infix fun LocalDate.månederOgÅrTil(tilDato: LocalDate): List<MånedOgÅr> =
    (this dagerTil tilDato).map { MånedOgÅr(it.year, it.month) }.distinct()

data class MånedOgÅr(val år: Int, val måned: Month)
