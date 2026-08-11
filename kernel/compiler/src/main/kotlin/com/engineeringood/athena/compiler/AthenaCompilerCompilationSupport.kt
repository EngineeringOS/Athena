package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.plugin.AthenaDomainSemanticsCoordinator
import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.language.AthenaSheetCompanionParser
import com.engineeringood.athena.language.SheetCompanionParseFailure
import com.engineeringood.athena.language.SheetCompanionParseSuccess
import com.engineeringood.athena.language.SheetCompanionFound
import com.engineeringood.athena.language.SheetCompanionLocator
import com.engineeringood.athena.language.AthenaSheetStyleCompanionParser
import com.engineeringood.athena.language.SheetStyleCompanionLocator
import com.engineeringood.athena.language.SheetStyleCompanionParseSuccess
import com.engineeringood.athena.language.SheetStyleCompanionSource
import com.engineeringood.athena.language.RepresentationSourceUnit
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.projection.ProjectionSheetGrid
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
import com.engineeringood.athena.spatial.ConnectionAnnotationDisplayRole
import com.engineeringood.athena.spatial.ConnectionAnnotationSelection
import com.engineeringood.athena.ir.StableSemanticIdentity
import java.nio.file.Files
import java.nio.file.Path

internal class AthenaCompilerCompilationSupport(
    private val lowerer: EngineeringIrLowerer,
    private val validator: EngineeringIrValidator,
    private val domainSemanticsCoordinator: AthenaDomainSemanticsCoordinator,
) {
    fun compileParsedSource(
        parseResult: CompilerParseResult,
        sheetCompanionOverride: com.engineeringood.athena.language.SheetCompanionSource? = null,
    ): CompilerCompilationResult {
        return when (parseResult) {
            is CompilerParseFailure -> parseFailure(parseResult)
            is CompilerParseSuccess -> compileParsedSource(parseResult.source, sheetCompanionOverride)
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
        sheetCompanionOverride: com.engineeringood.athena.language.SheetCompanionSource?,
    ): CompilerCompilationResult {
        if (source.ast.unit is RepresentationSourceUnit) {
            return representationSourceFailure(source)
        }

        val document = lowerer.lower(source)
        val enrichment = semanticEnrichment(source, document)
        val validation = validate(
            source = source,
            document = document,
            enrichmentDiagnostics = enrichment.diagnostics,
        )
        val connectionIrResult = ConnectionIrCompiler().compile(document, validation.semanticResult)
        val connectionIr = (connectionIrResult as? ConnectionIrCompilation.Success)?.document
        val connectionIrDiagnostics = (connectionIrResult as? ConnectionIrCompilation.Failure)?.diagnostics.orEmpty()
        val projection = if (validation.semanticResult.continuationDecision == SemanticContinuationDecision.CONTINUE) {
            AuthoredProjectionViewCompiler.compile(document, connectionIr)
        } else {
            null
        }
        val rawProjections = (projection as? AuthoredProjectionCompilation.Success)?.documents.orEmpty()
        val projectionDiagnostics = (projection as? AuthoredProjectionCompilation.Failure)
            ?.diagnostics
            ?.map { diagnostic -> diagnostic.message }
            .orEmpty()
        val sheetCompanion = loadSheetCompanion(source, sheetCompanionOverride)
        val styleCompanion = loadSheetStyleCompanion(source)
        val projectionIntentResults = rawProjections.map { projectedDocument ->
            applySheetCompanionProjectionIntent(projectedDocument, sheetCompanion.source)
        }
        val projections = projectionIntentResults.map(SheetProjectionIntentResult::projection)
        val annotationSelections = styleAnnotationSelections(styleCompanion?.source, connectionIr, projections)
        val spatialCandidates = mutableListOf<SpatialDocument>()
        val spatialDiagnostics = mutableListOf<RealityTransformationDiagnostic>()
        if (projectionDiagnostics.isEmpty()) {
            spatialDiagnostics += sheetCompanion.diagnostics
            spatialDiagnostics += projectionIntentResults.flatMap(SheetProjectionIntentResult::diagnostics)
            projections.forEach { projectedDocument ->
                when (val result = ProjectionSpatialCompiler().transform(
                    projectedDocument,
                    placementConstraints = emptyList(),
                    connectionIr = connectionIr,
                    annotationSelections = annotationSelections,
                )) {
                    is RealityTransformationResult.Success -> {
                        val constrained = applySheetCompanion(
                            projectedDocument,
                            result.output,
                            sheetCompanion.source,
                            connectionIr,
                            annotationSelections,
                        )
                        spatialDiagnostics += constrained.diagnostics
                        constrained.document?.let(spatialCandidates::add)
                    }
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
            sheetCompanion = sheetCompanion.source,
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
            connectionIr = connectionIr,
            connectionIrDiagnostics = connectionIrDiagnostics,
        )
    }

    private fun applySheetCompanionProjectionIntent(
        projection: ProjectionDocument,
        companion: com.engineeringood.athena.language.SheetCompanionSource?,
    ): SheetProjectionIntentResult {
        if (companion == null) return SheetProjectionIntentResult(projection, emptyList())
        val target = projection.sheets.firstOrNull { sheet ->
            sheet.displayName == companion.name || sheet.sheetId.value == companion.name
        } ?: projection.sheets.singleOrNull() ?: return SheetProjectionIntentResult(projection, emptyList())
        val publication = target.publication.copy(
            pageSize = target.publication.pageSize.copy(
                format = companion.page.format,
                orientation = companion.page.orientation.name.lowercase(),
            ),
            titleBlock = target.publication.titleBlock.copy(
                sheetTitle = companion.title?.text ?: target.publication.titleBlock.sheetTitle,
            ),
        )
        val companionSheet = target.copy(
            grid = target.grid?.copy(
                rows = companion.frame.rows,
                columns = companion.frame.columns,
            ) ?: ProjectionSheetGrid(
                gridId = "${target.sheetId.value}/plot-frame",
                rows = companion.frame.rows,
                columns = companion.frame.columns,
            ),
            publication = publication,
            composition = target.composition.copy(publication = publication),
        )
        val sheetProjection = projection.copy(
            sheets = projection.sheets.map { sheet ->
                if (sheet.sheetId == target.sheetId) companionSheet else sheet
            },
        )
        val routeMapping = SheetCompanionProjectionPlacementMapper().mapRouteConstraints(
            companion,
            companionSheet,
            sheetProjection,
        )
        val routeDiagnostics = routeMapping.diagnostics.map { diagnostic ->
            RealityTransformationDiagnostic(
                reality = "Sheet Companion",
                message = diagnostic.message,
                subject = companionSheet.sheetId.value,
                problem = diagnostic.message,
                correction = "Use a Connection Projection placed on this Sheet and a unique stable route target.",
                sourceTrace = SpatialSourceTrace(
                    projectionIds = listOf(companionSheet.sheetId.value),
                    geometryElementIds = listOf(companionSheet.originGeometryElementId),
                ),
            )
        }
        return SheetProjectionIntentResult(
            projection = sheetProjection.copy(connections = routeMapping.connections),
            diagnostics = routeDiagnostics,
        )
    }

    private fun parseFailure(
        result: CompilerParseFailure,
    ): CompilerCompilationParseFailure = CompilerCompilationParseFailure(
        diagnostics = result.diagnostics,
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

    private fun loadSheetCompanion(
        source: CompilerSourceDocument,
        sheetCompanionOverride: com.engineeringood.athena.language.SheetCompanionSource?,
    ): SheetCompanionLoadResult {
        if (sheetCompanionOverride != null) return SheetCompanionLoadResult(sheetCompanionOverride, emptyList())
        val sourcePath = runCatching { Path.of(source.file) }.getOrNull() ?: return SheetCompanionLoadResult(null, emptyList())
        val fileName = sourcePath.fileName?.toString() ?: return SheetCompanionLoadResult(null, emptyList())
        if (!fileName.endsWith(".athena") || fileName.endsWith(".sheet.athena")) {
            return SheetCompanionLoadResult(null, emptyList())
        }
        val location = SheetCompanionLocator.locate(sourcePath)
        val companionPath = (location as? SheetCompanionFound)?.path
        if (companionPath == null) {
            val expected = location.expectedPath
            val problem = when (location) {
                is com.engineeringood.athena.language.SheetCompanionMissing ->
                    "Sheet Companion `${expected.fileName}` is missing."
                is com.engineeringood.athena.language.SheetCompanionAmbiguous ->
                    "Sheet Companion `${expected.fileName}` has more than one candidate."
                is SheetCompanionFound -> error("Found companion must have a path.")
            }
            return SheetCompanionLoadResult(
                null,
                listOf(sheetCompanionDiagnostic(sourcePath, expected, problem)),
            )
        }
        return when (val parsed = AthenaSheetCompanionParser().parse(companionPath.toString(), Files.readString(companionPath))) {
            is SheetCompanionParseSuccess -> SheetCompanionLoadResult(parsed.source, emptyList())
            is SheetCompanionParseFailure -> SheetCompanionLoadResult(
                null,
                parsed.diagnostics.map { diagnostic ->
                    sheetCompanionDiagnostic(
                        sourcePath = sourcePath,
                        companionPath = companionPath,
                        problem = diagnostic.message,
                    )
                },
            )
        }
    }

    private fun loadSheetStyleCompanion(source: CompilerSourceDocument): SheetStyleCompanionLoadResult? {
        val sourcePath = runCatching { Path.of(source.file) }.getOrNull() ?: return null
        val fileName = sourcePath.fileName?.toString() ?: return null
        val sheetPath = sourcePath.resolveSibling(fileName.removeSuffix(".athena") + ".sheet.athena")
        if (!Files.exists(sheetPath)) return null
        val location = SheetStyleCompanionLocator.locate(sheetPath)
        val stylePath = (location as? SheetCompanionFound)?.path ?: return null
        return when (val parsed = AthenaSheetStyleCompanionParser().parse(stylePath.toString(), Files.readString(stylePath))) {
            is SheetStyleCompanionParseSuccess -> SheetStyleCompanionLoadResult(parsed.source)
            else -> null
        }
    }

    private fun styleAnnotationSelections(
        style: SheetStyleCompanionSource?,
        connectionIr: com.engineeringood.athena.connection.ConnectionDocument?,
        projections: List<ProjectionDocument>,
    ): List<ConnectionAnnotationSelection> {
        if (style == null || connectionIr == null) return emptyList()
        val facts = connectionIr.connections.map { fact -> fact.id to (fact.id.value.substringAfterLast(':')) } +
            connectionIr.nets.map { fact -> fact.id to fact.name }
        val intents = style.styles.flatMap { it.annotations }
        return projections.flatMap { projection ->
            projection.sheets.flatMap { sheet ->
                intents.mapNotNull { intent ->
                    val fact = facts.firstOrNull { (_, name) -> name == intent.subjectName } ?: return@mapNotNull null
                    val role = when (intent.displayRole) {
                        "kind" -> ConnectionAnnotationDisplayRole.KIND
                        "potential-or-signal" -> ConnectionAnnotationDisplayRole.POTENTIAL_OR_SIGNAL
                        "specification" -> ConnectionAnnotationDisplayRole.SPECIFICATION
                        else -> return@mapNotNull null
                    }
                    ConnectionAnnotationSelection(
                        sheetId = sheet.sheetId.value,
                        semanticId = StableSemanticIdentity(fact.first.value),
                        displayRole = role,
                        sourceTrace = SpatialSourceTrace(
                            projectionIds = listOf(sheet.sheetId.value, fact.first.value),
                            geometryElementIds = listOf(GeometryElementId("annotation:${fact.first.value}")),
                        ),
                    )
                }
            }
        }.distinct()
    }

    private fun sheetCompanionDiagnostic(
        sourcePath: Path,
        companionPath: Path,
        problem: String,
    ): RealityTransformationDiagnostic = RealityTransformationDiagnostic(
        reality = "Sheet Companion",
        message = problem,
        subject = companionPath.fileName.toString(),
        problem = problem,
        correction = "Correct the Sheet Companion statement before publishing Spatial Reality.",
        sourceTrace = SpatialSourceTrace(
            projectionIds = listOf(sourcePath.toString(), companionPath.toString()),
            geometryElementIds = listOf(GeometryElementId("sheet-companion:${companionPath.fileName}")),
        ),
    )

    private fun applySheetCompanion(
        projection: com.engineeringood.athena.projection.ProjectionDocument,
        spatial: SpatialDocument,
        companion: com.engineeringood.athena.language.SheetCompanionSource?,
        connectionIr: com.engineeringood.athena.connection.ConnectionDocument? = null,
        annotationSelections: List<ConnectionAnnotationSelection> = emptyList(),
    ): ConstrainedSpatialResult {
        if (companion == null) return ConstrainedSpatialResult(spatial, emptyList())
        val sheet = projection.sheets.firstOrNull { candidate ->
            candidate.displayName == companion.name || candidate.sheetId.value == companion.name
        } ?: projection.sheets.singleOrNull()
        if (sheet == null) {
            return ConstrainedSpatialResult(
                null,
                listOf(
                    RealityTransformationDiagnostic(
                        reality = "Sheet Companion",
                        message = "Sheet Companion `${companion.name}` does not resolve to a Projection Sheet.",
                        subject = companion.name,
                        problem = "No matching Projection Sheet exists.",
                        correction = "Use the authored Projection Sheet name.",
                        sourceTrace = SpatialSourceTrace(listOf(companion.name), emptyList()),
                    ),
                ),
            )
        }
        val mapping = SheetCompanionProjectionPlacementMapper().map(companion, sheet, projection)
        val mappingDiagnostics = mapping.diagnostics.map { diagnostic ->
            RealityTransformationDiagnostic(
                reality = "Sheet Companion",
                message = diagnostic.message,
                subject = sheet.sheetId.value,
                problem = diagnostic.message,
                correction = "Use an occurrence label present on the Projection Sheet.",
                sourceTrace = SpatialSourceTrace(
                    projectionIds = listOf(sheet.sheetId.value),
                    geometryElementIds = listOf(sheet.originGeometryElementId),
                ),
            )
        }
        if (mappingDiagnostics.isNotEmpty()) return ConstrainedSpatialResult(null, mappingDiagnostics)
        val pageGeometries = projection.sheets.associate { candidate ->
            candidate.sheetId.value to SpatialPageGeometryProfiles.logical(
                grid = requireNotNull(candidate.grid),
                pageSize = candidate.publication.pageSize,
            )
        }
        return when (val constrained = ProjectionSpatialCompiler().transform(
            input = projection,
            placementConstraints = mapping.constraints,
            pageGeometries = pageGeometries,
            connectionIr = connectionIr,
            annotationSelections = annotationSelections,
        )) {
            is RealityTransformationResult.Success -> ConstrainedSpatialResult(constrained.output, emptyList())
            is RealityTransformationResult.Failure -> ConstrainedSpatialResult(
                null,
                constrained.diagnostics.map { diagnostic ->
                    RealityTransformationDiagnostic(
                        reality = diagnostic.reality,
                        message = diagnostic.message,
                        subject = diagnostic.subject,
                        problem = diagnostic.problem,
                        correction = diagnostic.correction,
                        sourceTrace = diagnostic.sourceTrace,
                    )
                },
            )
        }
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

private data class SheetCompanionLoadResult(
    val source: com.engineeringood.athena.language.SheetCompanionSource?,
    val diagnostics: List<RealityTransformationDiagnostic>,
)

private data class SheetStyleCompanionLoadResult(val source: SheetStyleCompanionSource)

private data class SheetProjectionIntentResult(
    val projection: com.engineeringood.athena.projection.ProjectionDocument,
    val diagnostics: List<RealityTransformationDiagnostic>,
)

private data class ConstrainedSpatialResult(
    val document: SpatialDocument?,
    val diagnostics: List<RealityTransformationDiagnostic>,
)

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
