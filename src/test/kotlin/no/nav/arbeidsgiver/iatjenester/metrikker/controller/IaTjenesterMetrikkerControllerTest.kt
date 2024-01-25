package no.nav.arbeidsgiver.iatjenester.metrikker.controller

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.arbeidsgiver.iatjenester.metrikker.IntegrationTestSuite
import no.nav.arbeidsgiver.iatjenester.metrikker.TestUtils.Companion.APPLICATION_JSON
import no.nav.arbeidsgiver.iatjenester.metrikker.TestUtils.Companion.ORGNR_SOM_RETURNERES_AV_MOCK_ALTINN
import no.nav.arbeidsgiver.iatjenester.metrikker.TestUtils.Companion.ORGNR_UTEN_NÆRINGSKODE_I_ENHETSREGISTERET
import no.nav.arbeidsgiver.iatjenester.metrikker.TestUtils.Companion.ugyldigInnloggetIaTjenesteAsString
import no.nav.arbeidsgiver.iatjenester.metrikker.TestUtils.Companion.vilkårligInnloggetIaTjenesteAsString
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers

private class IaTjenesterMetrikkerControllerTest : IntegrationTestSuite() {

    @Autowired
    private val mockOAuth2Server: MockOAuth2Server? = null

    @LocalServerPort
    lateinit var port: String

    private val objectMapper = ObjectMapper()
    private var innloggetEndepunkt = "/ia-tjenester-metrikker/innlogget/mottatt-iatjeneste"

    @Test
    fun `Innlogget endepunkt mottatt-ia-tjeneste skal tillate at mottakkelsesdato ikke sendes i payload`() {
        // language=JSON
        val requestBodyUtenMottakkelsesdato: String = """
            {
              "orgnr": "$ORGNR_SOM_RETURNERES_AV_MOCK_ALTINN",
              "kilde": "FOREBYGGE_FRAVÆR",
              "type": "DIGITAL_IA_TJENESTE"
            }
        """.trimIndent()
        val gyldigToken = issueGyldigTokenXToken()
        val response = HttpClient.newBuilder().build().send(
            HttpRequest.newBuilder()
                .uri(URI.create(hostAndPort() + innloggetEndepunkt))
                .header(HttpHeaders.AUTHORIZATION, "Bearer $gyldigToken")
                .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyUtenMottakkelsesdato))
                .build(),
            BodyHandlers.ofString()
        )

        Assertions.assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value())
        val body: JsonNode = objectMapper.readTree(response.body())
        Assertions.assertThat(body.get("status").asText()).isEqualTo("created")
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
    fun `Innlogget endepunkt mottatt-ia-tjeneste returnerer 200 OK dersom noe går galt ved kall til enhetsregisteret`() {
        val requestBody: String =
            vilkårligInnloggetIaTjenesteAsString(ORGNR_UTEN_NÆRINGSKODE_I_ENHETSREGISTERET)

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

        Assertions.assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
        val body: JsonNode = objectMapper.readTree(response.body())
        Assertions.assertThat(body.get("status").asText()).isEqualTo("ok")
    }

    @Test
    fun `Innlogget endepunkt mottatt-ia-tjeneste returnerer 403 forbidden dersom bruker ikke har rettigheter i Altinn`() {
        val brukerUtenRettigheter = "789999999"
        val requestBodyMedOrgnrBrukerIkkeHarTilgangTil: String =
            vilkårligInnloggetIaTjenesteAsString(brukerUtenRettigheter)

        val gyldigToken = issueGyldigTokenXToken()
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
        val ugyldigOrgnr = "83838"
        val requestBodyMedUgyldigOrgnr: String = vilkårligInnloggetIaTjenesteAsString(ugyldigOrgnr)

        val gyldigToken = issueGyldigTokenXToken()
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

    @Test
    fun `Innlogget endepunkt mottatt-ia-tjeneste returnerer 400 bad request ved ugyldig data (håndterer feil Enum verdi i request body)`() {

        val requestBody: String = ugyldigInnloggetIaTjenesteAsString(ORGNR_SOM_RETURNERES_AV_MOCK_ALTINN)

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

        Assertions.assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value())
        val body: JsonNode = objectMapper.readTree(response.body())
        Assertions.assertThat(body.get("message").asText()).startsWith("Innhold til request er ikke gyldig")
    }

    private fun hostAndPort() = "http://localhost:$port"

    private fun issueGyldigTokenXToken() =
        mockOAuth2Server!!.issueToken(
            issuerId = "tokenx",
            clientId = "localhost:teamia:min-ia",
            tokenCallback = DefaultOAuth2TokenCallback(
                issuerId = "tokenx",
                subject = "01079812345",
                typeHeader = "JWT",
                audience = listOf("someaudience"),
                claims = mapOf(Pair("pid", "01079812345")),
                expiry = 3600
            )
        ).serialize()
}
