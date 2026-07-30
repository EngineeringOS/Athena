package com.engineeringood.athena.compiler

import com.engineeringood.athena.layout.LayoutConstraintOwner
import com.engineeringood.athena.layout.LayoutConstraintStrength
import com.engineeringood.athena.layout.LayoutGraphAnchor
import com.engineeringood.athena.layout.LayoutGraphBounds
import com.engineeringood.athena.layout.LayoutGraphConstraintKind
import com.engineeringood.athena.layout.LayoutGraphConstraint
import com.engineeringood.athena.layout.LayoutGraphObjectId
import com.engineeringood.athena.layout.LayoutGraphObstacleKind
import com.engineeringood.athena.layout.LayoutGraphObstacle
import com.engineeringood.athena.layout.LayoutGraphOccurrence
import com.engineeringood.athena.layout.LayoutGraphPoint
import com.engineeringood.athena.layout.LayoutGraphPort
import com.engineeringood.athena.layout.LayoutGraphProvenance
import com.engineeringood.athena.layout.LayoutGraphRelationshipKind
import com.engineeringood.athena.layout.LayoutGraphRelationship
import com.engineeringood.athena.layout.LayoutGraphSnapshotId
import com.engineeringood.athena.layout.LayoutGraphSourceSpan
import com.engineeringood.athena.layout.LayoutGraphSourceUnitId
import com.engineeringood.athena.layout.LayoutGraph
import com.engineeringood.athena.physical.PhysicalDuct
import com.engineeringood.athena.physical.PhysicalInfrastructureOrientation
import com.engineeringood.athena.physical.PhysicalInstallationIR
import com.engineeringood.athena.physical.PhysicalMountedOccurrence
import com.engineeringood.athena.physical.PhysicalMountTargetRef
import com.engineeringood.athena.physical.PhysicalRail
import com.engineeringood.athena.physical.PhysicalRouteChannel
import com.engineeringood.athena.physical.PhysicalRigidFrame2i
import com.engineeringood.athena.physical.PhysicalSourceProvenance
import com.engineeringood.athena.physical.PhysicalTerminalGroup
import com.engineeringood.athena.physical.PhysicalPoint2i
import com.engineeringood.athena.physical.PhysicalVector2i
import kotlin.math.max
import kotlin.math.roundToInt
import java.security.MessageDigest

