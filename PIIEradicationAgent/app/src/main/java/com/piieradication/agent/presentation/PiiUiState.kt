package com.piieradication.agent.presentation

import com.piieradication.agent.domain.model.PiiRecord

data class PiiUiState(
    val records: List<PiiRecord> = emptyList(),
    val isSyncing: Boolean = false,
    val lastError: String? = null
)
