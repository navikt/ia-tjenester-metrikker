package no.nav.arbeidsgiver.iatjenester.metrikker.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
class DBConfig(private val dbConfigProperties: DBConfigProperties) {

    @Bean
    fun dataSource(): DataSource {
        val url: String= dbConfigProperties.url
        val username: String= dbConfigProperties.username
        val password: String= dbConfigProperties.password
        val driverClassName: String= dbConfigProperties.driverClassName

        println("---------> url is:$url")
        println("---------> username is:$username")
        println("---------> Driver is:$driverClassName")
        return HikariConfig().let { config ->
            config.jdbcUrl = "$url-FROM-DB-CONFIG"
            config.username = username
            config.password = password
            config.driverClassName = driverClassName
            config.maximumPoolSize = 2 // TODO: use spring.datasource.hikari.maximum-pool-size
            config.initializationFailTimeout = 60000
            HikariDataSource(config)
        }
    }
}