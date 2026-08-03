package com.engineeringood.athena.compiler

import com.engineeringood.athena.authoring.AuthoringDiagnostic
import com.engineeringood.athena.authoring.AuthoringDiagnosticAuthority
import com.engineeringood.athena.authoring.AuthoringDiagnosticCode
import com.engineeringood.athena.authoring.AuthoringLifecycleState
import com.engineeringood.athena.authoring.AuthoringRecoveryAction
import com.engineeringood.athena.authoring.AuthoringRevisionGuard
import com.engineeringood.athena.authoring.AuthoringValue
import com.engineeringood.athena.authoring.CreateSemanticEntityIntent
import com.engineeringood.athena.authoring.SemanticRelationshipIntent
import com.engineeringood.athena.component.EngineeringConceptPortDirection
import com.engineeringood.athena.component.EngineeringConceptPropertyValueKind
import com.engineeringood.athena.component.EngineeringConceptTemplate
import com.engineeringood.athena.language.SourceFileAst
import com.engineeringood.athena.language.AthenaLanguageParser
import com.engineeringood.athena.language.InstallationDeclaration
import com.engineeringood.athena.language.InstallationOrientation
import com.engineeringood.athena.language.ParseFailure
import com.engineeringood.athena.layout.AuthoredLayoutIntent
import kotlin.math.roundToInt

data class BackendAuthoringSourceDocument(
    val sourceUri: String,
    val documentVersion: Int,
    val semanticSnapshotId: String,
    val sourceUnitId: String = sourceUri,
    val sourceText: String,
    val ast: SourceFileAst,
) {
    val revisionGuard: AuthoringRevisionGuard
        get() = AuthoringRevisionGuard.from(
            semanticSnapshotId = semanticSnapshotId,
            sourceUri = sourceUri,
            documentVersion = documentVersion,
            sourceText = sourceText,
        )
}

data class BackendAuthoringSourceOffsetRange(
    val startOffset: Int,
    val endOffset: Int,
) {
    init {
        require(startOffset >= 0) { "Source edit start offset must not be negative." }
        require(endOffset >= startOffset) { "Source edit end offset must not precede its start." }
    }
}

data class BackendAuthoringSourceEditPlan(
    val revisionGuard: AuthoringRevisionGuard,
    val sourceUri: String,
    val replacement: BackendAuthoringSourceOffsetRange,
    val admittedText: String,
    val selection: BackendAuthoringSourceOffsetRange? = null,
    val affectedSemanticIds: List<String>,
) {
    fun applyTo(sourceText: String): String {
        require(replacement.endOffset <= sourceText.length) { "Source edit replacement exceeds the active source length." }
        return sourceText.substring(0, replacement.startOffset) +
            admittedText + sourceText.substring(replacement.endOffset)
    }
}

sealed interface BackendAuthoringSourceEditPlanningResult

data class BackendAuthoringSourceEditPlanned(
    val plan: BackendAuthoringSourceEditPlan,
) : BackendAuthoringSourceEditPlanningResult

data class BackendAuthoringSourceEditRejected(
    val diagnostics: List<AuthoringDiagnostic>,
) : BackendAuthoringSourceEditPlanningResult

sealed interface BackendAuthoringSourceEditPlanningRequest {
    val document: BackendAuthoringSourceDocument
    val revisionGuard: AuthoringRevisionGuard
}

data class BackendEntityCreationPlanningRequest(
    override val document: BackendAuthoringSourceDocument,
    override val revisionGuard: AuthoringRevisionGuard,
    val intent: CreateSemanticEntityIntent,
    val template: EngineeringConceptTemplate,
) : BackendAuthoringSourceEditPlanningRequest

data class BackendRelationshipPlanningRequest(
    override val document: BackendAuthoringSourceDocument,
    override val revisionGuard: AuthoringRevisionGuard,
    val intent: SemanticRelationshipIntent,
    val sourceAuthoredPath: String,
    val targetAuthoredPath: String,
) : BackendAuthoringSourceEditPlanningRequest

