package no.nav.arbeidsgiver.iatjenester.metrikker.config

import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnConfig
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnrettigheterProxyKlient
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnrettigheterProxyKlientConfig
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.ProxyConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AltinnKlientConfig(
    private val altinnConfigProperties: AltinnConfigProperties,
) {
    @Bean
    fun altinnrettigheterProxyKlient(): AltinnrettigheterProxyKlient {
        val proxyKlientConfig = AltinnrettigheterProxyKlientConfig(
            ProxyConfig("ia-tjenester-metrikker", altinnConfigProperties.proxyUrl),
            AltinnConfig(
                altinnConfigProperties.fallbackUrl,
                altinnConfigProperties.altinnApiKey,
                altinnConfigProperties.apiGwKey,
            ),
        )
        return AltinnrettigheterProxyKlient(proxyKlientConfig)
    }
}
