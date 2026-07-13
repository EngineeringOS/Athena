package com.engineeringood.athena.domain.electricalruntime

import com.engineeringood.athena.layout.ElectricalProjectionDescriptor
import com.engineeringood.athena.layout.ElectricalProjectionFamily
import com.engineeringood.athena.layout.LayoutIntent
import com.engineeringood.athena.layout.ProjectionInteractivity
import com.engineeringood.athena.layout.ProjectionOwnershipContract
import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.layout.ViewEmphasis

internal val ELECTRICAL_RUNTIME_VIEW_DEFINITIONS = listOf(
    ViewDefinition(
        id = "cabinet",
        displayName = "Cabinet",
        layoutIntent = LayoutIntent.STRUCTURAL,
        groupingRules = listOf("group-by-owner", "group-by-component"),
        viewEmphasis = listOf(ViewEmphasis.OWNERSHIP, ViewEmphasis.PLACEMENT),
        description = "Highlights structural placement and ownership relationships for electrical devices.",
        ownershipContract = ProjectionOwnershipContract(
            interactivity = ProjectionInteractivity.INTERACTIVE,
            displayScopes = listOf(
                "devices",
                "ports",
                "ownership-relationships",
                "connectivity-relationships",
                "grouped-placement",
                "electrical-anchors",
                "electrical-routing-corridors",
            ),
            semanticCommandIds = listOf("connect-ports"),
            projectionCommandIds = listOf(
                "adjust-layout-placement",
                "adjust-layout-grouping",
            ),
            transientInteractionKinds = listOf(
                "navigate-view",
                "inspect-selection",
                "preview-related-elements",
            ),
            persistedProjectionMetadataKeys = listOf(
                "layout-placement",
                "layout-group-membership",
            ),
        ),
        familyContract = ElectricalProjectionDescriptor(
            family = ElectricalProjectionFamily.CABINET,
        ),
    ),
    ViewDefinition(
        id = "wiring",
        displayName = "Wiring",
        layoutIntent = LayoutIntent.CONNECTIVITY,
        groupingRules = listOf("group-by-signal", "group-by-connection-path"),
        viewEmphasis = listOf(ViewEmphasis.CONNECTIVITY, ViewEmphasis.SIGNAL_FLOW),
        description = "Highlights compatible signal flow and connection relationships between ports.",
        ownershipContract = ProjectionOwnershipContract(
            interactivity = ProjectionInteractivity.INSPECT_ONLY,
            displayScopes = listOf(
                "devices",
                "ports",
                "signal-groups",
                "connectivity-relationships",
                "electrical-anchors",
                "electrical-routing-corridors",
            ),
            transientInteractionKinds = listOf(
                "navigate-view",
                "inspect-selection",
                "preview-related-elements",
            ),
        ),
        familyContract = ElectricalProjectionDescriptor(
            family = ElectricalProjectionFamily.WIRING,
        ),
    ),
    ViewDefinition(
        id = "schematic",
        displayName = "Schematic",
        layoutIntent = LayoutIntent.CONNECTIVITY,
        groupingRules = listOf("group-by-signal", "group-by-component"),
        viewEmphasis = listOf(ViewEmphasis.CONNECTIVITY, ViewEmphasis.SIGNAL_FLOW),
        description = "Highlights canonical electrical connectivity as a schematic-oriented presentation family.",
        ownershipContract = ProjectionOwnershipContract(
            interactivity = ProjectionInteractivity.INSPECT_ONLY,
            displayScopes = listOf(
                "devices",
                "ports",
                "connectivity-relationships",
                "signal-groups",
                "canonical-connectivity",
                "electrical-anchors",
                "electrical-routing-corridors",
            ),
            transientInteractionKinds = listOf(
                "navigate-view",
                "inspect-selection",
                "preview-related-elements",
            ),
        ),
        familyContract = ElectricalProjectionDescriptor(
            family = ElectricalProjectionFamily.SCHEMATIC,
        ),
    ),
    ViewDefinition(
        id = "documentation",
        displayName = "Documentation",
        layoutIntent = LayoutIntent.STRUCTURAL,
        groupingRules = listOf("group-by-component", "group-by-owner"),
        viewEmphasis = listOf(ViewEmphasis.OWNERSHIP, ViewEmphasis.CONNECTIVITY),
        description = "Summarizes canonical electrical subjects for documentation-oriented inspection.",
        ownershipContract = ProjectionOwnershipContract(
            interactivity = ProjectionInteractivity.INSPECT_ONLY,
            displayScopes = listOf(
                "devices",
                "ports",
                "ownership-relationships",
                "connectivity-relationships",
                "documentation-summary",
                "electrical-anchors",
                "electrical-routing-corridors",
            ),
            transientInteractionKinds = listOf(
                "navigate-view",
                "inspect-selection",
                "preview-related-elements",
            ),
        ),
        familyContract = ElectricalProjectionDescriptor(
            family = ElectricalProjectionFamily.DOCUMENTATION,
        ),
    ),
)