internal object AthenaLayoutGraphLowerer {
    fun lower(
        physical: PhysicalInstallationIR,
        materials: List<AthenaResolvedRepresentationMaterial>,
    ): AthenaLayoutGraphLoweringResult {
        val diagnostics = mutableListOf<AthenaCabinetProjectionDiagnostic>()
        val materialsBySubject = materials.associateBy { material -> material.semanticSubjectId }
        val enclosureBounds = LayoutGraphBounds(
            x = 0,
            y = 0,
            width = max(1, physical.space.enclosure.size.width),
            height = max(1, physical.space.enclosure.size.height),
        )

        val surfacesById = physical.space.surfaces.associateBy { it.id }
        val ductsById = physical.space.ducts.associateBy { it.id }
        val obstacles = buildList {
            add(physical.space.enclosure.toObstacle())
            addAll(physical.space.surfaces.map { surface -> surface.toObstacle() })
            addAll(physical.space.rails.map { rail -> rail.toObstacle(surfacesById.getValue(rail.surfaceId).at) })
            addAll(physical.space.ducts.map { duct -> duct.toObstacle() })
            addAll(physical.space.channels.map { channel -> channel.toObstacle(ductsById.getValue(channel.ductId)) })
            addAll(physical.space.terminalGroups.map { group -> group.toObstacle() })
        }

        val occurrences = physical.space.mountedOccurrences.mapNotNull { occurrence ->
            val material = materialsBySubject[occurrence.semanticSubjectId.value]
            if (material == null) {
                diagnostics += diagnostic(
                    code = "layout.graph.material.missing",
                    subject = occurrence.semanticSubjectId.value,
                    expected = "resolved cabinet representation material",
                    measured = occurrence.occurrenceId.value,
                    span = occurrence.provenance.span,
                )
                return@mapNotNull null
            }

            val graphicBounds = material.definition.graphicBody.bounds
            val representationBounds = graphicBounds?.toLayoutBounds()
            if (representationBounds == null) {
                diagnostics += diagnostic(
                    code = "layout.graph.bounds.missing",
                    subject = material.semanticSubjectId,
                    expected = "representation bounds",
                    measured = material.definition.symbolId.value,
                    span = occurrence.provenance.span,
                )
                return@mapNotNull null
            }

            val occurrenceBounds = occurrence.toAbsoluteBounds(physical)
            if (!enclosureBounds.contains(occurrenceBounds)) {
                diagnostics += diagnostic(
                    code = "layout.graph.containment.invalid",
                    subject = occurrence.semanticSubjectId.value,
                    expected = "occurrence inside enclosure bounds",
                    measured = "${occurrenceBounds.x},${occurrenceBounds.y},${occurrenceBounds.width},${occurrenceBounds.height}",
                    span = occurrence.provenance.span,
                )
            }

            val anchorById = material.definition.anchors.associateBy { anchor -> anchor.anchorId.value }
            val ports = material.terminalBindings.mapNotNull { (portSemanticId, terminalIdentity) ->
                val anchorId = material.resolution.anchorMapping[portSemanticId]?.value
                if (anchorId == null) {
                    diagnostics += diagnostic(
                        code = "layout.graph.port.anchor.missing",
                        subject = portSemanticId,
                        expected = "bound representation anchor id",
                        measured = terminalIdentity,
                        span = occurrence.provenance.span,
                    )
                    return@mapNotNull null
                }
                val anchor = anchorById[anchorId]
                if (anchor == null) {
                    diagnostics += diagnostic(
                        code = "layout.graph.port.anchor.unresolved",
                        subject = portSemanticId,
                        expected = "resolved anchor in representation definition",
                        measured = anchorId,
                        span = occurrence.provenance.span,
                    )
                    return@mapNotNull null
                }
                LayoutGraphPort(
                    portSemanticId = portSemanticId,
                    terminalIdentity = terminalIdentity,
                    anchorId = anchorId,
                    direction = anchor.acceptedDirections.singleOrNull()?.name,
                    signal = anchor.acceptedSignals.singleOrNull()?.value,
                    required = anchor.required,
                    provenance = materialProvenance(material, occurrence),
                )
            }.sortedBy { port -> port.portSemanticId }

            val anchors = material.definition.anchors.map { anchor ->
                LayoutGraphAnchor(
                    anchorId = anchor.anchorId.value,
                    geometryRef = anchor.geometryRef,
                    primitiveId = anchor.primitiveId.value,
                    point = LayoutGraphPoint(anchor.point.x.roundToInt(), anchor.point.y.roundToInt()),
                    role = anchor.role.name,
                    required = anchor.required,
                    acceptedDirections = anchor.acceptedDirections.map { direction -> direction.name }.sorted(),
                    acceptedSignals = anchor.acceptedSignals.map { signal -> signal.value }.sorted(),
                    provenance = materialProvenance(material, occurrence),
                )
            }.sortedBy { anchor -> anchor.anchorId }

            val constraints = buildList {
                add(
                    LayoutGraphConstraint(
                        constraintId = LayoutGraphObjectId("layout:${occurrence.occurrenceId.value}:representation-bounds"),
                        owner = LayoutConstraintOwner.REPRESENTATION,
                        strength = LayoutConstraintStrength.REQUIRED,
                        kind = LayoutGraphConstraintKind.REPRESENTATION_BOUNDS,
                        subjectId = occurrence.occurrenceId.value,
                        targetId = material.definition.symbolId.value,
                        note = "intrinsic representation bounds",
                        provenance = materialProvenance(material, occurrence),
                    ),
                )
                add(
                    LayoutGraphConstraint(
                        constraintId = LayoutGraphObjectId("layout:${occurrence.occurrenceId.value}:physical-mount"),
                        owner = LayoutConstraintOwner.PHYSICAL,
                        strength = LayoutConstraintStrength.REQUIRED,
                        kind = LayoutGraphConstraintKind.PHYSICAL_MOUNT,
                        subjectId = occurrence.occurrenceId.value,
                        targetId = occurrence.target.id.value,
                        note = occurrence.selectedOrientation.name,
                        provenance = occurrence.provenance.toLayoutProvenance(),
                    ),
                )
                add(
                    LayoutGraphConstraint(
                        constraintId = LayoutGraphObjectId("layout:${occurrence.occurrenceId.value}:clearance"),
                        owner = LayoutConstraintOwner.PHYSICAL,
                        strength = LayoutConstraintStrength.REQUIRED,
                        kind = LayoutGraphConstraintKind.PHYSICAL_CLEARANCE,
                        subjectId = occurrence.occurrenceId.value,
                        note = "clearance top=${occurrence.contract.clearance.top.value} right=${occurrence.contract.clearance.right.value} " +
                            "bottom=${occurrence.contract.clearance.bottom.value} left=${occurrence.contract.clearance.left.value}",
                        provenance = occurrence.provenance.toLayoutProvenance(),
                    ),
                )
                add(
                    LayoutGraphConstraint(
                        constraintId = LayoutGraphObjectId("layout:${occurrence.occurrenceId.value}:orientation"),
                        owner = LayoutConstraintOwner.LAYOUT_PREFERENCE,
                        strength = LayoutConstraintStrength.PREFERRED,
                        kind = LayoutGraphConstraintKind.PHYSICAL_ORIENTATION,
                        subjectId = occurrence.occurrenceId.value,
                        note = occurrence.selectedOrientation.name,
                        provenance = occurrence.provenance.toLayoutProvenance(),
                    ),
                )
                ports.forEach { port ->
                    add(
                        LayoutGraphConstraint(
                            constraintId = LayoutGraphObjectId("layout:${occurrence.occurrenceId.value}:port:${port.portSemanticId}"),
                            owner = LayoutConstraintOwner.SEMANTIC,
                            strength = LayoutConstraintStrength.REQUIRED,
                            kind = LayoutGraphConstraintKind.PORT_ANCHOR_BINDING,
                            subjectId = port.portSemanticId,
                            targetId = port.anchorId,
                            note = port.terminalIdentity,
                            provenance = port.provenance,
                        ),
                    )
                }
            }.sortedBy { constraint -> constraint.constraintId.value }

            LayoutGraphOccurrence(
                occurrenceId = LayoutGraphObjectId(occurrence.occurrenceId.value),
                semanticSubjectId = occurrence.semanticSubjectId.value,
                physicalOccurrenceId = occurrence.occurrenceId.value,
                bounds = occurrenceBounds,
                representationBounds = representationBounds,
                ports = ports,
                anchors = anchors,
                constraints = constraints,
                provenance = occurrence.provenance.toLayoutProvenance(),
            )
        }

        for (firstIndex in 0 until occurrences.size) {
            for (secondIndex in firstIndex + 1 until occurrences.size) {
                val first = occurrences[firstIndex]
                val second = occurrences[secondIndex]
                if (first.bounds.intersects(second.bounds)) {
                    diagnostics += diagnostic(
                        code = "layout.graph.occurrence.overlap",
                        subject = first.semanticSubjectId,
                        expected = "non-overlapping occurrence bounds",
                        measured = second.semanticSubjectId,
                        span = first.provenance.span?.toPhysicalSourceSpan(),
                    )
                }
            }
        }

        val mountedByOccurrenceId = physical.space.mountedOccurrences.associateBy { mounted -> mounted.occurrenceId.value }
        val relationships = buildList {
            occurrences.forEach { occurrence ->
                add(
                    LayoutGraphRelationship(
                        relationshipId = LayoutGraphObjectId("layout:${occurrence.occurrenceId.value}:containment"),
                        kind = LayoutGraphRelationshipKind.CONTAINMENT,
                        sourceId = occurrence.occurrenceId.value,
                        targetId = physical.space.enclosure.id.value,
                        provenance = occurrence.provenance,
                    ),
                )
                add(
                    LayoutGraphRelationship(
                        relationshipId = LayoutGraphObjectId("layout:${occurrence.occurrenceId.value}:mount"),
                        kind = LayoutGraphRelationshipKind.MOUNT,
                        sourceId = occurrence.occurrenceId.value,
                        targetId = mountedByOccurrenceId.getValue(occurrence.physicalOccurrenceId).target.id.value,
                        provenance = occurrence.provenance,
                    ),
                )
            }
            physical.routes.forEach { route ->
                route.channelIds.forEach { channelId ->
                    add(
                        LayoutGraphRelationship(
                            relationshipId = LayoutGraphObjectId("layout:${route.connectionAlias}:channel:${channelId.value}"),
                            kind = LayoutGraphRelationshipKind.ROUTE_CHANNEL,
                            sourceId = route.connectionAlias,
                            targetId = channelId.value,
                            provenance = route.provenance.toLayoutProvenance(),
                        ),
                    )
                }
            }
        }.sortedBy { relationship -> relationship.relationshipId.value }

        if (diagnostics.isNotEmpty()) {
            return AthenaLayoutGraphLoweringResult(
                graph = null,
                diagnostics = diagnostics.sortedBy { diagnostic -> listOf(diagnostic.code, diagnostic.subject, diagnostic.message).joinToString("|") },
            )
        }

        val graphConstraints = occurrences.flatMap { occurrence -> occurrence.constraints } +
            physical.routes.flatMap { route ->
                route.channelIds.map { channelId ->
                    LayoutGraphConstraint(
                        constraintId = LayoutGraphObjectId("layout:${route.connectionAlias}:channel:${channelId.value}"),
                        owner = LayoutConstraintOwner.PHYSICAL,
                        strength = LayoutConstraintStrength.REQUIRED,
                        kind = LayoutGraphConstraintKind.ROUTE_CHANNEL,
                        subjectId = route.connectionAlias,
                        targetId = channelId.value,
                        note = "route channel fact",
                        provenance = route.provenance.toLayoutProvenance(),
                    )
                }
            }
        val snapshotDigest = layoutDigest(
            physical.toString() + materials.sortedBy { it.semanticSubjectId }.joinToString("|") { it.toString() },
        )
        return AthenaLayoutGraphLoweringResult(
            graph = LayoutGraph.canonical(
                snapshotId = LayoutGraphSnapshotId("layout:${physical.sourceUnitId.value}:${physical.installationId.value}:$snapshotDigest"),
                sourceUnitId = LayoutGraphSourceUnitId(physical.sourceUnitId.value),
                installationId = physical.installationId.value,
                compilerSnapshotId = "athena-cabinet-compiler:$snapshotDigest",
                occurrences = occurrences,
                obstacles = obstacles,
                relationships = relationships,
                constraints = graphConstraints,
            ),
        )
    }

