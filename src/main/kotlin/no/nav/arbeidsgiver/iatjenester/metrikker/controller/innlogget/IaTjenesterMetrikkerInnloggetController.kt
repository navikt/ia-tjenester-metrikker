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
import no.nav.security.token.support.core.api.Protected
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.http.HttpHeaders

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
    ): ResponseStatus {
        setNavCallid(headers)
        log("IaTjenesterMetrikkerInnloggetController")
            .info("Mottatt IA tjeneste (innlogget) fra ${innloggetIaTjeneste.kilde.name}")

        val orgnr = Orgnr(innloggetIaTjeneste.orgnr)
        val bruker: InnloggetBruker = tilgangskontrollService.hentInnloggetBruker()

        TilgangskontrollService.sjekkTilgangTilOrgnr(orgnr, bruker)
        iaTjenesterMetrikkerService.sjekkOgOpprett(innloggetIaTjeneste)

        clearNavCallid()
        return ResponseStatus.Created
    }
}


