package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.plugin.AthenaDomainSemanticsCoordinator
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.language.RepresentationSourceUnit
import com.engineeringood.athena.plugin.AthenaCompilerContributionStage
import com.engineeringood.athena.plugin.AthenaPluginValidationContext
import com.engineeringood.athena.plugin.AthenaSemanticEnrichmentContext
import com.engineeringood.athena.plugin.AthenaSourceDocument
import com.engineeringood.athena.semantics.core.EngineeringIrValidator
import com.engineeringood.athena.semantics.core.SemanticContinuationDecision
import com.engineeringood.athena.semantics.core.SemanticDiagnostic
import com.engineeringood.athena.semantics.core.SemanticDiagnosticCategory
import com.engineeringood.athena.semantics.core.SemanticDiagnosticSeverity
import com.engineeringood.athena.semantics.core.SemanticRuleId
import com.engineeringood.athena.semantics.core.SemanticValidationResult
import com.engineeringood.athena.spatial.SpatialDocument
import com.engineeringood.athena.spatial.SpatialReality
import com.engineeringood.athena.spatial.SpatialSourceTrace

internal class AthenaCompilerCompilationSupport(
    private val lowerer: EngineeringIrLowerer,
    private val validator: EngineeringIrValidator,
    private val domainSemanticsCoordinator: AthenaDomainSemanticsCoordinator,
) {
    fun compileParsedSource(
        parseResult: CompilerParseResult,
        knowledgeContext: com.engineeringood.athena.compiler.knowledge.AthenaCompilationKnowledgeContext,
        boundaryValidation: com.engineeringood.athena.compiler.boundary.AthenaBoundaryValidationReport,
    ): CompilerCompilationResult {
        return when (parseResult) {
            is CompilerParseFailure -> parseFailure(parseResult, knowledgeContext, boundaryValidation)
            is CompilerParseSuccess -> compileParsedSource(parseResult.source, knowledgeContext, boundaryValidation)
        }
    }

    fun domainSemanticsUnavailableDiagnostics(
        source: CompilerSourceDocument,
        document: EngineeringDocument,
    ): List<SemanticDiagnostic> {
        if (source.ast.declarations.isEmpty()) return emptyList()
        if (!domainSemanticsCoordinator.hasParticipants(AthenaCompilerContributionStage.LOWER)) {
            return listOf(domainSemanticsUnavailableDiagnostic(document))
        }
        if (document.entities.isEmpty() && document.ports.isEmpty() && document.relationships.isEmpty()) {
            return listOf(domainSemanticsUnavailableDiagnostic(document))
        }
        return emptyList()
    }

    private fun compileParsedSource(
        source: CompilerSourceDocument,
        knowledgeContext: com.engineeringood.athena.compiler.knowledge.AthenaCompilationKnowledgeContext,
        boundaryValidation: com.engineeringood.athena.compiler.boundary.AthenaBoundaryValidationReport,
    ): CompilerCompilationResult {
        if (source.ast.unit is RepresentationSourceUnit) {
            return representationSourceFailure(source, knowledgeContext, boundaryValidation)
        }

        val document = lowerer.lower(source)
        val enrichment = semanticEnrichment(source, document)
        val validation = validate(
            source = source,
            document = document,
            enrichmentDiagnostics = enrichment.diagnostics,
        )
        val projection = if (validation.semanticResult.continuationDecision == SemanticContinuationDecision.CONTINUE) {
            AuthoredProjectionViewCompiler.compile(document)
        } else {
            null
        }
        val projections = (projection as? AuthoredProjectionCompilation.Success)?.documents.orEmpty()
        val projectionDiagnostics = (projection as? AuthoredProjectionCompilation.Failure)
            ?.diagnostics
            ?.map { diagnostic -> diagnostic.message }
            .orEmpty()
        val spatialCandidates = mutableListOf<SpatialDocument>()
        val spatialDiagnostics = mutableListOf<RealityTransformationDiagnostic>()
        if (projectionDiagnostics.isEmpty()) {
            projections.forEach { projectedDocument ->
                when (val result = ProjectionSpatialCompiler().transform(projectedDocument)) {
                    is RealityTransformationResult.Success -> spatialCandidates += result.output
                    is RealityTransformationResult.Failure -> spatialDiagnostics += result.diagnostics
                }
            }
        }
        val spatialResolution = resolveCanonicalSpatialDocuments(spatialCandidates)
        spatialDiagnostics += spatialResolution.diagnostics
        val spatialPublicationFailed = projectionDiagnostics.isNotEmpty() || spatialDiagnostics.isNotEmpty()

        return CompilerCompilationSuccess(
            source = source,
            document = document,
            semanticResult = validation.semanticResult,
            validationBreakdown = validation.validationBreakdown,
            projections = projections,
            projectionDiagnostics = projectionDiagnostics,
            spatialDocuments = if (spatialPublicationFailed) {
                CompilerSpatialDocuments.empty()
            } else {
                CompilerSpatialDocuments.of(spatialResolution.documents)
            },
            realityTransformationDiagnostics = spatialDiagnostics.distinct().sortedWith(
                compareBy(
                    RealityTransformationDiagnostic::reality,
                    { diagnostic -> diagnostic.subject.orEmpty() },
                    { diagnostic -> diagnostic.problem.orEmpty() },
                    RealityTransformationDiagnostic::message,
                ),
            ),
            knowledgeContext = knowledgeContext,
            boundaryValidation = boundaryValidation,
            knowledgeAttributions = buildKnowledgeAttributions(knowledgeContext),
            pipeline = CompilerPipelineReport(
                listOf(
                    CompilerPassRecord(PARSE_PASS, CompilerPassExecutionStatus.SUCCEEDED, systemIdentitySummary(source)),
                    CompilerPassRecord(
                        LOWER_PASS,
                        CompilerPassExecutionStatus.SUCCEEDED,
                        "${document.entities.size} entities, ${document.functions.size} functions, ${document.ports.size} ports",
                    ),
                    CompilerPassRecord(
                        VALIDATE_PASS,
                        CompilerPassExecutionStatus.SUCCEEDED,
                        if (validation.semanticResult.isSemanticallyValid) "engineering-valid" else "engineering-invalid",
                    ),
                    projectionPass(validation.semanticResult, projection, projections.size),
                    spatialPass(validation.semanticResult, projectionDiagnostics, spatialResolution.documents.size, spatialDiagnostics),
                ),
            ),
        )
    }

    private fun parseFailure(
        result: CompilerParseFailure,
        knowledgeContext: com.engineeringood.athena.compiler.knowledge.AthenaCompilationKnowledgeContext,
        boundaryValidation: com.engineeringood.athena.compiler.boundary.AthenaBoundaryValidationReport,
    ): CompilerCompilationParseFailure = CompilerCompilationParseFailure(
        diagnostics = result.diagnostics,
        knowledgeContext = knowledgeContext,
        boundaryValidation = boundaryValidation,
        pipeline = CompilerPipelineReport(
            listOf(
                CompilerPassRecord(PARSE_PASS, CompilerPassExecutionStatus.FAILED, "${result.diagnostics.size} syntax diagnostics"),
                skippedPassRecord(LOWER_PASS, "parse failed"),
                skippedPassRecord(VALIDATE_PASS, "parse failed"),
                skippedPassRecord(PROJECT_PASS, "parse failed"),
                skippedPassRecord(SPATIAL_PASS, "parse failed"),
            ),
        ),
    )

    private fun representationSourceFailure(
        source: CompilerSourceDocument,
        knowledgeContext: com.engineeringood.athena.compiler.knowledge.AthenaCompilationKnowledgeContext,
        boundaryValidation: com.engineeringood.athena.compiler.boundary.AthenaBoundaryValidationReport,
    ): CompilerCompilationParseFailure {
        val span = source.ast.span
        return CompilerCompilationParseFailure(
            diagnostics = listOf(
                CompilerSyntaxDiagnostic(
                    file = source.file,
                    line = span.start.line,
                    column = span.start.column,
                    endLine = span.end.line,
                    endColumn = span.end.column,
                    message = "Representation source is outside the M42 engineering source contract.",
                ),
            ),
            knowledgeContext = knowledgeContext,
            boundaryValidation = boundaryValidation,
            pipeline = CompilerPipelineReport(
                listOf(
                    CompilerPassRecord(PARSE_PASS, CompilerPassExecutionStatus.SUCCEEDED, "representation source"),
                    skippedPassRecord(LOWER_PASS, "not engineering source"),
                    skippedPassRecord(VALIDATE_PASS, "not engineering source"),
                    skippedPassRecord(PROJECT_PASS, "not engineering source"),
                    skippedPassRecord(SPATIAL_PASS, "not engineering source"),
                ),
            ),
        )
    }

    private fun semanticEnrichment(
        source: CompilerSourceDocument,
        document: EngineeringDocument,
    ): com.engineeringood.athena.plugin.AthenaDomainSemanticEnrichmentContribution {
        if (!domainSemanticsCoordinator.hasParticipants(AthenaCompilerContributionStage.SEMANTIC_ENRICHMENT)) {
            return com.engineeringood.athena.plugin.AthenaDomainSemanticEnrichmentContribution.EMPTY
        }
        return domainSemanticsCoordinator.enrichSemantics(
            document,
            AthenaSemanticEnrichmentContext(
                document = document,
                source = source.toAthenaSourceDocument(),
                approvedPluginIds = domainSemanticsCoordinator.activePluginIds,
            ),
        )
    }

    private fun validate(
        source: CompilerSourceDocument,
        document: EngineeringDocument,
        enrichmentDiagnostics: List<SemanticDiagnostic>,
    ): ValidationComputation {
        val kernelResult = validator.validate(document)
        val anatomyDiagnostics = EngineeringAnatomySourceValidator.validate(source)
        val domainContribution = domainSemanticsCoordinator.validate(
            document,
            AthenaPluginValidationContext(
                document = document,
                source = source.toAthenaSourceDocument(),
                approvedPluginIds = domainSemanticsCoordinator.activePluginIds,
            ),
        )
        val domainDiagnostics = buildList {
            if (anatomyDiagnostics.isEmpty()) {
                addAll(domainSemanticsUnavailableDiagnostics(source, document))
            }
            addAll(domainContribution.diagnostics)
        }
        val breakdown = CompilerValidationBreakdown(
            semanticEnrichmentDiagnostics = enrichmentDiagnostics,
            kernelDiagnostics = anatomyDiagnostics + kernelResult.diagnostics,
            domainDiagnostics = domainDiagnostics,
            domainValidationAttributions = domainContribution.attributions,
        )
        val diagnostics = breakdown.semanticEnrichmentDiagnostics +
            breakdown.kernelDiagnostics +
            breakdown.domainDiagnostics
        return ValidationComputation(
            semanticResult = SemanticValidationResult(
                diagnostics = diagnostics,
                continuationDecision = if (diagnostics.any { diagnostic -> diagnostic.severity == SemanticDiagnosticSeverity.ERROR }) {
                    SemanticContinuationDecision.STOP_DOWNSTREAM
                } else {
                    SemanticContinuationDecision.CONTINUE
                },
            ),
            validationBreakdown = breakdown,
        )
    }

    private fun domainSemanticsUnavailableDiagnostic(document: EngineeringDocument): SemanticDiagnostic = SemanticDiagnostic(
        severity = SemanticDiagnosticSeverity.ERROR,
        ruleId = SemanticRuleId("domain.semantics.unavailable"),
        category = SemanticDiagnosticCategory.DOMAIN,
        subjectIdentity = document.system.id,
        provenance = document.system.provenance,
        message = "No approved domain plugin claimed the authored domain semantics in `${document.system.name}`.",
    )

    private fun buildKnowledgeAttributions(
        context: com.engineeringood.athena.compiler.knowledge.AthenaCompilationKnowledgeContext,
    ): List<CompilerKnowledgeAttribution> {
        val artifacts = context.activeArtifacts.map { artifact ->
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
                responsibleArtifacts = artifacts,
                rationale = "Reviewed package facts admitted for this compilation.",
            ),
            CompilerKnowledgeAttribution(
                target = CompilerKnowledgeAttributionTarget.SEMANTIC_RESULT,
                responsibleArtifacts = emptyList(),
                rationale = "M42 knowledge evaluation is not installed by Story 1.1.",
            ),
        )
    }

    private fun projectionPass(
        semanticResult: SemanticValidationResult,
        projection: AuthoredProjectionCompilation?,
        count: Int,
    ): CompilerPassRecord = when {
        semanticResult.continuationDecision != SemanticContinuationDecision.CONTINUE ->
            skippedPassRecord(PROJECT_PASS, "engineering validation stopped projection")
        projection is AuthoredProjectionCompilation.Failure ->
            CompilerPassRecord(PROJECT_PASS, CompilerPassExecutionStatus.FAILED, "${projection.diagnostics.size} projection diagnostics")
        else -> CompilerPassRecord(PROJECT_PASS, CompilerPassExecutionStatus.SUCCEEDED, "$count projection documents")
    }

    private fun spatialPass(
        semanticResult: SemanticValidationResult,
        projectionDiagnostics: List<String>,
        count: Int,
        diagnostics: List<RealityTransformationDiagnostic>,
    ): CompilerPassRecord = when {
        semanticResult.continuationDecision != SemanticContinuationDecision.CONTINUE ->
            skippedPassRecord(SPATIAL_PASS, "engineering validation stopped spatial derivation")
        projectionDiagnostics.isNotEmpty() -> skippedPassRecord(SPATIAL_PASS, "projection failed")
        diagnostics.isNotEmpty() ->
            CompilerPassRecord(SPATIAL_PASS, CompilerPassExecutionStatus.FAILED, "${diagnostics.size} spatial diagnostics")
        else -> CompilerPassRecord(SPATIAL_PASS, CompilerPassExecutionStatus.SUCCEEDED, "$count spatial documents")
    }
}

