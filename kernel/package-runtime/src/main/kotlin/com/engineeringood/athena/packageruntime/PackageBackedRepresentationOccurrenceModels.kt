package com.engineeringood.athena.packageruntime

import com.engineeringood.athena.packageplatform.RepresentationDescriptor
import com.engineeringood.athena.representation.RepresentationDiagnostic
import com.engineeringood.athena.representation.RepresentationOccurrence
import com.engineeringood.athena.representation.RepresentationOccurrenceRole
import com.engineeringood.athena.representation.RepresentationProjectionKind
import com.engineeringood.athena.representation.RepresentationProjectionOccurrenceId
import com.engineeringood.athena.representation.RepresentationSemanticRole
import com.engineeringood.athena.representation.RepresentationSubjectKind

data class PackageBackedRepresentationOccurrenceRequest(
    val bindingEvidence: BindingEvidencePayload,
    val descriptor: RepresentationDescriptor?,
    val projectionOccurrenceId: RepresentationProjectionOccurrenceId,
    val subjectKind: RepresentationSubjectKind,
    val semanticRole: RepresentationSemanticRole?,
    val projectionKind: RepresentationProjectionKind,
    val occurrenceRole: RepresentationOccurrenceRole,
)

data class PackageBackedRepresentationOccurrenceResult(
    val occurrence: RepresentationOccurrence?,
    val diagnostics: List<RepresentationDiagnostic>,
    val rendererFallbackAccepted: Boolean = false,
)
