package no.nav.arbeidsgiver.iatjenester.metrikker.controller.innlogget

import no.nav.arbeidsgiver.iatjenester.metrikker.controller.ResponseStatus
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.InnloggetIaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.service.IaTjenesterMetrikkerService
import no.nav.security.token.support.core.api.Protected
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.CrossOrigin

@Protected
@RestController
@CrossOrigin(origins = ["https://arbeidsgiver-q.nav.no", "https://arbeidsgiver.labs.nais.io"], allowCredentials = "true")
@RequestMapping("/innlogget")
class IaTjenesterMetrikkerInnloggetController(private val iaTjenesterMetrikkerService: IaTjenesterMetrikkerService) {

    @PostMapping(value = ["/mottatt-iatjeneste"], consumes = ["application/json"], produces = ["application/json"])
    fun leggTilNyIaMottattTjenesteForInnloggetKlient(@RequestBody innloggetIaTjeneste: InnloggetIaTjeneste): ResponseStatus {
        iaTjenesterMetrikkerService.sjekkOgOpprett(innloggetIaTjeneste)
        return ResponseStatus.Created
    }
}