internal data class CanonicalSpatialResolution(
    val documents: List<SpatialDocument>,
    val diagnostics: List<RealityTransformationDiagnostic>,
)

internal fun resolveCanonicalSpatialDocuments(candidates: List<SpatialDocument>): CanonicalSpatialResolution {
    val distinctCandidates = candidates.distinct()
    val bySheetIdentity = distinctCandidates.groupBy { document -> document.sheets.map { sheet -> sheet.sheetId } }
    val conflictingDocuments = mutableSetOf<SpatialDocument>()
    val diagnostics = mutableListOf<RealityTransformationDiagnostic>()

    bySheetIdentity.entries
        .filter { (_, documents) -> documents.size > 1 }
        .sortedWith { left, right -> compareSpatialSheetIds(left.key, right.key) }
        .forEach { (sheetIds, documents) ->
            conflictingDocuments += documents
            val subject = if (sheetIds.size == 1) {
                "Spatial document on Sheet ${sheetIds.single()}"
            } else {
                "Spatial document for ordered Sheets ${sheetIds.joinToString(", ")}"
            }
            diagnostics += spatialResolutionDiagnostic(
                subject,
                "has ${documents.size} unequal candidates",
                "Publish one canonical Spatial document for each ordered Sheet identity set.",
                documents,
            )
        }

    val identities = bySheetIdentity.keys.sortedWith(::compareSpatialSheetIds)
    for (leftIndex in identities.indices) {
        for (rightIndex in leftIndex + 1 until identities.size) {
            val left = identities[leftIndex]
            val right = identities[rightIndex]
            val sharedSheetIds = left.toSet().intersect(right.toSet()).sorted()
            if (sharedSheetIds.isEmpty()) continue
            val documents = bySheetIdentity.getValue(left) + bySheetIdentity.getValue(right)
            conflictingDocuments += documents
            val subject = if (sharedSheetIds.size == 1) {
                "Spatial documents sharing Sheet ${sharedSheetIds.single()}"
            } else {
                "Spatial documents sharing Sheets ${sharedSheetIds.joinToString(", ")}"
            }
            diagnostics += spatialResolutionDiagnostic(
                subject,
                "declare overlapping but unequal ordered Sheet identity sets [${left.joinToString(", ")}] and [${right.joinToString(", ")}]",
                "Publish disjoint Spatial documents, or one canonical document for the complete ordered Sheet identity set.",
                documents,
            )
        }
    }
    return CanonicalSpatialResolution(
        documents = distinctCandidates.filterNot(conflictingDocuments::contains).sortedWith { left, right ->
            compareSpatialSheetIds(left.sheets.map { it.sheetId }, right.sheets.map { it.sheetId })
        },
        diagnostics = diagnostics.sortedWith(compareBy({ it.subject.orEmpty() }, { it.problem.orEmpty() })),
    )
}

