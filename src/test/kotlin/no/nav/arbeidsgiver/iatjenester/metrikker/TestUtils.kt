package no.nav.arbeidsgiver.iatjenester.metrikker

import no.nav.arbeidsgiver.iatjenester.metrikker.domene.IaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.Kilde
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.TypeIATjeneste
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