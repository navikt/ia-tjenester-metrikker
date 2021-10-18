package no.nav.arbeidsgiver.iatjenester.metrikker

import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.InnloggetMottattIaTjenesteMedVirksomhetGrunndata
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.Kilde
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.TypeIATjeneste
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.time.ZonedDateTime

@Profile("dev-gcp")
@RestController
class KafkaController(var kafkaService: KafkaService) {

    val dummyIaTjeneste = InnloggetMottattIaTjenesteMedVirksomhetGrunndata(
        "orgnr",
        "næringskode",
        TypeIATjeneste.RÅDGIVNING,
        Kilde.DIALOG,
        ZonedDateTime.now(),
        0,
        "næringskode 5 beskrivelse",
        "næringskode 2 beskrivelse",
        "sektorkode",
        "sektorkode beskrivelse",
        "fylke",
        "kommunenummer",
        "kommune"
    )

    @GetMapping("/sendKafka")
    fun sendKafka(): ResponseEntity<String> {
        kafkaService.send(dummyIaTjeneste)
        return ResponseEntity.ok("Melding sendt")
    }
}