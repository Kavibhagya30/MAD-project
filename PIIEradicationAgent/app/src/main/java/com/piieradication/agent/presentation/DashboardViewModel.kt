package com.piieradication.agent.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piieradication.agent.domain.model.DeletionRequestStatus
import com.piieradication.agent.domain.model.PrivacyInsights
import com.piieradication.agent.domain.model.RiskLevel
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
        val detectedBrokers = records.flatMap { DataBrokerRegistry.detect(it.detectedFieldTypes) }
        val distinctBrokerIds = detectedBrokers.map { it.id }.distinct()

        val requestsPending = requests.count { it.status == DeletionRequestStatus.PENDING }
        val requestsSent = requests.count { it.status == DeletionRequestStatus.SENT }
        val requestsAcknowledged = requests.count { it.status == DeletionRequestStatus.ACKNOWLEDGED }
        val requestsCompleted = requests.count { it.status == DeletionRequestStatus.COMPLETED }
        val requestsFailed = requests.count { it.status == DeletionRequestStatus.FAILED }
        val total = requestsPending + requestsSent + requestsAcknowledged + requestsCompleted + requestsFailed

        // Privacy score: share of detected exposures that have actually been
        // resolved (COMPLETED). No exposures detected yet = perfect score.
        val privacyScore = if (total == 0) 100 else ((requestsCompleted * 100f) / total).toInt()

        // Overall risk = the worst risk level among brokers that still have
        // an outstanding (non-COMPLETED) request against them.
        val brokerIdsById = DataBrokerRegistry.all.associateBy { it.id }
        val outstandingBrokerIds = requests
            .filter { it.status != DeletionRequestStatus.COMPLETED }
            .map { it.brokerId }
            .distinct()
        val overallRisk = outstandingBrokerIds
            .mapNotNull { brokerIdsById[it]?.riskLevel }
            .maxByOrNull { it.ordinal } ?: RiskLevel.LOW

        val insights = PrivacyInsights(
            recordsSynced = records.size,
            fieldsRedacted = records.sumOf { it.fieldsRedactedCount },
            brokersDetected = distinctBrokerIds.size,
            requestsPending = requestsPending,
            requestsSent = requestsSent,
            requestsAcknowledged = requestsAcknowledged,
            requestsCompleted = requestsCompleted,
            requestsFailed = requestsFailed,
            privacyScore = privacyScore,
            overallRiskLevel = overallRisk
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
