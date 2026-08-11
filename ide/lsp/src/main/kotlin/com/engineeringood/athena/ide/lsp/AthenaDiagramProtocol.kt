package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.compiler.AthenaDiagramSceneCompiler
import com.engineeringood.athena.compiler.CompilerCompilationParseFailure
import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import com.engineeringood.athena.interaction.SourceRevision
import com.engineeringood.athena.interaction.EditOperationResult
import com.engineeringood.athena.language.SheetCompanionFound
import com.engineeringood.athena.language.SheetCompanionLocator
import com.engineeringood.athena.language.SheetCompanionAmbiguous
import com.engineeringood.athena.language.SheetStyleCompanionLocator
import com.engineeringood.athena.language.AthenaSheetStyleCompanionParser
import com.engineeringood.athena.language.SheetStyleCompanionParseFailure
import com.engineeringood.athena.language.SheetStyleCompanionParseSuccess
import com.engineeringood.athena.language.SheetStyleCompanionSource
import com.engineeringood.athena.packageplatform.PackageItemAdmissionState
import com.engineeringood.athena.packageplatform.PackageItemValue
import com.engineeringood.athena.packageplatform.PlaceholderAssignment
import com.engineeringood.athena.packageplatform.PlaceholderSchema
import com.engineeringood.athena.packageplatform.PlaceholderValueType
import com.engineeringood.athena.packageplatform.RepresentationPlaceholder
import com.engineeringood.athena.packageruntime.FunctionRepresentationBindingAdmission
import com.engineeringood.athena.packageruntime.LockedRepresentationPackageRuntime
import com.engineeringood.athena.packageruntime.PlaceholderResolver
import com.engineeringood.athena.presentation.AssetBundle
import com.engineeringood.athena.presentation.AthenaDiagramScene
import com.engineeringood.athena.presentation.AthenaDiagramSceneContract
import com.engineeringood.athena.presentation.AthenaScenePublication
import com.engineeringood.athena.presentation.InputRevision
import com.engineeringood.athena.presentation.PresentationAssetCompiler
import com.engineeringood.athena.presentation.SceneDiagnostic
import com.engineeringood.athena.presentation.SceneId
import java.nio.file.Files
import java.nio.file.Path
import java.util.Base64

class AthenaDiagramSceneParams

data class AthenaPresentationEditContextPayload(
    val schemaVersion: Int = 1,
    val state: String,
    val sceneId: SceneId? = null,
    val sourceRevision: SourceRevision,
    val sheetId: String? = null,
    val engineeringWritableFiles: List<String> = emptyList(),
    val routeWritableFiles: List<String> = emptyList(),
    val placementWritableFiles: List<String> = emptyList(),
    val styleWritableFiles: List<String> = emptyList(),
    val diagnostics: List<SceneDiagnostic> = emptyList(),
)

internal fun AthenaPresentationEditContextPayload.toWirePayload(): Map<String, Any?> = buildMap {
    put("schemaVersion", schemaVersion)
    put("state", state)
    sceneId?.let { put("sceneId", it.value) }
    put("sourceRevision", sourceRevision.toWirePayload())
    sheetId?.let { put("sheetId", it) }
    put("engineeringWritableFiles", engineeringWritableFiles)
    put("routeWritableFiles", routeWritableFiles)
    put("placementWritableFiles", placementWritableFiles)
    put("styleWritableFiles", styleWritableFiles)
    put("diagnostics", diagnostics.map(SceneDiagnostic::toWirePayload))
}

internal fun EditOperationResult.toWirePayload(): Map<String, Any?> = buildMap {
    put("schemaVersion", schemaVersion)
    put("status", status.name)
    put("operationId", operationId)
    put("currentSourceRevision", currentSourceRevision.toWirePayload())
    acceptance?.let { accepted ->
        put("acceptance", mapOf(
            "previousSourceRevision" to accepted.previousSourceRevision.toWirePayload(),
            "resultingSourceRevision" to accepted.resultingSourceRevision.toWirePayload(),
            "acceptedPatchSet" to accepted.acceptedPatchSet.toWirePayload(),
            "publicationCorrelationId" to accepted.publicationCorrelationId,
            "journalEntryId" to accepted.journalEntryId,
        ))
    }
    rejection?.let { rejected ->
        put("rejection", mapOf(
            "reason" to rejected.reason.name,
            "diagnostics" to rejected.diagnostics.map { diagnostic -> mapOf(
                "subject" to diagnostic.subject,
                "problem" to diagnostic.problem,
                "correction" to diagnostic.correction,
                "code" to diagnostic.code,
            ) },
        ))
    }
}

