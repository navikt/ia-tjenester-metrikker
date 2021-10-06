package no.nav.arbeidsgiver.iatjenester.metrikker.controller

import arrow.core.Either
import arrow.core.flatMap
import no.nav.arbeidsgiver.iatjenester.metrikker.config.AltinnServiceKey
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker.Underenhet
import no.nav.arbeidsgiver.iatjenester.metrikker.enhetsregisteret.EnhetsregisteretException
import no.nav.arbeidsgiver.iatjenester.metrikker.enhetsregisteret.EnhetsregisteretOpplysningerService
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.InnloggetIaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.InnloggetIaTjenesteKunOrgnr
import no.nav.arbeidsgiver.iatjenester.metrikker.service.IaTjenesterMetrikkerService
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
    private val tilgangskontrollService: TilgangskontrollService,
    private val enhetsregisteretOpplysningerService: EnhetsregisteretOpplysningerService
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
            .hentInnloggetBruker(AltinnServiceKey.IA)
            .flatMap { TilgangskontrollService.sjekkTilgangTilOrgnr(orgnr, it) }

        when (brukerSjekk) {
            is Either.Left -> {
                log("IaTjenesterMetrikkerInnloggetController")
                    .warn(brukerSjekk.value.message, brukerSjekk.value)
                clearNavCallid()
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ResponseStatus.Forbidden)
            }
            else -> {
            }
        }

        val iaSjekk = iaTjenesterMetrikkerService.sjekkOgOpprett(innloggetIaTjeneste)
        when (iaSjekk) {
            is Either.Left -> {
                log("IaTjenesterMetrikkerInnloggetController")
                    .warn(iaSjekk.value.message, iaSjekk.value)
                clearNavCallid()
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ResponseStatus.BadRequest)
            }
            else -> {
            }
        }

        clearNavCallid()
        return ResponseEntity.status(HttpStatus.CREATED)
            .contentType(MediaType.APPLICATION_JSON)
            .body(ResponseStatus.Created)
    }

    @PostMapping(
        value = ["/forenklet/mottatt-iatjeneste"],
        consumes = ["application/json"],
        produces = ["application/json"]
    )
    fun leggTilNyIaMottattTjenesteForInnloggetKlient(
        @RequestHeader headers: HttpHeaders,
        @RequestBody innloggetIaTjenesteKunOrgnr: InnloggetIaTjenesteKunOrgnr
    ): ResponseEntity<ResponseStatus> {
        setNavCallid(headers)
        log("IaTjenesterMetrikkerInnloggetController")
            .info("Mottatt forenklet IA tjeneste (innlogget) fra ${innloggetIaTjenesteKunOrgnr.kilde.name}")

        if (!sjekkDataKvalitet(innloggetIaTjenesteKunOrgnr)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ResponseStatus.BadRequest)
        }

        val orgnr = Orgnr(innloggetIaTjenesteKunOrgnr.orgnr)
        val brukerSjekk = tilgangskontrollService
            .hentInnloggetBruker(innloggetIaTjenesteKunOrgnr.altinnRettighet)
            .flatMap { TilgangskontrollService.sjekkTilgangTilOrgnr(orgnr, it) }

        when (brukerSjekk) {
            is Either.Left -> {
                log("IaTjenesterMetrikkerInnloggetController")
                    .warn(brukerSjekk.value.message, brukerSjekk.value)
                clearNavCallid()
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ResponseStatus.Forbidden)
            }
            else -> {
            }
        }
        log("IaTjenesteMetrikkerInnloggetBruker, mottok hendelse fra forenklet innlogget iatjeneste")
            .info(innloggetIaTjenesteKunOrgnr.altinnRettighet.name)

        val opplysningerForUnderenhet: Either<EnhetsregisteretException, Underenhet> =
            enhetsregisteretOpplysningerService.hentOpplysningerForUnderenhet(orgnr)

        if (opplysningerForUnderenhet.isLeft()) {
            val errorMelding = opplysningerForUnderenhet.fold(
                {itLeft -> itLeft.message}, { "No Error" })
            log.warn("Kunne ikke hente opplysninger for underenhet i enhetsregisteret. Feilmelding er: '$errorMelding'")
            clearNavCallid()
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ResponseStatus.Accepted)
        }
        // opprett i DB

        clearNavCallid()
        return ResponseEntity.status(HttpStatus.CREATED)
            .contentType(MediaType.APPLICATION_JSON)
            .body(ResponseStatus.Created)
    }

}


