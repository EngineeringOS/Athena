package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.boundary.AthenaBoundaryDescriptorResolver
import com.engineeringood.athena.compiler.boundary.AthenaBoundaryDescriptorSource
import com.engineeringood.athena.compiler.knowledge.AthenaKnowledgePackageSource
import com.engineeringood.athena.compiler.knowledge.AthenaKnowledgeResolver
import com.engineeringood.athena.compiler.plugin.AthenaDomainSemanticsCoordinator
import com.engineeringood.athena.compiler.repository.AthenaRepositoryContractLoader
import com.engineeringood.athena.compiler.repository.AthenaRepositoryContractValidationResult
import com.engineeringood.athena.compiler.repository.AthenaRepositoryGraphResolutionResult
import com.engineeringood.athena.compiler.repository.AthenaRepositoryGraphResolver
import com.engineeringood.athena.compiler.repository.AthenaRepositoryLockMaterializationResult
import com.engineeringood.athena.compiler.repository.AthenaRepositoryLockMaterializer
import com.engineeringood.athena.compiler.repository.AthenaRepositoryLockValidationResult
import com.engineeringood.athena.compiler.repository.AthenaRepositoryReportPublicationResult
import com.engineeringood.athena.compiler.repository.AthenaRepositoryReportPublisher
import com.engineeringood.athena.compiler.repository.AthenaRepositoryResolutionInputBuilder
import com.engineeringood.athena.compiler.repository.AthenaRepositoryResolutionInputResult
import com.engineeringood.athena.geometry.GeometryDocument
import com.engineeringood.athena.language.AthenaLanguageParser
import com.engineeringood.athena.language.ParseFailure
import com.engineeringood.athena.language.ParseResult
import com.engineeringood.athena.language.ParseSuccess
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.layout.LayoutDocument
import com.engineeringood.athena.layout.ViewDefinition
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
import com.engineeringood.athena.plugin.AthenaDomainPlugin
import com.engineeringood.athena.plugin.AthenaCompilerContributionStage
import com.engineeringood.athena.plugin.AthenaExtensionPoint
import com.engineeringood.athena.plugin.AthenaRenderContributor
import com.engineeringood.athena.plugin.AthenaPluginValidationContext
import com.engineeringood.athena.plugin.AthenaSemanticEnrichmentContext
import com.engineeringood.athena.plugin.AthenaSourceDocument
import com.engineeringood.athena.plugin.AthenaViewDefinitionContributor
import com.engineeringood.athena.plugin.host.AthenaApprovedPluginInventory
import com.engineeringood.athena.plugin.host.AthenaPluginDiscovery
import com.engineeringood.athena.plugin.host.AthenaPluginDiscoveryReport
import java.nio.file.Files
import java.nio.file.Path

