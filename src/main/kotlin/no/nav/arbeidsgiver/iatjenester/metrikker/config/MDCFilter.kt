package no.nav.arbeidsgiver.iatjenester.metrikker.config

import arrow.core.Option
import arrow.core.Some
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*

private const val DEFAULT_CALLID_HEADER_NAME = "Nav-CallId"
private const val ALTERNATIVE_CALLID_HEADER_NAME = "Nav-Call-Id"

private const val CORRELATION_ID_MDC_NAME = "correlationId"
private const val CORRELATION_ID_HEADER_NAME = "X-Correlation-ID"

@Component
class MDCFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val correlationId = extractCorrelationId(request)
            val callId = extractCallId(request)

            addToMDC(callId, correlationId)

            response.addHeader(CORRELATION_ID_HEADER_NAME, correlationId)
            filterChain.doFilter(request, response)
        } finally {
            clearMDC()
        }
    }

    private fun addToMDC(callId: String, correlationId: String) {
        MDC.put(DEFAULT_CALLID_HEADER_NAME, callId)
        MDC.put(ALTERNATIVE_CALLID_HEADER_NAME, callId)
        MDC.put(CORRELATION_ID_MDC_NAME, correlationId)
    }

    private fun clearMDC() {
        MDC.remove(DEFAULT_CALLID_HEADER_NAME)
        MDC.remove(ALTERNATIVE_CALLID_HEADER_NAME)
        MDC.remove(CORRELATION_ID_MDC_NAME)
    }

    private fun extractCorrelationId(request: HttpServletRequest): String {
        return Option.fromNullable(request.getHeader(CORRELATION_ID_HEADER_NAME))
            .filter { it.isNotBlank() }
            .fold({ UUID.randomUUID().toString() }, { it })
    }

    private fun extractCallId(request: HttpServletRequest): String {
        val defaultCallid: Option<String> =
            Option.fromNullable(request.getHeader(DEFAULT_CALLID_HEADER_NAME))
                .filter { it.isNotBlank() }
        val altCallid: Option<String> =
            Option.fromNullable(request.getHeader(ALTERNATIVE_CALLID_HEADER_NAME))
                .filter { it.isNotBlank() }
        val uuid = UUID.randomUUID().toString()

        return when {
            defaultCallid is Some -> defaultCallid.value
            altCallid is Some -> altCallid.value
            else -> uuid
        }
    }
}