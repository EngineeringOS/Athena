package com.engineeringood.athena.compiler.semantic

class ProjectSemanticCapabilityProvenanceProjector {
    fun project(
        snapshot: ProjectSemanticGraphSnapshot,
        capabilitiesByPackage: Map<PackageKey, List<String>>,
    ): ProjectSemanticGraphSnapshot {
        val indexedNamespaces = snapshot.namespaces.map { namespace ->
            namespace.copy(
                admittedCapabilities = namespace.admittedCapabilities + capabilitiesByPackage[namespace.packageKey].orEmpty(),
            )
        }
        val diagnostics = indexedNamespaces.flatMap { namespace ->
            namespace.admittedCapabilities.map { capability ->
                ProjectSemanticDiagnostic(
                    code = ProjectSemanticDiagnosticCode("semantic.capability.namespace.available"),
                    severity = ProjectSemanticDiagnosticSeverity.INFO,
                    message = "Namespace `${namespace.qualifiedName.joinToString(".")}` admits governed capability `$capability`.",
                    sourceUnitId = namespace.sourceUnitIds.minByOrNull { it.value },
                )
            }
        }
        return ProjectSemanticGraphSnapshot.canonical(
            snapshot.graphId,
            snapshot.rootPackageId,
            snapshot.packages,
            snapshot.sourceUnits,
            indexedNamespaces,
            snapshot.declarations,
            snapshot.bindings,
            snapshot.diagnostics + diagnostics,
        )
    }
}
