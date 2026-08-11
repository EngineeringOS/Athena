package com.engineeringood.athena.compiler.repository

import com.engineeringood.athena.repository.PackageIdentifier
import org.snakeyaml.engine.v2.api.Load
import org.snakeyaml.engine.v2.api.LoadSettings
import java.nio.file.Files
import java.nio.file.Path

internal data class LocalPackageManifest(
    val packageId: PackageIdentifier,
    val exports: Map<String, Set<String>>,
)

internal sealed interface LocalPackageManifestParseResult {
    data class Success(val manifest: LocalPackageManifest) : LocalPackageManifestParseResult {
        val packageId: PackageIdentifier get() = manifest.packageId
    }
    data class Failure(val problem: String) : LocalPackageManifestParseResult
}

internal object LocalPackageManifestParser {
    private val settings = LoadSettings.builder()
        .setLabel("package.yaml")
        .setAllowDuplicateKeys(false)
        .setAllowRecursiveKeys(false)
        .setMaxAliasesForCollections(0)
        .setCodePointLimit(1_000_000)
        .build()

    fun parse(path: Path): LocalPackageManifestParseResult {
        val document = runCatching {
            Files.newBufferedReader(path).use { reader -> Load(settings).loadFromReader(reader) }
        }.getOrElse { failure ->
            return LocalPackageManifestParseResult.Failure(
                failure.message?.lineSequence()?.firstOrNull()?.trim().orEmpty()
                    .ifBlank { "YAML document could not be parsed." },
            )
        }
        val root = document.asStringMap()
            ?: return LocalPackageManifestParseResult.Failure("Document root must be a YAML mapping.")
        val packageId = root["packageId"].asStringMap()
            ?: return LocalPackageManifestParseResult.Failure("`packageId` must be a YAML mapping.")
        val name = packageId.requiredScalar("name")
            ?: return LocalPackageManifestParseResult.Failure("`packageId.name` must be a scalar value.")
        val version = packageId.requiredScalar("version")
            ?: return LocalPackageManifestParseResult.Failure("`packageId.version` must be a scalar value.")
        val exports = root["exports"].asStringMap()
            ?.mapValues { (_, value) -> value.asStringSet() }
            .orEmpty()
        return LocalPackageManifestParseResult.Success(
            LocalPackageManifest(
                packageId = PackageIdentifier(name, version),
                exports = exports,
            ),
        )
    }
}

private fun Any?.asStringMap(): Map<String, Any?>? {
    val map = this as? Map<*, *> ?: return null
    if (map.keys.any { it !is String }) return null
    return map.entries.associate { (key, value) -> key as String to value }
}

private fun Map<String, Any?>.requiredScalar(key: String): String? {
    val value = get(key) ?: return null
    if (value is Map<*, *> || value is Collection<*>) return null
    return value.toString().trim().takeIf(String::isNotBlank)
}

private fun Any?.asStringSet(): Set<String> = when (this) {
    null -> emptySet()
    is Collection<*> -> mapNotNull { value -> value?.toString()?.trim()?.takeIf(String::isNotBlank) }.toSet()
    else -> emptySet()
}
