package com.piieradication.agent.presentation.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

private enum class HomeTab(val label: String) {
    RECORDS("Records"),
    DASHBOARD("Dashboard"),
    PROFILE("Profile")
}

/**
 * Top-level scaffold: a bottom navigation bar switching between the
 * three real feature areas of the app. Each tab keeps its own
 * Hilt-scoped ViewModel via hiltViewModel() inside its own composable.
 */
@Composable
fun HomeScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = HomeTab.entries.toList()

    Scaffold(
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
        androidx.compose.foundation.layout.Box(modifier = Modifier.padding(padding)) {
            when (tabs[selectedTab]) {
                HomeTab.RECORDS -> PiiListScreen()
                HomeTab.DASHBOARD -> DashboardScreen()
                HomeTab.PROFILE -> ProfileScreen()
            }
        }
    }
}

private fun iconFor(tab: HomeTab) = when (tab) {
    HomeTab.RECORDS -> Icons.Default.List
    HomeTab.DASHBOARD -> Icons.Default.Dashboard
    HomeTab.PROFILE -> Icons.Default.Person
}
