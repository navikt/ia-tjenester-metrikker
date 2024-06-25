package no.nav.arbeidsgiver.iatjenester.metrikker.service


import arrow.core.Either
import no.nav.arbeidsgiver.iatjenester.metrikker.IntegrationTestSuite
import no.nav.arbeidsgiver.iatjenester.metrikker.TestUtils
import no.nav.arbeidsgiver.iatjenester.metrikker.observability.PrometheusMetrics
import no.nav.arbeidsgiver.iatjenester.metrikker.repository.IaTjenesterMetrikkerRepository
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.MottattIaTjenesteMedVirksomhetGrunndata
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.andExpectMetricValueToBe
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.getPrometheusMetrics
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.MockMvc
import java.time.ZonedDateTime.now

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
internal class IaTjenesterMetrikkerServiceTest : IntegrationTestSuite() {
    @Autowired
    lateinit var prometheusMetrics: PrometheusMetrics

    @MockBean
    lateinit var iaTjenesterMetrikkerRepository: IaTjenesterMetrikkerRepository

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    @Throws(Exception::class)
    fun `sjekkOgPersister validerer gyldig IA-tjeneste OK`() {

        val sjekkOgOpprett =
            IaTjenesterMetrikkerService(iaTjenesterMetrikkerRepository, prometheusMetrics)
                .sjekkOgPersister(TestUtils.vilkårligIaTjeneste())

        assertThat(sjekkOgOpprett is Either.Right).isEqualTo(true)
    }

    @Test
    @Throws(Exception::class)
    fun `sjekkOgPersister oppdaterer teller for metrikk`() {

        val counterName = "innloggede_ia_tjenester_metrikker_persistert_total{kilde=\"FOREBYGGE_FRAVÆR\"}"
        mockMvc.getPrometheusMetrics().andExpectMetricValueToBe(counterName, 0.0)

        val sjekkOgOpprett =
            IaTjenesterMetrikkerService(iaTjenesterMetrikkerRepository, prometheusMetrics)
                .sjekkOgPersister(TestUtils.vilkårligIaTjeneste())

        assertThat(sjekkOgOpprett is Either.Right).isEqualTo(true)

        mockMvc.getPrometheusMetrics().andExpectMetricValueToBe(counterName, 1.0)
    }

    @Test
    @Throws(Exception::class)
    fun `Skal ikke godkjenne datoer i fremtiden`() {

        val iaTjenesteMedDatoIFremtiden = TestUtils.vilkårligIaTjeneste()
        iaTjenesteMedDatoIFremtiden.tjenesteMottakkelsesdato = now().plusMinutes(2)

        val iaSjekk =
            IaTjenesterMetrikkerService(iaTjenesterMetrikkerRepository, prometheusMetrics)
                .sjekkOgPersister(iaTjenesteMedDatoIFremtiden)

        assertThat(iaSjekk is Either.Left).isEqualTo(true)
        assertThat((iaSjekk as Either.Left).value.årsak)
            .isEqualTo("tjenesteMottakkelsesdato kan ikke være i fremtiden")
    }

    @Test
    fun `Fylke blir utledet fra kommunenummer før innlogget IA-tjeneste persisteres`() {
        val levertIaTjenesteFraInnlandet =
            TestUtils.vilkårligIaTjeneste().apply { kommunenummer = "3403" }

        val resultat =
            IaTjenesterMetrikkerService(iaTjenesterMetrikkerRepository, prometheusMetrics)
                .sjekkOgPersister(levertIaTjenesteFraInnlandet)

        val persisterteData = (resultat as Either.Right).value
                as MottattIaTjenesteMedVirksomhetGrunndata

        assertThat(persisterteData.fylke).isEqualTo("Innlandet")
    }
}

