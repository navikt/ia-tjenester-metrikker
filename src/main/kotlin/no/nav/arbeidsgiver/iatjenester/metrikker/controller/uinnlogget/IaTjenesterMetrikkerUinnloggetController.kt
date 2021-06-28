package no.nav.arbeidsgiver.iatjenester.metrikker.controller.uinnlogget

import arrow.core.Either
import no.nav.arbeidsgiver.iatjenester.metrikker.controller.ResponseStatus
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.UinnloggetIaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.service.IaTjenesterMetrikkerService
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.setNavCallid
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.clearNavCallid
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity


@Unprotected
@RestController
@CrossOrigin(
    origins = ["https://arbeidsgiver-q.nav.no", "https://arbeidsgiver.labs.nais.io"],
    allowCredentials = "false"
)
@RequestMapping("/uinnlogget")
class IaTjenesterMetrikkerUinnloggetController(private val iaTjenesterMetrikkerService: IaTjenesterMetrikkerService) {

    @PostMapping(value = ["/mottatt-iatjeneste"], consumes = ["application/json"], produces = ["application/json"])
    fun leggTilNyMottattIaTjeneste(@RequestHeader headers: HttpHeaders, @RequestBody uinnloggetIaTjeneste: UinnloggetIaTjeneste): ResponseStatus {
        setNavCallid(headers)
        log("IaTjenesterMetrikkerUinnloggetController")
            .info("Mottatt IA tjeneste (uinnlogget) fra ${uinnloggetIaTjeneste.kilde.name}")

        val iaSjekk = iaTjenesterMetrikkerService.sjekkOgOpprett(uinnloggetIaTjeneste)
        when(iaSjekk){
            is Either.Left -> {
                log("IaTjenesterMetrikkerInnloggetController")
                    .warn(iaSjekk.value.message, iaSjekk.value)
                clearNavCallid()
                return ResponseStatus.BadRequest
            }
            else -> {}
        }

        clearNavCallid()
        return ResponseStatus.Created
    }
}
