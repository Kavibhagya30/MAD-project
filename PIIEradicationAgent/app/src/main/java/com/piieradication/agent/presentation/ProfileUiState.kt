package com.piieradication.agent.presentation

data class ProfileUiState(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val justSaved: Boolean = false
)
