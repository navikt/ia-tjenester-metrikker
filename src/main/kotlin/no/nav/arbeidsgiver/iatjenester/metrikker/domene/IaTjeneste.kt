package no.nav.arbeidsgiver.iatjenester.metrikker.domene

import java.sql.Timestamp

data class IaTjeneste(val orgnr: String,
                      val næringKode5Siffer :String,
                      val type: TypeIATjeneste,
                      val kilde: Kilde,
                      val tjenesteMottakkelsesdato: Timestamp,
                      val antallAnsatte : Int,
                      val næringskode5SifferBeskrivelse: String,
                      val næring2SifferBeskrivelse: String,
                      val SSBSektorKode: String,
                      val SSBSektorKodeBeskrivelse: String,
                      val fylkesnummer: String,
                      val fylke: String,
                      val kommunenummer: String,
                      val kommune: String
)

enum class Kilde {
    SYKKEFRAVÆRSSTATISTIKK,
    DIALOG
}

enum class TypeIATjeneste {
    DIGITAL_IA_TJENESTE,
    RÅDGIVNING
}
