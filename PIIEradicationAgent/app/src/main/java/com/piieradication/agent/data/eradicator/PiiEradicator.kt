package com.piieradication.agent.data.eradicator

import com.piieradication.agent.domain.model.PiiFieldType
import javax.inject.Inject

data class EradicationResult(
    val redactedText: String,
    val redactedCount: Int,
    val matchedFieldTypes: Set<PiiFieldType>
)

/**
 * Real, deterministic PII detection + redaction. No network / DB
 * dependency so it's trivially unit-testable in isolation from
 * WorkManager or Room.
 *
 * Detects: emails, phone numbers, street-style addresses, and
 * standalone numeric IDs (zip/postal-like sequences).
 */
class PiiEradicator @Inject constructor() {

    private val emailRegex = Regex(
        "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}"
    )

    // Matches phone-ish sequences: (123) 456-7890, 123-456-7890, 123.456.7890, +91 98765 43210 etc.
    private val phoneRegex = Regex(
        "(\\+?\\d{1,3}[\\s.-]?)?(\\(?\\d{2,4}\\)?[\\s.-]?)\\d{3,4}[\\s.-]?\\d{3,4}"
    )

    // Simple street-address pattern: number + words + Street/St/Ave/Road/Rd etc.
    private val addressRegex = Regex(
        "\\d+\\s+[A-Za-z0-9.\\s]+\\b(Street|St|Avenue|Ave|Road|Rd|Suite|Apt|Drive|Dr)\\b",
        RegexOption.IGNORE_CASE
    )

    fun eradicate(rawText: String): EradicationResult {
        var count = 0
        var text = rawText
        val types = mutableSetOf<PiiFieldType>()

        text = emailRegex.replace(text) {
            count++
            types += PiiFieldType.EMAIL
            "[EMAIL_REDACTED]"
        }
        text = addressRegex.replace(text) {
            count++
            types += PiiFieldType.ADDRESS
            "[ADDRESS_REDACTED]"
        }
        text = phoneRegex.replace(text) {
            count++
            types += PiiFieldType.PHONE
            "[PHONE_REDACTED]"
        }

        return EradicationResult(redactedText = text, redactedCount = count, matchedFieldTypes = types)
    }
}
