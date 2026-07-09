package com.engineeringood.athena.compiler.repository

import com.engineeringood.athena.repository.EngineeringRepository
import com.engineeringood.athena.repository.PackageDependency
import com.engineeringood.athena.repository.PackageDependencySource
import com.engineeringood.athena.repository.PackageIdentifier
import com.engineeringood.athena.repository.PrimaryPackage
import com.engineeringood.athena.repository.RepositoryDiagnostic
import com.engineeringood.athena.repository.RepositoryDiagnosticSeverity
import com.engineeringood.athena.repository.RepositoryManifest
import java.nio.file.Files
import java.nio.file.Path
import java.util.Locale
import java.util.stream.Collectors
import kotlin.io.path.extension
import kotlin.io.path.isRegularFile

/**
 * Loads the governed repository-root contract and validates the first M5 layout rules.
 *
 * This loader is intentionally narrow: it validates canonical root files, the primary package
 * identity block, the governed `src/` source root, and unsupported nested manifests. Dependency
 * resolution and lock-materialization semantics remain later stories.
 */
class AthenaRepositoryContractLoader {
    /**
     * Loads and validates the repository-root contract anchored at [repositoryRoot].
     */
    fun load(
        repositoryRoot: Path,
        options: AthenaRepositoryContractLoadOptions = AthenaRepositoryContractLoadOptions(),
    ): AthenaRepositoryContractValidationResult {
        val normalizedRepositoryRoot = normalizeRoot(repositoryRoot)
        val manifestPath = normalizedRepositoryRoot.resolve(MANIFEST_FILE_NAME)
        val lockPath = normalizedRepositoryRoot.resolve(LOCK_FILE_NAME)
        val diagnostics = mutableListOf<RepositoryDiagnostic>()

        if (!Files.exists(normalizedRepositoryRoot)) {
            diagnostics += diagnostic(
                code = "repository.contract.root.missing",
                message = "Repository root does not exist: $normalizedRepositoryRoot",
            )
            return AthenaRepositoryContractValidationResult(
                repositoryRoot = normalizedRepositoryRoot,
                manifestPath = manifestPath,
                lockPath = lockPath,
                manifestPresent = false,
                lockPresent = false,
                diagnostics = diagnostics,
            )
        }

        if (!Files.isDirectory(normalizedRepositoryRoot)) {
            diagnostics += diagnostic(
                code = "repository.contract.root.not-directory",
                message = "Repository root is not a directory: $normalizedRepositoryRoot",
            )
            return AthenaRepositoryContractValidationResult(
                repositoryRoot = normalizedRepositoryRoot,
                manifestPath = manifestPath,
                lockPath = lockPath,
                manifestPresent = false,
                lockPresent = false,
                diagnostics = diagnostics,
            )
        }

        val manifestPresent = Files.exists(manifestPath)
        val lockPresent = Files.exists(lockPath)

        if (!manifestPresent) {
            diagnostics += diagnostic(
                code = "repository.contract.manifest.missing",
                message = "Repository root must contain `$MANIFEST_FILE_NAME`.",
            )
        } else if (!Files.isRegularFile(manifestPath)) {
            diagnostics += diagnostic(
                code = "repository.contract.manifest.not-file",
                message = "Repository root `$MANIFEST_FILE_NAME` must be a regular file.",
            )
        }

        if (!lockPresent) {
            diagnostics += diagnostic(
                code = "repository.contract.lock.missing",
                message = "Repository root must contain `$LOCK_FILE_NAME`.",
            )
        } else if (!Files.isRegularFile(lockPath)) {
            diagnostics += diagnostic(
                code = "repository.contract.lock.not-file",
                message = "Repository root `$LOCK_FILE_NAME` must be a regular file.",
            )
        }

        val manifest = if (manifestPresent && Files.isRegularFile(manifestPath)) {
            parseManifest(manifestPath, diagnostics)
        } else {
            null
        }

        val excludedGovernedRoots = if (options.allowNestedGovernedSubrepositories) {
            discoverNestedGovernedRoots(normalizedRepositoryRoot)
        } else {
            emptySet()
        }

        diagnostics += findNestedManifestDiagnostics(
            repositoryRoot = normalizedRepositoryRoot,
            excludedGovernedRoots = excludedGovernedRoots,
        )

        if (manifest != null) {
            diagnostics += validateSourceRootLayout(
                repositoryRoot = normalizedRepositoryRoot,
                sourceRoot = manifest.primaryPackage.sourceRoot,
                excludedGovernedRoots = excludedGovernedRoots,
            )
        }

        return AthenaRepositoryContractValidationResult(
            repositoryRoot = normalizedRepositoryRoot,
            manifestPath = manifestPath,
            lockPath = lockPath,
            manifestPresent = manifestPresent && Files.isRegularFile(manifestPath),
            lockPresent = lockPresent && Files.isRegularFile(lockPath),
            repository = manifest?.let { loadedManifest ->
                EngineeringRepository(
                    manifest = loadedManifest,
                    lock = null,
                )
            },
            diagnostics = diagnostics,
        )
    }

