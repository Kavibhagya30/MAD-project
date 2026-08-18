package com.piieradication.agent.domain.repository

import com.piieradication.agent.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    fun observeProfile(): Flow<UserProfile>
    suspend fun saveProfile(profile: UserProfile)
    suspend fun clearProfile()
}
