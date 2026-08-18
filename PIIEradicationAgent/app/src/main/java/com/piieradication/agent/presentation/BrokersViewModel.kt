package com.piieradication.agent.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piieradication.agent.domain.model.DeletionRequestStatus
import com.piieradication.agent.domain.registry.DataBrokerRegistry
import com.piieradication.agent.domain.usecase.ObserveDeletionRequestsUseCase
import com.piieradication.agent.domain.usecase.ObservePiiRecordsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

private val statusSeverity = listOf(
    DeletionRequestStatus.COMPLETED,
    DeletionRequestStatus.ACKNOWLEDGED,
    DeletionRequestStatus.SENT,
    DeletionRequestStatus.PENDING,
    DeletionRequestStatus.FAILED
)

@HiltViewModel
class BrokersViewModel @Inject constructor(
    observePiiRecordsUseCase: ObservePiiRecordsUseCase,
    observeDeletionRequestsUseCase: ObserveDeletionRequestsUseCase
) : ViewModel() {

    val uiState: StateFlow<BrokersUiState> = combine(
        observePiiRecordsUseCase(),
        observeDeletionRequestsUseCase()
    ) { records, requests ->
        val matchesByBroker = records
            .flatMap { record -> DataBrokerRegistry.detect(record.detectedFieldTypes).map { it.id } }
            .groupingBy { it }
            .eachCount()

        val rows = DataBrokerRegistry.all
            .filter { matchesByBroker.containsKey(it.id) }
            .map { broker ->
                val statusesForBroker = requests.filter { it.brokerId == broker.id }.map { it.status }
                val worst = statusSeverity.lastOrNull { it in statusesForBroker }
                BrokerRow(
                    broker = broker,
                    matchedRecordCount = matchesByBroker[broker.id] ?: 0,
                    worstOutstandingStatus = worst
                )
            }
            .sortedByDescending { it.broker.riskLevel.ordinal }

        BrokersUiState(rows = rows)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BrokersUiState()
    )
}
