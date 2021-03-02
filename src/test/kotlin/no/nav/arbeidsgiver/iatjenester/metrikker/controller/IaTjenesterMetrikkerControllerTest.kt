package no.nav.arbeidsgiver.iatjenester.metrikker.controller

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import no.nav.arbeidsgiver.iatjenester.metrikker.TestUtils
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IaTjenesterMetrikkerControllerTest {

    @LocalServerPort
    lateinit var port: String

    private val objectMapper = ObjectMapper()

    @BeforeEach
    fun setUp() {
        objectMapper.registerModule(JavaTimeModule())
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    @Throws(Exception::class)
    fun `POST til uinnlogget-iatjeneste endepunkt`() {
        val requestBody: String = objectMapper
            .writeValueAsString(
                TestUtils.vilkårligIaTjeneste()
            )

        val response = HttpClient.newBuilder().build().send(
            HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:$port/uinnlogget/iatjeneste"))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .build(),
            BodyHandlers.ofString()
        )

        Assertions.assertThat(response.statusCode()).isEqualTo(200)
        val body: JsonNode = objectMapper.readTree(response.body())
        Assertions.assertThat(body.get("status").asText()).isEqualTo("created")
    }

    @Test
    fun `Endepunkt innlogget-metrikker krever AUTH header med gyldig token`() {
        val requestBody: String = objectMapper
            .writeValueAsString(
                TestUtils.vilkårligIaTjeneste()
            )

        val response = HttpClient.newBuilder().build().send(
            HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:$port/innlogget/iatjeneste"))
                .header(
                    HttpHeaders.AUTHORIZATION,
                    "Bearer " + "DETTE_ER_IKKE_EN_GYLDIG_TOKEN"
                )
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build(),
            BodyHandlers.ofString()
        )

        Assertions.assertThat(response.statusCode()).isEqualTo(401)
        Assertions.assertThat(response.body()).isEqualTo("{\"message\":\"You are not authorized to access this ressource\"}")
    }

}
