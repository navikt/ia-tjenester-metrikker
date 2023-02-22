package no.nav.arbeidsgiver.iatjenester.metrikker.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.filter.CommonsRequestLoggingFilter

@Configuration
class CommonsRequestLoggingFilterConfig {
    @Bean
    fun requestLoggingFilter(): CommonsRequestLoggingFilter {
        return CommonsRequestLoggingFilter().apply {
            setIncludeClientInfo(true)
            setIncludeHeaders(true)
            setIncludeQueryString(true)
            setIncludePayload(true)
            setMaxPayloadLength(500)
        }
    }
}