package com.piieradication.agent.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piieradication.agent.domain.model.DeletionRequestStatus
import com.piieradication.agent.domain.model.PrivacyInsights
import com.piieradication.agent.domain.registry.DataBrokerRegistry
import com.piieradication.agent.domain.usecase.ObserveDeletionRequestsUseCase
import com.piieradication.agent.domain.usecase.ObservePiiRecordsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    observePiiRecordsUseCase: ObservePiiRecordsUseCase,
    observeDeletionRequestsUseCase: ObserveDeletionRequestsUseCase
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        observePiiRecordsUseCase(),
        observeDeletionRequestsUseCase()
    ) { records, requests ->
        val distinctBrokers = records
            .flatMap { DataBrokerRegistry.detect(it.detectedFieldTypes) }
            .map { it.id }
            .distinct()

        val insights = PrivacyInsights(
            recordsSynced = records.size,
            fieldsRedacted = records.sumOf { it.fieldsRedactedCount },
            brokersDetected = distinctBrokers.size,
            requestsPending = requests.count { it.status == DeletionRequestStatus.PENDING },
            requestsSent = requests.count { it.status == DeletionRequestStatus.SENT },
            requestsAcknowledged = requests.count { it.status == DeletionRequestStatus.ACKNOWLEDGED },
            requestsCompleted = requests.count { it.status == DeletionRequestStatus.COMPLETED },
            requestsFailed = requests.count { it.status == DeletionRequestStatus.FAILED }
        )

        DashboardUiState(
            insights = insights,
            recentRequests = requests.take(10)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState()
    )
}
