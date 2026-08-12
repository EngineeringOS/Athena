package com.engineeringood.athena.compiler

import com.engineeringood.athena.presentation.AthenaDiagramScene
import com.engineeringood.athena.presentation.AthenaDiagramSceneContract
import com.engineeringood.athena.presentation.DecorationKind
import com.engineeringood.athena.presentation.InputRevision
import com.engineeringood.athena.presentation.PortDirection
import com.engineeringood.athena.presentation.PortDisplay
import com.engineeringood.athena.presentation.ResolvedStyle
import com.engineeringood.athena.presentation.RouteMarker
import com.engineeringood.athena.presentation.SceneBounds
import com.engineeringood.athena.presentation.SceneDecoration
import com.engineeringood.athena.presentation.SceneDigest
import com.engineeringood.athena.presentation.SceneElementId
import com.engineeringood.athena.presentation.SceneId
import com.engineeringood.athena.presentation.SceneLabel
import com.engineeringood.athena.presentation.SceneOccurrence
import com.engineeringood.athena.presentation.ScenePage
import com.engineeringood.athena.presentation.ScenePoint
import com.engineeringood.athena.presentation.ScenePlotFrame
import com.engineeringood.athena.presentation.ScenePort
import com.engineeringood.athena.presentation.SceneConnection
import com.engineeringood.athena.presentation.SceneConnectionAnnotation
import com.engineeringood.athena.presentation.SceneConnectionMarker
import com.engineeringood.athena.presentation.SceneConnectionMarkerKind
import com.engineeringood.athena.presentation.SceneConnectionSegment
import com.engineeringood.athena.presentation.SceneConnectionSegmentKind
import com.engineeringood.athena.presentation.SceneSnapGrid
import com.engineeringood.athena.presentation.SceneTrace
import com.engineeringood.athena.presentation.SceneDiagnostic
import com.engineeringood.athena.presentation.SourceOrigin
import com.engineeringood.athena.presentation.StyleId
import com.engineeringood.athena.presentation.TraceId
import com.engineeringood.athena.presentation.TraceRole
import com.engineeringood.athena.presentation.StrokeLineCap
import com.engineeringood.athena.presentation.StrokeLineJoin
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.ir.EngineeringPortDirection
import com.engineeringood.athena.spatial.SpatialAnchorPosition
import com.engineeringood.athena.spatial.SpatialDocument
import com.engineeringood.athena.spatial.SpatialOccurrenceGeometry
import com.engineeringood.athena.spatial.SpatialPoint
import com.engineeringood.athena.spatial.SpatialRect
import com.engineeringood.athena.spatial.SpatialReality
import com.engineeringood.athena.spatial.SpatialSourceTrace
import com.engineeringood.athena.semantics.core.SemanticDiagnosticSeverity
import com.engineeringood.athena.language.SheetStyleCompanionSource
import com.engineeringood.athena.language.SheetStyleIntent
import com.engineeringood.athena.language.SheetCompanionSource
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.security.MessageDigest

data class AthenaDiagramSceneCompilationResult(
    val scene: AthenaDiagramScene? = null,
    val diagnostics: List<SceneDiagnostic> = emptyList(),
)

