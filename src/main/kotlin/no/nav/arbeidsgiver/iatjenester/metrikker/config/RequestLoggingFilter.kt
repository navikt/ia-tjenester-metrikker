package no.nav.arbeidsgiver.iatjenester.metrikker.config

import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class RequestLoggingFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val reader = request.reader
        val requestBody = reader.lines().toList().joinToString("")
        log.info(requestBody)
    }
}