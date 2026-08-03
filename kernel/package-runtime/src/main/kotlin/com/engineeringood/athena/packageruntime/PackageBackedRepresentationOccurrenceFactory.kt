package com.engineeringood.athena.packageruntime

import com.engineeringood.athena.packageplatform.RepresentationAnchorDefinition
import com.engineeringood.athena.packageplatform.RepresentationAnchorId
import com.engineeringood.athena.packageplatform.RepresentationAnchorSide
import com.engineeringood.athena.packageplatform.RepresentationDescriptor
import com.engineeringood.athena.packageplatform.RepresentationLabelSlotRole
import com.engineeringood.athena.representation.GraphicBounds
import com.engineeringood.athena.representation.GraphicFill
import com.engineeringood.athena.representation.GraphicLineCap
import com.engineeringood.athena.representation.GraphicLineJoin
import com.engineeringood.athena.representation.GraphicPaintToken
import com.engineeringood.athena.representation.GraphicPoint
import com.engineeringood.athena.representation.GraphicPrimitive
import com.engineeringood.athena.representation.GraphicPrimitiveDocument
import com.engineeringood.athena.representation.GraphicPrimitiveDocumentId
import com.engineeringood.athena.representation.GraphicPrimitiveId
import com.engineeringood.athena.representation.GraphicStyleToken
import com.engineeringood.athena.representation.GraphicStyleTokenId
import com.engineeringood.athena.representation.CompositionIntentMembershipId
import com.engineeringood.athena.representation.LabelValue
import com.engineeringood.athena.representation.PresentationLabelRole
import com.engineeringood.athena.representation.PhysicalTerminalId
import com.engineeringood.athena.representation.RepresentationAnchorContract
import com.engineeringood.athena.representation.RepresentationAnchorRole
import com.engineeringood.athena.representation.RepresentationBindingCompiler
import com.engineeringood.athena.representation.RepresentationBindingRequest
import com.engineeringood.athena.representation.RepresentationDefinition
import com.engineeringood.athena.representation.RepresentationDiagnostic
import com.engineeringood.athena.representation.RepresentationDiagnosticCode
import com.engineeringood.athena.representation.RepresentationDirectionPredicate
import com.engineeringood.athena.representation.RepresentationLabelSlot
import com.engineeringood.athena.representation.RepresentationLabelSlotId
import com.engineeringood.athena.representation.RepresentationLibraryId
import com.engineeringood.athena.representation.RepresentationLifecycle
import com.engineeringood.athena.representation.RepresentationLifecycleState
import com.engineeringood.athena.representation.RepresentationOccurrenceRole
import com.engineeringood.athena.representation.RepresentationPortAnchorBinding
import com.engineeringood.athena.representation.RepresentationPortAnchorBindingId
import com.engineeringood.athena.representation.RepresentationPolicy
import com.engineeringood.athena.representation.RepresentationPolicyId
import com.engineeringood.athena.representation.RepresentationPolicyPriority
import com.engineeringood.athena.representation.RepresentationProjectPortFact
import com.engineeringood.athena.representation.RepresentationProvenance
import com.engineeringood.athena.representation.RepresentationSignalPredicate
import com.engineeringood.athena.representation.RepresentationSubjectId
import com.engineeringood.athena.representation.RepresentationSymbolId
import com.engineeringood.athena.representation.RepresentationSymbolKind
import com.engineeringood.athena.representation.RepresentationVariantId
import com.engineeringood.athena.representation.SemanticPortId
import com.engineeringood.athena.representation.SymbolFamilyId

