package no.nav.arbeidsgiver.iatjenester.metrikker.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "spring.datasource")
data class DBConfigProperties(
    var url: String,
    var username: String,
    var password: String,
    var driverClassName: String
)
