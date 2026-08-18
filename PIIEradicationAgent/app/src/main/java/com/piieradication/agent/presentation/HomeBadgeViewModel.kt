package com.piieradication.agent.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piieradication.agent.domain.usecase.ObserveUnreadEventCountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeBadgeViewModel @Inject constructor(
    observeUnreadEventCountUseCase: ObserveUnreadEventCountUseCase
) : ViewModel() {
    val unreadCount: StateFlow<Int> = observeUnreadEventCountUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
}
