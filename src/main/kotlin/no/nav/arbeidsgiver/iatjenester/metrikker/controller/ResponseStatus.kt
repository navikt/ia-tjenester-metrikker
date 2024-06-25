package no.nav.arbeidsgiver.iatjenester.metrikker.controller


object ResponseStatusConsts {
    val OK = ResponseStatus("ok")
    val Created = ResponseStatus("created")
    val BadRequest = ResponseStatus("bad request")
    val Forbidden = ResponseStatus("forbidden")
}

data class ResponseStatus(
    val status: String
)