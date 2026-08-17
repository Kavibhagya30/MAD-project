package com.piieradication.agent.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piieradication.agent.domain.model.UserProfile
import com.piieradication.agent.domain.usecase.ObserveUserProfileUseCase
import com.piieradication.agent.domain.usecase.SaveUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Backs the "your identity" screen, persisted only through
 * [UserProfileSecureStore]'s Keystore-encrypted preferences — never
 * sent over the network, never logged, never written in plaintext.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val observeUserProfileUseCase: ObserveUserProfileUseCase,
    private val saveUserProfileUseCase: SaveUserProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeUserProfileUseCase().collect { profile ->
                _uiState.value = _uiState.value.copy(
                    fullName = profile.fullName,
                    email = profile.email,
                    phone = profile.phone
                )
            }
        }
    }

    fun onFullNameChange(value: String) {
        _uiState.value = _uiState.value.copy(fullName = value, justSaved = false)
    }

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, justSaved = false)
    }

    fun onPhoneChange(value: String) {
        _uiState.value = _uiState.value.copy(phone = value, justSaved = false)
    }

    fun save() {
        val state = _uiState.value
        viewModelScope.launch {
            saveUserProfileUseCase(
                UserProfile(fullName = state.fullName, email = state.email, phone = state.phone)
            )
            _uiState.value = _uiState.value.copy(justSaved = true)
        }
    }
}
