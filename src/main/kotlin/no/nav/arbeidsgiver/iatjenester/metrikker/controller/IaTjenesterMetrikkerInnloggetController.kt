package no.nav.arbeidsgiver.iatjenester.metrikker.controller

import arrow.core.Either
import arrow.core.flatMap
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.OverordnetEnhet
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.Underenhet
import no.nav.arbeidsgiver.iatjenester.metrikker.enhetsregisteret.EnhetsregisteretException
import no.nav.arbeidsgiver.iatjenester.metrikker.enhetsregisteret.EnhetsregisteretService
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.InnloggetMottattIaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.MottattIaTjenesteMedVirksomhetGrunndata
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
        "https://forebygge-fravar.intern.dev.nav.no",
    ],
    allowCredentials = "true",
)
@RequestMapping("/innlogget")
class IaTjenesterMetrikkerInnloggetController(
    private val iaTjenesterMetrikkerService: IaTjenesterMetrikkerService,
    private val tilgangskontrollService: TilgangskontrollService,
    private val enhetsregisteretService: EnhetsregisteretService,
) {
    @PostMapping(
        value = ["/mottatt-iatjeneste"],
        consumes = ["application/json"],
        produces = ["application/json"],
    )
    fun leggTilNyIaMottattTjeneste(
        @RequestHeader headers: HttpHeaders,
        @RequestBody iaTjeneste: InnloggetMottattIaTjeneste,
    ): ResponseEntity<ResponseStatus> {
        log.info("Mottatt IA tjeneste (innlogget) fra ${iaTjeneste.kilde.name}")

        if (!erOrgnrGyldig(iaTjeneste)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ResponseStatusConsts.BadRequest)
        }
        val orgnr = Orgnr(iaTjeneste.orgnr)

        val innloggetBruker: Either<Exception, InnloggetBruker> =
            tilgangskontrollService
                .hentInnloggetBruker()
                .flatMap { TilgangskontrollService.sjekkTilgangTilOrgnr(orgnr, it) }

        return innloggetBruker.fold(
            {
                val httpStatus = when (it) {
                    is TilgangskontrollException -> {
                        HttpStatus.FORBIDDEN
                    }
                    else -> {
                        log.info("Feil ved validering av rettigheter til bruker. Årsaken er: '${it.message}', exception er: '$it' ")
                        log.debug("StackTrace: \n ${it.stackTraceToString()}")
                        HttpStatus.INTERNAL_SERVER_ERROR
                    }
                }
                log.warn(it.message, it)
                ResponseEntity.status(httpStatus)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ResponseStatusConsts.Forbidden)
            },
            {
                byggIaTjenesteMetrikk(iaTjeneste).fold(
                    { enhetsregisteretException ->
                        log.warn(
                            enhetsregisteretException.message,
                            enhetsregisteretException,
                        )
                        ResponseEntity.status(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(ResponseStatusConsts.OK)
                    },
                    { innloggetIaTjeneste -> persisterMottattIaTjenesteMetrikk(innloggetIaTjeneste) },
                )
            },
        )
    }

    private fun byggIaTjenesteMetrikk(
        innloggetIaTjeneste: InnloggetMottattIaTjeneste,
    ): Either<EnhetsregisteretException, MottattIaTjenesteMedVirksomhetGrunndata> {
        log.info(
            "Mottatt IaTjenester metrikk (med virksomhet metadata) av tpye '${innloggetIaTjeneste.type}' " +
                "fra kilde '${innloggetIaTjeneste.kilde}' ",
        )

        val opplysningerForUnderenhet: Either<EnhetsregisteretException, Underenhet> =
            enhetsregisteretService.hentUnderenhet(Orgnr(innloggetIaTjeneste.orgnr))

        return opplysningerForUnderenhet.fold(
            { enhetsregisteretException ->
                log.warn(
                    "Kunne ikke hente opplysninger for underenhet i enhetsregisteret. " +
                        "Feilmelding er: '${enhetsregisteretException.message}'",
                )
                return Either.Left(enhetsregisteretException)
            },
            { underenhet ->
                val opplysningerForOverordnetEnhet: Either<EnhetsregisteretException, OverordnetEnhet> =
                    enhetsregisteretService.hentOverordnetEnhet(
                        underenhet.overordnetEnhetOrgnr,
                    )

                opplysningerForOverordnetEnhet.fold(
                    { enhetsregisteretException ->
                        log.warn(
                            "Kunne ikke hente opplysninger for overordnetEnhet i enhetsregisteret. " +
                                "Feilmelding er: '${enhetsregisteretException.message}'",
                        )
                        Either.Left(enhetsregisteretException)
                    },
                    { overordnetEnhet ->
                        Either.Right(
                            getInnloggetMottattIaTjenesteMedVirksomhetGrunndata(
                                innloggetIaTjeneste,
                                underenhet,
                                overordnetEnhet,
                            ),
                        )
                    },
                )
            },
        )
    }

    private fun persisterMottattIaTjenesteMetrikk(
        innloggetIaTjenesteMedVirksomhetGrunndata: MottattIaTjenesteMedVirksomhetGrunndata,
    ): ResponseEntity<ResponseStatus> =
        when (
            val iaSjekk =
                iaTjenesterMetrikkerService.sjekkOgPersister(innloggetIaTjenesteMedVirksomhetGrunndata)
        ) {
            is Either.Left -> {
                log.warn(iaSjekk.value.message, iaSjekk.value)
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ResponseStatusConsts.BadRequest)
            }
            is Either.Right -> {
                ResponseEntity.status(HttpStatus.CREATED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ResponseStatusConsts.Created)
            }
        }
}
