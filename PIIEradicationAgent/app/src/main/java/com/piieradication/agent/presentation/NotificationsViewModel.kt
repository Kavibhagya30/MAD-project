package com.piieradication.agent.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piieradication.agent.domain.usecase.MarkAllEventsReadUseCase
import com.piieradication.agent.domain.usecase.ObserveEventLogUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    observeEventLogUseCase: ObserveEventLogUseCase,
    private val markAllEventsReadUseCase: MarkAllEventsReadUseCase
) : ViewModel() {

    val uiState: StateFlow<NotificationsUiState> = observeEventLogUseCase()
        .map { NotificationsUiState(events = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NotificationsUiState()
        )

    init {
        viewModelScope.launch { markAllEventsReadUseCase() }
    }
}
