package com.engineeringood.athena.compiler.semantic

class ProjectSemanticDiagnosticProjector {
    fun project(snapshot: ProjectSemanticGraphSnapshot): ProjectSemanticGraphSnapshot {
        val importDiagnostics = snapshot.sourceUnits.flatMap { sourceUnit ->
            sourceUnit.resolvedImports.mapNotNull { resolution ->
                resolution.toDiagnostic(sourceUnit.sourceUnitId)
            }
        }
        return ProjectSemanticGraphSnapshot.canonical(
            snapshot.graphId,
            snapshot.rootPackageId,
            snapshot.packages,
            snapshot.sourceUnits,
            snapshot.namespaces,
            snapshot.declarations,
            snapshot.bindings,
            snapshot.diagnostics + importDiagnostics,
        )
    }

    private fun ProjectSemanticImportResolution.toDiagnostic(sourceUnitId: SourceUnitId): ProjectSemanticDiagnostic? {
        val target = importDeclaration.target.parts.joinToString(".")
        val code = when (status) {
            ProjectSemanticImportResolutionStatus.RESOLVED -> return null
            ProjectSemanticImportResolutionStatus.UNAVAILABLE_PACKAGE -> "semantic.import.package.unavailable"
            ProjectSemanticImportResolutionStatus.UNAVAILABLE_NAMESPACE -> "semantic.import.namespace.unavailable"
            ProjectSemanticImportResolutionStatus.AMBIGUOUS_NAMESPACE -> "semantic.import.namespace.ambiguous"
        }
        val message = when (status) {
            ProjectSemanticImportResolutionStatus.RESOLVED -> error("Resolved imports are not diagnostics")
            ProjectSemanticImportResolutionStatus.UNAVAILABLE_PACKAGE ->
                "Import `$target` resolves to a package outside the source package and direct governed dependencies."

            ProjectSemanticImportResolutionStatus.UNAVAILABLE_NAMESPACE ->
                "Import `$target` does not match an available semantic namespace."

            ProjectSemanticImportResolutionStatus.AMBIGUOUS_NAMESPACE ->
                "Import `$target` matches multiple available semantic namespaces."
        }
        return ProjectSemanticDiagnostic(
            code = ProjectSemanticDiagnosticCode(code),
            severity = ProjectSemanticDiagnosticSeverity.ERROR,
            message = message,
            sourceUnitId = sourceUnitId,
            sourceSpan = importDeclaration.target.span,
        )
    }
}
