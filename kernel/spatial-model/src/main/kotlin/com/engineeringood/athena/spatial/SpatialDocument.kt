package com.engineeringood.athena.spatial

import com.engineeringood.athena.ir.RealityDeclaration
import com.engineeringood.athena.ir.RealityIdentityRule
import java.util.Collections

/**
 * Concrete root for Spatial Reality.
 *
 * Spatial Reality turns projected engineering content into geometry. Routing is a subsystem of this
 * reality, so lanes and routes live here with placement, bounds, anchors, and alignment.
 */
class SpatialDocument(sheets: List<SpatialSheet>) {
    val sheets: List<SpatialSheet> = sheets.immutableDocumentCopy()

    init {
        require(this.sheets.isNotEmpty()) { "Spatial document must contain at least one Sheet." }
    }

    override fun equals(other: Any?): Boolean =
        this === other || other is SpatialDocument && sheets == other.sheets

    override fun hashCode(): Int = sheets.hashCode()

    override fun toString(): String = "SpatialDocument(sheets=$sheets)"
}

class SpatialSheet(
    val sheetId: String,
    val extent: SpatialRect,
    val drawingArea: SpatialRect,
    val grid: SpatialGridDefinition,
    occurrences: List<SpatialOccurrenceGeometry>,
    regions: List<SpatialRegionGeometry>,
    constructs: List<SpatialConstructGeometry>,
    alignments: List<SpatialAlignment>,
    anchors: List<SpatialAnchorPosition>,
    lanes: List<SpatialLane>,
    routes: List<SpatialRoute>,
    gridReferences: List<SpatialGridReference>,
    val quality: SpatialQualitySnapshot,
    val sourceTrace: SpatialSourceTrace,
) {
    val occurrences: List<SpatialOccurrenceGeometry> = occurrences.immutableDocumentCopy()
    val regions: List<SpatialRegionGeometry> = regions.immutableDocumentCopy()
    val constructs: List<SpatialConstructGeometry> = constructs.immutableDocumentCopy()
    val alignments: List<SpatialAlignment> = alignments.immutableDocumentCopy()
    val anchors: List<SpatialAnchorPosition> = anchors.immutableDocumentCopy()
    val lanes: List<SpatialLane> = lanes.immutableDocumentCopy()
    val routes: List<SpatialRoute> = routes.immutableDocumentCopy()
    val gridReferences: List<SpatialGridReference> = gridReferences.immutableDocumentCopy()

    init {
        require(sheetId.isNotBlank()) { "Spatial Sheet identity must not be blank." }
    }

    fun copy(
        sheetId: String = this.sheetId,
        extent: SpatialRect = this.extent,
        drawingArea: SpatialRect = this.drawingArea,
        grid: SpatialGridDefinition = this.grid,
        occurrences: List<SpatialOccurrenceGeometry> = this.occurrences,
        regions: List<SpatialRegionGeometry> = this.regions,
        constructs: List<SpatialConstructGeometry> = this.constructs,
        alignments: List<SpatialAlignment> = this.alignments,
        anchors: List<SpatialAnchorPosition> = this.anchors,
        lanes: List<SpatialLane> = this.lanes,
        routes: List<SpatialRoute> = this.routes,
        gridReferences: List<SpatialGridReference> = this.gridReferences,
        quality: SpatialQualitySnapshot = this.quality,
        sourceTrace: SpatialSourceTrace = this.sourceTrace,
    ): SpatialSheet = SpatialSheet(
        sheetId,
        extent,
        drawingArea,
        grid,
        occurrences,
        regions,
        constructs,
        alignments,
        anchors,
        lanes,
        routes,
        gridReferences,
        quality,
        sourceTrace,
    )

    override fun equals(other: Any?): Boolean =
        this === other || other is SpatialSheet &&
            sheetId == other.sheetId &&
            extent == other.extent &&
            drawingArea == other.drawingArea &&
            grid == other.grid &&
            occurrences == other.occurrences &&
            regions == other.regions &&
            constructs == other.constructs &&
            alignments == other.alignments &&
            anchors == other.anchors &&
            lanes == other.lanes &&
            routes == other.routes &&
            gridReferences == other.gridReferences &&
            quality == other.quality &&
            sourceTrace == other.sourceTrace

    override fun hashCode(): Int = listOf(
        sheetId,
        extent,
        drawingArea,
        grid,
        occurrences,
        regions,
        constructs,
        alignments,
        anchors,
        lanes,
        routes,
        gridReferences,
        quality,
        sourceTrace,
    ).hashCode()

    override fun toString(): String =
        "SpatialSheet(sheetId=$sheetId, extent=$extent, drawingArea=$drawingArea, grid=$grid, " +
            "occurrences=$occurrences, regions=$regions, constructs=$constructs, alignments=$alignments, " +
            "anchors=$anchors, lanes=$lanes, routes=$routes, gridReferences=$gridReferences, " +
            "quality=$quality, sourceTrace=$sourceTrace)"
}

object SpatialReality {
    const val name: String = "Spatial Reality"
    const val rootName: String = "SpatialDocument"
    const val purpose: String = "Turns view-specific projection facts into geometry."
    const val authority: String = "spatial compiler"

    val ownedFacts: List<String> = listOf(
        "occurrence geometry",
        "Region geometry",
        "Construct geometry",
        "anchor position",
        "alignment",
        "lane",
        "route",
        "grid definition",
        "Grid Reference",
        "quality snapshot",
    )

    val identityRules: List<RealityIdentityRule> = listOf(
        RealityIdentityRule("occurrence geometry", "Occurrence identity names its owning Sheet and Projection occurrence."),
        RealityIdentityRule("Region geometry", "Region identity names its owning Sheet and Projection Region."),
        RealityIdentityRule("Construct geometry", "Construct identity names its owning Sheet and Projection Construct."),
        RealityIdentityRule("anchor position", "Anchor identity names its owning Sheet, typed Occurrence, and semantic port."),
        RealityIdentityRule("lane", "Lane identity is spatial-local and owned by the spatial compiler."),
        RealityIdentityRule("route", "Route identity traces to the projection connection identity plus route id."),
        RealityIdentityRule("grid definition", "Grid identity names its owning Sheet and Projection grid."),
        RealityIdentityRule("Grid Reference", "Grid Reference identity names its owning Sheet and typed subject."),
        RealityIdentityRule("quality snapshot", "Quality snapshot identity names its owning Sheet and contributing Spatial facts."),
    )

    val requiredFacts: List<String> = listOf(
        "occurrence geometry identity",
        "Region geometry identity",
        "anchor position identity",
        "lane identity",
        "route identity",
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

    fun validate(document: SpatialDocument): SpatialValidationResult = SpatialValidation.validate(document)
}

private fun <T> List<T>.immutableDocumentCopy(): List<T> = Collections.unmodifiableList(toList())
