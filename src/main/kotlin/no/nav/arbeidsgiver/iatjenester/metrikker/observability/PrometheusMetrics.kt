package no.nav.arbeidsgiver.iatjenester.metrikker.observability

import io.prometheus.metrics.core.metrics.Counter
import io.prometheus.metrics.model.registry.PrometheusRegistry
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.Kilde
import org.springframework.stereotype.Component

@Component
class PrometheusMetrics(
    meterRegistry: PrometheusRegistry,
) {
    private val innloggedeMetrikkerPersistert = Counter.builder()
        .name("innloggede_ia_tjenester_metrikker_persistert")
        .help("Teller hvor mange innloggede IA-tjenestemetrikker som har blitt persistert i databasen")
        .labelNames("kilde")
        .withoutExemplars()
        .register(meterRegistry)

    init {
        settOppTellere()
    }

    fun inkrementerInnloggedeMetrikkerPersistert(kilde: Kilde) {
        innloggedeMetrikkerPersistert.labelValues(kilde.name).inc()
    }

    private fun settOppTellere() {
        Kilde.entries.forEach {
            innloggedeMetrikkerPersistert.labelValues(it.name).inc(0.0)
        }
    }
}