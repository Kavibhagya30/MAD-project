package com.piieradication.agent.data.secure

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.piieradication.agent.domain.model.UserProfile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stores the app owner's own identity fields using an
 * [EncryptedSharedPreferences] file whose key is generated and held in
 * the Android Keystore (via [MasterKey]) — the raw AES key never
 * exists outside secure hardware/OS storage, and the values on disk
 * are ciphertext. This is the real, working stand-in for the
 * "secure storage (Android Keystore)" objective from the project brief.
 */
@Singleton
class UserProfileSecureStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "user_profile_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun read(): UserProfile = UserProfile(
        fullName = prefs.getString(KEY_NAME, "") ?: "",
        email = prefs.getString(KEY_EMAIL, "") ?: "",
        phone = prefs.getString(KEY_PHONE, "") ?: ""
    )

    fun write(profile: UserProfile) {
        prefs.edit()
            .putString(KEY_NAME, profile.fullName)
            .putString(KEY_EMAIL, profile.email)
            .putString(KEY_PHONE, profile.phone)
            .apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    /** Live updates whenever the encrypted profile changes, for reactive UI. */
    fun observe(): Flow<UserProfile> = callbackFlow {
        trySend(read())
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            trySend(read())
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }.distinctUntilChanged()

    private companion object {
        const val KEY_NAME = "full_name"
        const val KEY_EMAIL = "email"
        const val KEY_PHONE = "phone"
    }
}
