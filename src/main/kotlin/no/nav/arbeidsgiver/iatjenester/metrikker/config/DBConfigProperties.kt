package no.nav.arbeidsgiver.iatjenester.metrikker.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "spring.datasource")
data class DBConfigProperties(
    var url: String = "jdbc:h2:mem:DEFAULT-CONSTRUCTOR-VALUE",
    var username: String = "sa",
    var password: String = "",
    var driverClassName: String = "org.h2.Driver"
)
