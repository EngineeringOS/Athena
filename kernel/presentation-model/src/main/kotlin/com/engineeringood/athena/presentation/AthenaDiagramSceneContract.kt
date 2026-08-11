package com.engineeringood.athena.presentation

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

object AthenaDiagramSceneContract {
    fun canonicalize(scene: AthenaDiagramScene): AthenaDiagramScene {
        val ordered = scene.copy(
            sceneDigest = SceneDigest.uncomputed(),
            styles = scene.styles.sortedBy { it.styleId.value },
            assets = scene.assets.sortedBy { it.assetId.value },
            occurrences = scene.occurrences.sortedWith(compareBy(SceneOccurrence::zIndex, { it.elementId.value })).map { occurrence ->
                occurrence.copy(
                    ports = occurrence.ports.sortedWith(compareBy(ScenePort::semanticPortId, ScenePort::anchorId, { it.elementId.value })),
                    labels = occurrence.labels.sortedWith(compareBy(SceneLabel::role, { it.elementId.value })),
                )
            },
            connections = scene.connections.sortedWith(compareBy(SceneConnection::zIndex, SceneConnection::connectionId, { it.elementId.value })).map { connection ->
                connection.copy(
                    markers = connection.markers.sortedWith(compareBy({ it.kind.ordinal }, { it.point.x }, { it.point.y }, { it.elementId.value })).map { marker ->
                        marker.copy(relatedConnectionIds = marker.relatedConnectionIds.distinct().sorted())
                    },
                    annotations = connection.annotations.sortedWith(compareBy(SceneConnectionAnnotation::semanticId, SceneConnectionAnnotation::displayRole, { it.elementId.value })),
                )
            },
            decorations = scene.decorations.sortedWith(compareBy(SceneDecoration::zIndex, { it.kind.ordinal }, { it.elementId.value })),
            traces = scene.traces.sortedBy { it.traceId.value }.map { trace ->
                trace.copy(origins = trace.origins.sortedWith(compareBy({ it.role.ordinal }, SourceOrigin::relativePath, SourceOrigin::startLine, SourceOrigin::startCharacter, SourceOrigin::endLine, SourceOrigin::endCharacter, SourceOrigin::subjectId)))
            },
        )
        validate(ordered, verifyDigest = false)
        return ordered.copy(sceneDigest = digest(ordered))
    }

    fun digest(scene: AthenaDiagramScene): SceneDigest {
        val bytes = CanonicalJson.write(sceneMap(scene.copy(sceneDigest = SceneDigest.uncomputed()))).toByteArray(StandardCharsets.UTF_8)
        val hex = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
        return SceneDigest("sha256:$hex")
    }

    fun validate(scene: AthenaDiagramScene) = validate(scene, verifyDigest = true)

    /** Validates one immutable publication: scene and admitted bytes share one revision and coverage. */
    fun validate(scene: AthenaDiagramScene, bundle: AssetBundle) {
        validate(scene)
        require(scene.inputRevision == bundle.inputRevision) { "Scene and asset bundle revisions must match." }
        val sceneEntries = scene.assets.map { it.bundleEntryId }
        require(sceneEntries.size == sceneEntries.distinct().size) { "Scene asset bundle entry ids must be unique." }
        require(bundle.entries.map { it.entryId }.toSet() == sceneEntries.toSet()) {
            "Asset bundle entries must exactly cover scene assets."
        }
        scene.assets.forEach { asset ->
            val entry = bundle.entries.single { it.entryId == asset.bundleEntryId }
            require(entry.digest == asset.digest) { "Asset `${asset.assetId.value}` digest does not match its bundle entry." }
            require(asset.digest == "sha256:${sha256(entry.bytes)}") { "Asset `${asset.assetId.value}` bytes are not digest verified." }
            require(PresentationAssetIdentity.assetId(asset.mediaKind, asset.profileId, entry.bytes) == asset.assetId) {
                "Asset `${asset.assetId.value}` identity does not match admitted bytes."
            }
        }
    }

