package com.piieradication.agent.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piieradication.agent.domain.usecase.ObserveUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** null = still loading from encrypted storage, true/false once known. */
@HiltViewModel
class RootViewModel @Inject constructor(
    observeUserProfileUseCase: ObserveUserProfileUseCase
) : ViewModel() {

    val isRegistered: StateFlow<Boolean?> = observeUserProfileUseCase()
        .map { it.isComplete }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
