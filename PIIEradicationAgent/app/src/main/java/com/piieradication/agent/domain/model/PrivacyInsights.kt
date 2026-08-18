package com.piieradication.agent.domain.model

/** Aggregated stats the dashboard shows — computed from records + deletion requests. */
data class PrivacyInsights(
    val recordsSynced: Int = 0,
    val fieldsRedacted: Int = 0,
    val brokersDetected: Int = 0,
    val requestsPending: Int = 0,
    val requestsSent: Int = 0,
    val requestsAcknowledged: Int = 0,
    val requestsCompleted: Int = 0,
    val requestsFailed: Int = 0,
    val privacyScore: Int = 100,
    val overallRiskLevel: RiskLevel = RiskLevel.LOW
) {
    val totalRequests: Int
        get() = requestsPending + requestsSent + requestsAcknowledged + requestsCompleted + requestsFailed
}
