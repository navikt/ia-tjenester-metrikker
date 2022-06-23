package no.nav.arbeidsgiver.iatjenester.metrikker.config

import io.micrometer.core.aop.CountedAspect
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy


@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
class MetricsConfig() {

    @Bean
    fun registry(): MeterRegistry {
        return SimpleMeterRegistry()
    }

    @Bean
    fun countedAspect(registry: MeterRegistry): CountedAspect {
        return CountedAspect(registry)
    }
}

