package com.engineeringood.athena.compiler.semantic

import com.engineeringood.athena.language.SourceSpan
import com.engineeringood.athena.repository.PackageIdentifier
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Locale

object CanonicalSemanticIdentityBuilder {
    fun packageKey(packageId: PackageIdentifier): PackageKey {
        require(packageId.name.isNotBlank()) { "Package name must not be blank" }
        val version = packageId.version
        require(version == null || version.isNotBlank()) { "Package version must be null or nonblank" }
        return PackageKey(listOf(packageId.name, version.orEmpty()).joinToString("|"))
    }

    fun sourceUnitId(packageKey: PackageKey, sourceRootRelativePath: String): SourceUnitId {
        val normalizedPath = normalizeRelativePath(sourceRootRelativePath, allowRoot = false)
        return SourceUnitId(canonicalValue("source", listOf(packageKey.value, normalizedPath)))
    }

    fun declarationId(
        sourceUnitId: SourceUnitId,
        declarationKind: String,
        qualifiedAuthoredName: List<String>,
    ): DeclarationId {
        val kind = declarationKind.trim().lowercase(Locale.ROOT)
        require(kind.isNotEmpty()) { "Declaration kind must not be blank" }
        return DeclarationId(
            canonicalValue("declaration", listOf(sourceUnitId.value, kind, qualifiedName(qualifiedAuthoredName))),
        )
    }

    fun namespaceId(packageKey: PackageKey, qualifiedNamespaceName: List<String>): NamespaceId {
        return NamespaceId(
            canonicalValue("namespace", listOf(packageKey.value, qualifiedName(qualifiedNamespaceName))),
        )
    }

    fun bindingId(
        sourceUnitId: SourceUnitId,
        referenceSpan: SourceSpan,
        resolvedDeclarationId: DeclarationId,
    ): BindingId {
        require(referenceSpan.start.offset >= 0) { "Binding span start offset must be nonnegative" }
        require(referenceSpan.end.offset >= referenceSpan.start.offset) { "Binding span must be ordered" }
        return BindingId(
            canonicalValue(
                "binding",
                listOf(
                    sourceUnitId.value,
                    referenceSpan.start.offset.toString(),
                    referenceSpan.end.offset.toString(),
                    resolvedDeclarationId.value,
                ),
            ),
        )
    }

    fun sourceContentIdentity(sourceUnitId: SourceUnitId, sourceContent: String): SourceUnitContentIdentity {
        return SourceUnitContentIdentity(sourceUnitId, sha256(sourceContent))
    }

    fun graphId(
        rootPackageKey: PackageKey,
        packages: List<GraphPackageIdentity>,
        sourceContents: List<SourceUnitContentIdentity>,
    ): GraphId {
        require(packages.map { it.packageKey }.distinct().size == packages.size) { "Graph package keys must be unique" }
        require(packages.any { it.packageKey == rootPackageKey }) { "Graph packages must contain the root package key" }
        val packageKeys = packages.mapTo(mutableSetOf()) { it.packageKey }
        require(packages.flatMap { it.directDependencies }.all(packageKeys::contains)) {
            "Graph dependency edges must target resolved package keys"
        }
        require(sourceContents.map { it.sourceUnitId }.distinct().size == sourceContents.size) {
            "Graph source unit content identities must be unique"
        }

        val packageRecords = packages
            .sortedBy { it.packageKey.value }
            .map { packageIdentity ->
                val dependencies = packageIdentity.directDependencies
                    .distinct()
                    .sortedBy { it.value }
                    .map { it.value }
                canonicalSequence(
                    listOf(
                        packageIdentity.packageKey.value,
                        normalizeRelativePath(packageIdentity.sourceRoot, allowRoot = true),
                        canonicalSequence(dependencies),
                    ),
                )
            }
        val sourceRecords = sourceContents
            .sortedBy { it.sourceUnitId.value }
            .map { sourceIdentity -> canonicalSequence(listOf(sourceIdentity.sourceUnitId.value, sourceIdentity.contentHash)) }
        val payload = canonicalSequence(
            listOf(
                rootPackageKey.value,
                canonicalSequence(packageRecords),
                canonicalSequence(sourceRecords),
            ),
        )
        return GraphId("graph:${sha256(payload)}")
    }

    private fun qualifiedName(parts: List<String>): String {
        require(parts.isNotEmpty()) { "Qualified name must contain at least one part" }
        require(parts.all { it.isNotBlank() && '.' !in it }) { "Qualified name parts must be nonblank and undotted" }
        return parts.joinToString(".")
    }

    private fun normalizeRelativePath(path: String, allowRoot: Boolean): String {
        require(path.isNotBlank()) { "Relative path must not be blank" }
        require('\u0000' !in path) { "Relative path must not contain NUL" }
        val portable = path.replace('\\', '/')
        require(!portable.startsWith('/') && !WINDOWS_DRIVE_PATH.matches(portable)) {
            "Path must be source-root-relative: $path"
        }

        val segments = mutableListOf<String>()
        portable.split('/').forEach { segment ->
            when (segment) {
                "", "." -> Unit
                ".." -> {
                    require(segments.isNotEmpty()) { "Relative path escapes its source root: $path" }
                    segments.removeLast()
                }
                else -> segments += segment
            }
        }
        require(allowRoot || segments.isNotEmpty()) { "Source unit path must name a file" }
        return segments.joinToString("/").ifEmpty { "." }
    }

    private fun canonicalValue(prefix: String, components: List<String>): String {
        return "$prefix:${canonicalSequence(components)}"
    }

    private fun canonicalSequence(values: List<String>): String {
        return values.joinToString(separator = "") { value ->
            val byteLength = value.toByteArray(StandardCharsets.UTF_8).size
            "$byteLength:$value"
        }
    }

    private fun sha256(value: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(StandardCharsets.UTF_8))
            .joinToString(separator = "") { byte -> "%02x".format(Locale.ROOT, byte.toInt() and 0xff) }
    }

    private val WINDOWS_DRIVE_PATH = Regex("^[A-Za-z]:.*")
}
