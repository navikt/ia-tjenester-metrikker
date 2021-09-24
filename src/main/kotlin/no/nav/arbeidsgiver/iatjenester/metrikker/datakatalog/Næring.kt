package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog

/*
Næringskode 2 siffer og næringskode 5 siffer er standard for næringsgruppering (SN) fra SSB:
Ref: https://www.ssb.no/klass/klassifikasjoner/6
*/
data class Næring(val kode5Siffer: String, val kode5SifferBeskrivelse: String, val kode2SifferBeskrivelse: String) {

    fun getKode2siffer(): String = if (kode5Siffer.length > 1) kode5Siffer.substring(0, 2) else ""

    fun getArbeidstilsynetBransje(): ArbeidstilsynetBransje {
        when (getKode2siffer()) {
            "10" -> return ArbeidstilsynetBransje.NÆRINGSMIDDELINDUSTRI
            "41" -> return ArbeidstilsynetBransje.BYGG
            "42" -> return ArbeidstilsynetBransje.ANLEGG
        }

        when (kode5Siffer) {
            "88911" -> return ArbeidstilsynetBransje.BARNEHAGER
            "86101", "86102", "86104", "86105", "86106", "86107" -> return ArbeidstilsynetBransje.SYKEHUS
            "87101", "87102" -> return ArbeidstilsynetBransje.SYKEHJEM
            "49100", "49311", "49391", "49392" -> return ArbeidstilsynetBransje.TRANSPORT
        }

        return ArbeidstilsynetBransje.ANDRE_BRANSJER
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
}