package no.nav.arbeidsgiver.iatjenester.metrikker.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource

class DBConfig(jdbcUrl: String, username: String, password: String, driverClassName: String) {
    private val dataSource: DataSource

    init {
        dataSource = HikariConfig().let { config ->
            config.jdbcUrl = jdbcUrl
            config.username = username
            config.password = password
            config.driverClassName = driverClassName
            HikariDataSource(config)
        }
    }

    fun getDataSource(): DataSource {
        return dataSource
    }

}

data class DatabaseCredentials(val miljø: String, val host: String, val port: String, val name: String) {
    fun getUrl(): String {
        return if (miljø == "local")
            "jdbc:h2:mem:ia-tjenester-metrikker"
        else
            "jdbc:postgresql://${host}:${port}/${name}"
    }
}
