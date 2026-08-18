package com.piieradication.agent.domain.repository

import com.piieradication.agent.domain.model.AppEvent
import com.piieradication.agent.domain.model.EventType
import kotlinx.coroutines.flow.Flow

interface EventLogRepository {
    fun observeAll(): Flow<List<AppEvent>>
    fun observeUnreadCount(): Flow<Int>
    suspend fun log(type: EventType, message: String)
    suspend fun markAllRead()
}
