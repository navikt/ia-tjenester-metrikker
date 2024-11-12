package no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.FORBIDDEN)
class TilgangskontrollException(
    msg: String?,
) : RuntimeException(msg)
