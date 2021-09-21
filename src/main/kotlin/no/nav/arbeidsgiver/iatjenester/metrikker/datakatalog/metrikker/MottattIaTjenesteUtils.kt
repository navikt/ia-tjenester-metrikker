package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker

import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker.MottattIaTjenesteUtils.Companion.getArbeidstilsynetBransje
import no.nav.arbeidsgiver.iatjenester.metrikker.repository.IaTjenesterMetrikkerRepository

class MottattIaTjenesteUtils {

    /*
      Næringskode 2 siffer og næringskode 5 siffer er standard for næringsgruppering (SN) fra SSB:
      Ref: https://www.ssb.no/klass/klassifikasjoner/6
     */

    companion object {
        fun getArbeidstilsynetBransje(næringskode2siffer: String, næringskode5siffer: String): ArbeidstilsynetBransje {
            when (næringskode2siffer) {
                "10" -> return ArbeidstilsynetBransje.NÆRINGSMIDDELINDUSTRI
                "41" -> return ArbeidstilsynetBransje.BYGG
                "42" -> return ArbeidstilsynetBransje.ANLEGG
            }

            when (næringskode5siffer) {
                "88911" -> return ArbeidstilsynetBransje.BARNEHAGER
                "86101", "86102", "86104", "86105", "86106", "86107" -> return ArbeidstilsynetBransje.SYKEHUS
                "87101", "87102" -> return ArbeidstilsynetBransje.SYKEHJEM
                "49100", "49311", "49391", "49392" -> return ArbeidstilsynetBransje.TRANSPORT
            }

            return ArbeidstilsynetBransje.ANDRE_BRANSJER
        }
    }
}

fun IaTjenesterMetrikkerRepository.MottattInnloggetIaTjenesteMetrikk.getMetadata(): VirksomhetMetadata {

    val næringskode2sifferKode: String =
        if (næringskode5Siffer.kode.length > 1) næringskode5Siffer.kode.substring(0, 2) else ""

    return VirksomhetMetadata(
        orgnr,
        IaTjenesterMetrikkerRepository.Næringskode2Siffer(
            næringskode2sifferKode, næringskode2SifferBeskrivelse
        ),
        næringskode5Siffer,
        getArbeidstilsynetBransje(næringskode2sifferKode, næringskode5Siffer.kode)
    )
}

enum class ArbeidstilsynetBransje {
    BARNEHAGER,
    NÆRINGSMIDDELINDUSTRI,
    SYKEHUS,
    SYKEHJEM,
    TRANSPORT,
    BYGG,
    ANLEGG,
    ANDRE_BRANSJER
}
