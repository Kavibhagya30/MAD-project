package com.piieradication.agent.presentation

import com.piieradication.agent.domain.model.DeletionRequest
import com.piieradication.agent.domain.model.PrivacyInsights

data class DashboardUiState(
    val insights: PrivacyInsights = PrivacyInsights(),
    val recentRequests: List<DeletionRequest> = emptyList()
)
