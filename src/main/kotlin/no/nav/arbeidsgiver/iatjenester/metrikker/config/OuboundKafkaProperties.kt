package no.nav.arbeidsgiver.iatjenester.metrikker.config

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "kafka.outbound")
@ConstructorBinding
class OutboundKafkaProperties(var topic: String = "",
                              var bootstrapServers: String? = null,
                              var caPath: String? = null,
                              var truststorePath: String? = null,
                              var keystorePath: String? = null,
                              var credstorePassword: String? = null,
                              var acks: String = "all",
                              var securityProtocol: String? = null,
                              var clientId: String = "ia-tjenester-metrikker",
                              var valueSerializerClass: String = StringSerializer::class.java.name,
                              var keySerializerCLass: String = StringSerializer::class.java.name,
                              var retries: Int = Int.MAX_VALUE,
                              var deliveryTimeoutMs: Int = 10100,
                              var requestTimeoutMs: Int = 10000,
                              var lingerMs: Int = 100,
                              var batchSize: Int = 16384*4
) {
    fun asProperties(): Map<String, Any> {
        val props = mutableMapOf<String, Any>()
        bootstrapServers?.let { props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers!! }
        credstorePassword?.let {
            props[SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG] = credstorePassword!!
            props[SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG] = credstorePassword!!
            props[SslConfigs.SSL_KEY_PASSWORD_CONFIG] = credstorePassword!!
            props[SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG] = "JKS"
            props[SslConfigs.SSL_KEYSTORE_TYPE_CONFIG] = "PKCS12"
        }
        truststorePath?.let { props[SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG] = truststorePath!! }
        keystorePath?.let { props[SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG] = keystorePath!! }
        securityProtocol?.let { props[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = securityProtocol!! }

        props[ProducerConfig.CLIENT_ID_CONFIG] = clientId
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = valueSerializerClass
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = keySerializerCLass

        props[ProducerConfig.RETRIES_CONFIG] = retries
        props[ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG] = deliveryTimeoutMs
        props[ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG] = requestTimeoutMs
        props[ProducerConfig.LINGER_MS_CONFIG] = lingerMs
        props[ProducerConfig.BATCH_SIZE_CONFIG] = batchSize
        props[ProducerConfig.ACKS_CONFIG] = acks

        return props
    }
}