package com.engineeringood.athena.compiler

import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.packageplatform.EngineeringConceptDefinition
import com.engineeringood.athena.packageplatform.EngineeringConceptId
import com.engineeringood.athena.packageplatform.EngineeringPackageArtifactId
import com.engineeringood.athena.packageplatform.EngineeringPackageCoordinates
import com.engineeringood.athena.packageplatform.EngineeringPackageDescriptor
import com.engineeringood.athena.packageplatform.EngineeringPackageGroupId
import com.engineeringood.athena.packageplatform.EngineeringPackageId
import com.engineeringood.athena.packageplatform.EngineeringPackageKind
import com.engineeringood.athena.packageplatform.EngineeringPackageLifecycle
import com.engineeringood.athena.packageplatform.EngineeringPackageLifecycleState
import com.engineeringood.athena.packageplatform.EngineeringPackageProvenance
import com.engineeringood.athena.packageplatform.EngineeringPackageVersion
import com.engineeringood.athena.packageplatform.GraphicResourceRef
import com.engineeringood.athena.packageplatform.PresentationPackageCompatibilityConstraint
import com.engineeringood.athena.packageplatform.PresentationProfileDescriptor
import com.engineeringood.athena.packageplatform.RepresentationPackageArtifactId
import com.engineeringood.athena.packageplatform.RepresentationPackageCoordinates
import com.engineeringood.athena.packageplatform.RepresentationPackageDescriptor
import com.engineeringood.athena.packageplatform.RepresentationPackageDescriptorEntry
import com.engineeringood.athena.packageplatform.RepresentationPackageGroupId
import com.engineeringood.athena.packageplatform.RepresentationPackageId
import com.engineeringood.athena.packageplatform.RepresentationPackageLifecycle
import com.engineeringood.athena.packageplatform.RepresentationPackageLifecycleState
import com.engineeringood.athena.packageplatform.RepresentationPackageProvenance
import com.engineeringood.athena.packageplatform.RepresentationPackageVersion
import com.engineeringood.athena.packageplatform.RepresentationSupportedProfile
import com.engineeringood.athena.packageplatform.RepresentationVariantDefinition
import java.nio.file.Path

internal object AthenaRepresentationMaterialContractFactory {
    fun engineeringPackage(
        document: EngineeringDocument,
        packageName: String,
        packageVersion: String,
        repositoryRoot: Path,
    ): EngineeringPackageDescriptor {
        val coordinates = engineeringCoordinates(packageName, packageVersion)
        val concepts = document.components.mapNotNull { component -> component.materialPropertyValue("type") }
            .distinct()
            .sorted()
            .map { type -> EngineeringConceptDefinition(EngineeringConceptId(type)) }
        return EngineeringPackageDescriptor(
            packageId = EngineeringPackageId(packageName),
            coordinates = coordinates,
            kind = EngineeringPackageKind.CATALOG,
            concepts = concepts,
            lifecycle = EngineeringPackageLifecycle(EngineeringPackageLifecycleState.ACTIVE, coordinates.version),
            provenance = EngineeringPackageProvenance(
                sources = listOf(repositoryRoot.resolve("athena.yaml").toString()),
                reviewedBy = "Athena repository contract loader",
            ),
        )
    }

    fun representationPackages(
        compiled: AthenaRepresentationPackageSnapshotCompilationResult,
        profiles: List<PresentationProfileDescriptor>,
        diagnostics: MutableList<AthenaRepresentationMaterialDiagnostic>,
    ): List<RepresentationPackageDescriptor> = compiled.bindingRules
        .groupBy { rule -> rule.target.representationPackageId }
        .mapNotNull { (packageId, rules) ->
            val versions = rules.map { rule -> rule.target.packageVersion }.distinct()
            if (versions.size != 1) {
                diagnostics += materialDiagnostic(
                    "material.package.version.ambiguous",
                    packageId.value,
                    "Compiled binding rules must target one exact representation package version.",
                )
                return@mapNotNull null
            }
            val coordinates = representationCoordinates(packageId, versions.single())
            val descriptorIds = rules.map { rule -> rule.target.descriptorId }.distinct().sortedBy { it.value }
            val descriptors = compiled.descriptors.filter { descriptor ->
                descriptor.representationPackageId == packageId && descriptor.descriptorId in descriptorIds
            }
            val entries = descriptors.map { descriptor ->
                RepresentationPackageDescriptorEntry(
                    descriptorId = descriptor.descriptorId,
                    resourceId = descriptor.resource.resourceId,
                    variants = descriptor.variants.distinct().sortedBy { variant -> variant.value },
                    styleTokenRefs = descriptor.styleTokenRefs,
                )
            }
            RepresentationPackageDescriptor(
                packageId = packageId,
                coordinates = coordinates,
                supportedProfiles = profiles.map { profile -> RepresentationSupportedProfile(profile.profileId) },
                descriptorEntries = entries,
                resourceReferences = descriptors.map { descriptor ->
                    GraphicResourceRef(descriptor.resource.resourceId, descriptor.resource.kind, descriptor.resource.resourceId.value)
                },
                styleTokenRefs = entries.flatMap { entry -> entry.styleTokenRefs }.distinct(),
                variants = entries.flatMap { entry -> entry.variants }.distinct().map { variant ->
                    RepresentationVariantDefinition(variant, variant.value)
                },
                lifecycle = RepresentationPackageLifecycle(RepresentationPackageLifecycleState.ACTIVE, coordinates.version),
                provenance = RepresentationPackageProvenance(
                    sources = compiled.evidence.stagedSourcePaths,
                    reviewedBy = "Athena representation package snapshot compiler",
                ),
            )
        }.sortedBy { representationPackage -> representationPackage.packageId.value }

    fun compatibleProfile(
        profile: PresentationProfileDescriptor,
        packageName: String,
        packageVersion: String,
    ): PresentationProfileDescriptor = profile.copy(
        compatibilityConstraints = (
            profile.compatibilityConstraints + PresentationPackageCompatibilityConstraint(packageName, packageVersion)
            ).distinct(),
    )

    private fun engineeringCoordinates(packageName: String, version: String): EngineeringPackageCoordinates {
        val (group, artifact) = splitCoordinates(packageName)
        return EngineeringPackageCoordinates(
            EngineeringPackageGroupId(group),
            EngineeringPackageArtifactId(artifact),
            EngineeringPackageVersion(version),
        )
    }

    private fun representationCoordinates(
        packageId: RepresentationPackageId,
        version: RepresentationPackageVersion,
    ): RepresentationPackageCoordinates {
        val (group, artifact) = splitCoordinates(packageId.value)
        return RepresentationPackageCoordinates(
            RepresentationPackageGroupId(group),
            RepresentationPackageArtifactId(artifact),
            version,
        )
    }

    private fun splitCoordinates(value: String): Pair<String, String> {
        val segments = value.split('.')
        return (segments.dropLast(1).joinToString(".").ifBlank { value }) to segments.last()
    }
}
