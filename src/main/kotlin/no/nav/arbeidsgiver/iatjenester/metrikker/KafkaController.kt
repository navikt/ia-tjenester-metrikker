package no.nav.arbeidsgiver.iatjenester.metrikker

import no.nav.arbeidsgiver.iatjenester.metrikker.domene.InnloggetIaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.Kilde
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.TypeIATjeneste
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.time.ZonedDateTime

@RestController
class KafkaController(var kafkaService: KafkaService) {

    val dummyIaTjeneste = InnloggetIaTjeneste(
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
        "fylkesnummer",
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