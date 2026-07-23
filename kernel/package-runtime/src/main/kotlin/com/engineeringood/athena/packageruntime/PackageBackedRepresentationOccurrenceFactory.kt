package com.engineeringood.athena.packageruntime

import com.engineeringood.athena.packageplatform.RepresentationAnchorDefinition
import com.engineeringood.athena.packageplatform.RepresentationAnchorId
import com.engineeringood.athena.packageplatform.RepresentationAnchorSide
import com.engineeringood.athena.packageplatform.RepresentationDescriptor
import com.engineeringood.athena.packageplatform.RepresentationLabelSlotRole
import com.engineeringood.athena.representation.CompositionIntentMembershipId
import com.engineeringood.athena.representation.GridUnit
import com.engineeringood.athena.representation.LabelValue
import com.engineeringood.athena.representation.PresentationAnatomy
import com.engineeringood.athena.representation.PresentationBounds
import com.engineeringood.athena.representation.PresentationHotspot
import com.engineeringood.athena.representation.PresentationLabelAnchor
import com.engineeringood.athena.representation.PresentationLabelAnchorId
import com.engineeringood.athena.representation.PresentationLabelRole
import com.engineeringood.athena.representation.PresentationPoint
import com.engineeringood.athena.representation.PresentationPrimitive
import com.engineeringood.athena.representation.PresentationPrimitiveId
import com.engineeringood.athena.representation.PresentationSide
import com.engineeringood.athena.representation.PresentationSize
import com.engineeringood.athena.representation.PresentationTerminalId
import com.engineeringood.athena.representation.PresentationTerminalPoint
import com.engineeringood.athena.representation.RepresentationBindingCompiler
import com.engineeringood.athena.representation.RepresentationBindingRequest
import com.engineeringood.athena.representation.RepresentationDefinition
import com.engineeringood.athena.representation.RepresentationDiagnostic
import com.engineeringood.athena.representation.RepresentationDiagnosticCode
import com.engineeringood.athena.representation.RepresentationFallbackBehavior
import com.engineeringood.athena.representation.RepresentationId
import com.engineeringood.athena.representation.RepresentationLabelSlot
import com.engineeringood.athena.representation.RepresentationLibraryId
import com.engineeringood.athena.representation.RepresentationLifecycle
import com.engineeringood.athena.representation.RepresentationLifecycleState
import com.engineeringood.athena.representation.RepresentationOccurrenceRole
import com.engineeringood.athena.representation.RepresentationPolicy
import com.engineeringood.athena.representation.RepresentationPolicyId
import com.engineeringood.athena.representation.RepresentationPolicyPriority
import com.engineeringood.athena.representation.RepresentationProvenance
import com.engineeringood.athena.representation.RepresentationSubjectId
import com.engineeringood.athena.representation.RepresentationSymbolId
import com.engineeringood.athena.representation.RepresentationSymbolKind
import com.engineeringood.athena.representation.RepresentationVariantId
import com.engineeringood.athena.representation.SemanticPortId
import com.engineeringood.athena.representation.SymbolFamilyId
import com.engineeringood.athena.representation.TerminalMarker
import com.engineeringood.athena.representation.TerminalNotation
import com.engineeringood.athena.representation.TerminalNumber
import com.engineeringood.athena.representation.TerminalPresentationRole

