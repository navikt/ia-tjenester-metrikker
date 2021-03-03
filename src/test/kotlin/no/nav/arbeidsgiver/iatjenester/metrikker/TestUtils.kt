package no.nav.arbeidsgiver.iatjenester.metrikker

import no.nav.arbeidsgiver.iatjenester.metrikker.domene.IaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.Kilde
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.TypeIATjeneste
import java.sql.Date
import java.sql.Timestamp
import java.time.ZonedDateTime

class TestUtils {

    companion object {
        fun vilkårligIaTjeneste(): IaTjeneste = IaTjeneste(
            "987654321",
            "12345",
            TypeIATjeneste.DIGITAL_IA_TJENESTE,
            Kilde.SYKKEFRAVÆRSSTATISTIKK,
            ZonedDateTime.now(),
            10,
            "En beskrivelse for næringskode 5 siffer",
            "En beskrivelse for næring kode 2 siffer",
            "21000",
            "Beskrivelse ssb sektor kode",
            "30",
            "Viken",
            "0234",
            "Gjerdrum"
        )
    }
}

data class IaTjenesteRad(
    val id: Int,
    val orgnr: String,
    val næringKode5Siffer: String,
    val type: TypeIATjeneste,
    val kilde: Kilde,
    val tjeneste_mottakkelsesdato: Timestamp,
    val antallAnsatte: Int,
    val næringskode5SifferBeskrivelse: String,
    val næring2SifferBeskrivelse: String,
    val SSBSektorKode: String,
    val SSBSektorKodeBeskrivelse: String,
    val fylkesnummer: String,
    val fylke: String,
    val kommunenummer: String,
    val kommune: String,
    val opprettet: Date?
)

