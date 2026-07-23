package com.engineeringood.athena.packageruntime

import com.engineeringood.athena.packageplatform.PresentationProfileTag
import com.engineeringood.athena.packageplatform.RepresentationDescriptor
import com.engineeringood.athena.packageplatform.RepresentationPackageDescriptor
import com.engineeringood.athena.packageplatform.RepresentationPackageId
import com.engineeringood.athena.packageplatform.RepresentationVariantId

class BindingResolver {
    fun resolve(request: BindingResolutionRequest): BindingResolutionResult {
        val diagnostics = mutableListOf<BindingResolverDiagnostic>()

        validateEngineeringPackage(request, diagnostics)
        validateProfile(request, diagnostics)
        validateManifest(request, diagnostics)

        val selectedPackage = selectRepresentationPackage(request)
        if (selectedPackage == null) {
            diagnostics += diagnostic(
                code = "binding.resolution.representation-package.missing",
                authority = BindingAuthority.REPRESENTATION_PACKAGE,
                subject = request.manifest.defaultRepresentationPackageId,
                message = "No compatible representation package was found for the active Presentation Profile.",
            )
        }

        val descriptorId = selectedPackage?.descriptorEntries
            ?.sortedBy { it.descriptorId.value }
            ?.firstOrNull()
            ?.descriptorId
        val descriptor = descriptorId?.let { id ->
            request.descriptors.firstOrNull { it.descriptorId == id }
        }
        if (descriptor == null) {
            diagnostics += diagnostic(
                code = "binding.resolution.descriptor.missing",
                authority = BindingAuthority.DESCRIPTOR,
                subject = descriptorId?.value ?: request.subject.semanticSubjectId,
                message = "No validated Representation Descriptor was available for binding.",
            )
        }

        validateRequiredAnchors(request, descriptor, diagnostics)
        validateRequiredLabels(request, descriptor, diagnostics)

        val hasErrors = diagnostics.any { it.severity == BindingResolverDiagnosticSeverity.ERROR }
        val resolution = if (!hasErrors && selectedPackage != null && descriptor != null) {
            BindingResolution(
                semanticSubjectId = request.subject.semanticSubjectId,
                engineeringPackageId = request.engineeringPackage.packageId,
                presentationProfileId = request.activeProfile.profileId,
                representationPackageId = selectedPackage.packageId,
                descriptorId = descriptor.descriptorId,
                variantId = selectVariant(selectedPackage, descriptor),
                anchorMapping = request.subject.requiredAnchorBindings.toSortedMap(),
                labelBinding = request.subject.requiredLabelBindings.toSortedMap(compareBy { it.value }),
                styleProfile = request.activeProfile.styleProfile,
            )
        } else {
            null
        }

        return BindingResolutionResult(
            resolution = resolution,
            diagnostics = diagnostics,
            rendererFallbackAccepted = false,
        )
    }

    private fun validateEngineeringPackage(
        request: BindingResolutionRequest,
        diagnostics: MutableList<BindingResolverDiagnostic>,
    ) {
        if (request.engineeringPackage.packageId.value != request.manifest.engineeringPackageId) {
            diagnostics += diagnostic(
                code = "binding.resolution.engineering-package.mismatch",
                authority = BindingAuthority.ENGINEERING_PACKAGE,
                subject = request.engineeringPackage.packageId.value,
                message = "Engineering Package does not match the Binding Manifest package id.",
            )
        }
        if (request.engineeringPackage.concepts.none { it.conceptId == request.subject.conceptId }) {
            diagnostics += diagnostic(
                code = "binding.resolution.engineering-package.concept-missing",
                authority = BindingAuthority.ENGINEERING_PACKAGE,
                subject = request.subject.conceptId.value,
                message = "Engineering Package does not declare the semantic subject concept.",
            )
        }
    }

    private fun validateProfile(
        request: BindingResolutionRequest,
        diagnostics: MutableList<BindingResolverDiagnostic>,
    ) {
        if (request.activeProfile.projectionContexts.none { it == request.projectionContext }) {
            diagnostics += diagnostic(
                code = "binding.resolution.presentation-profile.context-missing",
                authority = BindingAuthority.PRESENTATION_PROFILE,
                subject = request.projectionContext.value,
                message = "Presentation Profile does not support the projection context.",
            )
        }
        val compatibleEngineeringPackage = request.activeProfile.compatibilityConstraints.any { constraint ->
            constraint.packageId == request.engineeringPackage.packageId.value &&
                versionSatisfies(request.engineeringPackage.coordinates.version.value, constraint.versionRange)
        }
        if (!compatibleEngineeringPackage) {
            diagnostics += diagnostic(
                code = "binding.resolution.presentation-profile.incompatible",
                authority = BindingAuthority.PRESENTATION_PROFILE,
                subject = request.activeProfile.profileId.value,
                message = "Presentation Profile is not compatible with the Engineering Package.",
            )
        }
    }

