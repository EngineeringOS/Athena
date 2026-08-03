package com.engineeringood.athena.compiler

import com.engineeringood.athena.presentation.PresentationDocument
import com.engineeringood.athena.presentation.PresentationPaintItem
import com.engineeringood.athena.presentation.PresentationPaintPlan

class PresentationPaintCompiler {
    fun compile(document: PresentationDocument): PresentationPaintPlan {
        val graphicOccurrenceIds = document.graphicOccurrences.map { occurrence -> occurrence.occurrenceId.value }.toSet()
        val shapeTargets = document.graphicOccurrences.map { occurrence -> occurrence.occurrenceId.value } +
            document.occurrences
                .filterNot { occurrence -> occurrence.occurrenceId.value in graphicOccurrenceIds }
                .map { occurrence -> occurrence.occurrenceId.value }
        val shapeItems = shapeTargets.mapIndexed { index, target ->
            paintItem(
                id = "paint:item:shape:$target",
                target = target,
                kind = "shape",
                order = 10 + index,
            )
        }
        val connectorItems = document.connectors.mapIndexed { index, connector ->
            paintItem(
                id = "paint:item:connector:${connector.occurrenceId.value}",
                target = connector.occurrenceId.value,
                kind = "connector",
                order = 20 + index,
            )
        }
        val markerItems = document.connectionMarkers.mapIndexed { index, marker ->
            paintItem(
                id = "paint:item:marker:${marker.markerId.value}",
                target = marker.markerId.value,
                kind = "marker",
                order = 30 + index,
            )
        }
        val labelItems = document.connectors.flatMap { connector ->
            connector.labels.map { label -> connector.occurrenceId.value to label }
        }.mapIndexed { index, (_, label) ->
            require(label.point.x >= 0 && label.point.y >= 0) {
                "Presentation connector label position must be explicit and non-negative."
            }
            paintItem(
                id = "paint:item:label:${label.labelId}",
                target = label.labelId,
                kind = "label",
                order = 40 + index,
            )
        }
        return PresentationPaintPlan(shapeItems + connectorItems + markerItems + labelItems)
    }

    private fun paintItem(
        id: String,
        target: String,
        kind: String,
        order: Int,
    ): PresentationPaintItem =
        PresentationPaintItem(
            itemId = id,
            targetId = target,
            kind = kind,
            visible = true,
            order = order,
        )
}
