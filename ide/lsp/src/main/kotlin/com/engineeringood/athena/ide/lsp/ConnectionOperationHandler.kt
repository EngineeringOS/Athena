package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.compiler.AthenaDiagramSceneCompiler
import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import com.engineeringood.athena.interaction.AdjustConnectionRoute
import com.engineeringood.athena.interaction.ConnectPorts
import com.engineeringood.athena.interaction.ConnectionEditEndpointRole
import com.engineeringood.athena.interaction.ConnectionEditKind
import com.engineeringood.athena.interaction.ConnectionRequirementIntent
import com.engineeringood.athena.interaction.ConnectionRequirementKind
import com.engineeringood.athena.interaction.ConnectionRequirementValue
import com.engineeringood.athena.interaction.EditOperationBody
import com.engineeringood.athena.interaction.EditOperationEnvelope
import com.engineeringood.athena.interaction.EditOperationResult
import com.engineeringood.athena.interaction.OperationRejectionReason
import com.engineeringood.athena.interaction.ReconnectConnectionEndpoint
import com.engineeringood.athena.interaction.SourceFilePatch
import com.engineeringood.athena.interaction.SourcePatchSet
import com.engineeringood.athena.language.AthenaLanguageParser
import com.engineeringood.athena.language.AthenaSheetCompanionParser
import com.engineeringood.athena.language.ConnectionDeclaration
import com.engineeringood.athena.language.ConnectionSourceEditResult
import com.engineeringood.athena.language.ConnectionSourceEditor
import com.engineeringood.athena.language.ConnectionSourceEndpoint
import com.engineeringood.athena.language.ConnectionSourceInsertion
import com.engineeringood.athena.language.ConnectionSourceKind
import com.engineeringood.athena.language.ConnectionSourceProperty
import com.engineeringood.athena.language.ConnectionSourceReconnect
import com.engineeringood.athena.language.ConnectionSourceValue
import com.engineeringood.athena.language.ParseSuccess
import com.engineeringood.athena.language.SheetCompanionEditor
import com.engineeringood.athena.language.PageCompanionFound
import com.engineeringood.athena.language.SheetCompanionParseSuccess
import com.engineeringood.athena.language.SheetPoint
import com.engineeringood.athena.language.SheetRouteConstraintWrite
import com.engineeringood.athena.presentation.AthenaDiagramScene
import com.engineeringood.athena.presentation.PublicationState
import java.nio.file.Files
import java.nio.file.Path