private fun spatialResolutionDiagnostic(
    subject: String,
    problem: String,
    correction: String,
    documents: List<SpatialDocument>,
): RealityTransformationDiagnostic {
    val traces = documents.flatMap { document -> document.sheets.map { sheet -> sheet.sourceTrace } }
    return RealityTransformationDiagnostic(
        reality = SpatialReality.name,
        message = "$subject $problem. $correction",
        subject = subject,
        problem = problem,
        correction = correction,
        sourceTrace = SpatialSourceTrace(
            projectionIds = traces.flatMap(SpatialSourceTrace::projectionIds).distinct().sorted(),
            geometryElementIds = traces.flatMap(SpatialSourceTrace::geometryElementIds)
                .distinct()
                .sortedBy { geometryId -> geometryId.value },
        ),
    )
}

private fun compareSpatialSheetIds(left: List<String>, right: List<String>): Int {
    for (index in 0 until minOf(left.size, right.size)) {
        val comparison = left[index].compareTo(right[index])
        if (comparison != 0) return comparison
    }
    return left.size.compareTo(right.size)
}

private fun CompilerSourceDocument.toAthenaSourceDocument(): AthenaSourceDocument = AthenaSourceDocument(file, ast)

private fun systemIdentitySummary(source: CompilerSourceDocument): String = "system:${source.ast.system.name}"

