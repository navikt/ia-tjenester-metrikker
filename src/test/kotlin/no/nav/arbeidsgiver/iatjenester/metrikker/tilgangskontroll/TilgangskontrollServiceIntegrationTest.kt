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
    private lateinit var dummyTokendingsService: TokenxService

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
            object : TokenxService(
                tokenXConfig = tokenXConfigProperties
            ) {
                override fun exchangeTokenToAltinnProxy(subjectToken: JwtToken): JwtToken {
                    return JwtToken(FAKE_TOKEN_FRA_TOKENX)
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

    companion object {
        val FAKE_TOKEN_FRA_TOKENX = "eyJraWQiOiJtb2NrLW9hdXRoMi1zZXJ2ZXIta2V5IiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiIzNDVjMzkzMi0wZmM3LTRmYWQtOTZhMi1kYzYzOWU0NDZhYzgiLCJhbXIiOlsiQmFua0lEIl0sImlzcyI6Imh0dHBzOlwvXC9mYWtlZGluZ3MuZGV2LWdjcC5uYWlzLmlvXC9mYWtlIiwicGlkIjoibm90Zm91bmQiLCJsb2NhbGUiOiJuYiIsInRva2VuX3R5cGUiOiJCZWFyZXIiLCJjbGllbnRfaWQiOiJub3Rmb3VuZCIsImF1ZCI6Im5vdGZvdW5kIiwiYWNyIjoibm90Zm91bmQiLCJuYmYiOjE2NTMzODg3OTcsImlkcCI6Imh0dHBzOlwvXC9mYWtlZGluZ3MuZGV2LWdjcC5uYWlzLmlvXC9mYWtlXC9pZHBvcnRlbiIsInNjb3BlIjoib3BlbmlkIiwiZXhwIjoxNjU2OTg4Nzk3LCJpYXQiOjE2NTMzODg3OTcsImNsaWVudF9vcmdubyI6Ijg4OTY0MDc4MiIsImp0aSI6ImU0MDQ3OTVlLWQ3YmUtNDI2NS1iZTQ2LTFhY2ExZTU3Zjc5MyJ9.DfQ9vojDed9IR8-7r2DmgpToUaBwb70-t_k2BVKnWZhTaDu2y85nS2ME4niGxutXBtZbzhQsgDPQ1eHAnX7gBgvjwyEhbhXKHfx-FgiSVXqLw6fvBUsmg1PP07a1fhZJ1RXXDSN8sM5ImPEomhOEnRLPgFsLcfPYC_44HTHxXP37wPWcioo3DW_lPb90ApgehgNbGzUu5YJm0QFaPI71jKdLhpNWs6ybYLbpOciQJPT-e1eoRNtuWblKJQc8nNU7JTVMBVRv6kVPC_V2Gi5ggF3fIDnBXFdUmpPJIj_F-ezO50KVExKT6KQDSVWLjmn8WaSr37LkN8CF27soth2Kpg";
    }
}