/** Owns M46 engineering Connection and presentation route-constraint source transactions. */
internal class ConnectionOperationHandler(
    private val host: AthenaLspSessionHostReady,
    private val publicationService: AthenaDiagramPublicationService,
    private val revisionService: SourceRevisionService,
    private val transactionEngine: SourceTransactionEngine,
    private val connectionEditor: ConnectionSourceEditor = ConnectionSourceEditor(),
    private val sheetEditor: SheetCompanionEditor = SheetCompanionEditor(),
    private val parser: AthenaLanguageParser = AthenaLanguageParser(),
) {
    fun apply(operation: EditOperationEnvelope, body: EditOperationBody): EditOperationResult {
        val currentRevision = revisionService.current()
        val publication = publicationService.current()
        val scene = publication.scene
        if (publication.state != PublicationState.READY || scene == null) {
            return reject(operation, currentRevision, body.kind.name, "Canonical Scene is unavailable for this connection edit.", "Refresh a READY engineering document and retry.", "connection.edit.scene-unavailable")
        }
        return when (body) {
            is ConnectPorts -> connect(operation, body, scene, currentRevision)
            is ReconnectConnectionEndpoint -> reconnect(operation, body, scene, currentRevision)
            is AdjustConnectionRoute -> adjustRoute(operation, body, scene, currentRevision)
            else -> error("Unsupported Connection operation ${body.kind}")
        }
    }

    private fun connect(
        operation: EditOperationEnvelope,
        body: ConnectPorts,
        scene: AthenaDiagramScene,
        currentRevision: com.engineeringood.athena.interaction.SourceRevision,
    ): EditOperationResult {
        val sourceEndpoint = body.endpoints.single { it.role == ConnectionEditEndpointRole.SOURCE }
        val sinkEndpoint = body.endpoints.single { it.role == ConnectionEditEndpointRole.SINK }
        val sourcePort = scene.packageBackedPort(sourceEndpoint.portId)
            ?: return rejectPort(operation, currentRevision, sourceEndpoint.portId)
        val sinkPort = scene.packageBackedPort(sinkEndpoint.portId)
            ?: return rejectPort(operation, currentRevision, sinkEndpoint.portId)
        if (sourcePort.direction.name !in setOf("OUT", "BIDIRECTIONAL") || sinkPort.direction.name !in setOf("IN", "BIDIRECTIONAL")) {
            return reject(operation, currentRevision, "${sourceEndpoint.portId} -> ${sinkEndpoint.portId}", "Connection endpoints do not flow from an output-capable Port to an input-capable Port.", "Choose compatible package-backed Port directions.", "connection.edit.direction-incompatible")
        }
        if (!scene.traceMatchesPort(operation, sourcePort.semanticPortId, sourcePort.traceId.value)) {
            return reject(operation, currentRevision, sourcePort.semanticPortId, "Connection source Port does not match supplied Source Trace.", "Refresh Port selection and retry.", "connection.edit.trace-invalid")
        }
        val before = Files.readString(host.sourcePath)
        val edited = runCatching {
            connectionEditor.insertConnection(
                host.sourcePath.toString(),
                before,
                ConnectionSourceInsertion(
                    kind = body.connectionKind.toSourceKind(),
                    sourcePort = sourceEndpoint.portId.removePrefix("port:"),
                    targetPort = sinkEndpoint.portId.removePrefix("port:"),
                    properties = body.requirements.map { it.toSourceProperty() },
                ),
            )
        }.getOrElse { failure ->
            return reject(operation, currentRevision, "${sourceEndpoint.portId} -> ${sinkEndpoint.portId}", failure.message ?: "Connection intent cannot be authored.", "Correct the Connection endpoints and requirements.", "connection.edit.source-invalid")
        }
        return applyEngineeringPatch(operation, body, before, edited, currentRevision, scene.snapGrid.sheetId) { compiled ->
            val endpoints = setOf(sourceEndpoint.portId, sinkEndpoint.portId)
            val created = compiled.document.connections.singleOrNull { connection ->
                connection.kind.name == body.connectionKind.name && connection.endpoints.mapNotNull { it.port.resolvedIdentity?.value }.toSet() == endpoints
            } ?: stagedFailure(body.kind.name, "Compiled Connection does not preserve requested endpoint and kind intent.", "Correct Connection compatibility and physical requirements.", "connection.edit.intent-not-realized")
            created.id.value
        }
    }

    private fun reconnect(
        operation: EditOperationEnvelope,
        body: ReconnectConnectionEndpoint,
        scene: AthenaDiagramScene,
        currentRevision: com.engineeringood.athena.interaction.SourceRevision,
    ): EditOperationResult {
        val currentSceneConnection = scene.connections.singleOrNull { it.connectionId == body.connectionId }
            ?: return reject(operation, currentRevision, body.connectionId, "Connection is not present in the accepted Canonical Scene.", "Refresh the current Connection selection and retry.", "connection.edit.connection-missing")
        val replacement = scene.packageBackedPort(body.replacementPortId)
            ?: return rejectPort(operation, currentRevision, body.replacementPortId)
        val expectedDirection = when (body.endpointRole) {
            ConnectionEditEndpointRole.SOURCE -> setOf("OUT", "BIDIRECTIONAL")
            ConnectionEditEndpointRole.SINK -> setOf("IN", "BIDIRECTIONAL")
        }
        if (replacement.direction.name !in expectedDirection) {
            return reject(operation, currentRevision, body.replacementPortId, "Replacement Port direction is incompatible with ${body.endpointRole.name.lowercase()} endpoint role.", "Choose a compatible package-backed Port.", "connection.edit.direction-incompatible")
        }
        if (!scene.traceMatchesConnection(operation, currentSceneConnection.traceId.value, body.connectionId)) {
            return reject(operation, currentRevision, body.connectionId, "Connection does not match supplied Source Trace.", "Refresh Connection selection and retry.", "connection.edit.trace-invalid")
        }
        val before = Files.readString(host.sourcePath)
        val compiled = currentCompilation() ?: return rejectedCompilation(operation, currentRevision)
        val current = compiled.document.connections.singleOrNull { it.id.value == body.connectionId }
            ?: return reject(operation, currentRevision, body.connectionId, "Connection is not authored in current Engineering Reality.", "Refresh the current Connection and retry.", "connection.edit.connection-missing")
        val parsed = parser.parse(host.sourcePath.toString(), before) as? ParseSuccess
            ?: return reject(operation, currentRevision, host.sourcePath.fileName.toString(), "Engineering source does not parse.", "Correct source before reconnecting.", "connection.edit.source-invalid")
        val sourcePath = current.endpoints.single { it.role.name == "SOURCE" }.port.authoredPath
        val sinkPath = current.endpoints.single { it.role.name == "SINK" }.port.authoredPath
        val declaration = parsed.ast.declarations.filterIsInstance<ConnectionDeclaration>().singleOrNull {
            it.kind.value == current.kind.name.lowercase().replace('_', '-') &&
                it.source.parts == sourcePath && it.target.parts == sinkPath
        } ?: return reject(operation, currentRevision, body.connectionId, "Connection source declaration cannot be resolved uniquely.", "Correct duplicate or stale Connection source.", "connection.edit.source-unresolved")
        val edited = runCatching {
            connectionEditor.reconnectConnection(
                host.sourcePath.toString(),
                before,
                ConnectionSourceReconnect(
                    declarationSpan = declaration.span,
                    endpoint = if (body.endpointRole == ConnectionEditEndpointRole.SOURCE) ConnectionSourceEndpoint.SOURCE else ConnectionSourceEndpoint.TARGET,
                    replacementPort = body.replacementPortId.removePrefix("port:"),
                ),
            )
        }.getOrElse { failure ->
            return reject(operation, currentRevision, body.connectionId, failure.message ?: "Connection endpoint cannot be changed.", "Choose a distinct compatible current Port.", "connection.edit.source-invalid")
        }
        return applyEngineeringPatch(operation, body, before, edited, currentRevision, scene.snapGrid.sheetId) { next ->
            val expectedPaths = setOf(
                if (body.endpointRole == ConnectionEditEndpointRole.SOURCE) body.replacementPortId else "port:${sourcePath.joinToString(".")}",
                if (body.endpointRole == ConnectionEditEndpointRole.SINK) body.replacementPortId else "port:${sinkPath.joinToString(".")}",
            )
            next.document.connections.singleOrNull { candidate ->
                candidate.kind == current.kind && candidate.endpoints.mapNotNull { it.port.resolvedIdentity?.value }.toSet() == expectedPaths
            }?.id?.value ?: stagedFailure(body.connectionId, "Recompiled Connection does not preserve reconnect intent.", "Choose a compatible replacement Port.", "connection.edit.intent-not-realized")
        }
    }

    private fun adjustRoute(
        operation: EditOperationEnvelope,
        body: AdjustConnectionRoute,
        scene: AthenaDiagramScene,
        currentRevision: com.engineeringood.athena.interaction.SourceRevision,
    ): EditOperationResult {
        val selected = scene.connections.singleOrNull { it.connectionId == body.connectionId }
            ?: return reject(operation, currentRevision, body.connectionId, "Connection is not present in the accepted Canonical Scene.", "Refresh Connection selection and retry.", "route.edit.connection-missing")
        if (!scene.traceMatchesConnection(operation, selected.traceId.value, body.connectionId)) {
            return reject(operation, currentRevision, body.connectionId, "Connection does not match supplied Source Trace.", "Refresh Connection selection and retry.", "route.edit.trace-invalid")
        }
        if (scene.snapGrid.sheetId != body.sheetId) {
            return reject(operation, currentRevision, body.sheetId, "Target Sheet is not the accepted Connection Sheet.", "Refresh the active Sheet and retry.", "route.edit.sheet-mismatch")
        }
        val current = currentCompilation() ?: return rejectedCompilation(operation, currentRevision)
        val projection = current.projections.flatMap { it.connections }.singleOrNull {
            it.projectionId.value == body.projectionId && it.semanticId.value == body.connectionId
        } ?: return reject(operation, currentRevision, body.projectionId, "Connection Projection is not present in current Projection Reality.", "Refresh the current route and retry.", "route.edit.projection-missing")
        val sheet = host.activePageCompanionLocation() as? PageCompanionFound
            ?: return reject(operation, currentRevision, body.sheetId, "Page Companion is unavailable.", "Restore the active Folio Page Companion.", "page.companion.unavailable")
        val before = Files.readString(sheet.path)
        val targetId = "${body.target.kind.name.lowercase()}:${body.target.ordinal}"
        val edited = runCatching {
            sheetEditor.writeRouteConstraints(
                before,
                listOf(SheetRouteConstraintWrite(body.projectionId, targetId, SheetPoint(body.point.column, body.point.row))),
            )
        }.getOrElse { failure ->
            return reject(operation, currentRevision, body.connectionId, failure.message ?: "Logical route constraint is invalid.", "Choose a logical point inside the active Sheet.", "route.edit.constraint-invalid")
        }
        if (!edited.changed) {
            return reject(operation, currentRevision, body.connectionId, "Route adjustment would not change Sheet intent.", "Choose a different logical route point.", "edit.operation.noop")
        }
        val staged = revisionService.current(sheetCompanionBytesOverride = edited.updatedSource.toByteArray())
        val patchSet = SourcePatchSet(listOf(SourceFilePatch(relative(sheet.path), before, edited.updatedSource)))
        return transactionEngine.execute(operation, patchSet, staged) {
            val parsedSheet = AthenaSheetCompanionParser().parse(sheet.path.toString(), edited.updatedSource) as? SheetCompanionParseSuccess
                ?: stagedFailure(body.sheetId, "Edited Sheet route constraint does not parse.", "Correct logical route intent.", "route.edit.sheet-invalid")
            val compiled = host.executionContext.compiler().compile(host.sourcePath, parsedSheet.source) as? CompilerCompilationSuccess
                ?: stagedFailure(body.connectionId, "Route adjustment does not compile.", "Correct logical route intent and retry.", "route.edit.compilation-failed")
            val nextProjection = compiled.projections.flatMap { it.connections }.singleOrNull { it.projectionId == projection.projectionId }
                ?: stagedFailure(body.projectionId, "Route adjustment lost Connection Projection identity.", "Preserve current Connection Projection identity.", "route.edit.identity-changed")
            if (nextProjection.semanticId != projection.semanticId || nextProjection.logicalRouteConstraints.none { it.targetId.value == targetId }) {
                stagedFailure(body.connectionId, "Route adjustment changed engineering identity or was not admitted.", "Change presentation route intent only.", "route.edit.authority-violation")
            }
            val raw = AthenaDiagramSceneCompiler().compile(compiled, staged.sceneInputRevision, scene.snapGrid.sheetId).scene
                ?: stagedFailure(body.connectionId, "Route adjustment does not produce a complete Canonical Scene.", "Choose a route point compatible with current layout.", "route.edit.scene-invalid")
            host.packageBackedScene(raw)
                ?: stagedFailure(body.connectionId, "Route adjustment does not preserve PACKAGE_READY representations.", "Restore current package-backed symbols.", "route.edit.representation-invalid")
        }
    }

    private fun applyEngineeringPatch(
        operation: EditOperationEnvelope,
        body: EditOperationBody,
        before: String,
        edited: ConnectionSourceEditResult,
        currentRevision: com.engineeringood.athena.interaction.SourceRevision,
        activeSheetId: String,
        validateIntent: (CompilerCompilationSuccess) -> String,
    ): EditOperationResult {
        if (!edited.changed) {
            return reject(operation, currentRevision, body.kind.name, "Connection edit would not change Engineering Reality.", "Choose different compatible Connection intent.", "edit.operation.noop")
        }
        val staged = revisionService.current(engineeringSourceBytesOverride = edited.updatedSource.toByteArray())
        val patchSet = SourcePatchSet(listOf(SourceFilePatch(relative(host.sourcePath), before, edited.updatedSource)))
        return transactionEngine.execute(operation, patchSet, staged) {
            val compiled = host.executionContext.compiler().compile(host.sourcePath, edited.updatedSource) as? CompilerCompilationSuccess
                ?: stagedFailure(body.kind.name, "Connection edit does not parse and compile.", "Correct Connection endpoints and requirements.", "connection.edit.compilation-failed")
            validateIntent(compiled)
            val sceneResult = AthenaDiagramSceneCompiler().compile(compiled, staged.sceneInputRevision, activeSheetId)
            val raw = sceneResult.scene
                ?: stagedFailure(
                    body.kind.name,
                    "Connection edit does not produce a complete Canonical Scene: ${sceneResult.diagnostics.joinToString { diagnostic -> diagnostic.problem }}",
                    "Correct Port compatibility and Connection requirements.",
                    "connection.edit.scene-invalid",
                )
            host.packageBackedScene(raw)
                ?: stagedFailure(body.kind.name, "Connection edit does not preserve PACKAGE_READY representations.", "Use package-backed compatible Port anchors.", "connection.edit.representation-invalid")
        }
    }

    private fun currentCompilation(): CompilerCompilationSuccess? =
        host.executionContext.compiler().compile(host.sourcePath) as? CompilerCompilationSuccess

    private fun rejectedCompilation(operation: EditOperationEnvelope, revision: com.engineeringood.athena.interaction.SourceRevision) =
        reject(operation, revision, host.sourcePath.fileName.toString(), "Current engineering source does not compile.", "Correct current source before editing Connections.", "connection.edit.source-invalid")

    private fun AthenaDiagramScene.packageBackedPort(portId: String) = occurrences.asSequence()
        .filter { it.assetId != null && it.representationRef != null }
        .flatMap { it.ports.asSequence() }
        .singleOrNull { it.semanticPortId == portId }

    private fun AthenaDiagramScene.traceMatchesPort(operation: EditOperationEnvelope, portId: String, traceId: String): Boolean =
        operation.sourceTrace.traceId == traceId && traces.any { trace ->
            trace.traceId.value == traceId && trace.origins.any { it.primary && it.subjectId in setOf(portId, operation.sourceTrace.subjectId) }
        }

    private fun AthenaDiagramScene.traceMatchesConnection(operation: EditOperationEnvelope, traceId: String, connectionId: String): Boolean =
        operation.sourceTrace.traceId == traceId && operation.sourceTrace.subjectId == connectionId && traces.any { trace ->
            trace.traceId.value == traceId && trace.origins.any { it.primary && it.subjectId == connectionId }
        }

    private fun rejectPort(operation: EditOperationEnvelope, revision: com.engineeringood.athena.interaction.SourceRevision, portId: String) =
        reject(operation, revision, portId, "Port is not a package-backed anchor in the accepted Canonical Scene.", "Choose a visible Port supplied by an admitted Element representation.", "connection.edit.port-unavailable")

    private fun reject(
        operation: EditOperationEnvelope,
        revision: com.engineeringood.athena.interaction.SourceRevision,
        subject: String,
        problem: String,
        correction: String,
        code: String,
    ) = rejected(operation, revision, OperationRejectionReason.INVALID, diagnostic(subject, problem, correction, code))

    private fun stagedFailure(subject: String, problem: String, correction: String, code: String): Nothing =
        throw StagedOperationFailure(OperationRejectionReason.COMPILATION_FAILURE, diagnostic(subject, problem, correction, code))

    private fun ConnectionEditKind.toSourceKind(): ConnectionSourceKind = ConnectionSourceKind.valueOf(name)

    private fun ConnectionRequirementIntent.toSourceProperty(): ConnectionSourceProperty = ConnectionSourceProperty(
        name = when (kind) {
            ConnectionRequirementKind.CROSS_SECTION -> "crossSection"
            ConnectionRequirementKind.COLOR_CODE -> "colorCode"
            ConnectionRequirementKind.CONDUCTOR_TYPE -> "conductorType"
            ConnectionRequirementKind.SHIELDING -> "shielding"
            ConnectionRequirementKind.SOURCE_TERMINATION -> "sourceTermination"
            ConnectionRequirementKind.TARGET_TERMINATION -> "targetTermination"
            ConnectionRequirementKind.REQUIRED_LENGTH -> "requiredLength"
        },
        value = when (val typed = value) {
            is ConnectionRequirementValue.Quantity -> ConnectionSourceValue.Quantity(typed.value, typed.unit)
            is ConnectionRequirementValue.Symbol -> ConnectionSourceValue.Symbol(typed.value)
            is ConnectionRequirementValue.Boolean -> ConnectionSourceValue.Boolean(typed.value)
        },
    )

    private fun relative(path: Path): String = host.repositoryRoot.toAbsolutePath().normalize()
        .relativize(path.toAbsolutePath().normalize()).toString().replace('\\', '/')
}
