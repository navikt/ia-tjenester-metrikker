package no.nav.arbeidsgiver.iatjenester.metrikker.config

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class MetricsConfig {

    @Bean
    fun registry(): MeterRegistry {
        return SimpleMeterRegistry()
    }

}

