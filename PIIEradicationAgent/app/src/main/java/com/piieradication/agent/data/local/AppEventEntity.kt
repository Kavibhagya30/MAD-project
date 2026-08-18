package com.piieradication.agent.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_events")
data class AppEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val type: String,
    val message: String,
    val timestampEpochMillis: Long,
    val read: Boolean = false
)