/** Lowers validated Projection + Spatial facts into the one renderer-neutral Presentation Reality scene. */
class AthenaDiagramSceneCompiler {
    /**
     * Compiles only a validated compiler result. Presentation must never publish a scene when an
     * upstream pass produced an error or when canonical Projection/Spatial cardinality is unclear.
     */
    fun compile(
        compilation: CompilerCompilationSuccess,
        inputRevision: InputRevision,
        activeSheetId: String? = null,
        styleCompanion: SheetStyleCompanionSource? = null,
        pageCompanionPath: String? = null,
    ): AthenaDiagramSceneCompilationResult {
        val semanticError = compilation.semanticResult.diagnostics.firstOrNull { diagnostic ->
            diagnostic.severity == SemanticDiagnosticSeverity.ERROR
        }
        if (semanticError != null) {
            return failure(
                subject = semanticError.subjectIdentity?.value ?: "Engineering Reality",
                correction = "Correct engineering diagnostics before publishing Presentation Reality.",
                problem = semanticError.message,
            )
        }
        compilation.projectionDiagnostics.firstOrNull()?.let { diagnostic ->
            return failure(
                subject = "Projection Reality",
                correction = "Correct Projection diagnostics before publishing Presentation Reality.",
                problem = diagnostic,
            )
        }
        compilation.realityTransformationDiagnostics.firstOrNull()?.let { diagnostic ->
            return failure(
                subject = diagnostic.subject ?: "Spatial Reality",
                correction = diagnostic.correction ?: "Correct Spatial diagnostics before publishing Presentation Reality.",
                problem = diagnostic.problem ?: diagnostic.message,
            )
        }
        val projection = compilation.projections.singleOrNull()
            ?: return failure(
                subject = "Projection Reality",
                correction = "Publish exactly one canonical Projection document for the active Sheet.",
                problem = "Expected one Projection document but found ${compilation.projections.size}.",
            )
        val spatial = compilation.spatialDocuments.singleOrNull()
            ?: return failure(
                subject = "Spatial Reality",
                correction = "Publish exactly one canonical Spatial document for the active Sheet.",
                problem = "Expected one Spatial document but found ${compilation.spatialDocuments.size}.",
            )
        val sheet = when {
            activeSheetId != null -> {
                val canonicalSheetId = projection.sheets.singleOrNull { candidate ->
                    candidate.sheetId.value == activeSheetId || candidate.displayName == activeSheetId
                }?.sheetId?.value ?: activeSheetId
                spatial.sheets.singleOrNull { it.sheetId == canonicalSheetId }
            }
            spatial.sheets.size == 1 -> spatial.sheets.single()
            else -> null
        } ?: return failure(
            subject = "Folio Page",
            correction = "Select one published Folio Page before opening the engineering document.",
            problem = "No unique active Spatial Sheet is available.",
        )
        val projectionSheet = projection.sheets.singleOrNull { it.sheetId.value == sheet.sheetId }
            ?: return failure(
                subject = "Folio Page",
                correction = "Publish one Projection Sheet for active Spatial Sheet `${sheet.sheetId}`.",
                problem = "Active Spatial Sheet `${sheet.sheetId}` has no Projection Sheet.",
            )
        val companion = compilation.sheetCompanions.singleOrNull { it.name == projectionSheet.displayName }
            ?: return failure(
                subject = "Folio Page",
                correction = "Publish one Page Companion for active Sheet `${sheet.sheetId}`.",
                problem = "Active Spatial Sheet `${sheet.sheetId}` has no Page Companion.",
            )
        return compile(
            projection = projection,
            spatial = SpatialDocument(listOf(sheet)),
            inputRevision = inputRevision,
            sourcePaths = SceneTraceSourcePaths.from(compilation.source.file, pageCompanionPath),
            portDirections = compilation.document.ports.associate { port -> port.id.value to port.direction.toScenePortDirection() },
            styleCompanion = styleCompanion,
            sheetCompanion = companion,
        )
    }

    fun compile(
        projection: ProjectionDocument,
        spatial: SpatialDocument?,
        inputRevision: InputRevision,
    ): AthenaDiagramSceneCompilationResult = compile(projection, spatial, inputRevision, null, emptyMap())

