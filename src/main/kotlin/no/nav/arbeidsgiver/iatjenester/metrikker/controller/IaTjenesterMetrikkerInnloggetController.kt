package no.nav.arbeidsgiver.iatjenester.metrikker.controller

import arrow.core.Either
import arrow.core.flatMap
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker.OverordnetEnhet
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker.Underenhet
import no.nav.arbeidsgiver.iatjenester.metrikker.enhetsregisteret.EnhetsregisteretException
import no.nav.arbeidsgiver.iatjenester.metrikker.enhetsregisteret.EnhetsregisteretService
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.InnloggetMottattIaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.InnloggetMottattIaTjenesteMedVirksomhetGrunndata
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.getInnloggetMottattIaTjenesteMedVirksomhetGrunndata
import no.nav.arbeidsgiver.iatjenester.metrikker.service.IaTjenesterMetrikkerService
import no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll.InnloggetBruker
import no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll.Orgnr
import no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll.TilgangskontrollException
import no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll.TilgangskontrollService
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.erOrgnrGyldig
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log
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
    origins = [
        "https://arbeidsgiver-gcp.dev.nav.no",
        "https://arbeidsgiver.dev.nav.no",
        "https://min-ia.dev.nav.no"
    ],
    allowCredentials = "true"
)
@RequestMapping("/innlogget")
class IaTjenesterMetrikkerInnloggetController(
    private val iaTjenesterMetrikkerService: IaTjenesterMetrikkerService,
    private val tilgangskontrollService: TilgangskontrollService,
    private val enhetsregisteretService: EnhetsregisteretService
) {
    val innloggetControllerlogger = log("IaTjenesterMetrikkerInnloggetController")

    @PostMapping(
        value = ["/mottatt-iatjeneste"],
        consumes = ["application/json"],
        produces = ["application/json"]
    )
    fun leggTilNyIaMottattTjenesteForInnloggetKlient(
        @RequestHeader headers: HttpHeaders,
        @RequestBody innloggetIaTjeneste: InnloggetMottattIaTjeneste
    ): ResponseEntity<ResponseStatus> {
        innloggetControllerlogger.info("Mottatt IA tjeneste (innlogget) fra ${innloggetIaTjeneste.kilde.name}")

        if (!erOrgnrGyldig(innloggetIaTjeneste)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ResponseStatus.BadRequest)
        }
        val orgnr = Orgnr(innloggetIaTjeneste.orgnr)

        val innloggetBruker: Either<Exception, InnloggetBruker> =
            tilgangskontrollService
                .hentInnloggetBruker(innloggetIaTjeneste.altinnRettighet)
                .flatMap { TilgangskontrollService.sjekkTilgangTilOrgnr(orgnr, it) }

        return innloggetBruker.fold(
            {
                val httpStatus = when (it) {
                    is TilgangskontrollException -> {
                        HttpStatus.FORBIDDEN
                    }
                    else -> {
                        HttpStatus.INTERNAL_SERVER_ERROR
                    }
                }
                innloggetControllerlogger.warn(
                    it.message,
                    it
                )
                ResponseEntity.status(httpStatus)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ResponseStatus.Forbidden)
            }, {
                byggIaTjenesteMetrikk(innloggetIaTjeneste).fold(
                    { enhetsregisteretException ->
                        innloggetControllerlogger.warn(
                            enhetsregisteretException.message,
                            enhetsregisteretException
                        )
                        ResponseEntity.status(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(ResponseStatus.OK)
                    },
                    { innloggetIaTjeneste -> persisterMottattIaTjenesteMetrikk(innloggetIaTjeneste) }
                )
            }
        )
    }


    private fun byggIaTjenesteMetrikk(
        innloggetIaTjeneste: InnloggetMottattIaTjeneste
    ): Either<EnhetsregisteretException, InnloggetMottattIaTjenesteMedVirksomhetGrunndata> {
        log.info(
            "Mottatt IaTjenester metrikk (med virksomhet metadata) av tpye '${innloggetIaTjeneste.type}' " +
                    "fra kilde '${innloggetIaTjeneste.kilde}' " +
                    "med rettighet '${innloggetIaTjeneste.altinnRettighet.name}'"
        )

        val opplysningerForUnderenhet: Either<EnhetsregisteretException, Underenhet> =
            enhetsregisteretService.hentUnderenhet(Orgnr(innloggetIaTjeneste.orgnr))

        return opplysningerForUnderenhet.fold(
            { enhetsregisteretException ->
                log.warn(
                    "Kunne ikke hente opplysninger for underenhet i enhetsregisteret. " +
                            "Feilmelding er: '${enhetsregisteretException.message}'"
                )
                return Either.Left(enhetsregisteretException)
            }, { underenhet ->
                val opplysningerForOverordnetEnhet: Either<EnhetsregisteretException, OverordnetEnhet> =
                    enhetsregisteretService.hentOverordnetEnhet(
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
                            getInnloggetMottattIaTjenesteMedVirksomhetGrunndata(
                                innloggetIaTjeneste,
                                underenhet,
                                overordnetEnhet
                            )
                        )
                    }
                )
            }
        )
    }

    private fun persisterMottattIaTjenesteMetrikk(
        innloggetIaTjenesteMedVirksomhetGrunndata: InnloggetMottattIaTjenesteMedVirksomhetGrunndata
    ): ResponseEntity<ResponseStatus> {

        return when (val iaSjekk =
            iaTjenesterMetrikkerService.sjekkOgPersister(innloggetIaTjenesteMedVirksomhetGrunndata)) {
            is Either.Left -> {
                innloggetControllerlogger.warn(iaSjekk.value.message, iaSjekk.value)
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ResponseStatus.BadRequest)
            }
            is Either.Right -> {
                ResponseEntity.status(HttpStatus.CREATED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ResponseStatus.Created)

            }
        }
    }
}


