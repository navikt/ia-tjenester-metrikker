package no.nav.arbeidsgiver.iatjenester.metrikker.controller.innlogget

import arrow.core.Either
import arrow.core.flatMap
import no.nav.arbeidsgiver.iatjenester.metrikker.controller.ResponseStatus
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.InnloggetIaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.service.IaTjenesterMetrikkerService
import no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll.AltinnServiceId
import no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll.Orgnr
import no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll.TilgangskontrollService
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.clearNavCallid
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.setNavCallid
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.sjekkDataKvalitet
import no.nav.security.token.support.core.api.Protected
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Protected
@RestController
@CrossOrigin(
    origins = ["https://arbeidsgiver-q.nav.no", "https://arbeidsgiver.labs.nais.io"],
    allowCredentials = "true"
)
@RequestMapping("/innlogget")
class IaTjenesterMetrikkerInnloggetController(
    private val iaTjenesterMetrikkerService: IaTjenesterMetrikkerService,
    private val tilgangskontrollService: TilgangskontrollService
) {

    @PostMapping(value = ["/mottatt-iatjeneste"], consumes = ["application/json"], produces = ["application/json"])
    fun leggTilNyIaMottattTjenesteForInnloggetKlient(
        @RequestHeader headers: HttpHeaders,
        @RequestBody innloggetIaTjeneste: InnloggetIaTjeneste
    ): ResponseEntity<ResponseStatus> {
        setNavCallid(headers)
        log("IaTjenesterMetrikkerInnloggetController")
            .info("Mottatt IA tjeneste (innlogget) fra ${innloggetIaTjeneste.kilde.name}")

        if (!sjekkDataKvalitet(innloggetIaTjeneste)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ResponseStatus.BadRequest)
        }

        val orgnr = Orgnr(innloggetIaTjeneste.orgnr)

        val brukerSjekk = tilgangskontrollService
            .hentInnloggetBruker(AltinnServiceId.IA_SERVICE)
            .flatMap { TilgangskontrollService.sjekkTilgangTilOrgnr(orgnr, it) }

        when(brukerSjekk) {
            is Either.Left -> {
                log("IaTjenesterMetrikkerInnloggetController")
                    .warn(brukerSjekk.value.message, brukerSjekk.value)
                clearNavCallid()
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ResponseStatus.Forbidden)
            }
            else -> {}
        }

        val iaSjekk = iaTjenesterMetrikkerService.sjekkOgOpprett(innloggetIaTjeneste)
        when(iaSjekk) {
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


