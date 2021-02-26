package no.nav.arbeidsgiver.iatjenester.metrikker.datamottakelse

import no.nav.arbeidsgiver.iatjenester.metrikker.utils.IaTjenesteRad
import org.h2.util.json.JSONString
import org.springframework.web.bind.annotation.*
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

@RestController
@RequestMapping("/metrikker")
class DatamottakelseController() {

    @PostMapping
    fun addCustomer(@RequestBody metrikkerData: IaTjenesteRad) = println(metrikkerData)
    // = repository.save(customer)

    @PutMapping("/{id}")
    fun updateCustomer(@PathVariable id: Long, @RequestBody metrikekrData: IaTjenesteRad) {
        println("putmapping--->${metrikekrData}")
        println("id--->${id}")
    }
}