private fun skippedPassRecord(pass: CompilerPassDescriptor, reason: String): CompilerPassRecord =
    CompilerPassRecord(pass, CompilerPassExecutionStatus.SKIPPED, reason)

private data class ValidationComputation(
    val semanticResult: SemanticValidationResult,
    val validationBreakdown: CompilerValidationBreakdown,
)

private val PARSE_PASS = CompilerPassDescriptor(
    id = CompilerPassId.PARSE,
    responsibility = "Parse Athena source with exact spans.",
    inputState = "Athena source text",
    outputState = "Athena AST",
)

private val LOWER_PASS = CompilerPassDescriptor(
    id = CompilerPassId.LOWER_ENGINEERING_REALITY,
    responsibility = "Lower authored meaning into Engineering Reality.",
    inputState = "Athena AST",
    outputState = "EngineeringDocument",
)

private val VALIDATE_PASS = CompilerPassDescriptor(
    id = CompilerPassId.VALIDATE_ENGINEERING_REALITY,
    responsibility = "Validate exact engineering anatomy and domain contributions.",
    inputState = "EngineeringDocument",
    outputState = "SemanticValidationResult",
)

private val PROJECT_PASS = CompilerPassDescriptor(
    id = CompilerPassId.PROJECT,
    responsibility = "Project validated engineering facts without presentation ownership.",
    inputState = "Validated EngineeringDocument",
    outputState = "ProjectionDocument",
)

private val SPATIAL_PASS = CompilerPassDescriptor(
    id = CompilerPassId.DERIVE_SPATIAL,
    responsibility = "Derive deterministic Spatial Reality from Projection Reality.",
    inputState = "ProjectionDocument",
    outputState = "SpatialDocument",
)
