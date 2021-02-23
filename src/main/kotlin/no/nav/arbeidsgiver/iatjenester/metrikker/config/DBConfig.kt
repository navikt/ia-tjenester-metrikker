package no.nav.arbeidsgiver.iatjenester.metrikker.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

class DBConfig(jdbcUrl: String, username: String, password: String, driverClassName: String) {
    private val dataSource: HikariDataSource = HikariConfig().let { config ->
        config.jdbcUrl = jdbcUrl
        config.username = username
        config.password = password
        config.driverClassName = driverClassName
        config.maximumPoolSize = 5
        //config.initializationFailTimeout = 60000
        HikariDataSource(config)
    }

    fun getDataSource(): HikariDataSource {
        return dataSource
    }

}

data class DatabaseCredentials(val miljø: String, val host: String, val port: String, val name: String) {
    fun getUrl(): String {
        return if (miljø == "local")
            "jdbc:h2:mem:${name}"
        else
            "jdbc:postgresql://${host}:${port}/${name}"
    }
}
