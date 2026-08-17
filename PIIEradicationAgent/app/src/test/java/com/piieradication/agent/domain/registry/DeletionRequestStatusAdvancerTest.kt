package com.piieradication.agent.domain.registry

import com.piieradication.agent.domain.model.DeletionRequestStatus.ACKNOWLEDGED
import com.piieradication.agent.domain.model.DeletionRequestStatus.COMPLETED
import com.piieradication.agent.domain.model.DeletionRequestStatus.FAILED
import com.piieradication.agent.domain.model.DeletionRequestStatus.PENDING
import com.piieradication.agent.domain.model.DeletionRequestStatus.SENT
import org.junit.Assert.assertEquals
import org.junit.Test

class DeletionRequestStatusAdvancerTest {

    @Test
    fun `newly sent request stays sent before the acknowledge window`() {
        val result = DeletionRequestStatusAdvancer.advance(SENT, elapsedSinceCreatedMillis = 1_000)
        assertEquals(SENT, result)
    }

    @Test
    fun `sent request becomes acknowledged after the acknowledge window`() {
        val result = DeletionRequestStatusAdvancer.advance(
            SENT,
            elapsedSinceCreatedMillis = DeletionRequestStatusAdvancer.ACKNOWLEDGE_AFTER_MILLIS + 1
        )
        assertEquals(ACKNOWLEDGED, result)
    }

    @Test
    fun `acknowledged request becomes completed after the complete window`() {
        val result = DeletionRequestStatusAdvancer.advance(
            ACKNOWLEDGED,
            elapsedSinceCreatedMillis = DeletionRequestStatusAdvancer.COMPLETE_AFTER_MILLIS + 1
        )
        assertEquals(COMPLETED, result)
    }

    @Test
    fun `sent request can jump straight to completed after enough elapsed time`() {
        val result = DeletionRequestStatusAdvancer.advance(
            SENT,
            elapsedSinceCreatedMillis = DeletionRequestStatusAdvancer.COMPLETE_AFTER_MILLIS + 1
        )
        assertEquals(COMPLETED, result)
    }

    @Test
    fun `pending, failed and completed are left untouched`() {
        assertEquals(PENDING, DeletionRequestStatusAdvancer.advance(PENDING, Long.MAX_VALUE))
        assertEquals(FAILED, DeletionRequestStatusAdvancer.advance(FAILED, Long.MAX_VALUE))
        assertEquals(COMPLETED, DeletionRequestStatusAdvancer.advance(COMPLETED, Long.MAX_VALUE))
    }
}
