package no.nav.arbeidsgiver.iatjenester.metrikker.observability

import io.prometheus.client.CollectorRegistry
import io.prometheus.client.Counter
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.Kilde
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.TypeIATjeneste
import org.springframework.stereotype.Component

@Component
class PrometheusMetrics(
    meterRegistry: CollectorRegistry,
    ) {
    private val innloggedeMetrikkerPersistert = Counter.build()
        .name("innloggede_metrikker_persistert")
        .help("Teller hvor mange innloggede IA-tjenestemetrikker som har blitt persistert i databasen")
        .labelNames("kilde", "type")
        .register(meterRegistry)

    fun inkrementerInnloggedeMetrikkerPersistert(kilde: Kilde, type: TypeIATjeneste) {
        innloggedeMetrikkerPersistert.labels(kilde.name, type.name).inc()
    }
}