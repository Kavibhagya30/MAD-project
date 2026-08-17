package com.piieradication.agent.domain.usecase

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.piieradication.agent.data.worker.SyncPiiWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Wraps all real WorkManager scheduling for the app: a periodic
 * background sync (network-constrained, with backoff) plus an
 * on-demand one-off sync triggered from the UI.
 */
class SchedulePiiSyncUseCase @Inject constructor(
    private val workManager: WorkManager
) {

    fun schedulePeriodic() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<SyncPiiWorker>(
            repeatInterval = 6, repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun triggerOneOffNow() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<SyncPiiWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 15, TimeUnit.SECONDS)
            .build()

        workManager.enqueueUniqueWork(
            ONE_OFF_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    companion object {
        const val PERIODIC_WORK_NAME = "pii_periodic_sync"
        const val ONE_OFF_WORK_NAME = "pii_manual_sync"
    }
}
