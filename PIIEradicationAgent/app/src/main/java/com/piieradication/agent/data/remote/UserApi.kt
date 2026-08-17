package com.piieradication.agent.data.remote

import com.piieradication.agent.data.remote.dto.UserDto
import retrofit2.http.GET

/**
 * Real network endpoint. JSONPlaceholder's /users response naturally
 * contains PII-shaped fields (email, phone, street address), which
 * makes it a realistic source to demonstrate eradication against.
 */
interface UserApi {
    @GET("users")
    suspend fun getUsers(): List<UserDto>
}
