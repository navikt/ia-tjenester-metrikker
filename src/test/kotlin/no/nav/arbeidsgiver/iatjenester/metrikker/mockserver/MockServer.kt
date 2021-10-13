package no.nav.arbeidsgiver.iatjenester.metrikker.mockserver

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer
import com.github.tomakehurst.wiremock.matching.StringValuePattern
import no.nav.arbeidsgiver.iatjenester.metrikker.TestUtils.Companion.ORGNR_SOM_RETURNERES_AV_MOCK_ALTINN
import no.nav.arbeidsgiver.iatjenester.metrikker.TestUtils.Companion.ORGNR_UTEN_NÆRINGSKODE_I_ENHETSREGISTERET
import no.nav.arbeidsgiver.iatjenester.metrikker.TestUtils.Companion.OVERORDNETENHET_ORGNR
import no.nav.arbeidsgiver.iatjenester.metrikker.config.AltinnConfigProperties
import no.nav.arbeidsgiver.iatjenester.metrikker.enhetsregisteret.EnhetsregisteretProperties
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.net.URL

@Profile("local", "test")
@Component
class MockServer @Autowired constructor(
    private val altinnConfigProperties: AltinnConfigProperties,
    private val enhetsregisteretProperties: EnhetsregisteretProperties
) : InitializingBean {

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

        val altinnProxyPathToV2Organisasjoner = URL(altinnConfigProperties.proxyUrl).path + "v2/organisasjoner"
        mockAltinnResponseWithParameters(
            wireMockServer,
            "$altinnProxyPathToV2Organisasjoner",
            mapOf(
                "serviceCode" to WireMock.equalTo("3403"),
                "serviceEdition" to WireMock.equalTo("1")
            ),
           listOf( ORGNR_SOM_RETURNERES_AV_MOCK_ALTINN)
        )

        mockAltinnResponseWithParameters(
            wireMockServer,
            "$altinnProxyPathToV2Organisasjoner",
            mapOf(
                "serviceCode" to WireMock.equalTo("5062"),
                "serviceEdition" to WireMock.equalTo("1")
            ),
            listOf( ORGNR_SOM_RETURNERES_AV_MOCK_ALTINN, ORGNR_UTEN_NÆRINGSKODE_I_ENHETSREGISTERET)
        )

        mockEnhetsregisteretResponseForOverordnetEnhet(
            wireMockServer,
            OVERORDNETENHET_ORGNR,
            """{
              "beskrivelse": "Annen forskning og annet utviklingsarbeid innen naturvitenskap og teknikk",
              "kode": "72.190"
            }"""
        )
        mockEnhetsregisteretResponseForUnderenhet(
            wireMockServer,
            ORGNR_UTEN_NÆRINGSKODE_I_ENHETSREGISTERET,
            OVERORDNETENHET_ORGNR,
            ""
        )
        mockEnhetsregisteretResponseForUnderenhet(
            wireMockServer,
            ORGNR_SOM_RETURNERES_AV_MOCK_ALTINN,
            OVERORDNETENHET_ORGNR,
            """{"kode": "86.234", "beskrivelse":"En random kode"}"""
        )

        mockDatakatalog(
            wireMockServer,
            "/lokal_datakatalog/ikke_en_ekte_datapakke_id"
        )

        log("MockServer.init()").warn("***** Starter MockServer lokalt on port $wiremockPort -- OBS: Denne meldingen skal ikke vises i PROD *****")
        wireMockServer.start()
    }

    fun verify() {
        this.verify()
    }

    private fun mockAltinnResponseWithParameters(
        server: WireMockServer,
        basePath: String,
        parameters: Map<String, StringValuePattern>,
        orgnrListe: List<String>
    ) {
        val listeAvBedrifterBrukerenHarTilgangTil = """[
            ${orgnrListe.joinToString(separator = ",") { 
            """{
                  "Name": "BALLSTAD OG HORTEN",
                  "Type": "Enterprise",
                  "ParentOrganizationNumber": null,
                  "OrganizationNumber": "$it",
                  "OrganizationForm": "AS",
                  "Status": "Active"
                  }"""
            }
        }]""".trimIndent()

        server.stubFor(
            WireMock.get(WireMock.urlPathEqualTo(basePath))
                .withHeader("Accept", WireMock.containing("application/json"))
                .withQueryParams(parameters)
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            listeAvBedrifterBrukerenHarTilgangTil
                        )
                )
        )
    }

    private fun mockEnhetsregisteretResponseForUnderenhet(
        server: WireMockServer,
        orgnrUnderenhet: String,
        orgnrOverordnetEnhet: String,
        næringskode: String
    ) {
        val basePath = URL(enhetsregisteretProperties.url).path +
                "underenheter/$orgnrUnderenhet"

        server.stubFor(
            WireMock.get(WireMock.urlPathEqualTo(basePath))
                .withHeader("Accept", WireMock.containing("application/json"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""{
                            "organisasjonsnummer": "$orgnrUnderenhet",
                            "navn": "Bedrift uten næringskode",
                            "naeringskode1": $næringskode,
                            "antallAnsatte": 6, 
                            "overordnetEnhet": "$orgnrOverordnetEnhet", 
                            "oppstartsdato": "2013-02-25",
                            "beliggenhetsadresse": {
                                "land": "Norge",
                                "landkode": "NO",
                                "postnummer": "3036",
                                "poststed": "DRAMMEN",
                                "adresse": [
                                  "Testenekjørerveien 13"
                                ],
                                "kommune": "DRAMMEN",
                                "kommunenummer": "3005"
                              }
                          }""".trimIndent()
                        )
                )
        )
    }

    private fun mockEnhetsregisteretResponseForOverordnetEnhet(
        server: WireMockServer,
        orgnrOverordnetEnhet: String,
        næringskode: String
    ) {
        val basePath = URL(enhetsregisteretProperties.url).path + "enheter/$orgnrOverordnetEnhet"

        server.stubFor(
            WireMock.get(WireMock.urlPathEqualTo(basePath))
                .withHeader("Accept", WireMock.containing("application/json"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""{
                            "organisasjonsnummer": "$orgnrOverordnetEnhet",
                            "navn": "Overordnet enhet for test",
                            "naeringskode1": $næringskode,
                            "institusjonellSektorkode": {
                              "kode": "2100",
                              "beskrivelse": "Private aksjeselskaper mv."
                            },
                            "antallAnsatte": 6, 
                            "overordnetEnhet": "$orgnrOverordnetEnhet", 
                            "forretningsadresse": {
                              "land": "Norge",
                              "landkode": "NO",
                              "postnummer": "0576",
                              "poststed": "OSLO",
                              "adresse": [
                                "Enannenveien 99"
                              ],
                              "kommune": "OSLO",
                              "kommunenummer": "0301"
                            }
                          }""".trimIndent()
                        )
                )
        )
    }

    private fun mockDatakatalog(
        server: WireMockServer,
        basePath: String
    ) {
        server.stubFor(
            WireMock.put(WireMock.urlPathEqualTo(basePath))
                .withHeader("Accept", WireMock.containing("application/json"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """{"id":"ikke_en_ekte_datapakke_id","status":"Successfully updated datapackage IA tjenester-metrikker"}""".trimIndent()
                        )
                )
        )
    }
}
