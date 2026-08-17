package com.piieradication.agent.domain.usecase

import com.piieradication.agent.domain.model.PiiRecord
import com.piieradication.agent.domain.repository.PiiRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservePiiRecordsUseCase @Inject constructor(
    private val repository: PiiRepository
) {
    operator fun invoke(): Flow<List<PiiRecord>> = repository.observeRecords()
}
