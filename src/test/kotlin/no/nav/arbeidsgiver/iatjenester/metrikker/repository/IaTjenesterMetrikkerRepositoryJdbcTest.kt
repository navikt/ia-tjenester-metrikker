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
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.Næring
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.Kilde
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.TypeIATjeneste
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
        opprettUinnloggetIaTjenester(listOf(Date.valueOf(now()), Date.valueOf(now())))

        val innloggetMetrikker =
            IaTjenesterMetrikkerRepository(NamedParameterJdbcTemplate(dataSource)).hentUinnloggetMetrikker(
                now().withDayOfMonth(
                    1
                ).withMonth(1)
            )

        assertThat(innloggetMetrikker.size).isEqualTo(2)
    }

    @Test
    fun `hentUinnloggetIaTjenesterMetrikker fra en vis dato`() {
        opprettUinnloggetIaTjenester(listOf(Date.valueOf(now()), Date.valueOf(now().minusYears(1))))

        val innloggetMetrikker =
            IaTjenesterMetrikkerRepository(NamedParameterJdbcTemplate(dataSource)).hentUinnloggetMetrikker(
                now().withDayOfMonth(
                    1
                ).withMonth(1)
            )
        startWebConsoleForInMemDatabase(IN_MEM_DB2_INTERACTIVE_CONSOLE_ACTIVATED)
        assertThat(innloggetMetrikker.size).isEqualTo(1)
    }

    @Test
    fun `hentInnloggetIaTjenesterMetrikker`() {
        opprettInnloggetIaTjenesterFraDatoer(
            listOf(
                Date.valueOf(now().minusDays(3)),
                Date.valueOf(now().minusDays(3))
            )
        )

        startWebConsoleForInMemDatabase(IN_MEM_DB2_INTERACTIVE_CONSOLE_ACTIVATED)
        val uinnloggetMetrikker =
            IaTjenesterMetrikkerRepository(NamedParameterJdbcTemplate(dataSource)).hentInnloggetMetrikker(
                now().withDayOfMonth(
                    1
                ).withMonth(1)
            )

        assertThat(uinnloggetMetrikker.size).isEqualTo(2)
    }

    @Test
    fun `hentInnloggetIaTjenesterMetrikker fra en viss dato`() {
        val iaTjenesteMetrikk = IaTjenesterMetrikkerRepository.MottattInnloggetIaTjenesteMetrikk(
            "987654321",
            Kilde.SYKEFRAVÆRSSTATISTIKK,
            Næring("12345", "Test 5 siffer", "Test 2 siffer"),
            "0576",
            "Oslo",
            "Oslo",
            now().atStartOfDay()
        )
        opprettInnloggetIaTjenester(
            listOf(
                iaTjenesteMetrikk
            )
        )

        startWebConsoleForInMemDatabase(IN_MEM_DB2_INTERACTIVE_CONSOLE_ACTIVATED)
        val uinnloggetMetrikker =
            IaTjenesterMetrikkerRepository(NamedParameterJdbcTemplate(dataSource)).hentInnloggetMetrikker(
                now().withDayOfMonth(
                    1
                ).withMonth(1)
            )

        assertThat(uinnloggetMetrikker.size).isEqualTo(1)
        assertThat(uinnloggetMetrikker.get(0)).isEqualTo(iaTjenesteMetrikk)
    }

    @Test
    fun `opprett() lagrer en UinnloggetIaTjeneste i DB`() {
        IaTjenesterMetrikkerRepository(NamedParameterJdbcTemplate(dataSource)).persister(
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

        IaTjenesterMetrikkerRepository(NamedParameterJdbcTemplate(dataSource)).persister(
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
        assertThat(iaTjenesteRad.fylke).isEqualTo("Viken")
        assertThat(iaTjenesteRad.kommunenummer).isEqualTo("0234")
        assertThat(iaTjenesteRad.kommune).isEqualTo("Gjerdrum")
        assertThat(iaTjenesteRad.opprettet).isNotNull()
    }


    private fun opprettUinnloggetIaTjenester(dates: List<Date>) {
        dates.forEachIndexed() { index, date ->
            dataSource.connection.opprettUinnloggetIaTjeneste(
                UinnloggetIaTjenesteRad(
                    id = index + 1,
                    type = TypeIATjeneste.DIGITAL_IA_TJENESTE,
                    kilde = Kilde.SAMTALESTØTTE,
                    Timestamp.valueOf(date.toLocalDate().atStartOfDay()),
                    Date.valueOf(now())
                )
            )
        }
    }

    private fun opprettInnloggetIaTjenester(
        mottatteInnloggetIATjenester: List<IaTjenesterMetrikkerRepository.MottattInnloggetIaTjenesteMetrikk>
    ) {
        mottatteInnloggetIATjenester.forEachIndexed() { index, mottattIATjeneste ->
            dataSource.connection.opprettInnloggetIaTjeneste(
                IaTjenesteRad(
                    id = index + 1,
                    type = TypeIATjeneste.DIGITAL_IA_TJENESTE,
                    kilde = mottattIATjeneste.kilde,
                    orgnr = mottattIATjeneste.orgnr,
                    næringKode5Siffer = mottattIATjeneste.næring.kode5Siffer,
                    næring2SifferBeskrivelse = mottattIATjeneste.næring.kode2SifferBeskrivelse,
                    tjeneste_mottakkelsesdato = Timestamp.valueOf(now().atStartOfDay()),
                    antallAnsatte = 5 + index,
                    næringskode5SifferBeskrivelse = mottattIATjeneste.næring.kode5SifferBeskrivelse,
                    SSBSektorKode = "",
                    SSBSektorKodeBeskrivelse = "",
                    fylke = mottattIATjeneste.fylke,
                    kommunenummer = mottattIATjeneste.kommunenummer,
                    kommune = mottattIATjeneste.kommune,
                    opprettet = Date.valueOf(now())
                )
            )
        }
    }

    private fun opprettInnloggetIaTjenesterFraDatoer(dates: List<Date>) {
        dates.forEachIndexed() { index, date ->
            dataSource.connection.opprettInnloggetIaTjeneste(
                IaTjenesteRad(
                    id = index + 1,
                    type = TypeIATjeneste.DIGITAL_IA_TJENESTE,
                    kilde = Kilde.SYKEFRAVÆRSSTATISTIKK,
                    orgnr = (999999900 + index).toString(),
                    næringKode5Siffer = "",
                    næring2SifferBeskrivelse = "",
                    tjeneste_mottakkelsesdato = Timestamp.valueOf(date.toLocalDate().atStartOfDay()),
                    antallAnsatte = 5 + index,
                    næringskode5SifferBeskrivelse = "",
                    SSBSektorKode = "",
                    SSBSektorKodeBeskrivelse = "",
                    fylke = "",
                    kommunenummer = "",
                    kommune = "",
                    opprettet = Date.valueOf(now())
                )
            )
        }
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



