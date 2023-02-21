package no.nav.arbeidsgiver.iatjenester.metrikker.restdto

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.Næringsbeskrivelser
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.OverordnetEnhet
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.Underenhet

enum class Kilde {
    SYKEFRAVÆRSSTATISTIKK,
    SAMTALESTØTTE,
    FOREBYGGE_FRAVÆR,
    KALKULATOR,
    NETTKURS,
    DIALOG,
    FOREBYGGINGSPLAN,
}

enum class TypeIATjeneste {
    DIGITAL_IA_TJENESTE,
    INFORMASJONSTJENESTE,
    INTERAKSJONSTJENESTE,
    RÅDGIVNING,
}


interface MottattIaTjeneste {
    var type: TypeIATjeneste
    var kilde: Kilde
}

data class UinnloggetMottattIaTjeneste(
    override var type: TypeIATjeneste,
    override var kilde: Kilde,
) : MottattIaTjeneste


data class InnloggetMottattIaTjeneste(
    var orgnr: String,
    override var type: TypeIATjeneste,
    override var kilde: Kilde,
) : MottattIaTjeneste

data class InnloggetMottattIaTjenesteMedVirksomhetGrunndata(
    var orgnr: String,
    var næringKode5Siffer: String,
    override var type: TypeIATjeneste,
    override var kilde: Kilde,
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
