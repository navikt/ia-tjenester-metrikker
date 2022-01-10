package no.nav.arbeidsgiver.iatjenester.metrikker.restdto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.Næringsbeskrivelser
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker.OverordnetEnhet
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker.Underenhet
import java.time.ZonedDateTime

enum class Kilde {
    SYKEFRAVÆRSSTATISTIKK,
    SAMTALESTØTTE,
    DIALOG
}

enum class IaTjenesteTilgjengelighet {
    INNLOGGET,
    UINNLOGGET
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


fun getInnloggetMottattIaTjenesteMedVirksomhetGrunndata(
    innloggetIaTjeneste: InnloggetMottattIaTjeneste,
    underenhet: Underenhet,
    overordnetEnhet: OverordnetEnhet
) = InnloggetMottattIaTjenesteMedVirksomhetGrunndata(
    orgnr = innloggetIaTjeneste.orgnr,
    næringKode5Siffer = underenhet.næringskode.kode!!,
    type = innloggetIaTjeneste.type,
    kilde = innloggetIaTjeneste.kilde,
    tjenesteMottakkelsesdato = innloggetIaTjeneste.tjenesteMottakkelsesdato,
    antallAnsatte = underenhet.antallAnsatte,
    næringskode5SifferBeskrivelse = underenhet.næringskode.beskrivelse,
    næring2SifferBeskrivelse = Næringsbeskrivelser.mapTilNæringsbeskrivelse(
        underenhet.næringskode.kode!!.substring(0, 2)
    ),
    SSBSektorKode = overordnetEnhet.institusjonellSektorkode.kode,
    SSBSektorKodeBeskrivelse = overordnetEnhet.institusjonellSektorkode.beskrivelse,
    fylke = underenhet.fylke.navn,
    kommunenummer = underenhet.kommunenummer,
    kommune = underenhet.kommune
)
