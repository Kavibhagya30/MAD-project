package com.piieradication.agent.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppEventDao {

    @Query("SELECT * FROM app_events ORDER BY timestampEpochMillis DESC")
    fun observeAll(): Flow<List<AppEventEntity>>

    @Query("SELECT COUNT(*) FROM app_events WHERE read = 0")
    fun observeUnreadCount(): Flow<Int>

    @Insert
    suspend fun insert(entity: AppEventEntity): Long

    @Query("UPDATE app_events SET read = 1 WHERE read = 0")
    suspend fun markAllRead()

    @Query("DELETE FROM app_events")
    suspend fun clearAll()
}
