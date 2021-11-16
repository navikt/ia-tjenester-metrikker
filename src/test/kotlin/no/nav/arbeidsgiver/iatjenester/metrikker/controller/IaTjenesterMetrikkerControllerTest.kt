package no.nav.arbeidsgiver.iatjenester.metrikker.controller

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import no.nav.arbeidsgiver.iatjenester.metrikker.TestUtils.Companion.APPLICATION_JSON
import no.nav.arbeidsgiver.iatjenester.metrikker.TestUtils.Companion.ORGNR_SOM_RETURNERES_AV_MOCK_ALTINN
import no.nav.arbeidsgiver.iatjenester.metrikker.TestUtils.Companion.ORGNR_UTEN_NÆRINGSKODE_I_ENHETSREGISTERET
import no.nav.arbeidsgiver.iatjenester.metrikker.TestUtils.Companion.vilkårligInnloggetIaTjenesteAsString
import no.nav.arbeidsgiver.iatjenester.metrikker.config.AltinnConfigProperties
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableConfigurationProperties(value = [AltinnConfigProperties::class])
@EnableMockOAuth2Server
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = ["wiremock.port=8787"])
class IaTjenesterMetrikkerControllerTest {

    @Autowired
    private val mockOAuth2Server: MockOAuth2Server? = null

    @LocalServerPort
    lateinit var port: String

    private val objectMapper = ObjectMapper()
    private var uinnloggetEndepunkt = "/ia-tjenester-metrikker/uinnlogget/mottatt-iatjeneste"
    private var innloggetEndepunkt = "/ia-tjenester-metrikker/innlogget/mottatt-iatjeneste"

    @BeforeEach
    fun setUp() {
        objectMapper.registerModule(JavaTimeModule())
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    @Throws(Exception::class)
    fun `POST til uinnlogget-iatjeneste endepunkt skal returnere 200 created ved suksess`() {
        val requestBody: String =
            vilkårligInnloggetIaTjenesteAsString(ORGNR_SOM_RETURNERES_AV_MOCK_ALTINN)

        val response = HttpClient.newBuilder().build().send(
            HttpRequest.newBuilder()
                .uri(URI.create(hostAndPort() + uinnloggetEndepunkt))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
                .build(),
            BodyHandlers.ofString()
        )

        Assertions.assertThat(response.statusCode()).isEqualTo(201)
        val body: JsonNode = objectMapper.readTree(response.body())
        val status = body.get("status")
        Assertions.assertThat(status).isNotNull
        Assertions.assertThat(status.asText()).isEqualTo("created")
    }


    @Test
    fun `Endepunkt innlogget-metrikker krever AUTH header med gyldig token`() {
        val requestBody: String = vilkårligInnloggetIaTjenesteAsString()

        val response = HttpClient.newBuilder().build().send(
            HttpRequest.newBuilder()
                .uri(URI.create(hostAndPort() + innloggetEndepunkt))
                .header(
                    HttpHeaders.AUTHORIZATION,
                    "Bearer " + "DETTE_ER_IKKE_EN_GYLDIG_TOKEN"
                )
                .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build(),
            BodyHandlers.ofString()
        )

        Assertions.assertThat(response.statusCode()).isEqualTo(401)
        Assertions.assertThat(response.body())
            .isEqualTo("{\"message\":\"You are not authorized to access this ressource\"}")
    }

    @Test
    fun `Innlogget endepunkt mottatt-ia-tjeneste returnerer 201 OK dersom token er gyldig`() {
        val requestBody: String =
            vilkårligInnloggetIaTjenesteAsString(ORGNR_SOM_RETURNERES_AV_MOCK_ALTINN)

        val gyldigToken = issueGyldigSelvbetjeningToken()
        val response = HttpClient.newBuilder().build().send(
            HttpRequest.newBuilder()
                .uri(URI.create(hostAndPort() + innloggetEndepunkt))
                .header(HttpHeaders.AUTHORIZATION, "Bearer $gyldigToken")
                .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build(),
            BodyHandlers.ofString()
        )

        Assertions.assertThat(response.statusCode()).isEqualTo(201)
        val body: JsonNode = objectMapper.readTree(response.body())
        Assertions.assertThat(body.get("status").asText()).isEqualTo("created")
    }

    @Test
    fun `Innlogget endepunkt mottatt-ia-tjeneste returnerer 200 OK dersom noe går galt ved kall til enhetsregisteret`() {
        val requestBody: String =
            vilkårligInnloggetIaTjenesteAsString(ORGNR_UTEN_NÆRINGSKODE_I_ENHETSREGISTERET)

        val gyldigToken = issueGyldigSelvbetjeningToken()
        val response = HttpClient.newBuilder().build().send(
            HttpRequest.newBuilder()
                .uri(URI.create(hostAndPort() + innloggetEndepunkt))
                .header(HttpHeaders.AUTHORIZATION, "Bearer $gyldigToken")
                .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build(),
            BodyHandlers.ofString()
        )

        Assertions.assertThat(response.statusCode()).isEqualTo(200)
        val body: JsonNode = objectMapper.readTree(response.body())
        Assertions.assertThat(body.get("status").asText()).isEqualTo("ok")
    }

    @Test
    fun `Innlogget endepunkt mottatt-ia-tjeneste returnerer 400 bad request ved ugyldig data`() {
        val requestBodyMedUgyldigOrgnr: String = vilkårligInnloggetIaTjenesteAsString("83838")

        val gyldigToken = issueGyldigSelvbetjeningToken()
        val response = HttpClient.newBuilder().build().send(
            HttpRequest.newBuilder()
                .uri(URI.create(hostAndPort() + innloggetEndepunkt))
                .header(HttpHeaders.AUTHORIZATION, "Bearer $gyldigToken")
                .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyMedUgyldigOrgnr))
                .build(),
            BodyHandlers.ofString()
        )

        Assertions.assertThat(response.statusCode()).isEqualTo(400)
        val body: JsonNode = objectMapper.readTree(response.body())
        Assertions.assertThat(body.get("status").asText()).isEqualTo("bad request")
    }


    private fun hostAndPort() = "http://localhost:$port"


    private fun issueGyldigSelvbetjeningToken() =
        mockOAuth2Server!!.issueToken(
            "selvbetjening",
            "theclientid",
            DefaultOAuth2TokenCallback(
                "selvbetjening",
                "01079812345",
                listOf("aud-localhost"),
                emptyMap(),
                3600
            )
        ).serialize()
}
