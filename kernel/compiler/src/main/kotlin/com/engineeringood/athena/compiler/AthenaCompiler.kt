package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.boundary.AthenaBoundaryDescriptorResolver
import com.engineeringood.athena.compiler.boundary.AthenaBoundaryDescriptorSource
import com.engineeringood.athena.compiler.knowledge.AthenaComponentKnowledgeContextBuilder
import com.engineeringood.athena.compiler.knowledge.AthenaComponentKnowledgeContributionSource
import com.engineeringood.athena.compiler.knowledge.AthenaKnowledgePackageSource
import com.engineeringood.athena.compiler.knowledge.AthenaKnowledgeResolver
import com.engineeringood.athena.compiler.plugin.AthenaDomainSemanticsCoordinator
import com.engineeringood.athena.compiler.repository.AthenaRepositoryContractLoader
import com.engineeringood.athena.compiler.repository.AthenaRepositoryContractLoadOptions
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
import com.engineeringood.athena.compiler.semantic.GovernedProjectSemanticGraphBuilder
import com.engineeringood.athena.compiler.semantic.ProjectSemanticDeclarationIndexer
import com.engineeringood.athena.compiler.semantic.ProjectSemanticDiagnosticProjector
import com.engineeringood.athena.compiler.semantic.ProjectSemanticGraphBuildResult
import com.engineeringood.athena.compiler.semantic.ProjectSemanticGraphSnapshot
import com.engineeringood.athena.compiler.semantic.ProjectSemanticImportResolver
import com.engineeringood.athena.compiler.semantic.ProjectSemanticSourceInput
import com.engineeringood.athena.geometry.GeometryDocument
import com.engineeringood.athena.language.AthenaLanguageParser
import com.engineeringood.athena.language.ParseFailure
import com.engineeringood.athena.language.ParseResult
import com.engineeringood.athena.language.ParseSuccess
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.layout.LayoutDocument
import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.renderer.svg.SvgRenderer
import com.engineeringood.athena.presentation.PresentationCompositePack
import com.engineeringood.athena.presentation.PresentationPrimitivePack
import com.engineeringood.athena.presentation.PresentationDocument
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.semantics.core.EngineeringIrValidator
import com.engineeringood.athena.plugin.AthenaDomainPlugin
import com.engineeringood.athena.plugin.AthenaComponentKnowledgeContributor
import com.engineeringood.athena.plugin.AthenaExtensionPoint
import com.engineeringood.athena.plugin.AthenaPresentationPackContributor
import com.engineeringood.athena.plugin.AthenaRenderContributor
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
    private val projectionModelDeriver: ProjectionModelDeriver = ProjectionModelDeriver(),
    private val presentationModelDeriver: PresentationModelDeriver = PresentationModelDeriver(),
    private val renderModelDeriver: SvgRenderModelDeriver = SvgRenderModelDeriver(),
    private val svgRenderer: SvgRenderer = SvgRenderer(),
    private val pluginDiscovery: AthenaPluginDiscovery = AthenaPluginDiscovery(),
    hostedPluginDiscoveryReport: AthenaPluginDiscoveryReport? = null,
    hostedDomainPlugins: List<AthenaDomainPlugin>? = null,
    private val knowledgePackageSource: AthenaKnowledgePackageSource = AthenaKnowledgePackageSource.empty(),
    private val knowledgeResolver: AthenaKnowledgeResolver = AthenaKnowledgeResolver(),
    private val boundaryDescriptorSource: AthenaBoundaryDescriptorSource = AthenaBoundaryDescriptorSource.empty(),
    private val boundaryDescriptorResolver: AthenaBoundaryDescriptorResolver = AthenaBoundaryDescriptorResolver(),
    private val derivedEngineeringContextDeriver: DerivedEngineeringContextDeriver = DerivedEngineeringContextDeriver(),
    private val capabilityFactPromoter: EngineeringCapabilityFactPromoter = EngineeringCapabilityFactPromoter(),
    private val constraintEvaluator: EngineeringConstraintEvaluator = EngineeringConstraintEvaluator(),
    private val impactConsequenceCalculator: EngineeringImpactConsequenceCalculator = EngineeringImpactConsequenceCalculator(),
    private val repositoryContractLoader: AthenaRepositoryContractLoader = AthenaRepositoryContractLoader(),
    private val repositoryResolutionInputBuilder: AthenaRepositoryResolutionInputBuilder = AthenaRepositoryResolutionInputBuilder(),
    private val repositoryGraphResolver: AthenaRepositoryGraphResolver = AthenaRepositoryGraphResolver(
        contractLoader = repositoryContractLoader,
        resolutionInputBuilder = repositoryResolutionInputBuilder,
    ),
    private val repositoryLockMaterializer: AthenaRepositoryLockMaterializer = AthenaRepositoryLockMaterializer(
        graphResolver = repositoryGraphResolver,
    ),
    private val componentKnowledgeContextBuilder: AthenaComponentKnowledgeContextBuilder = AthenaComponentKnowledgeContextBuilder(),
    lowerer: EngineeringIrLowerer? = null,
    private val projectSemanticGraphBuilder: GovernedProjectSemanticGraphBuilder = GovernedProjectSemanticGraphBuilder(parser),
    private val projectSemanticImportResolver: ProjectSemanticImportResolver = ProjectSemanticImportResolver(),
    private val projectSemanticDiagnosticProjector: ProjectSemanticDiagnosticProjector = ProjectSemanticDiagnosticProjector(),
    private val projectSemanticDeclarationIndexer: ProjectSemanticDeclarationIndexer = ProjectSemanticDeclarationIndexer(),
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
                        displayName = contribution.displayName,
                        description = contribution.description,
                        viewIds = contribution.viewIds,
                        rendererTargets = contribution.rendererTargets,
                        surfaceMappings = contribution.surfaceMappings,
                    )
                }
        }
    private val supportedPrimitivePresentationPacksCache: List<PresentationPrimitivePack> = pluginInventory
        .attachedPlugins(AthenaExtensionPoint.PRESENTATION_PACKS)
        .flatMap { approvedPlugin ->
            (approvedPlugin.candidate.plugin as? AthenaPresentationPackContributor)
                ?.primitivePresentationPacks()
                .orEmpty()
        }
    private val supportedCompositePresentationPacksCache: List<PresentationCompositePack> = pluginInventory
        .attachedPlugins(AthenaExtensionPoint.PRESENTATION_PACKS)
        .flatMap { approvedPlugin ->
            (approvedPlugin.candidate.plugin as? AthenaPresentationPackContributor)
                ?.compositePresentationPacks()
                .orEmpty()
        }
    private val componentKnowledgeContributionsCache: List<AthenaComponentKnowledgeContributionSource> = pluginInventory.approvedPlugins
        .mapNotNull { approvedPlugin ->
            val contributor = approvedPlugin.candidate.plugin as? AthenaComponentKnowledgeContributor ?: return@mapNotNull null
            val contribution = contributor.componentKnowledge()
            if (
                contribution.engineeringConcepts.isEmpty() &&
                contribution.partImplementations.isEmpty() &&
                contribution.semanticPorts.isEmpty() &&
                contribution.physicalTraits.isEmpty()
            ) {
                null
            } else {
                AthenaComponentKnowledgeContributionSource(
                    artifactId = approvedPlugin.candidate.manifest.pluginId,
                    artifactVersion = approvedPlugin.candidate.manifest.pluginVersion,
                    contribution = contribution,
                )
            }
        }
    private val compilationSupport = AthenaCompilerCompilationSupport(
        lowerer = this.lowerer,
        validator = validator,
        layoutIrDeriver = layoutIrDeriver,
        geometryIrDeriver = geometryIrDeriver,
        projectionModelDeriver = projectionModelDeriver,
        presentationModelDeriver = presentationModelDeriver,
        renderModelDeriver = renderModelDeriver,
        svgRenderer = svgRenderer,
        componentKnowledgeContextBuilder = componentKnowledgeContextBuilder,
        componentKnowledgeContributionsCache = componentKnowledgeContributionsCache,
        derivedEngineeringContextDeriver = derivedEngineeringContextDeriver,
        capabilityFactPromoter = capabilityFactPromoter,
        constraintEvaluator = constraintEvaluator,
        domainSemanticsCoordinator = domainSemanticsCoordinator,
        supportedViewDefinitionsCache = supportedViewDefinitionsCache,
        supportedRenderContributionsCache = supportedRenderContributionsCache,
        supportedPrimitivePresentationPacksCache = supportedPrimitivePresentationPacksCache,
        supportedCompositePresentationPacksCache = supportedCompositePresentationPacksCache,
    )

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
                val diagnostics = compilationSupport.domainSemanticsUnavailableDiagnostics(parseResult.source, document)
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

    /**
     * Computes deterministic engineering impact consequences across two compiled canonical states.
     */
    fun calculateImpactConsequences(
        before: CompilerCompilationSuccess,
        after: CompilerCompilationSuccess,
    ): com.engineeringood.athena.ir.EngineeringImpactConsequences {
        return impactConsequenceCalculator.calculate(
            before = before,
            after = after,
        )
    }

    /** Returns supported view definitions in deterministic approved-plugin order. */
    fun supportedViewDefinitions(): List<ViewDefinition> = supportedViewDefinitionsCache

    /** Returns declared render contributions in deterministic approved-plugin order. */
    fun supportedRenderContributions(): List<CompilerRenderContributionAttribution> = supportedRenderContributionsCache

    /** Returns render contributions active for one downstream view and renderer target. */
    fun activeRenderContributions(
        viewId: String,
        rendererTarget: String,
    ): List<CompilerRenderContributionAttribution> {
        return compilationSupport.activeRenderContributions(viewId, rendererTarget)
    }

    /**
     * Loads and validates the governed repository-root contract through the compiler-owned semantic path.
     */
    fun validateRepositoryContract(
        repositoryRoot: Path,
        options: AthenaRepositoryContractLoadOptions = AthenaRepositoryContractLoadOptions(),
    ): AthenaRepositoryContractValidationResult {
        return repositoryContractLoader.load(repositoryRoot, options)
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

    /** Builds one compiler-owned semantic workspace from governed repository publication state and admitted source inputs. */
    fun buildProjectSemanticGraph(
        publication: AthenaRepositoryReportPublicationResult,
        sources: List<ProjectSemanticSourceInput>,
    ): ProjectSemanticGraphBuildResult {
        return projectSemanticGraphBuilder.build(publication, sources)
    }

    /** Resolves authored imports strictly against one compiler-owned project semantic graph snapshot. */
    fun resolveProjectSemanticImports(snapshot: ProjectSemanticGraphSnapshot): ProjectSemanticGraphSnapshot {
        return projectSemanticImportResolver.resolve(snapshot)
    }

    /** Emits compiler-owned diagnostics derived from one project semantic graph snapshot. */
    fun emitProjectSemanticDiagnostics(snapshot: ProjectSemanticGraphSnapshot): ProjectSemanticGraphSnapshot {
        return projectSemanticDiagnosticProjector.project(snapshot)
    }

    /** Indexes authored declarations into compiler-owned semantic namespaces. */
    fun indexProjectSemanticDeclarations(snapshot: ProjectSemanticGraphSnapshot): ProjectSemanticGraphSnapshot {
        return projectSemanticDeclarationIndexer.index(snapshot)
    }

    /** Derives all supported layouts from the supplied canonical [document]. */
    fun deriveSupportedLayouts(document: EngineeringDocument): List<LayoutDocument> {
        return compilationSupport.deriveSupportedLayouts(document)
    }

    /** Derives geometry for all supported layouts in deterministic view order. */
    fun deriveSupportedGeometries(layouts: List<LayoutDocument>): List<GeometryDocument> {
        return compilationSupport.deriveSupportedGeometries(layouts)
    }

    /** Derives renderer-neutral projection documents for all supported views in deterministic order. */
    fun deriveSupportedProjections(
        document: EngineeringDocument,
        geometries: List<GeometryDocument>,
        knowledgeContext: com.engineeringood.athena.compiler.knowledge.AthenaCompilationKnowledgeContext,
    ): List<ProjectionDocument> {
        return compilationSupport.deriveSupportedProjections(document, geometries, knowledgeContext)
    }

    /** Derives rebuildable presentation documents for all supported projections in deterministic order. */
    fun deriveSupportedPresentations(
        document: EngineeringDocument,
        projections: List<ProjectionDocument>,
    ): List<PresentationDocument> {
        return compilationSupport.deriveSupportedPresentations(document, projections)
    }

    /** Derives one supported layout from the supplied canonical [document] and [viewId]. */
    fun deriveLayout(
        document: EngineeringDocument,
        viewId: String,
    ): LayoutDocument {
        return compilationSupport.deriveLayout(document, viewId)
    }

    /** Derives one geometry document from the supplied [layoutDocument]. */
    fun deriveGeometry(layoutDocument: LayoutDocument): GeometryDocument {
        return compilationSupport.deriveGeometry(layoutDocument)
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
        return compilationSupport.compileParsedSource(parseResult, knowledgeContext, boundaryValidation)
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

        return compilationSupport.recompute(
            source = source,
            document = document,
            knowledgeContext = knowledgeContext,
            boundaryValidation = boundaryValidation,
            affectedScope = affectedScope,
            previousLayouts = previousLayouts,
            previousGeometries = previousGeometries,
            previousRendering = previousRendering,
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
}
