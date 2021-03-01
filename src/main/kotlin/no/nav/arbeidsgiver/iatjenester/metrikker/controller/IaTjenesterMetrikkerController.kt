package no.nav.arbeidsgiver.iatjenester.metrikker.controller

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.IaTjeneste
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/metrikker")
class IATjenesterMetrikkerController() {

    @PostMapping(value = ["/"], consumes = ["application/json"], produces = ["application/json"])
    fun leggTilNyMottattTjeneste(@RequestBody metrikkerData: IaTjeneste): ResponseStatus {

        println(metrikkerData)
        return ResponseStatus.Created
    }
}

data class Response(val status: ResponseStatus)

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class ResponseStatus(val status: String) {
    Created("created"),
    Error("error")
}

