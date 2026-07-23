package com.engineeringood.athena.compiler

import com.engineeringood.athena.packageplatform.PresentationProfileId
import com.engineeringood.athena.packageplatform.RepresentationAnchorDefinition
import com.engineeringood.athena.packageplatform.RepresentationAnchorSide
import com.engineeringood.athena.packageplatform.RepresentationDescriptor
import com.engineeringood.athena.packageplatform.RepresentationLabelSlotRole
import com.engineeringood.athena.packageruntime.BindingEvidencePayload
import com.engineeringood.athena.packageruntime.BindingEvidencePayloadMapper
import com.engineeringood.athena.packageruntime.BindingResolver
import com.engineeringood.athena.packageruntime.M32SamplePackageSet
import com.engineeringood.athena.packageruntime.PackageBackedRepresentationOccurrenceFactory
import com.engineeringood.athena.packageruntime.PackageBackedRepresentationOccurrenceRequest
import com.engineeringood.athena.presentation.PresentationPackageEvidence
import com.engineeringood.athena.presentation.PresentationRepresentationFact
import com.engineeringood.athena.projection.ElectricalAnchor
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.representation.GridUnit
import com.engineeringood.athena.representation.LabelFact
import com.engineeringood.athena.representation.LabelFactId
import com.engineeringood.athena.representation.LabelValue
import com.engineeringood.athena.representation.PhysicalTerminalId
import com.engineeringood.athena.representation.PresentationAnatomy
import com.engineeringood.athena.representation.PresentationBounds
import com.engineeringood.athena.representation.PresentationHotspot
import com.engineeringood.athena.representation.PresentationLabelAnchor
import com.engineeringood.athena.representation.PresentationLabelAnchorId
import com.engineeringood.athena.representation.PresentationLabelRole
import com.engineeringood.athena.representation.PresentationPoint
import com.engineeringood.athena.representation.PresentationPrimitive
import com.engineeringood.athena.representation.PresentationPrimitiveId
import com.engineeringood.athena.representation.PresentationRouteAnchor
import com.engineeringood.athena.representation.PresentationRouteAnchorId
import com.engineeringood.athena.representation.PresentationSide
import com.engineeringood.athena.representation.PresentationSize
import com.engineeringood.athena.representation.PresentationTerminalFact
import com.engineeringood.athena.representation.PresentationTerminalId
import com.engineeringood.athena.representation.RepresentationContext
import com.engineeringood.athena.representation.RepresentationId
import com.engineeringood.athena.representation.RepresentationOccurrenceRole
import com.engineeringood.athena.representation.RepresentationProjectionKind
import com.engineeringood.athena.representation.RepresentationProjectionOccurrenceId
import com.engineeringood.athena.representation.RepresentationSemanticRole
import com.engineeringood.athena.representation.RepresentationSubjectId
import com.engineeringood.athena.representation.RepresentationSubjectKind
import com.engineeringood.athena.representation.SemanticPortId
import com.engineeringood.athena.representation.SymbolAnatomy
import com.engineeringood.athena.representation.SymbolFamilyId
import com.engineeringood.athena.representation.TerminalMarker
import com.engineeringood.athena.representation.TerminalNotation
import com.engineeringood.athena.representation.TerminalNumber
import com.engineeringood.athena.representation.TerminalPresentationRole

/**
 * Bridges the M32 package platform proof into live Presentation IR for supported customer-demo
 * surfaces.
 */