    private fun parseManifest(
        manifestPath: Path,
        diagnostics: MutableList<RepositoryDiagnostic>,
    ): RepositoryManifest? {
        val manifestLines = readManifestLines(manifestPath)
        val primaryPackageEntries = readPrimaryPackageEntries(manifestLines)
        if (primaryPackageEntries == null) {
            diagnostics += diagnostic(
                code = "repository.contract.manifest.primary-package.block.missing",
                message = "`$MANIFEST_FILE_NAME` must declare a `primaryPackage:` block.",
            )
            return null
        }

        val packageName = primaryPackageEntries["name"]
        val packageVersion = primaryPackageEntries["version"]
        val sourceRoot = primaryPackageEntries["sourceRoot"]
        val dependencies = parseDependencies(manifestLines, diagnostics)

        if (packageName.isNullOrBlank()) {
            diagnostics += diagnostic(
                code = "repository.contract.manifest.primary-package.name.missing",
                message = "`primaryPackage.name` is required and cannot be blank.",
            )
        } else if (!PACKAGE_NAME_PATTERN.matches(packageName)) {
            diagnostics += diagnostic(
                code = "repository.contract.manifest.primary-package.name.invalid",
                message = "`primaryPackage.name` must use lowercase dot-separated package identity segments.",
            )
        }

        if (packageVersion != null && packageVersion.isBlank()) {
            diagnostics += diagnostic(
                code = "repository.contract.manifest.primary-package.version.blank",
                message = "`primaryPackage.version` cannot be blank when declared.",
            )
        }

        if (sourceRoot.isNullOrBlank()) {
            diagnostics += diagnostic(
                code = "repository.contract.manifest.primary-package.source-root.missing",
                message = "`primaryPackage.sourceRoot` is required and must be `src` in M5.",
            )
        } else if (sourceRoot != GOVERNED_SOURCE_ROOT) {
            diagnostics += diagnostic(
                code = "repository.contract.manifest.primary-package.source-root.unsupported",
                message = "`primaryPackage.sourceRoot` must be `$GOVERNED_SOURCE_ROOT` in M5.",
            )
        }

        if (diagnostics.any { diagnostic ->
                diagnostic.code.startsWith("repository.contract.manifest.primary-package.")
                    || diagnostic.code.startsWith("repository.contract.manifest.dependencies.")
            }
        ) {
            return null
        }

        return RepositoryManifest(
            primaryPackage = PrimaryPackage(
                id = PackageIdentifier(
                    name = packageName!!,
                    version = packageVersion?.takeIf(String::isNotBlank),
                ),
                sourceRoot = sourceRoot!!,
            ),
            dependencies = dependencies,
        )
    }

