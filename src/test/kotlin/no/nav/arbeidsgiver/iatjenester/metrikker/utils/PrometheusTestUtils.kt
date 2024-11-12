package no.nav.arbeidsgiver.iatjenester.metrikker.utils

import org.hamcrest.Matchers
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.get

fun MockMvc.getPrometheusMetrics() = this.get("/internal/actuator/prometheus")

fun ResultActionsDsl.andExpectMetricValueToBe(
    metricName: String,
    value: Double,
) {
    this.andExpect {
        content {
            string(
                Matchers.containsString(
                    "$metricName $value",
                ),
            )
        }
    }
}
