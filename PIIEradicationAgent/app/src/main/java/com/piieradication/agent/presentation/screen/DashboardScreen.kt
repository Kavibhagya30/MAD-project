package com.piieradication.agent.presentation.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.piieradication.agent.domain.model.DeletionRequest
import com.piieradication.agent.domain.model.DeletionRequestStatus
import com.piieradication.agent.domain.model.PrivacyInsights
import com.piieradication.agent.presentation.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Privacy Dashboard") }) }
    ) { padding ->
        if (uiState.insights.recordsSynced == 0) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No data yet. Sync from the Records tab to populate your privacy insights.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { StatGrid(uiState.insights) }
                item { RequestStatusBar(uiState.insights) }
                item {
                    Text(
                        "Recent deletion requests",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                items(uiState.recentRequests, key = { it.id }) { request ->
                    DeletionRequestRow(request)
                }
            }
        }
    }
}

@Composable
private fun StatGrid(insights: PrivacyInsights) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Records synced", insights.recordsSynced.toString(), Modifier.weight(1f))
            StatCard("Fields redacted", insights.fieldsRedacted.toString(), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Brokers detected", insights.brokersDetected.toString(), Modifier.weight(1f))
            StatCard("Requests completed", insights.requestsCompleted.toString(), Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/** Simple proportional stacked bar — no external charting library required. */
@Composable
private fun RequestStatusBar(insights: PrivacyInsights) {
    val segments = listOf(
        insights.requestsPending to MaterialTheme.colorScheme.outline,
        insights.requestsSent to MaterialTheme.colorScheme.tertiary,
        insights.requestsAcknowledged to MaterialTheme.colorScheme.secondary,
        insights.requestsCompleted to MaterialTheme.colorScheme.primary,
        insights.requestsFailed to MaterialTheme.colorScheme.error
    )
    val total = insights.totalRequests.coerceAtLeast(1)

    Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Deletion request pipeline", fontWeight = FontWeight.Bold)
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(10.dp))
            ) {
                var startX = 0f
                segments.forEach { (count, color) ->
                    val segWidth = size.width * (count.toFloat() / total)
                    if (segWidth > 0f) {
                        drawRect(
                            color = color,
                            topLeft = Offset(startX, 0f),
                            size = Size(segWidth, size.height)
                        )
                        startX += segWidth
                    }
                }
            }
            Column(modifier = Modifier.padding(top = 12.dp)) {
                LegendRow("Pending", insights.requestsPending, MaterialTheme.colorScheme.outline)
                LegendRow("Sent", insights.requestsSent, MaterialTheme.colorScheme.tertiary)
                LegendRow("Acknowledged", insights.requestsAcknowledged, MaterialTheme.colorScheme.secondary)
                LegendRow("Completed", insights.requestsCompleted, MaterialTheme.colorScheme.primary)
                LegendRow("Failed", insights.requestsFailed, MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun LegendRow(label: String, count: Int, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(10.dp)
                .height(10.dp)
                .clip(RoundedCornerShape(2.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) { drawRect(color) }
        }
        Text(" $label: $count", modifier = Modifier.padding(start = 6.dp))
    }
}

@Composable
private fun DeletionRequestRow(request: DeletionRequest) {
    val formatter = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("${request.recordDisplayName} → ${request.brokerName}", fontWeight = FontWeight.Medium)
                Text(
                    "Updated ${formatter.format(Date(request.updatedAtEpochMillis))}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(statusLabel(request.status), fontWeight = FontWeight.Bold)
        }
    }
}

private fun statusLabel(status: DeletionRequestStatus): String = when (status) {
    DeletionRequestStatus.PENDING -> "Pending"
    DeletionRequestStatus.SENT -> "Sent"
    DeletionRequestStatus.ACKNOWLEDGED -> "Acknowledged"
    DeletionRequestStatus.COMPLETED -> "Completed"
    DeletionRequestStatus.FAILED -> "Failed"
}
