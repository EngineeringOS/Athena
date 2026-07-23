package com.engineeringood.athena.packageruntime

import com.engineeringood.athena.packageplatform.PresentationProfileId
import com.engineeringood.athena.packageplatform.RepresentationDescriptorValidationContext
import com.engineeringood.athena.packageplatform.RepresentationDescriptorValidator
import com.engineeringood.athena.packageplatform.RepresentationLabelSlotId
import com.engineeringood.athena.representation.RepresentationOccurrenceRole
import com.engineeringood.athena.representation.RepresentationProjectionKind
import com.engineeringood.athena.representation.RepresentationProjectionOccurrenceId
import com.engineeringood.athena.representation.RepresentationSemanticRole
import com.engineeringood.athena.representation.RepresentationSubjectKind
import kotlin.io.path.readText

data class M32ProductSmokeProof(
    val acceptanceAuthority: String,
    val subjects: List<M32ProductSmokeSubjectProof>,
    val routes: List<M32ProductSmokeRouteProof>,
    val profileSwitch: M32ProductSmokeProfileSwitchProof,
    val visualEvidence: M32ProductSmokeVisualEvidenceProof,
) {
    val isValid: Boolean
        get() = acceptanceAuthority == "structured-proof" &&
            subjects.isNotEmpty() &&
            subjects.all { it.isValid } &&
            routes.any { it.routeAnchored } &&
            routes.all { !it.centerFallbackAccepted } &&
            profileSwitch.sourceUnchanged &&
            profileSwitch.representationChanged &&
            !visualEvidence.satisfiesPackageClaimsWithoutStructuredProof
}

data class M32ProductSmokeSubjectProof(
    val semanticSubjectId: String,
    val engineeringPackageResolved: Boolean,
    val representationPackageResolved: Boolean,
    val manifestSelected: Boolean,
    val descriptorValidated: Boolean,
    val anchorMapped: Boolean,
    val labelBound: Boolean,
    val occurrenceCreated: Boolean,
    val derivedBounds: Boolean,
    val rendererFallbackAccepted: Boolean,
) {
    val isValid: Boolean
        get() = engineeringPackageResolved &&
            representationPackageResolved &&
            manifestSelected &&
            descriptorValidated &&
            anchorMapped &&
            labelBound &&
            occurrenceCreated &&
            derivedBounds &&
            !rendererFallbackAccepted
}

data class M32ProductSmokeRouteProof(
    val connectionSemanticId: String,
    val routeAnchored: Boolean,
    val centerFallbackAccepted: Boolean,
)

data class M32ProductSmokeProfileSwitchProof(
    val semanticSubjectId: String,
    val sourceUnchanged: Boolean,
    val representationChanged: Boolean,
)

data class M32ProductSmokeVisualEvidenceProof(
    val role: String,
    val screenshotRefs: List<String>,
    val structuredAssertionCount: Int,
    val satisfiesPackageClaimsWithoutStructuredProof: Boolean,
)

class M32ProductSmokeProofRunner {
    fun run(sample: M32SamplePackageSet): M32ProductSmokeProof {
        val subjectProofs = sample.semanticSubjectIds.map { subjectId -> subjectProof(sample, subjectId) }
        val routeProofs = listOf(routeProof(sample))
        val profileSwitch = profileSwitchProof(sample)
        return M32ProductSmokeProof(
            acceptanceAuthority = "structured-proof",
            subjects = subjectProofs,
            routes = routeProofs,
            profileSwitch = profileSwitch,
            visualEvidence = M32ProductSmokeVisualEvidenceProof(
                role = "secondary-human-review",
                screenshotRefs = emptyList(),
                structuredAssertionCount = subjectProofs.size * 8 + routeProofs.size + 2,
                satisfiesPackageClaimsWithoutStructuredProof = false,
            ),
        )
    }