internal fun SourceRevision.toWirePayload(): Map<String, Any?> = mapOf(
    "sceneInputRevision" to sceneInputRevision.value,
    "sourceRootIdentity" to sourceRootIdentity,
    "engineeringSourceDigest" to engineeringSourceDigest,
    "sheetDigest" to sheetDigest,
    "styleDigest" to styleDigest.canonicalValue(),
    "lockDigest" to lockDigest.canonicalValue(),
    "packageItemDigests" to packageItemDigests.map { item -> mapOf(
        "packageId" to item.packageId,
        "itemId" to item.itemId,
        "sha256" to item.sha256,
    ) },
    "compilerVersion" to compilerVersion,
    "sceneSchemaVersion" to sceneSchemaVersion,
    "profileVersion" to profileVersion,
)

private fun com.engineeringood.athena.interaction.SourcePatchSet.toWirePayload(): Map<String, Any?> = mapOf(
    "files" to files.map { patch -> mapOf(
        "relativePath" to patch.relativePath,
        "beforeUtf8" to patch.beforeUtf8,
        "afterUtf8" to patch.afterUtf8,
    ) },
    "writableFiles" to writableFiles,
)

private fun SceneDiagnostic.toWirePayload(): Map<String, String> = mapOf(
    "subject" to subject,
    "problem" to problem,
    "correction" to correction,
    "code" to code,
)

/** LSP wire mapping only: converts verified publication bytes to the schema's base64 field. */
internal fun AthenaScenePublication.toDiagramScenePayload(): Map<String, Any?> = buildMap {
    put("schemaVersion", schemaVersion)
    put("state", state.name)
    put("attemptedInputRevision", attemptedInputRevision.value)
    acceptedInputRevision?.let { put("acceptedInputRevision", it.value) }
    scene?.let { put("scene", it.toDiagramScenePayload()) }
    assetBundle?.let { put("assetBundle", it.toDiagramBundlePayload()) }
    put("diagnostics", diagnostics.map { diagnostic ->
        mapOf(
            "subject" to diagnostic.subject,
            "problem" to diagnostic.problem,
            "correction" to diagnostic.correction,
            "code" to diagnostic.code,
        )
    })
}

