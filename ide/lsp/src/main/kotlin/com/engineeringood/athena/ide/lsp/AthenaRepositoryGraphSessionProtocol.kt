package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.repository.PackageDependencySource
import com.engineeringood.athena.repository.RepositoryDiagnosticSeverity

/**
 * Parameters for the Athena-owned repository graph session request.
 *
 * Story 3.2 does not need caller-controlled filtering yet, but the request remains a typed noun so
 * later IDE work can evolve the request surface without falling back to untyped maps.
 */
class AthenaRepositoryGraphSessionParams

/**
 * Read-only repository/package session payload returned through the Athena LSP boundary.
 */
data class AthenaRepositoryGraphSessionPayload(
    val repositoryRoot: String,
    val manifestPath: String,
    val lockPath: String,
    val sourceRootPath: String,
    val sourcePath: String,
    val projectName: String,
    val primaryPackageName: String,
    val semanticPath: String,
    val lastOpenedDocumentUri: String? = null,
    val lockState: String,
    val isValid: Boolean,
    val manifestDependencies: List<AthenaRepositoryManifestDependencyPayload>,
    val resolvedPackages: List<AthenaRepositoryResolvedPackagePayload>,
    val diagnostics: List<AthenaRepositoryDiagnosticPayload>,
)

/**
 * One authored manifest dependency entry returned through the Athena LSP boundary.
 */
data class AthenaRepositoryManifestDependencyPayload(
    val name: String,
    val version: String? = null,
    val source: String,
    val locator: String? = null,
)

/**
 * One resolved package entry returned through the Athena LSP boundary.
 */
data class AthenaRepositoryResolvedPackagePayload(
    val name: String,
    val version: String? = null,
    val sourceRoot: String,
    val directDependencies: List<String>,
)

/**
 * One inspectable repository/package diagnostic returned through the Athena LSP boundary.
 */
data class AthenaRepositoryDiagnosticPayload(
    val code: String,
    val severity: String,
    val message: String,
)

internal fun AthenaLspSessionHostReady.toRepositoryGraphSessionPayload(
    snapshot: AthenaLspSessionSnapshot?,
): AthenaRepositoryGraphSessionPayload {
    val report = session.report
    val repository = report?.repository
    val manifest = repository?.manifest
    val graph = report?.graph

    return AthenaRepositoryGraphSessionPayload(
        repositoryRoot = repositoryRoot.toString(),
        manifestPath = manifestPath.toString(),
        lockPath = lockPath.toString(),
        sourceRootPath = sourceRootPath.toString(),
        sourcePath = sourcePath.toString(),
        projectName = projectName,
        primaryPackageName = primaryPackageName,
        semanticPath = snapshot?.semanticPath ?: "frontend -> LSP -> runtime/compiler",
        lastOpenedDocumentUri = snapshot?.lastOpenedDocumentUri,
        lockState = session.publication.lockState.name.lowercase(),
        isValid = session.publication.isValid,
        manifestDependencies = manifest
            ?.dependencies
            .orEmpty()
            .map { dependency ->
                AthenaRepositoryManifestDependencyPayload(
                    name = dependency.packageId.name,
                    version = dependency.packageId.version,
                    source = dependency.source.toProtocolValue(),
                    locator = dependency.locator,
                )
            },
        resolvedPackages = graph
            ?.packages
            .orEmpty()
            .map { resolvedPackage ->
                AthenaRepositoryResolvedPackagePayload(
                    name = resolvedPackage.packageId.name,
                    version = resolvedPackage.packageId.version,
                    sourceRoot = resolvedPackage.sourceRoot,
                    directDependencies = resolvedPackage.directDependencies.map { dependency -> dependency.name },
                )
            },
        diagnostics = report
            ?.diagnostics
            .orEmpty()
            .map { diagnostic ->
                AthenaRepositoryDiagnosticPayload(
                    code = diagnostic.code,
                    severity = diagnostic.severity.toProtocolValue(),
                    message = diagnostic.message,
                )
            },
    )
}

private fun PackageDependencySource.toProtocolValue(): String {
    return name.lowercase().replace('_', '-')
}

private fun RepositoryDiagnosticSeverity.toProtocolValue(): String {
    return name.lowercase()
}
