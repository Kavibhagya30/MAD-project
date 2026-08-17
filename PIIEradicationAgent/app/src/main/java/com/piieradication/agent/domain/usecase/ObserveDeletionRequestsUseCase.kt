package com.piieradication.agent.domain.usecase

import com.piieradication.agent.domain.model.DeletionRequest
import com.piieradication.agent.domain.repository.DeletionRequestRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveDeletionRequestsUseCase @Inject constructor(
    private val repository: DeletionRequestRepository
) {
    operator fun invoke(): Flow<List<DeletionRequest>> = repository.observeAll()
}