private fun AthenaDiagramScene.toDiagramScenePayload(): Map<String, Any?> = buildMap {
    put("schemaVersion", schemaVersion)
    put("sceneId", sceneId.value)
    put("inputRevision", inputRevision.value)
    put("sceneDigest", sceneDigest.value)
    put("page", mapOf("pageBounds" to page.pageBounds.toDiagramBoundsPayload(), "drawingBounds" to page.drawingBounds.toDiagramBoundsPayload()))
    put("plotFrame", mapOf("columns" to plotFrame.columns, "rows" to plotFrame.rows, "columnLabels" to plotFrame.columnLabels.name, "rowLabels" to plotFrame.rowLabels.name))
    put("snapGrid", mapOf("sheetId" to snapGrid.sheetId, "step" to snapGrid.step, "drawingOrigin" to snapGrid.drawingOrigin.toDiagramPointPayload(), "formulaVersion" to snapGrid.formulaVersion))
    put("styles", styles.map { style -> buildMap<String, Any> {
        put("styleId", style.styleId.value); put("strokeRgba", style.strokeRgba); put("fillRgba", style.fillRgba); put("strokeWidth", style.strokeWidth); put("dash", style.dash)
        put("lineCap", style.lineCap.name); put("lineJoin", style.lineJoin.name); put("fillRule", style.fillRule.name); put("opacity", style.opacity)
        style.fontAssetId?.let { put("fontAssetId", it.value) }; put("fontSize", style.fontSize); put("fontWeight", style.fontWeight); put("textAlign", style.textAlign.name); put("textBaseline", style.textBaseline.name); put("routeMarker", style.routeMarker.name); put("portDisplay", style.portDisplay.name)
    } })
    put("assets", assets.map { asset -> buildMap<String, Any> {
        put("assetId", asset.assetId.value); put("digest", asset.digest); put("mediaKind", asset.mediaKind.name); put("profileId", asset.profileId)
        asset.intrinsicBounds?.let { put("intrinsicBounds", it.toDiagramBoundsPayload()) }; put("bundleEntryId", asset.bundleEntryId); put("traceId", asset.traceId.value)
    } })
    put("occurrences", occurrences.map { occurrence -> buildMap<String, Any> {
        put("elementId", occurrence.elementId.value); put("occurrenceId", occurrence.occurrenceId); put("subjectId", occurrence.subjectId); put("semanticId", occurrence.semanticId); occurrence.representationRef?.let { put("representationRef", it) }; put("bounds", occurrence.bounds.toDiagramBoundsPayload()); put("placementAnchor", occurrence.placementAnchor.toDiagramPointPayload()); put("zIndex", occurrence.zIndex); put("styleId", occurrence.styleId.value); put("traceId", occurrence.traceId.value)
        occurrence.assetId?.let { put("assetId", it.value) }
        put("ports", occurrence.ports.map { port -> mapOf("elementId" to port.elementId.value, "anchorId" to port.anchorId, "semanticPortId" to port.semanticPortId, "point" to port.point.toDiagramPointPayload(), "hitRadius" to port.hitRadius, "direction" to port.direction.name, "styleId" to port.styleId.value, "traceId" to port.traceId.value) })
        put("labels", occurrence.labels.map { label -> mapOf("elementId" to label.elementId.value, "role" to label.role, "text" to label.text, "anchor" to label.anchor.toDiagramPointPayload(), "bounds" to label.bounds.toDiagramBoundsPayload(), "rotationDegrees" to label.rotationDegrees, "styleId" to label.styleId.value, "traceId" to label.traceId.value) })
    } })
    put("connections", connections.map { connection -> buildMap<String, Any> {
        put("elementId", connection.elementId.value); put("connectionId", connection.connectionId); put("projectionId", connection.projectionId); put("sourceAnchorId", connection.sourceAnchorId); put("targetAnchorId", connection.targetAnchorId)
        put("segments", connection.segments.map { segment -> mapOf("segmentId" to segment.segmentId, "start" to segment.start.toDiagramPointPayload(), "end" to segment.end.toDiagramPointPayload(), "kind" to segment.kind.name) })
        put("markers", connection.markers.map { marker -> mapOf("elementId" to marker.elementId.value, "kind" to marker.kind.name, "point" to marker.point.toDiagramPointPayload(), "relatedConnectionIds" to marker.relatedConnectionIds, "bridgeOwner" to marker.bridgeOwner, "traceId" to marker.traceId.value) })
        put("annotations", connection.annotations.map { annotation -> mapOf("elementId" to annotation.elementId.value, "semanticId" to annotation.semanticId, "displayRole" to annotation.displayRole, "value" to annotation.value, "anchor" to annotation.anchor.toDiagramPointPayload(), "bounds" to annotation.bounds.toDiagramBoundsPayload(), "traceId" to annotation.traceId.value) })
        put("zIndex", connection.zIndex); put("styleId", connection.styleId.value); put("traceId", connection.traceId.value)
    } })
    put("decorations", decorations.map { decoration -> buildMap<String, Any> {
        put("elementId", decoration.elementId.value); put("kind", decoration.kind.name); put("bounds", decoration.bounds.toDiagramBoundsPayload()); put("zIndex", decoration.zIndex); put("styleId", decoration.styleId.value); put("traceId", decoration.traceId.value); decoration.text?.let { put("text", it) }
    } })
    put("traces", traces.map { trace -> mapOf("traceId" to trace.traceId.value, "origins" to trace.origins.map { origin -> mapOf("relativePath" to origin.relativePath, "sourceDigest" to origin.sourceDigest, "role" to origin.role.name, "startLine" to origin.startLine, "startCharacter" to origin.startCharacter, "endLine" to origin.endLine, "endCharacter" to origin.endCharacter, "subjectId" to origin.subjectId, "primary" to origin.primary) }) })
}

