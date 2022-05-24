package no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll

import arrow.core.Either
import com.nimbusds.jose.jwk.RSAKey
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnConfig
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnrettigheterProxyKlient
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnrettigheterProxyKlientConfig
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.ProxyConfig
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.error.exceptions.AltinnrettigheterProxyKlientFallbackException
import no.nav.arbeidsgiver.iatjenester.metrikker.TestUtils.Companion.TEST_FNR
import no.nav.arbeidsgiver.iatjenester.metrikker.TestUtils.Companion.testTokenForTestFNR
import no.nav.arbeidsgiver.iatjenester.metrikker.config.AltinnConfigProperties
import no.nav.arbeidsgiver.iatjenester.metrikker.config.AltinnServiceKey
import no.nav.arbeidsgiver.iatjenester.metrikker.config.TilgangskontrollConfig
import no.nav.arbeidsgiver.iatjenester.metrikker.config.TokenXConfigProperties
import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.fail
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import java.util.Optional

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableConfigurationProperties(value = [AltinnConfigProperties::class])
@EnableMockOAuth2Server
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureWireMock(port = 0)
internal class TilgangskontrollServiceIntegrationTest {

    private lateinit var dummyTilgangskontrollUtils: TilgangskontrollUtils
    private lateinit var tilgangskontrollService: TilgangskontrollService
    private lateinit var tilgangskontrollServiceHvorAltinnOgAltinnProxyIkkeSvarer: TilgangskontrollService
    private lateinit var proxyKlientSomIkkeSvarer: AltinnrettigheterProxyKlient
    private lateinit var dummyTokendingsService: TokendingsService

    @Autowired
    private lateinit var tokenXConfigProperties: TokenXConfigProperties

    @Autowired
    private lateinit var iaServiceIAltinnKonfig: TilgangskontrollConfig

    @Autowired
    private lateinit var altinnrettigheterProxyKlient: AltinnrettigheterProxyKlient


    init {
        val tokenValidationContextHolderMock: TokenValidationContextHolder =
            Mockito.mock(TokenValidationContextHolder::class.java)

        val tokenValidationContextMock: TokenValidationContext =
            Mockito.mock(TokenValidationContext::class.java)

        Mockito.`when`(tokenValidationContextHolderMock.tokenValidationContext)
            .thenReturn(tokenValidationContextMock)

        Mockito.`when`(tokenValidationContextMock.firstValidToken)
            .thenReturn(
                Optional.of(JwtToken(testTokenForTestFNR()))
            )

        dummyTilgangskontrollUtils =
            object : TilgangskontrollUtils(
                contextHolder = tokenValidationContextHolderMock,
            ) {
                override fun hentInnloggetBruker(): InnloggetBruker {
                    return InnloggetBruker(TEST_FNR)
                }
            }

        proxyKlientSomIkkeSvarer = AltinnrettigheterProxyKlient(
            AltinnrettigheterProxyKlientConfig(
                ProxyConfig(consumerId = "", url = "http://localhost:7777/virker/ikke/"),
                AltinnConfig(
                    url = "http://localhost:7778/virker/ikke/heller",
                    altinnApiGwApiKey = "",
                    altinnApiKey = ""
                )
            )
        )
    }


    @BeforeAll
    fun setUpClassUnderTestWithInjectedAndDummyBeans() {
        dummyTokendingsService =
            object : TokendingsService(
                tokenXConfig = tokenXConfigProperties
            ) {
                override fun clientAssertion(clientId: String, audience: String, rsaKey: RSAKey): String {
                    println("##################### Mock clientAssertion() called #######################")
                    return "eyJraWQiOiJjbGllbnRBc3NlcnRpb25LZXkiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJub3Rmb3VuZCIsImF1ZCI6Im5vdGZvdW5kIiwibmJmIjoxNjUzMzgwMzU3LCJpc3MiOiJub3Rmb3VuZCIsImV4cCI6MTY1MzM4MDQ1NywiaWF0IjoxNjUzMzgwMzU3fQ.T54HdPb64jSDeQCSYFP4Gk45WdtM-h1_7a-7l8ZVLFQgklP8kwkbA0Y6LgRGe4DC7hjUBMwpO_sJWavV1V0Ykg8M--bei0kGCgwp0scz-LxGCrPSxUWk3Oy8YyGtUWKAR-xN07dTp2kmt-bcBZ37N92G3_55jHQcqPHleiATsSRfheYU3bqtPUnDqNHCWEY7BZ6F7poqirb2LgKlTdXXy_2DRaY-oWMHjreirMK7bcjBjH3vCR4li94-WquNTJJL-EBTsuPxMVjydnSP-4S4d7K2GJhw65tQG9AXwNbEc_vo2wO_G74bz-MaW7gxLzA3FQ5NobnC-HgodSljJXFFWg"
                }
            }

        tilgangskontrollService =
            TilgangskontrollService(
                altinnrettigheterProxyKlient,
                iaServiceIAltinnKonfig,
                dummyTilgangskontrollUtils,
                dummyTokendingsService
            )
        tilgangskontrollServiceHvorAltinnOgAltinnProxyIkkeSvarer =
            TilgangskontrollService(
                proxyKlientSomIkkeSvarer,
                iaServiceIAltinnKonfig,
                dummyTilgangskontrollUtils,
                dummyTokendingsService
            )

    }


    @Test
    @Throws(Exception::class)
    fun `Verifiserer mot Altinn`() {
        val expectedInnloggetBruker = InnloggetBruker(TEST_FNR)
        expectedInnloggetBruker.organisasjoner = listOf(
            AltinnOrganisasjon(
                name = "BALLSTAD OG HORTEN",
                parentOrganizationNumber = null,
                organizationNumber = "811076112",
                organizationForm = "AS",
                status = "Active",
                type = "Enterprise",
            )
        )

        val actualInnloggetBruker =
            tilgangskontrollService.hentInnloggetBrukerFraAltinn(AltinnServiceKey.IA)

        Assertions.assertThat(actualInnloggetBruker.orNull()!!.fnr)
            .isEqualTo(expectedInnloggetBruker.fnr)
        Assertions.assertThat(actualInnloggetBruker.orNull()!!.organisasjoner)
            .isEqualTo(expectedInnloggetBruker.organisasjoner)
            .usingRecursiveFieldByFieldElementComparator(
                RecursiveComparisonConfiguration.builder().withStrictTypeChecking(true).build()
            )
    }

    @Test
    fun `Returnerer feil (Either Left) dersom hverken AltinnProxy eller Altinn svarer`() {
        val result =
            tilgangskontrollServiceHvorAltinnOgAltinnProxyIkkeSvarer.hentInnloggetBrukerFraAltinn(
                AltinnServiceKey.IA
            )

        when (result) {
            is Either.Left -> Assertions.assertThat(result.value)
                .isInstanceOf(AltinnrettigheterProxyKlientFallbackException::class.java)
            else -> fail("Returnerte ikke forventet feil")
        }
    }
}
