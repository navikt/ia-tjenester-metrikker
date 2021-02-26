package no.nav.arbeidsgiver.iatjenester.metrikker.datamottakelse

import org.h2.util.json.JSONString
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/metrikker")
class DatamottakelseController() {

    @PostMapping
    fun addCustomer(@RequestBody metrikkerData: JSONString) = println(metrikkerData)
    // = repository.save(customer)

    @PutMapping("/{id}")
    fun updateCustomer(@PathVariable id: Long, @RequestBody metrikekrData: JSONString) {
        println("putmapping--->${metrikekrData}")
        println("id--->${id}")
    }
}
