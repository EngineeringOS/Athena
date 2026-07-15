package com.engineeringood.athena.compiler.semantic

@JvmInline
value class PackageKey(val value: String) {
    init {
        require(value.isNotBlank()) { "Package key must not be blank" }
    }
}

@JvmInline
value class SourceUnitId(val value: String) {
    init {
        require(value.isNotBlank()) { "Source unit id must not be blank" }
    }
}

@JvmInline
value class DeclarationId(val value: String) {
    init {
        require(value.isNotBlank()) { "Declaration id must not be blank" }
    }
}

@JvmInline
value class NamespaceId(val value: String) {
    init {
        require(value.isNotBlank()) { "Namespace id must not be blank" }
    }
}

@JvmInline
value class BindingId(val value: String) {
    init {
        require(value.isNotBlank()) { "Binding id must not be blank" }
    }
}

@JvmInline
value class GraphId(val value: String) {
    init {
        require(value.matches(Regex("graph:[0-9a-f]{64}"))) { "Graph id must be a canonical SHA-256 value" }
    }
}

data class GraphPackageIdentity(
    val packageKey: PackageKey,
    val sourceRoot: String,
    val directDependencies: List<PackageKey>,
)

data class SourceUnitContentIdentity(
    val sourceUnitId: SourceUnitId,
    val contentHash: String,
) {
    init {
        require(contentHash.matches(Regex("[0-9a-f]{64}"))) { "Source content hash must be lowercase SHA-256" }
    }
}