class PackageBackedRepresentationOccurrenceFactory(
    private val compiler: RepresentationBindingCompiler = RepresentationBindingCompiler(),
) {
    fun create(request: PackageBackedRepresentationOccurrenceRequest): PackageBackedRepresentationOccurrenceResult {
        val diagnostics = request.preflightDiagnostics()
        if (diagnostics.isNotEmpty()) {
            return PackageBackedRepresentationOccurrenceResult(
                occurrence = null,
                diagnostics = diagnostics.sortedRepresentationDiagnostics(),
                rendererFallbackAccepted = false,
            )
        }

        val evidence = request.bindingEvidence
        val descriptor = requireNotNull(request.descriptor)
        val symbolId = RepresentationSymbolId(requireNotNull(evidence.descriptorId))
        val definition = descriptor.toRepresentationDefinition(
            symbolId = symbolId,
            libraryId = RepresentationLibraryId(requireNotNull(evidence.representationPackageId)),
            version = evidence.representationPackageVersion ?: "0.0.0",
            occurrenceRole = request.occurrenceRole,
        )
        val policy = RepresentationPolicy(
            policyId = RepresentationPolicyId("package-policy:${evidence.semanticSubjectId}"),
            projectionKind = request.projectionKind,
            standardProfile = null,
            subjectKind = request.subjectKind,
            semanticRole = request.semanticRole,
            occurrenceRole = request.occurrenceRole,
            symbolFamilyId = SymbolFamilyId(symbolId.value),
            symbolId = symbolId,
            variant = evidence.variant?.let(::RepresentationVariantId),
            fallback = RepresentationFallbackBehavior.DIAGNOSTIC_ONLY,
            priority = RepresentationPolicyPriority(100),
        )
        val binding = compiler.bind(
            RepresentationBindingRequest(
                canonicalSemanticId = RepresentationSubjectId(evidence.semanticSubjectId),
                projectionOccurrenceId = request.projectionOccurrenceId,
                subjectKind = request.subjectKind,
                semanticRole = request.semanticRole,
                projectionKind = request.projectionKind,
                policy = policy,
                definition = definition,
                labelValues = evidence.labelBindingSummary.toLabelValues(),
                terminalPorts = evidence.anchorMapSummary.toTerminalPorts(),
                priority = RepresentationPolicyPriority(100),
                compositionIntentMembership = listOf(CompositionIntentMembershipId("package:${evidence.resolverStage}")),
            ),
        )
        return PackageBackedRepresentationOccurrenceResult(
            occurrence = binding.occurrenceOrNull,
            diagnostics = binding.diagnostics,
            rendererFallbackAccepted = false,
        )
    }

    private fun PackageBackedRepresentationOccurrenceRequest.preflightDiagnostics(): List<RepresentationDiagnostic> {
        val evidence = bindingEvidence
        val diagnostics = mutableListOf<RepresentationDiagnostic>()
        if (evidence.descriptorId.isNullOrBlank() || evidence.representationPackageId.isNullOrBlank() || descriptor == null) {
            diagnostics += diagnostic(
                RepresentationDiagnosticCode.SYMBOL_MISSING,
                "Package-backed occurrence requires resolved representation package and descriptor evidence.",
                evidence.semanticSubjectId,
            )
        }
        val descriptorAnchors = descriptor?.anchors.orEmpty().map { it.anchorId }.toSet()
        evidence.anchorMapSummary.parsePairs().forEach { (semanticPortId, anchorId) ->
            if (RepresentationAnchorId(anchorId) !in descriptorAnchors) {
                diagnostics += diagnostic(
                    RepresentationDiagnosticCode.ANCHOR_MISSING,
                    "Package-backed occurrence is missing descriptor anchor `$anchorId` for `$semanticPortId`.",
                    evidence.semanticSubjectId,
                )
            }
        }
        val descriptorLabels = descriptor?.labelSlots.orEmpty().map { it.slotId.value }.toSet()
        evidence.labelBindingSummary.parsePairs().forEach { (slotId, _) ->
            if (slotId !in descriptorLabels) {
                diagnostics += diagnostic(
                    RepresentationDiagnosticCode.LABEL_SLOT_MISSING,
                    "Package-backed occurrence is missing descriptor label slot `$slotId`.",
                    evidence.semanticSubjectId,
                )
            }
        }
        return diagnostics
    }

    private fun RepresentationDescriptor.toRepresentationDefinition(
        symbolId: RepresentationSymbolId,
        libraryId: RepresentationLibraryId,
        version: String,
        occurrenceRole: RepresentationOccurrenceRole,
    ): RepresentationDefinition = RepresentationDefinition(
        symbolId = symbolId,
        libraryId = libraryId,
        version = com.engineeringood.athena.representation.RepresentationVersion(version),
        lifecycle = RepresentationLifecycle(
            state = RepresentationLifecycleState.ACTIVE,
            provenance = RepresentationProvenance("package-runtime"),
        ),
        kind = occurrenceRole.toSymbolKind(),
        anatomy = PresentationAnatomy(
            representationId = RepresentationId(descriptorId.value),
            context = com.engineeringood.athena.representation.RepresentationContext.ELECTRICAL_SCHEMATIC,
            bounds = PresentationBounds(GridUnit(bounds.width.toInt()), GridUnit(bounds.height.toInt())),
            hotspot = PresentationHotspot(PresentationPoint(GridUnit(0), GridUnit(0))),
            primitives = listOf(
                PresentationPrimitive.Rectangle(
                    primitiveId = PresentationPrimitiveId("descriptor-bounds"),
                    origin = PresentationPoint(GridUnit(0), GridUnit(0)),
                    size = PresentationSize(GridUnit(bounds.width.toInt()), GridUnit(bounds.height.toInt())),
                ),
            ),
            terminals = anchors.map { it.toTerminalPoint() },
            labelAnchors = labelSlots.map { slot ->
                PresentationLabelAnchor(
                    anchorId = PresentationLabelAnchorId(slot.slotId.value),
                    role = slot.role.toPresentationRole(),
                    point = PresentationPoint(GridUnit(0), GridUnit(0)),
                )
            },
        ),
        labelSlots = labelSlots.map { slot ->
            RepresentationLabelSlot(
                slotId = com.engineeringood.athena.representation.RepresentationLabelSlotId(slot.slotId.value),
                role = slot.role.toPresentationRole(),
            )
        },
        variants = variants.map { RepresentationVariantId(it.value) },
    )

    private fun RepresentationAnchorDefinition.toTerminalPoint(): PresentationTerminalPoint =
        PresentationTerminalPoint(
            terminalId = PresentationTerminalId(anchorId.value),
            role = TerminalPresentationRole.POWER_INPUT,
            localPoint = PresentationPoint(GridUnit(x.toInt()), GridUnit(y.toInt())),
            side = side.toPresentationSide(),
            notation = TerminalNotation(TerminalMarker.CIRCLE, TerminalNumber(anchorId.value.uppercase())),
        )

    private fun RepresentationAnchorSide.toPresentationSide(): PresentationSide =
        when (this) {
            RepresentationAnchorSide.LEFT -> PresentationSide.LEFT
            RepresentationAnchorSide.RIGHT -> PresentationSide.RIGHT
            RepresentationAnchorSide.TOP -> PresentationSide.TOP
            RepresentationAnchorSide.BOTTOM -> PresentationSide.BOTTOM
            RepresentationAnchorSide.CENTER -> PresentationSide.LEFT
        }

    private fun RepresentationLabelSlotRole.toPresentationRole(): PresentationLabelRole =
        when (this) {
            RepresentationLabelSlotRole.DEVICE_TAG -> PresentationLabelRole.DEVICE_TAG
            RepresentationLabelSlotRole.MODEL -> PresentationLabelRole.COMPONENT_LABEL
            RepresentationLabelSlotRole.TERMINAL_NUMBER -> PresentationLabelRole.TERMINAL_LABEL
            RepresentationLabelSlotRole.REFERENCE -> PresentationLabelRole.ROUTE_LABEL
        }

    private fun RepresentationOccurrenceRole.toSymbolKind(): RepresentationSymbolKind =
        when (this) {
            RepresentationOccurrenceRole.SUPPLY_REFERENCE -> RepresentationSymbolKind.SUPPLY_REFERENCE
            RepresentationOccurrenceRole.TERMINAL -> RepresentationSymbolKind.TERMINAL
            RepresentationOccurrenceRole.SWITCH_CONTACT -> RepresentationSymbolKind.SWITCH_CONTACT
            RepresentationOccurrenceRole.COIL_ACTUATOR -> RepresentationSymbolKind.COIL_ACTUATOR
            RepresentationOccurrenceRole.LAMP_INDICATOR -> RepresentationSymbolKind.LAMP_INDICATOR
            RepresentationOccurrenceRole.MOTOR_LOAD,
            RepresentationOccurrenceRole.LOAD_SYMBOL,
                -> RepresentationSymbolKind.MOTOR_LOAD
            RepresentationOccurrenceRole.PROTECTIVE_DEVICE -> RepresentationSymbolKind.PROTECTIVE_DEVICE
            RepresentationOccurrenceRole.FOLIO_REFERENCE -> RepresentationSymbolKind.FOLIO_REFERENCE
            RepresentationOccurrenceRole.ROUTE,
            RepresentationOccurrenceRole.LABEL,
                -> RepresentationSymbolKind.FOLIO_REFERENCE
        }

    private fun List<String>.toLabelValues(): Map<com.engineeringood.athena.representation.RepresentationLabelSlotId, LabelValue> =
        parsePairs().mapKeys { (slotId, _) -> com.engineeringood.athena.representation.RepresentationLabelSlotId(slotId) }
            .mapValues { (_, value) -> LabelValue(value) }

    private fun List<String>.toTerminalPorts(): Map<PresentationTerminalId, SemanticPortId> =
        parsePairs()
            .map { (semanticPortId, anchorId) -> PresentationTerminalId(anchorId) to SemanticPortId(semanticPortId) }
            .toMap()

    private fun List<String>.parsePairs(): Map<String, String> =
        mapNotNull { summary ->
            val key = summary.substringBefore("=", missingDelimiterValue = "")
            val value = summary.substringAfter("=", missingDelimiterValue = "")
            if (key.isBlank() || value.isBlank()) null else key to value
        }.toMap()

    private fun diagnostic(
        code: RepresentationDiagnosticCode,
        message: String,
        semanticSubjectId: String,
    ): RepresentationDiagnostic = RepresentationDiagnostic(
        code = code,
        message = message,
        subjectId = RepresentationSubjectId(semanticSubjectId),
    )
}

private fun List<RepresentationDiagnostic>.sortedRepresentationDiagnostics(): List<RepresentationDiagnostic> =
    sortedWith(
        compareBy<RepresentationDiagnostic>(
            { diagnostic -> diagnostic.code.wireValue },
            { diagnostic -> diagnostic.subjectId?.value.orEmpty() },
            { diagnostic -> diagnostic.message },
        ),
    )