    private fun readPrimaryPackageEntries(manifestLines: List<ManifestLine>): Map<String, String?>? {
        val primaryPackageEntries = linkedMapOf<String, String?>()
        var insidePrimaryPackage = false

        manifestLines.forEach { line ->
            if (line.indent == 0) {
                insidePrimaryPackage = line.trimmed == "primaryPackage:"
                return@forEach
            }

            if (!insidePrimaryPackage || line.indent < 2) {
                return@forEach
            }

            val separatorIndex = line.trimmed.indexOf(':')
            if (separatorIndex <= 0) {
                return@forEach
            }

            val key = line.trimmed.substring(0, separatorIndex).trim()
            val rawValue = line.trimmed.substring(separatorIndex + 1).trim()
            primaryPackageEntries[key] = rawValue.unquote().ifBlank { "" }
        }

        return primaryPackageEntries.ifEmpty { null }
    }

    private fun parseDependencies(
        manifestLines: List<ManifestLine>,
        diagnostics: MutableList<RepositoryDiagnostic>,
    ): List<PackageDependency> {
        val dependencies = mutableListOf<PackageDependency>()
        var insideDependencies = false
        var currentEntry: LinkedHashMap<String, String?>? = null

        fun flushCurrentEntry() {
            val entry = currentEntry ?: return
            parseDependency(entry, diagnostics)?.let(dependencies::add)
            currentEntry = null
        }

        manifestLines.forEach { line ->
            if (line.indent == 0) {
                if (insideDependencies) {
                    flushCurrentEntry()
                }
                insideDependencies = line.trimmed == "dependencies:"
                return@forEach
            }

            if (!insideDependencies) {
                return@forEach
            }

            if (line.indent == 2) {
                if (!line.trimmed.startsWith("-")) {
                    diagnostics += diagnostic(
                        code = "repository.contract.manifest.dependencies.item.malformed",
                        message = "`dependencies` entries must use list item syntax beginning with `-`.",
                    )
                    return@forEach
                }

                flushCurrentEntry()
                currentEntry = linkedMapOf()
                val inlineEntry = line.trimmed.removePrefix("-").trim()
                if (inlineEntry.isNotEmpty() && !parseManifestEntryLine(currentEntry!!, inlineEntry)) {
                    diagnostics += diagnostic(
                        code = "repository.contract.manifest.dependencies.item.malformed",
                        message = "Dependency entry `$inlineEntry` must use `key: value` syntax.",
                    )
                }
                return@forEach
            }

            if (line.indent >= 4) {
                val entry = currentEntry
                if (entry == null) {
                    diagnostics += diagnostic(
                        code = "repository.contract.manifest.dependencies.item.malformed",
                        message = "`dependencies` detail lines must belong to a preceding list item.",
                    )
                    return@forEach
                }
                if (!parseManifestEntryLine(entry, line.trimmed)) {
                    diagnostics += diagnostic(
                        code = "repository.contract.manifest.dependencies.item.malformed",
                        message = "Dependency detail `${line.trimmed}` must use `key: value` syntax.",
                    )
                }
            }
        }

        if (insideDependencies) {
            flushCurrentEntry()
        }

        return dependencies.sortedWith(compareBy(::stableDependencyKey))
    }

