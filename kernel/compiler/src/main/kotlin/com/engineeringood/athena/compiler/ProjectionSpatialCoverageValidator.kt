package com.engineeringood.athena.compiler

import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.spatial.SpatialAnchorPosition
import com.engineeringood.athena.spatial.SpatialConstructGeometry
import com.engineeringood.athena.spatial.SpatialDiagnostic
import com.engineeringood.athena.spatial.SpatialDocument
import com.engineeringood.athena.spatial.SpatialGridReference
import com.engineeringood.athena.spatial.SpatialOccurrenceGeometry
import com.engineeringood.athena.spatial.SpatialRegionGeometry
import com.engineeringood.athena.spatial.SpatialRoute

internal class ProjectionSpatialCoverageValidator(
    planner: ProjectionPlacementPlanner = ProjectionPlacementPlanner(),
) {
    private val inventory = ProjectionSpatialCoverageInventory(planner)
    private val authorityValidator = ProjectionSpatialAuthorityValidator(planner, inventory)

    fun validate(projection: ProjectionDocument, spatial: SpatialDocument): List<SpatialDiagnostic> {
        val expectedSheets = projection.sheets.distinctBy { sheet -> sheet.sheetId }
        val actualSheetsById = spatial.sheets.groupBy { sheet -> sheet.sheetId }
        return buildList {
            expectedSheets.forEach { sheet ->
                val actualCount = actualSheetsById[sheet.sheetId.value].orEmpty().size
                if (actualCount != 1) {
                    add(
                        SpatialDiagnostic(
                            subject = "Sheet ${sheet.sheetId.value}",
                            problem = "has $actualCount final Spatial Sheet roots",
                            correction = "Publish exactly one Spatial Sheet for every Projection Sheet.",
                            sourceTrace = sheetTrace(sheet),
                        ),
                    )
                }
            }
            actualSheetsById.filterKeys { sheetId -> expectedSheets.none { it.sheetId.value == sheetId } }
                .forEach { (sheetId, sheets) ->
                    add(
                        SpatialDiagnostic(
                            subject = "Sheet $sheetId",
                            problem = "has ${sheets.size} final Spatial Sheet roots but no matching Projection Sheet",
                            correction = "Remove Spatial Sheet roots not required by canonical Projection coverage.",
                            sourceTrace = combinedCoverageTrace(sheets.map { sheet -> sheet.sourceTrace }),
                        ),
                    )
                }

            expectedSheets.forEach { sheet ->
                val actualSheets = actualSheetsById[sheet.sheetId.value].orEmpty()
                val occurrences = actualSheets.flatMap { it.occurrences }
                val regions = actualSheets.flatMap { it.regions }
                val constructs = actualSheets.flatMap { it.constructs }
                val anchors = actualSheets.flatMap { it.anchors }
                val routes = actualSheets.flatMap { it.routes }
                val gridReferences = actualSheets.flatMap { it.gridReferences }
                val occurrenceExpectations = inventory.occurrenceExpectations(projection, sheet)
                val regionExpectations = inventory.regionExpectations(projection, sheet)
                val constructExpectations = inventory.constructExpectations(projection, sheet)
                val anchorExpectations = inventory.anchorExpectations(projection, sheet)
                val routeExpectations = inventory.routeExpectations(projection, sheet)
                val gridReferenceExpectations = inventory.gridReferenceExpectations(
                    occurrenceExpectations,
                    constructExpectations,
                )

                addAll(
                    exactCoverage(
                        expectations = occurrenceExpectations,
                        actual = occurrences,
                        actualKey = SpatialOccurrenceGeometry::occurrenceId,
                        actualSubject = { occurrence ->
                            "Occurrence ${occurrence.occurrenceId.projectionId} on Sheet ${occurrence.sheetId}"
                        },
                        actualTrace = SpatialOccurrenceGeometry::sourceTrace,
                        factName = "geometry",
                        expectedCorrection =
                            "Publish exactly one Sheet-qualified Occurrence for every projected Sheet subject.",
                    ),
                )
                addAll(
                    exactCoverage(
                        expectations = regionExpectations,
                        actual = regions,
                        actualKey = SpatialRegionGeometry::regionId,
                        actualSubject = { region ->
                            "Region ${region.regionId.projectionId} on Sheet ${region.sheetId}"
                        },
                        actualTrace = SpatialRegionGeometry::sourceTrace,
                        factName = "geometry",
                        expectedCorrection =
                            "Publish exactly one Sheet-qualified Region for every Projection placement group.",
                    ),
                )
                addAll(
                    exactCoverage(
                        expectations = constructExpectations,
                        actual = constructs,
                        actualKey = SpatialConstructGeometry::constructId,
                        actualSubject = { construct ->
                            "Construct ${construct.constructId.projectionId} on Sheet ${construct.sheetId}"
                        },
                        actualTrace = SpatialConstructGeometry::sourceTrace,
                        factName = "geometry",
                        expectedCorrection =
                            "Publish exactly one Sheet-qualified Construct for every projected Construct.",
                    ),
                )
                addAll(
                    exactCoverage(
                        expectations = anchorExpectations,
                        actual = anchors,
                        actualKey = SpatialAnchorPosition::anchorId,
                        actualSubject = { anchor -> "Anchor ${anchor.anchorId.value}" },
                        actualTrace = SpatialAnchorPosition::sourceTrace,
                        factName = "Anchor",
                        expectedCorrection =
                            "Publish exactly one Sheet-qualified Anchor for every referenced projected occurrence-port.",
                    ),
                )
                addAll(
                    exactCoverage(
                        expectations = routeExpectations,
                        actual = routes,
                        actualKey = SpatialRoute::routeId,
                        actualSubject = { route -> "Route ${route.routeId.value}" },
                        actualTrace = SpatialRoute::sourceTrace,
                        factName = "Route",
                        expectedCorrection =
                            "Publish exactly one Sheet-qualified Route for every visible Projection Connection.",
                    ),
                )
                addAll(
                    exactCoverage(
                        expectations = gridReferenceExpectations,
                        actual = gridReferences,
                        actualKey = { reference -> reference.subject },
                        actualSubject = { reference -> gridReferenceSubject(reference.subject) },
                        actualTrace = SpatialGridReference::sourceTrace,
                        factName = "Grid Reference",
                        expectedCorrection =
                            "Publish exactly one Sheet-qualified Grid Reference for every projected Occurrence and Construct.",
                    ),
                )
                addAll(authorityValidator.validate(projection, sheet, actualSheets, occurrences, routes))
            }
        }.canonicalCoverageDiagnostics()
    }
}