/** Compiler entry point that orchestrates parsing and semantic lowering for the current pipeline. */
class AthenaCompiler(
    private val parser: AthenaLanguageParser = AthenaLanguageParser(),
    private val validator: EngineeringIrValidator = EngineeringIrValidator(),
    private val geometryIrDeriver: GeometryIrDeriver = GeometryIrDeriver(),
    private val layoutIrDeriver: LayoutIrDeriver = LayoutIrDeriver(),
    private val renderModelDeriver: SvgRenderModelDeriver = SvgRenderModelDeriver(),
    private val svgRenderer: SvgRenderer = SvgRenderer(),
    private val pluginDiscovery: AthenaPluginDiscovery = AthenaPluginDiscovery(),
    hostedPluginDiscoveryReport: AthenaPluginDiscoveryReport? = null,
    hostedDomainPlugins: List<AthenaDomainPlugin>? = null,
    private val knowledgePackageSource: AthenaKnowledgePackageSource = AthenaKnowledgePackageSource.empty(),
    private val knowledgeResolver: AthenaKnowledgeResolver = AthenaKnowledgeResolver(),
    private val boundaryDescriptorSource: AthenaBoundaryDescriptorSource = AthenaBoundaryDescriptorSource.empty(),
    private val boundaryDescriptorResolver: AthenaBoundaryDescriptorResolver = AthenaBoundaryDescriptorResolver(),
    private val repositoryContractLoader: AthenaRepositoryContractLoader = AthenaRepositoryContractLoader(),
    private val repositoryResolutionInputBuilder: AthenaRepositoryResolutionInputBuilder = AthenaRepositoryResolutionInputBuilder(),
    private val repositoryGraphResolver: AthenaRepositoryGraphResolver = AthenaRepositoryGraphResolver(
        contractLoader = repositoryContractLoader,
        resolutionInputBuilder = repositoryResolutionInputBuilder,
    ),
    private val repositoryLockMaterializer: AthenaRepositoryLockMaterializer = AthenaRepositoryLockMaterializer(
        graphResolver = repositoryGraphResolver,
    ),
    lowerer: EngineeringIrLowerer? = null,
) {
    /** Deterministic discovery report built before any compilation pass uses plugin inventory. */
    val pluginDiscoveryReport: AthenaPluginDiscoveryReport = hostedPluginDiscoveryReport ?: pluginDiscovery.discover()

    /** Approved plugin inventory attached at core-owned extension points for this compiler instance. */
    val pluginInventory: AthenaApprovedPluginInventory = pluginDiscoveryReport.approvedInventory

    private val domainSemanticsCoordinator: AthenaDomainSemanticsCoordinator =
        hostedDomainPlugins?.let { domainPlugins ->
            AthenaDomainSemanticsCoordinator(activeDomainPlugins = domainPlugins)
        } ?: AthenaDomainSemanticsCoordinator(pluginInventory)
    private val repositoryReportPublisher = AthenaRepositoryReportPublisher(
        lockMaterializer = repositoryLockMaterializer,
    )
    private val lowerer: EngineeringIrLowerer = lowerer ?: EngineeringIrLowerer(domainSemanticsCoordinator)
    private val supportedViewDefinitionsCache: List<ViewDefinition> = pluginInventory
        .attachedPlugins(AthenaExtensionPoint.VIEW_DEFINITIONS)
        .flatMap { approvedPlugin ->
            (approvedPlugin.candidate.plugin as? AthenaViewDefinitionContributor)?.viewDefinitions().orEmpty()
        }
    private val supportedRenderContributionsCache: List<CompilerRenderContributionAttribution> = pluginInventory.approvedPlugins
        .flatMap { approvedPlugin ->
            val pluginId = approvedPlugin.candidate.manifest.pluginId
            (approvedPlugin.candidate.plugin as? AthenaRenderContributor)
                ?.renderContributions
                .orEmpty()
                .map { contribution ->
                    CompilerRenderContributionAttribution(
                        pluginId = pluginId,
                        contributionId = contribution.contributionId,
                        viewIds = contribution.viewIds,
                        rendererTargets = contribution.rendererTargets,
                    )
                }
        }

    /** Parses the authored source file at [path] and returns the full syntax-owned document. */
    fun parse(path: Path): CompilerParseResult {
        return parseSource(path)
    }

    /**
     * Parses in-memory authored source text through the same syntax boundary used for file-backed compilation.
     */
    fun parse(path: Path, sourceText: String): CompilerParseResult {
        return parseSource(path.toString(), sourceText)
    }

    /**
     * Parses, lowers, and validates in-memory authored source text through the same JVM compiler stack.
     */
    fun compile(path: Path, sourceText: String): CompilerCompilationResult {
        val knowledgeContext = knowledgeResolver.resolve(knowledgePackageSource)
        val boundaryValidation = boundaryDescriptorResolver.resolve(boundaryDescriptorSource)
        return compileParsedSource(
            parseResult = parseSource(path.toString(), sourceText),
            knowledgeContext = knowledgeContext,
            boundaryValidation = boundaryValidation,
        )
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

    /** Returns supported view definitions in deterministic approved-plugin order. */
    fun supportedViewDefinitions(): List<ViewDefinition> = supportedViewDefinitionsCache

    /** Returns declared render contributions in deterministic approved-plugin order. */
    fun supportedRenderContributions(): List<CompilerRenderContributionAttribution> = supportedRenderContributionsCache

    /**
     * Loads and validates the governed repository-root contract through the compiler-owned semantic path.
     */
    fun validateRepositoryContract(repositoryRoot: Path): AthenaRepositoryContractValidationResult {
        return repositoryContractLoader.load(repositoryRoot)
    }

    /**
     * Loads the governed repository contract and derives deterministic package-resolution input from it.
     */
    fun buildRepositoryResolutionInput(repositoryRoot: Path): AthenaRepositoryResolutionInputResult {
        return repositoryResolutionInputBuilder.build(
            validateRepositoryContract(repositoryRoot),
        )
    }

    /**
     * Resolves the governed repository into the first canonical local-first package graph.
     */
    fun resolveRepositoryGraph(repositoryRoot: Path): AthenaRepositoryGraphResolutionResult {
        return repositoryGraphResolver.resolve(repositoryRoot)
    }

    /**
     * Resolves the governed repository graph and writes the canonical `athena.lock` derived state.
     */
    fun materializeRepositoryLock(repositoryRoot: Path): AthenaRepositoryLockMaterializationResult {
        return repositoryLockMaterializer.materialize(repositoryRoot)
    }

    /**
     * Resolves the governed repository graph and validates the current `athena.lock` against canonical output.
     */
    fun validateRepositoryLock(repositoryRoot: Path): AthenaRepositoryLockValidationResult {
        return repositoryLockMaterializer.validate(repositoryRoot)
    }

    /**
     * Publishes the canonical repository graph report that downstream runtime and IDE layers should consume.
     */
    fun publishRepositoryGraphReport(repositoryRoot: Path): AthenaRepositoryReportPublicationResult {
        return repositoryReportPublisher.publish(repositoryRoot)
    }

    /** Derives all supported layouts from the supplied canonical [document]. */
    fun deriveSupportedLayouts(document: EngineeringDocument): List<LayoutDocument> {
        return supportedViewDefinitionsCache.map { viewDefinition ->
            layoutIrDeriver.derive(document, viewDefinition)
        }
    }

    /** Derives geometry for all supported layouts in deterministic view order. */
    fun deriveSupportedGeometries(layouts: List<LayoutDocument>): List<GeometryDocument> {
        return layouts.map { layoutDocument ->
            geometryIrDeriver.derive(layoutDocument)
        }
    }

    /** Derives one supported layout from the supplied canonical [document] and [viewId]. */
    fun deriveLayout(
        document: EngineeringDocument,
        viewId: String,
    ): LayoutDocument {
        val viewDefinition = supportedViewDefinitionsCache.firstOrNull { definition -> definition.id == viewId }
            ?: error("Unsupported view definition `$viewId` for layout derivation.")
        return layoutIrDeriver.derive(document, viewDefinition)
    }

    /** Derives one geometry document from the supplied [layoutDocument]. */
    fun deriveGeometry(layoutDocument: LayoutDocument): GeometryDocument {
        return geometryIrDeriver.derive(layoutDocument)
    }

    /** Parses, lowers, and semantically validates the authored source file at [path]. */
    fun compile(path: Path): CompilerCompilationResult {
        val knowledgeContext = knowledgeResolver.resolve(knowledgePackageSource)
        val boundaryValidation = boundaryDescriptorResolver.resolve(boundaryDescriptorSource)

        return compileParsedSource(
            parseResult = parseSource(path),
            knowledgeContext = knowledgeContext,
            boundaryValidation = boundaryValidation,
        )
    }

    private fun compileParsedSource(
        parseResult: CompilerParseResult,
        knowledgeContext: com.engineeringood.athena.compiler.knowledge.AthenaCompilationKnowledgeContext,
        boundaryValidation: com.engineeringood.athena.compiler.boundary.AthenaBoundaryValidationReport,
    ): CompilerCompilationResult {
        return when (parseResult) {
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
                        skippedPassRecord(SEMANTIC_ENRICHMENT_PASS, "parse failed"),
                        skippedPassRecord(VALIDATE_PASS, "parse failed"),
                        skippedPassRecord(BACKEND_PREPARATION_PASS, "parse failed"),
                        skippedPassRecord(BACKEND_EMISSION_PASS, "parse failed"),
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
        document: EngineeringDocument,
        affectedScope: CompilerAffectedScope,
        previousLayouts: List<LayoutDocument>,
        previousGeometries: List<GeometryDocument>,
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
            previousLayouts = previousLayouts,
            previousGeometries = previousGeometries,
            previousRendering = previousRendering,
        )
    }

    private fun buildCompilationSuccess(
        source: CompilerSourceDocument,
        document: EngineeringDocument,
        knowledgeContext: com.engineeringood.athena.compiler.knowledge.AthenaCompilationKnowledgeContext,
        boundaryValidation: com.engineeringood.athena.compiler.boundary.AthenaBoundaryValidationReport,
        parseRecord: CompilerPassRecord,
        lowerRecord: CompilerPassRecord,
        affectedScope: CompilerAffectedScope? = null,
        previousLayouts: List<LayoutDocument>? = null,
        previousGeometries: List<GeometryDocument>? = null,
        previousRendering: CompilerRenderingResult? = null,
    ): CompilerCompilationSuccess {
        val semanticEnrichmentResult = semanticEnrichmentPass(
            source = source,
            document = document,
        )
        val validationResult = validatePass(
            source = source,
            document = document,
            affectedScope = affectedScope,
            semanticEnrichment = semanticEnrichmentResult,
        )
        val backendPreparation = prepareBackend(
            result = validationResult.semanticResult,
            document = document,
            affectedScope = affectedScope,
            previousLayouts = previousLayouts,
            previousGeometries = previousGeometries,
        )
        val backendEmission = emitBackend(
            result = validationResult.semanticResult,
            document = document,
            preparedGeometry = backendPreparation.preparedGeometry,
            affectedScope = affectedScope,
            layoutMode = backendPreparation.layoutMode,
            geometryMode = backendPreparation.geometryMode,
            previousRendering = previousRendering,
            blockedReason = backendPreparation.blockedReason,
            blockedByPass = backendPreparation.blockedByPass,
        )
        val knowledgeAttributions = buildKnowledgeAttributions(knowledgeContext)
        return CompilerCompilationSuccess(
            source = source,
            document = document,
            semanticResult = validationResult.semanticResult,
            validationBreakdown = validationResult.validationBreakdown,
            layouts = backendPreparation.layouts,
            geometries = backendPreparation.geometries,
            rendering = backendEmission.rendering,
            knowledgeContext = knowledgeContext,
            boundaryValidation = boundaryValidation,
            knowledgeAttributions = knowledgeAttributions,
            pipeline = CompilerPipelineReport(
                passes = listOf(
                    parseRecord,
                    lowerRecord,
                    semanticEnrichmentResult.passRecord,
                    validationResult.passRecord,
                    backendPreparation.passRecord,
                    backendEmission.passRecord,
                ),
            ),
            incrementalUpdateReport = affectedScope?.let { scope ->
                CompilerIncrementalUpdateReport(
                    affectedScope = scope,
                    validationMode = validationResult.validationMode,
                    layoutMode = backendPreparation.layoutMode,
                    layoutScopedViewIds = backendPreparation.layoutScopedViewIds,
                    geometryMode = backendPreparation.geometryMode,
                    geometryScopedViewIds = backendPreparation.geometryScopedViewIds,
                    renderingMode = backendEmission.mode,
                    renderingViewIds = backendEmission.viewIds,
                )
            },
        )
    }

    private fun deriveLayouts(
        document: EngineeringDocument,
        affectedScope: CompilerAffectedScope?,
        previousLayouts: List<LayoutDocument>?,
    ): LayoutDerivationResult {
        if (affectedScope == null || previousLayouts == null) {
            return LayoutDerivationResult(
                layouts = deriveSupportedLayouts(document),
                mode = CompilerIncrementalPassMode.FULL_FALLBACK,
                scopedViewIds = emptyList(),
            )
        }

        val previousLayoutsByViewId = previousLayouts.associateBy { layout -> layout.view.id }
        val scopedLayouts = supportedViewDefinitionsCache.map { viewDefinition ->
            val previousLayout = previousLayoutsByViewId[viewDefinition.id] ?: return fallbackLayoutDerivation(document)
            layoutIrDeriver.deriveIncremental(
                document = document,
                view = viewDefinition,
                previousLayout = previousLayout,
                affectedScope = affectedScope,
            ) ?: return fallbackLayoutDerivation(document)
        }

        return LayoutDerivationResult(
            layouts = scopedLayouts,
            mode = CompilerIncrementalPassMode.SCOPED,
            scopedViewIds = scopedLayouts.map { layout -> layout.view.id },
        )
    }

    private fun fallbackLayoutDerivation(document: EngineeringDocument): LayoutDerivationResult {
        return LayoutDerivationResult(
            layouts = deriveSupportedLayouts(document),
            mode = CompilerIncrementalPassMode.FULL_FALLBACK,
            scopedViewIds = emptyList(),
        )
    }

    private fun deriveGeometries(
        layouts: List<LayoutDocument>,
        affectedScope: CompilerAffectedScope?,
        previousGeometries: List<GeometryDocument>?,
    ): GeometryDerivationResult {
        if (affectedScope == null || previousGeometries == null) {
            return GeometryDerivationResult(
                geometries = deriveSupportedGeometries(layouts),
                mode = CompilerIncrementalPassMode.FULL_FALLBACK,
                scopedViewIds = emptyList(),
            )
        }

        val previousGeometriesByViewId = previousGeometries.associateBy { geometry -> geometry.viewId }
        val scopedGeometries = layouts.map { layout ->
            val previousGeometry =
                previousGeometriesByViewId[layout.view.id] ?: return fallbackGeometryDerivation(layouts)
            geometryIrDeriver.deriveIncremental(
                layoutDocument = layout,
                previousGeometry = previousGeometry,
                affectedScope = affectedScope,
            ) ?: return fallbackGeometryDerivation(layouts)
        }

        return GeometryDerivationResult(
            geometries = scopedGeometries,
            mode = CompilerIncrementalPassMode.SCOPED,
            scopedViewIds = scopedGeometries.map { geometry -> geometry.viewId },
        )
    }

    private fun fallbackGeometryDerivation(layouts: List<LayoutDocument>): GeometryDerivationResult {
        return GeometryDerivationResult(
            geometries = deriveSupportedGeometries(layouts),
            mode = CompilerIncrementalPassMode.FULL_FALLBACK,
            scopedViewIds = emptyList(),
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
                        endLine = 0,
                        endColumn = 1,
                        message = "Could not read source file: ${exception.message ?: exception::class.simpleName}",
                    ),
                ),
            )
        }

        return parseSource(path.toString(), sourceText)
    }

    private fun parseSource(file: String, sourceText: String): CompilerParseResult {
        return when (val result: ParseResult = parser.parse(file, sourceText)) {
            is ParseSuccess -> CompilerParseSuccess(
                CompilerSourceDocument(
                    file = file,
                    ast = result.ast,
                ),
            )

            is ParseFailure -> CompilerParseFailure(
                result.diagnostics.map { diagnostic ->
                    CompilerSyntaxDiagnostic(
                        file = diagnostic.file,
                        line = diagnostic.line,
                        column = diagnostic.column,
                        endLine = diagnostic.span.end.line,
                        endColumn = diagnostic.span.end.column,
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
        result: SemanticValidationResult,
        validationBreakdown: CompilerValidationBreakdown,
        affectedScope: CompilerAffectedScope?,
        mode: CompilerIncrementalPassMode,
    ): String {
        val baseSummary = if (result.isSemanticallyValid) "semantic-valid" else "semantic-invalid"
        val boundarySummary =
            "kernel=${validationBreakdown.kernelDiagnostics.size}, " +
                "domain=${validationBreakdown.domainDiagnostics.size}, " +
                "enrichment=${validationBreakdown.semanticEnrichmentDiagnostics.size}"
        return if (affectedScope == null) {
            "$baseSummary ($boundarySummary)"
        } else {
            "$baseSummary ($boundarySummary, ${mode.name.lowercase()} ${affectedScope.validationSemanticIds.size} ids)"
        }
    }

    private fun semanticEnrichmentPass(
        source: CompilerSourceDocument,
        document: EngineeringDocument,
    ): SemanticEnrichmentPassResult {
        val declaredSemanticEnrichers = domainSemanticsCoordinator.declaredContributionIds(
            AthenaCompilerContributionStage.SEMANTIC_ENRICHMENT,
        )
        if (declaredSemanticEnrichers.isEmpty()) {
            return SemanticEnrichmentPassResult(
                contribution = com.engineeringood.athena.plugin.AthenaDomainSemanticEnrichmentContribution.EMPTY,
                passRecord = CompilerPassRecord(
                    pass = SEMANTIC_ENRICHMENT_PASS,
                    status = CompilerPassExecutionStatus.SUCCEEDED,
                    outputSummary = "no semantic enrichers",
                ),
            )
        }

        val contribution = domainSemanticsCoordinator.enrichSemantics(
            document = document,
            context = AthenaSemanticEnrichmentContext(
                document = document,
                source = source.toAthenaSourceDocument(),
                approvedPluginIds = domainSemanticsCoordinator.activePluginIds,
            ),
        )
        val detailParts = buildList {
            if (contribution.notes.isNotEmpty()) {
                add("notes=${contribution.notes.joinToString(";") { note -> note.message }}")
            }
            if (contribution.diagnostics.isNotEmpty()) {
                add("diagnostics=${contribution.diagnostics.size}")
            }
        }
        val summary = buildString {
            append("semantic-enriched by: ${declaredSemanticEnrichers.joinToString(",")}")
            if (detailParts.isNotEmpty()) {
                append(" (")
                append(detailParts.joinToString(", "))
                append(")")
            }
        }
        return SemanticEnrichmentPassResult(
            contribution = contribution,
            passRecord = CompilerPassRecord(
                pass = SEMANTIC_ENRICHMENT_PASS,
                status = CompilerPassExecutionStatus.SUCCEEDED,
                outputSummary = summary,
            ),
        )
    }

    private fun validatePass(
        source: CompilerSourceDocument,
        document: EngineeringDocument,
        affectedScope: CompilerAffectedScope?,
        semanticEnrichment: SemanticEnrichmentPassResult,
    ): ValidationPassResult {
        val validation = validateSemantics(
            source = source,
            document = document,
            affectedScope = affectedScope,
            enrichmentDiagnostics = semanticEnrichment.contribution.diagnostics,
        )
        return ValidationPassResult(
            semanticResult = validation.semanticResult,
            validationBreakdown = validation.validationBreakdown,
            validationMode = validation.validationMode,
            passRecord = CompilerPassRecord(
                pass = VALIDATE_PASS,
                status = CompilerPassExecutionStatus.SUCCEEDED,
                outputSummary = semanticSummary(
                    result = validation.semanticResult,
                    validationBreakdown = validation.validationBreakdown,
                    affectedScope = affectedScope,
                    mode = validation.validationMode,
                ),
            ),
        )
    }

    private fun prepareBackend(
        result: SemanticValidationResult,
        document: EngineeringDocument,
        affectedScope: CompilerAffectedScope?,
        previousLayouts: List<LayoutDocument>?,
        previousGeometries: List<GeometryDocument>?,
    ): BackendPreparationPassResult {
        if (result.continuationDecision != SemanticContinuationDecision.CONTINUE) {
            return BackendPreparationPassResult(
                layouts = emptyList(),
                geometries = emptyList(),
                preparedGeometry = null,
                layoutMode = CompilerIncrementalPassMode.FULL_FALLBACK,
                layoutScopedViewIds = emptyList(),
                geometryMode = CompilerIncrementalPassMode.FULL_FALLBACK,
                geometryScopedViewIds = emptyList(),
                blockedReason = "semantic validation requested ${result.continuationDecision}",
                blockedByPass = CompilerPassId.VALIDATE,
                passRecord = CompilerPassRecord(
                    pass = BACKEND_PREPARATION_PASS,
                    status = CompilerPassExecutionStatus.SKIPPED,
                    outputSummary = "backend-preparation-skipped (validation stopped downstream)",
                ),
            )
        }

        val layoutResult = deriveLayouts(
            document = document,
            affectedScope = affectedScope,
            previousLayouts = previousLayouts,
        )
        val geometryResult = if (layoutResult.layouts.isNotEmpty()) {
            deriveGeometries(
                layouts = layoutResult.layouts,
                affectedScope = affectedScope,
                previousGeometries = previousGeometries,
            )
        } else {
            GeometryDerivationResult(
                geometries = emptyList(),
                mode = CompilerIncrementalPassMode.FULL_FALLBACK,
                scopedViewIds = emptyList(),
            )
        }
        val renderingGeometry = selectRenderingGeometry(geometryResult.geometries)
        if (renderingGeometry == null) {
            return BackendPreparationPassResult(
                layouts = layoutResult.layouts,
                geometries = geometryResult.geometries,
                preparedGeometry = null,
                layoutMode = layoutResult.mode,
                layoutScopedViewIds = layoutResult.scopedViewIds,
                geometryMode = geometryResult.mode,
                geometryScopedViewIds = geometryResult.scopedViewIds,
                blockedReason = "no supported geometry-backed backend input was derived",
                blockedByPass = CompilerPassId.BACKEND_PREPARATION,
                passRecord = CompilerPassRecord(
                    pass = BACKEND_PREPARATION_PASS,
                    status = CompilerPassExecutionStatus.FAILED,
                    outputSummary = "backend-input-missing (no geometry)",
                ),
            )
        }

        return BackendPreparationPassResult(
            layouts = layoutResult.layouts,
            geometries = geometryResult.geometries,
            preparedGeometry = renderingGeometry,
            layoutMode = layoutResult.mode,
            layoutScopedViewIds = layoutResult.scopedViewIds,
            geometryMode = geometryResult.mode,
            geometryScopedViewIds = geometryResult.scopedViewIds,
            blockedReason = null,
            blockedByPass = null,
            passRecord = CompilerPassRecord(
                pass = BACKEND_PREPARATION_PASS,
                status = CompilerPassExecutionStatus.SUCCEEDED,
                outputSummary = if (affectedScope == null) {
                    "geometry-prepared"
                } else {
                    "geometry-prepared (layout=${layoutResult.mode.name.lowercase()} geometry=${geometryResult.mode.name.lowercase()})"
                },
            ),
        )
    }

    private fun emitBackend(
        result: SemanticValidationResult,
        document: EngineeringDocument,
        preparedGeometry: GeometryDocument?,
        affectedScope: CompilerAffectedScope?,
        layoutMode: CompilerIncrementalPassMode,
        geometryMode: CompilerIncrementalPassMode,
        previousRendering: CompilerRenderingResult?,
        blockedReason: String?,
        blockedByPass: CompilerPassId?,
    ): BackendEmissionPassResult {
        if (preparedGeometry == null || blockedReason != null || blockedByPass != null) {
            return BackendEmissionPassResult(
                rendering = CompilerRenderingBlocked(
                    reason = blockedReason ?: "backend emission was blocked before execution",
                    blockedByPass = blockedByPass ?: CompilerPassId.BACKEND_PREPARATION,
                ),
                mode = CompilerIncrementalPassMode.FULL_FALLBACK,
                viewIds = emptyList(),
                passRecord = CompilerPassRecord(
                    pass = BACKEND_EMISSION_PASS,
                    status = CompilerPassExecutionStatus.SKIPPED,
                    outputSummary = if (result.continuationDecision == SemanticContinuationDecision.CONTINUE) {
                        "backend-emission-skipped (backend preparation failed)"
                    } else {
                        "backend-emission-skipped (validation stopped downstream)"
                    },
                ),
            )
        }

        val incrementalRenderModel = incrementalRenderModel(
            systemName = document.system.name,
            geometry = preparedGeometry,
            affectedScope = affectedScope,
            previousRendering = previousRendering,
        )
        val activeRenderContributions = activeRenderContributions(
            viewId = preparedGeometry.viewId,
            rendererTarget = SVG_RENDERER_TARGET,
        )
        val renderingMode = if (incrementalRenderModel != null) {
            CompilerIncrementalPassMode.SCOPED
        } else {
            CompilerIncrementalPassMode.FULL_FALLBACK
        }
        val renderModel = incrementalRenderModel ?: renderModelDeriver.derive(
            systemName = document.system.name,
            geometry = preparedGeometry,
        )
        val svg = svgRenderer.render(
            systemName = document.system.name,
            geometry = preparedGeometry,
        )
        return BackendEmissionPassResult(
            rendering = CompilerRenderingSuccess(
                model = renderModel,
                svg = svg,
                viewId = preparedGeometry.viewId,
                rendererTarget = SVG_RENDERER_TARGET,
                activeRenderContributions = activeRenderContributions,
            ),
            mode = renderingMode,
            viewIds = if (renderingMode == CompilerIncrementalPassMode.SCOPED) {
                listOf(preparedGeometry.viewId)
            } else {
                emptyList()
            },
            passRecord = CompilerPassRecord(
                pass = BACKEND_EMISSION_PASS,
                status = CompilerPassExecutionStatus.SUCCEEDED,
                outputSummary = if (affectedScope == null) {
                    "svg-emitted"
                } else {
                    "svg-emitted (layout=${layoutMode.name.lowercase()} geometry=${geometryMode.name.lowercase()} rendering=${renderingMode.name.lowercase()})"
                },
            ),
        )
    }

    private fun validateSemantics(
        source: CompilerSourceDocument,
        document: EngineeringDocument,
        affectedScope: CompilerAffectedScope?,
        enrichmentDiagnostics: List<SemanticDiagnostic>,
    ): ValidationComputationResult {
        val validationMode = if (affectedScope == null) {
            CompilerIncrementalPassMode.FULL_FALLBACK
        } else {
            CompilerIncrementalPassMode.SCOPED
        }
        val kernelResult = if (affectedScope == null) {
            validator.validate(document)
        } else {
            validator.validate(
                document = document,
                scope = EngineeringIrValidationScope(affectedScope.validationSemanticIds.toSet()),
            )
        }
        val domainValidationContribution = domainSemanticsCoordinator.validate(
            document = document,
            context = AthenaPluginValidationContext(
                document = document,
                source = source.toAthenaSourceDocument(),
                approvedPluginIds = domainSemanticsCoordinator.activePluginIds,
            ),
        )
        val domainDiagnostics = buildList {
            addAll(domainSemanticsUnavailableDiagnostics(source, document))
            addAll(domainValidationContribution.diagnostics)
        }
        val validationBreakdown = CompilerValidationBreakdown(
            semanticEnrichmentDiagnostics = enrichmentDiagnostics,
            kernelDiagnostics = kernelResult.diagnostics,
            domainDiagnostics = domainDiagnostics,
            domainValidationAttributions = domainValidationContribution.attributions,
        )
        val diagnostics = validationBreakdown.semanticEnrichmentDiagnostics +
            validationBreakdown.kernelDiagnostics +
            validationBreakdown.domainDiagnostics
        val semanticResult = SemanticValidationResult(
            diagnostics = diagnostics,
            continuationDecision = if (diagnostics.any { it.severity == SemanticDiagnosticSeverity.ERROR }) {
                SemanticContinuationDecision.STOP_DOWNSTREAM
            } else {
                SemanticContinuationDecision.CONTINUE
            },
        )
        return ValidationComputationResult(
            semanticResult = semanticResult,
            validationBreakdown = validationBreakdown,
            validationMode = validationMode,
        )
    }

    private fun domainSemanticsUnavailableDiagnostics(
        source: CompilerSourceDocument,
        document: EngineeringDocument,
    ): List<SemanticDiagnostic> {
        if (source.ast.declarations.isEmpty()) {
            return emptyList()
        }

        if (!domainSemanticsCoordinator.hasParticipants(AthenaCompilerContributionStage.LOWER)) {
            return listOf(domainSemanticsUnavailableDiagnostic(document))
        }

        if (document.components.isEmpty() && document.ports.isEmpty() && document.connections.isEmpty()) {
            return listOf(domainSemanticsUnavailableDiagnostic(document))
        }

        return emptyList()
    }

    private fun domainSemanticsUnavailableDiagnostic(document: EngineeringDocument): SemanticDiagnostic {
        return SemanticDiagnostic(
            severity = SemanticDiagnosticSeverity.ERROR,
            ruleId = SemanticRuleId("domain.semantics.unavailable"),
            category = SemanticDiagnosticCategory.DOMAIN,
            subjectIdentity = document.system.id,
            provenance = document.system.provenance,
            message = "No approved domain plugin claimed the authored domain semantics in `${document.system.name}`.",
        )
    }

    private fun incrementalRenderModel(
        systemName: String,
        geometry: GeometryDocument,
        affectedScope: CompilerAffectedScope?,
        previousRendering: CompilerRenderingResult?,
    ): SvgRenderModel? {
        if (affectedScope == null) {
            return null
        }
        val previousModel = (previousRendering as? CompilerRenderingSuccess)?.model ?: return null
        return renderModelDeriver.deriveIncremental(
            systemName = systemName,
            geometry = geometry,
            previousModel = previousModel,
            affectedScope = affectedScope,
        )
    }

    private fun selectRenderingGeometry(geometries: List<GeometryDocument>): GeometryDocument? {
        return geometries.firstOrNull { geometry -> geometry.viewId == DEFAULT_RENDER_VIEW_ID }
            ?: geometries.firstOrNull()
    }

    private fun activeRenderContributions(
        viewId: String,
        rendererTarget: String,
    ): List<CompilerRenderContributionAttribution> {
        return supportedRenderContributionsCache.filter { contribution ->
            val supportsView = contribution.viewIds.isEmpty() || viewId in contribution.viewIds
            val supportsTarget = contribution.rendererTargets.isEmpty() || rendererTarget in contribution.rendererTargets
            supportsView && supportsTarget
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

private fun CompilerSourceDocument.toAthenaSourceDocument(): AthenaSourceDocument {
    return AthenaSourceDocument(
        file = file,
        ast = ast,
    )
}

private data class LayoutDerivationResult(
    val layouts: List<LayoutDocument>,
    val mode: CompilerIncrementalPassMode,
    val scopedViewIds: List<String>,
)

private data class GeometryDerivationResult(
    val geometries: List<GeometryDocument>,
    val mode: CompilerIncrementalPassMode,
    val scopedViewIds: List<String>,
)

private data class ValidationPassResult(
    val semanticResult: SemanticValidationResult,
    val validationBreakdown: CompilerValidationBreakdown,
    val validationMode: CompilerIncrementalPassMode,
    val passRecord: CompilerPassRecord,
)

private data class ValidationComputationResult(
    val semanticResult: SemanticValidationResult,
    val validationBreakdown: CompilerValidationBreakdown,
    val validationMode: CompilerIncrementalPassMode,
)

private data class SemanticEnrichmentPassResult(
    val contribution: com.engineeringood.athena.plugin.AthenaDomainSemanticEnrichmentContribution,
    val passRecord: CompilerPassRecord,
)

private data class BackendPreparationPassResult(
    val layouts: List<LayoutDocument>,
    val geometries: List<GeometryDocument>,
    val preparedGeometry: GeometryDocument?,
    val layoutMode: CompilerIncrementalPassMode,
    val layoutScopedViewIds: List<String>,
    val geometryMode: CompilerIncrementalPassMode,
    val geometryScopedViewIds: List<String>,
    val blockedReason: String?,
    val blockedByPass: CompilerPassId?,
    val passRecord: CompilerPassRecord,
)

private data class BackendEmissionPassResult(
    val rendering: CompilerRenderingResult,
    val mode: CompilerIncrementalPassMode,
    val viewIds: List<String>,
    val passRecord: CompilerPassRecord,
)

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

private val SEMANTIC_ENRICHMENT_PASS = CompilerPassDescriptor(
    id = CompilerPassId.SEMANTIC_ENRICHMENT,
    responsibility = "Coordinate governed semantic enrichment participation over canonical Engineering IR",
    inputState = "canonical Engineering IR",
    outputState = "semantic enrichment coordination result",
)

private val VALIDATE_PASS = CompilerPassDescriptor(
    id = CompilerPassId.VALIDATE,
    responsibility = "Validate canonical Engineering IR and compute continuation policy",
    inputState = "canonical Engineering IR plus semantic enrichment context",
    outputState = "semantic validation result",
)

private val BACKEND_PREPARATION_PASS = CompilerPassDescriptor(
    id = CompilerPassId.BACKEND_PREPARATION,
    responsibility = "Prepare downstream backend input from validated canonical semantics and supported projections",
    inputState = "semantic validation result plus canonical Engineering IR",
    outputState = "geometry-backed backend input or block reason",
)

private val BACKEND_EMISSION_PASS = CompilerPassDescriptor(
    id = CompilerPassId.BACKEND_EMISSION,
    responsibility = "Emit downstream backend output from prepared backend input",
    inputState = "prepared backend input",
    outputState = "backend emission result",
)

private const val DEFAULT_RENDER_VIEW_ID = "cabinet"
private const val SVG_RENDERER_TARGET = "svg"
