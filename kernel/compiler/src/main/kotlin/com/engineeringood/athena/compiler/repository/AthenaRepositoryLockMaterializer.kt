package com.engineeringood.athena.compiler.repository

import com.engineeringood.athena.repository.PackageIdentifier
import com.engineeringood.athena.repository.RepositoryDiagnostic
import com.engineeringood.athena.repository.RepositoryDiagnosticSeverity
import com.engineeringood.athena.repository.RepositoryLock
import com.engineeringood.athena.repository.ResolvedPackage
import com.engineeringood.athena.repository.ResolvedPackageGraph
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isRegularFile

/**
 * Materializes and validates the canonical `athena.lock` contract from compiler-owned resolver authority.
 */
class AthenaRepositoryLockMaterializer(
    private val graphResolver: AthenaRepositoryGraphResolver = AthenaRepositoryGraphResolver(),
) {
    /**
     * Resolves [repositoryRoot], renders the canonical lock content, and writes it to `athena.lock`.
     */
    fun materialize(repositoryRoot: Path): AthenaRepositoryLockMaterializationResult {
        val graphResult = graphResolver.resolve(repositoryRoot)
        if (!graphResult.isValid || graphResult.repository == null || graphResult.resolutionInput == null || graphResult.graph == null) {
            return AthenaRepositoryLockMaterializationResult(
                repositoryRoot = graphResult.repositoryRoot,
                manifestPath = graphResult.manifestPath,
                lockPath = graphResult.lockPath,
                manifestPresent = graphResult.manifestPresent,
                lockPresent = graphResult.lockPresent,
                repository = graphResult.repository,
                resolutionInput = graphResult.resolutionInput,
                graph = graphResult.graph,
                diagnostics = graphResult.diagnostics,
            )
        }

        val lock = graphResult.graph.toCanonicalRepositoryLock()
        val renderedLock = renderRepositoryLock(lock)
        Files.writeString(graphResult.lockPath, renderedLock)

        return AthenaRepositoryLockMaterializationResult(
            repositoryRoot = graphResult.repositoryRoot,
            manifestPath = graphResult.manifestPath,
            lockPath = graphResult.lockPath,
            manifestPresent = graphResult.manifestPresent,
            lockPresent = true,
            repository = graphResult.repository.copy(lock = lock),
            resolutionInput = graphResult.resolutionInput,
            graph = graphResult.graph,
            lock = lock,
            renderedLock = renderedLock,
            diagnostics = graphResult.diagnostics,
        )
    }

    /**
     * Resolves [repositoryRoot], parses the current `athena.lock`, and validates it against canonical resolver output.
     */
    fun validate(repositoryRoot: Path): AthenaRepositoryLockValidationResult {
        val graphResult = graphResolver.resolve(repositoryRoot)
        if (!graphResult.isValid || graphResult.repository == null || graphResult.resolutionInput == null || graphResult.graph == null) {
            return AthenaRepositoryLockValidationResult(
                repositoryRoot = graphResult.repositoryRoot,
                manifestPath = graphResult.manifestPath,
                lockPath = graphResult.lockPath,
                manifestPresent = graphResult.manifestPresent,
                lockPresent = graphResult.lockPresent,
                repository = graphResult.repository,
                resolutionInput = graphResult.resolutionInput,
                graph = graphResult.graph,
                diagnostics = graphResult.diagnostics,
            )
        }

        val expectedLock = graphResult.graph.toCanonicalRepositoryLock()
        val renderedExpectedLock = renderRepositoryLock(expectedLock)
        if (!graphResult.lockPath.isRegularFile()) {
            return AthenaRepositoryLockValidationResult(
                repositoryRoot = graphResult.repositoryRoot,
                manifestPath = graphResult.manifestPath,
                lockPath = graphResult.lockPath,
                manifestPresent = graphResult.manifestPresent,
                lockPresent = false,
                repository = graphResult.repository.copy(lock = expectedLock),
                resolutionInput = graphResult.resolutionInput,
                graph = graphResult.graph,
                expectedLock = expectedLock,
                renderedExpectedLock = renderedExpectedLock,
                diagnostics = graphResult.diagnostics + diagnostic(
                    code = "repository.lock.file.missing",
                    message = "Canonical `athena.lock` is missing at `${graphResult.lockPath.toDisplayPath()}`.",
                ),
            )
        }

        val actualLockText = Files.readString(graphResult.lockPath)
        val parseResult = parseRepositoryLock(actualLockText)
        val diagnostics = buildList {
            addAll(graphResult.diagnostics)
            addAll(parseResult.diagnostics)
            if (parseResult.lock != null) {
                if (parseResult.lock != expectedLock) {
                    add(
                        diagnostic(
                            code = "repository.lock.content.out-of-date",
                            message = "Canonical `athena.lock` differs from compiler-owned resolver output. Materialize the lock from current manifest authority.",
                        ),
                    )
                } else if (normalizeLockText(actualLockText) != normalizeLockText(renderedExpectedLock)) {
                    add(
                        diagnostic(
                            code = "repository.lock.content.noncanonical",
                            message = "Canonical `athena.lock` matches semantically but is not rendered in stable canonical form.",
                        ),
                    )
                }
            }
        }

        return AthenaRepositoryLockValidationResult(
            repositoryRoot = graphResult.repositoryRoot,
            manifestPath = graphResult.manifestPath,
            lockPath = graphResult.lockPath,
            manifestPresent = graphResult.manifestPresent,
            lockPresent = graphResult.lockPresent,
            repository = graphResult.repository.copy(lock = expectedLock),
            resolutionInput = graphResult.resolutionInput,
            graph = graphResult.graph,
            expectedLock = expectedLock,
            actualLock = parseResult.lock,
            renderedExpectedLock = renderedExpectedLock,
            diagnostics = diagnostics,
        )
    }
}

