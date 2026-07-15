package com.engineeringood.athena.compiler.semantic

import com.engineeringood.athena.language.SourceSpan

@JvmInline
value class ProjectSemanticDiagnosticCode(val value: String) {
    init {
        require(value.isNotBlank()) { "Project semantic diagnostic code must not be blank" }
    }
}

enum class ProjectSemanticDiagnosticSeverity {
    ERROR,
    WARNING,
    INFO,
}

data class ProjectSemanticRelatedLocation(
    val sourceUnitId: SourceUnitId,
    val sourceSpan: SourceSpan,
    val message: String? = null,
)

data class ProjectSemanticDiagnostic(
    val code: ProjectSemanticDiagnosticCode,
    val severity: ProjectSemanticDiagnosticSeverity,
    val message: String,
    val sourceUnitId: SourceUnitId? = null,
    val sourceSpan: SourceSpan? = null,
    val relatedLocations: List<ProjectSemanticRelatedLocation> = emptyList(),
) {
    init {
        require(message.isNotBlank()) { "Project semantic diagnostic message must not be blank" }
        require(sourceSpan == null || sourceUnitId != null) { "A diagnostic source span requires a source unit id" }
    }
}
