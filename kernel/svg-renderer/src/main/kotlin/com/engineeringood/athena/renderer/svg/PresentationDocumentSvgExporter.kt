package com.engineeringood.athena.renderer.svg

import com.engineeringood.athena.presentation.PresentationConnectionMarker
import com.engineeringood.athena.presentation.PresentationConnector
import com.engineeringood.athena.presentation.PresentationConnectorLabel
import com.engineeringood.athena.presentation.PresentationDocument
import com.engineeringood.athena.presentation.PresentationGraphicOccurrence
import com.engineeringood.athena.presentation.PresentationOccurrence
import com.engineeringood.athena.presentation.PresentationPaintItem
import com.engineeringood.athena.presentation.PresentationPoint

/** Exports the compiler-produced Presentation Document without lowering or repairing connections. */
class PresentationDocumentSvgExporter {
    fun export(document: PresentationDocument): String {
        val paintPlan = requireNotNull(document.paintPlan) {
            "Presentation Document requires paint plan before SVG export."
        }
        val paintItems = paintPlan.items.filter { item -> item.visible }
        val labelsById = document.connectors
            .flatMap { connector -> connector.labels.map { label -> label.labelId to (connector.occurrenceId.value to label) } }
            .toMap()
        val graphicOccurrencesById = document.graphicOccurrences.associateBy { occurrence -> occurrence.occurrenceId.value }
        val occurrencesById = document.occurrences.associateBy { occurrence -> occurrence.occurrenceId.value }
        val connectorsById = document.connectors.associateBy { connector -> connector.occurrenceId.value }
        val markersById = document.connectionMarkers.associateBy { marker -> marker.markerId.value }
        return buildString {
            appendLine(
                """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ${document.canvasWidth} ${document.canvasHeight}" width="${document.canvasWidth}" height="${document.canvasHeight}">""",
            )
            appendLine("""  <title>${document.view.displayName.escapeXml()}</title>""")
            paintItems.forEach { item ->
                when (item.kind) {
                    "shape" -> appendLine(
                        graphicOccurrencesById[item.targetId]?.toSvg(item)
                            ?: occurrencesById[item.targetId]?.toSvg(item)
                            ?: return@forEach,
                    )
                    "connector" -> appendLine(connectorsById[item.targetId]?.toSvg(item) ?: return@forEach)
                    "marker" -> appendLine(markersById[item.targetId]?.toSvg(item) ?: return@forEach)
                    "label" -> {
                        val (connectorId, label) = labelsById[item.targetId] ?: return@forEach
                        appendLine(label.toSvg(connectorId, item))
                    }
                }
            }
            append("""</svg>""")
        }
    }
}

private fun PresentationGraphicOccurrence.toSvg(item: PresentationPaintItem): String =
    """  <rect x="${bounds.x}" y="${bounds.y}" width="${bounds.width}" height="${bounds.height}" class="component" data-occurrence-id="${occurrenceId.value.escapeXml()}" data-subject="${semanticSubjectId.escapeXml()}" data-definition-id="${definitionId.escapeXml()}" data-paint-order="${item.order}" />"""

private fun PresentationOccurrence.toSvg(item: PresentationPaintItem): String =
    """  <rect x="${bounds.x}" y="${bounds.y}" width="${bounds.width}" height="${bounds.height}" class="component" data-occurrence-id="${occurrenceId.value.escapeXml()}" data-subject="${semanticId.value.escapeXml()}" data-paint-order="${item.order}" />"""

private fun PresentationConnector.toSvg(item: PresentationPaintItem): String =
    """  <polyline points="${routePoints.svgPoints()}" class="connection ${line.lineStyleId.escapeXml()}" data-connection-id="${semanticId.value.escapeXml()}" data-occurrence-id="${occurrenceId.value.escapeXml()}" data-source-anchor="${sourceEndpoint.anchorId.value.escapeXml()}" data-target-anchor="${targetEndpoint.anchorId.value.escapeXml()}" data-line-class="${line.classId.escapeXml()}" data-route-id="${routeId.escapeXml()}" data-trace="${sourceProjectionIds.joinToString("|").escapeXml()}" data-paint-order="${item.order}" />"""

private fun PresentationConnectorLabel.toSvg(connectorId: String, item: PresentationPaintItem): String =
    """  <text x="${point.x}" y="${point.y}" class="connection-label" data-connector-id="${connectorId.escapeXml()}" data-label-id="${labelId.escapeXml()}" data-label-class="${labelClassId.escapeXml()}" data-paint-order="${item.order}">${text.escapeXml()}</text>"""

private fun PresentationConnectionMarker.toSvg(item: PresentationPaintItem): String =
    """  <path data-athena-marker-kind="${kind.name.lowercase().escapeXml()}" data-marker-id="${markerId.value.escapeXml()}" data-connectors="${connectorIds.joinToString("|") { it.value }.escapeXml()}" data-paint-order="${item.order}" d="M ${point.x - 4} ${point.y - 4} L ${point.x + 4} ${point.y + 4} M ${point.x + 4} ${point.y - 4} L ${point.x - 4} ${point.y + 4}" class="connection-marker ${appearanceClassId.escapeXml()}" />"""

private fun List<PresentationPoint>.svgPoints(): String =
    joinToString(separator = " ") { point -> "${point.x},${point.y}" }

private fun String.escapeXml(): String {
    return buildString(length) {
        this@escapeXml.forEach { character ->
            append(
                when (character) {
                    '&' -> "&amp;"
                    '<' -> "&lt;"
                    '>' -> "&gt;"
                    '"' -> "&quot;"
                    '\'' -> "&apos;"
                    else -> character
                },
            )
        }
    }
}
