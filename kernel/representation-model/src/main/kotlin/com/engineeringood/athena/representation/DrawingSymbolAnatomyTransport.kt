package com.engineeringood.athena.representation

data class DrawingSymbolAnatomyTransportPayload(
    val identity: String,
    val version: String,
    val packageId: String,
    val domainTags: List<String>,
    val profileTags: List<String>,
    val lifecycle: String,
    val primitives: List<GraphicPrimitiveTransportPayload>,
    val anchors: List<DrawingSymbolAnchorTransportPayload>,
    val labelSlots: List<DrawingSymbolLabelSlotTransportPayload>,
    val referenceSlots: List<DrawingSymbolReferenceSlotTransportPayload>,
    val hotspots: List<DrawingSymbolHotspotTransportPayload>,
    val bounds: DrawingSymbolBoundsTransportPayload?,
    val orientations: List<String>,
    val provenance: DrawingSymbolProvenanceTransportPayload?,
    val forbiddenAuthorityClaims: List<String>,
)

data class DrawingSymbolAnchorTransportPayload(
    val anchorId: String,
    val point: DrawingSymbolPointTransportPayload,
    val role: String,
    val required: Boolean,
    val terminalRole: String?,
)

data class DrawingSymbolLabelSlotTransportPayload(
    val slotId: String,
    val role: String,
    val required: Boolean,
)

data class DrawingSymbolReferenceSlotTransportPayload(
    val slotId: String,
    val role: String,
    val required: Boolean,
)

data class DrawingSymbolHotspotTransportPayload(
    val hotspotId: String,
    val bounds: DrawingSymbolBoundsTransportPayload,
)

data class DrawingSymbolBoundsTransportPayload(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
)

data class DrawingSymbolPointTransportPayload(
    val x: Int,
    val y: Int,
)

data class DrawingSymbolProvenanceTransportPayload(
    val source: String,
    val version: String,
)

fun DrawingSymbolAnatomy.toTransportPayload(): DrawingSymbolAnatomyTransportPayload =
    DrawingSymbolAnatomyTransportPayload(
        identity = identity?.value.orEmpty(),
        version = version?.value.orEmpty(),
        packageId = packageId?.value.orEmpty(),
        domainTags = domainTags.map { it.value }.sorted(),
        profileTags = profileTags.map { it.value }.sorted(),
        lifecycle = lifecycle?.name?.lowercase().orEmpty(),
        primitives = primitives.map { it.toTransportPayload() },
        anchors = anchors.sortedBy { it.anchorId.value }.map { anchor ->
            DrawingSymbolAnchorTransportPayload(
                anchorId = anchor.anchorId.value,
                point = anchor.point.toTransportPayload(),
                role = anchor.role.name.lowercase(),
                required = anchor.required,
                terminalRole = anchor.terminalRole?.value,
            )
        },
        labelSlots = labelSlots.sortedBy { it.slotId.value }.map { slot ->
            DrawingSymbolLabelSlotTransportPayload(slot.slotId.value, slot.role.name.lowercase(), slot.required)
        },
        referenceSlots = referenceSlots.sortedBy { it.slotId.value }.map { slot ->
            DrawingSymbolReferenceSlotTransportPayload(slot.slotId.value, slot.role.name.lowercase(), slot.required)
        },
        hotspots = hotspots.sortedBy { it.hotspotId.value }.map { hotspot ->
            DrawingSymbolHotspotTransportPayload(hotspot.hotspotId.value, hotspot.bounds.toTransportPayload())
        },
        bounds = bounds?.toTransportPayload(),
        orientations = orientations.map { it.name.lowercase() }.sorted(),
        provenance = provenance?.let { DrawingSymbolProvenanceTransportPayload(it.source, it.version) },
        forbiddenAuthorityClaims = forbiddenAuthorityClaims.map { it.name }.sorted(),
    )

private fun DrawingSymbolBounds.toTransportPayload(): DrawingSymbolBoundsTransportPayload =
    DrawingSymbolBoundsTransportPayload(x, y, width, height)

private fun DrawingSymbolPoint.toTransportPayload(): DrawingSymbolPointTransportPayload =
    DrawingSymbolPointTransportPayload(x, y)