    private fun compile(
        projection: ProjectionDocument,
        spatial: SpatialDocument?,
        inputRevision: InputRevision,
        sourcePaths: SceneTraceSourcePaths?,
        portDirections: Map<String, PortDirection>,
        styleCompanion: SheetStyleCompanionSource? = null,
        sheetCompanion: SheetCompanionSource? = null,
    ): AthenaDiagramSceneCompilationResult {
        if (spatial == null || spatial.sheets.isEmpty()) {
            return failure("Spatial Reality is unavailable.", "Compile a valid Sheet Companion and Spatial document before publishing Presentation Reality.")
        }
        val spatialDiagnostics = SpatialReality.validate(spatial).diagnostics
        if (spatialDiagnostics.isNotEmpty()) {
            val diagnostic = spatialDiagnostics.first()
            return failure(diagnostic.subject, diagnostic.correction, diagnostic.problem)
        }
        return runCatching {
            val styleCascade = StyleCascade(styleCompanion)
            val defaultStyle = styleCascade.forRole("default")
            val sheets = spatial.sheets.sortedWith(compareBy({ it.sheetId }, { it.extent.x }, { it.extent.y }))
            require(sheets.size == 1) { "Presentation scene requires one active Spatial Sheet." }
            val sheet = sheets.single()
            val projectionNodes = projection.nodes.associateBy { it.projectionId.value }
            val page = ScenePage(sheet.extent.toSceneBounds(), sheet.drawingArea.toSceneBounds())
            val plotFrame = ScenePlotFrame(
                columns = sheetCompanion?.frame?.columns ?: sheet.grid.columns,
                rows = sheetCompanion?.frame?.rows ?: sheet.grid.rows,
            )
            val snapGrid = SceneSnapGrid(
                sheetId = sheetCompanion?.name ?: sheet.sheetId,
                step = sheetCompanion?.snap?.step ?: sheet.grid.subdivisions,
                drawingOrigin = ScenePoint(sheet.drawingArea.x, sheet.drawingArea.y),
            )
            val traces = linkedMapOf<String, SceneTrace>()
            val occurrences = sheet.occurrences.sortedBy { it.occurrenceId.projectionId }.map { occurrence ->
                occurrence.toSceneOccurrence(
                    sheet.sheetId,
                    projectionNodes,
                    sheet.anchors,
                    styleCascade.forOccurrence(occurrence.occurrenceId.projectionId, "symbol"),
                    styleCascade.forRole("port"),
                    styleCascade.forRole("label"),
                    traces,
                    sourcePaths,
                    portDirections,
                )
            }
            val connections = sheet.routes.sortedBy { it.routeId.value }.map { route ->
                val trace = trace(route.sourceTrace, TraceRole.RELATIONSHIP_DECLARATION, route.connectionId.value, traces, sourcePaths)
                val shared = sheet.connectionTopology.sharedSegments.filter { route.routeId in it.routeIds }
                val segments = route.segments.mapIndexed { index, segment ->
                    val sharedSegment = shared.firstOrNull { it.segment == segment }
                    SceneConnectionSegment(
                        segmentId = sharedSegment?.id?.value ?: "${route.routeId.value}:$index",
                        start = segment.start.toScenePoint(),
                        end = segment.end.toScenePoint(),
                        kind = if (sharedSegment == null) SceneConnectionSegmentKind.ORTHOGONAL else SceneConnectionSegmentKind.SHARED,
                    )
                }
                val markers = buildList {
                    sheet.connectionTopology.junctions.filter { route.routeId == it.routeIds.first() }.forEach { junction ->
                        val markerTrace = trace(junction.sourceTrace, TraceRole.SPATIAL_DERIVATION, junction.id.value, traces, sourcePaths)
                        add(SceneConnectionMarker(elementId("junction", junction.id.value), SceneConnectionMarkerKind.JUNCTION, junction.point.toScenePoint(), junction.routeIds.map { it.value }, traceId = markerTrace.traceId))
                    }
                    sheet.connectionTopology.crossings.filter { route.routeId == it.bridgeOwnerRouteId }.forEach { crossing ->
                        val markerTrace = trace(crossing.sourceTrace, TraceRole.SPATIAL_DERIVATION, crossing.id.value, traces, sourcePaths)
                        add(SceneConnectionMarker(elementId("crossing", crossing.id.value), SceneConnectionMarkerKind.CROSSING, crossing.point.toScenePoint(), crossing.routeIds.map { it.value }, bridgeOwner = crossing.bridgeOwnerRouteId == route.routeId, traceId = markerTrace.traceId))
                    }
                    sheet.connectionTopology.interruptions.filter { it.semanticId == route.connectionId && sheet.routes.filter { routePlan -> routePlan.connectionId == route.connectionId }.minByOrNull { routePlan -> routePlan.routeId.value }?.routeId == route.routeId }.forEach { interruption ->
                        val markerTrace = trace(interruption.sourceTrace, TraceRole.SPATIAL_DERIVATION, interruption.id.value, traces, sourcePaths)
                        interruption.points.forEachIndexed { index, point ->
                            add(SceneConnectionMarker(elementId("interruption", "${interruption.id.value}:$index"), if (index == 0) SceneConnectionMarkerKind.INTERRUPTION_START else SceneConnectionMarkerKind.INTERRUPTION_END, point.toScenePoint(), interruption.continuationIds, traceId = markerTrace.traceId))
                        }
                    }
                }
                val ownsSemanticDecorations = sheet.routes.filter { it.connectionId == route.connectionId }.minByOrNull { it.routeId.value }?.routeId == route.routeId
                val annotations = sheet.annotations.filter { ownsSemanticDecorations && it.semanticId == route.connectionId }.map { annotation ->
                    val annotationTrace = trace(annotation.sourceTrace, TraceRole.RELATIONSHIP_DECLARATION, annotation.id.value, traces, sourcePaths)
                    SceneConnectionAnnotation(elementId("annotation", annotation.id.value), annotation.semanticId.value, annotation.displayRole.name, annotation.value, annotation.anchor.toScenePoint(), annotation.bounds.toSceneBounds(), annotationTrace.traceId)
                }
                SceneConnection(
                    elementId = elementId("connection", route.routeId.value),
                    connectionId = route.connectionId.value,
                    projectionId = route.projectionConnectionId,
                    sourceAnchorId = route.sourceAnchorId.value,
                    targetAnchorId = route.targetAnchorId.value,
                    segments = segments,
                    markers = markers,
                    annotations = annotations,
                    zIndex = 50,
                    styleId = styleCascade.forRole("connection").styleId,
                    traceId = trace.traceId,
                )
            }
            val decorations = decorations(
                page.pageBounds,
                defaultStyle,
                traces,
                sheet.sourceTrace,
                sourcePaths,
            )
            val scene = AthenaDiagramScene(
                sceneId = sceneId(sheets.map { it.sheetId }),
                inputRevision = inputRevision,
                sceneDigest = SceneDigest.uncomputed(),
                page = page,
                plotFrame = plotFrame,
                snapGrid = snapGrid,
                styles = styleCascade.styles(),
                assets = emptyList(),
                occurrences = occurrences,
                connections = connections,
                decorations = decorations,
                traces = traces.values.toList(),
            ).let(AthenaDiagramSceneContract::canonicalize)
            AthenaDiagramSceneContract.validate(scene)
            AthenaDiagramSceneCompilationResult(scene = scene)
        }.getOrElse { error ->
            failure(
                "Presentation scene compilation failed.",
                error.message ?: "Correct the Spatial facts before publishing Presentation Reality.",
            )
        }
    }

