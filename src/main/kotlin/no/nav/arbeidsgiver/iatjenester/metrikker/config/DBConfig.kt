package no.nav.arbeidsgiver.iatjenester.metrikker.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

// TODO: bare brukt til test --> bruk dependency injection i stedet
class DBConfig(
    private val jdbcUrl: String,
    private val username: String,
    private val password: String,
    private val driverClassName: String
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