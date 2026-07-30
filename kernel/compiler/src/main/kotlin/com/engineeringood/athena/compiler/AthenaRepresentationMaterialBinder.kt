package com.engineeringood.athena.compiler

import com.engineeringood.athena.packageplatform.BindingManifest
import com.engineeringood.athena.packageplatform.BindingManifestId
import com.engineeringood.athena.packageplatform.BindingManifestProvenance
import com.engineeringood.athena.packageplatform.EngineeringPackageDescriptor
import com.engineeringood.athena.packageplatform.PresentationProfileDescriptor
import com.engineeringood.athena.packageplatform.PresentationProfileTag
import com.engineeringood.athena.packageplatform.ProjectionContextId
import com.engineeringood.athena.packageplatform.RepresentationAnchorId
import com.engineeringood.athena.packageplatform.RepresentationBindingRule
import com.engineeringood.athena.packageplatform.RepresentationDescriptor
import com.engineeringood.athena.packageplatform.RepresentationLabelSlotId
import com.engineeringood.athena.packageplatform.RepresentationPackageDescriptor
import com.engineeringood.athena.packageruntime.BindingResolutionRequest
import com.engineeringood.athena.packageruntime.BindingResolver
import com.engineeringood.athena.packageruntime.BindingSubject
import com.engineeringood.athena.representation.RepresentationDefinition
import com.engineeringood.athena.representation.RepresentationDefinitionKind
import com.engineeringood.athena.representation.RepresentationAnchorRole
import com.engineeringood.athena.representation.RepresentationDirectionPredicate