    private fun validateManifest(
        request: BindingResolutionRequest,
        diagnostics: MutableList<BindingResolverDiagnostic>,
    ) {
        if (request.manifest.conceptId != request.subject.conceptId) {
            diagnostics += diagnostic(
                code = "binding.resolution.binding-manifest.concept-mismatch",
                authority = BindingAuthority.BINDING_MANIFEST,
                subject = request.manifest.manifestId.value,
                message = "Binding Manifest concept does not match the semantic subject concept.",
            )
        }
        val activeProfileTag = PresentationProfileTag(request.activeProfile.profileId.value)
        if (request.manifest.compatibleProfileTags.isNotEmpty() &&
            activeProfileTag !in request.manifest.compatibleProfileTags
        ) {
            diagnostics += diagnostic(
                code = "binding.resolution.binding-manifest.profile-incompatible",
                authority = BindingAuthority.BINDING_MANIFEST,
                subject = request.activeProfile.profileId.value,
                message = "Binding Manifest does not allow the active Presentation Profile.",
            )
        }
    }

    private fun selectRepresentationPackage(request: BindingResolutionRequest): RepresentationPackageDescriptor? {
        val candidates = listOf(request.manifest.defaultRepresentationPackageId) +
            request.manifest.alternativeRepresentationPackageIds
        return candidates
            .distinct()
            .asSequence()
            .map { RepresentationPackageId(it) }
            .mapNotNull { packageId -> request.representationPackages.firstOrNull { it.packageId == packageId } }
            .firstOrNull { representationPackage -> representationPackage.supports(request.activeProfile.profileId.value) }
    }

    private fun RepresentationPackageDescriptor.supports(profileId: String): Boolean =
        supportedProfiles.any { supported ->
            supported.profileId.value == profileId || supported.tags.any { it.value == profileId }
        }

    private fun validateRequiredAnchors(
        request: BindingResolutionRequest,
        descriptor: RepresentationDescriptor?,
        diagnostics: MutableList<BindingResolverDiagnostic>,
    ) {
        val anchors = descriptor?.anchors.orEmpty().map { it.anchorId }.toSet()
        request.subject.requiredAnchorBindings
            .toSortedMap()
            .forEach { (semanticAnchor, descriptorAnchor) ->
                if (descriptorAnchor !in anchors) {
                    diagnostics += diagnostic(
                        code = "binding.resolution.anchor.missing",
                        authority = BindingAuthority.ANCHOR,
                        subject = semanticAnchor,
                        message = "Required descriptor anchor '${descriptorAnchor.value}' was not found.",
                    )
                }
            }
    }

    private fun validateRequiredLabels(
        request: BindingResolutionRequest,
        descriptor: RepresentationDescriptor?,
        diagnostics: MutableList<BindingResolverDiagnostic>,
    ) {
        val labelSlots = descriptor?.labelSlots.orEmpty().map { it.slotId }.toSet()
        request.subject.requiredLabelBindings
            .toSortedMap(compareBy { it.value })
            .forEach { (slotId, _) ->
                if (slotId !in labelSlots) {
                    diagnostics += diagnostic(
                        code = "binding.resolution.label-slot.missing",
                        authority = BindingAuthority.LABEL_SLOT,
                        subject = slotId.value,
                        message = "Required descriptor label slot was not found.",
                    )
                }
            }
    }

    private fun selectVariant(
        selectedPackage: RepresentationPackageDescriptor,
        descriptor: RepresentationDescriptor,
    ): RepresentationVariantId {
        val entry = selectedPackage.descriptorEntries
            .firstOrNull { it.descriptorId == descriptor.descriptorId }
        return entry?.variants?.firstOrNull()
            ?: descriptor.variants.firstOrNull()
            ?: RepresentationVariantId("default")
    }

    private fun versionSatisfies(actual: String, range: String): Boolean =
        when {
            range.endsWith("+") -> actual >= range.removeSuffix("+")
            else -> actual == range
        }

    private fun diagnostic(
        code: String,
        authority: BindingAuthority,
        subject: String,
        message: String,
    ): BindingResolverDiagnostic = BindingResolverDiagnostic(
        code = BindingResolverDiagnosticCode(code),
        severity = BindingResolverDiagnosticSeverity.ERROR,
        authority = authority,
        subject = subject,
        message = message,
    )
}
