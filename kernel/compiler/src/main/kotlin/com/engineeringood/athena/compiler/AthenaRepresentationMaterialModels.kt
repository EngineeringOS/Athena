package com.engineeringood.athena.compiler

import com.engineeringood.athena.ir.EngineeringPort
import com.engineeringood.athena.packageplatform.EngineeringConceptId
import com.engineeringood.athena.packageplatform.RepresentationBindingSubjectKind
import com.engineeringood.athena.packageruntime.BindingResolution
import com.engineeringood.athena.representation.RepresentationDefinition

data class AthenaResolvedRepresentationMaterial(
    val semanticSubjectId: String,
    val physicalComponentId: String,
    val functionId: String?,
    val definition: RepresentationDefinition,
    val resolution: BindingResolution,
    val terminalBindings: Map<String, String>,
)

data class AthenaRepresentationMaterialDiagnostic(
    val code: String,
    val subject: String,
    val message: String,
) {
    override fun toString(): String = "[$code] $subject: $message"
}

data class AthenaRepresentationMaterialEvidence(
    val stagedSourcePaths: List<String> = emptyList(),
)

data class AthenaRepresentationMaterialResolutionResult(
    val definitions: List<RepresentationDefinition> = emptyList(),
    val materials: List<AthenaResolvedRepresentationMaterial> = emptyList(),
    val diagnostics: List<AthenaRepresentationMaterialDiagnostic> = emptyList(),
    val evidence: AthenaRepresentationMaterialEvidence = AthenaRepresentationMaterialEvidence(),
)

internal data class MaterialSubject(
    val semanticSubjectId: String,
    val physicalComponentId: String,
    val functionId: String?,
    val conceptId: EngineeringConceptId,
    val semanticFacts: Map<String, String>,
    val ports: List<EngineeringPort>,
    val label: String,
    val subjectKind: RepresentationBindingSubjectKind,
)

internal fun materialDiagnostic(code: String, subject: String, message: String) =
    AthenaRepresentationMaterialDiagnostic(code, subject, message)
