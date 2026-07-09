package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import com.engineeringood.athena.compiler.CompilerCompilationParseFailure
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.repository.RepositoryDiagnostic
import com.engineeringood.athena.repository.RepositoryDiagnosticSeverity
import com.engineeringood.athena.scm.SemanticBaselineDescriptor
import com.engineeringood.athena.scm.SemanticBaselineSnapshot
import com.engineeringood.athena.scm.SemanticDiff
import com.engineeringood.athena.scm.SemanticDiffCalculator
import com.engineeringood.athena.semantics.core.SemanticDiagnostic
import com.engineeringood.athena.semantics.core.SemanticDiagnosticCategory
import com.engineeringood.athena.semantics.core.SemanticDiagnosticSeverity
import com.engineeringood.athena.semantics.core.SemanticRuleId

/**
 * Runtime-owned facade that compares the active repository session against a resolved baseline
 * through the shared semantic SCM kernel.
 */
class AthenaSemanticDiffService(
    private val calculator: SemanticDiffCalculator = SemanticDiffCalculator(),
) {
    /**
     * Compares the active runtime-owned repository session against one already-resolved semantic
     * baseline snapshot.
     */
    fun compareAgainstBaseline(
        session: RepositoryGraphSession,
        baseline: SemanticBaselineSnapshot,
    ): SemanticDiff {
        return calculator.calculate(
            baseline = baseline,
            current = currentSnapshot(session),
        )
    }

    private fun currentSnapshot(session: RepositoryGraphSession): SemanticBaselineSnapshot {
        val report = checkNotNull(session.report) {
            "Cannot produce a semantic diff when the active repository graph session has no canonical repository report."
        }
        val compilation = session.executionContext.compileActiveProject()
        val currentCompilation = compilation as? CompilerCompilationSuccess
        val currentDiagnostics = buildList {
            addAll(
                report.diagnostics.map { diagnostic ->
                    diagnostic.toSemanticDiagnostic(session)
                },
            )
            if (compilation is CompilerCompilationParseFailure) {
                addAll(
                    compilation.diagnostics.map { diagnostic ->
                        SemanticDiagnostic(
                            severity = SemanticDiagnosticSeverity.ERROR,
                            ruleId = SemanticRuleId("semantic.current.compile.parse-failed"),
                            category = SemanticDiagnosticCategory.DOMAIN,
                            subjectIdentity = null,
                            provenance = SourceProvenance(
                                file = diagnostic.file,
                                startLine = diagnostic.line,
                                startColumn = diagnostic.column,
                                endLine = diagnostic.endLine,
                                endColumn = diagnostic.endColumn,
                            ),
                            message = diagnostic.message,
                        )
                    },
                )
            }
        }.sortedWith(semanticDiagnosticComparator())

        return SemanticBaselineSnapshot(
            descriptor = SemanticBaselineDescriptor(
                baselineId = "current:${session.repositoryRoot.toAbsolutePath().normalize()}",
                label = "Current repository state",
            ),
            repositoryReport = report,
            engineeringDocuments = currentCompilation?.let { compilationSuccess ->
                listOf(compilationSuccess.document)
            }.orEmpty(),
            validationResult = currentCompilation?.semanticResult,
            diagnostics = currentDiagnostics,
        )
    }
}

private fun RepositoryDiagnostic.toSemanticDiagnostic(
    session: RepositoryGraphSession,
): SemanticDiagnostic {
    return SemanticDiagnostic(
        severity = if (severity == RepositoryDiagnosticSeverity.ERROR) {
            SemanticDiagnosticSeverity.ERROR
        } else {
            SemanticDiagnosticSeverity.WARNING
        },
        ruleId = SemanticRuleId("semantic.current.repository.$code"),
        category = SemanticDiagnosticCategory.DOMAIN,
        subjectIdentity = null,
        provenance = SourceProvenance(
            file = session.repositoryRoot.resolve("athena.yaml").normalize().toString(),
            startLine = 1,
            startColumn = 1,
            endLine = 1,
            endColumn = 1,
        ),
        message = message,
    )
}

private fun semanticDiagnosticComparator(): Comparator<SemanticDiagnostic> {
    return compareBy<SemanticDiagnostic>(
        { diagnostic -> diagnostic.ruleId.value },
        { diagnostic -> diagnostic.provenance.file },
        { diagnostic -> diagnostic.provenance.startLine },
        { diagnostic -> diagnostic.provenance.startColumn },
        { diagnostic -> diagnostic.message },
    )
}
