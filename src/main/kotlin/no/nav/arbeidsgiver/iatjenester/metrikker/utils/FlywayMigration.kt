package no.nav.arbeidsgiver.iatjenester.metrikker.utils

import org.flywaydb.core.Flyway
import javax.sql.DataSource

class FlywayMigration(private val dataSource: DataSource) {

    val flyway = Flyway()

    fun setupOgMigrer() {
        flyway.setDataSource(dataSource)
        flyway.setLocations("db/migration")
        flyway.migrate()
    }
}