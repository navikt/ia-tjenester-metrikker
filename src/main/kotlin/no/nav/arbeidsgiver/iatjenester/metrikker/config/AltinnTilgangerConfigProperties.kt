package no.nav.arbeidsgiver.iatjenester.metrikker.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "arbeidsgiver-altinn-tilganger")
data class AltinnTilgangerConfigProperties(
    var url: String,
)
