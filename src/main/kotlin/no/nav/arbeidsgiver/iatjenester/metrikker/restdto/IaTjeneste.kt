package no.nav.arbeidsgiver.iatjenester.metrikker.restdto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer
import java.time.ZonedDateTime

interface IaTjeneste {
    var tjenesteMottakkelsesdato: ZonedDateTime
    var type: TypeIATjeneste
    var kilde: Kilde
}

data class InnloggetIaTjeneste(
    var orgnr: String,
    var næringKode5Siffer: String,
    override var type: TypeIATjeneste,
    override var kilde: Kilde,
    @get: JsonSerialize(using = ZonedDateTimeSerializer::class)
    @get: JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssX")
    override var tjenesteMottakkelsesdato: ZonedDateTime,
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
) : IaTjeneste

data class UinnloggetIaTjeneste(
    override var type: TypeIATjeneste,
    override var kilde: Kilde,
    @get: JsonSerialize(using = ZonedDateTimeSerializer::class)
    @get: JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssX")
    override var tjenesteMottakkelsesdato: ZonedDateTime,
) : IaTjeneste


enum class Kilde {
    SYKEFRAVÆRSSTATISTIKK,
    SAMTALESTØTTE,
    DIALOG
}

enum class TypeIATjeneste {
    DIGITAL_IA_TJENESTE,
    RÅDGIVNING
}