class PackageBackedRepresentationOccurrenceFactory(
    private val compiler: RepresentationBindingCompiler = RepresentationBindingCompiler(),
) {
    fun create(request: PackageBackedRepresentationOccurrenceRequest): PackageBackedRepresentationOccurrenceResult {
        val diagnostics = request.preflightDiagnostics()
        if (diagnostics.isNotEmpty()) {
            return PackageBackedRepresentationOccurrenceResult(
                occurrence = null,
                diagnostics = diagnostics.sortedRepresentationDiagnostics(),
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
                portAnchorBindings = evidence.anchorMapSummary.toPortAnchorBindings(evidence.semanticSubjectId),
                projectPorts = evidence.anchorMapSummary.toProjectPortFacts(evidence.semanticSubjectId),
                priority = RepresentationPolicyPriority(100),
                compositionIntentMembership = listOf(CompositionIntentMembershipId("package:${evidence.resolverStage}")),
                functionSemanticId = request.functionSemanticId,
            ),
        )
        return PackageBackedRepresentationOccurrenceResult(
            occurrence = binding.occurrenceOrNull,
            diagnostics = binding.diagnostics,
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
        labelSlots = labelSlots.map { slot ->
            RepresentationLabelSlot(
                slotId = RepresentationLabelSlotId(slot.slotId.value),
                role = slot.role.toPresentationRole(),
                origin = slot.placement?.let { placement -> GraphicPoint(placement.originX, placement.originY) },
                bounds = slot.placement?.let { placement ->
                    GraphicBounds(placement.boundsX, placement.boundsY, placement.width, placement.height)
                },
                styleTokenId = slot.styleTokenRef?.let { ref -> GraphicStyleTokenId(ref.value) },
            )
        },
        variants = variants.map { RepresentationVariantId(it.value) },
        graphicBody = descriptorGraphicBody(symbolId),
        anchors = anchors.map { anchor -> anchor.toRepresentationAnchor() },
    )

    private fun RepresentationDescriptor.descriptorGraphicBody(symbolId: RepresentationSymbolId): GraphicPrimitiveDocument =
        GraphicPrimitiveDocument(
            documentId = GraphicPrimitiveDocumentId(symbolId.value),
            bounds = GraphicBounds(0.0, 0.0, bounds.width, bounds.height),
            primitives = listOf(
                GraphicPrimitive.Rectangle(
                    primitiveId = GraphicPrimitiveId("descriptor-bounds"),
                    bounds = GraphicBounds(0.0, 0.0, bounds.width, bounds.height),
                    cornerRadius = 0.0,
                    styleTokenId = GraphicStyleTokenId("descriptor.stroke"),
                ),
            ),
            styleTokens = listOf(
                GraphicStyleToken(
                    styleTokenId = GraphicStyleTokenId("descriptor.stroke"),
                    stroke = GraphicPaintToken("foreground"),
                    strokeWidth = 1.0,
                    fill = GraphicFill.TRANSPARENT,
                    lineCap = GraphicLineCap.BUTT,
                    lineJoin = GraphicLineJoin.MITER,
                ),
            ),
            provenanceSources = listOf("package-runtime:${descriptorId.value}"),
        )

    private fun RepresentationAnchorDefinition.toRepresentationAnchor(): RepresentationAnchorContract =
        RepresentationAnchorContract(
            anchorId = com.engineeringood.athena.representation.RepresentationAnchorId(anchorId.value),
            geometryRef = anchorId.value,
            primitiveId = GraphicPrimitiveId("descriptor-bounds"),
            point = GraphicPoint(x, y),
            role = RepresentationAnchorRole.TERMINAL,
            required = true,
        )

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

    private fun List<String>.toPortAnchorBindings(semanticSubjectId: String): List<RepresentationPortAnchorBinding> =
        parsePairs().map { (semanticPortId, anchorId) ->
            RepresentationPortAnchorBinding(
                bindingId = RepresentationPortAnchorBindingId("binding:$semanticSubjectId:$semanticPortId:$anchorId"),
                semanticPortId = SemanticPortId(semanticPortId),
                anchorId = com.engineeringood.athena.representation.RepresentationAnchorId(anchorId),
                provenance = RepresentationProvenance("binding-evidence:$semanticSubjectId"),
            )
        }

    private fun List<String>.toProjectPortFacts(semanticSubjectId: String): List<RepresentationProjectPortFact> =
        parsePairs().map { (semanticPortId, anchorId) ->
            RepresentationProjectPortFact(
                semanticPortId = SemanticPortId(semanticPortId),
                role = RepresentationAnchorRole.TERMINAL,
                direction = RepresentationDirectionPredicate.BIDIRECTIONAL,
                signal = RepresentationSignalPredicate("package-binding"),
                terminal = PhysicalTerminalId(anchorId),
                provenance = RepresentationProvenance("binding-evidence:$semanticSubjectId"),
            )
        }

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
