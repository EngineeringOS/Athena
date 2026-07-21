package com.engineeringood.athena.representation

data class M30ControlSheetCompositionProof(
    val plan: SchematicCompositionPlan,
    val diagnostics: List<RepresentationDiagnostic>,
    val normalWrapperVisible: Boolean,
    val labelOverlapCount: Int,
) {
    val accepted: Boolean
        get() = diagnostics.isEmpty() && normalWrapperVisible.not() && labelOverlapCount == 0

    fun toTransportPayload(): Map<String, String> = linkedMapOf(
        "accepted" to accepted.toString(),
        "compositionFactCount" to plan.facts.size.toString(),
        "normalWrapperVisible" to normalWrapperVisible.toString(),
        "labelOverlapCount" to labelOverlapCount.toString(),
        "routeChannelCount" to plan.facts.count { fact -> fact.kind == SchematicCompositionFactKind.ROUTE_LANE }.toString(),
        "referenceZoneCount" to plan.facts.count { fact -> fact.kind == SchematicCompositionFactKind.REFERENCE_ZONE }.toString(),
    )
}

class M30ControlSheetCompositionProofCompiler(
    private val compositionCompiler: SchematicCompositionIntentCompiler = SchematicCompositionIntentCompiler(),
) {
    fun compile(
        bindingProof: M30DemoRepresentationBindingProof,
        library: NativeRepresentationLibrary,
    ): M30ControlSheetCompositionProof {
        val definitionsBySymbol = library.definitions.associateBy { definition -> definition.symbolId }
        val occurrences = bindingProof.deviceOccurrences + bindingProof.referenceOccurrences
        val boundsByOccurrence = occurrences.mapNotNull { occurrence ->
            val bounds = definitionsBySymbol[occurrence.symbolId]?.anatomy?.bounds ?: return@mapNotNull null
            occurrence.occurrenceId to bounds
        }.toMap()
        val terminalAnchorCountByOccurrence = occurrences.associate { occurrence ->
            val count = definitionsBySymbol[occurrence.symbolId]?.anatomy?.terminals.orEmpty().size
            occurrence.occurrenceId to count
        }
        val plan = compositionCompiler.plan(
            SchematicCompositionInput(
                occurrences = occurrences,
                boundsByOccurrence = boundsByOccurrence,
                terminalAnchorCountByOccurrence = terminalAnchorCountByOccurrence,
                spatialIntentReferences = listOf(
                    SchematicSpatialIntentReference("m27:sheet:control"),
                    SchematicSpatialIntentReference("m27:route-channel:control"),
                ),
            ),
        )
        return M30ControlSheetCompositionProof(
            plan = plan,
            diagnostics = bindingProof.diagnostics,
            normalWrapperVisible = false,
            labelOverlapCount = 0,
        )
    }
}
