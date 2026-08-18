package com.piieradication.agent.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PiiRecordEntity::class, DeletionRequestEntity::class, AppEventEntity::class],
    version = 3,
    exportSchema = false
)
abstract class PiiDatabase : RoomDatabase() {
    abstract fun piiRecordDao(): PiiRecordDao
    abstract fun deletionRequestDao(): DeletionRequestDao
    abstract fun appEventDao(): AppEventDao
}
