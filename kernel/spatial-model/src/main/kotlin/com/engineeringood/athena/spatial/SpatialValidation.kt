package com.engineeringood.athena.spatial

import java.util.Collections

class SpatialValidationResult(diagnostics: List<SpatialDiagnostic>) {
    val diagnostics: List<SpatialDiagnostic> = Collections.unmodifiableList(diagnostics.toList())
    val isValid: Boolean
        get() = diagnostics.isEmpty()
}

object SpatialValidation {
    fun validate(document: SpatialDocument): SpatialValidationResult {
        val diagnostics = buildList {
            addAll(duplicateSheetDiagnostics(document.sheets))
            document.sheets.forEach { sheet ->
                addAll(requiredFactDiagnostics(sheet))
                addAll(ownershipDiagnostics(sheet))
                addAll(identityDiagnostics(sheet))
                addAll(geometryDiagnostics(sheet))
                addAll(groupingDiagnostics(sheet))
                addAll(gridDiagnostics(sheet))
                addAll(qualityDiagnostics(sheet))
                addAll(spatialTraceDiagnostics(sheet))
                addAll(spatialRoutingDiagnostics(sheet))
                addAll(annotationDiagnostics(sheet))
            }
        }.canonicalSpatialDiagnostics()
        return SpatialValidationResult(diagnostics)
    }

    private fun duplicateSheetDiagnostics(sheets: List<SpatialSheet>): List<SpatialDiagnostic> =
        sheets.groupBy(SpatialSheet::sheetId)
            .filterValues { matches -> matches.size > 1 }
            .map { (sheetId, matches) ->
                spatialIssue(
                    subject = "Sheet $sheetId",
                    problem = "has ${matches.size} facts with the same identity",
                    correction = "Publish exactly one Spatial Sheet for each Projection Sheet.",
                    traces = matches.map(SpatialSheet::sourceTrace),
                )
            }

    private fun requiredFactDiagnostics(sheet: SpatialSheet): List<SpatialDiagnostic> = buildList {
        if (sheet.occurrences.isEmpty()) {
            add(
                SpatialDiagnostic(
                    subject = "Sheet ${sheet.sheetId}",
                    problem = "has no Occurrence geometry facts",
                    correction = "Publish every projected Occurrence on Sheet ${sheet.sheetId} before Presentation.",
                    sourceTrace = sheet.sourceTrace,
                ),
            )
        }
        if (sheet.regions.isEmpty()) {
            add(
                SpatialDiagnostic(
                    subject = "Sheet ${sheet.sheetId}",
                    problem = "has no Region geometry facts",
                    correction = "Publish every projected Region on Sheet ${sheet.sheetId} before Presentation.",
                    sourceTrace = sheet.sourceTrace,
                ),
            )
        }
    }

    private fun ownershipDiagnostics(sheet: SpatialSheet): List<SpatialDiagnostic> = buildList {
        if (sheet.grid.sheetId != sheet.sheetId) {
            add(sheet.foreignIssue("grid ${sheet.grid.gridId}", sheet.grid.sheetId, sheet.grid.sourceTrace))
        }
        if (sheet.quality.sheetId != sheet.sheetId) {
            add(sheet.foreignIssue("quality snapshot", sheet.quality.sheetId, sheet.quality.sourceTrace))
        }
        sheet.occurrences.filter { it.sheetId != sheet.sheetId }.forEach {
            add(sheet.foreignIssue("Occurrence ${it.occurrenceId.projectionId}", it.sheetId, it.sourceTrace))
        }
        sheet.regions.filter { it.sheetId != sheet.sheetId }.forEach {
            add(sheet.foreignIssue("Region ${it.regionId.projectionId}", it.sheetId, it.sourceTrace))
        }
        sheet.constructs.filter { it.sheetId != sheet.sheetId }.forEach {
            add(sheet.foreignIssue("Construct ${it.constructId.projectionId}", it.sheetId, it.sourceTrace))
        }
        sheet.alignments.filter { it.sheetId != sheet.sheetId }.forEach {
            add(sheet.foreignIssue("alignment ${it.alignmentId}", it.sheetId, it.sourceTrace))
        }
        sheet.anchors.filter { it.sheetId != sheet.sheetId }.forEach {
            add(sheet.foreignIssue("Anchor ${it.anchorId.value}", it.sheetId, it.sourceTrace))
        }
        sheet.lanes.filter { it.sheetId != sheet.sheetId }.forEach {
            add(sheet.foreignIssue("Lane ${it.laneId.value}", it.sheetId, sheet.sourceTrace))
        }
        sheet.routes.filter { it.sheetId != sheet.sheetId }.forEach {
            add(sheet.foreignIssue("Route ${it.routeId.value}", it.sheetId, it.sourceTrace))
        }
        sheet.gridReferences.filter { it.sheetId != sheet.sheetId }.forEach {
            add(sheet.foreignIssue("Grid Reference ${it.cellReference}", it.sheetId, it.sourceTrace))
        }
        sheet.annotations.filter { it.sheetId != sheet.sheetId }.forEach {
            add(sheet.foreignIssue("Annotation ${it.id.value}", it.sheetId, it.sourceTrace))
        }
    }