    private fun parseDependency(
        entries: Map<String, String?>,
        diagnostics: MutableList<RepositoryDiagnostic>,
    ): PackageDependency? {
        val dependencyName = entries["name"]
        val dependencyVersion = entries["version"]?.takeIf(String::isNotBlank)
        val rawSource = entries["source"]
        val rawLocator = entries["locator"]
        var hasErrors = false

        if (dependencyName.isNullOrBlank()) {
            diagnostics += diagnostic(
                code = "repository.contract.manifest.dependencies.name.missing",
                message = "Each dependency must declare `name`.",
            )
            hasErrors = true
        } else if (!PACKAGE_NAME_PATTERN.matches(dependencyName)) {
            diagnostics += diagnostic(
                code = "repository.contract.manifest.dependencies.name.invalid",
                message = "Dependency `name` must use lowercase dot-separated package identity segments.",
            )
            hasErrors = true
        }

        val dependencySource = when {
            rawSource.isNullOrBlank() -> {
                diagnostics += diagnostic(
                    code = "repository.contract.manifest.dependencies.source.missing",
                    message = "Dependency `${dependencyName ?: "<unknown>"}` must declare `source`.",
                )
                hasErrors = true
                null
            }

            else -> rawSource.toDependencySourceOrNull().also { source ->
                if (source == null) {
                    diagnostics += diagnostic(
                        code = "repository.contract.manifest.dependencies.source.unsupported",
                        message = "Dependency `${dependencyName ?: "<unknown>"}` declares unsupported `source: $rawSource`. Supported M5 sources are `local-path` and `local-package`.",
                    )
                    hasErrors = true
                }
            }
        }

        val normalizedLocator = normalizeLocator(rawLocator)
        if (dependencySource == PackageDependencySource.LOCAL_PATH && normalizedLocator.isNullOrBlank()) {
            diagnostics += diagnostic(
                code = "repository.contract.manifest.dependencies.locator.missing",
                message = "Dependency `${dependencyName ?: "<unknown>"}` with `source: local-path` must declare a non-blank `locator`.",
            )
            hasErrors = true
        }

        if (hasErrors) {
            return null
        }

        return PackageDependency(
            packageId = PackageIdentifier(
                name = dependencyName!!,
                version = dependencyVersion,
            ),
            source = dependencySource!!,
            locator = if (dependencySource == PackageDependencySource.LOCAL_PATH) normalizedLocator else null,
        )
    }

    private fun findNestedManifestDiagnostics(
        repositoryRoot: Path,
        excludedGovernedRoots: Set<Path>,
    ): List<RepositoryDiagnostic> {
        Files.walk(repositoryRoot).use { candidates ->
            return candidates
                .filter { candidate -> candidate.isRegularFile() }
                .filter { candidate -> candidate.fileName.toString() == MANIFEST_FILE_NAME }
                .filter { candidate -> candidate != repositoryRoot.resolve(MANIFEST_FILE_NAME) }
                .filter { candidate -> !candidate.isWithinAny(excludedGovernedRoots) }
                .sorted(compareBy(::stablePathKey))
                .map { candidate ->
                    diagnostic(
                        code = "repository.contract.manifest.nested.unsupported",
                        message = "Nested `$MANIFEST_FILE_NAME` is not supported in M5: ${repositoryRoot.relativize(candidate).toDisplayPath()}",
                    )
                }
                .collect(Collectors.toList())
        }
    }

    private fun validateSourceRootLayout(
        repositoryRoot: Path,
        sourceRoot: String,
        excludedGovernedRoots: Set<Path>,
    ): List<RepositoryDiagnostic> {
        val diagnostics = mutableListOf<RepositoryDiagnostic>()
        val sourceRootPath = repositoryRoot.resolve(sourceRoot)

        if (!Files.exists(sourceRootPath)) {
            diagnostics += diagnostic(
                code = "repository.contract.layout.source-root.missing",
                message = "Governed source root `$sourceRoot/` does not exist at repository root.",
            )
            return diagnostics
        }

        if (!Files.isDirectory(sourceRootPath)) {
            diagnostics += diagnostic(
                code = "repository.contract.layout.source-root.not-directory",
                message = "Governed source root `$sourceRoot/` must be a directory.",
            )
            return diagnostics
        }

        diagnostics += findAuthoredSourcesOutsideSourceRoot(
            repositoryRoot = repositoryRoot,
            sourceRootPath = sourceRootPath,
            excludedGovernedRoots = excludedGovernedRoots,
        )
        return diagnostics
    }

    private fun findAuthoredSourcesOutsideSourceRoot(
        repositoryRoot: Path,
        sourceRootPath: Path,
        excludedGovernedRoots: Set<Path>,
    ): List<RepositoryDiagnostic> {
        Files.walk(repositoryRoot).use { candidates ->
            return candidates
                .filter { candidate -> candidate.isRegularFile() }
                .filter { candidate -> candidate.extension.equals("athena", ignoreCase = true) }
                .filter { candidate -> !candidate.startsWith(sourceRootPath) }
                .filter { candidate -> !candidate.isWithinAny(excludedGovernedRoots) }
                .sorted(compareBy(::stablePathKey))
                .map { candidate ->
                    diagnostic(
                        code = "repository.contract.layout.authored-source.outside-source-root",
                        message = "Authored `.athena` source must live under `${sourceRootPath.fileName}/`: ${repositoryRoot.relativize(candidate).toDisplayPath()}",
                    )
                }
                .collect(Collectors.toList())
        }
    }

