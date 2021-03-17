package no.nav.arbeidsgiver.iatjenester.metrikker.controller.uinnlogget

import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@Unprotected
@RestController
class SjekkController {

    @GetMapping("/sjekk")
    fun sjekk(): ResponseEntity<Unit> {
        return ResponseEntity.ok().build()
    }
}