    private fun com.engineeringood.athena.physical.PhysicalEnclosure.toObstacle(): LayoutGraphObstacle = LayoutGraphObstacle(
        obstacleId = LayoutGraphObjectId(id.value),
        kind = LayoutGraphObstacleKind.ENCLOSURE,
        bounds = LayoutGraphBounds(0, 0, max(1, size.width), max(1, size.height)),
        provenance = provenance.toLayoutProvenance(),
    )

    private fun com.engineeringood.athena.physical.PhysicalMountingSurface.toObstacle(): LayoutGraphObstacle =
        LayoutGraphObstacle(
            obstacleId = LayoutGraphObjectId(id.value),
            kind = LayoutGraphObstacleKind.SURFACE,
            bounds = LayoutGraphBounds(at.x, at.y, max(1, size.width), max(1, size.height)),
            provenance = provenance.toLayoutProvenance(),
        )

    private fun PhysicalRail.toObstacle(surfaceOrigin: com.engineeringood.athena.physical.PhysicalPoint2i): LayoutGraphObstacle {
        val width = if (orientation == PhysicalInfrastructureOrientation.Horizontal) max(1, length.value) else 1
        val height = if (orientation == PhysicalInfrastructureOrientation.Vertical) max(1, length.value) else 1
        return LayoutGraphObstacle(
            obstacleId = LayoutGraphObjectId(id.value),
            kind = LayoutGraphObstacleKind.RAIL,
            bounds = LayoutGraphBounds(surfaceOrigin.x + at.x, surfaceOrigin.y + at.y, width, height),
            provenance = provenance.toLayoutProvenance(),
        )
    }

