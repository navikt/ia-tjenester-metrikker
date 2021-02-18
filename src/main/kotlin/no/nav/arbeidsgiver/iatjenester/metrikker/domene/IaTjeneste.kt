package no.nav.arbeidsgiver.iatjenester.metrikker.domene

data class IaTjeneste(val orgnr: String, val næringKode5Siffer :String, val type: TypeIATjeneste, val kilde: Kilde)

enum class Kilde {
    SYKKEFRAVÆRSSTATISTIKK,
    DIALOG
}

enum class TypeIATjeneste {
    DIGITAL_IA_TJENESTE,
    RÅDGIVNING
}
