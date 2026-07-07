package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.boundary.AthenaBoundaryDescriptorResolver
import com.engineeringood.athena.compiler.boundary.AthenaBoundaryDescriptorSource
import com.engineeringood.athena.compiler.knowledge.AthenaKnowledgePackageSource
import com.engineeringood.athena.compiler.knowledge.AthenaKnowledgeResolver
import com.engineeringood.athena.compiler.plugin.AthenaApprovedPluginInventory
import com.engineeringood.athena.compiler.plugin.AthenaDomainPlugin
import com.engineeringood.athena.compiler.plugin.AthenaDomainSemanticsCoordinator
import com.engineeringood.athena.compiler.plugin.AthenaExtensionPoint
import com.engineeringood.athena.compiler.plugin.AthenaPluginDiscovery
import com.engineeringood.athena.compiler.plugin.AthenaPluginDiscoveryReport
import com.engineeringood.athena.compiler.plugin.AthenaPluginValidationContext
import com.engineeringood.athena.compiler.plugin.AthenaViewDefinitionContributor
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
    private val supportedViewDefinitionsCache: List<ViewDefinition> = pluginInventory
        .attachedPlugins(AthenaExtensionPoint.VIEW_DEFINITIONS)
        .flatMap { approvedPlugin ->
            (approvedPlugin.candidate.plugin as? AthenaViewDefinitionContributor)?.viewDefinitions().orEmpty()
        }

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

    /** Returns supported view definitions in deterministic approved-plugin order. */
    fun supportedViewDefinitions(): List<ViewDefinition> = supportedViewDefinitionsCache

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
        val (semanticResult, validationMode) = validateSemantics(source, document, affectedScope)
        val layoutResult = if (semanticResult.continuationDecision == SemanticContinuationDecision.CONTINUE) {
            deriveLayouts(
                document = document,
                affectedScope = affectedScope,
                previousLayouts = previousLayouts,
            )
        } else {
            LayoutDerivationResult(
                layouts = emptyList(),
                mode = CompilerIncrementalPassMode.FULL_FALLBACK,
                scopedViewIds = emptyList(),
            )
        }
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
        val validateRecord = CompilerPassRecord(
            pass = VALIDATE_PASS,
            status = CompilerPassExecutionStatus.SUCCEEDED,
            outputSummary = semanticSummary(semanticResult, affectedScope, validationMode),
        )
        val renderingResult = renderResult(
            result = semanticResult,
            document = document,
            geometries = geometryResult.geometries,
            affectedScope = affectedScope,
            layoutMode = layoutResult.mode,
            geometryMode = geometryResult.mode,
            previousRendering = previousRendering,
        )
        val knowledgeAttributions = buildKnowledgeAttributions(knowledgeContext)
        return CompilerCompilationSuccess(
            source = source,
            document = document,
            semanticResult = semanticResult,
            layouts = layoutResult.layouts,
            geometries = geometryResult.geometries,
            rendering = renderingResult.rendering,
            knowledgeContext = knowledgeContext,
            boundaryValidation = boundaryValidation,
            knowledgeAttributions = knowledgeAttributions,
            pipeline = CompilerPipelineReport(
                passes = listOf(parseRecord, lowerRecord, validateRecord, renderingResult.passRecord),
            ),
            incrementalUpdateReport = affectedScope?.let { scope ->
                CompilerIncrementalUpdateReport(
                    affectedScope = scope,
                    validationMode = validationMode,
                    layoutMode = layoutResult.mode,
                    layoutScopedViewIds = layoutResult.scopedViewIds,
                    geometryMode = geometryResult.mode,
                    geometryScopedViewIds = geometryResult.scopedViewIds,
                    renderingMode = renderingResult.mode,
                    renderingViewIds = renderingResult.viewIds,
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
            val previousGeometry = previousGeometriesByViewId[layout.view.id] ?: return fallbackGeometryDerivation(layouts)
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
        document: com.engineeringood.athena.ir.EngineeringDocument,
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
        document: com.engineeringood.athena.ir.EngineeringDocument,
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
        document: com.engineeringood.athena.ir.EngineeringDocument,
        geometries: List<GeometryDocument>,
        affectedScope: CompilerAffectedScope?,
        layoutMode: CompilerIncrementalPassMode,
        geometryMode: CompilerIncrementalPassMode,
        previousRendering: CompilerRenderingResult?,
    ): RenderingDerivationResult {
        return if (result.continuationDecision == com.engineeringood.athena.semantics.core.SemanticContinuationDecision.CONTINUE) {
            val renderingGeometry = selectRenderingGeometry(geometries)
            if (renderingGeometry == null) {
                return RenderingDerivationResult(
                    rendering = CompilerRenderingBlocked(
                        reason = "no supported geometry-backed backend input was derived",
                        blockedByPass = CompilerPassId.DOWNSTREAM_DERIVATION,
                    ),
                    mode = CompilerIncrementalPassMode.FULL_FALLBACK,
                    viewIds = emptyList(),
                    passRecord = CompilerPassRecord(
                        pass = DOWNSTREAM_DERIVATION_PASS,
                        status = CompilerPassExecutionStatus.FAILED,
                        outputSummary = "render-blocked (no geometry)",
                    ),
                )
            }
            val incrementalRenderModel = incrementalRenderModel(
                systemName = document.system.name,
                geometry = renderingGeometry,
                affectedScope = affectedScope,
                previousRendering = previousRendering,
            )
            val renderingMode = if (incrementalRenderModel != null) {
                CompilerIncrementalPassMode.SCOPED
            } else {
                CompilerIncrementalPassMode.FULL_FALLBACK
            }
            val renderModel = incrementalRenderModel ?: renderModelDeriver.derive(
                systemName = document.system.name,
                geometry = renderingGeometry,
            )
            val svg = svgRenderer.render(
                systemName = document.system.name,
                geometry = renderingGeometry,
            )
            RenderingDerivationResult(
                rendering = CompilerRenderingSuccess(
                    model = renderModel,
                    svg = svg,
                ),
                mode = renderingMode,
                viewIds = if (renderingMode == CompilerIncrementalPassMode.SCOPED) {
                    listOf(renderingGeometry.viewId)
                } else {
                    emptyList()
                },
                passRecord = CompilerPassRecord(
                    pass = DOWNSTREAM_DERIVATION_PASS,
                    status = CompilerPassExecutionStatus.SUCCEEDED,
                    outputSummary = if (affectedScope == null) {
                        "svg-emitted"
                    } else {
                        "svg-emitted (layout=${layoutMode.name.lowercase()} geometry=${geometryMode.name.lowercase()} rendering=${renderingMode.name.lowercase()})"
                    },
                ),
            )
        } else {
            RenderingDerivationResult(
                rendering = CompilerRenderingBlocked(
                    reason = "semantic validation requested ${result.continuationDecision}",
                    blockedByPass = CompilerPassId.VALIDATE,
                ),
                mode = CompilerIncrementalPassMode.FULL_FALLBACK,
                viewIds = emptyList(),
                passRecord = CompilerPassRecord(
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

private data class RenderingDerivationResult(
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

private val VALIDATE_PASS = CompilerPassDescriptor(
    id = CompilerPassId.VALIDATE,
    responsibility = "Validate canonical Engineering IR and compute continuation policy",
    inputState = "canonical Engineering IR",
    outputState = "semantic validation result",
)

private val DOWNSTREAM_DERIVATION_PASS = CompilerPassDescriptor(
    id = CompilerPassId.DOWNSTREAM_DERIVATION,
    responsibility = "Feed explicit Geometry IR into the first downstream backend and emit simple SVG when policy allows",
    inputState = "semantic validation result plus derived geometry",
    outputState = "geometry-backed render result",
)

private const val DEFAULT_RENDER_VIEW_ID = "cabinet"