private data class AthenaParsedRepositoryLockResult(
    val lock: RepositoryLock? = null,
    val diagnostics: List<RepositoryDiagnostic> = emptyList(),
)

private data class LockLine(
    val indent: Int,
    val trimmed: String,
)

private fun ResolvedPackageGraph.toCanonicalRepositoryLock(): RepositoryLock {
    val orderedPackages = packages
        .sortedWith(compareBy<ResolvedPackage> { it.packageId != rootPackage }.thenBy(::stableResolvedPackageKey))
        .map { resolvedPackage ->
            resolvedPackage.copy(
                directDependencies = resolvedPackage.directDependencies.sortedBy(::stablePackageIdentifierKey),
            )
        }

    return RepositoryLock(
        version = REPOSITORY_LOCK_VERSION,
        primaryPackage = rootPackage,
        packages = orderedPackages,
    )
}

private fun renderRepositoryLock(lock: RepositoryLock): String {
    val lines = mutableListOf(
        "# Derived resolution state for the Athena package graph.",
        "# Generated from compiler-owned repository resolution. Manifest intent remains authoritative.",
        "version: ${lock.version}",
        "primaryPackage:",
    )
    lines += renderPackageIdentifierBlock(lock.primaryPackage, indent = 2)
    lines += "packages:"
    lock.packages.forEach { resolvedPackage ->
        lines += renderResolvedPackageBlock(resolvedPackage)
    }
    lines += ""
    return lines.joinToString(separator = "\n")
}

private fun renderPackageIdentifierBlock(
    packageIdentifier: PackageIdentifier,
    indent: Int,
): List<String> {
    val indentation = " ".repeat(indent)
    return buildList {
        add("${indentation}name: ${packageIdentifier.name}")
        packageIdentifier.version?.let { version ->
            add("${indentation}version: $version")
        }
    }
}

