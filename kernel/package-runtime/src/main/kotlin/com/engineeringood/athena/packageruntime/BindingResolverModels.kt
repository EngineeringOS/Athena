package com.engineeringood.athena.packageruntime

import com.engineeringood.athena.packageplatform.BindingManifest
import com.engineeringood.athena.packageplatform.EngineeringConceptId
import com.engineeringood.athena.packageplatform.EngineeringPackageDescriptor
import com.engineeringood.athena.packageplatform.EngineeringPackageId
import com.engineeringood.athena.packageplatform.PresentationProfileDescriptor
import com.engineeringood.athena.packageplatform.PresentationProfileId
import com.engineeringood.athena.packageplatform.PresentationStyleProfileId
import com.engineeringood.athena.packageplatform.ProjectionContextId
import com.engineeringood.athena.packageplatform.RepresentationAnchorId
import com.engineeringood.athena.packageplatform.RepresentationBindingRule
import com.engineeringood.athena.packageplatform.RepresentationBindingRuleId
import com.engineeringood.athena.packageplatform.RepresentationBindingSubjectKind
import com.engineeringood.athena.packageplatform.RepresentationDescriptor
import com.engineeringood.athena.packageplatform.RepresentationDescriptorId
import com.engineeringood.athena.packageplatform.RepresentationLabelSlotId
import com.engineeringood.athena.packageplatform.RepresentationPackageDescriptor
import com.engineeringood.athena.packageplatform.RepresentationPackageId
import com.engineeringood.athena.packageplatform.RepresentationVariantId

data class BindingSubject(
    val semanticSubjectId: String,
    val conceptId: EngineeringConceptId,
    val requiredAnchorBindings: Map<String, RepresentationAnchorId> = emptyMap(),
    val requiredLabelBindings: Map<RepresentationLabelSlotId, String> = emptyMap(),
    val semanticFacts: Map<String, String> = emptyMap(),
    val subjectKind: RepresentationBindingSubjectKind = RepresentationBindingSubjectKind.DEVICE,
)

data class BindingResolutionRequest(
    val subject: BindingSubject,
    val projectionContext: ProjectionContextId,
    val engineeringPackage: EngineeringPackageDescriptor,
    val manifest: BindingManifest,
    val activeProfile: PresentationProfileDescriptor,
    val representationPackages: List<RepresentationPackageDescriptor>,
    val descriptors: List<RepresentationDescriptor>,
    val bindingRules: List<RepresentationBindingRule> = emptyList(),
)

enum class BindingAuthority {
    ENGINEERING_PACKAGE,
    PRESENTATION_PROFILE,
    BINDING_MANIFEST,
    REPRESENTATION_PACKAGE,
    DESCRIPTOR,
    ANCHOR,
    LABEL_SLOT,
    BINDING_POLICY,
    BINDING_RULE,
    VARIANT,
}

enum class BindingResolverDiagnosticSeverity {
    ERROR,
}

@JvmInline
value class BindingResolverDiagnosticCode(val wireValue: String) {
    override fun toString(): String = wireValue
}

data class BindingResolverDiagnostic(
    val code: BindingResolverDiagnosticCode,
    val severity: BindingResolverDiagnosticSeverity,
    val authority: BindingAuthority,
    val subject: String,
    val message: String,
)

data class BindingResolution(
    val semanticSubjectId: String,
    val engineeringPackageId: EngineeringPackageId,
    val presentationProfileId: PresentationProfileId,
    val representationPackageId: RepresentationPackageId,
    val descriptorId: RepresentationDescriptorId,
    val variantId: RepresentationVariantId,
    val anchorMapping: Map<String, RepresentationAnchorId>,
    val labelBinding: Map<RepresentationLabelSlotId, String>,
    val styleProfile: PresentationStyleProfileId,
    val bindingRuleId: RepresentationBindingRuleId? = null,
)

data class BindingResolutionResult(
    val resolution: BindingResolution?,
    val diagnostics: List<BindingResolverDiagnostic>,
) {
    val isValid: Boolean
        get() = resolution != null &&
            diagnostics.none { it.severity == BindingResolverDiagnosticSeverity.ERROR }
}
