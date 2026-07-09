package com.engineeringood.athena.compiler.repository

import com.engineeringood.athena.repository.PackageIdentifier
import com.engineeringood.athena.repository.RepositoryDiagnostic
import com.engineeringood.athena.repository.RepositoryDiagnosticSeverity
import com.engineeringood.athena.repository.RepositoryResolutionDependency
import com.engineeringood.athena.repository.RepositoryResolutionInput
import com.engineeringood.athena.repository.ResolvedPackage
import com.engineeringood.athena.repository.ResolvedPackageGraph
import java.nio.file.Path
import java.util.Locale

/**
 * Resolves a governed repository into the first canonical local-first package graph.
 */
class AthenaRepositoryGraphResolver(
    private val contractLoader: AthenaRepositoryContractLoader = AthenaRepositoryContractLoader(),
    private val resolutionInputBuilder: AthenaRepositoryResolutionInputBuilder = AthenaRepositoryResolutionInputBuilder(),
) {
    private val contractLoadOptions = AthenaRepositoryContractLoadOptions(
        allowNestedGovernedSubrepositories = true,
    )

    /**
     * Resolves [repositoryRoot] into a deterministic local package graph without touching lock state.
     */
    fun resolve(repositoryRoot: Path): AthenaRepositoryGraphResolutionResult {
        val rootValidation = contractLoader.load(repositoryRoot, contractLoadOptions)
        val rootInputResult = resolutionInputBuilder.build(rootValidation)
        if (!rootInputResult.isValid || rootInputResult.repository == null || rootInputResult.resolutionInput == null) {
            return AthenaRepositoryGraphResolutionResult(
                repositoryRoot = rootInputResult.repositoryRoot,
                manifestPath = rootInputResult.manifestPath,
                lockPath = rootInputResult.lockPath,
                manifestPresent = rootInputResult.manifestPresent,
                lockPresent = rootInputResult.lockPresent,
                repository = rootInputResult.repository,
                resolutionInput = rootInputResult.resolutionInput,
                diagnostics = rootInputResult.diagnostics,
            )
        }

        val diagnostics = rootInputResult.diagnostics.toMutableList()
        val rootResolutionRoot = normalizeRepositoryRoot(rootInputResult.repositoryRoot)
        val resolvedNodesByKey = linkedMapOf<String, ResolvedRepositoryNode>()
        val cachedInputResultsByRootKey = mutableMapOf<String, AthenaRepositoryResolutionInputResult>()
        val pendingIdentityReferences = mutableListOf<PendingIdentityReference>()
        val pendingRepositories = ArrayDeque<PendingRepositoryResolution>()

        val rootNode = rootInputResult.toResolvedNode(rootResolutionRoot)
        resolvedNodesByKey[rootNode.packageKey] = rootNode
        cachedInputResultsByRootKey[stablePathKey(rootResolutionRoot)] = rootInputResult
        pendingRepositories += PendingRepositoryResolution(rootResolutionRoot, rootInputResult)

        while (pendingRepositories.isNotEmpty()) {
            val pendingRepository = pendingRepositories.removeFirst()
            val ownerNode = resolvedNodesByKey[packageKey(pendingRepository.input.resolutionInput!!.rootPackage)]
                ?: continue

            pendingRepository.input.resolutionInput.dependencies.forEach { dependency ->
                when (dependency.source) {
                    com.engineeringood.athena.repository.PackageDependencySource.LOCAL_PATH -> {
                        resolveLocalPathDependency(
                            rootResolutionRoot = rootResolutionRoot,
                            ownerRepositoryRoot = pendingRepository.repositoryRoot,
                            ownerNode = ownerNode,
                            dependency = dependency,
                            cachedInputResultsByRootKey = cachedInputResultsByRootKey,
                            resolvedNodesByKey = resolvedNodesByKey,
                            pendingRepositories = pendingRepositories,
                            diagnostics = diagnostics,
                        )
                    }

                    com.engineeringood.athena.repository.PackageDependencySource.LOCAL_PACKAGE -> {
                        pendingIdentityReferences += PendingIdentityReference(
                            ownerPackageKey = ownerNode.packageKey,
                            dependency = dependency,
                        )
                    }
                }
            }
        }

        resolvePendingIdentityReferences(
            resolvedNodesByKey = resolvedNodesByKey,
            pendingIdentityReferences = pendingIdentityReferences,
            diagnostics = diagnostics,
        )

        val graph = buildResolvedGraph(
            rootPackage = rootInputResult.resolutionInput.rootPackage,
            resolvedNodesByKey = resolvedNodesByKey,
        )

        return AthenaRepositoryGraphResolutionResult(
            repositoryRoot = rootInputResult.repositoryRoot,
            manifestPath = rootInputResult.manifestPath,
            lockPath = rootInputResult.lockPath,
            manifestPresent = rootInputResult.manifestPresent,
            lockPresent = rootInputResult.lockPresent,
            repository = rootInputResult.repository,
            resolutionInput = rootInputResult.resolutionInput,
            graph = graph,
            diagnostics = diagnostics,
        )
    }

    private fun resolveLocalPathDependency(
        rootResolutionRoot: Path,
        ownerRepositoryRoot: Path,
        ownerNode: ResolvedRepositoryNode,
        dependency: RepositoryResolutionDependency,
        cachedInputResultsByRootKey: MutableMap<String, AthenaRepositoryResolutionInputResult>,
        resolvedNodesByKey: MutableMap<String, ResolvedRepositoryNode>,
        pendingRepositories: ArrayDeque<PendingRepositoryResolution>,
        diagnostics: MutableList<RepositoryDiagnostic>,
    ) {
        val locator = dependency.locator
        if (locator.isNullOrBlank()) {
            diagnostics += diagnostic(
                code = "repository.resolution.local-path.locator.missing",
                message = "Dependency `${dependency.packageId.render()}` cannot resolve because its normalized `local-path` locator is missing.",
            )
            return
        }

        val dependencyRepositoryRoot = normalizeDependencyRoot(ownerRepositoryRoot, locator)
        val cachedResult = cachedInputResultsByRootKey.getOrPut(stablePathKey(dependencyRepositoryRoot)) {
            resolutionInputBuilder.build(
                contractLoader.load(dependencyRepositoryRoot, contractLoadOptions),
            )
        }

        if (!cachedResult.isValid || cachedResult.repository == null || cachedResult.resolutionInput == null) {
            diagnostics += cachedResult.diagnostics.map { diagnostic ->
                diagnostic(
                    code = diagnostic.code,
                    message = "Dependency `${dependency.packageId.render()}` at `${locator}` is invalid: ${diagnostic.message}",
                )
            }
            if (cachedResult.diagnostics.isEmpty()) {
                diagnostics += diagnostic(
                    code = "repository.resolution.local-path.target.invalid",
                    message = "Dependency `${dependency.packageId.render()}` at `${locator}` could not be resolved as a valid governed repository root.",
                )
            }
            return
        }

        val resolvedPackageId = cachedResult.resolutionInput.rootPackage
        if (!declaredPackageMatches(dependency.packageId, resolvedPackageId)) {
            diagnostics += diagnostic(
                code = "repository.resolution.local-path.package-id.mismatch",
                message = "Dependency `${dependency.packageId.render()}` at `${locator}` resolved package `${resolvedPackageId.render()}` instead of the declared package identity.",
            )
            return
        }

        val dependencyNode = cachedResult.toResolvedNode(
            rootResolutionRoot = rootResolutionRoot,
            resolvedRepositoryRoot = dependencyRepositoryRoot,
        )
        val existingNode = resolvedNodesByKey[dependencyNode.packageKey]
        if (existingNode == null) {
            resolvedNodesByKey[dependencyNode.packageKey] = dependencyNode
            pendingRepositories += PendingRepositoryResolution(dependencyRepositoryRoot, cachedResult)
        } else if (stablePathKey(existingNode.repositoryRoot) != stablePathKey(dependencyRepositoryRoot)) {
            diagnostics += diagnostic(
                code = "repository.resolution.package.identity.conflict",
                message = "Package `${resolvedPackageId.render()}` resolved from conflicting roots `${existingNode.repositoryRoot.toDisplayPath()}` and `${dependencyRepositoryRoot.toDisplayPath()}`.",
            )
            return
        }

        ownerNode.addDirectDependency(resolvedPackageId)
    }

    private fun resolvePendingIdentityReferences(
        resolvedNodesByKey: Map<String, ResolvedRepositoryNode>,
        pendingIdentityReferences: List<PendingIdentityReference>,
        diagnostics: MutableList<RepositoryDiagnostic>,
    ) {
        pendingIdentityReferences.forEach { pendingReference ->
            val ownerNode = resolvedNodesByKey[pendingReference.ownerPackageKey] ?: return@forEach
            val matches = resolvedNodesByKey.values.filter { candidate ->
                candidate.packageId.name == pendingReference.dependency.packageId.name &&
                    (
                        pendingReference.dependency.packageId.version == null ||
                            candidate.packageId.version == pendingReference.dependency.packageId.version
                        )
            }

            when {
                matches.isEmpty() -> diagnostics += diagnostic(
                    code = "repository.resolution.local-package.unresolved",
                    message = "Dependency `${pendingReference.dependency.packageId.render()}` could not be resolved from the current local package graph context.",
                )

                matches.size > 1 && pendingReference.dependency.packageId.version == null -> diagnostics += diagnostic(
                    code = "repository.resolution.local-package.ambiguous",
                    message = "Dependency `${pendingReference.dependency.packageId.render()}` matched multiple discovered package versions. Declare an explicit version to disambiguate.",
                )

                else -> ownerNode.addDirectDependency(matches.single().packageId)
            }
        }
    }

    private fun buildResolvedGraph(
        rootPackage: PackageIdentifier,
        resolvedNodesByKey: Map<String, ResolvedRepositoryNode>,
    ): ResolvedPackageGraph {
        val rootPackageKey = packageKey(rootPackage)
        val rootNode = resolvedNodesByKey[rootPackageKey]
        val dependencyNodes = resolvedNodesByKey.values
            .filter { node -> node.packageKey != rootPackageKey }
            .sortedWith(compareBy(ResolvedRepositoryNode::packageSortKey))

        val orderedNodes = buildList {
            rootNode?.let(::add)
            addAll(dependencyNodes)
        }

        return ResolvedPackageGraph(
            rootPackage = rootPackage,
            packages = orderedNodes.map { node ->
                ResolvedPackage(
                    packageId = node.packageId,
                    sourceRoot = node.graphSourceRoot,
                    directDependencies = node.directDependencies
                        .distinctBy(::packageKey)
                        .sortedBy(::packageKey),
                )
            },
        )
    }
}

