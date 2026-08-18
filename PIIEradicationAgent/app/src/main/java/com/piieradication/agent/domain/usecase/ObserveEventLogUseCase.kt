package com.piieradication.agent.domain.usecase

import com.piieradication.agent.domain.model.AppEvent
import com.piieradication.agent.domain.repository.EventLogRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveEventLogUseCase @Inject constructor(
    private val repository: EventLogRepository
) {
    operator fun invoke(): Flow<List<AppEvent>> = repository.observeAll()
}
