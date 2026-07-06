package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.boundary.AthenaBoundaryDescriptorResolver
import com.engineeringood.athena.compiler.boundary.AthenaBoundaryDescriptorSource
import com.engineeringood.athena.compiler.knowledge.AthenaKnowledgePackageSource
import com.engineeringood.athena.compiler.knowledge.AthenaKnowledgeResolver
import com.engineeringood.athena.compiler.plugin.AthenaApprovedPluginInventory
import com.engineeringood.athena.compiler.plugin.AthenaDomainPlugin
import com.engineeringood.athena.compiler.plugin.AthenaDomainSemanticsCoordinator
import com.engineeringood.athena.compiler.plugin.AthenaPluginDiscovery
import com.engineeringood.athena.compiler.plugin.AthenaPluginDiscoveryReport
import com.engineeringood.athena.compiler.plugin.AthenaPluginValidationContext
import com.engineeringood.athena.language.AthenaLanguageParser
import com.engineeringood.athena.language.ParseFailure
import com.engineeringood.athena.language.ParseResult
import com.engineeringood.athena.language.ParseSuccess
import com.engineeringood.athena.ir.EngineeringIrDocument
import com.engineeringood.athena.renderer.svg.SvgRenderer
import com.engineeringood.athena.renderer.svg.SvgRenderModel
import com.engineeringood.athena.semantics.core.EngineeringIrValidationScope
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
    hostedPluginDiscoveryReport: AthenaPluginDiscoveryReport? = null,
    hostedDomainPlugins: List<AthenaDomainPlugin>? = null,
    private val knowledgePackageSource: AthenaKnowledgePackageSource = AthenaKnowledgePackageSource.empty(),
    private val knowledgeResolver: AthenaKnowledgeResolver = AthenaKnowledgeResolver(),
    private val boundaryDescriptorSource: AthenaBoundaryDescriptorSource = AthenaBoundaryDescriptorSource.empty(),
    private val boundaryDescriptorResolver: AthenaBoundaryDescriptorResolver = AthenaBoundaryDescriptorResolver(),
    lowerer: EngineeringIrLowerer? = null,
) {
    /** Deterministic discovery report built before any compilation pass uses plugin inventory. */
    val pluginDiscoveryReport: AthenaPluginDiscoveryReport = hostedPluginDiscoveryReport ?: pluginDiscovery.discover()

    /** Approved plugin inventory attached at core-owned extension points for this compiler instance. */
    val pluginInventory: AthenaApprovedPluginInventory = pluginDiscoveryReport.approvedInventory

    private val domainSemanticsCoordinator: AthenaDomainSemanticsCoordinator = hostedDomainPlugins?.let { domainPlugins ->
        AthenaDomainSemanticsCoordinator(activeDomainPlugins = domainPlugins)
    } ?: AthenaDomainSemanticsCoordinator(pluginInventory)
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
                buildCompilationSuccess(
                    source = parseResult.source,
                    document = document,
                    knowledgeContext = knowledgeContext,
                    boundaryValidation = boundaryValidation,
                    parseRecord = parseRecord,
                    lowerRecord = CompilerPassRecord(
                        pass = LOWER_PASS,
                        status = CompilerPassExecutionStatus.SUCCEEDED,
                        outputSummary = document.system.id.value,
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

    /**
     * Re-evaluates an already materialized canonical [document] after a runtime-owned mutation without reparsing source.
     */
    fun recompute(
        source: CompilerSourceDocument,
        document: EngineeringIrDocument,
        affectedScope: CompilerAffectedScope,
        previousRendering: CompilerRenderingResult,
    ): CompilerCompilationSuccess {
        val knowledgeContext = knowledgeResolver.resolve(knowledgePackageSource)
        val boundaryValidation = boundaryDescriptorResolver.resolve(boundaryDescriptorSource)

        return buildCompilationSuccess(
            source = source,
            document = document,
            knowledgeContext = knowledgeContext,
            boundaryValidation = boundaryValidation,
            parseRecord = skippedPassRecord(PARSE_PASS, "runtime-state reused"),
            lowerRecord = skippedPassRecord(LOWER_PASS, "runtime document reused: ${document.system.id.value}"),
            affectedScope = affectedScope,
            previousRendering = previousRendering,
        )
    }

    private fun buildCompilationSuccess(
        source: CompilerSourceDocument,
        document: EngineeringIrDocument,
        knowledgeContext: com.engineeringood.athena.compiler.knowledge.AthenaCompilationKnowledgeContext,
        boundaryValidation: com.engineeringood.athena.compiler.boundary.AthenaBoundaryValidationReport,
        parseRecord: CompilerPassRecord,
        lowerRecord: CompilerPassRecord,
        affectedScope: CompilerAffectedScope? = null,
        previousRendering: CompilerRenderingResult? = null,
    ): CompilerCompilationSuccess {
        val (semanticResult, validationMode) = validateSemantics(source, document, affectedScope)
        val validateRecord = CompilerPassRecord(
            pass = VALIDATE_PASS,
            status = CompilerPassExecutionStatus.SUCCEEDED,
            outputSummary = semanticSummary(semanticResult, affectedScope, validationMode),
        )
        val (rendering, renderingMode, downstreamRecord) = renderResult(
            result = semanticResult,
            document = document,
            affectedScope = affectedScope,
            previousRendering = previousRendering,
        )
        val knowledgeAttributions = buildKnowledgeAttributions(knowledgeContext)
        return CompilerCompilationSuccess(
            source = source,
            document = document,
            semanticResult = semanticResult,
            rendering = rendering,
            knowledgeContext = knowledgeContext,
            boundaryValidation = boundaryValidation,
            knowledgeAttributions = knowledgeAttributions,
            pipeline = CompilerPipelineReport(
                passes = listOf(parseRecord, lowerRecord, validateRecord, downstreamRecord),
            ),
            incrementalUpdateReport = affectedScope?.let { scope ->
                CompilerIncrementalUpdateReport(
                    affectedScope = scope,
                    validationMode = validationMode,
                    renderingMode = renderingMode,
                )
            },
        )
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

    private fun semanticSummary(
        result: com.engineeringood.athena.semantics.core.SemanticValidationResult,
        affectedScope: CompilerAffectedScope?,
        mode: CompilerIncrementalPassMode,
    ): String {
        val baseSummary = if (result.isSemanticallyValid) "semantic-valid" else "semantic-invalid"
        return if (affectedScope == null) {
            baseSummary
        } else {
            "$baseSummary (${mode.name.lowercase()} ${affectedScope.validationSemanticIds.size} ids)"
        }
    }

    private fun validateSemantics(
        source: CompilerSourceDocument,
        document: com.engineeringood.athena.ir.EngineeringIrDocument,
        affectedScope: CompilerAffectedScope?,
    ): Pair<SemanticValidationResult, CompilerIncrementalPassMode> {
        val validationMode = if (affectedScope == null) {
            CompilerIncrementalPassMode.FULL_FALLBACK
        } else {
            CompilerIncrementalPassMode.SCOPED
        }
        val baseResult = if (affectedScope == null) {
            validator.validate(document)
        } else {
            validator.validate(
                document = document,
                scope = EngineeringIrValidationScope(affectedScope.validationSemanticIds.toSet()),
            )
        }
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
        ) to validationMode
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
        affectedScope: CompilerAffectedScope?,
        previousRendering: CompilerRenderingResult?,
    ): Triple<CompilerRenderingResult, CompilerIncrementalPassMode, CompilerPassRecord> {
        return if (result.continuationDecision == com.engineeringood.athena.semantics.core.SemanticContinuationDecision.CONTINUE) {
            val incrementalRenderModel = incrementalRenderModel(
                document = document,
                affectedScope = affectedScope,
                previousRendering = previousRendering,
            )
            val renderingMode = if (incrementalRenderModel != null) {
                CompilerIncrementalPassMode.SCOPED
            } else {
                CompilerIncrementalPassMode.FULL_FALLBACK
            }
            val renderModel = incrementalRenderModel ?: renderModelDeriver.derive(document)
            val svg = svgRenderer.render(renderModel)
            Triple(
                CompilerRenderingSuccess(
                    model = renderModel,
                    svg = svg,
                ),
                renderingMode,
                CompilerPassRecord(
                    pass = DOWNSTREAM_DERIVATION_PASS,
                    status = CompilerPassExecutionStatus.SUCCEEDED,
                    outputSummary = if (affectedScope == null) {
                        "svg-emitted"
                    } else {
                        "svg-emitted (${renderingMode.name.lowercase()})"
                    },
                ),
            )
        } else {
            Triple(
                CompilerRenderingBlocked(
                    reason = "semantic validation requested ${result.continuationDecision}",
                    blockedByPass = CompilerPassId.VALIDATE,
                ),
                CompilerIncrementalPassMode.FULL_FALLBACK,
                CompilerPassRecord(
                    pass = DOWNSTREAM_DERIVATION_PASS,
                    status = CompilerPassExecutionStatus.FAILED,
                    outputSummary = if (affectedScope == null) {
                        "render-blocked"
                    } else {
                        "render-blocked (validation stopped downstream)"
                    },
                ),
            )
        }
    }

    private fun incrementalRenderModel(
        document: EngineeringIrDocument,
        affectedScope: CompilerAffectedScope?,
        previousRendering: CompilerRenderingResult?,
    ): SvgRenderModel? {
        if (affectedScope == null) {
            return null
        }
        val previousModel = (previousRendering as? CompilerRenderingSuccess)?.model ?: return null
        return renderModelDeriver.deriveIncremental(
            document = document,
            previousModel = previousModel,
            affectedScope = affectedScope,
        )
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