private data class PendingRepositoryResolution(
    val repositoryRoot: Path,
    val input: AthenaRepositoryResolutionInputResult,
)

private data class PendingIdentityReference(
    val ownerPackageKey: String,
    val dependency: RepositoryResolutionDependency,
)

private data class ResolvedRepositoryNode(
    val packageId: PackageIdentifier,
    val repositoryRoot: Path,
    val graphSourceRoot: String,
    val directDependencies: MutableList<PackageIdentifier> = mutableListOf(),
) {
    val packageKey: String
        get() = packageKey(packageId)

    val packageSortKey: String
        get() = listOf(packageId.name, packageId.version.orEmpty(), graphSourceRoot).joinToString("|")

    fun addDirectDependency(packageId: PackageIdentifier) {
        directDependencies += packageId
    }
}

private fun AthenaRepositoryResolutionInputResult.toResolvedNode(
    rootResolutionRoot: Path,
    resolvedRepositoryRoot: Path = repositoryRoot,
): ResolvedRepositoryNode {
    val resolutionInput = resolutionInput ?: error("Resolution input is required to build a resolved node.")
    return ResolvedRepositoryNode(
        packageId = resolutionInput.rootPackage,
        repositoryRoot = resolvedRepositoryRoot,
        graphSourceRoot = renderGraphSourceRoot(
            rootResolutionRoot = rootResolutionRoot,
            resolvedRepositoryRoot = resolvedRepositoryRoot,
            sourceRoot = resolutionInput.rootSourcePath,
        ),
    )
}

