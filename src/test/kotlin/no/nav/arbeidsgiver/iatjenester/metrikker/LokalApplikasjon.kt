package no.nav.arbeidsgiver.iatjenester.metrikker

import no.nav.arbeidsgiver.iatjenester.metrikker.config.DBConfig
import org.flywaydb.core.Flyway
import org.h2.tools.Server

fun main() {

    // Vi starter DB serveren lokalt
    Server.createPgServer().start()
    // Vi oppretter en DS til denne DB
    val dataSource = DBConfig("jdbc:h2:mem:ia-tjenester-metrikker", "sa", "").getDataSource()

    // Nå skal vi migrere vår DB lokalt !
    val flyway = Flyway()
    flyway.dataSource = dataSource
    flyway.setLocations("db/migration")
    flyway.migrate()

    // Og til slutt kaller vi applikasjon (akkurat som i PROD)
    App().start()
}
