package com.piieradication.agent.domain.model

enum class EventType { SYNC_COMPLETED, BROKER_DETECTED, REQUEST_SENT, REQUEST_COMPLETED, REQUEST_FAILED }

/** A single row in the "Notifications & Alerts" log (block-diagram module 8). */
data class AppEvent(
    val id: Long = 0L,
    val type: EventType,
    val message: String,
    val timestampEpochMillis: Long,
    val read: Boolean
)
