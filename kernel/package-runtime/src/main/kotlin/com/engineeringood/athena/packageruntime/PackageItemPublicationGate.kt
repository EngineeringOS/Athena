package com.engineeringood.athena.packageruntime

import com.engineeringood.athena.packageplatform.AdmittedPackageItem
import com.engineeringood.athena.packageplatform.PackageItemAdmissionState
import com.engineeringood.athena.packageplatform.PackageItemDiagnostic
import com.engineeringood.athena.repository.PackageIdentifier
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

data class PackageItemAdmissionReport(
    val packageId: PackageIdentifier,
    val state: PackageItemAdmissionState,
    val results: List<PackageItemAdmissionResult>,
    val diagnostics: List<PackageItemDiagnostic>,
) {
    /** Stable draft guard for report reads; excludes machine-local paths and runtime state. */
    val draftRevision: String
        get() = "draft:sha256:${sha256(canonicalBytes())}"

    fun canonicalBytes(): ByteArray = buildString {
        append("schema=athena-package-admission-report-v1\n")
        append("package=${packageId.name}@${packageId.version.orEmpty()}\n")
        append("state=${state.name}\n")
        results.sortedWith(
            compareBy<PackageItemAdmissionResult> { it.metadata.identity.key }
                .thenBy { it.admissionState.name }
                .thenBy { it.item?.canonicalDigest.orEmpty() }
                .thenBy { it.diagnostics.joinToString("|") { diagnostic -> diagnostic.code } },
        ).forEach { result ->
            val identity = result.metadata.identity
            append("item=${identity.key}|${result.metadata.kind.name}|${result.admissionState.name}|")
            append(result.item?.canonicalDigest.orEmpty())
            append("|${result.metadata.provenance.source}|${result.metadata.provenance.sourceDigest}|")
            append(result.metadata.provenance.license)
            append("|${result.metadata.payloadReference}\n")
            result.diagnostics.sortedWith(compareBy(PackageItemDiagnostic::subject, PackageItemDiagnostic::code)).forEach { diagnostic ->
                append("diagnostic=${diagnostic.subject}|${diagnostic.code}|${diagnostic.problem}|${diagnostic.correction}\n")
            }
        }
        diagnostics.sortedWith(compareBy(PackageItemDiagnostic::subject, PackageItemDiagnostic::code)).forEach { diagnostic ->
            append("packageDiagnostic=${diagnostic.subject}|${diagnostic.code}|${diagnostic.problem}|${diagnostic.correction}\n")
        }
    }.toByteArray(StandardCharsets.UTF_8)
}

typealias AdmissionReport = PackageItemAdmissionReport

data class ReadyPackageItemIndex(
    val packageId: PackageIdentifier,
    val items: List<AdmittedPackageItem>,
) {
    init {
        require(items.isNotEmpty())
        require(items.all { it.admissionState == PackageItemAdmissionState.PACKAGE_READY })
        require(items.all { it.metadata.identity.packageId == packageId })
        require(items.map { it.metadata.identity.key }.distinct().size == items.size)
    }
}

data class PackageItemPublicationResult(
    val index: ReadyPackageItemIndex?,
    val report: PackageItemAdmissionReport,
) {
    val isPublished: Boolean get() = index != null
}

object PackageItemPublicationGate {
    fun publish(
        packageId: PackageIdentifier,
        declaredItemIds: Set<String>,
        results: List<PackageItemAdmissionResult>,
    ): PackageItemPublicationResult {
        val diagnostics = results.flatMap(PackageItemAdmissionResult::diagnostics).toMutableList()
        val resultIds = results.map { it.metadata.identity.itemId }
        val missing = declaredItemIds - resultIds.toSet()
        missing.sorted().forEach { itemId ->
            diagnostics += PackageItemDiagnostic(
                "$packageId/$itemId",
                "Manifest-declared PackageItem `$itemId` was not admitted.",
                "Add and admit `$itemId`, or remove it from package exports.",
                "package.item.declared.missing",
            )
        }
        val duplicateIds = resultIds.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
        duplicateIds.sorted().forEach { itemId ->
            diagnostics += PackageItemDiagnostic(
                "$packageId/$itemId",
                "PackageItem identity `$itemId` is declared more than once.",
                "Keep one authored item for each package item identity.",
                "package.item.identity.duplicate",
            )
        }
        val unexpected = resultIds.toSet() - declaredItemIds
        unexpected.sorted().forEach { itemId ->
            diagnostics += PackageItemDiagnostic(
                "$packageId/$itemId",
                "PackageItem `$itemId` was admitted but is not exported by the package manifest.",
                "Add `$itemId` to package exports or remove the unexported item.",
                "package.item.export.unexpected",
            )
        }
        results.filter { it.metadata.identity.packageId != packageId }.forEach { result ->
            diagnostics += PackageItemDiagnostic(
                result.metadata.identity.key,
                "PackageItem belongs to `${result.metadata.identity.packageId.name}`, not `${packageId.name}`.",
                "Move the item to its owning package or correct its package identity.",
                "package.item.owner.mismatch",
            )
        }

        val aggregateState = when {
            results.any { it.admissionState == PackageItemAdmissionState.PACKAGE_INVALID } ||
                diagnostics.any { diagnostic ->
                    diagnostic.code != "package.item.reference.unresolved" &&
                        diagnostic.code != "package.item.declared.missing"
                } -> PackageItemAdmissionState.PACKAGE_INVALID
            declaredItemIds.isEmpty() || results.isEmpty() ||
                diagnostics.isNotEmpty() ||
                results.any { it.admissionState == PackageItemAdmissionState.PACKAGE_INCOMPLETE } ->
                PackageItemAdmissionState.PACKAGE_INCOMPLETE
            else -> PackageItemAdmissionState.PACKAGE_READY
        }
        val report = PackageItemAdmissionReport(
            packageId,
            aggregateState,
            results.sortedWith(compareBy<PackageItemAdmissionResult> { it.metadata.identity.key }.thenBy { it.admissionState.name }),
            diagnostics.sortedWith(compareBy(PackageItemDiagnostic::subject, PackageItemDiagnostic::code)),
        )
        if (aggregateState != PackageItemAdmissionState.PACKAGE_READY || results.isEmpty()) {
            return PackageItemPublicationResult(null, report)
        }
        val items = results.mapNotNull(PackageItemAdmissionResult::item).sortedBy { it.metadata.identity.key }
        return PackageItemPublicationResult(ReadyPackageItemIndex(packageId, items), report)
    }
}

private fun sha256(bytes: ByteArray): String =
    MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
