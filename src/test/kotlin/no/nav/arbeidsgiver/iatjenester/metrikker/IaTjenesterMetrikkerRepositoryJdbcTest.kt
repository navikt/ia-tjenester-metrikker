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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IaTjenesterMetrikkerRepositoryJdbcTest {


    // jdbc:h2:mem:ia-tjenester-metrikker

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
                Kilde.SYKKEFRAVÆRSSTATISTIKK
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
                SELECT id, orgnr, naering_kode_5siffer, form_av_tjeneste, kilde_applikasjon, opprettet 
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
        opprettet = getDate("opprettet")
    )
}

data class IaTjenesteRad(
    val id: Int,
    val orgnr: String,
    val næringKode5Siffer: String,
    val type: TypeIATjeneste,
    val kilde: Kilde,
    val opprettet: Date
)