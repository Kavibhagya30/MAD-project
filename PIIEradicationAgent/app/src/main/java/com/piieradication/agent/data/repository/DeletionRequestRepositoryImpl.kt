package com.piieradication.agent.data.repository

import android.util.Log
import com.piieradication.agent.data.local.DeletionRequestDao
import com.piieradication.agent.data.local.DeletionRequestEntity
import com.piieradication.agent.data.remote.DeletionRequestApi
import com.piieradication.agent.data.remote.dto.DeletionRequestDto
import com.piieradication.agent.domain.model.DeletionRequest
import com.piieradication.agent.domain.model.DeletionRequestStatus
import com.piieradication.agent.domain.model.PiiRecord
import com.piieradication.agent.domain.registry.DataBrokerRegistry
import com.piieradication.agent.domain.registry.DeletionRequestStatusAdvancer
import com.piieradication.agent.domain.repository.DeletionRequestRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import javax.inject.Inject

class DeletionRequestRepositoryImpl @Inject constructor(
    private val dao: DeletionRequestDao,
    private val api: DeletionRequestApi
) : DeletionRequestRepository {

    override fun observeAll(): Flow<List<DeletionRequest>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun generateForRecord(record: PiiRecord): Int = withContext(Dispatchers.IO) {
        val brokers = DataBrokerRegistry.detect(record.detectedFieldTypes)
        var created = 0
        val now = System.currentTimeMillis()
        for (broker in brokers) {
            if (dao.countForRecordAndBroker(record.sourceId, broker.id) > 0) continue
            dao.insert(
                DeletionRequestEntity(
                    piiRecordSourceId = record.sourceId,
                    recordDisplayName = record.displayName,
                    brokerId = broker.id,
                    brokerName = broker.displayName,
                    status = DeletionRequestStatus.PENDING.name,
                    attempts = 0,
                    createdAtEpochMillis = now,
                    updatedAtEpochMillis = now
                )
            )
            created++
        }
        created
    }

    override suspend fun processAndAdvance(): List<DeletionRequest> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val newlyCompleted = mutableListOf<DeletionRequest>()

        // 1. Send everything still PENDING (or previously FAILED, up to a retry cap).
        val toSend = dao.getByStatuses(
            listOf(DeletionRequestStatus.PENDING.name, DeletionRequestStatus.FAILED.name)
        ).filter { it.attempts < MAX_ATTEMPTS }

        for (entity in toSend) {
            val updated = try {
                val subjectRef = hashSubject(entity.piiRecordSourceId, entity.recordDisplayName, entity.brokerId)
                val response = api.submitDeletionRequest(
                    DeletionRequestDto(broker = entity.brokerName, subjectRef = subjectRef)
                )
                entity.copy(
                    status = DeletionRequestStatus.SENT.name,
                    attempts = entity.attempts + 1,
                    updatedAtEpochMillis = now,
                    createdAtEpochMillis = entity.createdAtEpochMillis,
                    lastResponseSnippet = response.origin?.let { "sent from $it" }
                )
            } catch (e: Exception) {
                Log.w(TAG, "Deletion request to ${entity.brokerName} failed: ${e.message}")
                entity.copy(
                    status = DeletionRequestStatus.FAILED.name,
                    attempts = entity.attempts + 1,
                    updatedAtEpochMillis = now,
                    lastResponseSnippet = e.message?.take(120)
                )
            }
            dao.update(updated)
        }

        // 2. Re-verify everything in flight and advance it based on elapsed time.
        val inFlight = dao.getByStatuses(
            listOf(DeletionRequestStatus.SENT.name, DeletionRequestStatus.ACKNOWLEDGED.name)
        )
        for (entity in inFlight) {
            val currentStatus = DeletionRequestStatus.valueOf(entity.status)
            val elapsed = now - entity.createdAtEpochMillis
            val nextStatus = DeletionRequestStatusAdvancer.advance(currentStatus, elapsed)
            if (nextStatus != currentStatus) {
                val updated = entity.copy(status = nextStatus.name, updatedAtEpochMillis = now)
                dao.update(updated)
                if (nextStatus == DeletionRequestStatus.COMPLETED) {
                    newlyCompleted += updated.toDomain()
                }
            }
        }

        newlyCompleted
    }

    private fun hashSubject(sourceId: Int, displayName: String, brokerId: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest("$sourceId|$displayName|$brokerId".toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }.take(16)
    }

    companion object {
        private const val TAG = "DeletionRequestRepo"
        private const val MAX_ATTEMPTS = 5
    }
}

private fun DeletionRequestEntity.toDomain() = DeletionRequest(
    id = id,
    piiRecordSourceId = piiRecordSourceId,
    recordDisplayName = recordDisplayName,
    brokerId = brokerId,
    brokerName = brokerName,
    status = DeletionRequestStatus.valueOf(status),
    attempts = attempts,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis,
    lastResponseSnippet = lastResponseSnippet
)
