package com.piieradication.agent.domain.usecase

import com.piieradication.agent.domain.repository.EventLogRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveUnreadEventCountUseCase @Inject constructor(
    private val repository: EventLogRepository
) {
    operator fun invoke(): Flow<Int> = repository.observeUnreadCount()
}