    private fun PhysicalDuct.toObstacle(): LayoutGraphObstacle = LayoutGraphObstacle(
        obstacleId = LayoutGraphObjectId(id.value),
        kind = LayoutGraphObstacleKind.DUCT,
        bounds = LayoutGraphBounds(at.x, at.y, max(1, size.width), max(1, size.height)),
        provenance = provenance.toLayoutProvenance(),
    )

    private fun PhysicalRouteChannel.toObstacle(duct: PhysicalDuct): LayoutGraphObstacle = LayoutGraphObstacle(
        obstacleId = LayoutGraphObjectId(id.value),
        kind = LayoutGraphObstacleKind.CHANNEL,
        bounds = LayoutGraphBounds(
            duct.at.x + duct.wall.value + at.x,
            duct.at.y + duct.wall.value + at.y,
            max(1, size.width),
            max(1, size.height),
        ),
        provenance = provenance.toLayoutProvenance(),
    )

    private fun PhysicalTerminalGroup.toObstacle(): LayoutGraphObstacle = LayoutGraphObstacle(
        obstacleId = LayoutGraphObjectId(id.value),
        kind = LayoutGraphObstacleKind.TERMINAL_GROUP,
        bounds = LayoutGraphBounds(at.x, at.y, max(1, size.width), max(1, size.height)),
        provenance = provenance.toLayoutProvenance(),
    )

