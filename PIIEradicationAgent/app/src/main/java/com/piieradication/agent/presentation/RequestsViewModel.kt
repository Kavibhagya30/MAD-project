package com.piieradication.agent.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piieradication.agent.domain.model.DeletionRequestStatus
import com.piieradication.agent.domain.usecase.ObserveDeletionRequestsUseCase
import com.piieradication.agent.domain.usecase.ProcessDeletionRequestsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RequestsViewModel @Inject constructor(
    observeDeletionRequestsUseCase: ObserveDeletionRequestsUseCase,
    private val processDeletionRequestsUseCase: ProcessDeletionRequestsUseCase
) : ViewModel() {

    private val selectedFilter = MutableStateFlow<DeletionRequestStatus?>(null)

    val uiState: StateFlow<RequestsUiState> = combine(
        observeDeletionRequestsUseCase(),
        selectedFilter
    ) { requests, filter ->
        RequestsUiState(allRequests = requests, selectedFilter = filter)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RequestsUiState()
    )

    fun onFilterSelected(status: DeletionRequestStatus?) {
        selectedFilter.value = status
    }

    /** Manual "resend / re-verify now" action — re-runs the same pipeline the background worker uses. */
    fun retryNow() {
        viewModelScope.launch { processDeletionRequestsUseCase() }
    }
}