private fun AssetBundle.toDiagramBundlePayload(): Map<String, Any> = mapOf(
    "inputRevision" to inputRevision.value,
    "entries" to entries.sortedBy { it.entryId }.map { entry ->
        mapOf("entryId" to entry.entryId, "digest" to entry.digest, "bytesBase64" to Base64.getEncoder().encodeToString(entry.bytes))
    },
)

private fun com.engineeringood.athena.presentation.ScenePoint.toDiagramPointPayload(): Map<String, Int> = mapOf("x" to x, "y" to y)

private fun com.engineeringood.athena.presentation.SceneBounds.toDiagramBoundsPayload(): Map<String, Int> = mapOf("x" to x, "y" to y, "width" to width, "height" to height)

internal fun AthenaLspSessionHostReady.currentInputRevision(styleCompanionBytesOverride: ByteArray? = null): InputRevision {
    return SourceRevisionService(this).current(styleCompanionBytesOverride = styleCompanionBytesOverride).sceneInputRevision
}

/** Publishes only a complete canonical scene compiled through runtime-owned Athena authority. */
internal fun AthenaLspSessionHostReady.diagramScenePublication(): AthenaScenePublication {
    val lock = executionContext.compiler().validateRepositoryLock(repositoryRoot)
    val revision = currentInputRevision()
    companionDiagnostic()?.let { diagnostic ->
        return AthenaScenePublication.unavailable(revision, diagnostic)
    }
    return runCatching {
        when (val compilation = executionContext.compiler().compile(sourcePath)) {
            is CompilerCompilationSuccess -> {
                val sceneResult = AthenaDiagramSceneCompiler().compile(
                    compilation,
                    revision,
                    styleCompanion = styleCompanionSource(),
                )
                sceneResult.scene?.let { scene ->
                    val lockError = lock.diagnostics.firstOrNull { it.severity == com.engineeringood.athena.repository.RepositoryDiagnosticSeverity.ERROR }
                    val expectedLock = lock.expectedLock
                    if (lockError != null || expectedLock == null) {
                        AthenaScenePublication.unavailable(
                            revision,
                            SceneDiagnostic(
                                subject = projectName,
                                problem = lockError?.message ?: "Repository lock is unavailable.",
                                correction = "Materialize a valid athena.lock before opening the engineering document.",
                                code = lockError?.code ?: "repository.lock.unavailable",
                            ),
                        )
                    } else {
                        val assets = executionContext.compiler().compilePresentationAssets(repositoryRoot, expectedLock.packageSnapshots)
                        assets.diagnostics.firstOrNull()?.let { diagnostic ->
                            AthenaScenePublication.unavailable(revision, diagnostic)
                        } ?: run {
                            val publishedScene = packageBackedScene(
                                scene = scene,
                                assets = assets,
                                packageSnapshots = expectedLock.packageSnapshots,
                            ) ?: return@run AthenaScenePublication.unavailable(
                                revision,
                                SceneDiagnostic(
                                    subject = projectName,
                                    problem = "A visible Function has no admitted package representation binding.",
                                    correction = "Persist one FunctionRepresentationBinding to a PACKAGE_READY Element with package-local SVG geometry.",
                                    code = "package.binding.visible-occurrence-missing",
                                ),
                            )
                            AthenaScenePublication.ready(publishedScene, PresentationAssetCompiler.bundle(revision, assets.admitted))
                        }
                    }
                } ?: AthenaScenePublication.unavailable(
                    revision = revision,
                    diagnostic = sceneResult.diagnostics.firstOrNull() ?: unavailableSceneDiagnostic(projectName),
                )
            }

            is CompilerCompilationParseFailure -> AthenaScenePublication.unavailable(
                revision = revision,
                diagnostic = compilation.diagnostics.firstOrNull()?.toSceneDiagnostic()
                    ?: unavailableSceneDiagnostic(projectName),
            )
        }
    }.getOrElse { failure ->
        AthenaScenePublication.unavailable(
            revision = revision,
            diagnostic = SceneDiagnostic(
                subject = projectName,
                problem = failure.message ?: "Diagram compilation did not complete.",
                correction = "Correct the authored source before opening the engineering document.",
                code = "diagram.scene.compile-failed",
            ),
        )
    }
}