    private fun identityDiagnostics(sheet: SpatialSheet): List<SpatialDiagnostic> = buildList {
        addAll(duplicateFacts(sheet.occurrences, SpatialOccurrenceGeometry::occurrenceId) { fact ->
            Triple(
                "Occurrence ${fact.occurrenceId.projectionId} on Sheet ${fact.sheetId}",
                fact.sourceTrace,
                "Publish exactly one Occurrence geometry fact for this Sheet-qualified identity.",
            )
        })
        addAll(duplicateFacts(sheet.regions, SpatialRegionGeometry::regionId) { fact ->
            Triple(
                "Region ${fact.regionId.projectionId} on Sheet ${fact.sheetId}",
                fact.sourceTrace,
                "Publish exactly one Region geometry fact for this Sheet-qualified identity.",
            )
        })
        addAll(duplicateFacts(sheet.constructs, SpatialConstructGeometry::constructId) { fact ->
            Triple(
                "Construct ${fact.constructId.projectionId} on Sheet ${fact.sheetId}",
                fact.sourceTrace,
                "Publish exactly one Construct geometry fact for this Sheet-qualified identity.",
            )
        })
        addAll(duplicateFacts(sheet.alignments, SpatialAlignment::alignmentId) { fact ->
            Triple(
                alignmentSubject(fact.constraintSource),
                fact.sourceTrace,
                "Publish exactly one alignment fact for this Sheet-qualified grouping source.",
            )
        })
        addAll(duplicateFacts(sheet.gridReferences, SpatialGridReference::gridReferenceId) { fact ->
            Triple(
                gridReferenceSubject(fact.subject),
                fact.sourceTrace,
                "Publish exactly one Grid Reference for this Sheet-qualified subject.",
            )
        })
        addAll(duplicateFacts(sheet.annotations, ConnectionAnnotation::id) { fact ->
            Triple(
                "Annotation ${fact.id.value} on Sheet ${fact.sheetId}",
                fact.sourceTrace,
                "Publish exactly one Connection Annotation for this Sheet-qualified identity.",
            )
        })
    }

    private fun geometryDiagnostics(sheet: SpatialSheet): List<SpatialDiagnostic> = buildList {
        if (!sheet.drawingArea.isInside(sheet.extent)) {
            add(
                SpatialDiagnostic(
                    subject = "Sheet ${sheet.sheetId}",
                    problem = "Drawing Area ${sheet.drawingArea.text()} is outside extent ${sheet.extent.text()}",
                    correction = "Keep the complete Drawing Area inside the owning Sheet extent.",
                    sourceTrace = sheet.sourceTrace,
                ),
            )
        }
        sheet.occurrences.filterNot { it.rectangle.isInside(sheet.drawingArea) }.forEach { occurrence ->
            add(
                SpatialDiagnostic(
                    subject = occurrenceSubject(occurrence),
                    problem = "rectangle is outside Drawing Area ${sheet.drawingArea.text()}",
                    correction = "Place the complete Occurrence rectangle inside its owning Sheet Drawing Area.",
                    sourceTrace = occurrence.sourceTrace,
                ),
            )
        }
        sheet.regions.filterNot { it.bounds.isInside(sheet.drawingArea) }.forEach { region ->
            add(
                SpatialDiagnostic(
                    subject = regionSubject(region),
                    problem = "bounds are outside Drawing Area ${sheet.drawingArea.text()}",
                    correction = "Keep the complete Region bounds inside the owning Sheet Drawing Area.",
                    sourceTrace = region.sourceTrace,
                ),
            )
        }
        sheet.constructs.filterNot { it.envelope.isInside(sheet.drawingArea) }.forEach { construct ->
            add(
                SpatialDiagnostic(
                    subject = constructSubject(construct),
                    problem = "envelope is outside Drawing Area ${sheet.drawingArea.text()}",
                    correction = "Keep the complete Construct envelope inside the owning Sheet Drawing Area.",
                    sourceTrace = construct.sourceTrace,
                ),
            )
        }
        sheet.annotations.filterNot { it.bounds.isInside(sheet.drawingArea) }.forEach { annotation ->
            add(
                SpatialDiagnostic(
                    subject = "Annotation ${annotation.id.value}",
                    problem = "label bounds are outside Drawing Area ${sheet.drawingArea.text()}",
                    correction = "Move the selected annotation completely inside its Sheet Drawing Area.",
                    sourceTrace = annotation.sourceTrace,
                ),
            )
        }
    }