data class BackendAuthoredLayoutPlanningRequest(
    override val document: BackendAuthoringSourceDocument,
    override val revisionGuard: AuthoringRevisionGuard,
    val subjectSemanticId: String,
    val intent: AuthoredLayoutIntent,
) : BackendAuthoringSourceEditPlanningRequest

data class BackendCabinetPlacementPlanningRequest(
    override val document: BackendAuthoringSourceDocument,
    override val revisionGuard: AuthoringRevisionGuard,
    val occurrenceId: String,
    val deltaXMillimeters: Double,
    val deltaYMillimeters: Double,
) : BackendAuthoringSourceEditPlanningRequest

class BackendAuthoringSourceEditPlanner(
    private val layoutSerializer: AuthoredLayoutIntentSourceSerializer = AuthoredLayoutIntentSourceSerializer(),
    private val parser: AthenaLanguageParser = AthenaLanguageParser(),
) {
    fun plan(request: BackendAuthoringSourceEditPlanningRequest): BackendAuthoringSourceEditPlanningResult {
        val activeGuard = request.document.revisionGuard
        if (request.revisionGuard != activeGuard) {
            return BackendAuthoringSourceEditRejected(
                diagnostics = listOf(
                    AuthoringDiagnostic(
                        code = AuthoringDiagnosticCode.SOURCE_CONFLICT,
                        message = "Active Athena source does not match the Revision Guard used for source planning.",
                        authority = AuthoringDiagnosticAuthority.SOURCE_PLANNING,
                        lifecycleStage = AuthoringLifecycleState.STALE,
                        recoveryAction = AuthoringRecoveryAction.REFRESH_PREVIEW,
                    ),
                ),
            )
        }

        val insertionOffset = request.document.ast.system.span.end.offset - 1
        if (insertionOffset !in request.document.sourceText.indices || request.document.sourceText[insertionOffset] != '}') {
            return BackendAuthoringSourceEditRejected(
                diagnostics = listOf(
                    AuthoringDiagnostic(
                        code = AuthoringDiagnosticCode.SOURCE_CONFLICT,
                        message = "Authored system span does not resolve to the active source closing boundary.",
                        authority = AuthoringDiagnosticAuthority.SOURCE_PLANNING,
                        lifecycleStage = AuthoringLifecycleState.BLOCKED,
                        recoveryAction = AuthoringRecoveryAction.FIX_SOURCE,
                    ),
                ),
            )
        }

        return when (request) {
            is BackendEntityCreationPlanningRequest -> planEntityCreation(request, insertionOffset)
            is BackendRelationshipPlanningRequest -> planRelationship(request, insertionOffset)
            is BackendAuthoredLayoutPlanningRequest -> planLayout(request, insertionOffset)
            is BackendCabinetPlacementPlanningRequest -> planCabinetPlacement(request)
        }
    }

    private fun planEntityCreation(
        request: BackendEntityCreationPlanningRequest,
        insertionOffset: Int,
    ): BackendAuthoringSourceEditPlanningResult {
        require(request.intent.revisionGuard == request.revisionGuard) {
            "Entity intent and source planning Revision Guards must match."
        }
        require(request.intent.conceptTemplateId == request.template.templateId) {
            "Entity intent template id must match the selected Engineering Concept Template."
        }
        require(request.intent.conceptId == request.template.conceptId) {
            "Entity intent concept id must match the selected Engineering Concept Template."
        }
        val requestedName = request.intent.suggestedName.orEmpty()
        val entityName = uniqueEntityName(requestedName, request.document.ast)
        val text = renderEntity(entityName, request.intent, request.template)
        return planned(
            request = request,
            insertionOffset = insertionOffset,
            admittedText = text,
            affectedSemanticIds = buildList {
                add("component:$entityName")
                request.template.nestedPorts.forEach { port -> add("port:$entityName.${port.name}") }
            },
        )
    }

    private fun planRelationship(
        request: BackendRelationshipPlanningRequest,
        insertionOffset: Int,
    ): BackendAuthoringSourceEditPlanningResult {
        if (request.sourceAuthoredPath.isBlank() || request.targetAuthoredPath.isBlank()) {
            return BackendAuthoringSourceEditRejected(
                diagnostics = listOf(
                    AuthoringDiagnostic(
                        code = AuthoringDiagnosticCode.SOURCE_INVALID,
                        message = "Relationship source planning requires admitted endpoint paths.",
                        authority = AuthoringDiagnosticAuthority.SOURCE_PLANNING,
                        lifecycleStage = AuthoringLifecycleState.BLOCKED,
                        relatedIds = listOf(request.intent.sourceSubjectId.value, request.intent.targetSubjectId.value),
                        recoveryAction = AuthoringRecoveryAction.FIX_SOURCE,
                    ),
                ),
            )
        }
        val alias = uniqueConnectionAlias(
            sourceAuthoredPath = request.sourceAuthoredPath,
            targetAuthoredPath = request.targetAuthoredPath,
            ast = request.document.ast,
        )
        val semanticId = "connection:${request.document.sourceUnitId.toPortableSourceUnitId()}:$alias"
        return planned(
            request = request,
            insertionOffset = insertionOffset,
            admittedText = "\n\n  connect $alias ${request.sourceAuthoredPath} to ${request.targetAuthoredPath}\n",
            affectedSemanticIds = listOf(semanticId),
        )
    }

    private fun planLayout(
        request: BackendAuthoredLayoutPlanningRequest,
        insertionOffset: Int,
    ): BackendAuthoringSourceEditPlanningResult {
        val source = layoutSerializer.serialize(request.intent)
            .lineSequence()
            .joinToString(separator = "\n") { line -> "  $line" }
        return planned(
            request = request,
            insertionOffset = insertionOffset,
            admittedText = "\n\n$source\n",
            affectedSemanticIds = listOf(request.subjectSemanticId),
        )
    }

    private fun planCabinetPlacement(
        request: BackendCabinetPlacementPlanningRequest,
    ): BackendAuthoringSourceEditPlanningResult {
        if (!request.deltaXMillimeters.isFinite() || !request.deltaYMillimeters.isFinite()) {
            return invalidSource("Cabinet placement deltas must be finite millimetre values.")
        }
        val mountId = request.occurrenceId.removePrefix("cabinet:")
        val matches = request.document.ast.declarations
            .filterIsInstance<InstallationDeclaration>()
            .flatMap { installation ->
                installation.mounts
                    .filter { mount -> mount.id == mountId }
                    .map { mount -> installation to mount }
            }
        if (matches.size != 1) {
            return invalidSource(
                "Cabinet occurrence `${request.occurrenceId}` must resolve to exactly one authored installation mount.",
            )
        }
        val (installation, mount) = matches.single()
        if (mount.at.x.unit != "mm" || mount.at.y.unit != "mm") {
            return invalidSource("Cabinet placement editing currently requires authored millimetre coordinates.")
        }
        val targetOrientation = installation.rails
            .singleOrNull { rail -> rail.id == mount.targetId }
            ?.orientation
            ?: installation.terminalGroups
                .singleOrNull { group -> group.id == mount.targetId }
                ?.orientation
            ?: InstallationOrientation.Horizontal
        val canvasDeltaX = request.deltaXMillimeters.roundToInt()
        val canvasDeltaY = request.deltaYMillimeters.roundToInt()
        val (deltaAlong, deltaNormal) = when (targetOrientation) {
            InstallationOrientation.Horizontal -> canvasDeltaX to canvasDeltaY
            InstallationOrientation.Vertical -> canvasDeltaY to -canvasDeltaX
        }
        val along = mount.at.x.value.roundToInt() + deltaAlong
        val normal = mount.at.y.value.roundToInt() + deltaNormal
        if (along < 0 || normal < 0) {
            return invalidSource("Cabinet placement move would produce negative target-local coordinates.")
        }
        val admittedText = "(${along}mm, ${normal}mm)"
        val replacement = BackendAuthoringSourceOffsetRange(
            startOffset = mount.at.span.start.offset,
            endOffset = mount.at.span.end.offset,
        )
        val proposedSource = request.document.sourceText.substring(0, replacement.startOffset) +
            admittedText + request.document.sourceText.substring(replacement.endOffset)
        val parseResult = parser.parse(request.document.sourceUri, proposedSource)
        if (parseResult is ParseFailure) {
            return invalidSource(
                parseResult.diagnostics.joinToString(separator = "; ") { diagnostic -> diagnostic.message },
            )
        }
        return BackendAuthoringSourceEditPlanned(
            BackendAuthoringSourceEditPlan(
                revisionGuard = request.revisionGuard,
                sourceUri = request.document.sourceUri,
                replacement = replacement,
                admittedText = admittedText,
                selection = replacement.copy(endOffset = replacement.startOffset + admittedText.length),
                affectedSemanticIds = listOf("component:${mount.deviceId}"),
            ),
        )
    }

    private fun invalidSource(message: String): BackendAuthoringSourceEditRejected =
        BackendAuthoringSourceEditRejected(
            diagnostics = listOf(
                AuthoringDiagnostic(
                    code = AuthoringDiagnosticCode.SOURCE_INVALID,
                    message = message,
                    authority = AuthoringDiagnosticAuthority.SOURCE_PLANNING,
                    lifecycleStage = AuthoringLifecycleState.BLOCKED,
                    recoveryAction = AuthoringRecoveryAction.FIX_SOURCE,
                ),
            ),
        )

    private fun planned(
        request: BackendAuthoringSourceEditPlanningRequest,
        insertionOffset: Int,
        admittedText: String,
        affectedSemanticIds: List<String>,
    ): BackendAuthoringSourceEditPlanningResult {
        val contentStart = admittedText.indexOfFirst { character -> !character.isWhitespace() }
        val contentEnd = admittedText.indexOfLast { character -> !character.isWhitespace() } + 1
        val proposedSource = request.document.sourceText.substring(0, insertionOffset) +
            admittedText + request.document.sourceText.substring(insertionOffset)
        val parseResult = parser.parse(request.document.sourceUri, proposedSource)
        if (parseResult is ParseFailure) {
            return BackendAuthoringSourceEditRejected(
                diagnostics = listOf(
                    AuthoringDiagnostic(
                        code = AuthoringDiagnosticCode.SOURCE_INVALID,
                        message = parseResult.diagnostics.joinToString(separator = "; ") { diagnostic -> diagnostic.message },
                        authority = AuthoringDiagnosticAuthority.PARSER,
                        lifecycleStage = AuthoringLifecycleState.BLOCKED,
                        recoveryAction = AuthoringRecoveryAction.FIX_SOURCE,
                    ),
                ),
            )
        }
        return BackendAuthoringSourceEditPlanned(
            BackendAuthoringSourceEditPlan(
                revisionGuard = request.revisionGuard,
                sourceUri = request.document.sourceUri,
                replacement = BackendAuthoringSourceOffsetRange(insertionOffset, insertionOffset),
                admittedText = admittedText,
                selection = if (contentStart < 0) null else BackendAuthoringSourceOffsetRange(
                    startOffset = insertionOffset + contentStart,
                    endOffset = insertionOffset + contentEnd,
                ),
                affectedSemanticIds = affectedSemanticIds.sorted(),
            ),
        )
    }

    private fun uniqueEntityName(requestedName: String, ast: SourceFileAst): String {
        val existing = ast.declarations
            .filterIsInstance<com.engineeringood.athena.language.DeviceDeclaration>()
            .mapTo(mutableSetOf()) { declaration -> declaration.name }
        val base = requestedName
            .replace(Regex("[^A-Za-z0-9_]"), "")
            .ifBlank { "Entity" }
            .let { value -> if (value.first().isLetter()) value else "Entity$value" }
        if (base !in existing) return base
        return generateSequence(2) { ordinal -> ordinal + 1 }
            .map { ordinal -> "$base$ordinal" }
            .first { candidate -> candidate !in existing }
    }

    private fun uniqueConnectionAlias(
        sourceAuthoredPath: String,
        targetAuthoredPath: String,
        ast: SourceFileAst,
    ): String {
        val existing = ast.declarations.flatMap { declaration ->
            when (declaration) {
                is com.engineeringood.athena.language.ConnectionDeclaration -> listOf(declaration.alias)
                is com.engineeringood.athena.language.ConnectionGroupDeclaration -> {
                    declaration.connections.map { connection -> connection.alias }
                }
                else -> emptyList()
            }
        }.toSet()
        val base = "${sourceAuthoredPath}_to_${targetAuthoredPath}"
            .replace(Regex("[^A-Za-z0-9_]"), "_")
            .replace(Regex("_+"), "_")
            .trim('_')
            .lowercase()
            .ifBlank { "connection" }
            .let { value -> if (value.first().isLetter()) value else "connection_$value" }
        if (base !in existing) return base
        return generateSequence(2) { ordinal -> ordinal + 1 }
            .map { ordinal -> "${base}_$ordinal" }
            .first { candidate -> candidate !in existing }
    }

    private fun renderEntity(
        entityName: String,
        intent: CreateSemanticEntityIntent,
        template: EngineeringConceptTemplate,
    ): String = buildString {
        appendLine()
        appendLine()
        append("  device ")
        append(entityName)
        appendLine(" {")
        append("    type ")
        append(template.semanticType.value)
        appendLine()
        append("    componentRef \"")
        append(template.conceptId.value.escapeAthenaString())
        appendLine("\"")
        template.propertySchema.forEach { property ->
            val authored = intent.properties.entries.firstOrNull { entry -> entry.key.value == property.name }?.value
            val value = authored ?: property.defaultValue?.let(AuthoringValue::Text)
            if (value != null) {
                append("    ")
                append(property.name)
                append(' ')
                append(renderValue(value, property.valueKind))
                appendLine()
            }
        }
        template.defaultModel
            ?.takeIf { template.propertySchema.none { property -> property.name == "model" } }
            ?.let { defaultModel ->
                append("    model \"")
                append(defaultModel.escapeAthenaString())
                appendLine("\"")
            }
        template.nestedPorts.forEach { port ->
            appendLine()
            append("    port ")
            append(port.name)
            appendLine(" {")
            append("      direction ")
            append(port.direction.render())
            appendLine()
            append("      signal ")
            append(port.signalOrMedium.value)
            appendLine()
            port.terminalNumber?.let { terminalNumber ->
                append("      terminal \"")
                append(terminalNumber.escapeAthenaString())
                appendLine("\"")
            }
            appendLine("    }")
        }
        appendLine("  }")
    }

    private fun renderValue(value: AuthoringValue, kind: EngineeringConceptPropertyValueKind): String {
        return when (value) {
            is AuthoringValue.Text -> "\"${value.text.escapeAthenaString()}\""
            is AuthoringValue.Symbol -> if (kind == EngineeringConceptPropertyValueKind.TEXT) {
                "\"${value.text.escapeAthenaString()}\""
            } else {
                value.text
            }
            is AuthoringValue.BooleanValue -> value.value.toString()
            is AuthoringValue.IntegerValue -> value.value.toString()
        }
    }

    private fun EngineeringConceptPortDirection.render(): String = name.lowercase()

    private fun String.escapeAthenaString(): String = replace("\\", "\\\\").replace("\"", "\\\"")
}

private fun String.toPortableSourceUnitId(): String {
    val normalized = replace('\\', '/')
    val examplesIndex = normalized.indexOf("/examples/")
    if (examplesIndex >= 0) {
        return normalized.substring(examplesIndex + 1)
    }
    val srcIndex = normalized.indexOf("/src/")
    if (srcIndex >= 0) {
        return normalized.substring(srcIndex + 1)
    }
    return normalized
}
