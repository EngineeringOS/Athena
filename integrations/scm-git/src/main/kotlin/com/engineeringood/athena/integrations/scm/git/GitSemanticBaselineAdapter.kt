package com.engineeringood.athena.integrations.scm.git

import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.compiler.CompilerCompilationParseFailure
import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import com.engineeringood.athena.repository.RepositoryDiagnostic
import com.engineeringood.athena.repository.RepositoryDiagnosticSeverity
import com.engineeringood.athena.repository.RepositoryGraphReport
import com.engineeringood.athena.scm.SemanticBaselineAdapter
import com.engineeringood.athena.scm.SemanticBaselineResolutionRequest
import com.engineeringood.athena.scm.SemanticBaselineResolutionResult
import com.engineeringood.athena.scm.SemanticBaselineSnapshot
import com.engineeringood.athena.scm.baselineResolutionDiagnostic
import com.engineeringood.athena.semantics.core.SemanticDiagnostic
import com.engineeringood.athena.semantics.core.SemanticDiagnosticSeverity
import java.nio.file.Path
import java.nio.file.Files
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.pathString

/**
 * First substrate-backed semantic baseline adapter seeded for M6.
 *
 * The adapter keeps Git-shaped repository loading in the integration layer while returning only
 * Athena-owned semantic baseline contracts to `:kernel:semantic-scm`.
 */
