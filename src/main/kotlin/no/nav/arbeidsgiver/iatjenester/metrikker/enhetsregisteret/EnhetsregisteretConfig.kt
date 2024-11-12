package no.nav.arbeidsgiver.iatjenester.metrikker.enhetsregisteret

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class EnhetsregisteretConfig(
    private val enhetsregisteretProperties: EnhetsregisteretProperties,
) {
    @Bean
    fun restTemplateEnhetsregisteret(): RestTemplate = RestTemplate()

    @Bean
    fun enhetsregisteretUrl(): String = enhetsregisteretProperties.url

    @Bean
    fun enhetregisteretClient(): EnhetsregisteretClient =
        EnhetsregisteretClient(
            restTemplateEnhetsregisteret(),
            enhetsregisteretProperties.url,
        )
}
