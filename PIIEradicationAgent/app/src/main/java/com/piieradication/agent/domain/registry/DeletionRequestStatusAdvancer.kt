package com.piieradication.agent.domain.registry

import com.piieradication.agent.domain.model.DeletionRequestStatus
import com.piieradication.agent.domain.model.DeletionRequestStatus.ACKNOWLEDGED
import com.piieradication.agent.domain.model.DeletionRequestStatus.COMPLETED
import com.piieradication.agent.domain.model.DeletionRequestStatus.SENT

/**
 * Pure re-verification logic for the "continuous monitoring &
 * re-verification" objective: given how long a request has been sitting
 * since it was sent, decide whether it should move forward. Kept free of
 * WorkManager/Room/System.currentTimeMillis() so it is trivially unit
 * testable (see [DeletionRequestStatusAdvancerTest]).
 */
object DeletionRequestStatusAdvancer {

    /** A broker is assumed to acknowledge a request after this much wall-clock time. */
    const val ACKNOWLEDGE_AFTER_MILLIS = 2 * 60 * 1000L

    /** ...and to have actually completed the deletion after this much. */
    const val COMPLETE_AFTER_MILLIS = 4 * 60 * 1000L

    /**
     * @param current status the request is currently in.
     * @param elapsedSinceCreatedMillis wall-clock time since the request was first sent.
     * @return the status the request should now be in. Only advances SENT/ACKNOWLEDGED
     *   forward — PENDING, FAILED and COMPLETED are left for other flows to manage.
     */
    fun advance(current: DeletionRequestStatus, elapsedSinceCreatedMillis: Long): DeletionRequestStatus {
        if (current != SENT && current != ACKNOWLEDGED) return current
        return when {
            elapsedSinceCreatedMillis >= COMPLETE_AFTER_MILLIS -> COMPLETED
            elapsedSinceCreatedMillis >= ACKNOWLEDGE_AFTER_MILLIS -> ACKNOWLEDGED
            else -> current
        }
    }
}
