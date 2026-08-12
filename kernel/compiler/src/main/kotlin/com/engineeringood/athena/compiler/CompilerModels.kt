package com.engineeringood.athena.compiler

import com.engineeringood.athena.connection.ConnectionDocument
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.language.SheetCompanionSource
import com.engineeringood.athena.language.SourceFileAst
import com.engineeringood.athena.plugin.AthenaDomainValidationAttribution
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.semantics.core.SemanticDiagnostic
import com.engineeringood.athena.semantics.core.SemanticValidationResult
import com.engineeringood.athena.spatial.SpatialDocument
import java.util.Collections

/** Syntax-owned source document handed back through the compiler facade for downstream passes. */
data class CompilerSourceDocument(
    val file: String,
    val ast: SourceFileAst,
)

/** Parse result exposed by the compiler facade. */
sealed interface CompilerParseResult

/** Successful compiler parse that preserves the full syntax-only source document. */
data class CompilerParseSuccess(
    val source: CompilerSourceDocument,
) : CompilerParseResult

/** Failed compiler parse containing diagnostics safe to expose outside `language`. */
data class CompilerParseFailure(
    val diagnostics: List<CompilerSyntaxDiagnostic>,
) : CompilerParseResult

/** Lowering result exposed by the compiler facade. */
sealed interface CompilerLoweringResult

/** Successful lowering that preserves both the syntax authority and canonical engineering IR. */
data class CompilerLoweringSuccess(
    val source: CompilerSourceDocument,
    val document: com.engineeringood.athena.ir.EngineeringDocument,
) : CompilerLoweringResult

/** Failed lowering containing the diagnostics that prevented semantic lowering from running. */
data class CompilerLoweringFailure(
    val diagnostics: List<CompilerSyntaxDiagnostic>,
) : CompilerLoweringResult

/** Failed lowering containing inspectable semantic diagnostics when no active domain semantics can lower the source. */
data class CompilerLoweringSemanticFailure(
    val source: CompilerSourceDocument,
    val document: EngineeringDocument,
    val diagnostics: List<SemanticDiagnostic>,
) : CompilerLoweringResult

/** Public-facing diagnostic shape returned by compiler parse and lowering entry points. */
data class CompilerSyntaxDiagnostic(
    val file: String,
    val line: Int,
    val column: Int,
    val endLine: Int = line,
    val endColumn: Int = column + 1,
    val message: String,
)

/** Unified compiler entry-path result that carries parse, lowering, and validation outcomes. */
sealed interface CompilerCompilationResult

/** Unified compiler failure when source parsing did not complete successfully. */
data class CompilerCompilationParseFailure(
    val diagnostics: List<CompilerSyntaxDiagnostic>,
    val pipeline: CompilerPipelineReport,
) : CompilerCompilationResult

/** Inspectable validation-boundary breakdown emitted by the compiler-owned validate pass. */
data class CompilerValidationBreakdown(
    val semanticEnrichmentDiagnostics: List<SemanticDiagnostic> = emptyList(),
    val kernelDiagnostics: List<SemanticDiagnostic> = emptyList(),
    val connectivityDiagnostics: List<SemanticDiagnostic> = emptyList(),
    val projectionPolicyDiagnostics: List<SemanticDiagnostic> = emptyList(),
    val domainDiagnostics: List<SemanticDiagnostic> = emptyList(),
    val domainValidationAttributions: List<AthenaDomainValidationAttribution> = emptyList(),
)

/** Immutable compiler-owned collection of validated Spatial pipeline results. */
class CompilerSpatialDocuments private constructor(
    private val documents: List<SpatialDocument>,
) : List<SpatialDocument> by documents {
    override fun equals(other: Any?): Boolean =
        this === other || other is List<*> && documents == other

    override fun hashCode(): Int = documents.hashCode()

    override fun toString(): String = documents.toString()

    companion object {
        private val EMPTY = CompilerSpatialDocuments(emptyList())

        fun of(documents: List<SpatialDocument>): CompilerSpatialDocuments =
            if (documents.isEmpty()) EMPTY else CompilerSpatialDocuments(Collections.unmodifiableList(documents.toList()))

        fun empty(): CompilerSpatialDocuments = EMPTY
    }
}

/** Unified compiler success carrying syntax authority, canonical IR, and semantic validation outcome. */
data class CompilerCompilationSuccess(
    val source: CompilerSourceDocument,
    val document: EngineeringDocument,
    val semanticResult: SemanticValidationResult,
    val sheetCompanions: List<SheetCompanionSource> = emptyList(),
    val validationBreakdown: CompilerValidationBreakdown = CompilerValidationBreakdown(),
    val projections: List<ProjectionDocument> = emptyList(),
    val projectionDiagnostics: List<String> = emptyList(),
    val spatialDocuments: CompilerSpatialDocuments = CompilerSpatialDocuments.empty(),
    val realityTransformationDiagnostics: List<RealityTransformationDiagnostic> = emptyList(),
    val pipeline: CompilerPipelineReport,
    val connectionIr: ConnectionDocument? = null,
    val connectionIrDiagnostics: List<String> = emptyList(),
) : CompilerCompilationResult

/**
 * Returns user-facing diagnostic messages without exposing lower-level semantic result types across module boundaries.
 */
fun CompilerCompilationResult.diagnosticMessages(): List<String> {
    return when (this) {
        is CompilerCompilationParseFailure -> diagnostics.map { diagnostic -> diagnostic.message }
        is CompilerCompilationSuccess -> (semanticResult.diagnostics.map { diagnostic -> diagnostic.message } +
            projectionDiagnostics +
            realityTransformationDiagnostics.map(RealityTransformationDiagnostic::message)).distinct()
    }
}
