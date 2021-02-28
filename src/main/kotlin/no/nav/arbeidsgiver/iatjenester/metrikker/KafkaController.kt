package no.nav.arbeidsgiver.iatjenester.metrikker

import no.nav.arbeidsgiver.iatjenester.metrikker.domene.IaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.Kilde
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.TypeIATjeneste
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.sql.Timestamp
import java.time.Instant

@RestController
class KafkaController(var kafkaService: KafkaService) {

    val dummyIaTjeneste = IaTjeneste(
        "orgnr",
        "næringskode",
        TypeIATjeneste.RÅDGIVNING,
        Kilde.DIALOG,
        Timestamp.from(Instant.now()),
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
    fun ping(): ResponseEntity<Unit> {
        kafkaService.send(dummyIaTjeneste)
        return ResponseEntity.ok("Melding sendt").build()
    }
}