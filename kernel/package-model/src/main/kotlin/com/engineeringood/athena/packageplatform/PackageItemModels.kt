package com.engineeringood.athena.packageplatform

import com.engineeringood.athena.repository.PackageIdentifier

enum class PackageItemKind { SYMBOL, ELEMENT, PART, MACRO, VARIANT, PLACEHOLDER }

data class PackageItemIdentity(
    val packageId: PackageIdentifier,
    val itemId: String,
    val itemVersion: String,
) {
    init {
        require(packageId.name.matches(PACKAGE_ID))
        require(!packageId.version.isNullOrBlank())
        require(itemId.matches(ITEM_ID))
        require(itemVersion.matches(VERSION))
    }

    val key: String = "${packageId.name}@${packageId.version}/$itemId@$itemVersion"
}

data class PackageItemProvenance(
    val source: String,
    val sourceDigest: String,
    val license: String,
) {
    init {
        requirePackageRelativePath(source)
        require(sourceDigest.matches(SHA256))
        require(license.isNotBlank())
    }
}

sealed interface PackageItemValue {
    data class ObjectValue(val fields: Map<String, PackageItemValue>) : PackageItemValue
    data class ListValue(val values: List<PackageItemValue>) : PackageItemValue
    data class DeclaredSetValue(val values: Set<String>) : PackageItemValue
    data class TextValue(val value: String) : PackageItemValue
    data class NumberValue(val canonicalText: String) : PackageItemValue
    data class BooleanValue(val value: Boolean) : PackageItemValue
}

data class PackageItemMetadata(
    val identity: PackageItemIdentity,
    val kind: PackageItemKind,
    val provenance: PackageItemProvenance,
    val payloadReference: String,
) {
    init {
        requirePackageRelativePath(payloadReference)
    }
}

enum class PackageItemAdmissionState { PACKAGE_READY, PACKAGE_INCOMPLETE, PACKAGE_INVALID }

data class PackageItemDiagnostic(
    val subject: String,
    val problem: String,
    val correction: String,
    val code: String,
)

data class AdmittedPackageItem(
    val metadata: PackageItemMetadata,
    val normalizedPayload: PackageItemValue.ObjectValue,
    val resolvedReferences: Map<String, PackageItemIdentity>,
    val canonicalDigest: String,
    val admissionState: PackageItemAdmissionState,
    val diagnostics: List<PackageItemDiagnostic>,
) {
    init {
        require(canonicalDigest.matches(SHA256))
        require(resolvedReferences.keys.all(String::isNotBlank))
        require(admissionState != PackageItemAdmissionState.PACKAGE_READY || diagnostics.isEmpty())
    }
}

internal fun requirePackageRelativePath(path: String) {
    val normalized = path.replace('\\', '/')
    require(normalized.isNotBlank())
    require(!normalized.startsWith('/') && !Regex("^[A-Za-z]:").containsMatchIn(normalized))
    require(normalized.split('/').none { it.isBlank() || it == "." || it == ".." })
}

private val PACKAGE_ID = Regex("^[a-z][a-z0-9]*(\\.[a-z][a-z0-9]*)*$")
private val ITEM_ID = Regex("^[a-z][a-z0-9]*(?:[._-][a-z0-9]+)*$")
private val VERSION = Regex("^[A-Za-z0-9][A-Za-z0-9.+_-]*$")
internal val SHA256 = Regex("^[0-9a-f]{64}$")
