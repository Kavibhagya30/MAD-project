package com.piieradication.agent.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PiiRecordEntity::class, DeletionRequestEntity::class],
    version = 2,
    exportSchema = false
)
abstract class PiiDatabase : RoomDatabase() {
    abstract fun piiRecordDao(): PiiRecordDao
    abstract fun deletionRequestDao(): DeletionRequestDao
}
