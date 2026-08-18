package com.piieradication.agent.presentation

import com.piieradication.agent.domain.model.DataBroker
import com.piieradication.agent.domain.model.DeletionRequestStatus

data class BrokerRow(
    val broker: DataBroker,
    val matchedRecordCount: Int,
    val worstOutstandingStatus: DeletionRequestStatus?
)

data class BrokersUiState(
    val rows: List<BrokerRow> = emptyList()
)