    private fun validate(scene: AthenaDiagramScene, verifyDigest: Boolean) {
        require(scene.snapGrid.drawingOrigin == ScenePoint(scene.page.drawingBounds.x, scene.page.drawingBounds.y))
        requireUnique(scene.styles.map { it.styleId.value }, "style ids")
        requireUnique(scene.assets.map { it.assetId.value }, "asset ids")
        requireUnique(scene.traces.map { it.traceId.value }, "trace ids")
        val elements = buildList {
            scene.occurrences.forEach { occurrence -> add(occurrence.elementId.value); addAll(occurrence.ports.map { it.elementId.value }); addAll(occurrence.labels.map { it.elementId.value }) }
            addAll(scene.connections.map { it.elementId.value })
            scene.connections.forEach { connection ->
                addAll(connection.markers.map { it.elementId.value })
                addAll(connection.annotations.map { it.elementId.value })
            }
            addAll(scene.decorations.map { it.elementId.value })
        }
        requireUnique(elements, "scene element ids")
        val styleIds = scene.styles.mapTo(mutableSetOf()) { it.styleId }
        val traceIds = scene.traces.mapTo(mutableSetOf()) { it.traceId }
        val assetIds = scene.assets.mapTo(mutableSetOf()) { it.assetId }
        val assetsById = scene.assets.associateBy { it.assetId }
        scene.styles.forEach { style ->
            style.fontAssetId?.let { fontAssetId ->
                require(assetsById[fontAssetId]?.mediaKind == AssetMediaKind.WOFF2) {
                    "Style font assets must reference admitted WOFF2 assets."
                }
            }
        }
        scene.occurrences.forEach { occurrence ->
            require(occurrence.styleId in styleIds && occurrence.traceId in traceIds && (occurrence.assetId == null || occurrence.assetId in assetIds))
            occurrence.ports.forEach { require(it.styleId in styleIds && it.traceId in traceIds) }
            occurrence.labels.forEach { require(it.styleId in styleIds && it.traceId in traceIds) }
        }
        scene.connections.forEach { connection ->
            require(connection.styleId in styleIds && connection.traceId in traceIds)
            connection.markers.forEach { require(it.traceId in traceIds) }
            connection.annotations.forEach { require(it.traceId in traceIds) }
        }
        scene.decorations.forEach { require(it.styleId in styleIds && it.traceId in traceIds) }
        val tracesById = scene.traces.associateBy { it.traceId }
        scene.assets.forEach { asset ->
            require(asset.traceId in traceIds)
            val trace = tracesById.getValue(asset.traceId)
            require(trace.origins.all { it.role == TraceRole.ASSET_DEFINITION }) {
                "Asset traces may contain only ASSET_DEFINITION origins."
            }
            require(trace.origins.single { it.primary }.subjectId == asset.assetId.value) {
                "Asset primary trace subject must be its content-addressed asset id."
            }
        }
        if (verifyDigest && scene.sceneDigest != SceneDigest.uncomputed()) require(scene.sceneDigest == digest(scene))
    }

    private fun requireUnique(values: List<String>, label: String) { require(values.size == values.distinct().size) { "Duplicate $label" } }

    private fun sceneMap(scene: AthenaDiagramScene): Map<String, Any?> = buildMap {
        put("schemaVersion", scene.schemaVersion); put("sceneId", scene.sceneId.value); put("inputRevision", scene.inputRevision.value)
        put("page", mapOf("pageBounds" to bounds(scene.page.pageBounds), "drawingBounds" to bounds(scene.page.drawingBounds)))
        put("plotFrame", mapOf("columns" to scene.plotFrame.columns, "rows" to scene.plotFrame.rows, "columnLabels" to scene.plotFrame.columnLabels.name, "rowLabels" to scene.plotFrame.rowLabels.name))
        put("snapGrid", mapOf("sheetId" to scene.snapGrid.sheetId, "step" to scene.snapGrid.step, "drawingOrigin" to point(scene.snapGrid.drawingOrigin), "formulaVersion" to scene.snapGrid.formulaVersion))
        put("styles", scene.styles.map { mapOf("styleId" to it.styleId.value, "strokeRgba" to it.strokeRgba, "fillRgba" to it.fillRgba, "strokeWidth" to it.strokeWidth, "dash" to it.dash, "lineCap" to it.lineCap.name, "lineJoin" to it.lineJoin.name, "fillRule" to it.fillRule.name, "opacity" to it.opacity, "fontAssetId" to it.fontAssetId?.value, "fontSize" to it.fontSize, "fontWeight" to it.fontWeight, "textAlign" to it.textAlign.name, "textBaseline" to it.textBaseline.name, "routeMarker" to it.routeMarker.name, "portDisplay" to it.portDisplay.name).filterValues { it != null } })
        put("assets", scene.assets.map { buildMap<String, Any?> { put("assetId", it.assetId.value); put("digest", it.digest); put("mediaKind", it.mediaKind.name); put("profileId", it.profileId); it.intrinsicBounds?.let { value -> put("intrinsicBounds", bounds(value)) }; put("bundleEntryId", it.bundleEntryId); put("traceId", it.traceId.value) } })
        put("occurrences", scene.occurrences.map { occurrence -> buildMap<String, Any?> { put("elementId", occurrence.elementId.value); put("occurrenceId", occurrence.occurrenceId); put("subjectId", occurrence.subjectId); put("semanticId", occurrence.semanticId); occurrence.representationRef?.let { put("representationRef", it) }; put("bounds", bounds(occurrence.bounds)); put("placementAnchor", point(occurrence.placementAnchor)); put("zIndex", occurrence.zIndex); put("styleId", occurrence.styleId.value); put("traceId", occurrence.traceId.value); occurrence.assetId?.let { put("assetId", it.value) }; put("ports", occurrence.ports.map(::portMap)); put("labels", occurrence.labels.map(::labelMap)) } })
        put("connections", scene.connections.map { connection -> buildMap<String, Any?> {
            put("elementId", connection.elementId.value); put("connectionId", connection.connectionId); put("projectionId", connection.projectionId); put("sourceAnchorId", connection.sourceAnchorId); put("targetAnchorId", connection.targetAnchorId)
            put("segments", connection.segments.map { segment -> mapOf("segmentId" to segment.segmentId, "start" to point(segment.start), "end" to point(segment.end), "kind" to segment.kind.name) })
            put("markers", connection.markers.map { marker -> mapOf("elementId" to marker.elementId.value, "kind" to marker.kind.name, "point" to point(marker.point), "relatedConnectionIds" to marker.relatedConnectionIds, "bridgeOwner" to marker.bridgeOwner, "traceId" to marker.traceId.value) })
            put("annotations", connection.annotations.map { annotation -> mapOf("elementId" to annotation.elementId.value, "semanticId" to annotation.semanticId, "displayRole" to annotation.displayRole, "value" to annotation.value, "anchor" to point(annotation.anchor), "bounds" to bounds(annotation.bounds), "traceId" to annotation.traceId.value) })
            put("zIndex", connection.zIndex); put("styleId", connection.styleId.value); put("traceId", connection.traceId.value)
        } })
        put("decorations", scene.decorations.map { decoration -> buildMap<String, Any?> { put("elementId", decoration.elementId.value); put("kind", decoration.kind.name); put("bounds", bounds(decoration.bounds)); put("zIndex", decoration.zIndex); put("styleId", decoration.styleId.value); put("traceId", decoration.traceId.value); decoration.text?.let { put("text", it) } } })
        put("traces", scene.traces.map { trace -> mapOf("traceId" to trace.traceId.value, "origins" to trace.origins.map(::originMap)) })
    }