internal fun AthenaLspSessionHostReady.packageBackedScene(
    scene: AthenaDiagramScene,
    bindingSourceOverride: String? = null,
): AthenaDiagramScene? {
    val lock = executionContext.compiler().validateRepositoryLock(repositoryRoot)
    val expectedLock = lock.expectedLock ?: return null
    if (!lock.isValid) return null
    val assets = executionContext.compiler().compilePresentationAssets(repositoryRoot, expectedLock.packageSnapshots)
    if (assets.diagnostics.isNotEmpty()) return null
    return packageBackedScene(scene, assets, expectedLock.packageSnapshots, bindingSourceOverride)
}

private fun AthenaLspSessionHostReady.packageBackedScene(
    scene: AthenaDiagramScene,
    assets: com.engineeringood.athena.compiler.PresentationAssetPackageCompilationResult,
    packageSnapshots: List<com.engineeringood.athena.repository.RepositoryLockedPackage>,
    bindingSourceOverride: String? = null,
): AthenaDiagramScene? = scene.withPackageBackedRepresentations(
    bindingPath = representationBindingCompanionPath(),
    bindingSourceOverride = bindingSourceOverride,
    assets = assets,
    lock = packageSnapshots,
)

private fun AthenaDiagramScene.withPackageBackedRepresentations(
    bindingPath: Path,
    bindingSourceOverride: String? = null,
    assets: com.engineeringood.athena.compiler.PresentationAssetPackageCompilationResult,
    lock: List<com.engineeringood.athena.repository.RepositoryLockedPackage>,
): AthenaDiagramScene? {
    val bindings = if (bindingSourceOverride != null || Files.exists(bindingPath)) {
        val source = bindingSourceOverride ?: runCatching { Files.readString(bindingPath) }.getOrNull() ?: return null
        runCatching { RepresentationBindingCompanionEditor().read(bindingPath, source).bindings }.getOrNull() ?: return null
    } else {
        emptyList()
    }
    val bindingsByFunction = bindings.associateBy { it.key.functionId }
    val lockedItemsByKey = lock.flatMap { snapshot ->
        snapshot.itemDigests.map { item ->
            "${snapshot.packageId.name}@${snapshot.packageId.version}/${item.itemId}@${item.itemVersion}" to LockedItem(snapshot.packageId, item)
        }
    }.toMap()
    val admissionStates = lockedItemsByKey.keys.associateWith { PackageItemAdmissionState.PACKAGE_READY }
    if (bindings.any { binding ->
            val element = lockedItemsByKey[binding.element.key]
            element?.item?.kind != "ELEMENT" ||
                !FunctionRepresentationBindingAdmission.admit(binding, admissionStates).diagnostics.isEmpty() ||
                (binding.variant?.let { variant ->
                    val lockedVariant = lockedItemsByKey[variant.key]
                    lockedVariant == null || !LockedRepresentationPackageRuntime.variantMatches(
                        element.packageId,
                        element.item,
                        lockedVariant.packageId,
                        lockedVariant.item,
                    )
                } == true) ||
                binding.placeholderResolution(lockedItemsByKey) == null
        }) return null
    val occurrenceAssets = occurrences.mapNotNull { occurrence ->
        if (!occurrence.semanticId.startsWith("function:")) return@mapNotNull occurrence
        val binding = bindingsByFunction[occurrence.semanticId] ?: return@mapNotNull null
        val item = binding.variant?.let { lockedItemsByKey[it.key]?.item } ?: lock.firstNotNullOfOrNull { snapshot ->
            snapshot.itemDigests.firstOrNull { candidate ->
                snapshot.packageId == binding.element.packageId &&
                    candidate.itemId == binding.element.itemId &&
                    candidate.itemVersion == binding.element.itemVersion &&
                    candidate.kind == "ELEMENT"
            }
        } ?: return@mapNotNull null
        val resourcePath = item.resourceReferences.singleOrNull() ?: return@mapNotNull null
        val admitted = assets.admittedByPackageResource[
            com.engineeringood.athena.compiler.PackageResourceKey(binding.element.packageId, resourcePath)
        ] ?: return@mapNotNull null
        val placeholderValues = binding.placeholderResolution(lockedItemsByKey) ?: return@mapNotNull null
        val labelText = placeholderValues.entries.singleOrNull { (name, _) ->
            lockedItemsByKey[placeholderKey(binding, name)]?.item?.attributes?.get("target") == "label.text"
        }?.value as? PackageItemValue.TextValue
        occurrence.copy(
            representationRef = "${binding.element.packageId.name}/${binding.element.itemId}@${binding.element.itemVersion}",
            assetId = admitted.asset.assetId,
            labels = labelText?.let { text -> occurrence.labels.map { label -> label.copy(text = text.value) } }
                ?: occurrence.labels,
        )
    }
    if (occurrenceAssets.size != occurrences.size) return null
    return AthenaDiagramSceneContract.canonicalize(
        copy(
            assets = assets.admitted.map { it.asset },
            occurrences = occurrenceAssets,
            traces = traces + assets.admitted.map { it.trace },
        ),
    )
}

