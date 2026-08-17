package com.piieradication.agent.domain.usecase

import com.piieradication.agent.domain.model.DeletionRequest
import com.piieradication.agent.domain.repository.DeletionRequestRepository
import javax.inject.Inject

/**
 * One "continuous monitoring / re-verification" cycle: sends anything
 * pending, and advances in-flight requests. Returns requests that
 * newly reached COMPLETED in this call, for notifying the user.
 */
class ProcessDeletionRequestsUseCase @Inject constructor(
    private val repository: DeletionRequestRepository
) {
    suspend operator fun invoke(): List<DeletionRequest> = repository.processAndAdvance()
}
