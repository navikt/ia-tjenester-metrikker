package no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll

import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnConfig
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnrettigheterProxyKlient
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnrettigheterProxyKlientConfig
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.ProxyConfig
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.error.exceptions.AltinnrettigheterProxyKlientFallbackException
import no.nav.arbeidsgiver.iatjenester.metrikker.TestUtils.Companion.TEST_FNR
import no.nav.arbeidsgiver.iatjenester.metrikker.TestUtils.Companion.testTokenForTestFNR
import no.nav.arbeidsgiver.iatjenester.metrikker.config.AltinnConfigProperties
import no.nav.arbeidsgiver.iatjenester.metrikker.config.TilgangskontrollConfigProperties
import no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll.AltinnServiceId.IA_SERVICE
import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.assertj.core.api.Assertions
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableConfigurationProperties(value = [AltinnConfigProperties::class])
@EnableMockOAuth2Server
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = ["wiremock.port=8686"])
internal class TilgangskontrollServiceIntegrationTest {

    private lateinit var dummyTilgangskontrollUtils: TilgangskontrollUtils
    private lateinit var tilgangskontrollService: TilgangskontrollService
    private lateinit var tilgangskontrollServiceHvorAltinnOgAltinnProxyIkkeSvarer: TilgangskontrollService
    private lateinit var proxyKlientSomIkkeSvarer: AltinnrettigheterProxyKlient

    @Autowired
    private lateinit var iaServiceIAltinnKonfig: TilgangskontrollConfigProperties

    @Autowired
    private lateinit var altinnrettigheterProxyKlient: AltinnrettigheterProxyKlient



    init {
        val dummyTokenValidationContextHolder: TokenValidationContextHolder = object : TokenValidationContextHolder {
            override fun getTokenValidationContext(): TokenValidationContext {
                return object : TokenValidationContext(emptyMap()) {
                    override fun getJwtToken(issuerName: String?): JwtToken {
                        return JwtToken(testTokenForTestFNR())
                    }
                }
            }

            override fun setTokenValidationContext(tokenValidationContext: TokenValidationContext) {
                /* do nothing */
            }
        }

        dummyTilgangskontrollUtils =
            object : TilgangskontrollUtils(contextHolder = dummyTokenValidationContextHolder) {
                override fun erInnloggetSelvbetjeningBruker(): Boolean {
                    return true
                }

                override fun hentInnloggetSelvbetjeningBruker(): InnloggetBruker {
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
            TilgangskontrollService(altinnrettigheterProxyKlient, iaServiceIAltinnKonfig, dummyTilgangskontrollUtils)
        tilgangskontrollServiceHvorAltinnOgAltinnProxyIkkeSvarer =
            TilgangskontrollService(proxyKlientSomIkkeSvarer, iaServiceIAltinnKonfig, dummyTilgangskontrollUtils)
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

        val actualInnloggetBruker = tilgangskontrollService.hentInnloggetBruker(IA_SERVICE)

        Assertions.assertThat(actualInnloggetBruker.orNull()!!.fnr).isEqualTo(expectedInnloggetBruker.fnr)
        Assertions.assertThat(actualInnloggetBruker.orNull()!!.organisasjoner)
            .isEqualTo(expectedInnloggetBruker.organisasjoner)
            .usingRecursiveFieldByFieldElementComparator(
                RecursiveComparisonConfiguration.builder().withStrictTypeChecking(true).build()
            )
    }

    @Test
    @Throws(Exception::class)
    fun `Kaster Exception dersom hverken AltinnProxy eller Altinn svarer`() {

        Assertions.assertThatExceptionOfType(AltinnrettigheterProxyKlientFallbackException::class.java).isThrownBy {
            tilgangskontrollServiceHvorAltinnOgAltinnProxyIkkeSvarer.hentInnloggetBruker(IA_SERVICE)
        }
    }
}


