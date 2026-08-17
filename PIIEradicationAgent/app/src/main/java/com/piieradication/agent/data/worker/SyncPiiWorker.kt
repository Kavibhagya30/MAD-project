package com.piieradication.agent.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.piieradication.agent.data.notification.NotificationHelper
import com.piieradication.agent.domain.usecase.GenerateDeletionRequestsUseCase
import com.piieradication.agent.domain.usecase.ProcessDeletionRequestsUseCase
import com.piieradication.agent.domain.usecase.SyncPiiUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Real background worker chaining every automated objective from the
 * project brief into one periodic (and on-demand) run:
 *
 *  1. [SyncPiiUseCase] — real network fetch + PII redaction + Room persistence.
 *  2. [GenerateDeletionRequestsUseCase] — detect matching data brokers for
 *     any newly synced record and file PENDING deletion requests.
 *  3. [ProcessDeletionRequestsUseCase] — send anything pending and
 *     re-verify in-flight requests (continuous monitoring / re-verification).
 *  4. Any request that reaches COMPLETED in this pass triggers a local notification.
 *
 * Registered with Hilt via @HiltWorker so it gets constructor
 * injection just like any other class, and picked up automatically
 * by HiltWorkerFactory (wired in PiiApplication).
 */
@HiltWorker
class SyncPiiWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val syncPiiUseCase: SyncPiiUseCase,
    private val generateDeletionRequestsUseCase: GenerateDeletionRequestsUseCase,
    private val processDeletionRequestsUseCase: ProcessDeletionRequestsUseCase,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val syncedRecords = syncPiiUseCase()
            Log.i(TAG, "Synced and eradicated PII for ${syncedRecords.size} record(s).")

            val newRequests = generateDeletionRequestsUseCase(syncedRecords)
            Log.i(TAG, "Filed $newRequests new deletion request(s) against detected brokers.")

            val newlyCompleted = processDeletionRequestsUseCase()
            newlyCompleted.forEach { notificationHelper.notifyCompleted(it) }
            Log.i(TAG, "${newlyCompleted.size} deletion request(s) confirmed complete this cycle.")

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed: ${e.message}", e)
            if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.failure()
        }
    }

    companion object {
        private const val TAG = "SyncPiiWorker"
        private const val MAX_RETRIES = 3
    }
}
