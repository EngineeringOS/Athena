package com.engineeringood.athena.compiler.repository

import com.engineeringood.athena.packageplatform.PackageAdmissionLimits
import com.engineeringood.athena.repository.PackageIdentifier
import com.engineeringood.athena.repository.RepositoryDiagnostic
import com.engineeringood.athena.repository.RepositoryDiagnosticSeverity
import com.engineeringood.athena.repository.RepositoryLockedPackage
import com.engineeringood.athena.repository.RepositoryLockedItem
import com.engineeringood.athena.repository.RepositoryLock
import com.engineeringood.athena.repository.RepositoryResourceHash
import com.engineeringood.athena.repository.RepositorySourceHash
import com.engineeringood.athena.repository.ResolvedPackage
import com.engineeringood.athena.repository.ResolvedPackageGraph
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import kotlin.io.path.deleteIfExists
import kotlin.io.path.isRegularFile

/**
 * Materializes and validates the canonical `athena.lock` contract from compiler-owned resolver authority.
 */
class AthenaRepositoryLockMaterializer(
    private val graphResolver: AthenaRepositoryGraphResolver = AthenaRepositoryGraphResolver(),
    private val admissionLimits: PackageAdmissionLimits = PackageAdmissionLimits.STANDARD,
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

        val lockBuild = graphResult.graph.toCanonicalRepositoryLock(graphResult.repositoryRoot, admissionLimits)
        val lock = lockBuild.lock
        val renderedLock = renderRepositoryLock(lock)
        if (lockBuild.diagnostics.isNotEmpty()) {
            return AthenaRepositoryLockMaterializationResult(
                repositoryRoot = graphResult.repositoryRoot,
                manifestPath = graphResult.manifestPath,
                lockPath = graphResult.lockPath,
                manifestPresent = graphResult.manifestPresent,
                lockPresent = graphResult.lockPresent,
                repository = graphResult.repository,
                resolutionInput = graphResult.resolutionInput,
                graph = graphResult.graph,
                lock = lock,
                renderedLock = renderedLock,
                diagnostics = graphResult.diagnostics + lockBuild.diagnostics,
            )
        }
        val writeDiagnostics = writeLockAtomically(graphResult.lockPath, renderedLock)
        if (writeDiagnostics.isNotEmpty()) {
            return AthenaRepositoryLockMaterializationResult(
                repositoryRoot = graphResult.repositoryRoot,
                manifestPath = graphResult.manifestPath,
                lockPath = graphResult.lockPath,
                manifestPresent = graphResult.manifestPresent,
                lockPresent = graphResult.lockPresent,
                repository = graphResult.repository,
                resolutionInput = graphResult.resolutionInput,
                graph = graphResult.graph,
                lock = lock,
                renderedLock = renderedLock,
                diagnostics = graphResult.diagnostics + lockBuild.diagnostics + writeDiagnostics,
            )
        }
        val validation = validate(repositoryRoot)

        return AthenaRepositoryLockMaterializationResult(
            repositoryRoot = graphResult.repositoryRoot,
            manifestPath = graphResult.manifestPath,
            lockPath = graphResult.lockPath,
            manifestPresent = graphResult.manifestPresent,
            lockPresent = true,
            repository = graphResult.repository.copy(lock = lock),
            resolutionInput = graphResult.resolutionInput,
            graph = graphResult.graph,
            lock = validation.expectedLock ?: lock,
            renderedLock = validation.renderedExpectedLock ?: renderedLock,
            diagnostics = graphResult.diagnostics + validation.diagnostics,
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

        val lockBuild = graphResult.graph.toCanonicalRepositoryLock(graphResult.repositoryRoot, admissionLimits)
        val expectedLock = lockBuild.lock
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
                diagnostics = graphResult.diagnostics + lockBuild.diagnostics + diagnostic(
                    code = "repository.lock.missing",
                    message = "Canonical `athena.lock` is missing at `${graphResult.lockPath.toDisplayPath()}`.",
                ),
            )
        }

        val actualLockText = Files.readString(graphResult.lockPath)
        val parseResult = parseRepositoryLock(actualLockText)
        val diagnostics = buildList {
            addAll(graphResult.diagnostics)
            addAll(lockBuild.diagnostics)
            addAll(parseResult.diagnostics)
            if (parseResult.schemaIncompatible) {
                add(
                    diagnostic(
                        code = "repository.lock.schema-incompatible",
                        message = "Canonical `athena.lock` uses an unsupported schema. Materialize athena-lock-v3 from current compiler authority.",
                    ),
                )
            } else if (parseResult.lock != null) {
                if (parseResult.lock != expectedLock) {
                    add(
                        diagnostic(
                            code = "repository.lock.stale",
                            message = "Canonical `athena.lock` differs from compiler-owned resolver output. Materialize the lock from current manifest authority.",
                        ),
                    )
                } else if (normalizeLockText(actualLockText) != normalizeLockText(renderedExpectedLock)) {
                    add(
                        diagnostic(
                            code = "repository.lock.noncanonical",
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
    val schemaIncompatible: Boolean = false,
    val diagnostics: List<RepositoryDiagnostic> = emptyList(),
)

private data class CanonicalRepositoryLockBuild(
    val lock: RepositoryLock,
    val diagnostics: List<RepositoryDiagnostic>,
)

private data class AdmittedSourceHashResult(
    val sourceHashes: List<RepositorySourceHash>,
    val resourceHashes: List<RepositoryResourceHash>,
    val admittedBytes: Long,
    val diagnostics: List<RepositoryDiagnostic>,
)

private data class LockLine(
    val indent: Int,
    val trimmed: String,
)

private fun ResolvedPackageGraph.toCanonicalRepositoryLock(
    repositoryRoot: Path,
    limits: PackageAdmissionLimits,
): CanonicalRepositoryLockBuild {
    val orderedPackages = packages
        .sortedWith(compareBy<ResolvedPackage> { it.packageId != rootPackage }.thenBy(::stableResolvedPackageKey))
        .map { resolvedPackage ->
            resolvedPackage.copy(
                directDependencies = resolvedPackage.directDependencies.sortedBy(::stablePackageIdentifierKey),
            )
        }
    val diagnostics = mutableListOf<RepositoryDiagnostic>()
    if (orderedPackages.size > limits.maxResolvedPackages) {
        diagnostics += diagnostic(
            code = "repository.admission.budget.resolved-packages-exceeded",
            message = "Resolved package graph exceeds PackageAdmissionLimits package budget.",
        )
    }
    val lockedPackageResults = orderedPackages.map { resolvedPackage ->
        resolvedPackage.toLockedPackage(repositoryRoot, limits)
    }
    val lockedPackages = lockedPackageResults.map { result -> result.first }
    lockedPackageResults.forEach { result -> diagnostics += result.second.diagnostics }
    diagnostics += sourceRootTopologyDiagnostics(repositoryRoot, orderedPackages)
    val admittedRepositoryBytes = lockedPackageResults.sumOf { result -> result.second.admittedBytes }
    if (admittedRepositoryBytes > limits.maxAdmittedBytesPerRepository) {
        diagnostics += diagnostic(
            code = "repository.admission.budget.repository-bytes-exceeded",
            message = "Admitted repository content exceeds PackageAdmissionLimits repository byte budget.",
        )
    }

    return CanonicalRepositoryLockBuild(
        lock = RepositoryLock(
            version = REPOSITORY_LOCK_VERSION,
            schema = REPOSITORY_LOCK_SCHEMA,
            compilerSchema = REPOSITORY_LOCK_COMPILER_SCHEMA,
            validatedLockStateDigest = validatedLockStateDigest(lockedPackages),
            primaryPackage = rootPackage,
            packages = orderedPackages,
            packageSnapshots = lockedPackages,
        ),
        diagnostics = diagnostics,
    )
}

private fun ResolvedPackage.toLockedPackage(
    repositoryRoot: Path,
    limits: PackageAdmissionLimits,
): Pair<RepositoryLockedPackage, AdmittedSourceHashResult> {
    val sourceAdmission = admittedSourceHashes(repositoryRoot, sourceRoot, limits)
    val packageRoot = resolveSourceRoot(repositoryRoot, sourceRoot)
    val packageManifest = packageRoot.resolve("package.yaml")
    val nativeIndex = if (!Files.isRegularFile(packageManifest, LinkOption.NOFOLLOW_LINKS)) {
        NativePackageItemIndexBuild(emptyList(), emptyList())
    } else when (val manifestResult = LocalPackageManifestParser.parse(packageManifest)) {
        is LocalPackageManifestParseResult.Success -> NativePackageItemIndexCompiler().compile(
            packageId = packageId,
            packageRoot = packageRoot,
            manifest = manifestResult.manifest,
            admittedResources = sourceAdmission.resourceHashes,
        )
        is LocalPackageManifestParseResult.Failure -> NativePackageItemIndexBuild(
            items = emptyList(),
            diagnostics = listOf(
                diagnostic(
                    code = "package.index.manifest.invalid",
                    message = "Package manifest at `${packageRoot.resolve("package.yaml").toDisplayPath()}` cannot build a native Package Index: ${manifestResult.problem}",
                ),
            ),
        )
    }
    return RepositoryLockedPackage(
        packageId = packageId,
        sourceRoot = sourceRoot,
        manifestDigest = manifestDigest(repositoryRoot, sourceRoot),
        itemDigests = nativeIndex.items,
        sourceHashes = sourceAdmission.sourceHashes,
        resourceHashes = sourceAdmission.resourceHashes,
        directDependencies = directDependencies,
    ) to sourceAdmission.copy(diagnostics = sourceAdmission.diagnostics + nativeIndex.diagnostics)
}

private fun renderRepositoryLock(lock: RepositoryLock): String {
    val lines = mutableListOf(
        "# Derived resolution state for the Athena package graph.",
        "# Generated from compiler-owned repository resolution. Manifest intent remains authoritative.",
        "version: ${lock.version}",
        "schema: ${lock.schema}",
        "compilerSchema: ${lock.compilerSchema}",
        "validatedLockStateDigest: ${lock.validatedLockStateDigest}",
        "primaryPackage:",
    )
    lines += renderPackageIdentifierBlock(lock.primaryPackage, indent = 2)
    lines += "packages:"
    lock.packageSnapshots.forEach { resolvedPackage ->
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

private fun renderResolvedPackageBlock(resolvedPackage: RepositoryLockedPackage): List<String> {
    return buildList {
        add("  - name: ${resolvedPackage.packageId.name}")
        resolvedPackage.packageId.version?.let { version ->
            add("    version: $version")
        }
        add("    sourceRoot: ${resolvedPackage.sourceRoot}")
        add("    manifestDigest: ${resolvedPackage.manifestDigest}")
        if (resolvedPackage.itemDigests.isEmpty()) {
            add("    items: []")
        } else {
            add("    items:")
            resolvedPackage.itemDigests.sortedBy { "${it.kind}:${it.itemId}@${it.itemVersion}" }.forEach { item ->
                add("      - itemId: ${item.itemId}")
                add("        itemVersion: ${item.itemVersion}")
                add("        kind: ${item.kind}")
                add("        digest: ${item.digest}")
                add("        sourcePath: ${item.sourcePath}")
                add("        resourceRefs: [${item.resourceReferences.sorted().joinToString(", ")}]")
                add("        attributes: [${item.attributes.toSortedMap().entries.joinToString(", ") { (key, value) -> "$key=$value" }}]")
            }
        }
        add("    assetProfile: ${resolvedPackage.assetProfile}")
        if (resolvedPackage.sourceHashes.isEmpty()) {
            add("    sourceHashes: []")
        } else {
            add("    sourceHashes:")
            resolvedPackage.sourceHashes.forEach { source ->
                add("      - path: ${source.path}")
                add("        hash: ${source.hash}")
            }
        }
        if (resolvedPackage.resourceHashes.isEmpty()) {
            add("    resourceHashes: []")
        } else {
            add("    resourceHashes:")
            resolvedPackage.resourceHashes.forEach { resource ->
                add("      - key: ${resource.key}")
                add("        path: ${resource.path}")
                add("        hash: ${resource.hash}")
            }
        }
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
    val schema = parseTopLevelValue(lockLines, "schema")
    val compilerSchema = parseTopLevelValue(lockLines, "compilerSchema")
    val validatedLockStateDigest = parseTopLevelValue(lockLines, "validatedLockStateDigest")
    val primaryPackage = parsePrimaryPackage(lockLines, diagnostics)
    val packages = parseResolvedPackages(lockLines, diagnostics)

    val schemaIncompatible = version != REPOSITORY_LOCK_VERSION ||
        schema != REPOSITORY_LOCK_SCHEMA ||
        compilerSchema != REPOSITORY_LOCK_COMPILER_SCHEMA
    if (schemaIncompatible) {
        return AthenaParsedRepositoryLockResult(schemaIncompatible = true)
    }
    if (validatedLockStateDigest.isNullOrBlank()) {
        diagnostics += diagnostic(
            code = "repository.lock.validated-state-digest.missing",
            message = "athena-lock-v3 must declare `validatedLockStateDigest`.",
        )
    }

    if (primaryPackage == null || diagnostics.any { diagnostic -> diagnostic.code.startsWith("repository.lock.") && diagnostic.severity == RepositoryDiagnosticSeverity.ERROR }) {
        return AthenaParsedRepositoryLockResult(
            diagnostics = diagnostics,
        )
    }

    return AthenaParsedRepositoryLockResult(
        lock = RepositoryLock(
            version = version,
            schema = schema,
            compilerSchema = compilerSchema,
            validatedLockStateDigest = validatedLockStateDigest!!,
            primaryPackage = primaryPackage,
            packages = packages.map { locked ->
                ResolvedPackage(
                    packageId = locked.packageId,
                    sourceRoot = locked.sourceRoot,
                    directDependencies = locked.directDependencies,
                )
            },
            packageSnapshots = packages,
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
            message = "Canonical `athena.lock` version `$version` is unsupported. Expected `$REPOSITORY_LOCK_VERSION`.",
        )
        return null
    }
    return version
}

private fun parseTopLevelValue(
    lockLines: List<LockLine>,
    key: String,
): String? = lockLines
    .firstOrNull { line -> line.indent == 0 && line.trimmed.startsWith("$key:") }
    ?.trimmed
    ?.substringAfter(':')
    ?.trim()
    ?.unquote()

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
): List<RepositoryLockedPackage> {
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

    val packages = mutableListOf<RepositoryLockedPackage>()
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
        val itemEntries = mutableListOf<Map<String, String?>>()
        val sourceHashEntries = mutableListOf<Map<String, String?>>()
        val resourceHashEntries = mutableListOf<Map<String, String?>>()
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

            if (detail.indent == 4 && detail.trimmed.startsWith("sourceHashes:")) {
                index = parseHashEntries(
                    lockLines = lockLines,
                    startIndex = index,
                    diagnostics = diagnostics,
                    target = sourceHashEntries,
                    codePrefix = "repository.lock.packages.source-hashes",
                    allowedKeys = setOf("path", "hash"),
                )
                continue
            }

            if (detail.indent == 4 && detail.trimmed.startsWith("resourceHashes:")) {
                index = parseHashEntries(
                    lockLines = lockLines,
                    startIndex = index,
                    diagnostics = diagnostics,
                    target = resourceHashEntries,
                    codePrefix = "repository.lock.packages.resource-hashes",
                    allowedKeys = setOf("key", "path", "hash"),
                )
                continue
            }

            if (detail.indent == 4 && detail.trimmed.startsWith("items:")) {
                index = parseHashEntries(
                    lockLines = lockLines,
                    startIndex = index,
                    diagnostics = diagnostics,
                    target = itemEntries,
                    codePrefix = "repository.lock.packages.items",
                    allowedKeys = setOf("itemId", "itemVersion", "kind", "digest", "sourcePath", "resourceRefs", "attributes"),
                )
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
    val manifestDigest = packageEntries["manifestDigest"]
        if (sourceRoot.isNullOrBlank()) {
            diagnostics += diagnostic(
                code = "repository.lock.packages.source-root.missing",
                message = "Each canonical lock package entry must declare `sourceRoot`.",
            )
        }
        if (manifestDigest.isNullOrBlank()) {
            diagnostics += diagnostic(
                code = "repository.lock.packages.manifest-digest.missing",
                message = "Each canonical lock package entry must declare `manifestDigest`.",
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
        val sourceHashes = sourceHashEntries.mapNotNull { entry ->
            val path = entry["path"]
            val hash = entry["hash"]
            if (path.isNullOrBlank() || hash.isNullOrBlank()) {
                diagnostics += diagnostic(
                    code = "repository.lock.packages.source-hashes.item.malformed",
                    message = "Source hash entries must declare `path` and `hash`.",
                )
                null
            } else {
                RepositorySourceHash(path = path, hash = hash)
            }
        }

        val itemDigests = itemEntries.mapNotNull { entry ->
            val itemId = entry["itemId"]
            val itemVersion = entry["itemVersion"]
            val kind = entry["kind"]
            val digest = entry["digest"]
            val sourcePath = entry["sourcePath"]
            if (itemId.isNullOrBlank() || itemVersion.isNullOrBlank() || kind.isNullOrBlank() || digest.isNullOrBlank() || sourcePath.isNullOrBlank()) {
                diagnostics += diagnostic(
                    code = "repository.lock.packages.items.item.malformed",
                    message = "Package item entries must declare itemId, itemVersion, kind, digest, and sourcePath.",
                )
                null
            } else {
                RepositoryLockedItem(
                    itemId = itemId,
                    itemVersion = itemVersion,
                    kind = kind,
                    digest = digest,
                    sourcePath = sourcePath,
                    resourceReferences = parseInlineReferences(entry["resourceRefs"]),
                    attributes = parseInlineAttributes(entry["attributes"]),
                )
            }
        }

        if (packageIdentifier != null && !sourceRoot.isNullOrBlank() && !manifestDigest.isNullOrBlank()) {
            val resourceHashes = resourceHashEntries.mapNotNull { entry ->
                val key = entry["key"]
                val path = entry["path"]
                val hash = entry["hash"]
                if (key.isNullOrBlank() || path.isNullOrBlank() || hash.isNullOrBlank()) {
                    diagnostics += diagnostic(
                        code = "repository.lock.packages.resource-hashes.item.malformed",
                        message = "Resource hash entries must declare `key`, `path`, and `hash`.",
                    )
                    null
                } else {
                    RepositoryResourceHash(key = key, path = path, hash = hash)
                }
            }
            packages += RepositoryLockedPackage(
                packageId = packageIdentifier,
                sourceRoot = sourceRoot,
                manifestDigest = manifestDigest,
                itemDigests = itemDigests,
                sourceHashes = sourceHashes,
                    resourceHashes = resourceHashes,
                    directDependencies = directDependencies,
                    assetProfile = packageEntries["assetProfile"] ?: "svg-safe-1",
            )
        }
    }

    return packages
}

private fun parseHashEntries(
    lockLines: List<LockLine>,
    startIndex: Int,
    diagnostics: MutableList<RepositoryDiagnostic>,
    target: MutableList<Map<String, String?>>,
    codePrefix: String,
    allowedKeys: Set<String>,
): Int {
    val header = lockLines[startIndex]
    val suffix = header.trimmed.substringAfter(':').trim()
    if (suffix == "[]") {
        return startIndex + 1
    }
    if (suffix.isNotEmpty()) {
        diagnostics += diagnostic(
            code = "$codePrefix.item.malformed",
            message = "Hash entries must use either an empty list or an indented list block.",
        )
        return startIndex + 1
    }
    var index = startIndex + 1
    while (index < lockLines.size) {
        val line = lockLines[index]
        if (line.indent <= 4) break
        if (line.indent != 6 || !line.trimmed.startsWith("-")) {
            diagnostics += diagnostic(
                code = "$codePrefix.item.malformed",
                message = "Hash entries must use list item syntax beginning with `-`.",
            )
            index++
            continue
        }
        val entry = linkedMapOf<String, String?>()
        val inline = line.trimmed.removePrefix("-").trim()
        if (inline.isNotEmpty() && !parseKeyValueEntry(entry, inline)) {
            diagnostics += diagnostic(
                code = "$codePrefix.item.malformed",
                message = "Hash entry `$inline` must use `key: value` syntax.",
            )
        }
        index++
        while (index < lockLines.size && lockLines[index].indent > 6) {
            val detail = lockLines[index]
            if (!parseKeyValueEntry(entry, detail.trimmed)) {
                diagnostics += diagnostic(
                    code = "$codePrefix.item.malformed",
                    message = "Hash entry detail `${detail.trimmed}` must use `key: value` syntax.",
                )
            }
            index++
        }
        val unknownKeys = entry.keys - allowedKeys
        if (unknownKeys.isNotEmpty()) {
            diagnostics += diagnostic(
                code = "$codePrefix.item.malformed",
                message = "Hash entry contains unsupported keys `${unknownKeys.sorted().joinToString(",")}`.",
            )
        }
        target += entry
    }
    return index
}

private fun parseInlineReferences(value: String?): List<String> {
    val text = value?.trim().orEmpty()
    if (text == "[]" || text.isBlank()) return emptyList()
    if (!text.startsWith('[') || !text.endsWith(']')) return emptyList()
    return text.removePrefix("[").removeSuffix("]")
        .split(',')
        .map(String::trim)
        .filter(String::isNotBlank)
        .sorted()
}

private fun parseInlineAttributes(value: String?): Map<String, String> = parseInlineReferences(value)
    .mapNotNull { entry ->
        val separator = entry.indexOf('=')
        if (separator <= 0 || separator == entry.lastIndex) null
        else entry.substring(0, separator).trim() to entry.substring(separator + 1).trim()
    }
    .toMap()

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

private fun admittedSourceHashes(
    repositoryRoot: Path,
    sourceRoot: String,
    limits: PackageAdmissionLimits,
): AdmittedSourceHashResult {
    val root = resolveSourceRoot(repositoryRoot, sourceRoot)
    val rootDiagnostics = validateAdmittedSourceRoot(repositoryRoot, sourceRoot, root)
    if (rootDiagnostics.isNotEmpty()) {
        return AdmittedSourceHashResult(
            sourceHashes = emptyList(),
            resourceHashes = emptyList(),
            admittedBytes = 0L,
            diagnostics = rootDiagnostics,
        )
    }
    if (!Files.isDirectory(root, LinkOption.NOFOLLOW_LINKS)) {
        return AdmittedSourceHashResult(
            sourceHashes = emptyList(),
            resourceHashes = emptyList(),
            admittedBytes = 0L,
            diagnostics = listOf(
                diagnostic(
                    code = "repository.admission.source-root.missing",
                    message = "Package source root `${root.toDisplayPath()}` is not a no-follow directory.",
                ),
            ),
        )
    }

    val diagnostics = mutableListOf<RepositoryDiagnostic>()
    val sourceHashes = mutableListOf<RepositorySourceHash>()
    val resourceHashes = mutableListOf<RepositoryResourceHash>()
    var admittedBytes = 0L
    Files.walk(root).use { stream ->
        stream
            .peek { path ->
                if (Files.isSymbolicLink(path)) {
                    diagnostics += diagnostic(
                        code = "repository.admission.source.link-forbidden",
                        message = "Package source admission rejects link or reparse candidate `${path.toDisplayPath()}`.",
                    )
                }
            }
            .filter { path -> Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) }
            .filter { path ->
                val name = path.fileName.toString()
                name.endsWith(".athena") &&
                    !name.endsWith(".sheet.athena") &&
                    !name.endsWith(".sheet.style.athena") &&
                    !name.endsWith(".binding.athena") &&
                    !isPackageResource(path, root)
            }
            .sorted(Comparator.comparing { path -> root.relativize(path).toDisplayPath() })
            .forEach { path ->
                val bytes = Files.readAllBytes(path)
                admittedBytes += bytes.size.toLong()
                val relativePath = root.relativize(path).toDisplayPath()
                sourceHashes += RepositorySourceHash(
                    path = relativePath,
                    hash = "sha256:${sha256(bytes)}",
                )
            }
    }
    Files.walk(root).use { stream ->
        stream
            .peek { path ->
                if (Files.isSymbolicLink(path)) {
                    diagnostics += diagnostic(
                        code = "repository.admission.resource.link-forbidden",
                        message = "Package resource admission rejects link or reparse candidate `${path.toDisplayPath()}`.",
                    )
                }
            }
            .filter { path -> Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) }
            .filter { path -> isPackageResource(path, root) }
            .filter { path -> path.fileName.toString().substringAfterLast('.', "").lowercase() in PRESENTATION_RESOURCE_EXTENSIONS }
            .sorted(Comparator.comparing { path -> root.relativize(path).toDisplayPath() })
            .forEach { path ->
                val bytes = Files.readAllBytes(path)
                admittedBytes += bytes.size.toLong()
                val relativePath = root.relativize(path).toDisplayPath()
                resourceHashes += RepositoryResourceHash(
                    key = relativePath,
                    path = relativePath,
                    hash = "sha256:${sha256(bytes)}",
                )
            }
    }

    if (sourceHashes.size > limits.maxGovernedSourceUnitsPerPackage) {
        diagnostics += diagnostic(
            code = "repository.admission.budget.source-units-exceeded",
            message = "Package source root `${root.toDisplayPath()}` exceeds PackageAdmissionLimits governed source unit budget.",
        )
    }
    if (admittedBytes > limits.maxAdmittedBytesPerPackage) {
        diagnostics += diagnostic(
            code = "repository.admission.budget.package-bytes-exceeded",
            message = "Package source root `${root.toDisplayPath()}` exceeds PackageAdmissionLimits package byte budget.",
        )
    }
    if (resourceHashes.size > limits.maxDeclaredResourcesPerPackage) {
        diagnostics += diagnostic(
            code = "repository.admission.budget.resources-exceeded",
            message = "Package source root `${root.toDisplayPath()}` exceeds PackageAdmissionLimits declared resource budget.",
        )
    }

    return AdmittedSourceHashResult(
        sourceHashes = sourceHashes,
        resourceHashes = resourceHashes,
        admittedBytes = admittedBytes,
        diagnostics = diagnostics,
    )
}

private fun resolveSourceRoot(
    repositoryRoot: Path,
    sourceRoot: String,
): Path {
    val sourceRootPath = Path.of(sourceRoot)
    return if (sourceRootPath.isAbsolute) {
        sourceRootPath.normalize()
    } else {
        repositoryRoot.resolve(sourceRoot).normalize()
    }
}

private fun validateAdmittedSourceRoot(
    repositoryRoot: Path,
    sourceRoot: String,
    resolvedRoot: Path,
): List<RepositoryDiagnostic> {
    val sourceRootPath = Path.of(sourceRoot)
    if (sourceRootPath.isAbsolute || sourceRoot.split('/').any { segment -> segment == ".." || segment.isBlank() }) {
        return listOf(
            diagnostic(
                code = "repository.admission.source-root.escaped",
                message = "Package source root `$sourceRoot` must remain repository-confined and relative.",
            ),
        )
    }
    val normalizedRepositoryRoot = repositoryRoot.toAbsolutePath().normalize()
    val normalizedSourceRoot = resolvedRoot.toAbsolutePath().normalize()
    if (!normalizedSourceRoot.startsWith(normalizedRepositoryRoot)) {
        return listOf(
            diagnostic(
                code = "repository.admission.source-root.escaped",
                message = "Package source root `${resolvedRoot.toDisplayPath()}` escapes repository root `${repositoryRoot.toDisplayPath()}`.",
            ),
        )
    }
    if (Files.isSymbolicLink(resolvedRoot)) {
        return listOf(
            diagnostic(
                code = "repository.admission.source-root.link-forbidden",
                message = "Package source root `${resolvedRoot.toDisplayPath()}` must not be a link or reparse point.",
            ),
        )
    }
    return emptyList()
}

private fun sourceRootTopologyDiagnostics(
    repositoryRoot: Path,
    packages: List<ResolvedPackage>,
): List<RepositoryDiagnostic> {
    val diagnostics = mutableListOf<RepositoryDiagnostic>()
    val roots = packages.map { resolvedPackage ->
        resolvedPackage to resolveSourceRoot(repositoryRoot, resolvedPackage.sourceRoot).toAbsolutePath().normalize()
    }
    roots
        .groupBy { (_, root) -> root.toDisplayPath().lowercase() }
        .filterValues { matches -> matches.size > 1 }
        .forEach { (_, matches) ->
            diagnostics += diagnostic(
                code = "repository.admission.source-root.duplicate-physical-root",
                message = "Multiple resolved packages map to the same physical source root: ${
                    matches.joinToString { (resolvedPackage, _) -> resolvedPackage.packageId.name }
                }.",
            )
        }
    roots.forEachIndexed { index, (leftPackage, leftRoot) ->
        roots.drop(index + 1).forEach { (rightPackage, rightRoot) ->
            if (leftRoot != rightRoot && (leftRoot.startsWith(rightRoot) || rightRoot.startsWith(leftRoot))) {
                diagnostics += diagnostic(
                    code = "repository.admission.source-root.overlap",
                    message = "Resolved package source roots must not overlap: `${leftPackage.packageId.name}` and `${rightPackage.packageId.name}`.",
                )
            }
        }
    }
    return diagnostics
}

private fun manifestDigest(repositoryRoot: Path, sourceRoot: String): String {
    val path = resolveSourceRoot(repositoryRoot, sourceRoot).resolve("package.yaml")
    return if (Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) "sha256:${sha256(Files.readAllBytes(path))}" else "sha256:${"0".repeat(64)}"
}

private val PRESENTATION_RESOURCE_EXTENSIONS = setOf("svg", "png", "woff2")

private fun isPackageResource(path: Path, sourceRoot: Path): Boolean = sourceRoot.relativize(path).any { segment ->
    segment.toString().equals("resources", ignoreCase = true)
}

private fun validatedLockStateDigest(packages: List<RepositoryLockedPackage>): String = "lock-state:" + sha256(
    buildString {
        appendLine("schema=$REPOSITORY_LOCK_SCHEMA")
        packages.sortedBy { locked -> stablePackageIdentifierKey(locked.packageId) + "|" + locked.sourceRoot }.forEach { locked ->
            appendLine("package=${locked.packageId.name}@${locked.packageId.version.orEmpty()}")
            appendLine("sourceRoot=${locked.sourceRoot}")
            appendLine("manifest=${locked.manifestDigest}")
            locked.itemDigests.sortedBy { it.itemId + "@" + it.itemVersion }.forEach { item ->
                appendLine("item=${item.itemId}@${item.itemVersion}:${item.digest}")
            }
            locked.directDependencies.sortedBy(::stablePackageIdentifierKey).forEach { dependency ->
                appendLine("dependency=${dependency.name}@${dependency.version.orEmpty()}")
            }
        }
    }.toByteArray(Charsets.UTF_8),
)

private fun writeLockAtomically(lockPath: Path, renderedLock: String): List<RepositoryDiagnostic> {
    val tempPath = lockPath.parent.resolve("athena.lock.tmp-${System.nanoTime()}")
    return try {
        Files.writeString(tempPath, renderedLock)
        Files.move(tempPath, lockPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        emptyList()
    } catch (exception: AtomicMoveNotSupportedException) {
        tempPath.deleteIfExists()
        listOf(
            diagnostic(
                code = "repository.lock.write-failed",
                message = "Atomic lock replacement is not supported for `${lockPath.toDisplayPath()}`: ${exception.message.orEmpty()}",
            ),
        )
    } catch (exception: Exception) {
        tempPath.deleteIfExists()
        listOf(
            diagnostic(
                code = "repository.lock.write-failed",
                message = "Could not write canonical `athena.lock` at `${lockPath.toDisplayPath()}`: ${exception.message ?: exception::class.simpleName}",
            ),
        )
    }
}

private fun sha256(bytes: ByteArray): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
    return digest.joinToString(separator = "") { byte -> "%02x".format(byte) }
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

private const val REPOSITORY_LOCK_VERSION = 3
private const val REPOSITORY_LOCK_SCHEMA = "athena-lock-v3"
private const val REPOSITORY_LOCK_COMPILER_SCHEMA = "athena-lock-v3-c14n-v1"
private val LOCK_PACKAGE_NAME_PATTERN = Regex("^[a-z][a-z0-9-]*(\\.[a-z][a-z0-9-]*)*$")
