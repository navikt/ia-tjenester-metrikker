package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker

import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.til
import no.nav.arbeidsgiver.iatjenester.metrikker.repository.IaTjenesterMetrikkerRepository
import java.time.LocalDate
import java.time.Month

class MottattIaTjenesterDatagrunnlag(
    innloggetMetrikker: List<IaTjenesterMetrikkerRepository.MottattIaTjenesteMetrikk>,
    uinnloggetMetrikker: List<IaTjenesterMetrikkerRepository.MottattIaTjenesteMetrikk>,
    dagensDato: () -> LocalDate
) {

    private val fraStartDato = LocalDate.of(2021, 1, 1)
    private val gjeldendeMåneder: List<Month> = fraStartDato til dagensDato()


    val antallInnloggetMetrikkerPerMåned: Map<Month, Int> =
        beregnAntallMetrikkerPerMåned(gjeldendeMåneder, beregnAntallMetrikkerPerDag(innloggetMetrikker))
    val antallUinnloggetMetrikkerPerMåned: Map<Month, Int> =
        beregnAntallMetrikkerPerMåned(gjeldendeMåneder, beregnAntallMetrikkerPerDag(uinnloggetMetrikker))


    fun beregnAntallMetrikkerPerMåned(
        måneder: List<Month>,
        metrikkerPerDag: Map<LocalDate, Int>
    ): Map<Month, Int> {
        val metrikkerPerMåned: Map<Month, Collection<Int>> =
            måneder.map { it to metrikkerPerDag.filter { (key, value) -> key.month == it }.values }.toMap()
        val antallMetrikkerPerMåned: Map<Month, Int> =
            metrikkerPerMåned.mapValues { (_, antallMetrikker) -> antallMetrikker.sumBy { it } }

        return antallMetrikkerPerMåned
    }

    fun beregnAntallMetrikkerPerDag(
        mottattIaTjenesteMetrikker: List<IaTjenesterMetrikkerRepository.MottattIaTjenesteMetrikk>
    ): Map<LocalDate, Int> {
        return mottattIaTjenesteMetrikker.distinctBy {
            Pair(it.orgnr, it.tidspunkt.toLocalDate())
        }.groupingBy { it.tidspunkt.toLocalDate() }.eachCount()
    }

    fun gjeldendeMåneder() = gjeldendeMåneder
}