private fun renderResolvedPackageBlock(resolvedPackage: ResolvedPackage): List<String> {
    return buildList {
        add("  - name: ${resolvedPackage.packageId.name}")
        resolvedPackage.packageId.version?.let { version ->
            add("    version: $version")
        }
        add("    sourceRoot: ${resolvedPackage.sourceRoot}")
        if (resolvedPackage.directDependencies.isEmpty()) {
            add("    dependencies: []")
        } else {
            add("    dependencies:")
            resolvedPackage.directDependencies.forEach { dependency ->
                add("      - name: ${dependency.name}")
                dependency.version?.let { version ->
                    add("        version: $version")
                }
            }
        }
    }
}

private fun parseRepositoryLock(lockText: String): AthenaParsedRepositoryLockResult {
    val diagnostics = mutableListOf<RepositoryDiagnostic>()
    val lockLines = readLockLines(lockText)
    val version = parseLockVersion(lockLines, diagnostics)
    val primaryPackage = parsePrimaryPackage(lockLines, diagnostics)
    val packages = parseResolvedPackages(lockLines, diagnostics)

    if (version == null || primaryPackage == null || diagnostics.any { diagnostic -> diagnostic.code.startsWith("repository.lock.") && diagnostic.severity == RepositoryDiagnosticSeverity.ERROR }) {
        return AthenaParsedRepositoryLockResult(
            diagnostics = diagnostics,
        )
    }

    return AthenaParsedRepositoryLockResult(
        lock = RepositoryLock(
            version = version,
            primaryPackage = primaryPackage,
            packages = packages,
        ),
        diagnostics = diagnostics,
    )
}

private fun parseLockVersion(
    lockLines: List<LockLine>,
    diagnostics: MutableList<RepositoryDiagnostic>,
): Int? {
    val versionLine = lockLines.firstOrNull { line -> line.indent == 0 && line.trimmed.startsWith("version:") }
    if (versionLine == null) {
        diagnostics += diagnostic(
            code = "repository.lock.version.missing",
            message = "Canonical `athena.lock` must declare a top-level `version:` entry.",
        )
        return null
    }

    val rawValue = versionLine.trimmed.substringAfter(':').trim()
    val version = rawValue.toIntOrNull()
    if (version == null) {
        diagnostics += diagnostic(
            code = "repository.lock.version.invalid",
            message = "Canonical `athena.lock` version must be an integer.",
        )
        return null
    }
    if (version != REPOSITORY_LOCK_VERSION) {
        diagnostics += diagnostic(
            code = "repository.lock.version.unsupported",
            message = "Canonical `athena.lock` version `$version` is unsupported. Expected `$REPOSITORY_LOCK_VERSION` in M5.",
        )
        return null
    }
    return version
}

private fun parsePrimaryPackage(
    lockLines: List<LockLine>,
    diagnostics: MutableList<RepositoryDiagnostic>,
): PackageIdentifier? {
    val entries = readTopLevelBlockEntries(lockLines, "primaryPackage:")
    if (entries == null) {
        diagnostics += diagnostic(
            code = "repository.lock.primary-package.block.missing",
            message = "Canonical `athena.lock` must declare a `primaryPackage:` block.",
        )
        return null
    }
    return parsePackageIdentifier(
        entries = entries,
        diagnostics = diagnostics,
        codePrefix = "repository.lock.primary-package",
        subject = "primaryPackage",
    )
}

