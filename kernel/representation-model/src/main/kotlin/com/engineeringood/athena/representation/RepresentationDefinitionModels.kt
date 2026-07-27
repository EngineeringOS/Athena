package com.engineeringood.athena.representation

@JvmInline
value class RepresentationAnchorId(val value: String) {
    init {
        require(value.isNotBlank()) { "Representation anchor id must not be blank." }
    }
}

@JvmInline
value class RepresentationSignalPredicate(val value: String) {
    init {
        require(value.isNotBlank()) { "Representation signal predicate must not be blank." }
    }
}

@JvmInline
value class RepresentationCompositionChildId(val value: String) {
    init {
        require(value.isNotBlank()) { "Representation composition child id must not be blank." }
    }
}

enum class RepresentationDefinitionKind {
    SYMBOL,
    ELEMENT,
}

enum class RepresentationBodyAuthority {
    GRAPHIC_PRIMITIVE,
    LEGACY_PRESENTATION_ANATOMY,
}

enum class RepresentationAnchorRole {
    TERMINAL,
    REFERENCE,
    PLACEMENT,
    LABEL,
    HOTSPOT,
}

enum class RepresentationDirectionPredicate {
    IN,
    OUT,
    BIDIRECTIONAL,
}

enum class RepresentationDefinitionForbiddenAuthority {
    PROJECT_DEVICE,
    PROJECT_PORT,
    PROJECT_CONNECTION,
    DEVICE_CLASSIFICATION,
    PROJECT_LAYOUT,
}

data class RepresentationAnchorContract(
    val anchorId: RepresentationAnchorId,
    val primitiveId: GraphicPrimitiveId,
    val point: GraphicPoint,
    val role: RepresentationAnchorRole,
    val required: Boolean,
    val acceptedDirections: Set<RepresentationDirectionPredicate> = emptySet(),
    val acceptedSignals: Set<RepresentationSignalPredicate> = emptySet(),
    val terminal: PhysicalTerminalId? = null,
) {
    init {
        require(point.x.isFinite() && point.y.isFinite()) {
            "Representation anchor point coordinates must be finite."
        }
    }
}

data class RepresentationCompositionChild(
    val childId: RepresentationCompositionChildId,
    val symbolId: RepresentationSymbolId,
    val zOrder: Int,
    val transforms: List<GraphicTransform>,
)

data class RepresentationExportedAnchor(
    val anchorId: RepresentationAnchorId,
    val childId: RepresentationCompositionChildId? = null,
    val childAnchorId: RepresentationAnchorId? = null,
    val primitiveId: GraphicPrimitiveId? = null,
) {
    init {
        val childReferenceIsComplete = childId != null && childAnchorId != null && primitiveId == null
        val primitiveReferenceIsComplete = childId == null && childAnchorId == null && primitiveId != null
        require(childReferenceIsComplete || primitiveReferenceIsComplete) {
            "Exported anchor must reference exactly one child anchor or one graphic primitive."
        }
    }
}

data class RepresentationIntrinsicComposition(
    val children: List<RepresentationCompositionChild>,
    val exportedAnchors: List<RepresentationExportedAnchor>,
    val exportedLabelSlots: List<RepresentationExportedLabelSlot> = emptyList(),
)

data class RepresentationExportedLabelSlot(
    val slotId: RepresentationLabelSlotId,
    val childId: RepresentationCompositionChildId,
    val childSlotId: RepresentationLabelSlotId,
)

data class RepresentationProvenance(
    val source: String,
) {
    init {
        require(source.isNotBlank()) { "Representation provenance source must not be blank." }
    }
}

data class RepresentationLifecycle(
    val state: RepresentationLifecycleState,
    val provenance: RepresentationProvenance,
    val supersededBy: RepresentationSymbolId? = null,
    val migrationHint: String? = null,
) {
    init {
        require(migrationHint == null || migrationHint.isNotBlank()) {
            "Representation migration hint must be null or non-blank."
        }
    }
}

data class RepresentationStyleToken(
    val name: String,
    val value: String,
) {
    init {
        require(name.isNotBlank()) { "Representation style token name must not be blank." }
        require(value.isNotBlank()) { "Representation style token value must not be blank." }
    }
}

data class RepresentationLabelSlot(
    val slotId: RepresentationLabelSlotId,
    val role: PresentationLabelRole,
    val origin: GraphicPoint? = null,
    val bounds: GraphicBounds? = null,
    val styleTokenId: GraphicStyleTokenId? = null,
)

data class RepresentationDefinition(
    val symbolId: RepresentationSymbolId,
    val libraryId: RepresentationLibraryId,
    val version: RepresentationVersion,
    val lifecycle: RepresentationLifecycle,
    val kind: RepresentationSymbolKind,
    val anatomy: PresentationAnatomy,
    val labelSlots: List<RepresentationLabelSlot>,
    val variants: List<RepresentationVariantId> = emptyList(),
    val styleTokens: List<RepresentationStyleToken> = emptyList(),
    val bodyAuthority: RepresentationBodyAuthority = RepresentationBodyAuthority.LEGACY_PRESENTATION_ANATOMY,
    val definitionKind: RepresentationDefinitionKind = RepresentationDefinitionKind.SYMBOL,
    val graphicBody: GraphicPrimitiveDocument = GraphicPrimitiveDocument(
        documentId = null,
        bounds = null,
        primitives = emptyList(),
        styleTokens = emptyList(),
    ),
    val anchors: List<RepresentationAnchorContract> = emptyList(),
    val intrinsicComposition: RepresentationIntrinsicComposition? = null,
    val forbiddenAuthorityClaims: Set<RepresentationDefinitionForbiddenAuthority> = emptySet(),
) {
    init {
        require(definitionKind == RepresentationDefinitionKind.ELEMENT || intrinsicComposition == null) {
            "Atomic Symbol definitions cannot own intrinsic child composition."
        }
        when (bodyAuthority) {
            RepresentationBodyAuthority.GRAPHIC_PRIMITIVE -> require(
                anatomy.authority == PresentationAnatomyAuthority.COMPATIBILITY_SHELL,
            ) {
                "Canonical Graphic Primitive definitions require a non-authoritative presentation compatibility shell."
            }
            RepresentationBodyAuthority.LEGACY_PRESENTATION_ANATOMY -> {
                require(labelSlots.isNotEmpty()) { "Legacy representation definition requires at least one label slot." }
                require(anatomy.authority == PresentationAnatomyAuthority.LEGACY_VISUAL) {
                    "Legacy presentation definitions require authoritative legacy anatomy."
                }
                require(graphicBody.primitives.isEmpty() && anchors.isEmpty() && intrinsicComposition == null) {
                    "Legacy presentation definitions cannot own canonical Graphic Primitive or composition facts."
                }
            }
        }
    }

    fun toTransportMap(): Map<String, String> = linkedMapOf(
        "anchorCount" to anchors.size.toString(),
        "bodyAuthority" to bodyAuthority.name,
        "compositionChildCount" to intrinsicComposition?.children?.size.orZero().toString(),
        "definitionKind" to definitionKind.name,
        "graphicPrimitiveCount" to graphicBody.primitives.size.toString(),
        "kind" to kind.name,
        "labelSlotCount" to labelSlots.size.toString(),
        "libraryId" to libraryId.value,
        "lifecycleState" to lifecycle.state.name,
        "symbolId" to symbolId.value,
        "variantCount" to variants.size.toString(),
        "version" to version.value,
    )
}

private fun Int?.orZero(): Int = this ?: 0
