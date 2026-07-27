package com.engineeringood.athena.compiler

import com.engineeringood.athena.packageruntime.RepresentationPackageSnapshot
import com.engineeringood.athena.packageplatform.GraphicResourceId
import com.engineeringood.athena.packageplatform.GraphicResourceKind
import com.engineeringood.athena.packageplatform.PackageResourceDeclaration
import com.engineeringood.athena.packageplatform.RepresentationAnchorDefinition
import com.engineeringood.athena.packageplatform.RepresentationAnchorId as DescriptorAnchorId
import com.engineeringood.athena.packageplatform.RepresentationAnchorSide
import com.engineeringood.athena.packageplatform.RepresentationDescriptor
import com.engineeringood.athena.packageplatform.RepresentationDescriptorBounds
import com.engineeringood.athena.packageplatform.RepresentationDescriptorId
import com.engineeringood.athena.packageplatform.RepresentationDescriptorResourceBinding
import com.engineeringood.athena.packageplatform.RepresentationLabelSlotDefinition
import com.engineeringood.athena.packageplatform.RepresentationLabelSlotId as DescriptorLabelSlotId
import com.engineeringood.athena.packageplatform.RepresentationLabelSlotPlacement
import com.engineeringood.athena.packageplatform.RepresentationLabelSlotRole
import com.engineeringood.athena.packageplatform.RepresentationStyleTokenRef
import com.engineeringood.athena.packageplatform.RepresentationVariantId as DescriptorVariantId
import com.engineeringood.athena.representation.PresentationLabelRole
import com.engineeringood.athena.representation.RepresentationAnchorContract
import com.engineeringood.athena.representation.RepresentationAnchorRole
import com.engineeringood.athena.representation.RepresentationDefinition
import java.nio.file.Files
import kotlin.math.abs

class AthenaRepresentationPackageSnapshotCompiler(
    private val compiler: AthenaRepresentationSourceCompiler = AthenaRepresentationSourceCompiler(),
) {
    fun compile(snapshot: RepresentationPackageSnapshot): AthenaRepresentationPackageSnapshotCompilationResult {
        val inputs = snapshot.files
            .filter { file -> file.repositoryRelativePath.endsWith(".athena") }
            .sortedBy { file -> file.repositoryRelativePath }
            .map { file ->
                AthenaRepresentationSourceInput(
                    file = file.stagedPath.toString(),
                    source = Files.readString(file.stagedPath),
                )
            }
        val compiled = compiler.compile(inputs)
        val generatedDescriptors = compiled.definitions.map { definition -> definition.toGeneratedDescriptor() }
        return AthenaRepresentationPackageSnapshotCompilationResult(
            definitions = compiled.definitions,
            descriptors = generatedDescriptors,
            profiles = compiled.profiles,
            bindingRules = compiled.bindingRules,
            resources = compiled.resources,
            diagnostics = compiled.diagnostics,
            proof = AthenaRepresentationPackageSnapshotProof(
                snapshotId = snapshot.snapshotId,
                dependencyLockDigest = snapshot.dependencyLockDigest,
                compilerSchemaVersion = snapshot.compilerSchemaVersion,
                stagedSourcePaths = snapshot.files.map { it.stagedPath.toString() }.sorted(),
                sourceHashes = snapshot.files.associate { it.repositoryRelativePath to it.contentHash }.toSortedMap(),
                stagedSourceExtensions = snapshot.files.map { file ->
                    file.repositoryRelativePath.substringAfterLast('.', missingDelimiterValue = "").lowercase()
                }.toSortedSet(),
                generatedResourceIds = generatedDescriptors.map { descriptor -> descriptor.resource.resourceId.value }.sorted(),
                compiledBodyAuthorities = compiled.definitions.map { definition -> definition.bodyAuthority.name }.toSortedSet(),
            ),
        )
    }
}

data class AthenaRepresentationPackageSnapshotCompilationResult(
    val definitions: List<RepresentationDefinition>,
    val descriptors: List<RepresentationDescriptor> = emptyList(),
    val profiles: List<com.engineeringood.athena.packageplatform.PresentationProfileDescriptor> = emptyList(),
    val bindingRules: List<com.engineeringood.athena.packageplatform.RepresentationBindingRule> = emptyList(),
    val resources: List<PackageResourceDeclaration> = emptyList(),
    val diagnostics: List<AthenaRepresentationSourceDiagnostic>,
    val proof: AthenaRepresentationPackageSnapshotProof,
)

