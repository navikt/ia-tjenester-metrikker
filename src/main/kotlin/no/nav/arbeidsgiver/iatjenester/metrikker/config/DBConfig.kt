package no.nav.arbeidsgiver.iatjenester.metrikker.config

import com.zaxxer.hikari.HikariDataSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
class DBConfig(
    private val dbConfigProperties: DBConfigProperties,
) {
    @Bean
    fun dataSource(): DataSource = HikariDataSource(dbConfigProperties.hikari)
}
