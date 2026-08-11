package com.engineeringood.athena.packageruntime

import com.engineeringood.athena.packageplatform.*

data class PackageBrowserPort(
    val key: String,
    val direction: String,
    val domain: String,
    val flowKind: String,
)

data class PackageBrowserItem(
    val identity: PackageItemIdentity,
    val kind: PackageItemKind,
    val digest: String,
    val center: String?,
    val ports: List<PackageBrowserPort>,
    val compatibleParts: List<PackageItemIdentity>,
    val variants: List<PackageItemIdentity>,
    val provenance: PackageProvenanceView,
    val license: String,
    val resource: String,
) {
    init {
        require(identity == provenance.item)
        require(digest.matches(Regex("^[0-9a-f]{64}$")))
        require(ports.map(PackageBrowserPort::key).distinct().size == ports.size)
    }
}

data class PackageBrowserSnapshot(
    val items: List<PackageBrowserItem>,
    val admissionReports: List<PackageItemAdmissionReport>,
) {
    init { require(items.map { it.identity.key }.distinct().size == items.size) }
}

object PackageBrowserReadModel {
    fun build(
        index: ReadyPackageItemIndex,
        report: PackageItemAdmissionReport,
        details: Map<String, PackageBrowserItem>,
    ): PackageBrowserSnapshot {
        val items = index.items.sortedBy { it.metadata.identity.key }.map { admitted ->
            val detail = details[admitted.metadata.identity.key]
                ?: throw IllegalArgumentException("READY PackageItem `${admitted.metadata.identity.key}` has no inspection metadata.")
            require(detail.identity == admitted.metadata.identity)
            require(detail.digest == admitted.canonicalDigest)
            require(detail.provenance == PackageProvenanceView(admitted.metadata.identity, admitted.metadata.provenance))
            detail
        }
        return PackageBrowserSnapshot(items, listOf(report))
    }
}

