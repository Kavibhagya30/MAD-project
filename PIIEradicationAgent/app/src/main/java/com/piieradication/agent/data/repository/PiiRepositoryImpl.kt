package com.piieradication.agent.data.repository

import com.piieradication.agent.data.eradicator.PiiEradicator
import com.piieradication.agent.data.local.PiiRecordDao
import com.piieradication.agent.data.local.PiiRecordEntity
import com.piieradication.agent.data.remote.UserApi
import com.piieradication.agent.domain.model.EventType
import com.piieradication.agent.domain.model.PiiFieldType
import com.piieradication.agent.domain.model.PiiRecord
import com.piieradication.agent.domain.repository.EventLogRepository
import com.piieradication.agent.domain.repository.PiiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PiiRepositoryImpl @Inject constructor(
    private val api: UserApi,
    private val dao: PiiRecordDao,
    private val eradicator: PiiEradicator,
    private val eventLog: EventLogRepository
) : PiiRepository {

    override fun observeRecords(): Flow<List<PiiRecord>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun syncAndEradicate(): List<PiiRecord> = withContext(Dispatchers.IO) {
        // 1. Real network call.
        val users = api.getUsers()

        val now = System.currentTimeMillis()

        // 2. Build a raw "bio" blob per user containing the PII-bearing
        //    fields, then run it through the real eradicator.
        val entities = users.map { user ->
            val rawBio = buildString {
                append("Email: ").append(user.email).append(". ")
                append("Phone: ").append(user.phone).append(". ")
                user.address?.let {
                    append("Address: ").append(it.suite).append(' ')
                        .append(it.street).append(", ").append(it.city)
                        .append(' ').append(it.zipcode).append('.')
                }
            }

            val result = eradicator.eradicate(rawBio)

            PiiRecordEntity(
                sourceId = user.id,
                displayName = user.name,
                redactedBio = result.redactedText,
                fieldsRedactedCount = result.redactedCount,
                syncedAtEpochMillis = now,
                detectedFieldTypes = PiiFieldType.serialize(result.matchedFieldTypes)
            )
        }

        // 3. Persist only the redacted result — raw PII never touches disk.
        dao.insertAll(entities)

        eventLog.log(
            EventType.SYNC_COMPLETED,
            "Synced ${entities.size} record(s), redacted ${entities.sumOf { it.fieldsRedactedCount }} field(s)."
        )

        entities.map { it.toDomain() }
    }

    override suspend fun clearAll() = withContext(Dispatchers.IO) {
        dao.clearAll()
    }
}

private fun PiiRecordEntity.toDomain() = PiiRecord(
    id = id,
    sourceId = sourceId,
    displayName = displayName,
    redactedBio = redactedBio,
    fieldsRedactedCount = fieldsRedactedCount,
    syncedAtEpochMillis = syncedAtEpochMillis,
    detectedFieldTypes = PiiFieldType.deserialize(detectedFieldTypes)
)
