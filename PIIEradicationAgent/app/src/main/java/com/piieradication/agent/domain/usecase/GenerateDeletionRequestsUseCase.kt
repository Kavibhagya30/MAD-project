package com.piieradication.agent.domain.usecase

import com.piieradication.agent.domain.model.PiiRecord
import com.piieradication.agent.domain.repository.DeletionRequestRepository
import javax.inject.Inject

/** Detects matching data brokers for each newly synced record and files a PENDING request per broker. */
class GenerateDeletionRequestsUseCase @Inject constructor(
    private val repository: DeletionRequestRepository
) {
    suspend operator fun invoke(records: List<PiiRecord>): Int =
        records.sumOf { repository.generateForRecord(it) }
}