private fun parseResolvedPackages(
    lockLines: List<LockLine>,
    diagnostics: MutableList<RepositoryDiagnostic>,
): List<ResolvedPackage> {
    val packagesStartIndex = lockLines.indexOfFirst { line ->
        line.indent == 0 && line.trimmed == "packages:"
    }
    if (packagesStartIndex < 0) {
        diagnostics += diagnostic(
            code = "repository.lock.packages.block.missing",
            message = "Canonical `athena.lock` must declare a `packages:` block.",
        )
        return emptyList()
    }

    val packages = mutableListOf<ResolvedPackage>()
    var index = packagesStartIndex + 1
    while (index < lockLines.size) {
        val line = lockLines[index]
        if (line.indent == 0) {
            break
        }
        if (line.indent != 2 || !line.trimmed.startsWith("-")) {
            diagnostics += diagnostic(
                code = "repository.lock.packages.item.malformed",
                message = "Canonical `packages` entries must use list item syntax beginning with `-`.",
            )
            index++
            continue
        }

        val packageEntries = linkedMapOf<String, String?>()
        val dependencyEntries = mutableListOf<Map<String, String?>>()
        val inlineEntry = line.trimmed.removePrefix("-").trim()
        if (inlineEntry.isNotEmpty() && !parseKeyValueEntry(packageEntries, inlineEntry)) {
            diagnostics += diagnostic(
                code = "repository.lock.packages.item.malformed",
                message = "Package entry `$inlineEntry` must use `key: value` syntax.",
            )
        }

        index++
        while (index < lockLines.size) {
            val detail = lockLines[index]
            if (detail.indent <= 2) {
                break
            }
            if (detail.indent == 4 && detail.trimmed.startsWith("dependencies:")) {
                val dependencySuffix = detail.trimmed.substringAfter(':').trim()
                if (dependencySuffix == "[]") {
                    index++
                    continue
                }
                if (dependencySuffix.isNotEmpty()) {
                    diagnostics += diagnostic(
                        code = "repository.lock.packages.dependencies.item.malformed",
                        message = "Package dependencies must use either `dependencies: []` or an indented list block.",
                    )
                    index++
                    continue
                }
                index++
                while (index < lockLines.size) {
                    val dependencyLine = lockLines[index]
                    if (dependencyLine.indent <= 4) {
                        break
                    }
                    if (dependencyLine.indent != 6 || !dependencyLine.trimmed.startsWith("-")) {
                        diagnostics += diagnostic(
                            code = "repository.lock.packages.dependencies.item.malformed",
                            message = "Package dependency entries must use list item syntax beginning with `-`.",
                        )
                        index++
                        continue
                    }
                    val dependencyEntry = linkedMapOf<String, String?>()
                    val inlineDependency = dependencyLine.trimmed.removePrefix("-").trim()
                    if (inlineDependency.isNotEmpty() && !parseKeyValueEntry(dependencyEntry, inlineDependency)) {
                        diagnostics += diagnostic(
                            code = "repository.lock.packages.dependencies.item.malformed",
                            message = "Package dependency entry `$inlineDependency` must use `key: value` syntax.",
                        )
                    }
                    index++
                    while (index < lockLines.size && lockLines[index].indent > 6) {
                        val dependencyDetail = lockLines[index]
                        if (!parseKeyValueEntry(dependencyEntry, dependencyDetail.trimmed)) {
                            diagnostics += diagnostic(
                                code = "repository.lock.packages.dependencies.item.malformed",
                                message = "Package dependency detail `${dependencyDetail.trimmed}` must use `key: value` syntax.",
                            )
                        }
                        index++
                    }
                    dependencyEntries += dependencyEntry
                }
                continue
            }

            if (!parseKeyValueEntry(packageEntries, detail.trimmed)) {
                diagnostics += diagnostic(
                    code = "repository.lock.packages.item.malformed",
                    message = "Package detail `${detail.trimmed}` must use `key: value` syntax.",
                )
            }
            index++
        }

        val packageIdentifier = parsePackageIdentifier(
            entries = packageEntries,
            diagnostics = diagnostics,
            codePrefix = "repository.lock.packages",
            subject = "package",
        )
        val sourceRoot = packageEntries["sourceRoot"]
        if (sourceRoot.isNullOrBlank()) {
            diagnostics += diagnostic(
                code = "repository.lock.packages.source-root.missing",
                message = "Each canonical lock package entry must declare `sourceRoot`.",
            )
        }
        val directDependencies = dependencyEntries.mapNotNull { dependencyEntry ->
            parsePackageIdentifier(
                entries = dependencyEntry,
                diagnostics = diagnostics,
                codePrefix = "repository.lock.packages.dependencies",
                subject = "dependency",
            )
        }

        if (packageIdentifier != null && !sourceRoot.isNullOrBlank()) {
            packages += ResolvedPackage(
                packageId = packageIdentifier,
                sourceRoot = sourceRoot,
                directDependencies = directDependencies,
            )
        }
    }

    return packages
}