private data class LockedItem(
    val packageId: com.engineeringood.athena.repository.PackageIdentifier,
    val item: com.engineeringood.athena.repository.RepositoryLockedItem,
)

private fun com.engineeringood.athena.packageplatform.FunctionRepresentationBinding.placeholderResolution(
    lockedItems: Map<String, LockedItem>,
): Map<String, PackageItemValue>? {
    if (placeholderValues.isEmpty()) return emptyMap()
    val placeholders = placeholderValues.keys.sorted().map { name ->
        val locked = lockedItems[placeholderKey(this, name)]?.item ?: return null
        if (locked.kind != "PLACEHOLDER") return null
        val target = locked.attributes["target"] ?: return null
        if (target != "label.text") return null
        val type = locked.attributes["type"]?.let { runCatching { PlaceholderValueType.valueOf(it) }.getOrNull() }
            ?: return null
        RepresentationPlaceholder(name, target, type)
    }
    val result = PlaceholderResolver.resolve(
        PlaceholderSchema(placeholders),
        placeholderValues.map { (name, value) -> PlaceholderAssignment(name, value) },
    )
    return result.values
}

private fun placeholderKey(
    binding: com.engineeringood.athena.packageplatform.FunctionRepresentationBinding,
    name: String,
): String = "${binding.element.packageId.name}@${binding.element.packageId.version}/$name@${binding.element.itemVersion}"

private fun PackageItemValue.placeholderType(): String = when (this) {
    is PackageItemValue.TextValue -> "TEXT"
    is PackageItemValue.NumberValue -> "NUMBER"
    is PackageItemValue.BooleanValue -> "BOOLEAN"
    is PackageItemValue.ObjectValue,
    is PackageItemValue.ListValue,
    is PackageItemValue.DeclaredSetValue,
    -> "UNSUPPORTED"
}

internal fun AthenaLspSessionHostReady.unavailableDiagramPublication(): AthenaScenePublication =
    AthenaScenePublication.unavailable(
        revision = currentInputRevision(),
        diagnostic = companionDiagnostic() ?: unavailableSceneDiagnostic(projectName),
    )

private fun com.engineeringood.athena.compiler.CompilerSyntaxDiagnostic.toSceneDiagnostic(): SceneDiagnostic = SceneDiagnostic(
    subject = file.substringAfterLast('/', file.substringAfterLast('\\')),
    problem = message,
    correction = "Correct the authored source before opening the engineering document.",
    code = "diagram.scene.compile-failed",
)

private fun unavailableSceneDiagnostic(projectName: String): SceneDiagnostic = SceneDiagnostic(
    subject = projectName,
    problem = "No canonical diagram scene is available.",
    correction = "Correct the authored source and Sheet Companion before opening the engineering document.",
    code = "diagram.scene.unavailable",
)

