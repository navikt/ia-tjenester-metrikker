package no.nav.arbeidsgiver.iatjenester.metrikker.controller

import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@Unprotected
@RestController
class DiagnosticsController {
    @GetMapping("/ping")
    fun ping(): ResponseEntity<Unit> = ResponseEntity.ok().build()

    @GetMapping("/internal/isalive")
    fun isalive(): ResponseEntity<String> = ResponseEntity.ok("Is alive")

    @GetMapping("/internal/isready")
    fun isready(): ResponseEntity<String> = ResponseEntity.ok("Is ready")
}
