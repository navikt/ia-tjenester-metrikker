package no.nav.arbeidsgiver.iatjenester.metrikker

import com.google.gson.Gson
import no.nav.arbeidsgiver.iatjenester.metrikker.config.OutboundKafkaProperties
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.InnloggetIaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class KafkaService(private var kafkaTemplate: KafkaTemplate<String, String>, private var outboundKafkaProperties: OutboundKafkaProperties) {

    val gson = Gson()

    fun send(innloggetIaTjeneste: InnloggetIaTjeneste){
        val futureResult = kafkaTemplate.send(outboundKafkaProperties.topic, gson.toJson(innloggetIaTjeneste))
        futureResult.addCallback(
            {log.info("Melding sendt på topic")},
            {log.error("Feil oppstod ved sending av melding", it)}
        )
    }
}