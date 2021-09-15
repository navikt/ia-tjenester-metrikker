package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker

import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.til
import no.nav.arbeidsgiver.iatjenester.metrikker.repository.IaTjenesterMetrikkerRepository
import java.time.LocalDate
import java.time.Month

class MottattIaTjenesterDatagrunnlag(
    innloggetMetrikker: List<IaTjenesterMetrikkerRepository.MottattInnloggetIaTjenesteMetrikk>,
    uinnloggetMetrikker: List<IaTjenesterMetrikkerRepository.MottattUinnloggetIaTjenesteMetrikk>,
    fraDato: LocalDate,
    tilDato: LocalDate
) {
    val startDate: LocalDate = fraDato
    val gjelendeÅr = fraDato.year
    val gjeldendeMåneder: List<Month> = fraDato til tilDato

    val antallInnloggetMetrikkerPerMåned: Map<Month, Int> =
        beregnAntallMetrikkerPerMåned(
            gjeldendeMåneder,
            beregnAntallMetrikkerPerDag(fjernDupliserteMetrikkerSammeDag(innloggetMetrikker))
        )
    val antallUinnloggetMetrikkerPerMåned: Map<Month, Int> =
        beregnAntallMetrikkerPerMåned(
            gjeldendeMåneder,
            beregnAntallMetrikkerPerDag(uinnloggetMetrikker)
        )

    val totalInnloggetMetrikker: Int = innloggetMetrikker.size
    val totalUinnloggetMetrikker: Int = uinnloggetMetrikker.size
    val totalUnikeBedrifterPerDag: Int =
        beregnAntallMetrikkerPerDag(fjernDupliserteMetrikkerSammeDag(innloggetMetrikker)).values.sum()


    fun beregnAntallMetrikkerPerMåned(
        måneder: List<Month>,
        metrikkerPerDag: Map<LocalDate, Int>
    ): Map<Month, Int> {
        val metrikkerPerMåned: Map<Month, Collection<Int>> =
            måneder.map { it to metrikkerPerDag.filter { (key, _) -> key.month == it }.values }.toMap()
        val antallMetrikkerPerMåned: Map<Month, Int> =
            metrikkerPerMåned.mapValues { (_, antallMetrikker) -> antallMetrikker.sumBy { it } }

        return antallMetrikkerPerMåned
    }

    fun fjernDupliserteMetrikkerSammeDag(
        mottattIaTjenesteMetrikker: List<IaTjenesterMetrikkerRepository.MottattInnloggetIaTjenesteMetrikk>
    ): List<IaTjenesterMetrikkerRepository.MottattIaTjenesteMetrikk> {
        return mottattIaTjenesteMetrikker.distinctBy {
            Pair(it.orgnr, it.tidspunkt.toLocalDate())
        }
    }

    fun beregnAntallMetrikkerPerDag(
        mottattIaTjenesteMetrikker: List<IaTjenesterMetrikkerRepository.MottattIaTjenesteMetrikk>
    ): Map<LocalDate, Int> {
        return mottattIaTjenesteMetrikker.groupingBy { it.tidspunkt.toLocalDate() }.eachCount()
    }

    fun gjeldendeMåneder() = gjeldendeMåneder
}

