package com.piieradication.agent.data.repository

import com.piieradication.agent.data.secure.UserProfileSecureStore
import com.piieradication.agent.domain.model.UserProfile
import com.piieradication.agent.domain.repository.UserProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserProfileRepositoryImpl @Inject constructor(
    private val secureStore: UserProfileSecureStore
) : UserProfileRepository {

    override fun observeProfile(): Flow<UserProfile> = secureStore.observe().flowOn(Dispatchers.IO)

    override suspend fun saveProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        secureStore.write(profile)
    }
}
