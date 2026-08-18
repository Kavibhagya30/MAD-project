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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.piieradication.agent.domain.model.DeletionRequestStatus
import com.piieradication.agent.domain.model.RiskLevel
import com.piieradication.agent.presentation.BrokerRow
import com.piieradication.agent.presentation.BrokersViewModel

@Composable
fun BrokersScreen(viewModel: BrokersViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Data Brokers") }) }
    ) { padding ->
        if (uiState.rows.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No brokers detected yet. Sync from the Records tab first.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "Platforms likely holding your data, ranked by risk.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                items(uiState.rows, key = { it.broker.id }) { row -> BrokerCard(row) }
            }
        }
    }
}

@Composable
private fun BrokerCard(row: BrokerRow) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(row.broker.displayName, fontWeight = FontWeight.Bold)
                RiskBadge(row.broker.riskLevel)
            }
            Text(row.broker.category, style = MaterialTheme.typography.bodySmall)
            Text(
                "Collects: ${row.broker.collects.joinToString { it.name.lowercase() }}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                "Matched ${row.matchedRecordCount} record(s)" +
                    (row.worstOutstandingStatus?.let { " · ${statusLabelFor(it)}" } ?: ""),
                modifier = Modifier.padding(top = 8.dp),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun RiskBadge(riskLevel: RiskLevel) {
    val color = when (riskLevel) {
        RiskLevel.HIGH -> MaterialTheme.colorScheme.error
        RiskLevel.MEDIUM -> MaterialTheme.colorScheme.tertiary
        RiskLevel.LOW -> MaterialTheme.colorScheme.primary
    }
    Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
        Text(
            riskLevel.name,
            color = color,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

private fun statusLabelFor(status: DeletionRequestStatus): String = when (status) {
    DeletionRequestStatus.PENDING -> "Pending"
    DeletionRequestStatus.SENT -> "Sent"
    DeletionRequestStatus.ACKNOWLEDGED -> "Acknowledged"
    DeletionRequestStatus.COMPLETED -> "Completed"
    DeletionRequestStatus.FAILED -> "Failed"
}
