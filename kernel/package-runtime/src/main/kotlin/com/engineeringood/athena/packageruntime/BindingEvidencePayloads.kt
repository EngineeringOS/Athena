package com.engineeringood.athena.packageruntime

data class BindingEvidencePayload(
    val semanticSubjectId: String,
    val engineeringPackageId: String,
    val engineeringPackageVersion: String,
    val presentationProfileId: String,
    val bindingManifestId: String? = null,
    val representationPackageId: String?,
    val representationPackageVersion: String?,
    val descriptorId: String?,
    val variant: String?,
    val anchorMapSummary: List<String>,
    val labelBindingSummary: List<String>,
    val resolverStage: String,
    val diagnostics: List<BindingEvidenceDiagnosticPayload>,
    val rendererFallbackAccepted: Boolean,
)

data class BindingEvidenceDiagnosticPayload(
    val severity: String,
    val code: String,
    val authority: String,
    val subject: String,
    val message: String,
)
