package no.nav.arbeidsgiver.iatjenester.metrikker.enhetsregisteret

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding


@ConstructorBinding
@ConfigurationProperties(prefix = "enhetsregisteret")
data class EnhetsregisteretProperties(
    var url: String,
)
