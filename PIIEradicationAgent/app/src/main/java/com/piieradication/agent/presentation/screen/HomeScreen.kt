package com.piieradication.agent.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.piieradication.agent.presentation.HomeBadgeViewModel

private enum class HomeTab(val label: String) {
    RECORDS("Records"),
    BROKERS("Brokers"),
    REQUESTS("Requests"),
    DASHBOARD("Dashboard"),
    PROFILE("Profile")
}

/**
 * Top-level scaffold: a bottom navigation bar switching between the five
 * real feature areas of the app, plus a top bar with a badged
 * notifications bell and a settings shortcut. Each tab / overlay keeps
 * its own Hilt-scoped ViewModel via hiltViewModel().
 */
@Composable
fun HomeScreen(onLoggedOut: () -> Unit, badgeViewModel: HomeBadgeViewModel = hiltViewModel()) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var overlay by remember { mutableStateOf<Overlay?>(null) }
    val tabs = HomeTab.entries.toList()
    val unreadCount by badgeViewModel.unreadCount.collectAsStateWithLifecycle()

    when (overlay) {
        Overlay.NOTIFICATIONS -> {
            NotificationsScreen(onBack = { overlay = null })
            return
        }
        Overlay.SETTINGS -> {
            SettingsScreen(onLoggedOut = {
                overlay = null
                onLoggedOut()
            })
            return
        }
        null -> Unit
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                actions = {
                    IconButton(onClick = { overlay = Overlay.NOTIFICATIONS }) {
                        BadgedBox(badge = {
                            if (unreadCount > 0) Badge { Text(unreadCount.coerceAtMost(99).toString()) }
                        }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                        }
                    }
                    IconButton(onClick = { overlay = Overlay.SETTINGS }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(iconFor(tab), contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (tabs[selectedTab]) {
                HomeTab.RECORDS -> PiiListScreen()
                HomeTab.BROKERS -> BrokersScreen()
                HomeTab.REQUESTS -> RequestsScreen()
                HomeTab.DASHBOARD -> DashboardScreen()
                HomeTab.PROFILE -> ProfileScreen()
            }
        }
    }
}

private enum class Overlay { NOTIFICATIONS, SETTINGS }

private fun iconFor(tab: HomeTab) = when (tab) {
    HomeTab.RECORDS -> Icons.Default.List
    HomeTab.BROKERS -> Icons.Default.Security
    HomeTab.REQUESTS -> Icons.Default.Send
    HomeTab.DASHBOARD -> Icons.Default.Dashboard
    HomeTab.PROFILE -> Icons.Default.Person
}
