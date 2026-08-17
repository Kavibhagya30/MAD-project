package com.piieradication.agent.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.piieradication.agent.domain.model.PiiRecord
import com.piieradication.agent.domain.registry.DataBrokerRegistry
import com.piieradication.agent.presentation.PiiViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PiiListScreen(viewModel: PiiViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("PII Eradication Agent") })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(if (uiState.isSyncing) "Syncing…" else "Sync now") },
                icon = {
                    if (uiState.isSyncing) {
                        CircularProgressIndicator(modifier = Modifier.padding(2.dp))
                    } else {
                        Icon(Icons.Default.Sync, contentDescription = null)
                    }
                },
                onClick = { viewModel.syncNow() }
            )
        }
    ) { padding ->
        if (uiState.records.isEmpty()) {
            EmptyState(padding)
        } else {
            RecordList(records = uiState.records, padding = padding)
        }
    }
}

@Composable
private fun EmptyState(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Text("No synced records yet. Tap \"Sync now\" to fetch and eradicate PII.")
    }
}

@Composable
private fun RecordList(records: List<PiiRecord>, padding: PaddingValues) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(records, key = { it.id }) { record ->
            RecordCard(record)
        }
    }
}

@Composable
private fun RecordCard(record: PiiRecord) {
    val formatter = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    val matchedBrokers = remember(record.detectedFieldTypes) {
        DataBrokerRegistry.detect(record.detectedFieldTypes)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(record.displayName, fontWeight = FontWeight.Bold)
            Text(record.redactedBio, modifier = Modifier.padding(top = 4.dp))
            Text(
                "Redacted ${record.fieldsRedactedCount} field(s) · synced ${formatter.format(Date(record.syncedAtEpochMillis))}",
                modifier = Modifier.padding(top = 8.dp)
            )
            if (matchedBrokers.isNotEmpty()) {
                Text(
                    "Brokers likely exposing this: ${matchedBrokers.joinToString { it.displayName }}",
                    modifier = Modifier.padding(top = 8.dp),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
