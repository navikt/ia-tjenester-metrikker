package no.nav.arbeidsgiver.iatjenester.metrikker.domene

import com.fasterxml.jackson.annotation.JsonProperty
import java.sql.Timestamp

data class IaTjeneste(
    var orgnr: String,
    var næringKode5Siffer: String,
    var type: TypeIATjeneste,
    var kilde: Kilde,
    var tjenesteMottakkelsesdato: Timestamp,
    var antallAnsatte: Int,
    var næringskode5SifferBeskrivelse: String,
    var næring2SifferBeskrivelse: String,
    @get:JsonProperty("ssbSektorKode")
    @param:JsonProperty("ssbSektorKode")
    var SSBSektorKode: String,
    @get:JsonProperty("ssbSektorKodeBeskrivelse")
    @param:JsonProperty("ssbSektorKodeBeskrivelse")
    var SSBSektorKodeBeskrivelse: String,
    var fylkesnummer: String,
    var fylke: String,
    var kommunenummer: String,
    var kommune: String
)

enum class Kilde {
    SYKKEFRAVÆRSSTATISTIKK,
    DIALOG
}

enum class TypeIATjeneste {
    DIGITAL_IA_TJENESTE,
    RÅDGIVNING
}
