package com.piieradication.agent.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.piieradication.agent.presentation.RootViewModel

/**
 * Mirrors the "User Registration / Login" gate from the architecture
 * diagram: shows [AuthGateScreen] until a profile has been saved to
 * the encrypted store, then [HomeScreen]. Logging out (Settings tab)
 * clears the profile, which flips this back to the gate automatically.
 */
@Composable
fun AppRoot(viewModel: RootViewModel = hiltViewModel()) {
    val isRegistered by viewModel.isRegistered.collectAsStateWithLifecycle()
    // Once a user has completed the gate in this process, keep them on
    // Home even if a transient recomposition reads a stale profile —
    // only an explicit logout (which recreates this state) sends them back.
    var manuallyContinued by remember { mutableStateOf(false) }

    when (isRegistered) {
        null -> Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
        false -> if (manuallyContinued) {
            HomeScreen(onLoggedOut = { manuallyContinued = false })
        } else {
            AuthGateScreen(onContinue = { manuallyContinued = true })
        }
        true -> HomeScreen(onLoggedOut = { manuallyContinued = false })
    }
}
