package com.piieradication.agent.domain.usecase

import com.piieradication.agent.domain.model.UserProfile
import com.piieradication.agent.domain.repository.UserProfileRepository
import javax.inject.Inject

class SaveUserProfileUseCase @Inject constructor(
    private val repository: UserProfileRepository
) {
    suspend operator fun invoke(profile: UserProfile) = repository.saveProfile(profile)
}
