package no.nav.arbeidsgiver.iatjenester.metrikker.observability

import no.nav.arbeidsgiver.iatjenester.metrikker.IntegrationTestSuite
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.Kilde
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.andExpectMetricValueToBe
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.getPrometheusMetrics
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.annotation.DirtiesContext.MethodMode.BEFORE_METHOD
import org.springframework.test.web.servlet.MockMvc


internal class PrometheusMetricsTest : IntegrationTestSuite() {
    @Autowired
    lateinit var prometheusMetrics: PrometheusMetrics

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    @DirtiesContext(methodMode = BEFORE_METHOD)
    fun `Applikasjoner får registrert en counter ved oppstart`() {
        val metricName = "innloggede_ia_tjenester_metrikker_persistert_total{kilde=\"FOREBYGGE_FRAVÆR\",}"
        mockMvc.getPrometheusMetrics().andExpectMetricValueToBe(metricName, 0.0)
    }

    @Test
    fun `inkrementerInnloggedeMetrikkerPersistert inkrementerer Prometheus-metrikk`() {
        prometheusMetrics.inkrementerInnloggedeMetrikkerPersistert(
            Kilde.FOREBYGGE_FRAVÆR,
        )
        val metricName = "innloggede_ia_tjenester_metrikker_persistert_total{kilde=\"FOREBYGGE_FRAVÆR\",}"
        mockMvc.getPrometheusMetrics().andExpectMetricValueToBe(metricName, 1.0)
    }
}
