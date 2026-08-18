package com.piieradication.agent.presentation

import com.piieradication.agent.domain.model.AppEvent

data class NotificationsUiState(
    val events: List<AppEvent> = emptyList()
)
