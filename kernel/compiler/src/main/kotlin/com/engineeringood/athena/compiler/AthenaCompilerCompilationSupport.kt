package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.knowledge.AthenaComponentKnowledgeContextBuilder
import com.engineeringood.athena.compiler.knowledge.AthenaComponentKnowledgeContributionSource
import com.engineeringood.athena.compiler.plugin.AthenaDomainSemanticsCoordinator
import com.engineeringood.athena.geometry.GeometryDocument
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.layout.LayoutDocument
import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.presentation.PresentationCompositePack
import com.engineeringood.athena.presentation.PresentationDocument
import com.engineeringood.athena.presentation.PresentationPrimitivePack
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.plugin.AthenaCompilerContributionStage
import com.engineeringood.athena.plugin.AthenaPluginValidationContext
import com.engineeringood.athena.plugin.AthenaSemanticEnrichmentContext
import com.engineeringood.athena.plugin.AthenaSourceDocument
import com.engineeringood.athena.renderer.svg.SvgRenderModel
import com.engineeringood.athena.renderer.svg.SvgRenderer
import com.engineeringood.athena.semantics.core.EngineeringIrValidationScope
import com.engineeringood.athena.semantics.core.EngineeringIrValidator
import com.engineeringood.athena.semantics.core.SemanticContinuationDecision
import com.engineeringood.athena.semantics.core.SemanticDiagnostic
import com.engineeringood.athena.semantics.core.SemanticDiagnosticCategory
import com.engineeringood.athena.semantics.core.SemanticDiagnosticSeverity
import com.engineeringood.athena.semantics.core.SemanticRuleId
import com.engineeringood.athena.semantics.core.SemanticValidationResult