private fun readTopLevelBlockEntries(
    lockLines: List<LockLine>,
    blockHeader: String,
): Map<String, String?>? {
    val entries = linkedMapOf<String, String?>()
    var insideBlock = false

    lockLines.forEach { line ->
        if (line.indent == 0) {
            insideBlock = line.trimmed == blockHeader
            return@forEach
        }

        if (!insideBlock || line.indent < 2) {
            return@forEach
        }

        val separatorIndex = line.trimmed.indexOf(':')
        if (separatorIndex <= 0) {
            return@forEach
        }

        val key = line.trimmed.substring(0, separatorIndex).trim()
        val rawValue = line.trimmed.substring(separatorIndex + 1).trim()
        entries[key] = rawValue.unquote().ifBlank { "" }
    }

    return entries.ifEmpty { null }
}

private fun parsePackageIdentifier(
    entries: Map<String, String?>,
    diagnostics: MutableList<RepositoryDiagnostic>,
    codePrefix: String,
    subject: String,
): PackageIdentifier? {
    val name = entries["name"]
    val version = entries["version"]?.takeIf(String::isNotBlank)
    var hasErrors = false

    if (name.isNullOrBlank()) {
        diagnostics += diagnostic(
            code = "$codePrefix.name.missing",
            message = "Canonical lock $subject entries must declare `name`.",
        )
        hasErrors = true
    } else if (!LOCK_PACKAGE_NAME_PATTERN.matches(name)) {
        diagnostics += diagnostic(
            code = "$codePrefix.name.invalid",
            message = "Canonical lock $subject `name` must use lowercase dot-separated package identity segments.",
        )
        hasErrors = true
    }

    if (entries.containsKey("version") && entries["version"].isNullOrBlank()) {
        diagnostics += diagnostic(
            code = "$codePrefix.version.blank",
            message = "Canonical lock $subject `version` cannot be blank when declared.",
        )
        hasErrors = true
    }

    if (hasErrors) {
        return null
    }

    return PackageIdentifier(
        name = name!!,
        version = version,
    )
}

private fun readLockLines(lockText: String): List<LockLine> {
    return lockText.lineSequence().mapNotNull { rawLine ->
        val line = rawLine.substringBefore('#')
        if (line.isBlank()) {
            return@mapNotNull null
        }
        LockLine(
            indent = line.indexOfFirst { character -> !character.isWhitespace() }.coerceAtLeast(0),
            trimmed = line.trim(),
        )
    }.toList()
}

private fun parseKeyValueEntry(
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

private fun stableResolvedPackageKey(resolvedPackage: ResolvedPackage): String {
    return listOf(
        stablePackageIdentifierKey(resolvedPackage.packageId),
        resolvedPackage.sourceRoot,
    ).joinToString("|")
}

private fun stablePackageIdentifierKey(packageIdentifier: PackageIdentifier): String {
    return listOf(packageIdentifier.name, packageIdentifier.version.orEmpty()).joinToString("|")
}

private fun normalizeLockText(lockText: String): String = lockText.replace("\r\n", "\n").trimEnd()

private fun String.unquote(): String = removeSurrounding("\"").removeSurrounding("'")

private fun Path.toDisplayPath(): String = toString().replace('\\', '/')

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

private const val REPOSITORY_LOCK_VERSION = 1
private val LOCK_PACKAGE_NAME_PATTERN = Regex("^[a-z][a-z0-9-]*(\\.[a-z][a-z0-9-]*)*$")