    private fun LayoutGraphSourceSpan.toPhysicalSourceSpan(): com.engineeringood.athena.physical.PhysicalSourceSpan =
        com.engineeringood.athena.physical.PhysicalSourceSpan(
            file = sourceUnitId.value,
            line = line,
            column = column,
        )

    private fun PhysicalSourceProvenance.toLayoutProvenance(): LayoutGraphProvenance =
        LayoutGraphProvenance(
            sourceUnitId = LayoutGraphSourceUnitId(sourceUnitId.value),
            declarationId = declarationId,
            span = span.toLayoutSourceSpan(),
        )

    private fun com.engineeringood.athena.physical.PhysicalSourceSpan?.toLayoutSourceSpan(): LayoutGraphSourceSpan? =
        this?.let { sourceSpan ->
            LayoutGraphSourceSpan(
                sourceUnitId = LayoutGraphSourceUnitId(sourceSpan.file),
                declarationId = sourceSpan.file,
                line = sourceSpan.line,
                column = sourceSpan.column,
        )
    }

    private fun PhysicalMountedOccurrence.toAbsoluteBounds(
        physical: PhysicalInstallationIR,
    ): LayoutGraphBounds {
        val surfaces = physical.space.surfaces.associateBy { surface -> surface.id }
        val rails = physical.space.rails.associateBy { rail -> rail.id }
        val terminalGroups = physical.space.terminalGroups.associateBy { group -> group.id }
        val frame = when (val target = target) {
            is PhysicalMountTargetRef.Surface -> surfaces.getValue(target.id).at.identityFrame()
            is PhysicalMountTargetRef.Rail -> {
                val rail = rails.getValue(target.id)
                val surface = surfaces.getValue(rail.surfaceId)
                rail.frame.translate(surface.at)
            }
            is PhysicalMountTargetRef.TerminalGroup -> terminalGroups.getValue(target.id).frame()
        }
        val rotated = selectedOrientation == com.engineeringood.athena.physical.PhysicalInstallationOrientation.Deg90 ||
            selectedOrientation == com.engineeringood.athena.physical.PhysicalInstallationOrientation.Deg270
        val width = if (rotated) contract.size.height.value else contract.size.width.value
        val height = if (rotated) contract.size.width.value else contract.size.height.value
        val corners = listOf(
            at,
            PhysicalPoint2i(at.x + width, at.y),
            PhysicalPoint2i(at.x + width, at.y + height),
            PhysicalPoint2i(at.x, at.y + height),
        ).map(frame::toParent)
        val minimumX = corners.minOf { point -> point.x }
        val minimumY = corners.minOf { point -> point.y }
        val maximumX = corners.maxOf { point -> point.x }
        val maximumY = corners.maxOf { point -> point.y }
        return LayoutGraphBounds(
            x = minimumX,
            y = minimumY,
            width = max(1, maximumX - minimumX),
            height = max(1, maximumY - minimumY),
        )
    }

