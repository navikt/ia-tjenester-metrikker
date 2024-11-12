package no.nav.arbeidsgiver.iatjenester.metrikker.config

import org.flywaydb.core.Flyway
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
class FlywayConfig(
    private val flywayConfigProperties: FlywayConfigProperties,
) {
    @Bean
    fun flyway(dataSource: DataSource): Flyway {
        val flywayConfig = Flyway
            .configure()
            .dataSource(dataSource)
        if (flywayConfigProperties.locations.isNotEmpty()) {
            flywayConfig.locations(*flywayConfigProperties.locations)
        }
        return flywayConfig.load()
    }

    @Bean
    fun flywayMigrationStrategy() =
        FlywayMigrationStrategy { flyway ->
            flyway.migrate()
        }

    @Bean
    fun flywayMigrationInitializer(
        flyway: Flyway,
        flywayMigrationStrategy: FlywayMigrationStrategy,
    ): FlywayMigrationInitializer = FlywayMigrationInitializer(flyway, flywayMigrationStrategy)
}
