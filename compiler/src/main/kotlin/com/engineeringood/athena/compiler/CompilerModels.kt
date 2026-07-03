package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.boundary.AthenaBoundaryValidationReport
import com.engineeringood.athena.compiler.knowledge.AthenaKnowledgeArtifactKind
import com.engineeringood.athena.compiler.knowledge.AthenaCompilationKnowledgeContext
import com.engineeringood.athena.compiler.knowledge.AthenaKnowledgeProvenance
import com.engineeringood.athena.ir.EngineeringIrDocument
import com.engineeringood.athena.language.SourceFileAst
import com.engineeringood.athena.semantics.core.SemanticDiagnostic
import com.engineeringood.athena.semantics.core.SemanticValidationResult

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
    val document: com.engineeringood.athena.ir.EngineeringIrDocument,
) : CompilerLoweringResult

/** Failed lowering containing the diagnostics that prevented semantic lowering from running. */
data class CompilerLoweringFailure(
    val diagnostics: List<CompilerSyntaxDiagnostic>,
) : CompilerLoweringResult

/** Failed lowering containing inspectable semantic diagnostics when no active domain semantics can lower the source. */
data class CompilerLoweringSemanticFailure(
    val source: CompilerSourceDocument,
    val document: EngineeringIrDocument,
    val diagnostics: List<SemanticDiagnostic>,
) : CompilerLoweringResult

/** Public-facing diagnostic shape returned by compiler parse and lowering entry points. */
data class CompilerSyntaxDiagnostic(
    val file: String,
    val line: Int,
    val column: Int,
    val message: String,
)

/** Unified compiler entry-path result that carries parse, lowering, and validation outcomes. */
sealed interface CompilerCompilationResult

/** Stable metadata reference for one governed knowledge artifact that may be attributed to a compiler-facing result. */
data class CompilerKnowledgeArtifactReference(
    val artifactId: String,
    val artifactKind: AthenaKnowledgeArtifactKind,
    val artifactVersion: String,
    val provenance: AthenaKnowledgeProvenance,
)

/** Compiler-facing result targets that may carry governed knowledge attribution metadata. */
enum class CompilerKnowledgeAttributionTarget {
    KNOWLEDGE_CONTEXT,
    SEMANTIC_RESULT,
    RENDERING,
}

/** Explicit governed knowledge attribution metadata for one compiler-facing result target. */
data class CompilerKnowledgeAttribution(
    val target: CompilerKnowledgeAttributionTarget,
    val responsibleArtifacts: List<CompilerKnowledgeArtifactReference>,
    val rationale: String,
)

/** Unified compiler failure when source parsing did not complete successfully. */
data class CompilerCompilationParseFailure(
    val diagnostics: List<CompilerSyntaxDiagnostic>,
    val knowledgeContext: AthenaCompilationKnowledgeContext,
    val boundaryValidation: AthenaBoundaryValidationReport,
    val pipeline: CompilerPipelineReport,
) : CompilerCompilationResult

/** Unified compiler success carrying syntax authority, canonical IR, and semantic validation outcome. */
data class CompilerCompilationSuccess(
    val source: CompilerSourceDocument,
    val document: EngineeringIrDocument,
    val semanticResult: SemanticValidationResult,
    val rendering: CompilerRenderingResult,
    val knowledgeContext: AthenaCompilationKnowledgeContext,
    val boundaryValidation: AthenaBoundaryValidationReport,
    val knowledgeAttributions: List<CompilerKnowledgeAttribution>,
    val pipeline: CompilerPipelineReport,
) : CompilerCompilationResult
