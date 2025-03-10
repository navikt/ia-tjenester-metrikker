package no.nav.arbeidsgiver.iatjenester.metrikker.repository

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.arbeidsgiver.iatjenester.metrikker.IaTjenesteRad
import no.nav.arbeidsgiver.iatjenester.metrikker.TestUtils
import no.nav.arbeidsgiver.iatjenester.metrikker.TestUtils.Companion.cleanTable
import no.nav.arbeidsgiver.iatjenester.metrikker.TestUtils.Companion.getAlleIATjenester
import no.nav.arbeidsgiver.iatjenester.metrikker.TestUtils.Companion.opprettInnloggetIaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.Næring
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
    private val inMemDb2InteractiveConsoleActivated = false

    @BeforeEach
    fun cleanUp() {
        dataSource.connection.cleanTable("metrikker_ia_tjenester_innlogget")
    }

    @Test
    fun `hentInnloggetIaTjenesterMetrikker fungerer`() {
        opprettInnloggetIaTjenesterFraDatoer(
            listOf(
                Date.valueOf(now().minusDays(3)),
                Date.valueOf(now().minusDays(3)),
            ),
        )

        startWebConsoleForInMemDatabase(inMemDb2InteractiveConsoleActivated)
        val innloggetMetrikker =
            IaTjenesterMetrikkerRepository(NamedParameterJdbcTemplate(dataSource)).hentInnloggetMetrikker(
                now().withDayOfMonth(
                    1,
                ).withMonth(1),
            )

        assertThat(innloggetMetrikker.size).isEqualTo(2)
    }

    @Test
    fun `hentInnloggetIaTjenesterMetrikker fra en viss dato`() {
        val iaTjenesteMetrikk = IaTjenesterMetrikkerRepository.MottattInnloggetIaTjenesteMetrikk(
            "987654321",
            Kilde.FOREBYGGE_FRAVÆR,
            Næring("12345", "Test 5 siffer", "Test 2 siffer"),
            "0576",
            "Oslo",
            "Oslo",
            now().atStartOfDay(),
        )
        opprettInnloggetIaTjenester(
            listOf(
                iaTjenesteMetrikk,
            ),
        )

        startWebConsoleForInMemDatabase(inMemDb2InteractiveConsoleActivated)
        val innloggetMetrikker =
            IaTjenesterMetrikkerRepository(NamedParameterJdbcTemplate(dataSource)).hentInnloggetMetrikker(
                now().withDayOfMonth(
                    1,
                ).withMonth(1),
            )

        assertThat(innloggetMetrikker.size).isEqualTo(1)
        assertThat(innloggetMetrikker.get(0)).isEqualTo(iaTjenesteMetrikk)
    }

    @Test
    fun `opprett() lagrer en IaTjeneste i DB`() {
        IaTjenesterMetrikkerRepository(NamedParameterJdbcTemplate(dataSource)).persister(
            TestUtils.vilkårligIaTjeneste(),
        )

        startWebConsoleForInMemDatabase(inMemDb2InteractiveConsoleActivated)
        val antallIATjenester = dataSource.connection.getAlleIATjenester()
        assertThat(antallIATjenester.size).isEqualTo(1)
        val iaTjenesteRad = antallIATjenester[0]
        assertThat(iaTjenesteRad.orgnr).isEqualTo("987654321")
        assertThat(iaTjenesteRad.næringKode5Siffer).isEqualTo("12345")
        assertThat(iaTjenesteRad.type).isEqualTo(TypeIATjeneste.DIGITAL_IA_TJENESTE)
        assertThat(iaTjenesteRad.kilde).isEqualTo(Kilde.FOREBYGGE_FRAVÆR)
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

    private fun opprettInnloggetIaTjenester(
        mottatteInnloggetIATjenester: List<IaTjenesterMetrikkerRepository.MottattInnloggetIaTjenesteMetrikk>,
    ) {
        mottatteInnloggetIATjenester.forEachIndexed { index, mottattIATjeneste ->
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
                    opprettet = Date.valueOf(now()),
                ),
            )
        }
    }

    private fun opprettInnloggetIaTjenesterFraDatoer(dates: List<Date>) {
        dates.forEachIndexed { index, date ->
            dataSource.connection.opprettInnloggetIaTjeneste(
                IaTjenesteRad(
                    id = index + 1,
                    type = TypeIATjeneste.DIGITAL_IA_TJENESTE,
                    kilde = Kilde.FOREBYGGE_FRAVÆR,
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
                    opprettet = Date.valueOf(now()),
                ),
            )
        }
    }

    companion object {
        private val dataSource: DataSource = HikariConfig().let { config ->
            config.jdbcUrl = "jdbc:h2:mem:ia-tjenester-metrikker;MODE=PostgreSQL"
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
            Flyway
                .configure()
                .dataSource(dataSource)
                .locations("db/migration")
                .load()
                .migrate()
        }

        private fun startWebConsoleForInMemDatabase(isActivated: Boolean) {
            if (!isActivated) return
            val lokalDBInMemoryServer = Server.createTcpServer(
                "-tcp",
                "-tcpAllowOthers",
                "-tcpPort",
                "9090",
            )
            lokalDBInMemoryServer.start()
            val connection = dataSource.connection.unwrap(JdbcConnection::class.java)
            Server.startWebServer(connection)
        }
    }
}
