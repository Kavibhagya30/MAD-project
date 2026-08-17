package com.piieradication.agent.domain.repository

import com.piieradication.agent.domain.model.PiiRecord
import kotlinx.coroutines.flow.Flow

interface PiiRepository {

    /** Live stream of all locally stored, already-redacted records. */
    fun observeRecords(): Flow<List<PiiRecord>>

    /**
     * Performs the real network call, redacts PII from the response,
     * and persists the redacted result to Room. Returns the freshly
     * synced records (so the caller can feed them into broker
     * detection) — throws on network/parse failure so the caller
     * (the Worker) can decide to retry.
     */
    suspend fun syncAndEradicate(): List<PiiRecord>

    suspend fun clearAll()
}
