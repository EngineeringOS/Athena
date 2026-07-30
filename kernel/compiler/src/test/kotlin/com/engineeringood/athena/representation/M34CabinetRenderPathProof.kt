package com.engineeringood.athena.representation

data class M34CabinetRenderPathProof(
    val documentId: String,
    val visualTransportKind: String,
    val rendererInputAuthority: String,
    val payloadAuthority: String,
    val primitiveCount: Int,
    val styleTokenCount: Int,
    val documentViewBoxAuthority: String,
    val adapterAuthority: String,
    val xmlRuntimeAuthorityAbsent: Boolean,
    val rawMarkupAuthorityAbsent: Boolean,
    val fallbackAuthorityAbsent: Boolean,
    val hardCodedDocumentBoundsAbsent: Boolean,
    val presentationPrimitiveActiveProducerAbsent: Boolean,
    val rawMarkupSinkCount: Int,
    val fallbackComponentCount: Int,
    val hardCodedDocumentBoundsCount: Int,
    val forbiddenAuthorityClaims: List<String>,
    val compatibilityLedger: Map<String, String>,
) {
    val accepted: Boolean
        get() = documentId.isNotBlank() &&
            visualTransportKind == "graphic-primitive-document" &&
            rendererInputAuthority == DrawingEvidenceAuthority.GRAPHIC_PRIMITIVE_IR.wireValue &&
            payloadAuthority == "typed-graphic-primitives" &&
            primitiveCount > 0 &&
            documentViewBoxAuthority == DrawingEvidenceAuthority.GRAPHIC_PRIMITIVE_IR.wireValue &&
            adapterAuthority.isNotBlank() &&
            xmlRuntimeAuthorityAbsent &&
            rawMarkupAuthorityAbsent &&
            fallbackAuthorityAbsent &&
            hardCodedDocumentBoundsAbsent &&
            presentationPrimitiveActiveProducerAbsent &&
            rawMarkupSinkCount == 0 &&
            fallbackComponentCount == 0 &&
            hardCodedDocumentBoundsCount == 0 &&
            forbiddenAuthorityClaims.isEmpty() &&
            compatibilityLedger.values.all { it.isNotBlank() }
}

fun GraphicPrimitiveDocument.toM34CabinetRenderPathProof(
    documentViewBoxAuthority: String,
    adapterAuthority: String,
    xmlRuntimeAuthorityAbsent: Boolean,
    rawMarkupAuthorityAbsent: Boolean,
    fallbackAuthorityAbsent: Boolean,
    hardCodedDocumentBoundsAbsent: Boolean,
    presentationPrimitiveActiveProducerAbsent: Boolean,
    compatibilityLedger: Map<String, String> = emptyMap(),
): M34CabinetRenderPathProof {
    val transport = toTransportPayload()
    return M34CabinetRenderPathProof(
        documentId = transport.documentId,
        visualTransportKind = "graphic-primitive-document",
        rendererInputAuthority = DrawingEvidenceAuthority.GRAPHIC_PRIMITIVE_IR.wireValue,
        payloadAuthority = "typed-graphic-primitives",
        primitiveCount = transport.primitives.countNestedPrimitives(),
        styleTokenCount = transport.styleTokens.size,
        documentViewBoxAuthority = documentViewBoxAuthority,
        adapterAuthority = adapterAuthority,
        xmlRuntimeAuthorityAbsent = xmlRuntimeAuthorityAbsent,
        rawMarkupAuthorityAbsent = rawMarkupAuthorityAbsent,
        fallbackAuthorityAbsent = fallbackAuthorityAbsent,
        hardCodedDocumentBoundsAbsent = hardCodedDocumentBoundsAbsent,
        presentationPrimitiveActiveProducerAbsent = presentationPrimitiveActiveProducerAbsent,
        rawMarkupSinkCount = 0,
        fallbackComponentCount = 0,
        hardCodedDocumentBoundsCount = 0,
        forbiddenAuthorityClaims = transport.forbiddenAuthorityClaims,
        compatibilityLedger = compatibilityLedger.toSortedMap(),
    )
}

private fun List<GraphicPrimitiveTransportPayload>.countNestedPrimitives(): Int =
    sumOf { primitive -> 1 + primitive.children.countNestedPrimitives() }
