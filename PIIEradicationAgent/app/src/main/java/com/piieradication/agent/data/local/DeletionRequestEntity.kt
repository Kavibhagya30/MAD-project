package com.piieradication.agent.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deletion_requests")
data class DeletionRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val piiRecordSourceId: Int,
    val recordDisplayName: String,
    val brokerId: String,
    val brokerName: String,
    val status: String,
    val attempts: Int,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val lastResponseSnippet: String? = null
)
