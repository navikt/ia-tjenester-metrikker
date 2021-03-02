package no.nav.arbeidsgiver.iatjenester.metrikker.controller

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.IaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.service.IaTjenesterMetrikkerService
import no.nav.security.oidc.api.Protected
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/")
class IATjenesterMetrikkerController(private val iaTjenesterMetrikkerService: IaTjenesterMetrikkerService) {

    @Protected
    @PostMapping(value = ["/innlogget/iatjeneste"], consumes = ["application/json"], produces = ["application/json"])
    fun leggTilNyIaMottattTjenesteForInnloggetKlient(@RequestBody iaTjeneste: IaTjeneste): no.nav.arbeidsgiver.iatjenester.metrikker.controller.uinnlogget.ResponseStatus {
        iaTjenesterMetrikkerService.sjekkOgOpprett(iaTjeneste)
        return no.nav.arbeidsgiver.iatjenester.metrikker.controller.uinnlogget.ResponseStatus.Created
    }

}


@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class ResponseStatus(val status: String) {
    Created("created"),
    Error("error")
}

