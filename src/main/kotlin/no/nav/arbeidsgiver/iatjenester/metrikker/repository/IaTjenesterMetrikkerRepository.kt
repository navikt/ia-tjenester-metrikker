package no.nav.arbeidsgiver.iatjenester.metrikker.repository

import no.nav.arbeidsgiver.iatjenester.metrikker.domene.InnloggetIaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.UinnloggetIaTjeneste
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Transactional
@Repository
class IaTjenesterMetrikkerRepository(private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate) {


    fun opprett(iatjeneste: InnloggetIaTjeneste) {
        insertIaTjeneste(iatjeneste)
    }

    fun opprett(uinnloggetIatjeneste: UinnloggetIaTjeneste) {
        namedParameterJdbcTemplate.update(
            """
                INSERT INTO metrikker_ia_tjenester_uinnlogget(
                form_av_tjeneste, 
                kilde_applikasjon,
                tjeneste_mottakkelsesdato
                ) 
                VALUES  (
                  :form_av_tjeneste, 
                  :kilde_applikasjon, 
                  :tjeneste_mottakkelsesdato
                )
                """,
            MapSqlParameterSource()
                .addValue("form_av_tjeneste", uinnloggetIatjeneste.type.name)
                .addValue("kilde_applikasjon", uinnloggetIatjeneste.kilde.name)
                .addValue(
                    "tjeneste_mottakkelsesdato",
                    uinnloggetIatjeneste.tjenesteMottakkelsesdato.toLocalDateTime()
                )
        )
    }

    private fun insertIaTjeneste(iatjeneste: InnloggetIaTjeneste) {
        namedParameterJdbcTemplate.update(
            """
                INSERT INTO metrikker_ia_tjenester_innlogget(
                orgnr, 
                naering_kode_5siffer, 
                form_av_tjeneste, 
                kilde_applikasjon,
                tjeneste_mottakkelsesdato,
                antall_ansatte,
                naering_kode5siffer_beskrivelse,
                naerring_2siffer_beskrivelse,
                ssb_sektor_kode,
                ssb_sektor_kode_beskrivelse,
                fylkesnummer,
                fylke,
                kommunenummer,
                kommune
                ) 
                VALUES  (
                  :orgnr, 
                  :naering_kode_5siffer, 
                  :form_av_tjeneste, 
                  :kilde_applikasjon, 
                  :tjeneste_mottakkelsesdato, 
                  :antall_ansatte, 
                  :naering_kode5siffer_beskrivelse,
                  :naerring_2siffer_beskrivelse,
                  :ssb_sektor_kode,
                  :ssb_sektor_kode_beskrivelse,
                  :fylkesnummer,
                  :fylke,
                  :kommunenummer,
                  :kommune
                )
                """,
            MapSqlParameterSource()
                .addValue("orgnr", iatjeneste.orgnr)
                .addValue("naering_kode_5siffer", iatjeneste.næringKode5Siffer)
                .addValue("form_av_tjeneste", iatjeneste.type.name)
                .addValue("kilde_applikasjon", iatjeneste.kilde.name)
                .addValue("tjeneste_mottakkelsesdato", iatjeneste.tjenesteMottakkelsesdato.toLocalDateTime())
                .addValue("antall_ansatte", iatjeneste.antallAnsatte)
                .addValue("naering_kode5siffer_beskrivelse", iatjeneste.næringskode5SifferBeskrivelse)
                .addValue("naerring_2siffer_beskrivelse", iatjeneste.næring2SifferBeskrivelse)
                .addValue("ssb_sektor_kode", iatjeneste.SSBSektorKode)
                .addValue("ssb_sektor_kode_beskrivelse", iatjeneste.SSBSektorKodeBeskrivelse)
                .addValue("fylkesnummer", iatjeneste.fylkesnummer)
                .addValue("fylke", iatjeneste.fylke)
                .addValue("kommunenummer", iatjeneste.kommunenummer)
                .addValue("kommune", iatjeneste.kommune)
        )
    }
}