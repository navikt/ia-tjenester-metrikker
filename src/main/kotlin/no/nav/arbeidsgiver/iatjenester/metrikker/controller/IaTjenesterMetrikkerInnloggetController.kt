package no.nav.arbeidsgiver.iatjenester.metrikker.controller

import arrow.core.Either
import arrow.core.flatMap
import no.nav.arbeidsgiver.iatjenester.metrikker.config.AltinnServiceKey
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.Næringsbeskrivelser
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker.OverordnetEnhet
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker.Underenhet
import no.nav.arbeidsgiver.iatjenester.metrikker.enhetsregisteret.EnhetsregisteretException
import no.nav.arbeidsgiver.iatjenester.metrikker.enhetsregisteret.EnhetsregisteretService
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.InnloggetMottattIaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.InnloggetMottattIaTjenesteMedVirksomhetGrunndata
import no.nav.arbeidsgiver.iatjenester.metrikker.service.IaTjenesterMetrikkerService
import no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll.InnloggetBruker
import no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll.Orgnr
import no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll.TilgangskontrollException
import no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll.TilgangskontrollService
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.clearNavCallid
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.erOrgnrGyldig
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.setNavCallid
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
    private val enhetsregisteretService: EnhetsregisteretService
) {

    @PostMapping(value = ["/mottatt-iatjeneste"], consumes = ["application/json"], produces = ["application/json"])
    fun leggTilNyIaMottattTjenesteForInnloggetKlient(
        @RequestHeader headers: HttpHeaders,
        @RequestBody innloggetIaTjenesteMedVirksomhetGrunndata: InnloggetMottattIaTjenesteMedVirksomhetGrunndata
    ): ResponseEntity<ResponseStatus> {
        setNavCallid(headers)
        log("IaTjenesterMetrikkerInnloggetController")
            .info("Mottatt IA tjeneste (innlogget) fra ${innloggetIaTjenesteMedVirksomhetGrunndata.kilde.name}")

        if (!erOrgnrGyldig(innloggetIaTjenesteMedVirksomhetGrunndata)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ResponseStatus.BadRequest)
        }

        val orgnr = Orgnr(innloggetIaTjenesteMedVirksomhetGrunndata.orgnr)

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
                opprettMottattIaTjenesteMetrikk(innloggetIaTjenesteMedVirksomhetGrunndata)
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
        @RequestBody innloggetIaTjeneste: InnloggetMottattIaTjeneste
    ): ResponseEntity<ResponseStatus> {
        setNavCallid(headers)
        log("IaTjenesterMetrikkerInnloggetController")
            .info("Mottatt forenklet IA tjeneste (innlogget) fra ${innloggetIaTjeneste.kilde.name}")

        if (!erOrgnrGyldig(innloggetIaTjeneste)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ResponseStatus.BadRequest)
        }

        val orgnr = Orgnr(innloggetIaTjeneste.orgnr)
        val innloggetBruker: Either<TilgangskontrollException, InnloggetBruker> = tilgangskontrollService
            .hentInnloggetBruker(innloggetIaTjeneste.altinnRettighet)
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
                byggIaTjenesteMetrikk(innloggetIaTjeneste).fold(
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
                            InnloggetMottattIaTjenesteMedVirksomhetGrunndata(
                                orgnr = innloggetIaTjeneste.orgnr,
                                næringKode5Siffer = underenhet.næringskode.kode!!,
                                type = innloggetIaTjeneste.type,
                                kilde = innloggetIaTjeneste.kilde,
                                tjenesteMottakkelsesdato = innloggetIaTjeneste.tjenesteMottakkelsesdato,
                                antallAnsatte = underenhet.antallAnsatte,
                                næringskode5SifferBeskrivelse = underenhet.næringskode.beskrivelse,
                                næring2SifferBeskrivelse = Næringsbeskrivelser.mapTilNæringsbeskrivelse(
                                    underenhet.næringskode.kode!!
                                ),
                                SSBSektorKode = overordnetEnhet.institusjonellSektorkode.kode,
                                SSBSektorKodeBeskrivelse = overordnetEnhet.institusjonellSektorkode.beskrivelse,
                                fylke = underenhet.fylke.navn,
                                kommunenummer = underenhet.kommune.nummer,
                                kommune = underenhet.kommune.navn
                            )
                        )
                    }
                )
            }
        )
    }

    private fun opprettMottattIaTjenesteMetrikk(
        innloggetIaTjenesteMedVirksomhetGrunndata: InnloggetMottattIaTjenesteMedVirksomhetGrunndata
    ): ResponseEntity<ResponseStatus> {

        return when (val iaSjekk =
            iaTjenesterMetrikkerService.sjekkOgPersister(innloggetIaTjenesteMedVirksomhetGrunndata)) {
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