class GitSemanticBaselineAdapter(
    private val compilerProvider: () -> AthenaCompiler = { AthenaCompiler() },
) : SemanticBaselineAdapter {
    override val adapterId: String = ADAPTER_ID

    override fun resolve(request: SemanticBaselineResolutionRequest): SemanticBaselineResolutionResult {
        val baselineRoot = resolveBaselineRoot(request)
            ?: return SemanticBaselineResolutionResult(
                descriptor = request.descriptor,
                diagnostics = listOf(
                    baselineResolutionDiagnostic(
                        ruleId = "semantic.baseline.locator.missing",
                        message = "Semantic baseline locator is empty for adapter `$adapterId`.",
                        locator = request.locator,
                    ),
                ),
            )

        if (!baselineRoot.exists() || !baselineRoot.isDirectory()) {
            return SemanticBaselineResolutionResult(
                descriptor = request.descriptor,
                diagnostics = listOf(
                    baselineResolutionDiagnostic(
                        ruleId = "semantic.baseline.repository-root.missing",
                        message = "Semantic baseline repository root `$baselineRoot` does not exist.",
                        locator = request.locator,
                    ),
                ),
            )
        }

        return try {
            val compiler = compilerProvider()
            val publication = compiler.publishRepositoryGraphReport(baselineRoot)
            val diagnostics = publication.diagnostics.toSemanticDiagnostics(request)
            val report = publication.report

            if (publication.isValid && report != null) {
                val engineeringState = compileEngineeringState(
                    compiler = compiler,
                    repositoryRoot = baselineRoot,
                    report = report,
                    request = request,
                )
                val snapshotDiagnostics = (diagnostics + engineeringState.diagnostics).sortedWith(semanticDiagnosticComparator())
                SemanticBaselineResolutionResult(
                    descriptor = request.descriptor,
                    snapshot = SemanticBaselineSnapshot(
                        descriptor = request.descriptor,
                        repositoryReport = report,
                        engineeringDocuments = engineeringState.engineeringDocuments,
                        validationResult = engineeringState.validationResult,
                        diagnostics = snapshotDiagnostics,
                    ),
                    diagnostics = snapshotDiagnostics,
                )
            } else {
                SemanticBaselineResolutionResult(
                    descriptor = request.descriptor,
                    diagnostics = if (diagnostics.isNotEmpty()) {
                        diagnostics
                    } else {
                        listOf(
                            baselineResolutionDiagnostic(
                                ruleId = "semantic.baseline.publication.unavailable",
                                message = "Compiler-owned baseline publication did not yield a canonical repository graph report.",
                                locator = request.locator,
                            ),
                        )
                    },
                )
            }
        } catch (error: Exception) {
            SemanticBaselineResolutionResult(
                descriptor = request.descriptor,
                diagnostics = listOf(
                    baselineResolutionDiagnostic(
                        ruleId = "semantic.baseline.adapter.execution.failed",
                        message = "Semantic baseline adapter `$adapterId` failed: ${error.message ?: error::class.simpleName.orEmpty()}",
                        locator = request.locator,
                    ),
                ),
            )
        }
    }

    private fun compileEngineeringState(
        compiler: AthenaCompiler,
        repositoryRoot: Path,
        report: RepositoryGraphReport,
        request: SemanticBaselineResolutionRequest,
    ): BaselineEngineeringState {
        val sourceRoot = repositoryRoot.resolve(report.repository.manifest.primaryPackage.sourceRoot).normalize()
        if (!sourceRoot.exists() || !sourceRoot.isDirectory()) {
            return BaselineEngineeringState()
        }

        val sourcePath = Files.walk(sourceRoot).use { paths ->
            paths
                .filter { path -> Files.isRegularFile(path) && path.extension.equals("athena", ignoreCase = true) }
                .sorted(compareBy<Path> { path -> path.toAbsolutePath().normalize().pathString })
                .findFirst()
                .orElse(null)
        } ?: return BaselineEngineeringState()

        return when (val compilation = compiler.compile(sourcePath)) {
            is CompilerCompilationSuccess -> BaselineEngineeringState(
                engineeringDocuments = listOf(compilation.document),
                validationResult = compilation.semanticResult,
                diagnostics = compilation.semanticResult.diagnostics,
            )

            is CompilerCompilationParseFailure -> BaselineEngineeringState(
                diagnostics = compilation.diagnostics.map { diagnostic ->
                    baselineResolutionDiagnostic(
                        ruleId = "semantic.baseline.compile.parse-failed",
                        message = diagnostic.message,
                        locator = request.locator,
                    )
                },
            )
        }
    }

    private fun resolveBaselineRoot(request: SemanticBaselineResolutionRequest): Path? {
        val rawLocator = request.locator.locator.trim()
        if (rawLocator.isEmpty()) {
            return null
        }

        val locatorPath = Path.of(rawLocator)
        return if (locatorPath.isAbsolute) {
            locatorPath.normalize()
        } else {
            request.currentRepositoryRoot.resolve(locatorPath).normalize()
        }
    }

    private fun List<RepositoryDiagnostic>.toSemanticDiagnostics(
        request: SemanticBaselineResolutionRequest,
    ): List<SemanticDiagnostic> {
        return map { diagnostic ->
            baselineResolutionDiagnostic(
                ruleId = "semantic.baseline.repository.${diagnostic.code}",
                message = diagnostic.message,
                locator = request.locator,
                severity = if (diagnostic.severity == RepositoryDiagnosticSeverity.ERROR) {
                    SemanticDiagnosticSeverity.ERROR
                } else {
                    SemanticDiagnosticSeverity.WARNING
                },
            )
        }
    }

    companion object {
        /** Stable adapter identifier used by the current Git-backed baseline-loading proof. */
        const val ADAPTER_ID: String = "scm-git"
    }
}

private data class BaselineEngineeringState(
    val engineeringDocuments: List<com.engineeringood.athena.ir.EngineeringDocument> = emptyList(),
    val validationResult: com.engineeringood.athena.semantics.core.SemanticValidationResult? = null,
    val diagnostics: List<SemanticDiagnostic> = emptyList(),
)

private fun semanticDiagnosticComparator(): Comparator<SemanticDiagnostic> {
    return compareBy<SemanticDiagnostic>(
        { diagnostic -> diagnostic.ruleId.value },
        { diagnostic -> diagnostic.provenance.file },
        { diagnostic -> diagnostic.provenance.startLine },
        { diagnostic -> diagnostic.provenance.startColumn },
        { diagnostic -> diagnostic.message },
    )
}
