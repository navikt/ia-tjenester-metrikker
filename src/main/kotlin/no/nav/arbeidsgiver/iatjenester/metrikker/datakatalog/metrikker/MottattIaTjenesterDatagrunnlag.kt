package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker

import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.MånedOgÅr
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.Næring.ArbeidsmiljøportalenBransje
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.alleFylkerAlfabetisk
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.månederOgÅrTil
import no.nav.arbeidsgiver.iatjenester.metrikker.repository.IaTjenesterMetrikkerRepository.MottattInnloggetIaTjenesteMetrikk
import no.nav.arbeidsgiver.iatjenester.metrikker.repository.IaTjenesterMetrikkerRepository.MottattUinnloggetIaTjenesteMetrikk
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.IaTjenesteTilgjengelighet
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.Kilde
import java.time.LocalDate

class MottattIaTjenesterDatagrunnlag(
    private val innloggetMetrikker: List<MottattInnloggetIaTjenesteMetrikk>,
    val uinnloggetMetrikker: List<MottattUinnloggetIaTjenesteMetrikk>,
    val fraDato: LocalDate,
    val tilDato: LocalDate
) {
    val gjeldendeMånederOgÅr: List<MånedOgÅr> = fraDato månederOgÅrTil tilDato
    val leverteInnloggedeIatjenester = fjernDupliserteMetrikkerSammeDag(innloggetMetrikker)

    private val alleFylkerAlfabetisk = alleFylkerAlfabetisk()

    val bransjeListe: List<ArbeidsmiljøportalenBransje> =
        ArbeidsmiljøportalenBransje
            .values()
            .toList()
            .filterNot { it == ArbeidsmiljøportalenBransje.ANDRE_BRANSJER }
            .sortedBy { it.name }

    val mottatteIaTjenesterInnloggetPerBransjeOgKilde: Map<BransjeOgKilde, Int> =
        beregnAntallMottatteIaTjenesterPerBransjeOgKilde(
            bransjeListe,
            leverteInnloggedeIatjenester
        )

    val antallUnikeBedrifterPerÅr: Map<Int, Int> = beregnAntallUnikeBedrifterPerÅr()

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
    ): Map<MånedOgÅr, Int> {
        val datagrunnlag =
            if (innloggetEllerUinlogget == IaTjenesteTilgjengelighet.INNLOGGET)
                leverteInnloggedeIatjenester else uinnloggetMetrikker

        return gjeldendeMånederOgÅr.associateWith { 0 } +
                datagrunnlag.filter { it.kilde == fraApp }
                    .filter { MånedOgÅr(it.tidspunkt.year, it.tidspunkt.month) in gjeldendeMånederOgÅr }
                    .groupingBy { MånedOgÅr(it.tidspunkt.year, it.tidspunkt.month) }
                    .eachCount()
    }

    private fun fjernDupliserteMetrikkerSammeDag(
        mottattIaTjenesteMetrikker: List<MottattInnloggetIaTjenesteMetrikk>
    ): List<MottattInnloggetIaTjenesteMetrikk> {
        return mottattIaTjenesteMetrikker.distinctBy {
            Triple(it.orgnr, it.kilde, it.tidspunkt.toLocalDate())
        }
    }

    private fun beregnAntallUnikeBedrifterPerÅr() =
        leverteInnloggedeIatjenester
            .distinctBy { Pair(it.orgnr, it.tidspunkt.year) }
            .groupingBy { it.tidspunkt.toLocalDate().year }
            .eachCount()
}

private fun beregnAntallMottatteIaTjenesterPerBransjeOgKilde(
    bransjeListe: List<ArbeidsmiljøportalenBransje>,
    mottattIaTjenesteMetrikker: List<MottattInnloggetIaTjenesteMetrikk>
): Map<BransjeOgKilde, Int> {

    val alleBransjerPerBransjeOgKilde: Map<BransjeOgKilde, Int> =
        bransjeListe.map { BransjeOgKilde(Kilde.SAMTALESTØTTE, it) }.associateWith { 0 } +
                bransjeListe.map { Pair(Kilde.SYKEFRAVÆRSSTATISTIKK, it) }.associateWith { 0 }

    val alleBransjerPerBransjeOgKildeMedAntallMetrikker: Map<BransjeOgKilde, Int> =
        mottattIaTjenesteMetrikker
            .groupingBy { BransjeOgKilde(it.kilde, it.næring.getArbeidstilsynetBransje()) }
            .eachCount()
            .filterNot { it.key.second == ArbeidsmiljøportalenBransje.ANDRE_BRANSJER }

    return alleBransjerPerBransjeOgKilde + alleBransjerPerBransjeOgKildeMedAntallMetrikker
}

typealias BransjeOgKilde = Pair<Kilde, ArbeidsmiljøportalenBransje>
