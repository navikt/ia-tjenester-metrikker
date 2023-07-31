package no.nav.arbeidsgiver.iatjenester.metrikker.observability

import io.prometheus.client.exporter.common.TextFormat
import no.nav.arbeidsgiver.iatjenester.metrikker.IntegrationTestSuite
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.Kilde
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get


internal class PrometheusMetricsTest : IntegrationTestSuite() {
    @Autowired
    lateinit var prometheusMetrics: PrometheusMetrics

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `Applikasjoner får registrert en counter ved oppstart`() {
        mockMvc.get("/internal/actuator/prometheus").andExpect {
            content {
                contentType(TextFormat.CONTENT_TYPE_004)
                string(
                    containsString(
                        "innloggede_ia_tjenester_metrikker_persistert_total{kilde=\"NETTKURS\",}"
                    )
                )
            }
        }
    }

    @Test
    fun `inkrementerInnloggedeMetrikkerPersistert inkrementerer Prometheus-metrikk`() {
        prometheusMetrics.inkrementerInnloggedeMetrikkerPersistert(
            Kilde.FOREBYGGE_FRAVÆR,
        )

        mockMvc.get("/internal/actuator/prometheus").andExpect {
            content {
                contentType(TextFormat.CONTENT_TYPE_004)
                string(
                    containsString(
                        "innloggede_ia_tjenester_metrikker_persistert_total{kilde=\"FOREBYGGE_FRAVÆR\",} 1.0"
                    )
                )
            }
        }
    }
}