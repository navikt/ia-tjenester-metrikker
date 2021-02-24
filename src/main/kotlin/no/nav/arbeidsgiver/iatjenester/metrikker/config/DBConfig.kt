package no.nav.arbeidsgiver.iatjenester.metrikker.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("local", "dev-gcp")
@Configuration
class DBConfig(
    @Value("\${spring.datasource.url}") private val jdbcUrl: String,
    @Value("\${spring.datasource.username}") private val username: String,
    @Value("\${spring.datasource.password}") private val password: String,
    @Value("\${spring.datasource.driver-class-name}") private val driverClassName: String
) {

    private val hikariDataSource: HikariDataSource = HikariConfig().let { config ->
        config.jdbcUrl = jdbcUrl
        config.username = username
        config.password = password
        config.driverClassName = driverClassName
        config.maximumPoolSize = 5
        config.initializationFailTimeout = 60000
        HikariDataSource(config)
    }

    fun getDataSource(): HikariDataSource {
        return hikariDataSource
    }

}