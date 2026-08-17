package com.piieradication.agent.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DeletionRequestDao {

    @Query("SELECT * FROM deletion_requests ORDER BY updatedAtEpochMillis DESC")
    fun observeAll(): Flow<List<DeletionRequestEntity>>

    @Query("SELECT * FROM deletion_requests WHERE status = :status")
    suspend fun getByStatus(status: String): List<DeletionRequestEntity>

    @Query("SELECT * FROM deletion_requests WHERE status IN (:statuses)")
    suspend fun getByStatuses(statuses: List<String>): List<DeletionRequestEntity>

    @Query(
        "SELECT COUNT(*) FROM deletion_requests " +
            "WHERE piiRecordSourceId = :sourceId AND brokerId = :brokerId"
    )
    suspend fun countForRecordAndBroker(sourceId: Int, brokerId: String): Int

    @Insert
    suspend fun insert(entity: DeletionRequestEntity): Long

    @Update
    suspend fun update(entity: DeletionRequestEntity)

    @Query("DELETE FROM deletion_requests")
    suspend fun clearAll()
}
