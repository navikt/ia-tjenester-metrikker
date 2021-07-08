package no.nav.arbeidsgiver.iatjenester.metrikker.controller.uinnlogget

import no.nav.arbeidsgiver.iatjenester.metrikker.controller.ResponseStatus
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.DatakatalogStatistikk
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.UinnloggetIaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.clearNavCallid
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.setNavCallid
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@Unprotected
@RestController
@Profile("local", "dev-gcp")
@RequestMapping("/utsending")
class UtsendingTilDatakatalogController(val datakatalogStatistikk: DatakatalogStatistikk) {

    @PostMapping(value = ["/datapakke"], produces = ["application/json"])
    fun leggTilNyMottattIaTjeneste(
        @RequestHeader headers: HttpHeaders
    ): ResponseEntity<ResponseStatus> {
        setNavCallid(headers)
        log("UtsendingTilDatakatalogController")
            .info("Sender datapakke til datakatalog")

        datakatalogStatistikk.byggOgSendDatapakke(erDebugAktivert = true)

        clearNavCallid()
        return ResponseEntity.status(HttpStatus.CREATED)
            .contentType(MediaType.APPLICATION_JSON)
            .body(ResponseStatus.Created)
    }
}
