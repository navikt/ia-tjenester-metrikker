package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker

import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.til
import no.nav.arbeidsgiver.iatjenester.metrikker.repository.IaTjenesterMetrikkerRepository
import java.time.LocalDate

class MottattIaTjenesterDatagrunnlag(
    innloggetMetrikker: List<IaTjenesterMetrikkerRepository.MottattIaTjenesteMetrikk>,
    uinnloggetMetrikker: List<IaTjenesterMetrikkerRepository.MottattIaTjenesteMetrikk>,
    dagensDato: () -> LocalDate
) {

    private val fraStartDato = LocalDate.of(2021, 1, 1)
    private val gjeldendeDatoer = fraStartDato til dagensDato()
    private val antallInnloggetMetrikkerPerDag: Map<LocalDate, Int> =
        beregnAntallMetrikkerPerDag(innloggetMetrikker, true) // TODO: fjern boolean
    private val antallUinnloggetMetrikkerPerDag: Map<LocalDate, Int> =
        beregnAntallMetrikkerPerDag(uinnloggetMetrikker, false)


    fun beregnAntallMetrikkerPerDag(
        innloggetMetrikker: List<IaTjenesterMetrikkerRepository.MottattIaTjenesteMetrikk>,
        erInnlogget: Boolean
    ): Map<LocalDate, Int> =
        innloggetMetrikker.filter { it.erInnlogget == erInnlogget }.groupingBy { it.tidspunkt.toLocalDate() }.eachCount()


    fun hentAntallMottatInnlogget(dato: LocalDate) = antallInnloggetMetrikkerPerDag[dato] ?: 0

    fun hentAntallMottatUinnlogget(dato: LocalDate) = antallUinnloggetMetrikkerPerDag[dato] ?: 0


    fun gjeldendeDatoer() = gjeldendeDatoer
}
