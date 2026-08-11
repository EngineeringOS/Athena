package com.engineeringood.athena.packageplatform

import java.nio.charset.StandardCharsets

/** Stable, package-owned provenance read projection. */
data class PackageProvenanceView(
    val item: PackageItemIdentity,
    val provenance: PackageItemProvenance,
) {
    val source: String get() = provenance.source

    init {
        require(provenance.source == provenance.source.replace('\\', '/'))
        require(!provenance.source.startsWith('/') && !Regex("^[A-Za-z]:").containsMatchIn(provenance.source))
        require(!provenance.source.startsWith("//"))
    }

    fun canonicalText(): String = buildString {
        append("item=").append(item.key).append('\n')
        append("source=").append(provenance.source).append('\n')
        append("sourceDigest=").append(provenance.sourceDigest).append('\n')
        append("license=").append(provenance.license).append('\n')
    }
}

/** Compiler-owned derived link from one source binding to visible scene occurrences. */
data class FunctionRepresentationUsageTrace(
    val bindingId: String,
    val functionId: String,
    val projectionId: String,
    val bindingRole: String,
    val item: PackageItemIdentity,
    val itemDigest: String,
    val provenance: PackageProvenanceView,
    val occurrenceIds: List<String>,
) {
    init {
        require(bindingId.isNotBlank() && functionId.isNotBlank())
        require(projectionId.isNotBlank() && bindingRole.isNotBlank())
        require(itemDigest.matches(SHA256))
        require(provenance.item == item)
        require(occurrenceIds.isNotEmpty() && occurrenceIds.all(String::isNotBlank))
        require(occurrenceIds.distinct().size == occurrenceIds.size)
    }

    fun canonicalText(): String = buildString {
        append("binding=").append(bindingId).append('\n')
        append("function=").append(functionId).append('\n')
        append("projection=").append(projectionId).append('\n')
        append("role=").append(bindingRole).append('\n')
        append("item=").append(item.key).append('\n')
        append("itemDigest=").append(itemDigest).append('\n')
        append(provenance.canonicalText())
        occurrenceIds.sorted().forEach { append("occurrence=").append(it).append('\n') }
    }
}

/** Deterministic read-only join. It never mutates source, bindings, or scene. */
data class PackageUsageTraceView(
    val traces: List<FunctionRepresentationUsageTrace>,
) {
    init {
        require(traces.map { it.bindingId }.distinct().size == traces.size)
    }

    fun canonicalBytes(): ByteArray = buildString {
        append("schema=athena-package-usage-trace-v1\n")
        traces.sortedWith(compareBy({ it.bindingId }, { it.item.key })).forEach { append(it.canonicalText()) }
    }.toByteArray(StandardCharsets.UTF_8)

    fun digest(): String = java.security.MessageDigest.getInstance("SHA-256")
        .digest(canonicalBytes()).joinToString("") { "%02x".format(it) }
}
