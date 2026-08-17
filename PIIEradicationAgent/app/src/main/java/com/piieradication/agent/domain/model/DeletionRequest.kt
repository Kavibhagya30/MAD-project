package com.piieradication.agent.domain.model

data class DeletionRequest(
    val id: Long = 0L,
    val piiRecordSourceId: Int,
    val recordDisplayName: String,
    val brokerId: String,
    val brokerName: String,
    val status: DeletionRequestStatus,
    val attempts: Int,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val lastResponseSnippet: String?
)