class M32PackageBackedPresentationFactDeriver(
    private val packageSet: M32SamplePackageSet = M32SamplePackageSet.loadDefault(),
    private val bindingResolver: BindingResolver = BindingResolver(),
    private val occurrenceFactory: PackageBackedRepresentationOccurrenceFactory = PackageBackedRepresentationOccurrenceFactory(),
) {
    fun derive(projection: ProjectionDocument): List<PresentationRepresentationFact> {
        if (projection.view.id !in setOf("cabinet", "documentation")) {
            return emptyList()
        }
        val projectedSubjectIds = projection.nodes.map { node -> node.semanticId.value }.toSet()
        val packageSubjects = packageSet.semanticSubjectIds
            .map { packageSubjectId -> packageSubjectId to packageSubjectId.toLiveComponentSubjectId() }
            .filter { (_, liveSubjectId) -> liveSubjectId in projectedSubjectIds }
        if (packageSubjects.isEmpty()) {
            return emptyList()
        }

        val sourceProjectionIdsBySemanticId = projection.nodes
            .groupBy { node -> node.semanticId.value }
            .mapValues { (_, nodes) -> nodes.map { node -> node.projectionId.value }.distinct().sorted() }
        val projectionAnchorByPort = projection.electricalAnchors.associateBy { anchor -> anchor.portSemanticId.value }

        return packageSubjects.mapNotNull { (packageSubjectId, liveSubjectId) ->
            val request = packageSet.bindingRequestForSubject(packageSubjectId, PresentationProfileId("m32-iec"))
            val resolution = bindingResolver.resolve(request)
            val evidence = BindingEvidencePayloadMapper.from(request, resolution).copy(
                semanticSubjectId = liveSubjectId,
            )
            val descriptor = packageSet.descriptorFor(evidence.descriptorId)
            val occurrence = occurrenceFactory.create(
                PackageBackedRepresentationOccurrenceRequest(
                    bindingEvidence = evidence,
                    descriptor = descriptor,
                    projectionOccurrenceId = RepresentationProjectionOccurrenceId(
                        sourceProjectionIdsBySemanticId[liveSubjectId]?.firstOrNull() ?: "projection:$liveSubjectId",
                    ),
                    subjectKind = RepresentationSubjectKind.COMPONENT,
                    semanticRole = RepresentationSemanticRole(request.subject.conceptId.value),
                    projectionKind = RepresentationProjectionKind.ELECTRICAL_SCHEMATIC,
                    occurrenceRole = occurrenceRoleForConcept(request.subject.conceptId.value),
                ),
            ).occurrence ?: return@mapNotNull null

            descriptor?.toPresentationRepresentationFact(
                evidence = evidence,
                occurrenceId = occurrence.occurrenceId.value,
                sourceProjectionIds = sourceProjectionIdsBySemanticId[liveSubjectId].orEmpty(),
                projectionAnchorByPort = projectionAnchorByPort,
            )
        }.sortedWith(
            compareBy<PresentationRepresentationFact> { fact -> fact.subjectId.value }
                .thenBy { fact -> fact.occurrenceId.value },
        )
    }

    private fun RepresentationDescriptor.toPresentationRepresentationFact(
        evidence: BindingEvidencePayload,
        occurrenceId: String,
        sourceProjectionIds: List<String>,
        projectionAnchorByPort: Map<String, ElectricalAnchor>,
    ): PresentationRepresentationFact {
        val subjectId = RepresentationSubjectId(evidence.semanticSubjectId)
        val representationOccurrenceId = com.engineeringood.athena.representation.RepresentationOccurrenceId(occurrenceId)
        val terminalFacts = evidence.anchorMapSummary.parsePairs().map { (semanticPortId, descriptorAnchorId) ->
            val descriptorAnchor = anchors.first { anchor -> anchor.anchorId.value == descriptorAnchorId }
            descriptorAnchor.toPresentationTerminalFact(
                subjectId = subjectId,
                occurrenceId = representationOccurrenceId,
                semanticPortId = semanticPortId,
                projectionAnchor = projectionAnchorByPort[semanticPortId],
            )
        }
        val labelFacts = evidence.labelBindingSummary.parsePairs().map { (slotId, value) ->
            val slot = labelSlots.first { candidate -> candidate.slotId.value == slotId }
            LabelFact(
                labelId = LabelFactId("label:${evidence.semanticSubjectId}:$slotId"),
                subjectId = subjectId,
                occurrenceId = representationOccurrenceId,
                role = slot.role.toPresentationRole(),
                value = LabelValue(value),
                anchor = PresentationLabelAnchor(
                    anchorId = PresentationLabelAnchorId("${representationOccurrenceId.value}:$slotId"),
                    role = slot.role.toPresentationRole(),
                    point = PresentationPoint(GridUnit(0), GridUnit(-12)),
                ),
            )
        }
        val anatomy = PresentationAnatomy(
            representationId = RepresentationId(descriptorId.value),
            context = RepresentationContext.ELECTRICAL_SCHEMATIC,
            bounds = PresentationBounds(GridUnit(bounds.width.toInt()), GridUnit(bounds.height.toInt())),
            hotspot = PresentationHotspot(PresentationPoint(GridUnit(0), GridUnit(0))),
            primitives = listOf(
                PresentationPrimitive.Rectangle(
                    primitiveId = PresentationPrimitiveId("${descriptorId.value}:bounds"),
                    origin = PresentationPoint(GridUnit(0), GridUnit(0)),
                    size = PresentationSize(GridUnit(bounds.width.toInt()), GridUnit(bounds.height.toInt())),
                ),
            ),
            terminals = anchors.map { anchor -> anchor.toTerminalPoint() },
            labelAnchors = labelFacts.map(LabelFact::anchor),
        )
        return PresentationRepresentationFact(
            subjectId = subjectId,
            occurrenceId = representationOccurrenceId,
            symbol = SymbolAnatomy(SymbolFamilyId(descriptorId.value), anatomy),
            anatomy = anatomy,
            terminals = terminalFacts,
            labels = labelFacts,
            sourceProjectionIds = sourceProjectionIds,
            packageEvidence = evidence.toPresentationPackageEvidence(this),
        )
    }

    private fun RepresentationAnchorDefinition.toPresentationTerminalFact(
        subjectId: RepresentationSubjectId,
        occurrenceId: com.engineeringood.athena.representation.RepresentationOccurrenceId,
        semanticPortId: String,
        projectionAnchor: ElectricalAnchor?,
    ): PresentationTerminalFact = PresentationTerminalFact(
        presentationTerminalId = PresentationTerminalId("terminal:${subjectId.value}:${anchorId.value}"),
        subjectId = subjectId,
        occurrenceId = occurrenceId,
        portId = SemanticPortId(semanticPortId),
        physicalTerminalId = PhysicalTerminalId("${subjectId.value}:${anchorId.value}"),
        side = side.toPresentationSide(),
        routeAnchor = PresentationRouteAnchor(
            anchorId = PresentationRouteAnchorId(projectionAnchor?.anchorId?.value ?: "descriptor-anchor:${subjectId.value}:${anchorId.value}"),
            point = PresentationPoint(GridUnit(x.toInt()), GridUnit(y.toInt())),
        ),
        notation = TerminalNotation(
            marker = TerminalMarker.CIRCLE,
            number = TerminalNumber(anchorId.value.uppercase()),
        ),
    )

    private fun RepresentationAnchorDefinition.toTerminalPoint(): com.engineeringood.athena.representation.PresentationTerminalPoint =
        com.engineeringood.athena.representation.PresentationTerminalPoint(
            terminalId = PresentationTerminalId(anchorId.value),
            role = TerminalPresentationRole.BIDIRECTIONAL,
            localPoint = PresentationPoint(GridUnit(x.toInt()), GridUnit(y.toInt())),
            side = side.toPresentationSide(),
            notation = TerminalNotation(TerminalMarker.CIRCLE, TerminalNumber(anchorId.value.uppercase())),
        )

    private fun BindingEvidencePayload.toPresentationPackageEvidence(
        descriptor: RepresentationDescriptor,
    ): PresentationPackageEvidence = PresentationPackageEvidence(
        engineeringPackageId = engineeringPackageId,
        engineeringPackageVersion = engineeringPackageVersion,
        presentationProfileId = presentationProfileId,
        bindingManifestId = requireNotNull(bindingManifestId),
        representationPackageId = requireNotNull(representationPackageId),
        representationPackageVersion = requireNotNull(representationPackageVersion),
        descriptorId = requireNotNull(descriptorId),
        graphicResourceId = descriptor.resource.resourceId.value,
        variant = variant.orEmpty(),
        anchorMapSummary = anchorMapSummary,
        labelBindingSummary = labelBindingSummary,
        resolverStage = resolverStage,
        rendererFallbackAccepted = rendererFallbackAccepted,
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

    private fun occurrenceRoleForConcept(conceptId: String): RepresentationOccurrenceRole =
        when (conceptId) {
            "PowerSupply" -> RepresentationOccurrenceRole.SUPPLY_REFERENCE
            "RollerRelay" -> RepresentationOccurrenceRole.COIL_ACTUATOR
            "ShutterMotor" -> RepresentationOccurrenceRole.MOTOR_LOAD
            else -> RepresentationOccurrenceRole.LOAD_SYMBOL
        }

    private fun String.toLiveComponentSubjectId(): String =
        replaceFirst("device:", "component:")

    private fun List<String>.parsePairs(): Map<String, String> =
        mapNotNull { summary ->
            val key = summary.substringBefore("=", missingDelimiterValue = "")
            val value = summary.substringAfter("=", missingDelimiterValue = "")
            if (key.isBlank() || value.isBlank()) null else key to value
        }.toMap()
}
