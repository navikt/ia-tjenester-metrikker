package no.nav.arbeidsgiver.iatjenester.metrikker.controller

import arrow.core.Either
import arrow.core.flatMap
import no.nav.arbeidsgiver.iatjenester.metrikker.config.AltinnServiceKey
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker.Næringsbeskrivelser
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker.OverordnetEnhet
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker.Underenhet
import no.nav.arbeidsgiver.iatjenester.metrikker.enhetsregisteret.EnhetsregisteretException
import no.nav.arbeidsgiver.iatjenester.metrikker.enhetsregisteret.EnhetsregisteretOpplysningerService
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.InnloggetIaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.InnloggetIaTjenesteKunOrgnr
import no.nav.arbeidsgiver.iatjenester.metrikker.service.IaTjenesterMetrikkerService
import no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll.InnloggetBruker
import no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll.Orgnr
import no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll.TilgangskontrollException
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
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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

        return when (brukerSjekk) {
            is Either.Left -> {
                log("IaTjenesterMetrikkerInnloggetController")
                    .warn(brukerSjekk.value.message, brukerSjekk.value)
                clearNavCallid()
                ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ResponseStatus.Forbidden)
            }
            is Either.Right -> {
                opprettMottattIaTjenesteMetrikk(innloggetIaTjeneste)
            }
        }
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
        val innloggetBruker: Either<TilgangskontrollException, InnloggetBruker> = tilgangskontrollService
            .hentInnloggetBruker(innloggetIaTjenesteKunOrgnr.altinnRettighet)
            .flatMap { TilgangskontrollService.sjekkTilgangTilOrgnr(orgnr, it) }

        return innloggetBruker.fold(
            { tilgangskontrollException ->
                log("IaTjenesterMetrikkerInnloggetController").warn(
                    tilgangskontrollException.message,
                    tilgangskontrollException
                )
                clearNavCallid()
                ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ResponseStatus.Forbidden)
            }, {
                byggIaTjenesteMetrikk(innloggetIaTjenesteKunOrgnr).fold(
                    { enhetsregisteretException ->
                        log("IaTjenesterMetrikkerInnloggetController").warn(
                            enhetsregisteretException.message,
                            enhetsregisteretException
                        )
                        clearNavCallid()
                        ResponseEntity.status(HttpStatus.ACCEPTED)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(ResponseStatus.Accepted)
                    }, { innloggetIaTjeneste -> opprettMottattIaTjenesteMetrikk(innloggetIaTjeneste) }
                )
            }
        )
    }


    private fun byggIaTjenesteMetrikk(
        innloggetIaTjenesteKunOrgnr: InnloggetIaTjenesteKunOrgnr
    ): Either<EnhetsregisteretException, InnloggetIaTjeneste> {
        log.info(
            "Mottatt IaTjenester metrikk (forenklet) av tpye '${innloggetIaTjenesteKunOrgnr.type}' " +
                    "fra kilde '${innloggetIaTjenesteKunOrgnr.kilde}' " +
                    "med rettighet '${innloggetIaTjenesteKunOrgnr.altinnRettighet.name}'"
        )

        val opplysningerForUnderenhet: Either<EnhetsregisteretException, Underenhet> =
            enhetsregisteretOpplysningerService.hentOpplysningerForUnderenhet(Orgnr(innloggetIaTjenesteKunOrgnr.orgnr))

        return opplysningerForUnderenhet.fold(
            { enhetsregisteretException ->
                log.warn(
                    "Kunne ikke hente opplysninger for underenhet i enhetsregisteret. " +
                            "Feilmelding er: '${enhetsregisteretException.message}'"
                )
                return Either.Left(enhetsregisteretException)
            }, { underenhet ->
                val opplysningerForOverordnetEnhet: Either<EnhetsregisteretException, OverordnetEnhet> =
                    enhetsregisteretOpplysningerService.hentOpplysningerForOverordnetEnhet(
                        underenhet.overordnetEnhetOrgnr
                    )

                opplysningerForOverordnetEnhet.fold(
                    { enhetsregisteretException ->
                        log.warn(
                            "Kunne ikke hente opplysninger for overordnetEnhet i enhetsregisteret. " +
                                    "Feilmelding er: '${enhetsregisteretException.message}'"
                        )
                        Either.Left(enhetsregisteretException)
                    }, { overordnetEnhet ->
                        Either.Right(
                            InnloggetIaTjeneste(
                                orgnr = innloggetIaTjenesteKunOrgnr.orgnr,
                                næringKode5Siffer = underenhet.næringskode.kode!!,
                                type = innloggetIaTjenesteKunOrgnr.type,
                                kilde = innloggetIaTjenesteKunOrgnr.kilde,
                                tjenesteMottakkelsesdato = innloggetIaTjenesteKunOrgnr.tjenesteMottakkelsesdato,
                                antallAnsatte = underenhet.antallAnsatte,
                                næringskode5SifferBeskrivelse = underenhet.næringskode.beskrivelse,
                                næring2SifferBeskrivelse = Næringsbeskrivelser.mapTilNæringsbeskrivelse(
                                    underenhet.næringskode.kode!!
                                ),
                                SSBSektorKode = overordnetEnhet.institusjonellSektorkode.kode,
                                SSBSektorKodeBeskrivelse = overordnetEnhet.institusjonellSektorkode.beskrivelse,
                                fylkesnummer = "TODO",
                                fylke = "TODO",
                                kommunenummer = "TODO",
                                kommune = "TODO"
                            )
                        )
                    }
                )
            }
        )
    }

    private fun opprettMottattIaTjenesteMetrikk(innloggetIaTjeneste: InnloggetIaTjeneste): ResponseEntity<ResponseStatus> {

        return when (val iaSjekk = iaTjenesterMetrikkerService.sjekkOgOpprett(innloggetIaTjeneste)) {
            is Either.Left -> {
                log("IaTjenesterMetrikkerInnloggetController")
                    .warn(iaSjekk.value.message, iaSjekk.value)
                clearNavCallid()
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ResponseStatus.BadRequest)
            }
            is Either.Right -> {
                clearNavCallid()
                ResponseEntity.status(HttpStatus.CREATED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ResponseStatus.Created)

            }
        }
    }
}