internal fun AthenaLspSessionHostReady.companionDiagnostic(): SceneDiagnostic? {
    return when (val location = SheetCompanionLocator.locate(sourcePath)) {
        is com.engineeringood.athena.language.SheetCompanionMissing -> SceneDiagnostic(
            subject = location.expectedPath.fileName.toString(),
            problem = "Required same-basename Sheet Companion is missing.",
            correction = "Add `${location.expectedPath.fileName}` beside `${sourcePath.fileName}`.",
            code = "sheet.companion.missing",
        )
        is com.engineeringood.athena.language.SheetCompanionAmbiguous -> SceneDiagnostic(
            subject = location.expectedPath.fileName.toString(),
            problem = "Same-basename Sheet Companion is ambiguous or uses different filename casing.",
            correction = "Keep exactly one companion named `${location.expectedPath.fileName}`.",
            code = "sheet.companion.ambiguous",
        )
        is com.engineeringood.athena.language.SheetCompanionFound -> {
            when (val style = SheetStyleCompanionLocator.locate(location.path)) {
                is SheetCompanionAmbiguous -> SceneDiagnostic(
                    subject = style.expectedPath.fileName.toString(),
                    problem = "Same-basename Style Companion is ambiguous or uses different filename casing.",
                    correction = "Keep zero or one companion named `${style.expectedPath.fileName}`.",
                    code = "sheet.style.companion.ambiguous",
                )
                is SheetCompanionFound -> {
                    when (val parsed = AthenaSheetStyleCompanionParser().parse(style.path.toString(), Files.readString(style.path))) {
                        is SheetStyleCompanionParseFailure -> {
                            val diagnostic = parsed.diagnostics.first()
                            SceneDiagnostic(
                                subject = style.path.fileName.toString(),
                                problem = diagnostic.message,
                                correction = "Correct the Style Companion before opening the engineering document.",
                                code = "sheet.style.companion.invalid",
                            )
                        }
                        else -> null
                    }
                }
                else -> null
            }
        }
    }
}

internal fun AthenaLspSessionHostReady.styleCompanionSource(): SheetStyleCompanionSource? {
    val location = SheetCompanionLocator.locate(sourcePath)
    val sheet = location as? SheetCompanionFound ?: return null
    val style = SheetStyleCompanionLocator.locate(sheet.path) as? SheetCompanionFound ?: return null
    return when (val parsed = AthenaSheetStyleCompanionParser().parse(style.path.toString(), Files.readString(style.path))) {
        is SheetStyleCompanionParseSuccess -> parsed.source
        is SheetStyleCompanionParseFailure -> null
    }
}

internal fun AthenaLspSessionHostReady.sheetCompanionLocation() =
    com.engineeringood.athena.language.SheetCompanionLocator.locate(sourcePath)

internal fun AthenaLspSessionHostReady.presentationEditContext(): AthenaPresentationEditContextPayload {
    val revision = SourceRevisionService(this).current()
    val publication = diagramScenePublication()
    val scene = publication.scene
    if (publication.state != com.engineeringood.athena.presentation.PublicationState.READY || scene == null) {
        return AthenaPresentationEditContextPayload(
            state = "UNAVAILABLE",
            sourceRevision = revision,
            diagnostics = publication.diagnostics,
        )
    }
    val sheet = SheetCompanionLocator.locate(sourcePath) as? SheetCompanionFound
        ?: return AthenaPresentationEditContextPayload(
            state = "UNAVAILABLE",
            sourceRevision = revision,
            diagnostics = listOf(unavailableSceneDiagnostic(projectName)),
        )
    return when (val style = SheetStyleCompanionLocator.locate(sheet.path)) {
        is SheetCompanionAmbiguous -> AthenaPresentationEditContextPayload(
            state = "UNAVAILABLE",
            sourceRevision = revision,
            diagnostics = listOf(requireNotNull(companionDiagnostic())),
        )
        is SheetCompanionFound -> {
            AthenaPresentationEditContextPayload(
                state = "READY",
                sceneId = scene.sceneId,
                sourceRevision = revision,
                sheetId = scene.snapGrid.sheetId,
                engineeringWritableFiles = listOf(relativeToRepository(sourcePath)),
                routeWritableFiles = listOf(relativeToRepository(sheet.path)),
                placementWritableFiles = listOf(relativeToRepository(sheet.path)),
                styleWritableFiles = listOf(relativeToRepository(style.path)),
            )
        }
        else -> {
            AthenaPresentationEditContextPayload(
                state = "READY",
                sceneId = scene.sceneId,
                sourceRevision = revision,
                sheetId = scene.snapGrid.sheetId,
                engineeringWritableFiles = listOf(relativeToRepository(sourcePath)),
                routeWritableFiles = listOf(relativeToRepository(sheet.path)),
                placementWritableFiles = listOf(relativeToRepository(sheet.path)),
                styleWritableFiles = listOf(relativeToRepository(style.expectedPath)),
            )
        }
    }
}

private fun AthenaLspSessionHostReady.relativeToRepository(path: java.nio.file.Path): String =
    repositoryRoot.relativize(path).toString().replace('\\', '/')
