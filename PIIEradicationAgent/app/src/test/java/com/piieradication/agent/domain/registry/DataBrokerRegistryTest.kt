package com.piieradication.agent.domain.registry

import com.piieradication.agent.domain.model.PiiFieldType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DataBrokerRegistryTest {

    @Test
    fun `no field types means no brokers detected`() {
        assertTrue(DataBrokerRegistry.detect(emptySet()).isEmpty())
    }

    @Test
    fun `email only detects brokers that collect email`() {
        val detected = DataBrokerRegistry.detect(setOf(PiiFieldType.EMAIL))
        assertTrue(detected.isNotEmpty())
        assertTrue(detected.all { PiiFieldType.EMAIL in it.collects })
    }

    @Test
    fun `record exposing all three field types matches the most brokers`() {
        val emailOnly = DataBrokerRegistry.detect(setOf(PiiFieldType.EMAIL))
        val allThree = DataBrokerRegistry.detect(
            setOf(PiiFieldType.EMAIL, PiiFieldType.PHONE, PiiFieldType.ADDRESS)
        )
        assertTrue(allThree.size >= emailOnly.size)
        assertEquals(DataBrokerRegistry.all.size, allThree.size)
    }

    @Test
    fun `broker ids are unique`() {
        val ids = DataBrokerRegistry.all.map { it.id }
        assertEquals(ids.distinct().size, ids.size)
    }
}
