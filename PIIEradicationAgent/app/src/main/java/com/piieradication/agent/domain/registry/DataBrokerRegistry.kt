package com.piieradication.agent.domain.registry

import com.piieradication.agent.domain.model.DataBroker
import com.piieradication.agent.domain.model.PiiFieldType
import com.piieradication.agent.domain.model.PiiFieldType.ADDRESS
import com.piieradication.agent.domain.model.PiiFieldType.EMAIL
import com.piieradication.agent.domain.model.PiiFieldType.PHONE
import com.piieradication.agent.domain.model.RiskLevel.HIGH
import com.piieradication.agent.domain.model.RiskLevel.LOW
import com.piieradication.agent.domain.model.RiskLevel.MEDIUM

/**
 * Deterministic, offline stand-in for a real broker-scanning service.
 * Real people-search / data-broker sites publicly disclose (in their own
 * privacy policies and opt-out pages) which categories of contact info
 * they index, which is exactly what [DataBroker.collects] encodes here.
 *
 * Detection is a pure set-intersection: a broker is "found" against a
 * record when the record's [PiiFieldType]s overlap what that broker is
 * known to collect. Swapping this for a real scanning API later only
 * means replacing [detect]'s body — nothing downstream changes, same
 * pattern the network layer already documents in the README.
 */
object DataBrokerRegistry {

    val all: List<DataBroker> = listOf(
        DataBroker("spokeo", "Spokeo", "People search", setOf(EMAIL, PHONE, ADDRESS), HIGH),
        DataBroker("whitepages", "Whitepages", "People search", setOf(PHONE, ADDRESS), MEDIUM),
        DataBroker("beenverified", "BeenVerified", "Background reports", setOf(EMAIL, PHONE, ADDRESS), HIGH),
        DataBroker("mylife", "MyLife", "Reputation profiles", setOf(EMAIL, ADDRESS), MEDIUM),
        DataBroker("radaris", "Radaris", "People search", setOf(PHONE, ADDRESS), MEDIUM),
        DataBroker("peoplefinders", "PeopleFinders", "People search", setOf(EMAIL, PHONE, ADDRESS), HIGH),
        DataBroker("intelius", "Intelius", "Background reports", setOf(EMAIL, PHONE), MEDIUM),
        DataBroker("truthfinder", "TruthFinder", "Background reports", setOf(PHONE, ADDRESS), MEDIUM)
    )

    fun detect(fieldTypes: Set<PiiFieldType>): List<DataBroker> =
        if (fieldTypes.isEmpty()) emptyList()
        else all.filter { broker -> broker.collects.any { it in fieldTypes } }
}
