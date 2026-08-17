package com.piieradication.agent.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pii_records")
data class PiiRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val sourceId: Int,
    val displayName: String,
    val redactedBio: String,
    val fieldsRedactedCount: Int,
    val syncedAtEpochMillis: Long,
    /** Comma-separated [com.piieradication.agent.domain.model.PiiFieldType] names. */
    val detectedFieldTypes: String = ""
)
