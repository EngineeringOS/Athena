package com.engineeringood.athena.representation

data class DrawingSymbolPrimitiveCompilationRequest(
    val descriptorId: String,
    val resourceHandle: String,
    val anatomy: DrawingSymbolAnatomy,
    val styleTokens: List<GraphicStyleToken>,
)

data class DrawingSymbolPrimitiveCompilationProof(
    val descriptorId: String,
    val symbolId: String,
    val resourceHandle: String,
    val primitiveIds: List<String>,
    val primitiveKinds: List<String>,
    val anchorIds: List<String>,
    val labelSlotIds: List<String>,
    val referenceSlotIds: List<String>,
    val styleTokenIds: List<String>,
    val bounds: GraphicBounds,
)

data class DrawingSymbolPrimitiveCompilationDiagnostic(
    val code: String,
    val subject: String,
    val message: String,
)

data class DrawingSymbolPrimitiveCompilationResult(
    val document: GraphicPrimitiveDocument?,
    val proof: DrawingSymbolPrimitiveCompilationProof?,
    val diagnostics: List<DrawingSymbolPrimitiveCompilationDiagnostic>,
) {
    val isValid: Boolean
        get() = diagnostics.isEmpty() && document != null && proof != null
}

class DrawingSymbolPrimitiveCompiler(
    private val supportedPrimitiveKinds: Set<GraphicPrimitiveKind> = GraphicPrimitiveKind.values().toSet(),
) {
    fun compile(request: DrawingSymbolPrimitiveCompilationRequest): DrawingSymbolPrimitiveCompilationResult {
        val diagnostics = mutableListOf<DrawingSymbolPrimitiveCompilationDiagnostic>()
        diagnostics += DrawingSymbolAnatomyValidator.validate(request.anatomy).diagnostics.map { diagnostic ->
            DrawingSymbolPrimitiveCompilationDiagnostic(diagnostic.code.wireValue, diagnostic.subject, diagnostic.message)
        }
        if (request.descriptorId.isBlank()) {
            diagnostics += diagnostic("drawing.symbol.compile.descriptor-id.invalid", "descriptorId", "Descriptor id must not be blank.")
        }
        if (request.resourceHandle.isBlank()) {
            diagnostics += diagnostic(
                "drawing.symbol.compile.resource-handle.invalid",
                "resourceHandle",
                "Resolved Graphic Resource handle must not be blank.",
            )
        }

        val anatomyBounds = request.anatomy.bounds
        if (diagnostics.isNotEmpty() || anatomyBounds == null) {
            return failed(diagnostics)
        }
        val bounds = GraphicBounds(
            anatomyBounds.x.toDouble(),
            anatomyBounds.y.toDouble(),
            anatomyBounds.width.toDouble(),
            anatomyBounds.height.toDouble(),
        )
        val document = GraphicPrimitiveDocument(
            documentId = GraphicPrimitiveDocumentId(request.descriptorId),
            bounds = bounds,
            primitives = request.anatomy.primitives,
            styleTokens = request.styleTokens,
        )
        diagnostics += GraphicPrimitiveIrValidator.validate(document).diagnostics.map { diagnostic ->
            DrawingSymbolPrimitiveCompilationDiagnostic(diagnostic.code.wireValue, diagnostic.subject, diagnostic.message)
        }
        if (diagnostics.isNotEmpty()) {
            return failed(diagnostics)
        }

        val flattened = request.anatomy.primitives.flatMap(::flatten)
        val unsupported = flattened.map { it.kind }.filterNot { it in supportedPrimitiveKinds }.distinct().sortedBy { it.wireValue }
        unsupported.forEach { kind ->
            diagnostics += diagnostic(
                "drawing.symbol.compile.primitive-kind.unsupported",
                kind.wireValue,
                "Graphic primitive kind `${kind.wireValue}` is unsupported by the active compilation target.",
            )
        }
        if (diagnostics.isNotEmpty()) {
            return failed(diagnostics)
        }

        val proof = DrawingSymbolPrimitiveCompilationProof(
            descriptorId = request.descriptorId,
            symbolId = requireNotNull(request.anatomy.identity).value,
            resourceHandle = request.resourceHandle,
            primitiveIds = flattened.map { it.primitiveId.value },
            primitiveKinds = flattened.map { it.kind.wireValue },
            anchorIds = request.anatomy.anchors.map { it.anchorId.value }.sorted(),
            labelSlotIds = request.anatomy.labelSlots.map { it.slotId.value }.sorted(),
            referenceSlotIds = request.anatomy.referenceSlots.map { it.slotId.value }.sorted(),
            styleTokenIds = request.styleTokens.map { it.styleTokenId.value }.sorted(),
            bounds = bounds,
        )
        return DrawingSymbolPrimitiveCompilationResult(document, proof, emptyList())
    }

    private fun flatten(primitive: GraphicPrimitive): List<GraphicPrimitive> = when (primitive) {
        is GraphicPrimitive.Group -> listOf(primitive) + primitive.children.flatMap(::flatten)
        is GraphicPrimitive.Transformed -> listOf(primitive) + flatten(primitive.child)
        else -> listOf(primitive)
    }

    private fun failed(
        diagnostics: List<DrawingSymbolPrimitiveCompilationDiagnostic>,
    ): DrawingSymbolPrimitiveCompilationResult = DrawingSymbolPrimitiveCompilationResult(
        document = null,
        proof = null,
        diagnostics = diagnostics.distinct().sortedWith(compareBy({ it.code }, { it.subject }, { it.message })),
    )

    private fun diagnostic(code: String, subject: String, message: String) =
        DrawingSymbolPrimitiveCompilationDiagnostic(code, subject, message)
}
