package com.engineeringood.athena.packageruntime

import com.engineeringood.athena.packageplatform.*

data class PackageItemAdmissionRequest(
    val metadata: PackageItemMetadata,
    val payload: PackageItemValue.ObjectValue,
    val authoredFieldNames: Set<String>,
    val resolvedReferences: Map<String, PackageItemIdentity> = emptyMap(),
    val requiredReferenceNames: Set<String> = emptySet(),
)

data class PackageItemAdmissionResult(
    val metadata: PackageItemMetadata,
    val admissionState: PackageItemAdmissionState,
    val item: AdmittedPackageItem?,
    val diagnostics: List<PackageItemDiagnostic>,
) {
    val isValid: Boolean get() = admissionState == PackageItemAdmissionState.PACKAGE_READY && item != null

    init {
        require((admissionState == PackageItemAdmissionState.PACKAGE_READY) == (item != null))
    }
}

object PackageItemAdmission {
    private val forbiddenAuthoredFields = setOf(
        "canonicalDigest", "digest", "admissionState", "state", "diagnostics", "resolvedReferences",
    )

    fun admit(request: PackageItemAdmissionRequest): PackageItemAdmissionResult {
        val metadata = normalizeMetadata(request.metadata)
        val forbidden = request.authoredFieldNames.intersect(forbiddenAuthoredFields)
        if (forbidden.isNotEmpty()) {
            val diagnostics = forbidden.sorted().map { field ->
                PackageItemDiagnostic(
                    metadata.identity.key,
                    "Authored PackageItem sets compiler-owned field `$field`.",
                    "Remove `$field`; compiler admission derives it.",
                    "package.item.authored-derived-field",
                )
            }
            return PackageItemAdmissionResult(metadata, PackageItemAdmissionState.PACKAGE_INVALID, null, diagnostics)
        }

        val unresolved = request.requiredReferenceNames - request.resolvedReferences.keys
        if (unresolved.isNotEmpty()) {
            val diagnostics = unresolved.sorted().map { reference ->
                PackageItemDiagnostic(
                    metadata.identity.key,
                    "PackageItem reference `$reference` is unresolved.",
                    "Declare and admit the referenced PackageItem before publishing this package.",
                    "package.item.reference.unresolved",
                )
            }
            return PackageItemAdmissionResult(metadata, PackageItemAdmissionState.PACKAGE_INCOMPLETE, null, diagnostics)
        }

        val normalized = runCatching { PackageItemCanonicalizer.normalize(request.payload) }.getOrElse { cause ->
            val diagnostic = PackageItemDiagnostic(
                metadata.identity.key,
                "PackageItem payload cannot be normalized: ${cause.message ?: cause::class.simpleName}.",
                "Use valid typed values and package-relative resource paths.",
                "package.item.payload.invalid",
            )
            return PackageItemAdmissionResult(metadata, PackageItemAdmissionState.PACKAGE_INVALID, null, listOf(diagnostic))
        }
        val digest = PackageItemCanonicalizer.digest(metadata, normalized)
        return PackageItemAdmissionResult(
            metadata = metadata,
            admissionState = PackageItemAdmissionState.PACKAGE_READY,
            item = AdmittedPackageItem(
                metadata = metadata,
                normalizedPayload = normalized,
                resolvedReferences = request.resolvedReferences.toSortedMap(),
                canonicalDigest = digest,
                admissionState = PackageItemAdmissionState.PACKAGE_READY,
                diagnostics = emptyList(),
            ),
            diagnostics = emptyList(),
        )
    }

    private fun normalizeMetadata(metadata: PackageItemMetadata): PackageItemMetadata = metadata.copy(
        provenance = metadata.provenance.copy(source = metadata.provenance.source.replace('\\', '/')),
        payloadReference = metadata.payloadReference.replace('\\', '/'),
    )
}
