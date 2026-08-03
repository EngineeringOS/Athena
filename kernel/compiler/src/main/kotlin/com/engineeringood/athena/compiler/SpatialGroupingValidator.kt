package com.engineeringood.athena.compiler

import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.projection.ProjectionNode
import com.engineeringood.athena.projection.ProjectionSheet
import com.engineeringood.athena.projection.ProjectionSheetConstruct
import com.engineeringood.athena.spatial.SpatialDiagnostic
import com.engineeringood.athena.spatial.SpatialOccurrenceGeometry
import com.engineeringood.athena.spatial.SpatialOccurrenceId
import com.engineeringood.athena.spatial.SpatialSourceTrace

internal class SpatialGroupingValidator(
    private val planner: ProjectionPlacementPlanner,
) {
    fun validate(
        projection: ProjectionDocument,
        occurrences: List<SpatialOccurrenceGeometry>,
    ): List<SpatialDiagnostic> {
        val sheets = projection.sheets.sortedWith(compareBy({ sheet -> sheet.order }, { sheet -> sheet.sheetId.value }))
        val diagnostics = mutableListOf<SpatialDiagnostic>()
        diagnostics += occurrenceIdentityDiagnostics(projection, occurrences)
        sheets.forEach { sheet ->
            val sheetNodes = projection.nodes.filter { node -> planner.sheetOwns(sheet, node) }
            val nodesByLabel = sheetNodes.groupBy(ProjectionNode::label)
            val occurrenceById = occurrences
                .filter { occurrence -> occurrence.sheetId == sheet.sheetId.value }
                .sortedWith(occurrenceGeometryOrder)
                .associateBy(SpatialOccurrenceGeometry::occurrenceId)
            diagnostics += groupingDefinitionDiagnostics(sheet)
            diagnostics += regionIdentityDiagnostics(sheet)
            diagnostics += constructIdentityDiagnostics(sheet)
            sheet.regions.filter { region -> region.regionId.isNotBlank() }.forEach { region ->
                val memberResolution = resolveMembers(
                    projection = projection,
                    sheets = sheets,
                    sheet = sheet,
                    groupSubject = "Region ${region.name}",
                    groupId = region.regionId,
                    groupSource = region.originGeometryElementId,
                    memberNames = region.occurrenceNames,
                    nodesByLabel = nodesByLabel,
                    occurrenceById = occurrenceById,
                )
                diagnostics += memberResolution.diagnostics
                diagnostics += outOfAreaDiagnostic(
                    subject = "Region ${region.name}",
                    geometryName = "bound",
                    sheet = sheet,
                    groupId = region.regionId,
                    groupSource = region.originGeometryElementId,
                    memberNodes = memberResolution.resolvedNodes,
                    memberOccurrences = memberResolution.resolvedOccurrences,
                )
            }
            planner.placementGroups(sheet, projection)
                .filterNot(ProjectionPlacementGroup::authored)
                .forEach { group ->
                    val subject = "Region ${group.regionName}"
                    val memberResolution = resolveCompilerOwnedMembers(
                        sheet = sheet,
                        groupSubject = subject,
                        groupId = group.regionId,
                        groupSource = sheet.originGeometryElementId,
                        memberNodes = group.nodes,
                        occurrenceById = occurrenceById,
                    )
                    diagnostics += memberResolution.diagnostics
                    diagnostics += outOfAreaDiagnostic(
                        subject = subject,
                        geometryName = "bound",
                        sheet = sheet,
                        groupId = group.regionId,
                        groupSource = sheet.originGeometryElementId,
                        memberNodes = memberResolution.resolvedNodes,
                        memberOccurrences = memberResolution.resolvedOccurrences,
                    )
                }
            sheet.constructs.filter { construct -> construct.constructId.value.isNotBlank() }.forEach { construct ->
                val subject = constructSubject(construct)
                val memberResolution = resolveMembers(
                    projection = projection,
                    sheets = sheets,
                    sheet = sheet,
                    groupSubject = subject,
                    groupId = construct.constructId.value,
                    groupSource = construct.originGeometryElementId,
                    memberNames = construct.memberNames,
                    nodesByLabel = nodesByLabel,
                    occurrenceById = occurrenceById,
                )
                diagnostics += memberResolution.diagnostics
                diagnostics += outOfAreaDiagnostic(
                    subject = subject,
                    geometryName = "envelope",
                    sheet = sheet,
                    groupId = construct.constructId.value,
                    groupSource = construct.originGeometryElementId,
                    memberNodes = memberResolution.resolvedNodes,
                    memberOccurrences = memberResolution.resolvedOccurrences,
                )
            }
        }
        return diagnostics.sortedWith(compareBy(SpatialDiagnostic::subject, SpatialDiagnostic::problem))
    }

    private fun occurrenceIdentityDiagnostics(
        projection: ProjectionDocument,
        occurrences: List<SpatialOccurrenceGeometry>,
    ): List<SpatialDiagnostic> = occurrences.groupBy(SpatialOccurrenceGeometry::occurrenceId)
        .filterValues { matches -> matches.size > 1 }
        .toSortedMap(compareBy({ id -> id.sheetId }, { id -> id.projectionId }))
        .map { (occurrenceId, matches) ->
            val label = projection.nodes
                .singleOrNull { node -> node.projectionId.value == occurrenceId.projectionId }
                ?.label
                ?: occurrenceId.projectionId
            SpatialDiagnostic(
                subject = "Occurrence $label on Sheet ${occurrenceId.sheetId}",
                problem = "has ${matches.size} placed geometry facts",
                correction = "Publish exactly one placed Occurrence geometry for $label on Sheet ${occurrenceId.sheetId}.",
                sourceTrace = SpatialSourceTrace(
                    projectionIds = listOf(occurrenceId.sheetId, occurrenceId.projectionId),
                    geometryElementIds = matches
                        .flatMap { occurrence -> occurrence.sourceTrace.geometryElementIds }
                        .distinctBy(GeometryElementId::value)
                        .sortedBy(GeometryElementId::value),
                ),
            )
        }

    private fun groupingDefinitionDiagnostics(sheet: ProjectionSheet): List<SpatialDiagnostic> = buildList {
        sheet.regions.filter { region -> region.regionId.isBlank() }.forEach { region ->
            add(
                SpatialDiagnostic(
                    subject = "Region identity",
                    problem = "is blank",
                    correction = "Give every Region on Sheet ${sheet.sheetId.value} a non-blank identity.",
                    sourceTrace = SpatialSourceTrace(
                        projectionIds = listOf(sheet.sheetId.value),
                        geometryElementIds = listOf(region.originGeometryElementId),
                    ),
                ),
            )
        }
        val reservedId = planner.unassignedRegionId(sheet)
        sheet.regions.filter { region -> region.regionId == reservedId }.forEach { region ->
            add(
                SpatialDiagnostic(
                    subject = "Region identity $reservedId",
                    problem = "is reserved for compiler-owned Unassigned Region",
                    correction = "Give the authored Region on Sheet ${sheet.sheetId.value} a different identity.",
                    sourceTrace = SpatialSourceTrace(
                        projectionIds = listOf(sheet.sheetId.value, reservedId),
                        geometryElementIds = listOf(region.originGeometryElementId),
                    ),
                ),
            )
        }
        sheet.constructs.filter { construct -> construct.constructId.value.isBlank() }.forEach { construct ->
            add(
                SpatialDiagnostic(
                    subject = "Construct identity",
                    problem = "is blank",
                    correction = "Give every Construct on Sheet ${sheet.sheetId.value} a non-blank identity.",
                    sourceTrace = SpatialSourceTrace(
                        projectionIds = listOf(sheet.sheetId.value),
                        geometryElementIds = listOf(construct.originGeometryElementId),
                    ),
                ),
            )
        }
        sheet.constructs.filter { construct -> construct.kind.isBlank() }.forEach { construct ->
            add(
                SpatialDiagnostic(
                    subject = constructSubject(construct),
                    problem = "has no kind",
                    correction = "Name the engineering grouping kind for ${constructSubject(construct)}.",
                    sourceTrace = SpatialSourceTrace(
                        projectionIds = listOf(sheet.sheetId.value) +
                            listOfNotNull(construct.constructId.value.takeIf(String::isNotBlank)),
                        geometryElementIds = listOf(construct.originGeometryElementId),
                    ),
                ),
            )
        }
    }

    private fun regionIdentityDiagnostics(sheet: ProjectionSheet): List<SpatialDiagnostic> =
        sheet.regions.groupBy { region -> region.regionId }
            .filterKeys(String::isNotBlank)
            .filterValues { regions -> regions.size > 1 }
            .map { (regionId, regions) ->
                SpatialDiagnostic(
                    subject = "Region identity $regionId",
                    problem = "is used by ${regions.size} Regions on one Sheet",
                    correction = "Give every Region on Sheet ${sheet.sheetId.value} a unique identity.",
                    sourceTrace = SpatialSourceTrace(
                        projectionIds = listOf(sheet.sheetId.value, regionId),
                        geometryElementIds = regions.map { region -> region.originGeometryElementId },
                    ),
                )
            }

    private fun constructIdentityDiagnostics(sheet: ProjectionSheet): List<SpatialDiagnostic> =
        sheet.constructs.groupBy { construct -> construct.constructId }
            .filterKeys { constructId -> constructId.value.isNotBlank() }
            .filterValues { constructs -> constructs.size > 1 }
            .map { (constructId, constructs) ->
                SpatialDiagnostic(
                    subject = "Construct identity ${constructId.value}",
                    problem = "is used by ${constructs.size} Constructs on one Sheet",
                    correction = "Give every Construct on Sheet ${sheet.sheetId.value} a unique identity.",
                    sourceTrace = SpatialSourceTrace(
                        projectionIds = listOf(sheet.sheetId.value, constructId.value),
                        geometryElementIds = constructs.map { construct -> construct.originGeometryElementId },
                    ),
                )
            }

    private fun resolveMembers(
        projection: ProjectionDocument,
        sheets: List<ProjectionSheet>,
        sheet: ProjectionSheet,
        groupSubject: String,
        groupId: String,
        groupSource: GeometryElementId,
        memberNames: List<String>,
        nodesByLabel: Map<String, List<ProjectionNode>>,
        occurrenceById: Map<SpatialOccurrenceId, SpatialOccurrenceGeometry>,
    ): MemberResolution {
        if (memberNames.isEmpty()) {
            return MemberResolution(
                diagnostics = listOf(
                    SpatialDiagnostic(
                        subject = groupSubject,
                        problem = "has no members",
                        correction = "Add at least one Occurrence to $groupSubject.",
                        sourceTrace = groupTrace(sheet, groupId, groupSource),
                    ),
                ),
            )
        }
        val diagnostics = mutableListOf<SpatialDiagnostic>()
        memberNames.groupingBy(String::toString).eachCount()
            .filterValues { count -> count > 1 }
            .keys
            .sorted()
            .forEach { name ->
                val node = nodesByLabel[name].orEmpty().singleOrNull()
                diagnostics += SpatialDiagnostic(
                    subject = "$groupSubject member $name",
                    problem = "is listed more than once",
                    correction = "List $name once in $groupSubject.",
                    sourceTrace = groupTrace(sheet, groupId, groupSource, listOfNotNull(node)),
                )
            }
        val resolvedNodes = mutableListOf<ProjectionNode>()
        val resolvedOccurrences = mutableListOf<SpatialOccurrenceGeometry>()
        memberNames.distinct().forEach { name ->
            val ownedMatches = nodesByLabel[name].orEmpty().sortedBy { node -> node.projectionId.value }
            val allMatches = projection.nodes.filter { node -> node.label == name }
                .sortedBy { node -> node.projectionId.value }
            when {
                allMatches.isEmpty() -> diagnostics += SpatialDiagnostic(
                    subject = "$groupSubject member $name",
                    problem = "does not resolve to a projected Occurrence",
                    correction = "Name an Occurrence owned by Sheet ${sheet.sheetId.value} in $groupSubject.",
                    sourceTrace = groupTrace(sheet, groupId, groupSource),
                )
                ownedMatches.size > 1 -> diagnostics += SpatialDiagnostic(
                    subject = "$groupSubject member $name",
                    problem = "matches ${ownedMatches.size} projected Occurrences",
                    correction = "Reference a unique projected Occurrence in $groupSubject.",
                    sourceTrace = groupTrace(sheet, groupId, groupSource, ownedMatches),
                )
                ownedMatches.isEmpty() -> {
                    val owners = sheets.filter { candidate ->
                        allMatches.any { node -> planner.sheetOwns(candidate, node) }
                    }.sortedBy { owner -> owner.sheetId.value }
                    val problem = if (owners.isEmpty()) {
                        "is not owned by any Sheet"
                    } else {
                        "belongs to ${owners.joinToString { owner -> "Sheet ${owner.sheetId.value}" }}, " +
                            "not Sheet ${sheet.sheetId.value}"
                    }
                    diagnostics += SpatialDiagnostic(
                        subject = "$groupSubject member $name",
                        problem = problem,
                        correction = "List $name only in a grouping on its owning Sheet.",
                        sourceTrace = groupTrace(sheet, groupId, groupSource, allMatches),
                    )
                }
                else -> {
                    val node = ownedMatches.single()
                    resolvedNodes += node
                    val occurrenceId = SpatialOccurrenceId(sheet.sheetId.value, node.projectionId.value)
                    val occurrence = occurrenceById[occurrenceId]
                    if (occurrence == null) {
                        diagnostics += SpatialDiagnostic(
                            subject = "$groupSubject member $name",
                            problem = "has no placed Occurrence geometry",
                            correction = "Place $name on Sheet ${sheet.sheetId.value} before deriving $groupSubject geometry.",
                            sourceTrace = groupTrace(sheet, groupId, groupSource, listOf(node)),
                        )
                    } else {
                        resolvedOccurrences += occurrence
                    }
                }
            }
        }
        return MemberResolution(
            resolvedNodes = resolvedNodes,
            resolvedOccurrences = resolvedOccurrences,
            diagnostics = diagnostics,
        )
    }

    private fun resolveCompilerOwnedMembers(
        sheet: ProjectionSheet,
        groupSubject: String,
        groupId: String,
        groupSource: GeometryElementId,
        memberNodes: List<ProjectionNode>,
        occurrenceById: Map<SpatialOccurrenceId, SpatialOccurrenceGeometry>,
    ): MemberResolution {
        val resolvedOccurrences = mutableListOf<SpatialOccurrenceGeometry>()
        val diagnostics = mutableListOf<SpatialDiagnostic>()
        memberNodes.forEach { node ->
            val occurrenceId = SpatialOccurrenceId(sheet.sheetId.value, node.projectionId.value)
            val occurrence = occurrenceById[occurrenceId]
            if (occurrence == null) {
                diagnostics += SpatialDiagnostic(
                    subject = "$groupSubject member ${node.label}",
                    problem = "has no placed Occurrence geometry",
                    correction = "Place ${node.label} on Sheet ${sheet.sheetId.value} before deriving $groupSubject geometry.",
                    sourceTrace = groupTrace(sheet, groupId, groupSource, listOf(node)),
                )
            } else {
                resolvedOccurrences += occurrence
            }
        }
        return MemberResolution(
            resolvedNodes = memberNodes,
            resolvedOccurrences = resolvedOccurrences,
            diagnostics = diagnostics,
        )
    }

    private fun outOfAreaDiagnostic(
        subject: String,
        geometryName: String,
        sheet: ProjectionSheet,
        groupId: String,
        groupSource: GeometryElementId,
        memberNodes: List<ProjectionNode>,
        memberOccurrences: List<SpatialOccurrenceGeometry>,
    ): List<SpatialDiagnostic> {
        if (memberOccurrences.isEmpty()) return emptyList()
        val padded = paddedGroupingUnionOrNull(memberOccurrences.map(SpatialOccurrenceGeometry::rectangle))
        if (padded?.isInside(ProjectionSpatialLayout.DRAWING_AREA) == true) return emptyList()
        return listOf(
            SpatialDiagnostic(
                subject = subject,
                problem = "has a ${ProjectionSpatialLayout.GROUPING_PADDING}-unit padded $geometryName outside " +
                    "Drawing Area (${ProjectionSpatialLayout.DRAWING_AREA.x},${ProjectionSpatialLayout.DRAWING_AREA.y}," +
                    "${ProjectionSpatialLayout.DRAWING_AREA.width},${ProjectionSpatialLayout.DRAWING_AREA.height})",
                correction = "Keep every $subject member at least ${ProjectionSpatialLayout.GROUPING_PADDING} units " +
                    "inside the Drawing Area.",
                sourceTrace = groupTrace(sheet, groupId, groupSource, memberNodes),
            ),
        )
    }

    private fun groupTrace(
        sheet: ProjectionSheet,
        groupId: String,
        groupSource: GeometryElementId,
        nodes: List<ProjectionNode> = emptyList(),
    ): SpatialSourceTrace = SpatialSourceTrace(
        projectionIds = listOf(sheet.sheetId.value, groupId) + nodes.map { node -> node.projectionId.value },
        geometryElementIds = listOf(groupSource) + nodes.map(ProjectionNode::originGeometryElementId),
    )

    private fun constructSubject(construct: ProjectionSheetConstruct): String {
        val name = construct.name?.takeIf(String::isNotBlank)
            ?: construct.kind.takeIf(String::isNotBlank)
            ?: construct.constructId.value.takeIf(String::isNotBlank)
            ?: "grouping"
        return "Construct $name"
    }

    private data class MemberResolution(
        val resolvedNodes: List<ProjectionNode> = emptyList(),
        val resolvedOccurrences: List<SpatialOccurrenceGeometry> = emptyList(),
        val diagnostics: List<SpatialDiagnostic>,
    )

    private companion object {
        val occurrenceGeometryOrder = compareBy<SpatialOccurrenceGeometry>(
            { occurrence -> occurrence.occurrenceId.sheetId },
            { occurrence -> occurrence.occurrenceId.projectionId },
            { occurrence -> occurrence.rectangle.x },
            { occurrence -> occurrence.rectangle.y },
            { occurrence -> occurrence.rectangle.width },
            { occurrence -> occurrence.rectangle.height },
            { occurrence -> occurrence.subjectId.value },
            { occurrence -> occurrence.regionId },
            { occurrence -> occurrence.placementReason.text },
            { occurrence -> occurrence.sourceTrace.toString() },
        )
    }
}