internal class AthenaCompilerCompilationSupport(
    private val lowerer: EngineeringIrLowerer,
    private val validator: EngineeringIrValidator,
    private val layoutIrDeriver: LayoutIrDeriver,
    private val geometryIrDeriver: GeometryIrDeriver,
    private val projectionModelDeriver: ProjectionModelDeriver,
    private val presentationModelDeriver: PresentationModelDeriver,
    private val renderModelDeriver: SvgRenderModelDeriver,
    private val svgRenderer: SvgRenderer,
    private val componentKnowledgeContextBuilder: AthenaComponentKnowledgeContextBuilder,
    private val componentKnowledgeContributionsCache: List<AthenaComponentKnowledgeContributionSource>,
    private val derivedEngineeringContextDeriver: DerivedEngineeringContextDeriver,
    private val capabilityFactPromoter: EngineeringCapabilityFactPromoter,
    private val constraintEvaluator: EngineeringConstraintEvaluator,
    private val domainSemanticsCoordinator: AthenaDomainSemanticsCoordinator,
    private val supportedViewDefinitionsCache: List<ViewDefinition>,
    private val supportedRenderContributionsCache: List<CompilerRenderContributionAttribution>,
    private val supportedPrimitivePresentationPacksCache: List<PresentationPrimitivePack>,
    private val supportedCompositePresentationPacksCache: List<PresentationCompositePack>,
) {
    fun deriveSupportedLayouts(document: EngineeringDocument): List<LayoutDocument> {
        return supportedViewDefinitionsCache.map { viewDefinition ->
            layoutIrDeriver.derive(document, viewDefinition)
        }
    }

    fun deriveSupportedGeometries(layouts: List<LayoutDocument>): List<GeometryDocument> {
        return layouts.map { layoutDocument ->
            geometryIrDeriver.derive(layoutDocument)
        }
    }

    fun deriveSupportedProjections(
        document: EngineeringDocument,
        geometries: List<GeometryDocument>,
        knowledgeContext: com.engineeringood.athena.compiler.knowledge.AthenaCompilationKnowledgeContext,
    ): List<ProjectionDocument> {
        return geometries.map { geometryDocument ->
            val viewDefinition = supportedViewDefinitionsCache.firstOrNull { definition -> definition.id == geometryDocument.viewId }
                ?: error("Unsupported view definition `${geometryDocument.viewId}` for projection derivation.")
            projectionModelDeriver.derive(
                view = viewDefinition,
                document = document,
                geometry = geometryDocument,
                knowledgeContext = knowledgeContext,
            )
        }
    }

    fun deriveSupportedPresentations(
        document: EngineeringDocument,
        projections: List<ProjectionDocument>,
    ): List<PresentationDocument> {
        return projections.map { projection ->
            presentationModelDeriver.derive(
                document = document,
                projection = projection,
                primitivePacks = supportedPrimitivePresentationPacksCache,
                compositePacks = supportedCompositePresentationPacksCache,
            )
        }
    }

    fun deriveLayout(
        document: EngineeringDocument,
        viewId: String,
    ): LayoutDocument {
        val viewDefinition = supportedViewDefinitionsCache.firstOrNull { definition -> definition.id == viewId }
            ?: error("Unsupported view definition `$viewId` for layout derivation.")
        return layoutIrDeriver.derive(document, viewDefinition)
    }

    fun deriveGeometry(layoutDocument: LayoutDocument): GeometryDocument {
        return geometryIrDeriver.derive(layoutDocument)
    }

    fun activeRenderContributions(
        viewId: String,
        rendererTarget: String,
    ): List<CompilerRenderContributionAttribution> {
        return activeRenderContributionsFor(viewId, rendererTarget)
    }

    fun compileParsedSource(
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

    fun recompute(
        source: CompilerSourceDocument,
        document: EngineeringDocument,
        knowledgeContext: com.engineeringood.athena.compiler.knowledge.AthenaCompilationKnowledgeContext,
        boundaryValidation: com.engineeringood.athena.compiler.boundary.AthenaBoundaryValidationReport,
        affectedScope: CompilerAffectedScope,
        previousLayouts: List<LayoutDocument>,
        previousGeometries: List<GeometryDocument>,
        previousRendering: CompilerRenderingResult,
    ): CompilerCompilationSuccess {
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
        val effectiveKnowledgeContext = knowledgeContext.withResolvedComponentKnowledge(
            componentKnowledgeContextBuilder.build(document, componentKnowledgeContributionsCache),
        )
        val derivedContext = derivedEngineeringContextDeriver.derive(document)
        val capabilityFacts = capabilityFactPromoter.promote(
            derivedContext = derivedContext,
            knowledgeContext = effectiveKnowledgeContext,
        )
        val constraintEvaluationOutcome = constraintEvaluator.evaluate(
            derivedContext = derivedContext,
            capabilityFacts = capabilityFacts,
            knowledgeContext = effectiveKnowledgeContext,
        )
        val validationResult = validatePass(
            source = source,
            document = document,
            affectedScope = affectedScope,
            semanticEnrichment = semanticEnrichmentResult,
            engineeringSufficiencyDiagnostics = constraintEvaluationOutcome.diagnostics,
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
        val projections = deriveSupportedProjections(
            document = document,
            geometries = backendPreparation.geometries,
            knowledgeContext = effectiveKnowledgeContext,
        )
        val presentations = deriveSupportedPresentations(
            document = document,
            projections = projections,
        )
        val knowledgeAttributions = buildKnowledgeAttributions(effectiveKnowledgeContext)
        return CompilerCompilationSuccess(
            source = source,
            document = document,
            derivedContext = derivedContext,
            capabilityFacts = capabilityFacts,
            constraintEvaluations = constraintEvaluationOutcome.evaluations,
            semanticResult = validationResult.semanticResult,
            validationBreakdown = validationResult.validationBreakdown,
            layouts = backendPreparation.layouts,
            geometries = backendPreparation.geometries,
            projections = projections,
            presentations = presentations,
            rendering = backendEmission.rendering,
            knowledgeContext = effectiveKnowledgeContext,
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
        engineeringSufficiencyDiagnostics: List<SemanticDiagnostic>,
    ): ValidationPassResult {
        val validation = validateSemantics(
            source = source,
            document = document,
            affectedScope = affectedScope,
            enrichmentDiagnostics = semanticEnrichment.contribution.diagnostics,
            engineeringSufficiencyDiagnostics = engineeringSufficiencyDiagnostics,
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
        engineeringSufficiencyDiagnostics: List<SemanticDiagnostic>,
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
            engineeringSufficiencyDiagnostics = engineeringSufficiencyDiagnostics,
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

    fun domainSemanticsUnavailableDiagnostics(
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
        return geometries.firstOrNull { geometry ->
            activeRenderContributionsFor(
                viewId = geometry.viewId,
                rendererTarget = SVG_RENDERER_TARGET,
            ).isNotEmpty()
        } ?: geometries.firstOrNull()
    }

    private fun activeRenderContributionsFor(
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

private const val SVG_RENDERER_TARGET = "svg"