    private fun failure(subject: String, correction: String, problem: String = subject) = AthenaDiagramSceneCompilationResult(
        diagnostics = listOf(SceneDiagnostic(subject, problem, correction, "presentation.scene.unavailable")),
    )

    private fun SpatialOccurrenceGeometry.toSceneOccurrence(
        sheetId: String,
        projectionNodes: Map<String, com.engineeringood.athena.projection.ProjectionNode>,
        anchors: List<SpatialAnchorPosition>,
        occurrenceStyle: ResolvedStyle,
        portStyle: ResolvedStyle,
        labelStyle: ResolvedStyle,
        traces: MutableMap<String, SceneTrace>,
        sourcePaths: SceneTraceSourcePaths?,
        portDirections: Map<String, PortDirection>,
    ): SceneOccurrence {
        val occurrenceAnchors = anchors.filter { it.subject.occurrenceId == occurrenceId }.sortedBy { it.subject.portId.value }
        val occurrenceTrace = trace(sourceTrace, TraceRole.SEMANTIC_DECLARATION, subjectId.value, traces, sourcePaths)
        val node = projectionNodes[occurrenceId.projectionId]
        val bounds = rectangle.toSceneBounds()
        val ports = occurrenceAnchors.map { anchor ->
            val portTrace = trace(anchor.sourceTrace, TraceRole.PORT_DECLARATION, anchor.subject.portId.value, traces, sourcePaths)
            ScenePort(
                elementId = elementId("port", "$sheetId/${occurrenceId.projectionId}/${anchor.subject.portId.value}"),
                anchorId = anchor.anchorId.value,
                semanticPortId = anchor.subject.portId.value,
                point = anchor.point.toScenePoint(),
                hitRadius = 1,
                direction = portDirections[anchor.subject.portId.value] ?: PortDirection.BIDIRECTIONAL,
                styleId = portStyle.styleId,
                traceId = portTrace.traceId,
            )
        }
        val labels = node?.let { projected ->
            val labelTrace = trace(sourceTrace, TraceRole.SEMANTIC_DECLARATION, projected.semanticId.value, traces, sourcePaths)
            val displayLabel = projected.label.substringBefore('.').ifBlank { projected.label }.take(12)
            listOf(
                SceneLabel(
                    elementId = elementId("label", occurrenceId.projectionId),
                    role = "occurrence",
                    text = displayLabel,
                    anchor = ScenePoint(bounds.x, bounds.y - 2),
                    bounds = SceneBounds(bounds.x, bounds.y - 2, displayLabel.length.coerceAtLeast(1), 1),
                    rotationDegrees = 0,
                    styleId = labelStyle.styleId,
                    traceId = labelTrace.traceId,
                ),
            )
        }.orEmpty()
        return SceneOccurrence(
            elementId = elementId("occurrence", "$sheetId/${occurrenceId.projectionId}"),
            occurrenceId = occurrenceId.projectionId,
            subjectId = subjectId.value,
            semanticId = subjectId.value,
            representationRef = null,
            bounds = bounds,
            placementAnchor = occurrenceAnchors.firstOrNull()?.point?.toScenePoint() ?: ScenePoint(bounds.x, bounds.y),
            zIndex = 100,
            styleId = occurrenceStyle.styleId,
            traceId = occurrenceTrace.traceId,
            ports = ports,
            labels = labels,
        )
    }

