package no.nav.arbeidsgiver.iatjenester.metrikker.controller.uinnlogget

import no.nav.arbeidsgiver.iatjenester.metrikker.domene.IaTjeneste
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
    fun leggTilNyMottattIaTjeneste(@RequestBody iaTjeneste: IaTjeneste): no.nav.arbeidsgiver.iatjenester.metrikker.controller.ResponseStatus {
        iaTjenesterMetrikkerService.sjekkOgOpprett(iaTjeneste)
        return no.nav.arbeidsgiver.iatjenester.metrikker.controller.ResponseStatus.Created
    }
}
