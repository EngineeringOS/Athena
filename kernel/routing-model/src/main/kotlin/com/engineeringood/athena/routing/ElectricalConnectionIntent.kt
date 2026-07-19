package com.engineeringood.athena.routing

import com.engineeringood.athena.connection.SemanticPortDirection
import com.engineeringood.athena.connection.SemanticSignalFamilyId
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.LayoutSourceSpan

/** Electrical-aware interpretation of a source semantic connection before route solving. */
data class ElectricalConnectionIntent(
    val connectionId: ElectricalConnectionId,
    val sourceSubjectId: StableSemanticIdentity,
    val sourcePortId: ElectricalPortId,
    val sourcePortSemanticId: StableSemanticIdentity? = null,
    val targetSubjectId: StableSemanticIdentity,
    val targetPortId: ElectricalPortId,
    val targetPortSemanticId: StableSemanticIdentity? = null,
    val role: ElectricalConnectionRole,
    val signalClass: ElectricalSignalClass = ElectricalSignalClass.UNKNOWN,
    val sourceSpan: LayoutSourceSpan? = null,
    val quality: ElectricalConnectionIntentQuality = ElectricalConnectionIntentQuality.satisfied(),
)

/** Schematic routing role for one electrical connection. */
enum class ElectricalConnectionRole {
    POWER_FEED,
    GROUND_REFERENCE,
    CONTROL_SIGNAL,
    TERMINAL_TRANSITION,
    LOAD_CONNECTION,
    UNKNOWN,
}

/** Signal class used by routing policy without binding policy to a renderer. */
enum class ElectricalSignalClass {
    POWER,
    GROUND,
    DIGITAL_INPUT,
    DIGITAL_OUTPUT,
    ANALOG_SIGNAL,
    CONTROL,
    UNKNOWN,
}

/** Endpoint category used for modest M24 routing-intent classification. */
enum class ElectricalConnectionEndpointKind {
    DEVICE,
    TERMINAL,
    LOAD,
}

/** Quality state for electrical connection intent classification. */
enum class ElectricalConnectionIntentQualityState {
    SATISFIED,
    DEGRADED,
}

/** Classification quality carried with intent so unsupported cases do not crash route derivation. */
data class ElectricalConnectionIntentQuality(
    val state: ElectricalConnectionIntentQualityState,
    val message: String? = null,
) {
    val isDegraded: Boolean
        get() = state == ElectricalConnectionIntentQualityState.DEGRADED

    init {
        require(message == null || message.isNotBlank()) {
            "Electrical connection intent quality message must be null or non-blank."
        }
    }

    companion object {
        fun satisfied(): ElectricalConnectionIntentQuality =
            ElectricalConnectionIntentQuality(ElectricalConnectionIntentQualityState.SATISFIED)

        fun degraded(message: String): ElectricalConnectionIntentQuality =
            ElectricalConnectionIntentQuality(ElectricalConnectionIntentQualityState.DEGRADED, message)
    }
}

/** Canonical endpoint facts consumed by electrical connection intent classification. */
data class ElectricalConnectionPortRef(
    val subjectId: StableSemanticIdentity,
    val portSemanticId: StableSemanticIdentity,
    val portId: ElectricalPortId,
    val endpointKind: ElectricalConnectionEndpointKind = ElectricalConnectionEndpointKind.DEVICE,
    val direction: SemanticPortDirection,
    val signalFamilyId: SemanticSignalFamilyId,
)

/** Classifier input derived from one semantic `connect` fact and resolved port facts. */
data class ElectricalConnectionIntentInput(
    val connectionId: ElectricalConnectionId,
    val sourcePort: ElectricalConnectionPortRef,
    val targetPort: ElectricalConnectionPortRef,
    val sourceSpan: LayoutSourceSpan? = null,
)

