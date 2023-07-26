package no.nav.arbeidsgiver.iatjenester.metrikker.enhetsregisteret

import org.springframework.boot.context.properties.ConfigurationProperties


@ConfigurationProperties(prefix = "enhetsregisteret")
data class EnhetsregisteretProperties(
    var url: String,
)
