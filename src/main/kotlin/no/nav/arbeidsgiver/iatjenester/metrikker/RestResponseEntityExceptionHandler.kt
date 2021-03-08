package no.nav.arbeidsgiver.iatjenester.metrikker

import no.nav.arbeidsgiver.iatjenester.metrikker.service.IaTjenesterMetrikkerValideringException
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
import java.util.*


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
        return getResponseEntity(e, "You are not authorized to access this ressource", HttpStatus.UNAUTHORIZED)
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
        log("RestResponseEntityExceptionHandler").info(String.format(
            "Returnerer følgende HttpStatus '%s' med melding '%s' pga exception '%s'",
            status.toString(),
            melding,
            e.message
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
