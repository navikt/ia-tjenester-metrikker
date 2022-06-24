package no.nav.arbeidsgiver.iatjenester.metrikker.observability

import no.nav.arbeidsgiver.iatjenester.metrikker.repository.IaTjenesterMetrikkerRepository
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class ObservabilityService(private val iaTjenesterMetrikkerRepository: IaTjenesterMetrikkerRepository) : Runnable {

    override fun run() {
        hentAntallMetrikkerMottattOgSkrivLogg()
    }

    fun fraDato(): LocalDate {
        return dagensDato().minusDays(2).atStartOfDay().toLocalDate()
    }

    fun dagensDato(): LocalDate {
        return LocalDate.now()
    }

    internal fun hentAntallMetrikkerMottattOgSkrivLogg() {
        val fraDato = fraDato()
        val innloggetMetrikker = iaTjenesterMetrikkerRepository.hentInnloggetMetrikker(fraDato)
        val uinnloggetMetrikker = iaTjenesterMetrikkerRepository.hentUinnloggetMetrikker(fraDato)

        sjekkAntallMetrikkerMottattOgSkrivLog(innloggetMetrikker.size, fraDato, "innlogget")
        sjekkAntallMetrikkerMottattOgSkrivLog(uinnloggetMetrikker.size, fraDato, "uinnlogget")
    }

    internal fun skrivErrorLog(typeMetrikk: String, fraDato: LocalDate) {
        log.error("Ingen $typeMetrikk metrikker mottatt siden '$fraDato'. " +
                "Sjekk log for å sikre at lagring i DB fungerer som den skal. " +
                "Dette kan også være en normal situasjon dersom vi har for lite trafikk på våre tjenester. ")
    }


    private fun sjekkAntallMetrikkerMottattOgSkrivLog(
        antallMetrikkerMottatt: Int,
        fraDato: LocalDate,
        typeMetrrikk: String
    ) {
        log.info("Antall $typeMetrrikk metrikker mottatt og lagret siden '$fraDato' er: ${antallMetrikkerMottatt}")

        if (antallMetrikkerMottatt == 0) {
            skrivErrorLog(typeMetrrikk, fraDato)
        }
    }
}
