package no.nav.arbeidsgiver.iatjenester.metrikker.utils

import org.flywaydb.core.Flyway
import java.util.*
import javax.sql.DataSource

class FlywayMigration(private val dataSource: DataSource) {

    val flyway = Flyway()

    fun setupOgMigrer(erLokalt: Boolean) {
        val locations= mutableListOf("db/migration")
        if (!erLokalt){
            locations.add("db/privileges")
        }
        flyway.setDataSource(dataSource)
        flyway.setLocations(*locations.toTypedArray())
        flyway.migrate()
    }
}