    private fun qualityDiagnostics(sheet: SpatialSheet): List<SpatialDiagnostic> = buildList {
        qualityRatioDiagnostic(sheet, "Density", sheet.quality.metrics.density)?.let(::add)
        qualityRatioDiagnostic(sheet, "Occupancy", sheet.quality.metrics.occupancy)?.let(::add)
    }

    private fun annotationDiagnostics(sheet: SpatialSheet): List<SpatialDiagnostic> = buildList {
        val rectangles = sheet.annotations
        rectangles.forEachIndexed { index, annotation ->
            if (sheet.routes.none { it.connectionId == annotation.semanticId }) {
                add(
                    SpatialDiagnostic(
                        subject = "Annotation ${annotation.id.value}",
                        problem = "semantic subject has no matching placed Connection Route",
                        correction = "Publish one same-Sheet Route for ${annotation.semanticId.value} or remove the annotation.",
                        sourceTrace = annotation.sourceTrace,
                    ),
                )
            }
            rectangles.drop(index + 1).filter { annotation.bounds.overlapsStrict(it.bounds) }.forEach { other ->
                add(
                    SpatialDiagnostic(
                        subject = "Annotations ${annotation.id.value} and ${other.id.value}",
                        problem = "label bounds overlap",
                        correction = "Move one annotation to a free logical anchor before publishing the Sheet.",
                        sourceTrace = annotation.sourceTrace,
                    ),
                )
            }
            val routeCollision = sheet.routes.any { route -> annotation.bounds.intersectsRoute(route.segments) }
            if (routeCollision) {
                add(
                    SpatialDiagnostic(
                        subject = "Annotation ${annotation.id.value}",
                        problem = "label bounds intersect a connection route",
                        correction = "Move the annotation away from route linework before publishing the Sheet.",
                        sourceTrace = annotation.sourceTrace,
                    ),
                )
            }
        }
    }

    private fun qualityRatioDiagnostic(
        sheet: SpatialSheet,
        metricName: String,
        value: Double,
    ): SpatialDiagnostic? {
        val problem = when {
            !value.isFinite() -> "$metricName value $value is not finite"
            value < 0.0 -> "$metricName value $value is negative"
            else -> return null
        }
        return SpatialDiagnostic(
            subject = "Quality snapshot on Sheet ${sheet.sheetId}",
            problem = problem,
            correction = "Compute $metricName from the owning Sheet Drawing Area without clamping or coercion.",
            sourceTrace = sheet.quality.sourceTrace,
        )
    }

}

private fun SpatialRect.overlapsStrict(other: SpatialRect): Boolean =
    x < other.right && right > other.x && y < other.bottom && bottom > other.y

private fun SpatialRect.intersectsRoute(segments: List<ConnectionRouteSegment>): Boolean = segments.any { segment ->
    when (segment.orientation) {
        SpatialLaneOrientation.HORIZONTAL -> segment.start.y > y && segment.start.y < bottom &&
            maxOf(minOf(segment.start.x, segment.end.x), x) < minOf(maxOf(segment.start.x, segment.end.x), right)
        SpatialLaneOrientation.VERTICAL -> segment.start.x > x && segment.start.x < right &&
            maxOf(minOf(segment.start.y, segment.end.y), y) < minOf(maxOf(segment.start.y, segment.end.y), bottom)
        null -> false
    }
}