    private fun subjectProof(
        sample: M32SamplePackageSet,
        semanticSubjectId: String,
    ): M32ProductSmokeSubjectProof {
        val request = sample.bindingRequestForSubject(semanticSubjectId, PresentationProfileId("m32-iec"))
        val result = BindingResolver().resolve(request)
        val evidence = BindingEvidencePayloadMapper.from(request, result)
        val descriptor = sample.descriptorFor(evidence.descriptorId)
        val representationPackage = sample.representationPackages
            .firstOrNull { representationPackage -> representationPackage.packageId.value == evidence.representationPackageId }
        val descriptorValidated = descriptor?.let {
            RepresentationDescriptorValidator.validate(
                descriptor = it,
                context = RepresentationDescriptorValidationContext(
                    resourceReferences = representationPackage?.resourceReferences.orEmpty(),
                    requiredLabelSlots = setOf(RepresentationLabelSlotId("device-tag")),
                    supportedVariants = representationPackage?.variants.orEmpty().map { variant -> variant.variantId }.toSet(),
                ),
            ).isValid
        } ?: false
        val occurrence = PackageBackedRepresentationOccurrenceFactory().create(
            PackageBackedRepresentationOccurrenceRequest(
                bindingEvidence = evidence,
                descriptor = descriptor,
                projectionOccurrenceId = RepresentationProjectionOccurrenceId("sheet:m32/$semanticSubjectId"),
                subjectKind = RepresentationSubjectKind.COMPONENT,
                semanticRole = RepresentationSemanticRole("m32-demo"),
                projectionKind = RepresentationProjectionKind.ELECTRICAL_SCHEMATIC,
                occurrenceRole = RepresentationOccurrenceRole.LOAD_SYMBOL,
            ),
        )
        val renderPayload = descriptor?.let {
            DescriptorBackedGraphicResourceRenderPayloadMapper.from(
                evidence = evidence,
                descriptor = it,
                governedMargin = 16.0,
                interactionState = "normal",
            )
        }
        val derivedBounds = renderPayload != null &&
            renderPayload.viewBox.width > renderPayload.bounds.width &&
            renderPayload.viewBox.height > renderPayload.bounds.height &&
            !renderPayload.normalBackgroundVisible &&
            !renderPayload.normalHitboxVisible

        return M32ProductSmokeSubjectProof(
            semanticSubjectId = semanticSubjectId,
            engineeringPackageResolved = request.engineeringPackage.packageId.value == evidence.engineeringPackageId,
            representationPackageResolved = evidence.representationPackageId != null,
            manifestSelected = request.manifest.engineeringPackageId == evidence.engineeringPackageId,
            descriptorValidated = descriptorValidated,
            anchorMapped = evidence.anchorMapSummary.isNotEmpty(),
            labelBound = evidence.labelBindingSummary.isNotEmpty(),
            occurrenceCreated = occurrence.occurrence != null,
            derivedBounds = derivedBounds,
            rendererFallbackAccepted = evidence.rendererFallbackAccepted || occurrence.rendererFallbackAccepted,
        )
    }

    private fun routeProof(sample: M32SamplePackageSet): M32ProductSmokeRouteProof {
        val source = bindingEvidence(sample, "device:MainPowerSupplyPS32")
        val target = bindingEvidence(sample, "device:ControlRelayK32")
        val result = DescriptorAnchorRouteEvidenceMapper.map(
            DescriptorAnchorRouteEvidenceRequest(
                connectionSemanticId = "connection:MainPowerSupplyPS32.lplus->ControlRelayK32.supply",
                sourceSemanticTerminalId = "port:MainPowerSupplyPS32.lplus",
                targetSemanticTerminalId = "port:ControlRelayK32.supply",
                sourceBindingEvidence = source,
                targetBindingEvidence = target,
                sourceDescriptor = sample.descriptorFor(source.descriptorId),
                targetDescriptor = sample.descriptorFor(target.descriptorId),
            ),
        )
        return M32ProductSmokeRouteProof(
            connectionSemanticId = "connection:MainPowerSupplyPS32.lplus->ControlRelayK32.supply",
            routeAnchored = result.route != null && result.diagnostics.isEmpty(),
            centerFallbackAccepted = result.centerFallbackAccepted,
        )
    }

    private fun bindingEvidence(sample: M32SamplePackageSet, semanticSubjectId: String): BindingEvidencePayload {
        val request = sample.bindingRequestForSubject(semanticSubjectId, PresentationProfileId("m32-iec"))
        return BindingEvidencePayloadMapper.from(request, BindingResolver().resolve(request))
    }

    private fun profileSwitchProof(sample: M32SamplePackageSet): M32ProductSmokeProfileSwitchProof {
        val sourceBefore = sample.sourceFile.readText()
        val iec = sample.resolveSubject("device:ShutterMotorM32", PresentationProfileId("m32-iec")).resolution
        val compact = sample.resolveSubject("device:ShutterMotorM32", PresentationProfileId("m32-compact")).resolution
        return M32ProductSmokeProfileSwitchProof(
            semanticSubjectId = "device:ShutterMotorM32",
            sourceUnchanged = sourceBefore == sample.sourceFile.readText(),
            representationChanged = iec?.representationPackageId != compact?.representationPackageId &&
                iec?.descriptorId != compact?.descriptorId,
        )
    }
}
