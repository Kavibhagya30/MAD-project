package com.piieradication.agent.domain.model

/**
 * A single synced record after PII eradication has been applied.
 * [originalPreview] is intentionally short/never persisted in full to avoid
 * storing raw PII at rest.
 */
data class PiiRecord(
    val id: Long = 0L,
    val sourceId: Int,
    val displayName: String,
    val redactedBio: String,
    val fieldsRedactedCount: Int,
    val syncedAtEpochMillis: Long,
    val detectedFieldTypes: Set<PiiFieldType> = emptySet()
)
