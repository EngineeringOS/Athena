package com.engineeringood.athena.packageruntime

object BindingEvidencePayloadMapper {
    fun from(
        request: BindingResolutionRequest,
        result: BindingResolutionResult,
    ): BindingEvidencePayload {
        val resolution = result.resolution
        val representationPackageVersion = resolution?.representationPackageId?.let { packageId ->
            request.representationPackages.firstOrNull { it.packageId == packageId }
                ?.coordinates
                ?.version
                ?.value
        }

        return BindingEvidencePayload(
            semanticSubjectId = request.subject.semanticSubjectId,
            engineeringPackageId = request.engineeringPackage.packageId.value,
            engineeringPackageVersion = request.engineeringPackage.coordinates.version.value,
            presentationProfileId = request.activeProfile.profileId.value,
            bindingManifestId = request.manifest.manifestId.value,
            representationPackageId = resolution?.representationPackageId?.value,
            representationPackageVersion = representationPackageVersion,
            descriptorId = resolution?.descriptorId?.value,
            variant = resolution?.variantId?.value,
            anchorMapSummary = resolution?.anchorMapping
                .orEmpty()
                .entries
                .sortedBy { it.key }
                .map { (semanticAnchor, descriptorAnchor) -> "$semanticAnchor=${descriptorAnchor.value}" },
            labelBindingSummary = resolution?.labelBinding
                .orEmpty()
                .entries
                .sortedBy { it.key.value }
                .map { (slotId, value) -> "${slotId.value}=$value" },
            resolverStage = "binding-resolver-v0",
            diagnostics = result.diagnostics.map { it.toPayload() },
            rendererFallbackAccepted = result.rendererFallbackAccepted,
        )
    }

    private fun BindingResolverDiagnostic.toPayload(): BindingEvidenceDiagnosticPayload =
        BindingEvidenceDiagnosticPayload(
            severity = severity.name.lowercase(),
            code = code.wireValue,
            authority = authority.toWireValue(),
            subject = subject,
            message = message,
        )

    private fun BindingAuthority.toWireValue(): String = name.lowercase().replace('_', '-')
}
