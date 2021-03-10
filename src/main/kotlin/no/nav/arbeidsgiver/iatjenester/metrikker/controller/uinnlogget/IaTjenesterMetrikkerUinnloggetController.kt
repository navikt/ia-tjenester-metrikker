package no.nav.arbeidsgiver.iatjenester.metrikker.controller.uinnlogget

import no.nav.arbeidsgiver.iatjenester.metrikker.controller.ResponseStatus
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.UinnloggetIaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.service.IaTjenesterMetrikkerService
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.web.bind.annotation.*


@Unprotected
@RestController
@CrossOrigin(origins = ["https://arbeidsgiver-q.nav.no", "https://arbeidsgiver.labs.nais.io"], allowCredentials = "true")
@RequestMapping("/uinnlogget")
class IaTjenesterMetrikkerUinnloggetController(private val iaTjenesterMetrikkerService: IaTjenesterMetrikkerService) {

    @PostMapping(value = ["/mottatt-iatjeneste"], consumes = ["application/json"], produces = ["application/json"])
    fun leggTilNyMottattIaTjeneste(@RequestBody uinnloggetIaTjeneste: UinnloggetIaTjeneste): ResponseStatus {
        iaTjenesterMetrikkerService.sjekkOgOpprett(uinnloggetIaTjeneste)
        return no.nav.arbeidsgiver.iatjenester.metrikker.controller.ResponseStatus.Created
    }
}
