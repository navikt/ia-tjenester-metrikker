package no.nav.arbeidsgiver.iatjenester.metrikker.observability

import no.nav.arbeidsgiver.iatjenester.metrikker.repository.IaTjenesterMetrikkerRepository
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class ObservabilityService(private val iaTjenesterMetrikkerRepository: IaTjenesterMetrikkerRepository) : Runnable {

    override fun run() {
        hentAntallMetrikkerLagretOgSendLoggMelding()
    }

    fun startDato(): LocalDate {
        return dagensDato().minusDays(2)
    }

    fun dagensDato(): LocalDate {
        return LocalDate.now()
    }

    internal fun hentAntallMetrikkerLagretOgSendLoggMelding() {
        val startDato = startDato()
        val uinnloggetMetrikker = iaTjenesterMetrikkerRepository.hentUinnloggetMetrikker(startDato)
        log.info("Antall uinnlogget metrikker mottatt og lagret siden '$startDato' er: ${uinnloggetMetrikker.size}")

        if (uinnloggetMetrikker.isEmpty()) {
            log.error("Ingen uinnlogget metrikk siden '$startDato'. Sjekk om det er normalt")
        }

        val innloggetMetrikker = iaTjenesterMetrikkerRepository.hentInnloggetMetrikker(dagensDato())
        log.info("Antall innlogget metrikker mottatt og lagret siden '$startDato' er: ${innloggetMetrikker.size}")

        if (innloggetMetrikker.isEmpty()) {
            log.error("Ingen innlogget metrikk siden '$startDato'. Sjekk om det er normalt")
        }

    }
}
