package com.piieradication.agent.domain.model

/**
 * The app owner's own identity, kept only for locally targeting which
 * broker categories matter most to them. Never sent over the network —
 * stored exclusively in Keystore-backed [android.content.SharedPreferences]
 * (see `data/secure/UserProfileSecureStore.kt`).
 */
data class UserProfile(
    val fullName: String = "",
    val email: String = "",
    val phone: String = ""
) {
    val isComplete: Boolean get() = fullName.isNotBlank() && email.isNotBlank()
}
