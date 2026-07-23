package com.engineeringood.athena.packageplatform

enum class RepresentationPackageDiagnosticSeverity {
    ERROR,
}

@JvmInline
value class RepresentationPackageDiagnosticCode(val wireValue: String) {
    override fun toString(): String = wireValue
}

data class RepresentationPackageDiagnostic(
    val code: RepresentationPackageDiagnosticCode,
    val severity: RepresentationPackageDiagnosticSeverity,
    val subject: String,
    val message: String,
)

data class RepresentationPackageValidationResult(
    val diagnostics: List<RepresentationPackageDiagnostic>,
) {
    val isValid: Boolean
        get() = diagnostics.none { it.severity == RepresentationPackageDiagnosticSeverity.ERROR }
}

object RepresentationPackageDescriptorValidator {
    private val packageIdPattern = Regex("[a-z][a-z0-9]*(\\.[a-z][a-z0-9-]*)+")
    private val groupIdPattern = Regex("[a-z][a-z0-9]*(\\.[a-z][a-z0-9-]*)+")
    private val artifactIdPattern = Regex("[a-z][a-z0-9]*(?:-[a-z0-9]+)*")
    private val versionPattern = Regex("\\d+\\.\\d+\\.\\d+(?:[-+][A-Za-z0-9.-]+)?")
    private val supportedResourceKinds = setOf(GraphicResourceKind.VECTOR_DOCUMENT)

    fun validate(descriptor: RepresentationPackageDescriptor): RepresentationPackageValidationResult {
        val diagnostics = mutableListOf<RepresentationPackageDiagnostic>()

        validateIdentity(descriptor, diagnostics)
        validateProfiles(descriptor, diagnostics)
        validateResources(descriptor, diagnostics)
        validateDescriptorEntries(descriptor, diagnostics)
        validatePreviews(descriptor, diagnostics)
        validateProvenance(descriptor, diagnostics)

        descriptor.forbiddenAuthorityFields.forEach { field ->
            diagnostics += diagnostic(
                code = "package.representation.semantic-leak-forbidden",
                subject = field.field,
                message = "Representation package descriptors must not contain ${field.authority.name.lowercase()} authority fields.",
            )
        }

        return RepresentationPackageValidationResult(diagnostics)
    }

    private fun validateIdentity(
        descriptor: RepresentationPackageDescriptor,
        diagnostics: MutableList<RepresentationPackageDiagnostic>,
    ) {
        if (!packageIdPattern.matches(descriptor.packageId.value)) {
            diagnostics += diagnostic(
                code = "package.representation.identity.invalid",
                subject = "packageId",
                message = "Representation package id must be a stable reverse-domain identifier.",
            )
        }

        if (!groupIdPattern.matches(descriptor.coordinates.groupId.value)) {
            diagnostics += diagnostic(
                code = "package.representation.group.invalid",
                subject = "coordinates.groupId",
                message = "Representation package group id must be a stable reverse-domain identifier.",
            )
        }

        if (!artifactIdPattern.matches(descriptor.coordinates.artifactId.value)) {
            diagnostics += diagnostic(
                code = "package.representation.artifact.invalid",
                subject = "coordinates.artifactId",
                message = "Representation package artifact id must be a non-blank kebab-case identifier.",
            )
        }

        if (!versionPattern.matches(descriptor.coordinates.version.value)) {
            diagnostics += diagnostic(
                code = "package.representation.version.invalid",
                subject = "coordinates.version",
                message = "Representation package version must be semver-compatible.",
            )
        }
    }

    private fun validateProfiles(
        descriptor: RepresentationPackageDescriptor,
        diagnostics: MutableList<RepresentationPackageDiagnostic>,
    ) {
        if (descriptor.supportedProfiles.isEmpty()) {
            diagnostics += diagnostic(
                code = "package.representation.profile.invalid",
                subject = "supportedProfiles",
                message = "Representation package must declare at least one supported Presentation Profile.",
            )
        }

        descriptor.supportedProfiles.forEachIndexed { index, profile ->
            if (profile.profileId.value.isBlank()) {
                diagnostics += diagnostic(
                    code = "package.representation.profile.invalid",
                    subject = "supportedProfiles[$index].profileId",
                    message = "Supported Presentation Profile id must not be blank.",
                )
            }
            profile.tags.forEachIndexed { tagIndex, tag ->
                if (tag.value.isBlank()) {
                    diagnostics += diagnostic(
                        code = "package.representation.profile.invalid",
                        subject = "supportedProfiles[$index].tags[$tagIndex]",
                        message = "Supported Presentation Profile tag must not be blank.",
                    )
                }
            }
        }
    }