    private fun diagnostic(
        code: String,
        message: String,
    ): RepositoryDiagnostic {
        return RepositoryDiagnostic(
            code = code,
            message = message,
            severity = RepositoryDiagnosticSeverity.ERROR,
        )
    }
}

private data class ManifestLine(
    val indent: Int,
    val trimmed: String,
)

private fun normalizeRoot(path: Path): Path = runCatching { path.toRealPath() }.getOrElse { path.toAbsolutePath().normalize() }

private fun discoverNestedGovernedRoots(repositoryRoot: Path): Set<Path> {
    Files.walk(repositoryRoot).use { candidates ->
        return candidates
            .filter { candidate -> candidate.isRegularFile() }
            .filter { candidate -> candidate.fileName.toString() == MANIFEST_FILE_NAME }
            .filter { candidate -> candidate != repositoryRoot.resolve(MANIFEST_FILE_NAME) }
            .map(Path::getParent)
            .map(::normalizeRoot)
            .collect(Collectors.toSet())
    }
}

private fun readManifestLines(manifestPath: Path): List<ManifestLine> {
    return Files.readAllLines(manifestPath).mapNotNull { rawLine ->
        val line = rawLine.substringBefore('#')
        if (line.isBlank()) {
            return@mapNotNull null
        }
        ManifestLine(
            indent = line.indexOfFirst { character -> !character.isWhitespace() }.coerceAtLeast(0),
            trimmed = line.trim(),
        )
    }
}

private fun parseManifestEntryLine(
    target: MutableMap<String, String?>,
    line: String,
): Boolean {
    val separatorIndex = line.indexOf(':')
    if (separatorIndex <= 0) {
        return false
    }

    val key = line.substring(0, separatorIndex).trim()
    val rawValue = line.substring(separatorIndex + 1).trim()
    target[key] = rawValue.unquote().ifBlank { "" }
    return true
}

private fun stableDependencyKey(dependency: PackageDependency): String {
    return listOf(
        dependency.packageId.name,
        dependency.packageId.version.orEmpty(),
        dependency.source.name,
        dependency.locator.orEmpty(),
    ).joinToString("|")
}

private fun stablePathKey(path: Path): String {
    val normalizedPath = runCatching { path.toRealPath() }.getOrElse { path.toAbsolutePath().normalize() }
    val pathKey = normalizedPath.toString().replace('\\', '/')
    return if (System.getProperty("os.name").startsWith("Windows")) {
        pathKey.lowercase(Locale.ROOT)
    } else {
        pathKey
    }
}

private fun Path.toDisplayPath(): String = toString().replace('\\', '/')

private fun Path.isWithinAny(candidateRoots: Set<Path>): Boolean {
    return candidateRoots.any { candidateRoot ->
        startsWith(candidateRoot)
    }
}

private fun String.unquote(): String = removeSurrounding("\"").removeSurrounding("'")

private fun String.toDependencySourceOrNull(): PackageDependencySource? {
    val normalized = trim().uppercase(Locale.ROOT).replace('-', '_')
    return PackageDependencySource.entries.firstOrNull { source ->
        source.name == normalized
    }
}

private fun normalizeLocator(locator: String?): String? {
    return locator
        ?.trim()
        ?.replace('\\', '/')
        ?.takeIf(String::isNotBlank)
}

private const val MANIFEST_FILE_NAME = "athena.yaml"
private const val LOCK_FILE_NAME = "athena.lock"
private const val GOVERNED_SOURCE_ROOT = "src"
private val PACKAGE_NAME_PATTERN = Regex("^[a-z][a-z0-9-]*(\\.[a-z][a-z0-9-]*)*$")
