package no.nav.arbeidsgiver.iatjenester.metrikker

import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.error.exceptions.AltinnrettigheterProxyKlientException
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.error.exceptions.AltinnrettigheterProxyKlientFallbackException
import no.nav.arbeidsgiver.iatjenester.metrikker.service.IaTjenesterMetrikkerValideringException
import no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll.TilgangskontrollException
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log
import no.nav.security.token.support.core.exceptions.JwtTokenMissingException
import no.nav.security.token.support.core.exceptions.JwtTokenValidatorException
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.WebRequest
import org.springframework.web.server.ResponseStatusException
import java.nio.file.AccessDeniedException


@ControllerAdvice(annotations = [RestController::class])
class RestResponseEntityExceptionHandler {

    @ExceptionHandler(value = [IaTjenesterMetrikkerValideringException::class])
    @ResponseBody
    protected fun handleBadRequestException(e: IaTjenesterMetrikkerValideringException, webRequest: WebRequest?): ResponseEntity<Any> {
        return getResponseEntity(
            "Innhold til request er ikke gyldig med årsak: '${e.årsak}'",
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(value = [JwtTokenValidatorException::class, JwtTokenMissingException::class, JwtTokenUnauthorizedException::class, AccessDeniedException::class])
    @ResponseBody
    protected fun handleUnauthorizedException(e: RuntimeException, webRequest: WebRequest?): ResponseEntity<Any> {
        log("RestResponseEntityExceptionHandler").info(
            "Validering av token feilet: '${e.cause?.message}'",
            e
        )
        return getResponseEntity(e, "You are not authorized to access this resource", HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(value = [AltinnrettigheterProxyKlientFallbackException::class, AltinnrettigheterProxyKlientException::class])
    @ResponseBody
    protected fun handleAltinnException(e: RuntimeException, webRequest: WebRequest?): ResponseEntity<Any> {
        log("RestResponseEntityExceptionHandler").error("Kunne ikke verifisere at innlogget bruker har tilgang til orgnr i Altinn", e)
        return getResponseEntity(e, "Forbidden", HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(value = [TilgangskontrollException::class])
    @ResponseBody
    protected fun handleTilgangskontrollException(e: RuntimeException, webRequest: WebRequest?): ResponseEntity<Any> {
        log("RestResponseEntityExceptionHandler").error("Bruker har tilgang til orgnr i Altinn", e)
        return getResponseEntity(e, "Forbidden", HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(value = [ResponseStatusException::class])
    @ResponseBody
    protected fun handleResponseStatusException(e: ResponseStatusException, webRequest: WebRequest?): ResponseEntity<Any> {
        log("RestResponseEntityExceptionHandler").warn(e.message, e)
        return getResponseEntity(e, e.message, e.status)
    }

    @ExceptionHandler(value = [Exception::class])
    @ResponseBody
    protected fun handleGenerellException(e: RuntimeException, webRequest: WebRequest?): ResponseEntity<Any> {
        log("RestResponseEntityExceptionHandler").error("Uhåndtert feil", e)
        return getResponseEntity(e, "Internal error", HttpStatus.INTERNAL_SERVER_ERROR)
    }

    private fun getResponseEntity(e: RuntimeException, melding: String, status: HttpStatus): ResponseEntity<Any> {
        val body = HashMap<String, String>(1)
        body["message"] = melding
        val opprineligMeldingEllerNavnTilException = e.message ?: e.toString()
        log("RestResponseEntityExceptionHandler").info(String.format(
            "Returnerer følgende HttpStatus '%s' med melding '%s' pga exception '%s'",
            status.toString(),
            melding,
            opprineligMeldingEllerNavnTilException
        ))
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body)
    }

    private fun getResponseEntity(melding: String, status: HttpStatus): ResponseEntity<Any> {
        val body = HashMap<String, String>(1)
        body["message"] = melding
        log("RestResponseEntityExceptionHandler").info(String.format(
            "Returnerer følgende HttpStatus '%s' med melding '%s'",
            status.toString(),
            melding
        ))
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body)
    }
}
