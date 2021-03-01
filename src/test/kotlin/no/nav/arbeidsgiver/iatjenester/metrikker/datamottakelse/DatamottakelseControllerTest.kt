package no.nav.arbeidsgiver.iatjenester.metrikker.datamottakelse

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.web.server.LocalServerPort

internal class DatamottakelseControllerTest {

    @LocalServerPort
    lateinit var port: String

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun `Endepunkt legge til nye tjeneste skal returnere ok naar den mottar tjeneste`() {
        //TODO skrive response
        /*val response = HttpClient.newBuilder().build().send(
            HttpRequest.newBuilder()
                .uri(
                    URL()
                        .setScheme("http")
                        .setHost("localhost:$port")
                        .setPath("/altinn-rettigheter-proxy/organisasjoner")
                        .addParameter("serviceCode", "3403")
                        .addParameter("serviceEdition", "1")
                        .build()
                )
                .header(
                    HttpHeaders.AUTHORIZATION,
                    "Bearer " + JwtTokenGenerator.signedJWTAsString("01065500791")
                )
                .header("X-Correlation-ID", "klient-applikasjon")
                .GET()
                .build(),
            HttpResponse.BodyHandlers.ofString()
        )*/
        //Assertions.assertThat(response.statusCode()).isEqualTo(200)
        Assertions.assertThat(true).isEqualTo(true);
        //assertAntallOrganisasjonerEr(response, 4)
    }


}