/** Deterministic M24 classifier from semantic connection facts to electrical routing intent. */
class ElectricalConnectionIntentClassifier {
    fun classify(input: ElectricalConnectionIntentInput): ElectricalConnectionIntent {
        val signalClass = classifySignal(input.sourcePort, input.targetPort)
        val role = classifyRole(input, signalClass)
        val quality = if (role == ElectricalConnectionRole.UNKNOWN || signalClass == ElectricalSignalClass.UNKNOWN) {
            ElectricalConnectionIntentQuality.degraded(
                "Unsupported electrical connection intent for `${input.connectionId.value}`.",
            )
        } else {
            ElectricalConnectionIntentQuality.satisfied()
        }

        return ElectricalConnectionIntent(
            connectionId = input.connectionId,
            sourceSubjectId = input.sourcePort.subjectId,
            sourcePortId = input.sourcePort.portId,
            sourcePortSemanticId = input.sourcePort.portSemanticId,
            targetSubjectId = input.targetPort.subjectId,
            targetPortId = input.targetPort.portId,
            targetPortSemanticId = input.targetPort.portSemanticId,
            role = role,
            signalClass = signalClass,
            sourceSpan = input.sourceSpan,
            quality = quality,
        )
    }

    private fun classifyRole(
        input: ElectricalConnectionIntentInput,
        signalClass: ElectricalSignalClass,
    ): ElectricalConnectionRole {
        if (input.sourcePort.endpointKind == ElectricalConnectionEndpointKind.TERMINAL ||
            input.targetPort.endpointKind == ElectricalConnectionEndpointKind.TERMINAL
        ) {
            return ElectricalConnectionRole.TERMINAL_TRANSITION
        }
        if (input.targetPort.endpointKind == ElectricalConnectionEndpointKind.LOAD) {
            return ElectricalConnectionRole.LOAD_CONNECTION
        }
        return when (signalClass) {
            ElectricalSignalClass.POWER -> ElectricalConnectionRole.POWER_FEED
            ElectricalSignalClass.GROUND -> ElectricalConnectionRole.GROUND_REFERENCE
            ElectricalSignalClass.DIGITAL_INPUT,
            ElectricalSignalClass.DIGITAL_OUTPUT,
            ElectricalSignalClass.ANALOG_SIGNAL,
            ElectricalSignalClass.CONTROL,
            -> if (isControlDirection(input.sourcePort.direction, input.targetPort.direction)) {
                ElectricalConnectionRole.CONTROL_SIGNAL
            } else {
                ElectricalConnectionRole.UNKNOWN
            }
            ElectricalSignalClass.UNKNOWN -> ElectricalConnectionRole.UNKNOWN
        }
    }

    private fun classifySignal(
        source: ElectricalConnectionPortRef,
        target: ElectricalConnectionPortRef,
    ): ElectricalSignalClass {
        val sourceSignal = source.signalFamilyId.value.normalizedSignal()
        val targetSignal = target.signalFamilyId.value.normalizedSignal()
        val candidate = sourceSignal.takeIf(String::isNotBlank) ?: targetSignal
        return when (candidate) {
            "power", "24v", "l+", "mains", "supply", "dc-power" -> ElectricalSignalClass.POWER
            "ground", "0v", "m", "pe" -> ElectricalSignalClass.GROUND
            "digital", "digital-output" -> if (source.direction == SemanticPortDirection.OUTPUT) {
                ElectricalSignalClass.DIGITAL_OUTPUT
            } else {
                ElectricalSignalClass.DIGITAL_INPUT
            }
            "digital-input" -> ElectricalSignalClass.DIGITAL_INPUT
            "analog", "analog-signal" -> ElectricalSignalClass.ANALOG_SIGNAL
            "control" -> ElectricalSignalClass.CONTROL
            else -> ElectricalSignalClass.UNKNOWN
        }
    }

    private fun isControlDirection(
        sourceDirection: SemanticPortDirection,
        targetDirection: SemanticPortDirection,
    ): Boolean {
        return sourceDirection in setOf(SemanticPortDirection.OUTPUT, SemanticPortDirection.BIDIRECTIONAL) &&
            targetDirection in setOf(
                SemanticPortDirection.INPUT,
                SemanticPortDirection.BIDIRECTIONAL,
                SemanticPortDirection.PASSIVE,
            )
    }

    private fun String.normalizedSignal(): String = trim().lowercase()
}
