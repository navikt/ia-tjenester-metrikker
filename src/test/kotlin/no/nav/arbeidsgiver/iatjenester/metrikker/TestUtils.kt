package no.nav.arbeidsgiver.iatjenester.metrikker

import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.InnloggetIaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.Kilde
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.TypeIATjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.UinnloggetIaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll.Fnr
import java.sql.Connection
import java.sql.Date
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.ZonedDateTime

class TestUtils {

    companion object {

        val ORGNR_SOM_RETURNERES_AV_MOCK_ALTINN: String = "811076112"
        val TEST_FNR: Fnr = Fnr("01019912345")

        fun testTokenForTestFNR(): String {
            val mockOAuth2Header = "eyJraWQiOiJtb2NrLW9hdXRoMi1zZXJ2ZXIta2V5IiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ"
            val localhostOnlyJasonPayload =
                "eyJzdWIiOiIwMTAxOTkxMjM0NSIsImF1ZCI6ImF1ZC1sb2NhbGhvc3QiLCJhY3IiOi" +
                        "JMZXZlbDQiLCJuYmYiOjE2MTY0OTQwMDksImF6cCI6Ik1vY2tMb2dpbkNvbnRyb2xsZXIiLCJpc3MiOiJodH" +
                        "RwOlwvXC9sb2NhbGhvc3Q6MTA4MzJcL3NlbHZiZXRqZW5pbmciLCJleHAiOjE2MTY0OTc2MDksImlhdCI6M" +
                        "TYxNjQ5NDAwOSwianRpIjoiZWY1ZGEzM2YtMTBhMC00YTZlLTgyMjYtOWY5NTUxNTA5Y2ZlIiwidGlkIjoic" +
                        "2VsdmJldGplbmluZyJ9"
            val signature = "Ingen signature"
            return "$mockOAuth2Header.$localhostOnlyJasonPayload.$signature"
        }

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
            return vilkårligInnloggetIaTjenesteAsString(ORGNR_SOM_RETURNERES_AV_MOCK_ALTINN)
        }

        fun vilkårligInnloggetIaTjenesteAsString(orgNr: String): String {
            return """
            {
              "orgnr":"${orgNr}",
              "antallAnsatte":99,
              "kilde":"SYKEFRAVÆRSSTATISTIKK",
              "type":"DIGITAL_IA_TJENESTE",
              "fylke":"Troms og Finnmark",
              "fylkesnummer":"54",
              "kommune":"Sør-Varanger",
              "kommunenummer":"5444",
              "næring2SifferBeskrivelse":"Offentlig administrasjon og forsvar, og trygdeordninger underlagt offentlig forvaltning",
              "næringKode5Siffer":"84300",
              "næringskode5SifferBeskrivelse":"Trygdeordninger underlagt offentlig orvaltning",
              "ssbSektorKode":"6500",
              "ssbSektorKodeBeskrivelse":"Offentlig sektor",
              "tjenesteMottakkelsesdato":"2021-03-11T18:48:38Z"
            }
        """.trimIndent()
        }

        fun realistiskkInnloggetIaTjenesteAsString(): String {
            return """
            {
              "orgnr":"${ORGNR_SOM_RETURNERES_AV_MOCK_ALTINN}",
              "antallAnsatte":99,
              "kilde":"SYKEFRAVÆRSSTATISTIKK",
              "type":"DIGITAL_IA_TJENESTE",
              "fylke":"IKKE_TILGJENGELIG",
              "fylkesnummer":"IKKE_TILGJENGELIG",
              "kommune":"OSLO",
              "kommunenummer":"0301",
              "næring2SifferBeskrivelse":"Offentlig administrasjon og forsvar, og trygdeordninger underlagt offentlig forvaltning",
              "næringKode5Siffer":"84300",
              "næringskode5SifferBeskrivelse":"Trygdeordninger underlagt offentlig orvaltning",
              "ssbSektorKode":"6500",
              "ssbSektorKodeBeskrivelse":"Offentlig sektor",
              "tjenesteMottakkelsesdato":"2021-03-11T18:48:38Z"
            }
        """.trimIndent()
        }

        fun Connection.cleanTable(tableName: String) =
            use {
                this.prepareStatement(
                    """delete from $tableName"""
                ).use {
                    it.executeUpdate()
                }
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

        fun Connection.opprettInnloggetIaTjeneste(rad: IaTjenesteRad): Any =
            use {
                this.prepareStatement(
                    """insert into metrikker_ia_tjenester_innlogget (
                         form_av_tjeneste, 
                         kilde_applikasjon, 
                         orgnr, 
                         antall_ansatte,
                         naering_2siffer_beskrivelse,
                         naering_kode_5siffer, 
                         naering_kode5siffer_beskrivelse,
                         ssb_sektor_kode,
                         ssb_sektor_kode_beskrivelse,
                         fylkesnummer, 
                         fylke,
                         kommunenummer, 
                         kommune,
                         tjeneste_mottakkelsesdato
                    ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""".trimMargin()
                ).run {
                    setString(1, rad.type.name)
                    setString(2, rad.kilde.name)
                    setString(3, rad.orgnr)
                    setInt(4, rad.antallAnsatte)
                    setString(5, rad.næring2SifferBeskrivelse)
                    setString(6, rad.næringKode5Siffer)
                    setString(7, rad.næringskode5SifferBeskrivelse)
                    setString(8, rad.SSBSektorKode)
                    setString(9, rad.SSBSektorKodeBeskrivelse)
                    setString(10, rad.fylkesnummer)
                    setString(11, rad.fylke)
                    setString(12, rad.kommunenummer)
                    setString(13, rad.kommune)
                    setTimestamp(14, rad.tjeneste_mottakkelsesdato)
                    executeUpdate()
                }
            }

        fun Connection.opprettUinnloggetIaTjeneste(rad: UinnloggetIaTjenesteRad): Any =
            use {
                this.prepareStatement(
                    """insert into metrikker_ia_tjenester_uinnlogget (
                        form_av_tjeneste, 
                        kilde_applikasjon, 
                        tjeneste_mottakkelsesdato
                    ) values (?, ?, ?)"""
                ).run {
                    setString(1, rad.type.name)
                    setString(2, rad.kilde.name)
                    setTimestamp(3, rad.tjeneste_mottakkelsesdato)
                    executeUpdate()
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

