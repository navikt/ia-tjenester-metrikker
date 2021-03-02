package no.nav.arbeidsgiver.iatjenester.metrikker.controller.uinnlogget

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.IaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.service.IaTjenesterMetrikkerService
import no.nav.security.oidc.api.Protected
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/uinnlogget")
class IATjenesterMetrikkerUinnloggetController(private val iaTjenesterMetrikkerService: IaTjenesterMetrikkerService) {

    @PostMapping(value = ["/iatjeneste"], consumes = ["application/json"], produces = ["application/json"])
    fun leggTilNyMottattIaTjeneste(@RequestBody iaTjeneste: IaTjeneste): no.nav.arbeidsgiver.iatjenester.metrikker.controller.ResponseStatus {
        iaTjenesterMetrikkerService.sjekkOgOpprett(iaTjeneste)
        return no.nav.arbeidsgiver.iatjenester.metrikker.controller.ResponseStatus.Created
    }
}


@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class ResponseStatus(val status: String) {
    Created("created"),
    Error("error")
}