internal class AthenaRepresentationMaterialBinder(
    private val bindingResolver: BindingResolver,
) {
    fun resolve(
        subject: MaterialSubject,
        projectionContext: ProjectionContextId,
        engineeringPackage: EngineeringPackageDescriptor,
        activeProfile: PresentationProfileDescriptor,
        representationPackages: List<RepresentationPackageDescriptor>,
        descriptors: List<RepresentationDescriptor>,
        bindingRules: List<RepresentationBindingRule>,
        definitionsByKey: Map<Pair<String, String>, RepresentationDefinition>,
        diagnostics: MutableList<AthenaRepresentationMaterialDiagnostic>,
    ): AthenaResolvedRepresentationMaterial? {
        val subjectRules = bindingRules.filter { rule ->
            rule.subjectKind == subject.subjectKind && rule.conceptId == subject.conceptId
        }
        val admittedPackages = subjectRules.map { rule -> rule.target.representationPackageId.value }.distinct().sorted()
        if (admittedPackages.isEmpty()) {
            diagnostics += materialDiagnostic(
                "material.binding.rule.missing",
                subject.semanticSubjectId,
                "No typed binding rule owns this subject.",
            )
            return null
        }
        val manifest = BindingManifest(
            manifestId = BindingManifestId("compiled:${subject.subjectKind.name.lowercase()}:${subject.conceptId.value}"),
            engineeringPackageId = engineeringPackage.packageId.value,
            engineeringPackageVersionRange = engineeringPackage.coordinates.version.value,
            conceptId = subject.conceptId,
            defaultRepresentationPackageId = admittedPackages.first(),
            alternativeRepresentationPackageIds = admittedPackages.drop(1),
            compatibleProfileTags = listOf(PresentationProfileTag(activeProfile.profileId.value)),
            provenance = BindingManifestProvenance(
                sources = subjectRules.flatMap { rule -> rule.provenance.sources }.distinct().sorted(),
                reviewedBy = "Athena representation material compiler",
            ),
        )
        val result = bindingResolver.resolve(
            BindingResolutionRequest(
                subject = BindingSubject(
                    semanticSubjectId = subject.semanticSubjectId,
                    conceptId = subject.conceptId,
                    requiredAnchorBindings = subject.ports.associate { port ->
                        port.id.value to RepresentationAnchorId(port.name)
                    }.toSortedMap(),
                    requiredLabelBindings = mapOf(RepresentationLabelSlotId("deviceTag") to subject.label),
                    semanticFacts = subject.semanticFacts,
                    subjectKind = subject.subjectKind,
                ),
                projectionContext = projectionContext,
                engineeringPackage = engineeringPackage,
                manifest = manifest,
                activeProfile = activeProfile,
                representationPackages = representationPackages,
                descriptors = descriptors,
                bindingRules = bindingRules,
            ),
        )
        diagnostics += result.diagnostics.map { issue ->
            materialDiagnostic(issue.code.wireValue, issue.subject, issue.message)
        }
        val resolution = result.resolution ?: return null
        val definition = definitionsByKey[resolution.representationPackageId.value to resolution.descriptorId.value]
        if (definition == null || definition.definitionKind != RepresentationDefinitionKind.ELEMENT) {
            diagnostics += materialDiagnostic(
                "material.definition.missing",
                resolution.descriptorId.value,
                "Binding resolution did not select one compiled package-local Element definition.",
            )
            return null
        }
        if (!validateCompatibility(subject, definition, diagnostics)) return null

        val terminalBindings = subject.ports.associate { port ->
            port.id.value to (port.materialPropertyValue("terminal") ?: "")
        }.toSortedMap()
        val missingTerminals = terminalBindings.filterValues(String::isBlank).keys
        if (missingTerminals.isNotEmpty()) {
            diagnostics += missingTerminals.map { portId ->
                materialDiagnostic(
                    "material.terminal.missing",
                    portId,
                    "Resolved representation ports require authored terminal identity.",
                )
            }
            return null
        }
        return AthenaResolvedRepresentationMaterial(
            semanticSubjectId = subject.semanticSubjectId,
            physicalComponentId = subject.physicalComponentId,
            functionId = subject.functionId,
            definition = definition,
            resolution = resolution,
            terminalBindings = terminalBindings,
        )
    }

    private fun validateCompatibility(
        subject: MaterialSubject,
        definition: RepresentationDefinition,
        diagnostics: MutableList<AthenaRepresentationMaterialDiagnostic>,
    ): Boolean {
        val initialSize = diagnostics.size
        val anchors = definition.anchors.associateBy { anchor -> anchor.anchorId.value }
        subject.ports.forEach { port ->
            val anchor = anchors[port.name] ?: return@forEach
            if (anchor.role != RepresentationAnchorRole.TERMINAL) {
                diagnostics += materialDiagnostic(
                    "material.anchor.role.incompatible",
                    port.id.value,
                    "Project ports may bind only to compiled terminal anchors; '${anchor.anchorId.value}' is ${anchor.role.name.lowercase()}.",
                )
                return@forEach
            }
            if (anchor.acceptedDirections.isEmpty()) {
                diagnostics += materialDiagnostic(
                    "material.anchor.direction-contract.missing",
                    port.id.value,
                    "Compiled terminal anchor '${anchor.anchorId.value}' has no governed direction compatibility contract.",
                )
            } else {
                val authoredDirection = port.materialPropertyValue("direction")
                val direction = authoredDirection?.let(::directionPredicate)
                when {
                    authoredDirection == null -> diagnostics += materialDiagnostic(
                        "material.port.direction.missing",
                        port.id.value,
                        "Port direction is required by compiled Element anchor '${anchor.anchorId.value}'.",
                    )
                    direction == null -> diagnostics += materialDiagnostic(
                        "material.port.direction.unknown",
                        port.id.value,
                        "Port direction '$authoredDirection' is not a governed direction predicate.",
                    )
                    direction !in anchor.acceptedDirections &&
                        RepresentationDirectionPredicate.BIDIRECTIONAL !in anchor.acceptedDirections -> diagnostics += materialDiagnostic(
                        "material.anchor.direction.incompatible",
                        port.id.value,
                        "Port direction is incompatible with compiled Element anchor '${anchor.anchorId.value}'.",
                    )
                }
            }
            if (anchor.acceptedSignals.isEmpty()) {
                diagnostics += materialDiagnostic(
                    "material.anchor.signal-contract.missing",
                    port.id.value,
                    "Compiled terminal anchor '${anchor.anchorId.value}' has no governed signal compatibility contract.",
                )
            } else {
                val signal = port.materialPropertyValue("signal")?.takeIf(String::isNotBlank)
                when {
                    signal == null -> diagnostics += materialDiagnostic(
                        "material.port.signal.missing",
                        port.id.value,
                        "Port signal is required by compiled Element anchor '${anchor.anchorId.value}'.",
                    )
                    anchor.acceptedSignals.none { accepted -> acceptedSignalMatches(signal, accepted.value) } -> diagnostics += materialDiagnostic(
                        "material.anchor.signal.incompatible",
                        port.id.value,
                        "Port signal is.family incompatible with compiled Element anchor '${anchor.anchorId.value}'.",
                    )
                }
            }
        }
        return diagnostics.size == initialSize
    }

    private fun directionPredicate(value: String): RepresentationDirectionPredicate? = when (value) {
        "in" -> RepresentationDirectionPredicate.IN
        "out" -> RepresentationDirectionPredicate.OUT
        "bidirectional" -> RepresentationDirectionPredicate.BIDIRECTIONAL
        "passive" -> RepresentationDirectionPredicate.BIDIRECTIONAL
        else -> null
    }

    private fun acceptedSignalMatches(portSignal: String, acceptedSignal: String): Boolean {
        val normalizedPortSignal = portSignal.removeSuffix(".family")
        val normalizedAcceptedSignal = acceptedSignal.removeSuffix(".family")
        return normalizedPortSignal == normalizedAcceptedSignal
    }
}
