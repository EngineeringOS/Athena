package com.engineeringood.athena.presentation

import com.engineeringood.athena.ir.RealityDeclaration
import com.engineeringood.athena.ir.RealityIdentityRule
import com.engineeringood.athena.ir.RealityValidationIssue
import com.engineeringood.athena.ir.RealityValidationResult

object PresentationReality {
    const val name: String = "Presentation Reality"
    const val rootName: String = "PresentationDocument"
    const val purpose: String = "Turns spatial facts into paintable drawing facts."
    const val authority: String = "presentation compiler"

    val ownedFacts: List<String> = listOf(
        "shape",
        "connector visual",
        "stroke",
        "label",
        "visibility",
        "theme result",
        "paint order",
    )

    val identityRules: List<RealityIdentityRule> = listOf(
        RealityIdentityRule("shape", "Shape identity traces to spatial geometry plus paint item id."),
        RealityIdentityRule("connector visual", "Connector visual identity traces to the spatial route plus paint item id."),
        RealityIdentityRule("label", "Label identity traces to the spatial or projection target plus label id."),
        RealityIdentityRule("paint order", "Paint order comes from presentation layer and drawing composition."),
    )

    val requiredFacts: List<String> = listOf(
        "paint target",
        "visibility",
        "paint order",
    )

    val declaration: RealityDeclaration = RealityDeclaration(
        name = name,
        rootName = rootName,
        purpose = purpose,
        authority = authority,
        ownedFacts = ownedFacts,
        identityRules = identityRules,
        requiredFacts = requiredFacts,
    )

    fun validate(document: PresentationDocument): RealityValidationResult {
        val hasPaintableFacts = document.occurrences.isNotEmpty() ||
            document.connectors.isNotEmpty() ||
            document.connectionMarkers.isNotEmpty() ||
            document.referenceMarkers.isNotEmpty() ||
            document.representationFacts.isNotEmpty()
        val issues = buildList {
            if (document.occurrences.any { occurrence -> occurrence.semanticId.value.isBlank() }) {
                add(RealityValidationIssue(name, "missing paint target"))
            }
            if (document.connectors.any { connector -> connector.semanticId.value.isBlank() }) {
                add(RealityValidationIssue(name, "missing paint target"))
            }
            if (document.referenceMarkers.any { marker -> marker.sourceProjectionIds.isEmpty() }) {
                add(RealityValidationIssue(name, "missing paint target"))
            }
            if (document.connectors.flatMap { connector -> connector.labels }.any { label -> label.targetId.isBlank() }) {
                add(RealityValidationIssue(name, "missing paint target"))
            }
            if (hasPaintableFacts && document.paintPlan == null) {
                add(RealityValidationIssue(name, "missing visibility and paint order"))
            }
        }
        return RealityValidationResult(issues)
    }
}
