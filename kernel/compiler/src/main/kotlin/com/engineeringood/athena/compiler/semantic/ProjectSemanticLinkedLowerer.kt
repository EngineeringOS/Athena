package com.engineeringood.athena.compiler.semantic

import com.engineeringood.athena.compiler.CompilerSourceDocument
import com.engineeringood.athena.compiler.EngineeringIrLowerer
import com.engineeringood.athena.ir.EngineeringDocument

data class ProjectSemanticLinkedLoweringResult(
    val graphId: GraphId,
    val loweredSourceUnits: List<ProjectSemanticLoweredSourceUnit>,
    val diagnostics: List<ProjectSemanticDiagnostic>,
)

data class ProjectSemanticLoweredSourceUnit(
    val graphId: GraphId,
    val sourceUnitId: SourceUnitId,
    val bindingIds: List<BindingId>,
    val document: EngineeringDocument,
)

class ProjectSemanticLinkedLowerer(
    private val lowerer: EngineeringIrLowerer = EngineeringIrLowerer(),
) {
    fun lower(
        snapshot: ProjectSemanticGraphSnapshot,
        documentsBySourceUnit: Map<SourceUnitId, CompilerSourceDocument>,
    ): ProjectSemanticLinkedLoweringResult {
        val bindingIdsBySourceUnit = snapshot.bindings
            .groupBy { it.sourceUnitId }
            .mapValues { (_, bindings) -> bindings.map { it.bindingId }.sortedBy { it.value } }
        val diagnostics = mutableListOf<ProjectSemanticDiagnostic>()
        val lowered = snapshot.sourceUnits.mapNotNull { sourceUnit ->
            val source = documentsBySourceUnit[sourceUnit.sourceUnitId]
            if (source == null) {
                diagnostics += ProjectSemanticDiagnostic(
                    code = ProjectSemanticDiagnosticCode("semantic.lowering.source.missing"),
                    severity = ProjectSemanticDiagnosticSeverity.ERROR,
                    message = "Missing source document for linked lowering source unit `${sourceUnit.sourceRootRelativePath}`.",
                    sourceUnitId = sourceUnit.sourceUnitId,
                )
                return@mapNotNull null
            }
            ProjectSemanticLoweredSourceUnit(
                graphId = snapshot.graphId,
                sourceUnitId = sourceUnit.sourceUnitId,
                bindingIds = bindingIdsBySourceUnit[sourceUnit.sourceUnitId].orEmpty(),
                document = lowerer.lower(source),
            )
        }.sortedBy { it.sourceUnitId.value }
        return ProjectSemanticLinkedLoweringResult(
            graphId = snapshot.graphId,
            loweredSourceUnits = java.util.List.copyOf(lowered),
            diagnostics = ProjectSemanticGraphSnapshot.canonicalizeDiagnostics(diagnostics),
        )
    }
}
