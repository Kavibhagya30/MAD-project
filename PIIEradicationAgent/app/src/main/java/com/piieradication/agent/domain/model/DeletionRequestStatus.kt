package com.piieradication.agent.domain.model

/**
 * Lifecycle of a single deletion request sent to a data broker on
 * behalf of an exposed [PiiRecord].
 *
 *   PENDING -> SENT -> ACKNOWLEDGED -> COMPLETED
 *      \-------------> FAILED (retried next cycle)
 */
enum class DeletionRequestStatus {
    PENDING,
    SENT,
    ACKNOWLEDGED,
    COMPLETED,
    FAILED
}
