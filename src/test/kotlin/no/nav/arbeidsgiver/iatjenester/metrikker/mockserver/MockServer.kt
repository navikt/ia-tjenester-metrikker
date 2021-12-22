package no.nav.arbeidsgiver.iatjenester.metrikker.mockserver

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("local")
@Component
class MockServer: InitializingBean {

    @Value("\${wiremock.port}")
    var wiremockPort: Int = 8484
    private val MOCK_SERVER_VERBOSE_CONSOLE_LOGGING_ENABLED = false;
    lateinit var wireMockServer: WireMockServer

    override fun afterPropertiesSet() {
        wireMockServer = WireMockServer(
            WireMockConfiguration()
                .port(wiremockPort)
                .extensions(
                    ResponseTemplateTransformer(true)
                )
                .notifier(
                    ConsoleNotifier(MOCK_SERVER_VERBOSE_CONSOLE_LOGGING_ENABLED)
                )
        )
        wireMockServer.start()
    }
}