    private fun decorations(
        page: SceneBounds,
        style: ResolvedStyle,
        traces: MutableMap<String, SceneTrace>,
        sourceTrace: SpatialSourceTrace,
        sourcePaths: SceneTraceSourcePaths?,
    ): List<SceneDecoration> {
        val trace = trace(sourceTrace, TraceRole.SHEET_DECLARATION, "sheet", traces, sourcePaths)
        require(page.width > 1 && page.height > 1) { "Page frame requires positive width and height." }
        return listOf(
            decoration("background", DecorationKind.PAGE_BACKGROUND, page, -100, style, trace, null),
            decoration("frame-top", DecorationKind.FRAME_SEGMENT, SceneBounds(page.x, page.y, page.width, 1), -90, style, trace, null),
            decoration("frame-right", DecorationKind.FRAME_SEGMENT, SceneBounds(page.x + page.width - 1, page.y, 1, page.height), -90, style, trace, null),
            decoration("frame-bottom", DecorationKind.FRAME_SEGMENT, SceneBounds(page.x, page.y + page.height - 1, page.width, 1), -90, style, trace, null),
            decoration("frame-left", DecorationKind.FRAME_SEGMENT, SceneBounds(page.x, page.y, 1, page.height), -90, style, trace, null),
        )
    }

    private fun decoration(key: String, kind: DecorationKind, bounds: SceneBounds, z: Int, style: ResolvedStyle, trace: SceneTrace, text: String?) = SceneDecoration(
        elementId = elementId("decoration", key), kind = kind, bounds = bounds, zIndex = z, styleId = style.styleId, traceId = trace.traceId, text = text,
    )

    private fun trace(
        source: SpatialSourceTrace,
        role: TraceRole,
        subject: String,
        traces: MutableMap<String, SceneTrace>,
        sourcePaths: SceneTraceSourcePaths?,
    ): SceneTrace {
        val traceId = TraceId(id("trace", "$role|$subject|${source.projectionIds}|${source.geometryElementIds.map { it.value }}"))
        return traces.getOrPut(traceId.value) {
            SceneTrace(
                traceId,
                buildList {
                    SourceOrigin(
                        relativePath = sourcePaths?.pathFor(role) ?: "spatial/${source.projectionIds.firstOrNull() ?: subject}",
                        sourceDigest = sha((source.projectionIds + source.geometryElementIds.map { it.value }).joinToString("|")),
                        role = role,
                        startLine = 0,
                        startCharacter = 0,
                        endLine = 0,
                        endCharacter = 0,
                        subjectId = subject,
                        primary = true,
                    ).also(::add)
                    relatedEntitySubject(subject)?.let { entitySubject ->
                        add(
                            SourceOrigin(
                                relativePath = sourcePaths?.pathFor(TraceRole.SEMANTIC_DECLARATION)
                                    ?: "spatial/${source.projectionIds.firstOrNull() ?: entitySubject}",
                                sourceDigest = sha(entitySubject),
                                role = TraceRole.SEMANTIC_DECLARATION,
                                startLine = 0,
                                startCharacter = 0,
                                endLine = 0,
                                endCharacter = 0,
                                subjectId = entitySubject,
                                primary = false,
                            ),
                        )
                    }
                    source.projectionIds.mapNotNull(::sheetPlacementLine).forEach { line ->
                        add(
                            SourceOrigin(
                                relativePath = sourcePaths?.sheetCompanionPath ?: "sheet-companion",
                                sourceDigest = sha("sheet-companion:$line"),
                                role = TraceRole.SHEET_PLACEMENT,
                                startLine = line - 1,
                                startCharacter = 0,
                                endLine = line - 1,
                                endCharacter = 0,
                                subjectId = subject,
                                primary = false,
                            ),
                        )
                    }
                },
            )
        }
    }

