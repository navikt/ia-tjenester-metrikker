package no.nav.arbeidsgiver.iatjenester.metrikker.config

import no.nav.arbeidsgiver.iatjenester.metrikker.altinn.AltinnTilgangerKlient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class AltinnTilgangerKlientConfig(
    private val altinnTilgangerConfigProperties: AltinnTilgangerConfigProperties,
) {
    @Bean(name = ["restTemplateAltinnTilganger"])
    fun restTemplateAltinnTilganger(): RestTemplate = RestTemplate()

    @Bean
    fun altinnTilgangerApiUrl(): String = "${altinnTilgangerConfigProperties.url}/altinn-tilganger"

    @Bean
    fun altinnTilgangerKlient(): AltinnTilgangerKlient =
        AltinnTilgangerKlient(
            altinnTilgangerApiUrl = altinnTilgangerApiUrl(),
            restTemplate = restTemplateAltinnTilganger(),
        )
}
