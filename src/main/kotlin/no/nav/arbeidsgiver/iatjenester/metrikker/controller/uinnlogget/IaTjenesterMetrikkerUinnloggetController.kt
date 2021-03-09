package no.nav.arbeidsgiver.iatjenester.metrikker.controller.uinnlogget

import no.nav.arbeidsgiver.iatjenester.metrikker.controller.ResponseStatus
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.UinnloggetIaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.service.IaTjenesterMetrikkerService
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@Unprotected
@RestController
@RequestMapping("/uinnlogget")
class IaTjenesterMetrikkerUinnloggetController(private val iaTjenesterMetrikkerService: IaTjenesterMetrikkerService) {

    @PostMapping(value = ["/mottatt-iatjeneste"], consumes = ["application/json"], produces = ["application/json"])
    fun leggTilNyMottattIaTjeneste(@RequestBody uinnloggetIaTjeneste: UinnloggetIaTjeneste): ResponseStatus {
        iaTjenesterMetrikkerService.sjekkOgOpprett(uinnloggetIaTjeneste)
        return no.nav.arbeidsgiver.iatjenester.metrikker.controller.ResponseStatus.Created
    }
}