    private fun SceneBounds.toPoint() = ScenePoint(x, y)
    private fun SpatialRect.toSceneBounds() = SceneBounds(x, y, width, height)
    private fun SpatialPoint.toScenePoint() = ScenePoint(x, y)

    private fun relatedEntitySubject(subject: String): String? {
        if (!subject.startsWith("function:")) return null
        val entityName = subject.removePrefix("function:").substringBefore('.', missingDelimiterValue = "")
        return entityName.takeIf(String::isNotBlank)?.let { "entity:$it" }
    }

    private fun sceneId(sheets: List<String>) = SceneId(id("scene", sheets.sorted().joinToString("|")))
    private fun elementId(prefix: String, key: String) = SceneElementId(id(prefix, key))
    private fun id(prefix: String, key: String) = "$prefix:sha256:${sha(key)}"
    private fun sha(value: String): String = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(StandardCharsets.UTF_8)).joinToString("") { "%02x".format(it) }
}

private fun EngineeringPortDirection.toScenePortDirection(): PortDirection = when (this) {
    EngineeringPortDirection.INPUT -> PortDirection.IN
    EngineeringPortDirection.OUTPUT -> PortDirection.OUT
    EngineeringPortDirection.BIDIRECTIONAL -> PortDirection.BIDIRECTIONAL
}

private class StyleCascade(styleCompanion: SheetStyleCompanionSource?) {
    private val declarations = styleCompanion?.styles?.associateBy { it.name }.orEmpty()
    private val resolved = linkedMapOf<String, ResolvedStyle>()

    fun forRole(role: String): ResolvedStyle {
        if (role != "default" && declarations[role] == null) return forRole("default")
        return resolved.getOrPut(role) {
            val parent = if (role == "default") baseStyle() else forRole("default")
            resolve(role, parent, declarations[role])
        }
    }

    fun forOccurrence(occurrenceId: String, role: String): ResolvedStyle {
        val key = "occurrence:$occurrenceId"
        val declaration = declarations[key] ?: return forRole(role)
        return resolved.getOrPut(key) { resolve(key, forRole(role), declaration) }
    }

    fun styles(): List<ResolvedStyle> = resolved.values.toList()

    private fun baseStyle() = ResolvedStyle(
        styleId = StyleId("style:sha256:${"0".repeat(64)}"),
        strokeRgba = "#000000ff",
        fillRgba = "#ffffffff",
        strokeWidth = 1,
    )

