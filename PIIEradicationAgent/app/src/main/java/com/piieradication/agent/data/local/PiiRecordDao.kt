package com.piieradication.agent.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PiiRecordDao {

    @Query("SELECT * FROM pii_records ORDER BY syncedAtEpochMillis DESC")
    fun observeAll(): Flow<List<PiiRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<PiiRecordEntity>)

    @Query("DELETE FROM pii_records")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM pii_records")
    suspend fun count(): Int
}
