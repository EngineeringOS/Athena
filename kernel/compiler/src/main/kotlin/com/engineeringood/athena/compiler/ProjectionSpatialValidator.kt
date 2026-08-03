package com.engineeringood.athena.compiler

import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.projection.ProjectionSheet
import com.engineeringood.athena.spatial.SpatialDiagnostic
import com.engineeringood.athena.spatial.SpatialSourceTrace

internal class ProjectionSpatialValidator(
    private val planner: ProjectionPlacementPlanner,
) {
    fun validate(
        projection: ProjectionDocument,
        sheets: List<ProjectionSheet>,
    ): List<SpatialDiagnostic> {
        val preflightDiagnostics = (
            duplicateSheetIdentityDiagnostics(sheets) +
                duplicateProjectionIdentityDiagnostics(projection) +
                regionIdentityDiagnostics(sheets) +
                ambiguousConnectionEndpointDiagnostics(projection, sheets)
            ).canonical()
        if (preflightDiagnostics.isNotEmpty()) {
            return preflightDiagnostics
        }
        return (
            occurrenceOwnershipDiagnostics(projection, sheets) +
            ambiguousRegionMembershipDiagnostics(projection, sheets) +
            invalidRegionMembershipDiagnostics(projection, sheets) +
            duplicateRegionMembershipDiagnostics(projection, sheets) +
            horizontalCapacityDiagnostics(projection, sheets) +
            verticalCapacityDiagnostics(projection, sheets)
            ).canonical()
    }

    private fun List<SpatialDiagnostic>.canonical(): List<SpatialDiagnostic> =
        sortedWith(compareBy(SpatialDiagnostic::subject, SpatialDiagnostic::problem))

    private fun duplicateSheetIdentityDiagnostics(
        sheets: List<ProjectionSheet>,
    ): List<SpatialDiagnostic> = sheets.groupBy { sheet -> sheet.sheetId }
        .filterValues { duplicates -> duplicates.size > 1 }
        .entries
        .sortedBy { (sheetId, _) -> sheetId.value }
        .map { (sheetId, duplicates) ->
            SpatialDiagnostic(
                subject = "Sheet identity ${sheetId.value}",
                problem = "is used by ${duplicates.size} Sheets",
                correction = "Give every Sheet a unique identity.",
                sourceTrace = SpatialSourceTrace(
                    projectionIds = listOf(sheetId.value),
                    geometryElementIds = duplicates.map { sheet -> sheet.originGeometryElementId },
                ),
            )
        }

    private fun regionIdentityDiagnostics(
        sheets: List<ProjectionSheet>,
    ): List<SpatialDiagnostic> = sheets.flatMap { sheet ->
        val reservedId = "${sheet.sheetId.value}/region/unassigned"
        val blank = sheet.regions.filter { region -> region.regionId.isBlank() }.map { region ->
            SpatialDiagnostic(
                subject = "Region identity <blank>",
                problem = "is blank",
                correction = "Give every Region a non-blank identity.",
                sourceTrace = SpatialSourceTrace(
                    projectionIds = listOf(sheet.sheetId.value),
                    geometryElementIds = listOf(region.originGeometryElementId),
                ),
            )
        }
        val duplicate = sheet.regions.groupBy { region -> region.regionId }
            .filter { (regionId, regions) -> regionId.isNotBlank() && regions.size > 1 }
            .entries
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
        val reserved = sheet.regions.filter { region -> region.regionId == reservedId }.map { region ->
            SpatialDiagnostic(
                subject = "Region identity $reservedId",
                problem = "is reserved for compiler-owned unassigned Occurrences",
                correction = "Give the authored Region a different identity.",
                sourceTrace = SpatialSourceTrace(
                    projectionIds = listOf(sheet.sheetId.value, reservedId),
                    geometryElementIds = listOf(region.originGeometryElementId),
                ),
            )
        }
        blank + duplicate + reserved
    }

    private fun ambiguousConnectionEndpointDiagnostics(
        projection: ProjectionDocument,
        sheets: List<ProjectionSheet>,
    ): List<SpatialDiagnostic> = sheets.flatMap { sheet ->
        val aliases = projection.nodes.filter { node -> planner.sheetOwns(sheet, node) }
            .flatMap { node ->
                listOf(node.projectionId.value, node.semanticId.value, node.label).map { alias -> alias to node }
            }
            .groupBy(keySelector = { (alias, _) -> alias }, valueTransform = { (_, node) -> node })
        projection.connections.flatMap { connection ->
            listOfNotNull(connection.sourceOccurrenceId, connection.targetOccurrenceId)
        }.distinct().mapNotNull { endpointId ->
            val matches = aliases[endpointId].orEmpty().distinctBy { node -> node.projectionId }
            if (matches.size <= 1) return@mapNotNull null
            SpatialDiagnostic(
                subject = "Connection endpoint $endpointId",
                problem = "matches ${matches.size} Occurrences on Sheet ${sheet.sheetId.value}",
                correction = "Reference the endpoint by its unique Projection occurrence identity.",
                sourceTrace = SpatialSourceTrace(
                    projectionIds = listOf(sheet.sheetId.value, endpointId) +
                        matches.map { node -> node.projectionId.value },
                    geometryElementIds = matches.map { node -> node.originGeometryElementId },
                ),
            )
        }
    }

    private fun duplicateProjectionIdentityDiagnostics(
        projection: ProjectionDocument,
    ): List<SpatialDiagnostic> = projection.nodes
        .groupBy { node -> node.projectionId }
        .entries
        .filter { (_, nodes) -> nodes.size > 1 }
        .sortedBy { (projectionId, _) -> projectionId.value }
        .map { (projectionId, nodes) ->
            val orderedNodes = nodes.sortedBy { node -> node.originGeometryElementId.value }
            SpatialDiagnostic(
                subject = "Projection occurrence identity ${projectionId.value}",
                problem = "is used by ${nodes.size} projected Occurrences",
                correction = "Give every projected Occurrence a unique Projection identity.",
                sourceTrace = SpatialSourceTrace(
                    projectionIds = listOf(projectionId.value),
                    geometryElementIds = orderedNodes.map { node -> node.originGeometryElementId },
                ),
            )
        }

    private fun occurrenceOwnershipDiagnostics(
        projection: ProjectionDocument,
        sheets: List<ProjectionSheet>,
    ): List<SpatialDiagnostic> = projection.nodes
        .sortedBy { node -> node.projectionId.value }
        .mapNotNull { node ->
            val owners = sheets.filter { sheet -> planner.sheetOwns(sheet, node) }
            if (owners.size == 1) {
                return@mapNotNull null
            }
            SpatialDiagnostic(
                subject = "Occurrence ${node.label}",
                problem = "resolves to ${owners.size} owning Sheets",
                correction = "Reference ${node.label} from exactly one Sheet subject list.",
                sourceTrace = SpatialSourceTrace(
                    projectionIds = listOf(node.projectionId.value) + owners.map { sheet -> sheet.sheetId.value },
                    geometryElementIds = listOf(node.originGeometryElementId),
                ),
            )
        }

    private fun ambiguousRegionMembershipDiagnostics(
        projection: ProjectionDocument,
        sheets: List<ProjectionSheet>,
    ): List<SpatialDiagnostic> = sheets.flatMap { sheet ->
        sheet.regions.flatMap { region ->
            region.occurrenceNames.distinct().mapNotNull { name ->
                val matches = projection.nodes
                    .filter { node -> planner.sheetOwns(sheet, node) && node.label == name }
                    .sortedBy { node -> node.projectionId.value }
                if (matches.size <= 1) {
                    return@mapNotNull null
                }
                SpatialDiagnostic(
                    subject = "Region ${region.name} member $name",
                    problem = "matches ${matches.size} projected Occurrences",
                    correction = "Give each projected Occurrence a unique label before assigning it to a Region.",
                    sourceTrace = SpatialSourceTrace(
                        projectionIds = listOf(sheet.sheetId.value, region.regionId) +
                            matches.map { node -> node.projectionId.value },
                        geometryElementIds = matches.map { node -> node.originGeometryElementId },
                    ),
                )
            }
        }
    }

    private fun duplicateRegionMembershipDiagnostics(
        projection: ProjectionDocument,
        sheets: List<ProjectionSheet>,
    ): List<SpatialDiagnostic> = sheets.flatMap { sheet ->
        val duplicateEntries = sheet.regions.flatMap { region ->
            region.occurrenceNames.groupingBy { name -> name }.eachCount().entries
                .filter { (_, count) -> count > 1 }
                .sortedBy { (name, _) -> name }
                .mapNotNull { (name, _) ->
                    val node = projection.nodes.singleOrNull { candidate ->
                        candidate.label == name && planner.sheetOwns(sheet, candidate)
                    } ?: return@mapNotNull null
                    SpatialDiagnostic(
                        subject = "Occurrence $name",
                        problem = "is listed more than once in Region ${region.name} on Sheet ${sheet.sheetId.value}",
                        correction = "List $name once in Region ${region.name}.",
                        sourceTrace = SpatialSourceTrace(
                            projectionIds = listOf(
                                sheet.sheetId.value,
                                region.regionId,
                                node.projectionId.value,
                            ),
                            geometryElementIds = listOf(region.originGeometryElementId, node.originGeometryElementId),
                        ),
                    )
                }
        }
        val membershipsByName = sheet.regions
            .flatMap { region -> region.occurrenceNames.distinct().map { name -> name to region.regionId } }
            .groupBy(keySelector = { (name, _) -> name }, valueTransform = { (_, regionId) -> regionId })
        val multipleRegions = membershipsByName.entries
            .filter { (_, regionIds) -> regionIds.distinct().size > 1 }
            .sortedBy { (name, _) -> name }
            .mapNotNull { (name, regionIds) ->
                val node = projection.nodes.singleOrNull { candidate ->
                    candidate.label == name && planner.sheetOwns(sheet, candidate)
                } ?: return@mapNotNull null
                SpatialDiagnostic(
                    subject = "Occurrence $name",
                    problem = "is assigned to more than one Region on Sheet ${sheet.sheetId.value}",
                    correction = "List $name in exactly one Region on that Sheet.",
                    sourceTrace = SpatialSourceTrace(
                        projectionIds = listOf(sheet.sheetId.value, node.projectionId.value) +
                            regionIds.distinct().sorted(),
                        geometryElementIds = listOf(node.originGeometryElementId),
                    ),
                )
            }
        duplicateEntries + multipleRegions
    }

    private fun invalidRegionMembershipDiagnostics(
        projection: ProjectionDocument,
        sheets: List<ProjectionSheet>,
    ): List<SpatialDiagnostic> = sheets.flatMap { sheet ->
        sheet.regions.flatMap { region ->
            region.occurrenceNames.distinct().mapNotNull { name ->
                val matches = projection.nodes.filter { node -> node.label == name }
                    .sortedBy { node -> node.projectionId.value }
                val ownedMatches = matches.filter { node -> planner.sheetOwns(sheet, node) }
                when {
                    matches.isEmpty() -> SpatialDiagnostic(
                        subject = "Region ${region.name} member $name",
                        problem = "does not resolve to a projected Occurrence",
                        correction = "Name an Occurrence owned by Sheet ${sheet.sheetId.value} in Region ${region.name}.",
                        sourceTrace = SpatialSourceTrace(
                            projectionIds = listOf(sheet.sheetId.value, region.regionId),
                            geometryElementIds = listOf(region.originGeometryElementId),
                        ),
                    )
                    ownedMatches.isEmpty() -> {
                        val owningSheets = sheets.filter { candidate ->
                            matches.any { node -> planner.sheetOwns(candidate, node) }
                        }.sortedBy { owner -> owner.sheetId.value }
                        if (owningSheets.isEmpty()) {
                            return@mapNotNull SpatialDiagnostic(
                                subject = "Region ${region.name} member $name",
                                problem = "is not owned by any Sheet",
                                correction = "Reference $name from exactly one Sheet before assigning it to Region ${region.name}.",
                                sourceTrace = SpatialSourceTrace(
                                    projectionIds = listOf(sheet.sheetId.value, region.regionId) +
                                        matches.map { node -> node.projectionId.value },
                                    geometryElementIds = listOf(region.originGeometryElementId) +
                                        matches.map { node -> node.originGeometryElementId },
                                ),
                            )
                        }
                        SpatialDiagnostic(
                            subject = "Region ${region.name} member $name",
                            problem = "belongs to ${owningSheets.joinToString { owner -> "Sheet ${owner.sheetId.value}" }}, " +
                                "not Sheet ${sheet.sheetId.value}",
                            correction = "List $name only in a Region on its owning Sheet.",
                            sourceTrace = SpatialSourceTrace(
                                projectionIds = listOf(sheet.sheetId.value, region.regionId) +
                                    owningSheets.map { owner -> owner.sheetId.value } +
                                    matches.map { node -> node.projectionId.value },
                                geometryElementIds = listOf(region.originGeometryElementId) +
                                    matches.map { node -> node.originGeometryElementId },
                            ),
                        )
                    }
                    else -> null
                }
            }
        }
    }

    private fun verticalCapacityDiagnostics(
        projection: ProjectionDocument,
        sheets: List<ProjectionSheet>,
    ): List<SpatialDiagnostic> {
        val availableTopRange = ProjectionSpatialLayout.DRAWING_AREA.height -
            ProjectionSpatialLayout.GROUPING_PADDING * 2 -
            ProjectionSpatialLayout.NODE_HEIGHT
        val requiredTopStep = ProjectionSpatialLayout.NODE_HEIGHT + ProjectionSpatialLayout.OCCURRENCE_SEPARATION
        return sheets.flatMap { sheet ->
            planner.placementGroups(sheet, projection).mapNotNull { group ->
                if (group.nodes.size <= 1 || availableTopRange / (group.nodes.size - 1) >= requiredTopStep) {
                    return@mapNotNull null
                }
                SpatialDiagnostic(
                    subject = "Region ${group.regionName}",
                    problem = "cannot place ${group.nodes.size} Occurrences with " +
                        "${ProjectionSpatialLayout.OCCURRENCE_SEPARATION}-unit vertical separation " +
                        "inside the Drawing Area",
                    correction = "Split Region ${group.regionName} or reduce its Occurrence count.",
                    sourceTrace = SpatialSourceTrace(
                        projectionIds = listOf(group.regionId) + group.nodes.map { node -> node.projectionId.value },
                        geometryElementIds = group.nodes.map { node -> node.originGeometryElementId },
                    ),
                )
            }
        }
    }

    private fun horizontalCapacityDiagnostics(
        projection: ProjectionDocument,
        sheets: List<ProjectionSheet>,
    ): List<SpatialDiagnostic> = sheets.mapNotNull { sheet ->
        val groups = planner.placementGroups(sheet, projection)
        val requiredWidth = groups.size.toLong() * ProjectionSpatialLayout.NODE_WIDTH +
            (groups.size - 1).coerceAtLeast(0).toLong() * ProjectionSpatialLayout.REGION_GUTTER
        if (groups.isEmpty() || requiredWidth <= ProjectionSpatialLayout.DRAWING_AREA.width.toLong()) {
            return@mapNotNull null
        }
        SpatialDiagnostic(
            subject = "Sheet ${sheet.sheetId.value}",
            problem = "cannot fit ${groups.size} Region columns with ${ProjectionSpatialLayout.NODE_WIDTH}-unit " +
                "Occurrences and ${ProjectionSpatialLayout.REGION_GUTTER}-unit gutters inside the Drawing Area",
            correction = "Reduce the Sheet Region count or move Regions to another Sheet.",
            sourceTrace = SpatialSourceTrace(
                projectionIds = listOf(sheet.sheetId.value) + groups.flatMap { group ->
                    listOf(group.regionId) + group.nodes.map { node -> node.projectionId.value }
                },
                geometryElementIds = groups.flatMap { group ->
                    group.nodes.map { node -> node.originGeometryElementId }
                },
            ),
        )
    }
}
