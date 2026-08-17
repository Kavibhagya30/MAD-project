package com.piieradication.agent.domain.model

/**
 * A known data-broker / people-search site that is known to index a
 * given category of PII. [collects] drives detection: a broker is
 * "found" against a [PiiRecord] when the record's detected field types
 * intersect the broker's known collection categories.
 */
data class DataBroker(
    val id: String,
    val displayName: String,
    val category: String,
    val collects: Set<PiiFieldType>
)
