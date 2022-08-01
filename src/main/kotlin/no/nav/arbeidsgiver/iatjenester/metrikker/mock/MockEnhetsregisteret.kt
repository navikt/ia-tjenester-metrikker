package no.nav.arbeidsgiver.iatjenester.metrikker.mock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.any
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer
import com.github.tomakehurst.wiremock.matching.UrlPathPattern
import io.micrometer.core.instrument.util.IOUtils
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.net.URL
import java.nio.charset.StandardCharsets

@Profile("local")
@Component
class MockEnhetsregisteret: InitializingBean {

    var wiremockPort: Int = 9191
    @Value("\${enhetsregisteret.url}")
    var enhetsregisteretUrl: String = "http://localhost:9191/enhetsregisteret/"

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
        mockKallFraEnhetsregisteret(enhetsregisteretUrl)

        log.info("[MOCK lokalt] Starter mock-server for enhetsregisteret på port $wiremockPort")
        wireMockServer.start()
    }

    private fun mockKallFraEnhetsregisteret(enhetsregisteretUrl: String) {
        val path = URL(enhetsregisteretUrl).path
        mockKall(WireMock.urlPathMatching(path + "underenheter/910562452"),
            lesFilSomString("dev_enhetsregisteretUnderenhet_910562452.json"))
        mockKall(WireMock.urlPathMatching(path + "enheter/910562223"),
            lesFilSomString("dev_enhetsregisteretEnhet.json"))
    }

    private fun mockKall(urlPathPattern: UrlPathPattern, body: String) {
        wireMockServer.stubFor(
            WireMock.get(urlPathPattern)
                .willReturn(WireMock.aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(HttpStatus.OK.value())
                .withBody(body)
            )
        )
    }

    private fun lesFilSomString(filnavn: String): String {
        return IOUtils.toString(this.javaClass.classLoader.getResourceAsStream("mock/$filnavn"), StandardCharsets.UTF_8)
    }
}
