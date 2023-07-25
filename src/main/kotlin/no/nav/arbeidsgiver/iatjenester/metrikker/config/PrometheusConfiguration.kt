package no.nav.arbeidsgiver.iatjenester.metrikker.config

import io.prometheus.client.CollectorRegistry
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class PrometheusConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun collectorRegistry(): CollectorRegistry {
        return CollectorRegistry.defaultRegistry
    }
}

