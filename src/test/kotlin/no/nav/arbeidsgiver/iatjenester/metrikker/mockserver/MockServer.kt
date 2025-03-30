package no.nav.arbeidsgiver.iatjenester.metrikker.mockserver

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("local")
@Component
class MockServer : InitializingBean {
    @Value("\${wiremock.port}")
    var wiremockPort: Int = 8484
    private val mockServerVerboseConsoleLoggingEnabled = false
    lateinit var wireMockServer: WireMockServer

    override fun afterPropertiesSet() {
        wireMockServer = WireMockServer(
            WireMockConfiguration()
                .port(wiremockPort)
                .extensions(
                    ResponseTemplateTransformer(true),
                )
                .notifier(
                    ConsoleNotifier(mockServerVerboseConsoleLoggingEnabled),
                ),
        )
        log.info("[Lokalt][Test] Starting WireMock server on port $wiremockPort")
        wireMockServer.start()
    }
}
