package com.piieradication.agent.data.remote.dto

/**
 * Outbound body for a deletion request. Deliberately carries no raw PII —
 * only the broker name and a one-way hash identifying the exposed
 * record — so that even the act of *requesting deletion* doesn't leak
 * the data we're trying to erase.
 */
data class DeletionRequestDto(
    val broker: String,
    val subjectRef: String,
    val requestType: String = "ERASE_MY_DATA"
)

/**
 * httpbin.org/post echoes the request back inside "json", plus an
 * "origin" field — enough to prove a real round trip happened without
 * needing a bespoke broker backend.
 */
data class DeletionResponseDto(
    val origin: String? = null,
    val url: String? = null
)
