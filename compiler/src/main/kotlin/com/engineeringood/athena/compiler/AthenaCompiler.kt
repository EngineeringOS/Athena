package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.boundary.AthenaBoundaryDescriptorResolver
import com.engineeringood.athena.compiler.boundary.AthenaBoundaryDescriptorSource
import com.engineeringood.athena.compiler.knowledge.AthenaKnowledgePackageSource
import com.engineeringood.athena.compiler.knowledge.AthenaKnowledgeResolver
import com.engineeringood.athena.compiler.plugin.AthenaApprovedPluginInventory
import com.engineeringood.athena.compiler.plugin.AthenaDomainSemanticsCoordinator
import com.engineeringood.athena.compiler.plugin.AthenaPluginDiscovery
import com.engineeringood.athena.compiler.plugin.AthenaPluginDiscoveryReport
import com.engineeringood.athena.compiler.plugin.AthenaPluginValidationContext
import com.engineeringood.athena.language.AthenaLanguageParser
import com.engineeringood.athena.language.ParseFailure
import com.engineeringood.athena.language.ParseResult
import com.engineeringood.athena.language.ParseSuccess
import com.engineeringood.athena.renderer.svg.SvgRenderer
import com.engineeringood.athena.semantics.core.SemanticContinuationDecision
import com.engineeringood.athena.semantics.core.SemanticDiagnostic
import com.engineeringood.athena.semantics.core.SemanticDiagnosticCategory
import com.engineeringood.athena.semantics.core.SemanticDiagnosticSeverity
import com.engineeringood.athena.semantics.core.SemanticRuleId
import com.engineeringood.athena.semantics.core.SemanticValidationResult
import com.engineeringood.athena.semantics.core.EngineeringIrValidator
import java.nio.file.Files
import java.nio.file.Path