private fun renderGraphSourceRoot(
    rootResolutionRoot: Path,
    resolvedRepositoryRoot: Path,
    sourceRoot: String,
): String {
    val resolvedSourceRoot = normalizeRepositoryRoot(resolvedRepositoryRoot.resolve(sourceRoot))
    return if (resolvedSourceRoot.startsWith(rootResolutionRoot)) {
        rootResolutionRoot.relativize(resolvedSourceRoot).toDisplayPath()
    } else {
        resolvedSourceRoot.toDisplayPath()
    }
}

private fun normalizeDependencyRoot(
    ownerRepositoryRoot: Path,
    locator: String,
): Path {
    val candidatePath = Path.of(locator)
    val unresolvedPath = if (candidatePath.isAbsolute) candidatePath else ownerRepositoryRoot.resolve(locator)
    return normalizeRepositoryRoot(unresolvedPath)
}

private fun normalizeRepositoryRoot(path: Path): Path = runCatching { path.toRealPath() }.getOrElse { path.toAbsolutePath().normalize() }

private fun stablePathKey(path: Path): String {
    val pathKey = normalizeRepositoryRoot(path).toDisplayPath()
    return if (System.getProperty("os.name").startsWith("Windows")) {
        pathKey.lowercase(Locale.ROOT)
    } else {
        pathKey
    }
}

private fun packageKey(packageId: PackageIdentifier): String {
    return listOf(packageId.name, packageId.version.orEmpty()).joinToString("|")
}

private fun declaredPackageMatches(
    declaredPackageId: PackageIdentifier,
    resolvedPackageId: PackageIdentifier,
): Boolean {
    return declaredPackageId.name == resolvedPackageId.name &&
        (
            declaredPackageId.version == null ||
                declaredPackageId.version == resolvedPackageId.version
            )
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

private fun PackageIdentifier.render(): String {
    return version?.let { "$name@$it" } ?: name
}

private fun Path.toDisplayPath(): String = toString().replace('\\', '/')
