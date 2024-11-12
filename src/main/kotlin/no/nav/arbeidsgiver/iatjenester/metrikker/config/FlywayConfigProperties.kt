package no.nav.arbeidsgiver.iatjenester.metrikker.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.flyway")
data class FlywayConfigProperties(
    var locations: Array<String>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FlywayConfigProperties

        if (!locations.contentEquals(other.locations)) return false

        return true
    }

    override fun hashCode(): Int = locations.contentHashCode()
}
