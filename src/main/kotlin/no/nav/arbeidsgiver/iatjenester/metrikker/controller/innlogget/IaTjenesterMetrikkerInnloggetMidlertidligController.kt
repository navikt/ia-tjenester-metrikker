package no.nav.arbeidsgiver.iatjenester.metrikker.controller.innlogget

import no.nav.arbeidsgiver.iatjenester.metrikker.controller.ResponseStatus
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.InnloggetIaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.service.IaTjenesterMetrikkerService
import no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll.InnloggetBruker
import no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll.Orgnr
import no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll.TilgangskontrollService
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.clearNavCallid
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.setNavCallid
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.sjekkDataKvalitet
import no.nav.security.token.support.core.api.Protected
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
    allowCredentials = "true"
)
@RequestMapping("/gjenopprett/innlogget")
class IaTjenesterMetrikkerInnloggetMidlertidligController(
    private val iaTjenesterMetrikkerService: IaTjenesterMetrikkerService,
    private val tilgangskontrollService: TilgangskontrollService
) {

    @PostMapping(value = ["/mottatt-iatjeneste"], consumes = ["application/json"], produces = ["application/json"])
    fun leggTilNyIaMottattTjenesteForInnloggetKlient(
        @RequestHeader headers: HttpHeaders,
        @RequestBody innloggetIaTjeneste: InnloggetIaTjeneste
    ): ResponseEntity<ResponseStatus> {
        setNavCallid(headers)
        log("IaTjenesterMetrikkerInnloggetMidlertidligController")
            .info("Mottatt IA tjeneste (innlogget) fra ${innloggetIaTjeneste.kilde.name} til gjenopprettelse")

        if (!sjekkDataKvalitet(innloggetIaTjeneste)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ResponseStatus.BadRequest)
        }
        iaTjenesterMetrikkerService.sjekkOgOpprett(innloggetIaTjeneste)

        clearNavCallid()
        return ResponseEntity.status(HttpStatus.CREATED)
            .contentType(MediaType.APPLICATION_JSON)
            .body(ResponseStatus.Created)
    }


}


