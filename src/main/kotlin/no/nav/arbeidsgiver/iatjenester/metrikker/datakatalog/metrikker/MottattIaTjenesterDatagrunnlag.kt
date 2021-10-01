package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker

import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.Næring.ArbeidstilsynetBransje
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.månederTil
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.Kilde
import no.nav.arbeidsgiver.iatjenester.metrikker.repository.IaTjenesterMetrikkerRepository
import no.nav.arbeidsgiver.iatjenester.metrikker.repository.IaTjenesterMetrikkerRepository.MottattInnloggetIaTjenesteMetrikk
import no.nav.arbeidsgiver.iatjenester.metrikker.repository.IaTjenesterMetrikkerRepository.MottattUinnloggetIaTjenesteMetrikk
import java.time.LocalDate
import java.time.Month

class MottattIaTjenesterDatagrunnlag(
    innloggetMetrikker: List<MottattInnloggetIaTjenesteMetrikk>,
    uinnloggetMetrikker: List<MottattUinnloggetIaTjenesteMetrikk>,
    fraDato: LocalDate,
    tilDato: LocalDate
) {
    val startDate: LocalDate = fraDato
    val gjelendeÅr = fraDato.year
    val gjeldendeMåneder: List<Month> = fraDato månederTil tilDato

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

    val bransjeListe: List<ArbeidstilsynetBransje> =
        ArbeidstilsynetBransje
            .values()
            .toList()
            .filterNot { it == ArbeidstilsynetBransje.ANDRE_BRANSJER }
            .sortedBy { it.name }

    val mottatteIaTjenesterInnloggetPerBransjeOgKilde: MottatteIaTjenesterPerBransjeOgKilde =
        beregnAntallMottatteIaTjenesterPerBransjeOgKilde(
            bransjeListe,
            fjernDupliserteMetrikkerSammeDag(innloggetMetrikker)
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
            metrikkerPerMåned.mapValues { (_, antallMetrikker) -> antallMetrikker.sumOf { it } }

        return antallMetrikkerPerMåned
    }

    fun fjernDupliserteMetrikkerSammeDag(
        mottattIaTjenesteMetrikker: List<MottattInnloggetIaTjenesteMetrikk>
    ): List<MottattInnloggetIaTjenesteMetrikk> {
        return mottattIaTjenesteMetrikker.distinctBy {
            Pair(it.orgnr, it.tidspunkt.toLocalDate())
        }
    }

    fun beregnAntallMetrikkerPerDag(
        mottattIaTjenesteMetrikker: List<IaTjenesterMetrikkerRepository.MottattIaTjenesteMetrikk>
    ): Map<LocalDate, Int> {
        return mottattIaTjenesteMetrikker.groupingBy { it.tidspunkt.toLocalDate() }.eachCount()
    }

    fun beregnAntallMottatteIaTjenesterPerBransjeOgKilde(
        bransjeListe: List<ArbeidstilsynetBransje>,
        mottattIaTjenesteMetrikker: List<MottattInnloggetIaTjenesteMetrikk>
    ): MottatteIaTjenesterPerBransjeOgKilde {

        val alleBransjerPerBransjeOgKilde: MottatteIaTjenesterPerBransjeOgKilde =
            bransjeListe.map { BransjePerKilde(Kilde.SAMTALESTØTTE, it) }
                .map { it to 0 }
                .toMap() +
                    bransjeListe.map { Pair(Kilde.SYKEFRAVÆRSSTATISTIKK, it) }
                        .map { it to 0 }
                        .toMap()

        val alleBransjerPerBransjeOgKildeMedAntallMetrikker: MottatteIaTjenesterPerBransjeOgKilde =
            mottattIaTjenesteMetrikker
                .groupingBy { BransjePerKilde(it.kilde, it.næring.getArbeidstilsynetBransje()) }
                .eachCount()
                .filterNot { it.key.second == ArbeidstilsynetBransje.ANDRE_BRANSJER }

        return alleBransjerPerBransjeOgKilde + alleBransjerPerBransjeOgKildeMedAntallMetrikker
    }

    fun gjeldendeMåneder() = gjeldendeMåneder
}

typealias BransjePerKilde = Pair<Kilde, ArbeidstilsynetBransje>
typealias MottatteIaTjenesterPerBransjeOgKilde = Map<BransjePerKilde, Int>
