package no.nav.arbeidsgiver.iatjenester.metrikker

import com.zaxxer.hikari.HikariDataSource
import no.nav.arbeidsgiver.iatjenester.metrikker.config.DBConfig
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.IaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.Kilde
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.TypeIATjeneste
import org.assertj.core.api.Assertions.assertThat
import org.flywaydb.core.Flyway
import org.h2.jdbc.JdbcConnection
import org.h2.tools.Server
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.sql.Connection
import java.sql.Date
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.LocalDateTime.now

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IaTjenesterMetrikkerRepositoryJdbcTest {

    private val dataSource: HikariDataSource
        get() {
            return DBConfig(
                "jdbc:h2:mem:ia-tjenester-metrikker",
                "sa",
                "",
                "org.h2.Driver"
            ).getDataSource()
        }

    @BeforeAll
    fun `Start og init DB med Flyway`() {
        System.out.println("Først starter vi en in-memory DB");

        // Nå skal vi migrere vår DB lokalt !
        val flyway = Flyway()
        flyway.dataSource = dataSource
        flyway.setLocations("db/migration")
        flyway.migrate()
    }

    @Test
    fun `Enkel test som sjekker at repository oppretter en rad i DB`() {

        IaTjenesterMetrikkerRepository(dataSource).connection.opprett(
            IaTjeneste(
                "987654321",
                "12345",
                TypeIATjeneste.DIGITAL_IA_TJENESTE,
                Kilde.SYKKEFRAVÆRSSTATISTIKK,
                Timestamp.valueOf(now()),
                10,
                "en beskrivelse for næringskode 5 siffer",
                "en beskrivelse for næring kode 2 siffer",
                "21000",
                "beskrivelse ssb sektor kode",
                "30",
                "Viken",
                "0234",
                "Gjerdrum"
            )
        )

        startWebConsoleForInMemDatabase(false)
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
        assertThat(iaTjenesteRad.næringskode5SifferBeskrivelse).isEqualTo("en beskrivelse for næringskode 5 siffer")
        assertThat(iaTjenesteRad.næring2SifferBeskrivelse).isEqualTo("en beskrivelse for næring kode 2 siffer")
        assertThat(iaTjenesteRad.SSBSektorKode).isEqualTo("21000")
        assertThat(iaTjenesteRad.SSBSektorKodeBeskrivelse).isEqualTo("beskrivelse ssb sektor kode")
        assertThat(iaTjenesteRad.fylkesnummer).isEqualTo("30")
        assertThat(iaTjenesteRad.fylke).isEqualTo("Viken")
        assertThat(iaTjenesteRad.kommunenummer).isEqualTo("0234")
        assertThat(iaTjenesteRad.kommune).isEqualTo("Gjerdrum")
        assertThat(iaTjenesteRad.opprettet).isNotNull()
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

fun Connection.getAntallIATjenester(): Int =
    use {
        this.prepareStatement(
            """
                SELECT id
                FROM metrikker_ia_tjenester_innlogget
                """
        ).use {
            val resultSet = it.executeQuery()
            val fetchSize = resultSet.fetchSize
            println("Size? $fetchSize")
            resultSet.getInt(1)
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
    val opprettet: Date
)