    private fun point(value: ScenePoint) = mapOf("x" to value.x, "y" to value.y)
    private fun bounds(value: SceneBounds) = mapOf("x" to value.x, "y" to value.y, "width" to value.width, "height" to value.height)
    private fun portMap(value: ScenePort) = mapOf("elementId" to value.elementId.value, "anchorId" to value.anchorId, "semanticPortId" to value.semanticPortId, "point" to point(value.point), "hitRadius" to value.hitRadius, "direction" to value.direction.name, "styleId" to value.styleId.value, "traceId" to value.traceId.value)
    private fun labelMap(value: SceneLabel) = mapOf("elementId" to value.elementId.value, "role" to value.role, "text" to value.text, "anchor" to point(value.anchor), "bounds" to bounds(value.bounds), "rotationDegrees" to value.rotationDegrees, "styleId" to value.styleId.value, "traceId" to value.traceId.value)
    private fun originMap(value: SourceOrigin) = mapOf("relativePath" to value.relativePath, "sourceDigest" to value.sourceDigest, "role" to value.role.name, "startLine" to value.startLine, "startCharacter" to value.startCharacter, "endLine" to value.endLine, "endCharacter" to value.endCharacter, "subjectId" to value.subjectId, "primary" to value.primary)
}

private object CanonicalJson {
    fun write(value: Any?): String = when (value) {
        null -> error("Canonical scene JSON omits absent values; null is forbidden")
        is String -> quote(value)
        is Number, is Boolean -> value.toString()
        is Map<*, *> -> value.entries.sortedWith { left, right -> compareUtf16(left.key.toString(), right.key.toString()) }.joinToString("", "{", "}") { "${quote(it.key.toString())}:${write(it.value)}" }
        is Iterable<*> -> value.joinToString("", "[", "]") { write(requireNotNull(it)) }
        else -> error("Unsupported canonical JSON value: ${value::class.qualifiedName}")
    }

    private fun quote(value: String): String = buildString {
        append('"')
        value.forEach { char ->
            when (char) {
                '"' -> append("\\\""); '\\' -> append("\\\\"); '\b' -> append("\\b"); '\t' -> append("\\t")
                '\n' -> append("\\n"); '\u000C' -> append("\\f"); '\r' -> append("\\r")
                in '\u0000'..'\u001F' -> append("\\u%04x".format(char.code)); else -> append(char)
            }
        }
        append('"')
    }

    private fun compareUtf16(left: String, right: String): Int {
        for (index in 0 until minOf(left.length, right.length)) {
            val result = left[index].code.compareTo(right[index].code)
            if (result != 0) return result
        }
        return left.length.compareTo(right.length)
    }
}
