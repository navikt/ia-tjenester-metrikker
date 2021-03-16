package no.nav.arbeidsgiver.iatjenester.metrikker.controller

import com.fasterxml.jackson.annotation.JsonFormat

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class ResponseStatus(val status: String) {
    Created("created"),
    Error("error"),
    Ok("ok")
}
