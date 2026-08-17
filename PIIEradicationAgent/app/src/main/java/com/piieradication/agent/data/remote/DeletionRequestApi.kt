package com.piieradication.agent.data.remote

import com.piieradication.agent.data.remote.dto.DeletionRequestDto
import com.piieradication.agent.data.remote.dto.DeletionResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Real network endpoint used to demonstrate the "send a deletion
 * request" flow end-to-end. httpbin.org/post is a public echo service —
 * it accepts and echoes back whatever JSON body is posted, which is
 * enough to prove a genuine HTTPS round trip without needing a live
 * broker's private API. Swap [com.piieradication.agent.di.DeletionNetworkModule.BASE_URL]
 * for a real broker/opt-out API when one is available; nothing else in
 * the deletion pipeline needs to change (same pattern as `UserApi`).
 */
interface DeletionRequestApi {
    @POST("post")
    suspend fun submitDeletionRequest(@Body body: DeletionRequestDto): DeletionResponseDto
}
