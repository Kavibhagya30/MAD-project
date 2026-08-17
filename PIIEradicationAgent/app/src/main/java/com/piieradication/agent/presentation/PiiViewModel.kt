package com.piieradication.agent.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.piieradication.agent.domain.usecase.ObservePiiRecordsUseCase
import com.piieradication.agent.domain.usecase.SchedulePiiSyncUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PiiViewModel @Inject constructor(
    observePiiRecordsUseCase: ObservePiiRecordsUseCase,
    private val schedulePiiSyncUseCase: SchedulePiiSyncUseCase,
    private val workManager: WorkManager
) : ViewModel() {

    private val manualSyncError = MutableStateFlow<String?>(null)

    // Real, live status of the WorkManager one-off job — not simulated.
    private val manualWorkInfo = workManager
        .getWorkInfosForUniqueWorkFlow(SchedulePiiSyncUseCase.ONE_OFF_WORK_NAME)

    val uiState: StateFlow<PiiUiState> = combine(
        observePiiRecordsUseCase(),
        manualWorkInfo,
        manualSyncError
    ) { records, workInfos, error ->
        val isRunning = workInfos.any {
            it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED
        }
        PiiUiState(
            records = records,
            isSyncing = isRunning,
            lastError = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PiiUiState()
    )

    fun syncNow() {
        manualSyncError.value = null
        viewModelScope.launch {
            schedulePiiSyncUseCase.triggerOneOffNow()
        }
    }
}
