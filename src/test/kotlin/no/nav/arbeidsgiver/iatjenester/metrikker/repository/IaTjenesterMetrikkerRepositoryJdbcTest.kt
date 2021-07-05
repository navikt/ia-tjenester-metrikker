package no.nav.arbeidsgiver.iatjenester.metrikker.repository

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.arbeidsgiver.iatjenester.metrikker.IaTjenesteRad
import no.nav.arbeidsgiver.iatjenester.metrikker.TestUtils
import no.nav.arbeidsgiver.iatjenester.metrikker.TestUtils.Companion.cleanTable
import no.nav.arbeidsgiver.iatjenester.metrikker.TestUtils.Companion.getAlleIATjenester
import no.nav.arbeidsgiver.iatjenester.metrikker.TestUtils.Companion.getAlleUinnloggetIaTjenester
import no.nav.arbeidsgiver.iatjenester.metrikker.TestUtils.Companion.opprettInnloggetIaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.TestUtils.Companion.opprettUinnloggetIaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.UinnloggetIaTjenesteRad
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.Kilde
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.TypeIATjeneste
import org.assertj.core.api.Assertions.assertThat
import org.flywaydb.core.Flyway
import org.h2.jdbc.JdbcConnection
import org.h2.tools.Server
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.Date
import java.sql.Timestamp
import java.time.LocalDate.now
import javax.sql.DataSource

class IaTjenesterMetrikkerRepositoryJdbcTest {

    private val IN_MEM_DB2_INTERACTIVE_CONSOLE_ACTIVATED = false

    @BeforeEach
    fun cleanUp() {
        dataSource.connection.cleanTable("metrikker_ia_tjenester_innlogget")
        dataSource.connection.cleanTable("metrikker_ia_tjenester_uinnlogget")
    }


    @Test
    fun `hentUinnloggetIaTjenesterMetrikker`() {
        dataSource.connection.opprettUinnloggetIaTjeneste(
            UinnloggetIaTjenesteRad(
                id = 1,
                type = TypeIATjeneste.DIGITAL_IA_TJENESTE,
                kilde = Kilde.SAMTALESTØTTE,
                Timestamp.valueOf(now().minusDays(3).atStartOfDay()),
                Date.valueOf(
                    now()
                )
            )
        )
        dataSource.connection.opprettUinnloggetIaTjeneste(
            UinnloggetIaTjenesteRad(
                id = 2,
                type = TypeIATjeneste.DIGITAL_IA_TJENESTE,
                kilde = Kilde.SAMTALESTØTTE,
                Timestamp.valueOf(now().minusDays(2).atStartOfDay()),
                Date.valueOf(
                    now()
                )
            )
        )

        startWebConsoleForInMemDatabase(IN_MEM_DB2_INTERACTIVE_CONSOLE_ACTIVATED)
        val innloggetMetrikker =
            IaTjenesterMetrikkerRepository(NamedParameterJdbcTemplate(dataSource)).hentUinnloggetMetrikker(now())

        assertThat(innloggetMetrikker.size).isEqualTo(2)
    }

    @Test
    fun `hentInnloggetIaTjenesterMetrikker`() {
        dataSource.connection.opprettInnloggetIaTjeneste(
            IaTjenesteRad(
                id = 1,
                type = TypeIATjeneste.DIGITAL_IA_TJENESTE,
                kilde = Kilde.SYKEFRAVÆRSSTATISTIKK,
                orgnr = "999999999",
                næringKode5Siffer = "",
                næring2SifferBeskrivelse = "",
                tjeneste_mottakkelsesdato = Timestamp.valueOf(now().minusDays(3).atStartOfDay()),
                antallAnsatte = 23,
                næringskode5SifferBeskrivelse = "",
                SSBSektorKode = "",
                SSBSektorKodeBeskrivelse = "",
                fylkesnummer = "",
                fylke = "",
                kommunenummer = "",
                kommune = "",
                opprettet = Date.valueOf(now())
            )
        )
        dataSource.connection.opprettInnloggetIaTjeneste(
            IaTjenesteRad(
                id = 1,
                type = TypeIATjeneste.DIGITAL_IA_TJENESTE,
                kilde = Kilde.SYKEFRAVÆRSSTATISTIKK,
                orgnr = "888888888",
                næringKode5Siffer = "",
                næring2SifferBeskrivelse = "",
                tjeneste_mottakkelsesdato = Timestamp.valueOf(now().minusDays(2).atStartOfDay()),
                antallAnsatte = 5,
                næringskode5SifferBeskrivelse = "",
                SSBSektorKode = "",
                SSBSektorKodeBeskrivelse = "",
                fylkesnummer = "",
                fylke = "",
                kommunenummer = "",
                kommune = "",
                opprettet = Date.valueOf(now())
            )
        )

        startWebConsoleForInMemDatabase(IN_MEM_DB2_INTERACTIVE_CONSOLE_ACTIVATED)
        val uinnloggetMetrikker =
            IaTjenesterMetrikkerRepository(NamedParameterJdbcTemplate(dataSource)).hentInnloggetMetrikker(now())

        assertThat(uinnloggetMetrikker.size).isEqualTo(2)
    }

    @Test
    fun `opprett() lagrer en UinnloggetIaTjeneste i DB`() {
        IaTjenesterMetrikkerRepository(NamedParameterJdbcTemplate(dataSource)).opprett(
            TestUtils.vilkårligUinnloggetIaTjeneste()
        )

        startWebConsoleForInMemDatabase(IN_MEM_DB2_INTERACTIVE_CONSOLE_ACTIVATED)
        val antallUinnloggetIaTjenester = dataSource.connection.getAlleUinnloggetIaTjenester()
        assertThat(antallUinnloggetIaTjenester.size).isEqualTo(1)
        val iaTjenesteRad = antallUinnloggetIaTjenester[0]
        assertThat(iaTjenesteRad.type).isEqualTo(TypeIATjeneste.DIGITAL_IA_TJENESTE)
        assertThat(iaTjenesteRad.kilde).isEqualTo(Kilde.SYKEFRAVÆRSSTATISTIKK)
        assertThat(iaTjenesteRad.tjeneste_mottakkelsesdato).isNotNull()
        assertThat(iaTjenesteRad.opprettet).isNotNull()
    }

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
        assertThat(iaTjenesteRad.kilde).isEqualTo(Kilde.SYKEFRAVÆRSSTATISTIKK)
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



