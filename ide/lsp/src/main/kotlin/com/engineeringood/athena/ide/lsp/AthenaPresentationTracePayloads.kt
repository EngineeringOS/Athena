package com.engineeringood.athena.ide.lsp

/**
 * Shared compiler trace transported through the Athena LSP boundary.
 */
data class AthenaPresentationTracePayload(
    val sourceProvenance: List<String> = emptyList(),
    val sourceProjectionIds: List<String> = emptyList(),
    val compilerStage: String,
    val packageTrace: AthenaPresentationPackageTracePayload? = null,
    val compilerSnapshotId: String? = null,
    val sourceSpan: AthenaPresentationSourceSpanPayload? = null,
)
