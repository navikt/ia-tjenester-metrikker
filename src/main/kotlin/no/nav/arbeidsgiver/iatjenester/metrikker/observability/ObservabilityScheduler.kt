package no.nav.arbeidsgiver.iatjenester.metrikker.observability

import net.javacrumbs.shedlock.core.LockConfiguration
import net.javacrumbs.shedlock.core.LockingTaskExecutor
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class ObservabilityScheduler(val taskExecutor :LockingTaskExecutor, val observabilityService: ObservabilityService) {

    @Scheduled(cron = "0 30 8 * * Mon-Fri")
    fun scheduledSjekkAntallIATjenestertMetrikkerLagret() {
        val lockAtMostFor = Duration.of(10, ChronoUnit.MINUTES)
        val lockAtLeastFor = Duration.of(1, ChronoUnit.MINUTES)

        taskExecutor.executeWithLock(
            Runnable { observabilityService.run() },
            LockConfiguration(Instant.now(), "sjekk antall metrikker mottatt", lockAtMostFor, lockAtLeastFor)
        )
    }
}