    private fun resolve(role: String, parent: ResolvedStyle, declaration: SheetStyleIntent?): ResolvedStyle {
        val style = parent.copy(
            styleId = StyleId(
                "style:sha256:${sha(
                    listOf(
                        role,
                        declaration?.strokeRgba ?: parent.strokeRgba,
                        declaration?.fillRgba ?: parent.fillRgba,
                        declaration?.strokeWidth ?: parent.strokeWidth,
                        declaration?.dash ?: parent.dash,
                        declaration?.lineCap ?: parent.lineCap.name,
                        declaration?.lineJoin ?: parent.lineJoin.name,
                        declaration?.opacity ?: parent.opacity,
                        declaration?.fontSize ?: parent.fontSize,
                        declaration?.fontWeight ?: parent.fontWeight,
                        declaration?.routeMarker ?: parent.routeMarker.name,
                        declaration?.portDisplay ?: parent.portDisplay.name,
                    ).joinToString("|")
                )}",
            ),
            strokeRgba = declaration?.strokeRgba ?: parent.strokeRgba,
            fillRgba = declaration?.fillRgba ?: parent.fillRgba,
            strokeWidth = declaration?.strokeWidth ?: parent.strokeWidth,
            dash = declaration?.dash ?: parent.dash,
            lineCap = declaration?.lineCap?.toLineCap() ?: parent.lineCap,
            lineJoin = declaration?.lineJoin?.toLineJoin() ?: parent.lineJoin,
            opacity = declaration?.opacity ?: parent.opacity,
            fontSize = declaration?.fontSize ?: parent.fontSize,
            fontWeight = declaration?.fontWeight ?: parent.fontWeight,
            routeMarker = declaration?.routeMarker?.toRouteMarker() ?: parent.routeMarker,
            portDisplay = declaration?.portDisplay?.toPortDisplay() ?: parent.portDisplay,
        )
        return style.also { resolved[role] = it }
    }

    private fun String.toLineCap() = when (lowercase()) {
        "butt" -> StrokeLineCap.BUTT
        "round" -> StrokeLineCap.ROUND
        "square" -> StrokeLineCap.SQUARE
        else -> error("Unsupported Style Companion cap `$this`.")
    }

    private fun String.toLineJoin() = when (lowercase()) {
        "miter" -> StrokeLineJoin.MITER
        "round" -> StrokeLineJoin.ROUND
        "bevel" -> StrokeLineJoin.BEVEL
        else -> error("Unsupported Style Companion join `$this`.")
    }

    private fun String.toRouteMarker() = when (lowercase()) {
        "none" -> RouteMarker.NONE
        "end-arrow" -> RouteMarker.END_ARROW
        else -> error("Unsupported Style Companion route marker `$this`.")
    }

    private fun String.toPortDisplay() = when (lowercase()) {
        "hidden" -> PortDisplay.HIDDEN
        "marker" -> PortDisplay.MARKER
        "marker-and-label" -> PortDisplay.MARKER_AND_LABEL
        else -> error("Unsupported Style Companion port display `$this`.")
    }

    private fun sha(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(StandardCharsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
}

private data class SceneTraceSourcePaths(
    val sourcePath: String,
    val sheetCompanionPath: String,
) {
    fun pathFor(role: TraceRole): String = when (role) {
        TraceRole.SHEET_DECLARATION,
        TraceRole.SHEET_PLACEMENT,
            -> sheetCompanionPath
        else -> sourcePath
    }

    companion object {
        fun from(sourceFile: String, pageCompanionFile: String? = null): SceneTraceSourcePaths {
            val sourcePath = runCatching { Path.of(sourceFile).normalize() }.getOrNull()
            if (sourcePath == null || sourcePath.fileName == null) {
                val fallback = sourceFile.ifBlank { "source.athena" }.replace('\\', '/')
                return SceneTraceSourcePaths(
                    sourcePath = fallback,
                    sheetCompanionPath = pageCompanionFile ?: fallback.removeSuffix(".athena") + ".page.sheet.athena",
                )
            }
            return SceneTraceSourcePaths(
                sourcePath = sourcePath.portableSourcePath(),
                sheetCompanionPath = pageCompanionFile?.let { Path.of(it).portableSourcePath() }
                    ?: sourcePath.resolveSibling(sourcePath.fileName.toString().removeSuffix(".athena") + ".page.sheet.athena").portableSourcePath(),
            )
        }
    }
}

private fun Path.portableSourcePath(): String {
    val segments = iterator().asSequence().map(Path::toString).toList()
    val sourceRoot = segments.indexOfLast { segment -> segment == "src" }
    return (if (sourceRoot >= 0) segments.drop(sourceRoot) else segments.takeLast(1))
        .joinToString("/")
        .ifBlank { "source.athena" }
}

private fun sheetPlacementLine(value: String): Int? {
    if (!value.startsWith("sheet-companion:")) return null
    val separator = value.lastIndexOf(':')
    if (separator <= 0 || separator == value.lastIndex) return null
    return value.substring(separator + 1).toIntOrNull()?.takeIf { line -> line > 0 }
}
