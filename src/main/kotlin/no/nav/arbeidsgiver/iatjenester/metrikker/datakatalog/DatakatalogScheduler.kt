package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog

import net.javacrumbs.shedlock.core.LockConfiguration
import net.javacrumbs.shedlock.core.LockingTaskExecutor
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class DatakatalogScheduler(val taskExecutor :LockingTaskExecutor, val datakatalogStatistikk: DatakatalogStatistikk) {

    @Scheduled(cron = "0 55 7 * * ?")
    fun scheduledUtsendingAvDatapakke() {
        val lockAtMostFor = Duration.of(10, ChronoUnit.MINUTES)
        val lockAtLeastFor = Duration.of(1, ChronoUnit.MINUTES)

        taskExecutor.executeWithLock(
            Runnable { datakatalogStatistikk.run() },
            LockConfiguration(Instant.now(), "utsending til datakatalog", lockAtMostFor, lockAtLeastFor)
        )
    }
}
