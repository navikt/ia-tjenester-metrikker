package no.nav.arbeidsgiver.iatjenester.metrikker.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate

@Configuration
@Profile("dev-gcp")
class KafkaProducerConfig {

    @Bean
    fun kafkaTemplate(outboundKafkaProperties: OutboundKafkaProperties): KafkaTemplate<String, String> {
        return KafkaTemplate(DefaultKafkaProducerFactory(outboundKafkaProperties.asProperties()))
    }
}