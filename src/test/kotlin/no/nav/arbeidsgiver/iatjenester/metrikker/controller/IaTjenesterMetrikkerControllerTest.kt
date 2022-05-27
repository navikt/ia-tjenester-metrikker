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
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
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
@AutoConfigureWireMock(port = 0)
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
    fun `POST til uinnlogget-iatjeneste endepunkt skal returnere 201 created ved suksess`() {
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

        Assertions.assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value())
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

        Assertions.assertThat(response.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value())
        Assertions.assertThat(response.body())
            .isEqualTo("{\"message\":\"You are not authorized to access this resource\"}")
    }

    @Test
    fun `Innlogget endepunkt mottatt-ia-tjeneste returnerer 201 OK dersom token er gyldig`() {
        val requestBody: String =
            vilkårligInnloggetIaTjenesteAsString(ORGNR_SOM_RETURNERES_AV_MOCK_ALTINN)

        //val gyldigToken = getFakedingsToken()
        val gyldigToken = issueGyldigTokenXToken()
        val response = HttpClient.newBuilder().build().send(
            HttpRequest.newBuilder()
                .uri(URI.create(hostAndPort() + innloggetEndepunkt))
                .header(HttpHeaders.AUTHORIZATION, "Bearer $gyldigToken")
                .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build(),
            BodyHandlers.ofString()
        )

        Assertions.assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value())
        val body: JsonNode = objectMapper.readTree(response.body())
        Assertions.assertThat(body.get("status").asText()).isEqualTo("created")
    }

    @Test
    fun `Innlogget endepunkt mottatt-ia-tjeneste validerer tokens fra tokenX og returnerer 201 OK dersom token er gyldig`() {
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

        Assertions.assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value())
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

        Assertions.assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
        val body: JsonNode = objectMapper.readTree(response.body())
        Assertions.assertThat(body.get("status").asText()).isEqualTo("ok")
    }

    @Test
    fun `Innlogget endepunkt mottatt-ia-tjeneste returnerer 403 forbidden dersom bruker ikke har rettigheter i Altinn`() {
        val requestBodyMedOrgnrBrukerIkkeHarTilgangTil: String = vilkårligInnloggetIaTjenesteAsString("789999999")

        val gyldigToken = issueGyldigSelvbetjeningToken()
        val response = HttpClient.newBuilder().build().send(
            HttpRequest.newBuilder()
                .uri(URI.create(hostAndPort() + innloggetEndepunkt))
                .header(HttpHeaders.AUTHORIZATION, "Bearer $gyldigToken")
                .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyMedOrgnrBrukerIkkeHarTilgangTil))
                .build(),
            BodyHandlers.ofString()
        )

        Assertions.assertThat(response.statusCode()).isEqualTo(HttpStatus.FORBIDDEN.value())
        val body: JsonNode = objectMapper.readTree(response.body())
        Assertions.assertThat(body.get("status").asText()).isEqualTo("forbidden")
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

        Assertions.assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value())
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
                "JWT",
                listOf("aud-localhost"),
                mapOf(Pair("pid", "01079812345")),
                3600
            )
        ).serialize()

    private fun getFakedingsToken() =
        "eyJraWQiOiJtb2NrLW9hdXRoMi1zZXJ2ZXIta2V5IiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiIyZDhlZjJiZi0zOWQyLTQwZTEtOGI5Mi05NmRlN2EzODMzYTEiLCJhbXIiOlsiQmFua0lEIl0sImlzcyI6Imh0dHBzOlwvXC9mYWtlZGluZ3MuZGV2LWdjcC5uYWlzLmlvXC9mYWtlIiwicGlkIjoiMTIzNDU2Nzg5MTAiLCJsb2NhbGUiOiJuYiIsInRva2VuX3R5cGUiOiJCZWFyZXIiLCJjbGllbnRfaWQiOiJkZXYtZ2NwOnRlYW1pYTptaW4taWEiLCJhdWQiOiJkZXYtZ2NwOmFyYmVpZHNnaXZlcjppYS10amVuZXN0ZXItbWV0cmlra2VyIiwiYWNyIjoiTGV2ZWw0IiwibmJmIjoxNjUzNjQ4NjIwLCJpZHAiOiJodHRwczpcL1wvZmFrZWRpbmdzLmRldi1nY3AubmFpcy5pb1wvZmFrZVwvaWRwb3J0ZW4iLCJzY29wZSI6Im9wZW5pZCIsImV4cCI6MTY1NzI0ODYyMCwiaWF0IjoxNjUzNjQ4NjIwLCJjbGllbnRfb3Jnbm8iOiI4ODk2NDA3ODIiLCJqdGkiOiJmZDA0NGRjYy05ODQ0LTQ2YzItOWNmMC00YjI3ODhhY2I2NjMifQ.ZBpaOUGXrzRoVc0eHefQuQx0cQLMVBrqs-y16JNswKWIWrziHpmQ24tYHDKKyYJoG_hOPA_a8B6s-Yivq3pugTsR48-v60r9H4XGJfwhVVc-C-RJzw34qlUOcTT4aVb6ZOX5bSY5h9IKyvv9EnxGVcR69D0uwhugTypqeJ1wR91RJAhCvvw-l_x_VkTmja7mAJQr_nbQJdMw44rMuLeVkEXDOvypLOpJxKF3KsfJibBvtF3lsblEp2ZxJ1-jZGN0mmHm8LVEIavw4zqL6z_brN2xVAqtonvhBURbDsJcFQ-mnNVRftkjaBe835_bRgFoV9AvmD5DroSSHOGrrd7Zkg"

    private fun issueGyldigTokenXToken() =
        mockOAuth2Server!!.issueToken(
            "tokenx",
            "localhost:teamia:min-ia",
            DefaultOAuth2TokenCallback(
                "tokenx",
                "01079812345",
                "JWT",
                listOf("localhost:arbeidsgiver:ia-tjenester-metrikker"),
                mapOf(Pair("pid", "01079812345")),
                3600
            )
        ).serialize()
}
