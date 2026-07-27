package com.engineeringood.athena.packageruntime

import com.engineeringood.athena.packageplatform.BindingManifest
import com.engineeringood.athena.packageplatform.PresentationProfileDescriptor
import com.engineeringood.athena.packageplatform.ProjectionContextId
import com.engineeringood.athena.packageplatform.RepresentationBindingPriority
import com.engineeringood.athena.packageplatform.RepresentationBindingRule
import com.engineeringood.athena.packageplatform.RepresentationBindingRuleId
import com.engineeringood.athena.packageplatform.RepresentationBindingRuleLifecycle
import com.engineeringood.athena.packageplatform.RepresentationBindingRuleLifecycleState
import com.engineeringood.athena.packageplatform.RepresentationBindingRuleProvenance
import com.engineeringood.athena.packageplatform.RepresentationBindingTarget
import com.engineeringood.athena.packageplatform.RepresentationPackageDescriptor

/**
 * Fixture-only bridge for M32/M33 package tests while active M34 selection moves to typed rules.
 */
class LegacyBindingPolicyTagRuleAdapter {
    fun translate(
        manifest: BindingManifest,
        activeProfile: PresentationProfileDescriptor,
        projectionContext: ProjectionContextId,
        representationPackages: List<RepresentationPackageDescriptor>,
    ): List<RepresentationBindingRule> {
        if (manifest.policyTags.isEmpty()) return emptyList()

        val admittedPackageIds = (listOf(manifest.defaultRepresentationPackageId) +
            manifest.alternativeRepresentationPackageIds)
            .toSet()
        val requestedTags = manifest.policyTags.toSet()

        return representationPackages
            .filter { it.packageId.value in admittedPackageIds }
            .flatMap { representationPackage ->
                representationPackage.descriptorEntries
                    .filter { entry -> entry.bindingPolicyTags.any { it in requestedTags } }
                    .map { entry ->
                        RepresentationBindingRule(
                            ruleId = RepresentationBindingRuleId(
                                "legacy.${manifest.manifestId.value}.${entry.descriptorId.value}",
                            ),
                            profileId = activeProfile.profileId,
                            projectionContext = projectionContext,
                            conceptId = manifest.conceptId,
                            target = RepresentationBindingTarget(
                                representationPackageId = representationPackage.packageId,
                                descriptorId = entry.descriptorId,
                                packageVersion = representationPackage.coordinates.version,
                                variantId = entry.variants.firstOrNull(),
                            ),
                            priority = RepresentationBindingPriority(0),
                            lifecycle = RepresentationBindingRuleLifecycle(RepresentationBindingRuleLifecycleState.ACTIVE),
                            provenance = RepresentationBindingRuleProvenance(
                                sources = listOf("legacy-binding-policy-tag-adapter"),
                                reviewedBy = "Athena M34 migration",
                            ),
                        )
                    }
            }
    }
}
