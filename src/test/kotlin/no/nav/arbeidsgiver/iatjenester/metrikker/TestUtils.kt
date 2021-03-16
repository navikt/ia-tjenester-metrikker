package no.nav.arbeidsgiver.iatjenester.metrikker

import no.nav.arbeidsgiver.iatjenester.metrikker.domene.InnloggetIaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.Kilde
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.TypeIATjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.UinnloggetIaTjeneste
import java.sql.Connection
import java.sql.Date
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.ZonedDateTime

class TestUtils {

    companion object {
        fun vilkårligIaTjeneste(): InnloggetIaTjeneste = InnloggetIaTjeneste(
            "987654321",
            "12345",
            TypeIATjeneste.DIGITAL_IA_TJENESTE,
            Kilde.SYKEFRAVÆRSSTATISTIKK,
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

        fun vilkårligUinnloggetIaTjeneste(): UinnloggetIaTjeneste = UinnloggetIaTjeneste(
            TypeIATjeneste.DIGITAL_IA_TJENESTE,
            Kilde.SYKEFRAVÆRSSTATISTIKK,
            ZonedDateTime.now(),
        )

        fun vilkårligUinnloggetIaTjenesteAsString(): String {
            return """
            {
              "kilde":"SYKEFRAVÆRSSTATISTIKK",
              "type":"DIGITAL_IA_TJENESTE",
              "tjenesteMottakkelsesdato":"2021-03-11T18:48:38Z"
            }
        """.trimIndent()
        }

        fun vilkårligInnloggetIaTjenesteAsString(): String {
            return """
            {
              "orgnr":"444444444",
              "antallAnsatte":99,
              "kilde":"SYKEFRAVÆRSSTATISTIKK",
              "type":"DIGITAL_IA_TJENESTE",
              "fylke":"IKKE_TILGJENGELIG",
              "fylkesnummer":"IKKE_TILGJENGELIG",
              "kommune":"OSLO",
              "kommunenummer":"9999",
              "næring2SifferBeskrivelse":"Offentlig administrasjon og forsvar, og trygdeordninger underlagt offentlig forvaltning",
              "næringKode5Siffer":"84300",
              "næringskode5SifferBeskrivelse":"Trygdeordninger underlagt offentlig orvaltning",
              "ssbSektorKode":"6500",
              "ssbSektorKodeBeskrivelse":"Offentlig sektor",
              "tjenesteMottakkelsesdato":"2021-03-11T18:48:38Z"
            }
        """.trimIndent()
        }

        fun Connection.getAlleIATjenester(): List<IaTjenesteRad> =
            use {
                this.prepareStatement(
                    """
                SELECT *  
                FROM metrikker_ia_tjenester_innlogget
                WHERE orgnr = ?
                """
                ).use {
                    it.setString(1, "987654321")
                    it.executeQuery()
                        .use {
                            generateSequence {
                                if (it.next()) {
                                    it.getIaTjenesteRad()
                                } else {
                                    null
                                }
                            }.toList()
                        }
                }
            }

        fun Connection.getAlleUinnloggetIaTjenester(): List<UinnloggetIaTjenesteRad> =
            use {
                this.prepareStatement(
                    """
                SELECT *  
                FROM metrikker_ia_tjenester_uinnlogget
                """
                ).use {
                    it.executeQuery()
                        .use {
                            generateSequence {
                                if (it.next()) {
                                    it.getUinnloggetIaTjenesteRad()
                                } else {
                                    null
                                }
                            }.toList()
                        }
                }
            }

        private fun ResultSet.getIaTjenesteRad(): IaTjenesteRad {
            return IaTjenesteRad(
                id = getInt("id"),
                orgnr = getString("orgnr"),
                næringKode5Siffer = getString("naering_kode_5siffer"),
                type = TypeIATjeneste.valueOf(getString("form_av_tjeneste")),
                kilde = Kilde.valueOf(getString("kilde_applikasjon")),
                tjeneste_mottakkelsesdato = getTimestamp("tjeneste_mottakkelsesdato"),
                antallAnsatte = getInt("antall_ansatte"),
                næringskode5SifferBeskrivelse = getString("naering_kode5siffer_beskrivelse"),
                næring2SifferBeskrivelse = getString("naering_2siffer_beskrivelse"),
                SSBSektorKode = getString("ssb_sektor_kode"),
                SSBSektorKodeBeskrivelse = getString("ssb_sektor_kode_beskrivelse"),
                fylkesnummer = getString("fylkesnummer"),
                fylke = getString("fylke"),
                kommunenummer = getString("kommunenummer"),
                kommune = getString("kommune"),
                opprettet = getDate("opprettet")
            )
        }

        private fun ResultSet.getUinnloggetIaTjenesteRad(): UinnloggetIaTjenesteRad {
            return UinnloggetIaTjenesteRad(
                id = getInt("id"),
                type = TypeIATjeneste.valueOf(getString("form_av_tjeneste")),
                kilde = Kilde.valueOf(getString("kilde_applikasjon")),
                tjeneste_mottakkelsesdato = getTimestamp("tjeneste_mottakkelsesdato"),
                opprettet = getDate("opprettet")
            )
        }
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

data class UinnloggetIaTjenesteRad(
    val id: Int,
    val type: TypeIATjeneste,
    val kilde: Kilde,
    val tjeneste_mottakkelsesdato: Timestamp,
    val opprettet: Date?
)

