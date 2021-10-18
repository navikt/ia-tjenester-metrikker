package no.nav.arbeidsgiver.iatjenester.metrikker.restdto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer
import java.time.ZonedDateTime

enum class Kilde {
    SYKEFRAVÆRSSTATISTIKK,
    SAMTALESTØTTE,
    DIALOG
}

enum class TypeIATjeneste {
    DIGITAL_IA_TJENESTE,
    RÅDGIVNING
}

enum class AltinnRettighet {
    SYKEFRAVÆRSSTATISTIKK_FOR_VIRKSOMHETER,
    ARBEIDSGIVERS_OPPFØLGINGSPLAN_FOR_SYKMELDTE
}

interface MottattIaTjeneste {
    var tjenesteMottakkelsesdato: ZonedDateTime
    var type: TypeIATjeneste
    var kilde: Kilde
}

data class UinnloggetMottattIaTjeneste(
    override var type: TypeIATjeneste,
    override var kilde: Kilde,
    @get: JsonSerialize(using = ZonedDateTimeSerializer::class)
    @get: JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssX")
    override var tjenesteMottakkelsesdato: ZonedDateTime,
) : MottattIaTjeneste


data class InnloggetMottattIaTjeneste(
    var orgnr: String,
    var altinnRettighet: AltinnRettighet,
    override var type: TypeIATjeneste,
    override var kilde: Kilde,
    @get: JsonSerialize(using = ZonedDateTimeSerializer::class)
    @get: JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssX")
    override var tjenesteMottakkelsesdato: ZonedDateTime,
) : MottattIaTjeneste

data class InnloggetMottattIaTjenesteMedVirksomhetGrunndata(
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
    var fylke: String,
    var kommunenummer: String,
    var kommune: String
) : MottattIaTjeneste
