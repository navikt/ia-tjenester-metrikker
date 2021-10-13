package no.nav.arbeidsgiver.iatjenester.metrikker

import com.google.gson.Gson
import no.nav.arbeidsgiver.iatjenester.metrikker.config.OutboundKafkaProperties
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.InnloggetMottattIaTjenesteMedVirksomhetGrunndata
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log
import org.springframework.context.annotation.Profile
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Profile("dev-gcp")
@Service
class KafkaService(private var kafkaTemplate: KafkaTemplate<String, String>, private var outboundKafkaProperties: OutboundKafkaProperties) {

    val gson = Gson()

    fun send(innloggetIaTjenesteMedVirksomhetGrunndata: InnloggetMottattIaTjenesteMedVirksomhetGrunndata){
        val futureResult = kafkaTemplate.send(outboundKafkaProperties.topic, gson.toJson(innloggetIaTjenesteMedVirksomhetGrunndata))
        futureResult.addCallback(
            {log.info("Melding sendt på topic")},
            {log.error("Feil oppstod ved sending av melding", it)}
        )
    }
}