package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker

import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.Næring.ArbeidstilsynetBransje
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.månederTil
import no.nav.arbeidsgiver.iatjenester.metrikker.repository.IaTjenesterMetrikkerRepository
import no.nav.arbeidsgiver.iatjenester.metrikker.repository.IaTjenesterMetrikkerRepository.MottattInnloggetIaTjenesteMetrikk
import no.nav.arbeidsgiver.iatjenester.metrikker.repository.IaTjenesterMetrikkerRepository.MottattUinnloggetIaTjenesteMetrikk
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.Kilde
import java.time.LocalDate
import java.time.Month

class MottattIaTjenesterDatagrunnlag(
    val innloggetMetrikker: List<MottattInnloggetIaTjenesteMetrikk>,
    val uinnloggetMetrikker: List<MottattUinnloggetIaTjenesteMetrikk>,
    val fraDato: LocalDate,
    val tilDato: LocalDate
) {
    val startDate: LocalDate = fraDato
    val gjeldendeÅr = fraDato.year
    val gjeldendeMåneder: List<Month> = fraDato månederTil tilDato

    val alleFylkerAlfabetisk = alleFylkerAlfabetisk()

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

    fun beregnInnloggedeIaTjenesterPerFylke(fraApp: Kilde): Collection<Int> =
        (alleFylkerAlfabetisk.associateWith { 0 } +
                innloggetMetrikker
                    .filter { it.kilde == fraApp }
                    .filter { it.fylke in alleFylkerAlfabetisk }
                    .groupingBy { it.fylke }
                    .eachCount()
                ).values

    fun beregnAntallMetrikkerPerMåned(
        måneder: List<Month>,
        metrikkerPerDag: Map<LocalDate, Int>
    ): Map<Month, Int> {
        val metrikkerPerMåned: Map<Month, Collection<Int>> =
            måneder.associateWith { metrikkerPerDag.filter { (key, _) -> key.month == it }.values }

        return metrikkerPerMåned.mapValues { (_, antallMetrikker) -> antallMetrikker.sumOf { it } }
    }

    private fun fjernDupliserteMetrikkerSammeDag(
        mottattIaTjenesteMetrikker: List<MottattInnloggetIaTjenesteMetrikk>
    ): List<MottattInnloggetIaTjenesteMetrikk> {
        return mottattIaTjenesteMetrikker.distinctBy {
            Pair(it.orgnr, it.tidspunkt.toLocalDate())
        }
    }

    private fun beregnAntallMetrikkerPerDag(
        mottattIaTjenesteMetrikker: List<IaTjenesterMetrikkerRepository.MottattIaTjenesteMetrikk>
    ): Map<LocalDate, Int> {
        return mottattIaTjenesteMetrikker.groupingBy { it.tidspunkt.toLocalDate() }.eachCount()
    }

    private fun beregnAntallMottatteIaTjenesterPerBransjeOgKilde(
        bransjeListe: List<ArbeidstilsynetBransje>,
        mottattIaTjenesteMetrikker: List<MottattInnloggetIaTjenesteMetrikk>
    ): MottatteIaTjenesterPerBransjeOgKilde {

        val alleBransjerPerBransjeOgKilde: MottatteIaTjenesterPerBransjeOgKilde =
            bransjeListe.map { BransjePerKilde(Kilde.SAMTALESTØTTE, it) }.associateWith { 0 } +
                    bransjeListe.map { Pair(Kilde.SYKEFRAVÆRSSTATISTIKK, it) }.associateWith { 0 }

        val alleBransjerPerBransjeOgKildeMedAntallMetrikker: MottatteIaTjenesterPerBransjeOgKilde =
            mottattIaTjenesteMetrikker
                .groupingBy { BransjePerKilde(it.kilde, it.næring.getArbeidstilsynetBransje()) }
                .eachCount()
                .filterNot { it.key.second == ArbeidstilsynetBransje.ANDRE_BRANSJER }

        return alleBransjerPerBransjeOgKilde + alleBransjerPerBransjeOgKildeMedAntallMetrikker
    }
}

typealias BransjePerKilde = Pair<Kilde, ArbeidstilsynetBransje>
typealias MottatteIaTjenesterPerBransjeOgKilde = Map<BransjePerKilde, Int>
