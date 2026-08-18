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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.piieradication.agent.domain.model.DeletionRequest
import com.piieradication.agent.domain.model.DeletionRequestStatus
import com.piieradication.agent.presentation.RequestsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RequestsScreen(viewModel: RequestsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Response Tracking") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::retryNow) {
                Icon(Icons.Default.Refresh, contentDescription = "Resend / re-verify now")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.selectedFilter == null,
                        onClick = { viewModel.onFilterSelected(null) },
                        label = { Text("All") }
                    )
                }
                items(DeletionRequestStatus.entries.toList()) { status ->
                    FilterChip(
                        selected = uiState.selectedFilter == status,
                        onClick = { viewModel.onFilterSelected(status) },
                        label = { Text(statusLabel(status)) }
                    )
                }
            }

            if (uiState.visibleRequests.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No requests in this state.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.visibleRequests, key = { it.id }) { request -> RequestCard(request) }
                }
            }
        }
    }
}

@Composable
private fun RequestCard(request: DeletionRequest) {
    val formatter = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${request.recordDisplayName} → ${request.brokerName}", fontWeight = FontWeight.Bold)
                Text(statusLabel(request.status), fontWeight = FontWeight.Bold)
            }
            Text(
                "Attempt ${request.attempts} · updated ${formatter.format(Date(request.updatedAtEpochMillis))}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
            request.lastResponseSnippet?.let {
                Text(
                    "Last response: $it",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
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
