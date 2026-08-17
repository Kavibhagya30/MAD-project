package com.piieradication.agent.domain.repository

import com.piieradication.agent.domain.model.DeletionRequest
import com.piieradication.agent.domain.model.PiiRecord
import kotlinx.coroutines.flow.Flow

interface DeletionRequestRepository {

    /** Live stream of every deletion request ever generated, any status. */
    fun observeAll(): Flow<List<DeletionRequest>>

    /**
     * Detects which known data brokers are likely holding [record]'s PII
     * and creates a PENDING request for each one not already requested.
     * Returns how many new requests were created.
     */
    suspend fun generateForRecord(record: PiiRecord): Int

    /**
     * One re-verification pass: submits PENDING/retryable requests over
     * the network, and advances in-flight (SENT/ACKNOWLEDGED) requests
     * based on elapsed time. Returns the ids of requests that newly
     * reached COMPLETED in this pass (for notifying the user).
     */
    suspend fun processAndAdvance(): List<DeletionRequest>
}
