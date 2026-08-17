package com.piieradication.agent.data.eradicator

import com.piieradication.agent.domain.model.PiiFieldType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PiiEradicatorTest {

    private val eradicator = PiiEradicator()

    @Test
    fun `redacts email addresses`() {
        val result = eradicator.eradicate("Contact me at jane.doe@example.com please.")
        assertFalse(result.redactedText.contains("jane.doe@example.com"))
        assertTrue(result.redactedText.contains("[EMAIL_REDACTED]"))
        assertEquals(1, result.redactedCount)
    }

    @Test
    fun `redacts phone numbers`() {
        val result = eradicator.eradicate("Call me on 987-654-3210 tomorrow.")
        assertFalse(result.redactedText.contains("987-654-3210"))
        assertTrue(result.redactedText.contains("[PHONE_REDACTED]"))
    }

    @Test
    fun `redacts street addresses`() {
        val result = eradicator.eradicate("I live at 742 Evergreen Street, nice place.")
        assertTrue(result.redactedText.contains("[ADDRESS_REDACTED]"))
    }

    @Test
    fun `counts multiple redactions across fields`() {
        val result = eradicator.eradicate(
            "Email: john@site.com. Phone: 123-456-7890. Address: 12 Main Road."
        )
        assertEquals(3, result.redactedCount)
    }

    @Test
    fun `leaves clean text untouched`() {
        val result = eradicator.eradicate("This sentence has no personal data at all.")
        assertEquals(0, result.redactedCount)
        assertTrue(result.matchedFieldTypes.isEmpty())
    }

    @Test
    fun `reports which field types were matched`() {
        val result = eradicator.eradicate(
            "Email: john@site.com. Phone: 123-456-7890. Address: 12 Main Road."
        )
        assertEquals(
            setOf(PiiFieldType.EMAIL, PiiFieldType.PHONE, PiiFieldType.ADDRESS),
            result.matchedFieldTypes
        )
    }
}
