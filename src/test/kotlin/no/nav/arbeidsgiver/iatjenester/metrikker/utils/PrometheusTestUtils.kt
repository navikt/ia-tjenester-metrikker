package no.nav.arbeidsgiver.iatjenester.metrikker.utils

import io.prometheus.client.exporter.common.TextFormat
import org.hamcrest.Matchers
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.get

fun MockMvc.getPrometheusMetrics() = this.get("/internal/actuator/prometheus")

fun ResultActionsDsl.andExpectMetricValueToBe(metricName: String, value: Double) {
    this.andExpect {
        content {
            contentType(TextFormat.CONTENT_TYPE_004)
            string(
                Matchers.containsString(
                    "$metricName $value"
                )
            )
        }
    }
}