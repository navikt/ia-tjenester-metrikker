package no.nav.arbeidsgiver.iatjenester.metrikker.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.filter.CommonsRequestLoggingFilter

@Configuration
class CommonsRequestLoggingFilterConfig {
    /**
     * Logs incoming requests to DEBUG.
     */
    @Bean
    fun requestLoggingFilter(): CommonsRequestLoggingFilter {
        return CommonsRequestLoggingFilter().apply {
            setIncludeClientInfo(false)
            setIncludeHeaders(false)
            setIncludeQueryString(false)
            setIncludePayload(false)
        }
    }
}