/** Compiler entry point that orchestrates parsing and semantic lowering for the current M0 pipeline. */
class AthenaCompiler(
    private val parser: AthenaLanguageParser = AthenaLanguageParser(),
    private val validator: EngineeringIrValidator = EngineeringIrValidator(),
    private val renderModelDeriver: SvgRenderModelDeriver = SvgRenderModelDeriver(),
    private val svgRenderer: SvgRenderer = SvgRenderer(),
    private val pluginDiscovery: AthenaPluginDiscovery = AthenaPluginDiscovery(),
    private val knowledgePackageSource: AthenaKnowledgePackageSource = AthenaKnowledgePackageSource.empty(),
    private val knowledgeResolver: AthenaKnowledgeResolver = AthenaKnowledgeResolver(),
    private val boundaryDescriptorSource: AthenaBoundaryDescriptorSource = AthenaBoundaryDescriptorSource.empty(),
    private val boundaryDescriptorResolver: AthenaBoundaryDescriptorResolver = AthenaBoundaryDescriptorResolver(),
    lowerer: EngineeringIrLowerer? = null,
) {
    /** Deterministic discovery report built before any compilation pass uses plugin inventory. */
    val pluginDiscoveryReport: AthenaPluginDiscoveryReport = pluginDiscovery.discover()

    /** Approved plugin inventory attached at core-owned extension points for this compiler instance. */
    val pluginInventory: AthenaApprovedPluginInventory = pluginDiscoveryReport.approvedInventory

    private val domainSemanticsCoordinator: AthenaDomainSemanticsCoordinator = AthenaDomainSemanticsCoordinator(pluginInventory)
    private val lowerer: EngineeringIrLowerer = lowerer ?: EngineeringIrLowerer(domainSemanticsCoordinator)

    /** Parses the authored source file at [path] and returns the full syntax-owned document. */
    fun parse(path: Path): CompilerParseResult {
        return parseSource(path)
    }

    /** Parses and lowers the authored source file at [path] into canonical Engineering IR. */
    fun lower(path: Path): CompilerLoweringResult {
        return when (val parseResult = parseSource(path)) {
            is CompilerParseSuccess -> {
                val document = lowerer.lower(parseResult.source)
                val diagnostics = domainSemanticsUnavailableDiagnostics(parseResult.source, document)
                if (diagnostics.isEmpty()) {
                    CompilerLoweringSuccess(
                        source = parseResult.source,
                        document = document,
                    )
                } else {
                    CompilerLoweringSemanticFailure(
                        source = parseResult.source,
                        document = document,
                        diagnostics = diagnostics,
                    )
                }
            }

            is CompilerParseFailure -> CompilerLoweringFailure(parseResult.diagnostics)
        }
    }

    /** Parses, lowers, and semantically validates the authored source file at [path]. */
    fun compile(path: Path): CompilerCompilationResult {
        val knowledgeContext = knowledgeResolver.resolve(knowledgePackageSource)
        val boundaryValidation = boundaryDescriptorResolver.resolve(boundaryDescriptorSource)

        return when (val parseResult = parseSource(path)) {
            is CompilerParseSuccess -> {
                val parseRecord = CompilerPassRecord(
                    pass = PARSE_PASS,
                    status = CompilerPassExecutionStatus.SUCCEEDED,
                    outputSummary = systemIdentitySummary(parseResult.source),
                )
                val document = lowerer.lower(parseResult.source)
                val lowerRecord = CompilerPassRecord(
                    pass = LOWER_PASS,
                    status = CompilerPassExecutionStatus.SUCCEEDED,
                    outputSummary = document.system.id.value,
                )
                val semanticResult = validateSemantics(parseResult.source, document)
                val validateRecord = CompilerPassRecord(
                    pass = VALIDATE_PASS,
                    status = CompilerPassExecutionStatus.SUCCEEDED,
                    outputSummary = semanticSummary(semanticResult),
                )
                val (rendering, downstreamRecord) = renderResult(semanticResult, document)
                val knowledgeAttributions = buildKnowledgeAttributions(knowledgeContext)
                CompilerCompilationSuccess(
                    source = parseResult.source,
                    document = document,
                    semanticResult = semanticResult,
                    rendering = rendering,
                    knowledgeContext = knowledgeContext,
                    boundaryValidation = boundaryValidation,
                    knowledgeAttributions = knowledgeAttributions,
                    pipeline = CompilerPipelineReport(
                        passes = listOf(parseRecord, lowerRecord, validateRecord, downstreamRecord),
                    ),
                )
            }

            is CompilerParseFailure -> CompilerCompilationParseFailure(
                diagnostics = parseResult.diagnostics,
                knowledgeContext = knowledgeContext,
                boundaryValidation = boundaryValidation,
                pipeline = CompilerPipelineReport(
                    passes = listOf(
                        CompilerPassRecord(
                            pass = PARSE_PASS,
                            status = CompilerPassExecutionStatus.FAILED,
                            outputSummary = "${parseResult.diagnostics.size} syntax diagnostics",
                        ),
                        skippedPassRecord(LOWER_PASS, "parse failed"),
                        skippedPassRecord(VALIDATE_PASS, "parse failed"),
                        skippedPassRecord(DOWNSTREAM_DERIVATION_PASS, "parse failed"),
                    ),
                ),
            )
        }
    }

    private fun parseSource(path: Path): CompilerParseResult {
        val sourceText = runCatching { Files.readString(path) }.getOrElse { exception ->
            return CompilerParseFailure(
                listOf(
                    CompilerSyntaxDiagnostic(
                        file = path.toString(),
                        line = 0,
                        column = 0,
                        message = "Could not read source file: ${exception.message ?: exception::class.simpleName}",
                    ),
                ),
            )
        }

        return when (val result: ParseResult = parser.parse(path.toString(), sourceText)) {
            is ParseSuccess -> CompilerParseSuccess(
                CompilerSourceDocument(
                    file = path.toString(),
                    ast = result.ast,
                ),
            )

            is ParseFailure -> CompilerParseFailure(
                result.diagnostics.map { diagnostic ->
                    CompilerSyntaxDiagnostic(
                        file = diagnostic.file,
                        line = diagnostic.line,
                        column = diagnostic.column,
                        message = diagnostic.message,
                    )
                },
            )
        }
    }

    private fun systemIdentitySummary(source: CompilerSourceDocument): String {
        return "system:${source.ast.system.name}"
    }

    private fun semanticSummary(result: com.engineeringood.athena.semantics.core.SemanticValidationResult): String {
        return if (result.isSemanticallyValid) "semantic-valid" else "semantic-invalid"
    }

    private fun validateSemantics(
        source: CompilerSourceDocument,
        document: com.engineeringood.athena.ir.EngineeringIrDocument,
    ): SemanticValidationResult {
        val baseResult = validator.validate(document)
        val domainDiagnostics = buildList {
            addAll(domainSemanticsUnavailableDiagnostics(source, document))

            addAll(
                domainSemanticsCoordinator.validate(
                    document = document,
                    context = AthenaPluginValidationContext(
                        document = document,
                        source = source,
                        approvedPluginIds = domainSemanticsCoordinator.activePluginIds,
                    ),
                ).diagnostics,
            )
        }

        val diagnostics = baseResult.diagnostics + domainDiagnostics
        return SemanticValidationResult(
            diagnostics = diagnostics,
            continuationDecision = if (diagnostics.any { it.severity == SemanticDiagnosticSeverity.ERROR }) {
                SemanticContinuationDecision.STOP_DOWNSTREAM
            } else {
                SemanticContinuationDecision.CONTINUE
            },
        )
    }

    private fun domainSemanticsUnavailableDiagnostics(
        source: CompilerSourceDocument,
        document: com.engineeringood.athena.ir.EngineeringIrDocument,
    ): List<SemanticDiagnostic> {
        if (domainSemanticsCoordinator.hasActivePlugins || source.ast.declarations.isEmpty()) {
            return emptyList()
        }

        return listOf(
            SemanticDiagnostic(
                severity = SemanticDiagnosticSeverity.ERROR,
                ruleId = SemanticRuleId("domain.semantics.unavailable"),
                category = SemanticDiagnosticCategory.DOMAIN,
                subjectIdentity = document.system.id,
                provenance = document.system.provenance,
                message = "No approved domain plugin is active for the authored domain semantics in `${document.system.name}`.",
            ),
        )
    }

    private fun renderResult(
        result: com.engineeringood.athena.semantics.core.SemanticValidationResult,
        document: com.engineeringood.athena.ir.EngineeringIrDocument,
    ): Pair<CompilerRenderingResult, CompilerPassRecord> {
        return if (result.continuationDecision == com.engineeringood.athena.semantics.core.SemanticContinuationDecision.CONTINUE) {
            val renderModel = renderModelDeriver.derive(document)
            val svg = svgRenderer.render(renderModel)
            CompilerRenderingSuccess(
                model = renderModel,
                svg = svg,
            ) to CompilerPassRecord(
                pass = DOWNSTREAM_DERIVATION_PASS,
                status = CompilerPassExecutionStatus.SUCCEEDED,
                outputSummary = "svg-emitted",
            )
        } else {
            CompilerRenderingBlocked(
                reason = "semantic validation requested ${result.continuationDecision}",
                blockedByPass = CompilerPassId.VALIDATE,
            ) to CompilerPassRecord(
                pass = DOWNSTREAM_DERIVATION_PASS,
                status = CompilerPassExecutionStatus.FAILED,
                outputSummary = "render-blocked",
            )
        }
    }

    private fun skippedPassRecord(pass: CompilerPassDescriptor, reason: String): CompilerPassRecord {
        return CompilerPassRecord(
            pass = pass,
            status = CompilerPassExecutionStatus.SKIPPED,
            outputSummary = reason,
        )
    }

    private fun buildKnowledgeAttributions(
        knowledgeContext: com.engineeringood.athena.compiler.knowledge.AthenaCompilationKnowledgeContext,
    ): List<CompilerKnowledgeAttribution> {
        val activeArtifactReferences = knowledgeContext.activeArtifacts.map { artifact ->
            CompilerKnowledgeArtifactReference(
                artifactId = artifact.artifactId,
                artifactKind = artifact.artifactKind,
                artifactVersion = artifact.artifactVersion,
                provenance = artifact.provenance,
            )
        }

        return listOf(
            CompilerKnowledgeAttribution(
                target = CompilerKnowledgeAttributionTarget.KNOWLEDGE_CONTEXT,
                responsibleArtifacts = activeArtifactReferences,
                rationale = "These reviewed governed knowledge artifacts define the active compilation context for this compiler run.",
            ),
            CompilerKnowledgeAttribution(
                target = CompilerKnowledgeAttributionTarget.SEMANTIC_RESULT,
                responsibleArtifacts = emptyList(),
                rationale = "Story 2.5 does not yet allow governed knowledge artifacts to directly influence semantic diagnostics or continuation decisions.",
            ),
            CompilerKnowledgeAttribution(
                target = CompilerKnowledgeAttributionTarget.RENDERING,
                responsibleArtifacts = emptyList(),
                rationale = "Story 2.5 does not yet allow governed knowledge artifacts to directly influence downstream derivation or SVG rendering.",
            ),
        )
    }
}

private val PARSE_PASS = CompilerPassDescriptor(
    id = CompilerPassId.PARSE,
    responsibility = "Parse authored source into syntax-owned AST",
    inputState = "authored source file",
    outputState = "syntax-owned source document",
)

private val LOWER_PASS = CompilerPassDescriptor(
    id = CompilerPassId.LOWER,
    responsibility = "Lower syntax-owned source into canonical Engineering IR",
    inputState = "syntax-owned source document",
    outputState = "canonical Engineering IR",
)

private val VALIDATE_PASS = CompilerPassDescriptor(
    id = CompilerPassId.VALIDATE,
    responsibility = "Validate canonical Engineering IR and compute continuation policy",
    inputState = "canonical Engineering IR",
    outputState = "semantic validation result",
)

private val DOWNSTREAM_DERIVATION_PASS = CompilerPassDescriptor(
    id = CompilerPassId.DOWNSTREAM_DERIVATION,
    responsibility = "Derive the thin render-facing model and emit simple SVG when policy allows",
    inputState = "semantic validation result",
    outputState = "render result",
)
