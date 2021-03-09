package no.nav.arbeidsgiver.iatjenester.metrikker.config

import com.zaxxer.hikari.HikariConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "spring.datasource")
data class DBConfigProperties(
    var hikari: HikariConfig
)
