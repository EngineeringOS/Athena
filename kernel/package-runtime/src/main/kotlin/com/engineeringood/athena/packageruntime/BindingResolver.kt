package com.engineeringood.athena.packageruntime

import com.engineeringood.athena.packageplatform.RepresentationDescriptor
import com.engineeringood.athena.packageplatform.RepresentationBindingRule
import com.engineeringood.athena.packageplatform.RepresentationBindingRuleLifecycleState
import com.engineeringood.athena.packageplatform.RepresentationPackageDescriptor
import com.engineeringood.athena.packageplatform.RepresentationPackageDescriptorEntry
import com.engineeringood.athena.packageplatform.RepresentationVariantId
import java.math.BigInteger

class BindingResolver {
    fun resolve(request: BindingResolutionRequest): BindingResolutionResult {
        val diagnostics = mutableListOf<BindingResolverDiagnostic>()

        validateEngineeringPackage(request, diagnostics)
        validateProfile(request, diagnostics)
        validateManifest(request, diagnostics)

        val selectedRule = selectBindingRule(request, diagnostics)

        val selectedPackage = selectedRule?.let { rule ->
            selectRepresentationPackage(request, rule, diagnostics)
        }

        val descriptorEntry = if (selectedRule != null && selectedPackage != null) {
            selectDescriptorEntry(selectedPackage, selectedRule, diagnostics)
        } else {
            null
        }
        val descriptor = when {
            descriptorEntry != null && selectedPackage != null -> selectDescriptor(
                request,
                selectedPackage,
                descriptorEntry,
                diagnostics,
            )
            selectedRule != null && selectedPackage == null -> {
                diagnostics += diagnostic(
                    code = "binding.resolution.descriptor.missing",
                    authority = BindingAuthority.DESCRIPTOR,
                    subject = selectedRule.target.descriptorId.value,
                    message = "No validated Representation Descriptor was available for binding.",
                )
                null
            }
            else -> null
        }

        if (selectedRule != null) {
            validateRequiredAnchors(request, descriptor, diagnostics)
            validateRequiredLabels(request, descriptor, diagnostics)
        }

        val variantId = resolveVariant(selectedRule, descriptorEntry, descriptor, diagnostics)

        val hasErrors = diagnostics.any { it.severity == BindingResolverDiagnosticSeverity.ERROR }
        val resolution = if (!hasErrors && selectedRule != null && selectedPackage != null && descriptor != null && variantId != null) {
            BindingResolution(
                semanticSubjectId = request.subject.semanticSubjectId,
                engineeringPackageId = request.engineeringPackage.packageId,
                presentationProfileId = request.activeProfile.profileId,
                representationPackageId = selectedPackage.packageId,
                descriptorId = descriptor.descriptorId,
                variantId = variantId,
                anchorMapping = request.subject.requiredAnchorBindings.toSortedMap(),
                labelBinding = request.subject.requiredLabelBindings.toSortedMap(compareBy { it.value }),
                styleProfile = request.activeProfile.styleProfile,
                bindingRuleId = selectedRule.ruleId,
            )
        } else {
            null
        }

        return BindingResolutionResult(
            resolution = resolution,
            diagnostics = diagnostics,
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
        if (request.manifest.compatibleProfileTags.isNotEmpty() &&
            request.manifest.compatibleProfileTags.none { it.value == request.activeProfile.profileId.value }
        ) {
            diagnostics += diagnostic(
                code = "binding.resolution.binding-manifest.profile-incompatible",
                authority = BindingAuthority.BINDING_MANIFEST,
                subject = request.activeProfile.profileId.value,
                message = "Binding Manifest does not allow the active Presentation Profile.",
            )
        }
    }

    private fun selectBindingRule(
        request: BindingResolutionRequest,
        diagnostics: MutableList<BindingResolverDiagnostic>,
    ): RepresentationBindingRule? {
        val candidates = request.bindingRules
            .filter { it.lifecycle.state == RepresentationBindingRuleLifecycleState.ACTIVE }
            .filter { it.profileId == request.activeProfile.profileId }
            .filter { it.projectionContext == request.projectionContext }
            .filter { it.conceptId == request.subject.conceptId }
            .filter { it.subjectKind == request.subject.subjectKind }
            .filter { rule ->
                rule.selectorFacts.all { selector -> request.subject.semanticFacts[selector.name] == selector.value }
            }

        if (candidates.isEmpty()) {
            diagnostics += diagnostic(
                code = "binding.resolution.rule.missing",
                authority = BindingAuthority.BINDING_RULE,
                subject = request.subject.semanticSubjectId,
                message = "No typed Representation Binding Rule matched the semantic subject and active profile.",
            )
            return null
        }

        val highestPriority = candidates.maxOf { it.priority.value }
        val winners = candidates.filter { it.priority.value == highestPriority }.sortedBy { it.ruleId.value }
        if (winners.size > 1) {
            diagnostics += diagnostic(
                code = "binding.resolution.rule.ambiguous",
                authority = BindingAuthority.BINDING_RULE,
                subject = winners.joinToString(",") { it.ruleId.value },
                message = "Multiple typed Representation Binding Rules matched with the same highest priority.",
            )
            return null
        }

        return winners.single()
    }

    private fun selectRepresentationPackage(
        request: BindingResolutionRequest,
        rule: RepresentationBindingRule,
        diagnostics: MutableList<BindingResolverDiagnostic>,
    ): RepresentationPackageDescriptor? {
        val admittedPackageIds = (listOf(request.manifest.defaultRepresentationPackageId) +
            request.manifest.alternativeRepresentationPackageIds)
            .distinct()
            .toSet()
        val selectedPackage = request.representationPackages.firstOrNull {
            it.packageId == rule.target.representationPackageId
        }
        if (
            rule.target.representationPackageId.value !in admittedPackageIds ||
            selectedPackage == null ||
            selectedPackage.coordinates.version != rule.target.packageVersion ||
            !selectedPackage.supports(request.activeProfile.profileId.value)
        ) {
            diagnostics += diagnostic(
                code = "binding.resolution.representation-package.missing",
                authority = BindingAuthority.REPRESENTATION_PACKAGE,
                subject = rule.target.representationPackageId.value,
                message = "No manifest-admitted exact representation package matched the selected Binding Rule.",
            )
            return null
        }

        return selectedPackage
    }

    private fun selectDescriptorEntry(
        selectedPackage: RepresentationPackageDescriptor,
        rule: RepresentationBindingRule,
        diagnostics: MutableList<BindingResolverDiagnostic>,
    ): RepresentationPackageDescriptorEntry? {
        val entry = selectedPackage.descriptorEntries.firstOrNull { it.descriptorId == rule.target.descriptorId }
        if (entry == null) {
            diagnostics += diagnostic(
                code = "binding.resolution.descriptor-rule.missing",
                authority = BindingAuthority.DESCRIPTOR,
                subject = rule.target.descriptorId.value,
                message = "Selected Binding Rule targeted a descriptor that is not in the representation package.",
            )
        }
        return entry
    }

    private fun selectDescriptor(
        request: BindingResolutionRequest,
        selectedPackage: RepresentationPackageDescriptor,
        entry: RepresentationPackageDescriptorEntry,
        diagnostics: MutableList<BindingResolverDiagnostic>,
    ): RepresentationDescriptor? {
        val matches = request.descriptors.filter { descriptor ->
            (descriptor.representationPackageId == null || descriptor.representationPackageId == selectedPackage.packageId) &&
                descriptor.descriptorId == entry.descriptorId &&
                descriptor.resource.resourceId == entry.resourceId
        }
        return when (matches.size) {
            1 -> matches.single()
            0 -> {
                diagnostics += diagnostic(
                    code = "binding.resolution.descriptor.missing",
                    authority = BindingAuthority.DESCRIPTOR,
                    subject = entry.descriptorId.value,
                    message = "No validated Representation Descriptor matched the selected package entry and resource.",
                )
                null
            }
            else -> {
                diagnostics += diagnostic(
                    code = "binding.resolution.descriptor.ambiguous",
                    authority = BindingAuthority.DESCRIPTOR,
                    subject = entry.descriptorId.value,
                    message = "Multiple validated Representation Descriptors matched the selected package entry and resource.",
                )
                null
            }
        }
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

    private fun resolveVariant(
        rule: RepresentationBindingRule?,
        descriptorEntry: RepresentationPackageDescriptorEntry?,
        descriptor: RepresentationDescriptor?,
        diagnostics: MutableList<BindingResolverDiagnostic>,
    ): RepresentationVariantId? {
        if (rule == null || descriptorEntry == null || descriptor == null) return null
        return selectVariant(rule, descriptorEntry, descriptor, diagnostics)
    }

    private fun selectVariant(
        rule: RepresentationBindingRule,
        descriptorEntry: RepresentationPackageDescriptorEntry,
        descriptor: RepresentationDescriptor,
        diagnostics: MutableList<BindingResolverDiagnostic>,
    ): RepresentationVariantId? {
        val variants = (descriptorEntry.variants + descriptor.variants).distinct()
        val requested = rule.target.variantId
        if (requested != null) {
            if (requested !in variants) {
                diagnostics += diagnostic(
                    code = "binding.resolution.variant.missing",
                    authority = BindingAuthority.VARIANT,
                    subject = requested.value,
                    message = "Selected Binding Rule requested a variant that is not available.",
                )
                return null
            }
            return requested
        }

        return when (variants.size) {
            1 -> variants.single()
            0 -> RepresentationVariantId("default")
            else -> {
                diagnostics += diagnostic(
                    code = "binding.resolution.variant.ambiguous",
                    authority = BindingAuthority.VARIANT,
                    subject = descriptor.descriptorId.value,
                    message = "No variant was requested and multiple variants are available.",
                )
                null
            }
        }
    }

    private fun versionSatisfies(actual: String, range: String): Boolean {
        val actualVersion = ComparableSemanticVersion.parse(actual) ?: return false
        val minimumRange = range.endsWith("+")
        val requiredVersion = ComparableSemanticVersion.parse(
            if (minimumRange) range.removeSuffix("+") else range,
        ) ?: return false
        return if (minimumRange) actualVersion >= requiredVersion else actualVersion.compareTo(requiredVersion) == 0
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

private data class ComparableSemanticVersion(
    val major: BigInteger,
    val minor: BigInteger,
    val patch: BigInteger,
    val prerelease: List<String>,
) : Comparable<ComparableSemanticVersion> {
    override fun compareTo(other: ComparableSemanticVersion): Int {
        listOf(major to other.major, minor to other.minor, patch to other.patch).forEach { (left, right) ->
            left.compareTo(right).takeIf { comparison -> comparison != 0 }?.let { return it }
        }
        if (prerelease.isEmpty() || other.prerelease.isEmpty()) {
            return when {
                prerelease.isEmpty() && other.prerelease.isEmpty() -> 0
                prerelease.isEmpty() -> 1
                else -> -1
            }
        }
        val sharedSize = minOf(prerelease.size, other.prerelease.size)
        for (index in 0 until sharedSize) {
            val comparison = comparePrereleaseIdentifier(prerelease[index], other.prerelease[index])
            if (comparison != 0) return comparison
        }
        return prerelease.size.compareTo(other.prerelease.size)
    }

    companion object {
        private val pattern = Regex(
            """(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)""" +
                """(?:-((?:0|[1-9][0-9]*|[0-9]*[A-Za-z-][0-9A-Za-z-]*)""" +
                """(?:\.(?:0|[1-9][0-9]*|[0-9]*[A-Za-z-][0-9A-Za-z-]*))*))?""" +
                """(?:\+([0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*))?""",
        )

        fun parse(value: String): ComparableSemanticVersion? {
            val match = pattern.matchEntire(value) ?: return null
            return ComparableSemanticVersion(
                major = match.groupValues[1].toBigInteger(),
                minor = match.groupValues[2].toBigInteger(),
                patch = match.groupValues[3].toBigInteger(),
                prerelease = match.groupValues[4].takeIf(String::isNotBlank)?.split('.').orEmpty(),
            )
        }
    }
}

private fun comparePrereleaseIdentifier(left: String, right: String): Int {
    val leftNumeric = left.all(Char::isDigit)
    val rightNumeric = right.all(Char::isDigit)
    return when {
        leftNumeric && rightNumeric -> left.toBigInteger().compareTo(right.toBigInteger())
        leftNumeric -> -1
        rightNumeric -> 1
        else -> left.compareTo(right)
    }
}