    private fun PhysicalPoint2i.identityFrame(): PhysicalRigidFrame2i = PhysicalRigidFrame2i(
        origin = this,
        alongAxis = PhysicalVector2i(1, 0),
        normalAxis = PhysicalVector2i(0, 1),
    )

    private fun PhysicalRigidFrame2i.translate(point: PhysicalPoint2i): PhysicalRigidFrame2i = copy(
        origin = PhysicalPoint2i(origin.x + point.x, origin.y + point.y),
    )

    private fun PhysicalTerminalGroup.frame(): PhysicalRigidFrame2i = when (orientation) {
        PhysicalInfrastructureOrientation.Horizontal -> at.identityFrame()
        PhysicalInfrastructureOrientation.Vertical -> PhysicalRigidFrame2i(
            origin = at,
            alongAxis = PhysicalVector2i(0, 1),
            normalAxis = PhysicalVector2i(-1, 0),
        )
    }

    private fun com.engineeringood.athena.representation.GraphicBounds.toLayoutBounds(): LayoutGraphBounds =
        LayoutGraphBounds(
            x = x.roundToInt(),
            y = y.roundToInt(),
            width = max(1, width.roundToInt()),
            height = max(1, height.roundToInt()),
        )

    private fun materialProvenance(
        material: AthenaResolvedRepresentationMaterial,
        occurrence: PhysicalMountedOccurrence,
    ): LayoutGraphProvenance = LayoutGraphProvenance(
        sourceUnitId = LayoutGraphSourceUnitId(occurrence.provenance.sourceUnitId.value),
        declarationId = material.definition.symbolId.value,
        span = occurrence.provenance.span.toLayoutSourceSpan(),
    )

    private fun diagnostic(
        code: String,
        subject: String,
        expected: String,
        measured: String? = null,
        span: com.engineeringood.athena.physical.PhysicalSourceSpan? = null,
    ): AthenaCabinetProjectionDiagnostic = AthenaCabinetProjectionDiagnostic(
        code = code,
        subject = subject,
        message = buildString {
            append(expected)
            if (measured != null) {
                append(" (measured: ")
                append(measured)
                append(')')
            }
            if (span != null) {
                append(" @ ")
                append(span.file)
                append(':')
                append(span.line)
                append(':')
                append(span.column)
            }
        },
    )
}

private fun layoutDigest(value: String): String = MessageDigest.getInstance("SHA-256")
    .digest(value.toByteArray(Charsets.UTF_8))
    .take(12)
    .joinToString("") { byte -> "%02x".format(byte) }

internal data class AthenaLayoutGraphLoweringResult(
    val graph: LayoutGraph? = null,
    val diagnostics: List<AthenaCabinetProjectionDiagnostic> = emptyList(),
)
