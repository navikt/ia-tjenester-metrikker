package no.nav.arbeidsgiver.iatjenester.metrikker.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "altinn-proxy")
data class AltinnConfigProperties(
    var proxyUrl: String,
    var fallbackUrl: String,
    var altinnApiKey: String,
    var apiGwKey: String
)
