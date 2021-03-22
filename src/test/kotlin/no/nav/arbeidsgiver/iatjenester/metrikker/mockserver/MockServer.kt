package no.nav.arbeidsgiver.iatjenester.metrikker.mockserver

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer
import com.github.tomakehurst.wiremock.matching.StringValuePattern
import no.nav.arbeidsgiver.iatjenester.metrikker.config.AltinnConfigProperties
import no.nav.arbeidsgiver.iatjenester.metrikker.config.WiremockConfigProperties
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.net.URL

@Profile("local", "test")
@Component
class MockServer @Autowired constructor(
    wiremockConfigProperties: WiremockConfigProperties,
    altinnConfigProperties: AltinnConfigProperties
) {

    private val MOCK_SERVER_VERBOSE_CONSOLE_LOGGING_ENABLED = false;

    init {
        val server = WireMockServer(
                WireMockConfiguration()
                        .port(wiremockConfigProperties.port)
                        .extensions(
                                ResponseTemplateTransformer(true)
                        )
                        .notifier(
                                ConsoleNotifier(MOCK_SERVER_VERBOSE_CONSOLE_LOGGING_ENABLED)
                        )
        )
        val altinnProxyPathToV2Organisasjoner = URL(altinnConfigProperties.proxyUrl).path + "v2/organisasjoner"
        mockWithParameters(
                server,
                "$altinnProxyPathToV2Organisasjoner",
                mapOf(
                        "serviceCode" to WireMock.equalTo("3403"),
                        "serviceEdition" to WireMock.equalTo("1")
                )
        )

        log("MockServer.init()").info("***** Starter MockServer -- LOKALT -- skal ikke komme ut i PROD *****")
        server.start()
    }


    private fun mockWithParameters(
            server: WireMockServer,
            basePath: String,
            parameters: Map<String, StringValuePattern>
    ) {
        server.stubFor(
                WireMock.get(WireMock.urlPathEqualTo(basePath))
                .withHeader("Accept", WireMock.containing("application/json"))
                .withQueryParams(parameters)
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                                """
                                    [
                                      {
                                        "Name": "BALLSTAD OG HORTEN",
                                        "Type": "Enterprise",
                                        "ParentOrganizationNumber": null,
                                        "OrganizationNumber": "811076112",
                                        "OrganizationForm": "AS",
                                        "Status": "Active"
                                      }
                                    ]
                                """.trimIndent()
                        )
                )
        )
    }
}