    private fun validateResources(
        descriptor: RepresentationPackageDescriptor,
        diagnostics: MutableList<RepresentationPackageDiagnostic>,
    ) {
        if (descriptor.resourceReferences.isEmpty()) {
            diagnostics += diagnostic(
                code = "package.representation.resource.invalid",
                subject = "resourceReferences",
                message = "Representation package must declare at least one Graphic Resource reference.",
            )
        }

        descriptor.resourceReferences.forEachIndexed { index, resource ->
            if (resource.resourceId.value.isBlank() || resource.path.isBlank()) {
                diagnostics += diagnostic(
                    code = "package.representation.resource.invalid",
                    subject = "resourceReferences[$index]",
                    message = "Graphic Resource id and path must not be blank.",
                )
            }

            if (resource.kind !in supportedResourceKinds) {
                diagnostics += diagnostic(
                    code = "package.representation.resource-kind.unsupported",
                    subject = "resourceReferences[$index].kind",
                    message = "Graphic Resource kind ${resource.kind.name} is deferred and unsupported by the M32 v0 backend.",
                )
            }
        }
    }

    private fun validateDescriptorEntries(
        descriptor: RepresentationPackageDescriptor,
        diagnostics: MutableList<RepresentationPackageDiagnostic>,
    ) {
        if (descriptor.descriptorEntries.isEmpty()) {
            diagnostics += diagnostic(
                code = "package.representation.descriptor.invalid",
                subject = "descriptorEntries",
                message = "Representation package must declare at least one descriptor entry.",
            )
        }

        val resourceIds = descriptor.resourceReferences.map { it.resourceId }.toSet()
        val variantIds = descriptor.variants.map { it.variantId }.toSet()
        val styleTokens = descriptor.styleTokenRefs.toSet()

        descriptor.descriptorEntries.forEachIndexed { index, entry ->
            if (entry.descriptorId.value.isBlank()) {
                diagnostics += diagnostic(
                    code = "package.representation.descriptor.invalid",
                    subject = "descriptorEntries[$index].descriptorId",
                    message = "Representation descriptor id must not be blank.",
                )
            }

            if (entry.resourceId !in resourceIds) {
                diagnostics += diagnostic(
                    code = "package.representation.resource.invalid",
                    subject = "descriptorEntries[$index].resourceId",
                    message = "Representation descriptor entry must reference a declared Graphic Resource.",
                )
            }

            entry.variants.forEachIndexed { variantIndex, variantId ->
                if (variantId !in variantIds) {
                    diagnostics += diagnostic(
                        code = "package.representation.variant.invalid",
                        subject = "descriptorEntries[$index].variants[$variantIndex]",
                        message = "Representation descriptor entry must reference a declared variant.",
                    )
                }
            }

            entry.styleTokenRefs.forEachIndexed { tokenIndex, styleToken ->
                if (styleToken !in styleTokens) {
                    diagnostics += diagnostic(
                        code = "package.representation.style-token.invalid",
                        subject = "descriptorEntries[$index].styleTokenRefs[$tokenIndex]",
                        message = "Representation descriptor entry must reference a declared style token.",
                    )
                }
            }
        }
    }

    private fun validatePreviews(
        descriptor: RepresentationPackageDescriptor,
        diagnostics: MutableList<RepresentationPackageDiagnostic>,
    ) {
        val variantIds = descriptor.variants.map { it.variantId }.toSet()
        descriptor.previews.forEachIndexed { index, preview ->
            if (preview.variantId !in variantIds || preview.path.isBlank()) {
                diagnostics += diagnostic(
                    code = "package.representation.preview.invalid",
                    subject = "previews[$index]",
                    message = "Representation preview must reference a declared variant and non-blank path.",
                )
            }
        }
    }

    private fun validateProvenance(
        descriptor: RepresentationPackageDescriptor,
        diagnostics: MutableList<RepresentationPackageDiagnostic>,
    ) {
        if (descriptor.provenance.sources.isEmpty() || descriptor.provenance.reviewedBy.isBlank()) {
            diagnostics += diagnostic(
                code = "package.representation.provenance.missing",
                subject = "provenance",
                message = "Representation package provenance must declare at least one source and reviewer.",
            )
        }
    }

    private fun diagnostic(
        code: String,
        subject: String,
        message: String,
    ): RepresentationPackageDiagnostic = RepresentationPackageDiagnostic(
        code = RepresentationPackageDiagnosticCode(code),
        severity = RepresentationPackageDiagnosticSeverity.ERROR,
        subject = subject,
        message = message,
    )
}
