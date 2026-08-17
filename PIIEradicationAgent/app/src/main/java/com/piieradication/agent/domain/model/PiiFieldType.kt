package com.piieradication.agent.domain.model

/**
 * The category of PII a single redaction matched. Persisted on each
 * [PiiRecord] (as a comma-separated name list) so downstream features —
 * data-broker detection in particular — can reason about *what kind* of
 * exposure a record represents without re-parsing the redacted text.
 */
enum class PiiFieldType {
    EMAIL,
    PHONE,
    ADDRESS;

    companion object {
        fun serialize(types: Set<PiiFieldType>): String = types.joinToString(",") { it.name }

        fun deserialize(raw: String): Set<PiiFieldType> =
            if (raw.isBlank()) emptySet()
            else raw.split(",").mapNotNull { token ->
                runCatching { valueOf(token.trim()) }.getOrNull()
            }.toSet()
    }
}
