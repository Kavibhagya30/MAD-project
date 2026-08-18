package com.piieradication.agent.domain.usecase

import com.piieradication.agent.domain.repository.UserProfileRepository
import javax.inject.Inject

class ClearUserProfileUseCase @Inject constructor(
    private val repository: UserProfileRepository
) {
    suspend operator fun invoke() = repository.clearProfile()
}
