package com.piieradication.agent.data.repository

import com.piieradication.agent.data.local.AppEventDao
import com.piieradication.agent.data.local.AppEventEntity
import com.piieradication.agent.domain.model.AppEvent
import com.piieradication.agent.domain.model.EventType
import com.piieradication.agent.domain.repository.EventLogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EventLogRepositoryImpl @Inject constructor(
    private val dao: AppEventDao
) : EventLogRepository {

    override fun observeAll(): Flow<List<AppEvent>> =
        dao.observeAll().map { list ->
            list.map {
                AppEvent(
                    id = it.id,
                    type = EventType.valueOf(it.type),
                    message = it.message,
                    timestampEpochMillis = it.timestampEpochMillis,
                    read = it.read
                )
            }
        }

    override fun observeUnreadCount(): Flow<Int> = dao.observeUnreadCount()

    override suspend fun log(type: EventType, message: String) = withContext(Dispatchers.IO) {
        dao.insert(
            AppEventEntity(
                type = type.name,
                message = message,
                timestampEpochMillis = System.currentTimeMillis()
            )
        )
        Unit
    }

    override suspend fun markAllRead() = withContext(Dispatchers.IO) {
        dao.markAllRead()
    }
}