data class AthenaRepresentationPackageSnapshotProof(
    val snapshotId: String,
    val dependencyLockDigest: String,
    val compilerSchemaVersion: String,
    val stagedSourcePaths: List<String>,
    val sourceHashes: Map<String, String>,
    val stagedSourceExtensions: Set<String>,
    val generatedResourceIds: List<String>,
    val compiledBodyAuthorities: Set<String>,
) {
    val rendererFileAccessAuthorityAbsent: Boolean
        get() = generatedResourceIds.isNotEmpty() && generatedResourceIds.all { resourceId ->
            resourceId.startsWith("generated:") && !resourceId.contains('/') && !resourceId.contains('\\')
        }

    val xmlRuntimeAuthorityAbsent: Boolean
        get() = stagedSourceExtensions.none { extension -> extension in FOREIGN_RUNTIME_EXTENSIONS }

    val rawSvgTransportAbsent: Boolean
        get() = compiledBodyAuthorities.isNotEmpty() && compiledBodyAuthorities == setOf("GRAPHIC_PRIMITIVE")
}

private val FOREIGN_RUNTIME_EXTENSIONS = setOf("elmt", "qet", "xml", "xsd")

private fun RepresentationDefinition.toGeneratedDescriptor(): RepresentationDescriptor {
    val bounds = graphicBody.bounds
    val descriptorBounds = if (bounds != null) {
        RepresentationDescriptorBounds(width = bounds.width, height = bounds.height)
    } else {
        RepresentationDescriptorBounds(
            width = anatomy.bounds.width.value.toDouble(),
            height = anatomy.bounds.height.value.toDouble(),
        )
    }
    return RepresentationDescriptor(
        descriptorId = RepresentationDescriptorId(symbolId.value),
        resource = RepresentationDescriptorResourceBinding(
            resourceId = GraphicResourceId("generated:${libraryId.value}:${symbolId.value}:${version.value}"),
            kind = GraphicResourceKind.VECTOR_DOCUMENT,
        ),
        bounds = descriptorBounds,
        anchors = anchors.map { anchor ->
            RepresentationAnchorDefinition(
                anchorId = DescriptorAnchorId(anchor.anchorId.value),
                x = anchor.point.x,
                y = anchor.point.y,
                side = anchor.toDescriptorSide(bounds),
            )
        },
        labelSlots = labelSlots.map { slot ->
            val origin = slot.origin
            val labelBounds = slot.bounds
            RepresentationLabelSlotDefinition(
                slotId = DescriptorLabelSlotId(slot.slotId.value),
                role = slot.role.toDescriptorLabelRole(),
                required = true,
                placement = if (origin != null && labelBounds != null) {
                    RepresentationLabelSlotPlacement(
                        originX = origin.x,
                        originY = origin.y,
                        boundsX = labelBounds.x,
                        boundsY = labelBounds.y,
                        width = labelBounds.width,
                        height = labelBounds.height,
                    )
                } else {
                    null
                },
                styleTokenRef = slot.styleTokenId?.value?.let(::RepresentationStyleTokenRef),
            )
        },
        variants = variants.map { variant -> DescriptorVariantId(variant.value) },
        representationPackageId = com.engineeringood.athena.packageplatform.RepresentationPackageId(libraryId.value),
    )
}

private fun RepresentationAnchorContract.toDescriptorSide(
    bounds: com.engineeringood.athena.representation.GraphicBounds?,
): RepresentationAnchorSide {
    if (role !in setOf(RepresentationAnchorRole.TERMINAL, RepresentationAnchorRole.REFERENCE) || bounds == null) {
        return RepresentationAnchorSide.CENTER
    }
    return listOf(
        RepresentationAnchorSide.LEFT to abs(point.x - bounds.x),
        RepresentationAnchorSide.RIGHT to abs(point.x - (bounds.x + bounds.width)),
        RepresentationAnchorSide.TOP to abs(point.y - bounds.y),
        RepresentationAnchorSide.BOTTOM to abs(point.y - (bounds.y + bounds.height)),
    ).minBy { (_, distance) -> distance }.first
}

private fun PresentationLabelRole.toDescriptorLabelRole(): RepresentationLabelSlotRole = when (this) {
    PresentationLabelRole.DEVICE_TAG -> RepresentationLabelSlotRole.DEVICE_TAG
    PresentationLabelRole.COMPONENT_LABEL,
    PresentationLabelRole.DYNAMIC_TEXT,
        -> RepresentationLabelSlotRole.MODEL
    PresentationLabelRole.TERMINAL_LABEL -> RepresentationLabelSlotRole.TERMINAL_NUMBER
    PresentationLabelRole.ROUTE_LABEL -> RepresentationLabelSlotRole.REFERENCE
}
