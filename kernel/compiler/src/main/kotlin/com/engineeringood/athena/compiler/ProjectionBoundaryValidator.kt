package com.engineeringood.athena.compiler

import com.engineeringood.athena.projection.ProjectionDocument

/**
 * Projection boundary validation (Story 3.2).
 *
 * A Projection snapshot must not carry spatial or presentation facts: no coordinates, anchors,
 * lanes, routes, strokes, labels, or paint order. Zones are logical document sections only.
 */
object ProjectionBoundaryValidator {
    fun report(document: ProjectionDocument): List<ProjectionBoundaryIssue> {
        val issues = mutableListOf<ProjectionBoundaryIssue>()
        document.sheets.forEach { sheet ->
            sheet.publication.coordinateZones.forEach { zone ->
                if (zone.zoneId.startsWith("layout-") || zone.label.startsWith("Layout")) {
                    issues += ProjectionBoundaryIssue(
                        subject = sheet.sheetId.value,
                        message = "Sheet '${sheet.sheetId.value}' carries layout-box zone '${zone.zoneId}'. Projection zones must be logical document sections, not layout boxes.",
                    )
                }
            }
        }
        return issues
    }
}

/** Plain-language projection boundary violation naming the subject and the problem. */
data class ProjectionBoundaryIssue(
    val subject: String,
    val message: String,
)
