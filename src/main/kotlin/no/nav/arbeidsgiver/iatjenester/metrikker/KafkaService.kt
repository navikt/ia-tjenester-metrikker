package no.nav.arbeidsgiver.iatjenester.metrikker

import com.google.gson.Gson
import no.nav.arbeidsgiver.iatjenester.metrikker.config.OutboundKafkaProperties
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.IaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class KafkaService(private var kafkaTemplate: KafkaTemplate<String, String>, private var outboundKafkaProperties: OutboundKafkaProperties) {

    val gson = Gson()

    fun send(iaTjeneste: IaTjeneste){
        val futureResult = kafkaTemplate.send(outboundKafkaProperties.topic, gson.toJson(iaTjeneste))
        futureResult.addCallback(
            {log.info("Melding sendt på topic")},
            {log.error("Feil oppstod ved sending av melding", it)}
        )
    }
}