package no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll

import arrow.core.Either
import no.nav.arbeidsgiver.iatjenester.metrikker.IntegrationTestSuite
import no.nav.arbeidsgiver.iatjenester.metrikker.TestUtils.Companion.TEST_FNR
import no.nav.arbeidsgiver.iatjenester.metrikker.TestUtils.Companion.testTokenForTestFNR
import no.nav.arbeidsgiver.iatjenester.metrikker.altinn.AltinnTilgangerKlient
import no.nav.arbeidsgiver.iatjenester.metrikker.config.TokenXConfigProperties
import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtToken
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.fail
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration
import org.junit.jupiter.api.BeforeAll
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.client.RestTemplate
import kotlin.test.Test

internal class TilgangskontrollServiceIntegrationTest : IntegrationTestSuite() {
    private lateinit var dummyTilgangskontrollUtils: TilgangskontrollUtils
    private lateinit var tilgangskontrollService: TilgangskontrollService
    private lateinit var tilgangskontrollServiceHvorAltinnTilgangIkkeSvarer: TilgangskontrollService
    private lateinit var altinnTilgangerKlientSomIkkeSvarer: AltinnTilgangerKlient
    private lateinit var dummyTokendingsService: TokenxService

    @Autowired
    private lateinit var tokenXConfigProperties: TokenXConfigProperties

    @Autowired
    private lateinit var altinnTilgangerKlient: AltinnTilgangerKlient

    @Autowired
    private lateinit var restTemplateAltinnTilganger: RestTemplate

    init {
        val tokenValidationContextHolderMock: TokenValidationContextHolder =
            Mockito.mock(TokenValidationContextHolder::class.java)

        val tokenValidationContextMock: TokenValidationContext =
            Mockito.mock(TokenValidationContext::class.java)

        Mockito.`when`(tokenValidationContextHolderMock.getTokenValidationContext())
            .thenReturn(tokenValidationContextMock)

        Mockito.`when`(tokenValidationContextMock.firstValidToken)
            .thenReturn(
                JwtToken(testTokenForTestFNR()),
            )

        dummyTilgangskontrollUtils =
            object : TilgangskontrollUtils(
                contextHolder = tokenValidationContextHolderMock,
            ) {
                override fun hentInnloggetBruker(): InnloggetBruker = InnloggetBruker(TEST_FNR)
            }
    }

    @BeforeAll
    fun setUpClassUnderTestWithInjectedAndDummyBeans() {
        altinnTilgangerKlientSomIkkeSvarer = AltinnTilgangerKlient(
            restTemplate = restTemplateAltinnTilganger,
            altinnTilgangerApiUrl = "http://localhost:7778/virker/ikke/heller",
        )

        dummyTokendingsService =
            object : TokenxService(
                tokenXConfig = tokenXConfigProperties,
            ) {
                override fun exchangeTokenToAltinnTilganger(subjectToken: JwtToken): JwtToken =
                    JwtToken(FAKE_TOKEN_FRA_TOKENX)
            }

        tilgangskontrollService =
            TilgangskontrollService(
                altinnTilgangerKlient,
                dummyTilgangskontrollUtils,
                dummyTokendingsService,
            )
        tilgangskontrollServiceHvorAltinnTilgangIkkeSvarer =
            TilgangskontrollService(
                altinnTilgangerKlientSomIkkeSvarer,
                dummyTilgangskontrollUtils,
                dummyTokendingsService,
            )
    }

    @Test
    @Throws(Exception::class)
    fun `Verifiserer mot Altinn`() {
        val expectedInnloggetBruker = InnloggetBruker(TEST_FNR)
        expectedInnloggetBruker.organisasjoner = listOf(
            AltinnOrganisasjon(
                name = "BALLSTAD OG HORTEN AS",
                parentOrganizationNumber = "",
                organizationNumber = "811076112",
                organizationForm = "ORG",
                status = null,
                type = null,
            ),
            AltinnOrganisasjon(
                name = "BALLSTAD OG HORTEN",
                parentOrganizationNumber = "811076112",
                organizationNumber = "833445566",
                organizationForm = "AS",
                status = null,
                type = null,
            ),
        )

        val actualInnloggetBruker =
            tilgangskontrollService.hentInnloggetBrukerFraAltinn()

        Assertions.assertThat(actualInnloggetBruker.getOrNull()!!.fnr)
            .isEqualTo(expectedInnloggetBruker.fnr)
        Assertions.assertThat(actualInnloggetBruker.getOrNull()!!.organisasjoner)
            .isEqualTo(expectedInnloggetBruker.organisasjoner)
            .usingRecursiveFieldByFieldElementComparator(
                RecursiveComparisonConfiguration.builder().withStrictTypeChecking(true).build(),
            )
    }

    @Test
    fun `Returnerer feil (Either Left) dersom AltinnTilganger ikke svarer`() {
        val result =
            tilgangskontrollServiceHvorAltinnTilgangIkkeSvarer.hentInnloggetBrukerFraAltinn()

        when (result) {
            is Either.Right -> Assertions.assertThat(result.value.organisasjoner).isEmpty()

            else -> fail("Returnerte ikke forventet resultat")
        }
    }

    companion object {
        val FAKE_TOKEN_FRA_TOKENX =
            "eyJraWQiOiJtb2NrLW9hdXRoMi1zZXJ2ZXIta2V5IiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiIzNDVjMzkzMi0wZmM3LTRmYWQtOTZhMi1kYzYzOWU0NDZhYzgiLCJhbXIiOlsiQmFua0lEIl0sImlzcyI6Imh0dHBzOlwvXC9mYWtlZGluZ3MuZGV2LWdjcC5uYWlzLmlvXC9mYWtlIiwicGlkIjoibm90Zm91bmQiLCJsb2NhbGUiOiJuYiIsInRva2VuX3R5cGUiOiJCZWFyZXIiLCJjbGllbnRfaWQiOiJub3Rmb3VuZCIsImF1ZCI6Im5vdGZvdW5kIiwiYWNyIjoibm90Zm91bmQiLCJuYmYiOjE2NTMzODg3OTcsImlkcCI6Imh0dHBzOlwvXC9mYWtlZGluZ3MuZGV2LWdjcC5uYWlzLmlvXC9mYWtlXC9pZHBvcnRlbiIsInNjb3BlIjoib3BlbmlkIiwiZXhwIjoxNjU2OTg4Nzk3LCJpYXQiOjE2NTMzODg3OTcsImNsaWVudF9vcmdubyI6Ijg4OTY0MDc4MiIsImp0aSI6ImU0MDQ3OTVlLWQ3YmUtNDI2NS1iZTQ2LTFhY2ExZTU3Zjc5MyJ9.DfQ9vojDed9IR8-7r2DmgpToUaBwb70-t_k2BVKnWZhTaDu2y85nS2ME4niGxutXBtZbzhQsgDPQ1eHAnX7gBgvjwyEhbhXKHfx-FgiSVXqLw6fvBUsmg1PP07a1fhZJ1RXXDSN8sM5ImPEomhOEnRLPgFsLcfPYC_44HTHxXP37wPWcioo3DW_lPb90ApgehgNbGzUu5YJm0QFaPI71jKdLhpNWs6ybYLbpOciQJPT-e1eoRNtuWblKJQc8nNU7JTVMBVRv6kVPC_V2Gi5ggF3fIDnBXFdUmpPJIj_F-ezO50KVExKT6KQDSVWLjmn8WaSr37LkN8CF27soth2Kpg"
    }
}
