package com.piieradication.agent.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.piieradication.agent.domain.model.AppEvent
import com.piieradication.agent.domain.model.EventType
import com.piieradication.agent.presentation.NotificationsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationsScreen(onBack: () -> Unit, viewModel: NotificationsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications & Alerts") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.events.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No alerts yet. Activity from syncs and deletion requests shows up here.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.events, key = { it.id }) { event -> EventCard(event) }
            }
        }
    }
}

@Composable
private fun EventCard(event: AppEvent) {
    val formatter = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    val (icon, tint) = iconFor(event.type)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = CircleShape, color = tint.copy(alpha = 0.15f)) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.padding(8.dp)
                )
            }
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(event.message, fontWeight = FontWeight.Medium)
                Text(
                    formatter.format(Date(event.timestampEpochMillis)),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private fun iconFor(type: EventType): Pair<ImageVector, androidx.compose.ui.graphics.Color> = when (type) {
    EventType.SYNC_COMPLETED -> Icons.Default.Sync to androidx.compose.ui.graphics.Color(0xFF1976D2)
    EventType.BROKER_DETECTED -> Icons.Default.Search to androidx.compose.ui.graphics.Color(0xFF7B1FA2)
    EventType.REQUEST_SENT -> Icons.Default.Send to androidx.compose.ui.graphics.Color(0xFF00796B)
    EventType.REQUEST_COMPLETED -> Icons.Default.CheckCircle to androidx.compose.ui.graphics.Color(0xFF388E3C)
    EventType.REQUEST_FAILED -> Icons.Default.Error to androidx.compose.ui.graphics.Color(0xFFD32F2F)
}
