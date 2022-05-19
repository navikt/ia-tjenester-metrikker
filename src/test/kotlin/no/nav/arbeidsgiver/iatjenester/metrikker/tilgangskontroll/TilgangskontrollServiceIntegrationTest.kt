package no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll

import arrow.core.Either
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
