package com.piieradication.agent.domain.usecase

import com.piieradication.agent.domain.model.PiiRecord
import com.piieradication.agent.domain.repository.PiiRepository
import javax.inject.Inject

/**
 * Used by the Worker (and by the UI's manual "Sync now" action) to
 * trigger a real network fetch followed by PII redaction + persistence.
 * Returns the freshly synced records so the caller can feed them into
 * broker detection.
 */
class SyncPiiUseCase @Inject constructor(
    private val repository: PiiRepository
) {
    suspend operator fun invoke(): List<PiiRecord> = repository.syncAndEradicate()
}
