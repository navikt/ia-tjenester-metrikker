package no.nav.arbeidsgiver.iatjenester.metrikker.observability

import io.prometheus.client.CollectorRegistry
import io.prometheus.client.Counter
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.Kilde
import org.springframework.stereotype.Component

@Component
class PrometheusMetrics(
    meterRegistry: CollectorRegistry,
) {
    private val innloggedeMetrikkerPersistert = Counter.build()
        .name("innloggede_ia_tjenester_metrikker_persistert")
        .help("Teller hvor mange innloggede IA-tjenestemetrikker som har blitt persistert i databasen")
        .labelNames("kilde")
        .register(meterRegistry)

    init {
        // For å kunne monitorere en kilde over tid, er vi avhengige at kilden har en teller knytta til seg fra a start.
        Kilde.entries.forEach {
            innloggedeMetrikkerPersistert.labels(it.name).inc(0.0)
        }
    }

    fun inkrementerInnloggedeMetrikkerPersistert(kilde: Kilde) {
        innloggedeMetrikkerPersistert.labels(kilde.name).inc()
    }
}