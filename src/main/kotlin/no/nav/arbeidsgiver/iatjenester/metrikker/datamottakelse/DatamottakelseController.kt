package no.nav.arbeidsgiver.iatjenester.metrikker.datamottakelse

import no.nav.arbeidsgiver.iatjenester.metrikker.utils.IaTjenesteRad
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/metrikker")
class DatamottakelseController() {

    @PostMapping
    fun leggTilNyMottattTjeneste(@RequestBody metrikkerData: IaTjenesteRad) {

        println(metrikkerData)
        // = repository.save(customer)
    }

    @PutMapping("/{id}")
    fun oppdaterMottatTjeneste(@PathVariable id: Long, @RequestBody metrikekrData: IaTjenesteRad) {
        println("putmapping--->${metrikekrData}")
        println("id--->${id}")
    }
}
