package com.piieradication.agent.presentation

import com.piieradication.agent.domain.model.DeletionRequest
import com.piieradication.agent.domain.model.DeletionRequestStatus

data class RequestsUiState(
    val allRequests: List<DeletionRequest> = emptyList(),
    val selectedFilter: DeletionRequestStatus? = null
) {
    val visibleRequests: List<DeletionRequest>
        get() = if (selectedFilter == null) allRequests else allRequests.filter { it.status == selectedFilter }
}
