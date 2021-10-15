package no.nav.arbeidsgiver.iatjenester.metrikker.controller

import arrow.core.Either
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.UinnloggetMottattIaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.service.IaTjenesterMetrikkerService
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.clearNavCallid
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.setNavCallid
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@Unprotected
@RestController
@CrossOrigin(
    origins = ["https://arbeidsgiver-q.nav.no", "https://arbeidsgiver.labs.nais.io"],
    allowCredentials = "false"
)
@RequestMapping("/uinnlogget")
class IaTjenesterMetrikkerUinnloggetController(private val iaTjenesterMetrikkerService: IaTjenesterMetrikkerService) {

    @PostMapping(value = ["/mottatt-iatjeneste"], consumes = ["application/json"], produces = ["application/json"])
    fun leggTilNyMottattIaTjeneste(
        @RequestHeader headers: HttpHeaders,
        @RequestBody uinnloggetIaTjeneste: UinnloggetMottattIaTjeneste
    ): ResponseEntity<ResponseStatus> {
        setNavCallid(headers)
        log("IaTjenesterMetrikkerUinnloggetController")
            .info("Mottatt IA tjeneste (uinnlogget) fra ${uinnloggetIaTjeneste.kilde.name}")

        val iaSjekk = iaTjenesterMetrikkerService.sjekkOgPersister(uinnloggetIaTjeneste)
        when(iaSjekk){
            is Either.Left -> {
                log("IaTjenesterMetrikkerInnloggetController")
                    .warn(iaSjekk.value.message, iaSjekk.value)
                clearNavCallid()
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ResponseStatus.BadRequest)
            }
            else -> {}
        }

        clearNavCallid()
        return ResponseEntity.status(HttpStatus.CREATED)
            .contentType(MediaType.APPLICATION_JSON)
            .body(ResponseStatus.Created)
    }
}
