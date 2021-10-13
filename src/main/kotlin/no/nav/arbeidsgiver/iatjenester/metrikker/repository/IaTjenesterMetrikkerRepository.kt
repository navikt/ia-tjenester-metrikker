package no.nav.arbeidsgiver.iatjenester.metrikker.repository

import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.Næring
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.InnloggetMottattIaTjenesteMedVirksomhetGrunndata
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.Kilde
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.UinnloggetMottattIaTjeneste
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.sql.ResultSet
import java.time.LocalDate
import java.time.LocalDateTime

@Transactional
@Repository
class IaTjenesterMetrikkerRepository(private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate) {


    fun opprett(iatjeneste: InnloggetMottattIaTjenesteMedVirksomhetGrunndata) {
        insertIaTjeneste(iatjeneste)
    }

    fun opprett(uinnloggetIatjeneste: UinnloggetMottattIaTjeneste) {
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

    private fun insertIaTjeneste(iatjeneste: InnloggetMottattIaTjenesteMedVirksomhetGrunndata) {
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
                naering_2siffer_beskrivelse,
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
                  :naering_2siffer_beskrivelse,
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
                .addValue("naering_2siffer_beskrivelse", iatjeneste.næring2SifferBeskrivelse)
                .addValue("ssb_sektor_kode", iatjeneste.SSBSektorKode)
                .addValue("ssb_sektor_kode_beskrivelse", iatjeneste.SSBSektorKodeBeskrivelse)
                .addValue("fylkesnummer", iatjeneste.fylkesnummer)
                .addValue("fylke", iatjeneste.fylke)
                .addValue("kommunenummer", iatjeneste.kommunenummer)
                .addValue("kommune", iatjeneste.kommune)
        )
    }

    sealed class MottattIaTjenesteMetrikk {
        abstract val tidspunkt: LocalDateTime
    }

    data class MottattInnloggetIaTjenesteMetrikk(
        val orgnr: String,
        val kilde: Kilde,
        val næring: Næring,
        val kommunenummer: String,
        val kommune: String,
        override val tidspunkt: LocalDateTime
    ) : MottattIaTjenesteMetrikk()

    data class MottattUinnloggetIaTjenesteMetrikk(val kilde: Kilde, override val tidspunkt: LocalDateTime) :
        MottattIaTjenesteMetrikk()


    fun hentUinnloggetMetrikker(startDato: LocalDate): List<MottattUinnloggetIaTjenesteMetrikk> =
        namedParameterJdbcTemplate.query("""
                select tjeneste_mottakkelsesdato, kilde_applikasjon 
                from metrikker_ia_tjenester_uinnlogget 
                where tjeneste_mottakkelsesdato >= :startDato
                """,
            MapSqlParameterSource().addValue("startDato", startDato),
            RowMapper { rs: ResultSet, _: Int ->
                MottattUinnloggetIaTjenesteMetrikk(
                    Kilde.valueOf(rs.getString("kilde_applikasjon")),
                    rs.getDate("tjeneste_mottakkelsesdato").toLocalDate().atStartOfDay()
                )
            }
        )

    fun hentInnloggetMetrikker(startDato: LocalDate): List<MottattInnloggetIaTjenesteMetrikk> =
        namedParameterJdbcTemplate.query("""
            select orgnr,
              tjeneste_mottakkelsesdato, 
              kilde_applikasjon, 
              naering_kode_5siffer, 
              naering_kode5siffer_beskrivelse, 
              naering_2siffer_beskrivelse, 
              kommunenummer, 
              kommune
            from metrikker_ia_tjenester_innlogget 
            where tjeneste_mottakkelsesdato >= :startDato
            """,
            MapSqlParameterSource().addValue("startDato", startDato),
            RowMapper { rs: ResultSet, _: Int ->
                MottattInnloggetIaTjenesteMetrikk(
                    rs.getString("orgnr"),
                    Kilde.valueOf(rs.getString("kilde_applikasjon")),
                    Næring(
                        rs.getString("naering_kode_5siffer"),
                        rs.getString("naering_kode5siffer_beskrivelse"),
                        rs.getString("naering_2siffer_beskrivelse")),
                    rs.getString("kommunenummer"),
                    rs.getString("kommune"),
                    rs.getDate("tjeneste_mottakkelsesdato").toLocalDate().atStartOfDay()
                )
            }
        )
}