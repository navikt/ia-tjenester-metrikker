package no.nav.arbeidsgiver.iatjenester.metrikker.repository

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.arbeidsgiver.iatjenester.metrikker.TestUtils
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.Kilde
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.TypeIATjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.IaTjenesteRad
import org.assertj.core.api.Assertions.assertThat
import org.flywaydb.core.Flyway
import org.h2.jdbc.JdbcConnection
import org.h2.tools.Server
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.Connection
import java.sql.ResultSet
import javax.sql.DataSource

class IaTjenesterMetrikkerRepositoryJdbcTest {

    private val IN_MEM_DB2_INTERACTIVE_CONSOLE_ACTIVATED = false

    @Test
    fun `opprett() lagrer en IaTjeneste i DB`() {

        IaTjenesterMetrikkerRepository(NamedParameterJdbcTemplate(dataSource)).opprett(
            TestUtils.vilkårligIaTjeneste()
        )

        startWebConsoleForInMemDatabase(IN_MEM_DB2_INTERACTIVE_CONSOLE_ACTIVATED)
        val antallIATjenester = dataSource.connection.getAlleIATjenester()
        assertThat(antallIATjenester.size).isEqualTo(1)
        val iaTjenesteRad = antallIATjenester[0]
        assertThat(iaTjenesteRad.id).isEqualTo(1)
        assertThat(iaTjenesteRad.orgnr).isEqualTo("987654321")
        assertThat(iaTjenesteRad.næringKode5Siffer).isEqualTo("12345")
        assertThat(iaTjenesteRad.type).isEqualTo(TypeIATjeneste.DIGITAL_IA_TJENESTE)
        assertThat(iaTjenesteRad.kilde).isEqualTo(Kilde.SYKKEFRAVÆRSSTATISTIKK)
        assertThat(iaTjenesteRad.tjeneste_mottakkelsesdato).isNotNull()
        assertThat(iaTjenesteRad.antallAnsatte).isEqualTo(10)
        assertThat(iaTjenesteRad.næringskode5SifferBeskrivelse).isEqualTo("En beskrivelse for næringskode 5 siffer")
        assertThat(iaTjenesteRad.næring2SifferBeskrivelse).isEqualTo("En beskrivelse for næring kode 2 siffer")
        assertThat(iaTjenesteRad.SSBSektorKode).isEqualTo("21000")
        assertThat(iaTjenesteRad.SSBSektorKodeBeskrivelse).isEqualTo("Beskrivelse ssb sektor kode")
        assertThat(iaTjenesteRad.fylkesnummer).isEqualTo("30")
        assertThat(iaTjenesteRad.fylke).isEqualTo("Viken")
        assertThat(iaTjenesteRad.kommunenummer).isEqualTo("0234")
        assertThat(iaTjenesteRad.kommune).isEqualTo("Gjerdrum")
        assertThat(iaTjenesteRad.opprettet).isNotNull()
    }


    companion object {

        private val dataSource: DataSource = HikariConfig().let { config ->
            config.jdbcUrl = "jdbc:h2:mem:ia-tjenester-metrikker"
            config.username = "sa"
            config.password = ""
            config.driverClassName = "org.h2.Driver"
            config.maximumPoolSize = 5
            config.initializationFailTimeout = 60000
            HikariDataSource(config)
        }

        @BeforeAll
        @JvmStatic
        fun `Start og init DB med Flyway`() {
            val flyway = Flyway()
            flyway.dataSource = dataSource
            flyway.setLocations("db/migration")
            flyway.migrate()
        }

        private fun Connection.getAlleIATjenester(): List<IaTjenesteRad> =
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
                næring2SifferBeskrivelse = getString("naerring_2siffer_beskrivelse"),
                SSBSektorKode = getString("ssb_sektor_kode"),
                SSBSektorKodeBeskrivelse = getString("ssb_sektor_kode_beskrivelse"),
                fylkesnummer = getString("fylkesnummer"),
                fylke = getString("fylke"),
                kommunenummer = getString("kommunenummer"),
                kommune = getString("kommune"),
                opprettet = getDate("opprettet")
            )
        }

        private fun startWebConsoleForInMemDatabase(isActivated: Boolean) {
            if (!isActivated) return
            val lokalDBInMemoryServer = Server.createTcpServer(
                "-tcp", "-tcpAllowOthers", "-tcpPort", "9090"
            )
            lokalDBInMemoryServer.start()
            val connection = dataSource.connection.unwrap(JdbcConnection::class.java)
            Server.startWebServer(connection)
        }
    }
}



