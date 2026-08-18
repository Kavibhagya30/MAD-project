package com.piieradication.agent.domain.usecase

import com.piieradication.agent.domain.repository.EventLogRepository
import javax.inject.Inject

class MarkAllEventsReadUseCase @Inject constructor(
    private val repository: EventLogRepository
) {
    suspend operator fun invoke() = repository.markAllRead()
}
