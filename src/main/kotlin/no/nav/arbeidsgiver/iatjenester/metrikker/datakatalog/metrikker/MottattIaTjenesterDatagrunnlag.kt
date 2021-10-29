package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker

import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.Næring.ArbeidstilsynetBransje
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.alleFylkerAlfabetisk
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.månederTil
import no.nav.arbeidsgiver.iatjenester.metrikker.repository.IaTjenesterMetrikkerRepository
import no.nav.arbeidsgiver.iatjenester.metrikker.repository.IaTjenesterMetrikkerRepository.MottattInnloggetIaTjenesteMetrikk
import no.nav.arbeidsgiver.iatjenester.metrikker.repository.IaTjenesterMetrikkerRepository.MottattUinnloggetIaTjenesteMetrikk
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.IaTjenesteTilgjengelighet
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

    val totalUinnloggetMetrikker: Int = uinnloggetMetrikker.size
    val totalUnikeBedrifterPerDag: Int =
        beregnAntallMetrikkerPerDag(fjernDupliserteMetrikkerSammeDag(innloggetMetrikker)).values.sum()

    fun totalInnloggetMetrikkerPerApp(fraApp: Kilde): Int =
        innloggetMetrikker.filter { it.kilde == fraApp }.size

    fun beregnInnloggedeIaTjenesterPerFylke(fraApp: Kilde): Collection<Int> =
        (alleFylkerAlfabetisk.associateWith { 0 } +
                innloggetMetrikker
                    .filter { it.kilde == fraApp }
                    .filter { it.fylke in alleFylkerAlfabetisk }
                    .groupingBy { it.fylke }
                    .eachCount()
                ).values


    fun beregnAntallMetrikkerPerMånedPerApp(
        fraApp: Kilde,
        innloggetEllerUinlogget: IaTjenesteTilgjengelighet
    ): Map<Month, Int> {
        val datagrunnlag =
            if (innloggetEllerUinlogget == IaTjenesteTilgjengelighet.INNLOGGET)
                fjernDupliserteMetrikkerSammeDag(innloggetMetrikker) else uinnloggetMetrikker

        return gjeldendeMåneder.associateWith { 0 } +
                datagrunnlag.filter { it.kilde == fraApp }
                    .filter { it.tidspunkt.month in gjeldendeMåneder }
                    .groupingBy { it.tidspunkt.month }
                    .eachCount()

    }

    private fun fjernDupliserteMetrikkerSammeDag(
        mottattIaTjenesteMetrikker: List<MottattInnloggetIaTjenesteMetrikk>
    ): List<MottattInnloggetIaTjenesteMetrikk> {
        return mottattIaTjenesteMetrikker.distinctBy {
            Triple(it.orgnr, it.kilde, it.tidspunkt.toLocalDate())
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
