package com.engineeringood.athena.interaction

import com.engineeringood.athena.presentation.InputRevision
import com.engineeringood.athena.presentation.SceneId
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

private val UUID_V4 = Regex("^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")
private val SHA256 = Regex("^[0-9a-f]{64}$")
private val TRACE_ID = Regex("^trace:sha256:[0-9a-f]{64}$")
private val ROOT_ID = Regex("^source-root:sha256:[0-9a-f]{64}$")

enum class EditAuthorityClass { PRESENTATION, REPRESENTATION, ENGINEERING, JOURNAL }

enum class EditOperationKind(val authorityClass: EditAuthorityClass) {
    MOVE_OCCURRENCE(EditAuthorityClass.PRESENTATION),
    ALIGN_OCCURRENCES(EditAuthorityClass.PRESENTATION),
    DISTRIBUTE_OCCURRENCES(EditAuthorityClass.PRESENTATION),
    SNAP_OCCURRENCE_TO_GRID(EditAuthorityClass.PRESENTATION),
    SET_STYLE(EditAuthorityClass.PRESENTATION),
    CHANGE_SYMBOL(EditAuthorityClass.REPRESENTATION),
    CONNECT_PORTS(EditAuthorityClass.ENGINEERING),
    RECONNECT_CONNECTION_ENDPOINT(EditAuthorityClass.ENGINEERING),
    ADJUST_CONNECTION_ROUTE(EditAuthorityClass.PRESENTATION),
    BIND_PART(EditAuthorityClass.ENGINEERING),
    ADD_PACKAGE_DEPENDENCY(EditAuthorityClass.ENGINEERING),
    INSERT_ELEMENT_OCCURRENCE(EditAuthorityClass.REPRESENTATION),
    INSERT_MACRO_OCCURRENCES(EditAuthorityClass.REPRESENTATION),
    UNDO(EditAuthorityClass.JOURNAL),
    REDO(EditAuthorityClass.JOURNAL),
}

enum class LockAction { PRESERVE, LOCK, UNLOCK }
enum class AlignmentAxis { LEFT, CENTER_X, RIGHT, TOP, CENTER_Y, BOTTOM }
enum class DistributionAxis { HORIZONTAL, VERTICAL }
enum class StyleTargetKind { ROLE, OCCURRENCE }
enum class RouteMarker { NONE, END_ARROW }
enum class PortDisplay { HIDDEN, MARKER, MARKER_AND_LABEL }

data class RevisionDigest(val state: State, val sha256: String? = null) {
    enum class State { PRESENT, ABSENT }

    init {
        when (state) {
            State.PRESENT -> require(sha256 != null && SHA256.matches(sha256))
            State.ABSENT -> require(sha256 == null)
        }
    }

    fun canonicalValue(): String = sha256 ?: "absent"

    companion object {
        fun present(sha256: String) = RevisionDigest(State.PRESENT, sha256)
        fun absent() = RevisionDigest(State.ABSENT)
    }
}

data class PackageItemDigest(val packageId: String, val itemId: String, val sha256: String) {
    init {
        require(packageId.isNotBlank() && itemId.isNotBlank() && SHA256.matches(sha256))
    }

    val key: String get() = "$packageId/$itemId"
}

data class SourceRevision(
    val sceneInputRevision: InputRevision,
    val sourceRootIdentity: String,
    val engineeringSourceDigest: String,
    val sheetDigest: String,
    val styleDigest: RevisionDigest,
    val lockDigest: RevisionDigest,
    val packageItemDigests: List<PackageItemDigest>,
    val compilerVersion: String,
    val sceneSchemaVersion: String,
    val profileVersion: String,
) {
    init {
        require(ROOT_ID.matches(sourceRootIdentity))
        require(SHA256.matches(engineeringSourceDigest) && SHA256.matches(sheetDigest))
        require(packageItemDigests == packageItemDigests.sortedBy(PackageItemDigest::key))
        require(packageItemDigests.map(PackageItemDigest::key).distinct().size == packageItemDigests.size)
        require(compilerVersion.isNotBlank() && sceneSchemaVersion.isNotBlank() && profileVersion.isNotBlank())
    }

    val token: String by lazy {
        val digest = MessageDigest.getInstance("SHA-256")
        listOf(
            sceneInputRevision.value,
            sourceRootIdentity,
            engineeringSourceDigest,
            sheetDigest,
            styleDigest.canonicalValue(),
            lockDigest.canonicalValue(),
            compilerVersion,
            sceneSchemaVersion,
            profileVersion,
        ).forEach { digest.updateRecord(it) }
        packageItemDigests.forEach { digest.updateRecord("${it.key}:${it.sha256}") }
        "source-revision:sha256:${digest.digest().toHex()}"
    }
}

data class OperationTarget(val identities: List<String>) {
    init {
        require(identities.isNotEmpty() && identities.all(String::isNotBlank))
        require(identities == identities.sorted() && identities.distinct().size == identities.size)
    }
}

data class SourceTraceContext(val traceId: String, val subjectId: String) {
    init { require(TRACE_ID.matches(traceId) && subjectId.isNotBlank()) }
}

data class SheetPoint(val x: Int, val y: Int) {
    init {
        require(x > 0 && y > 0)
    }
}

data class StyleTarget(val kind: StyleTargetKind, val id: String) {
    init {
        require(id.isNotBlank())
        if (kind == StyleTargetKind.ROLE) require(id in setOf("default", "symbol", "connection", "label", "port"))
    }
}

data class StyleFields(
    val strokeRgba: String? = null,
    val fillRgba: String? = null,
    val strokeWidth: Int? = null,
    val dash: List<Int>? = null,
    val lineCap: String? = null,
    val lineJoin: String? = null,
    val opacity: Int? = null,
    val fontSize: Int? = null,
    val fontWeight: Int? = null,
    val routeMarker: RouteMarker? = null,
    val portDisplay: PortDisplay? = null,
) {
    init {
        require(listOf(strokeRgba, fillRgba, strokeWidth, dash, lineCap, lineJoin, opacity, fontSize, fontWeight, routeMarker, portDisplay).any { it != null })
        require(strokeRgba == null || Regex("^#[0-9a-f]{8}$").matches(strokeRgba))
        require(fillRgba == null || Regex("^#[0-9a-f]{8}$").matches(fillRgba))
        require(strokeWidth == null || strokeWidth > 0)
        require(dash == null || dash.all { it > 0 })
        require(lineCap == null || lineCap in setOf("butt", "round", "square"))
        require(lineJoin == null || lineJoin in setOf("miter", "round", "bevel"))
        require(opacity == null || opacity in 0..255)
        require(fontSize == null || fontSize > 0)
        require(fontWeight == null || fontWeight in 1..1000)
    }
}

sealed interface EditOperationBody { val kind: EditOperationKind }

data class MoveOccurrence(val sheetId: String, val occurrenceId: String, val point: SheetPoint, val lockAction: LockAction) : EditOperationBody {
    override val kind = EditOperationKind.MOVE_OCCURRENCE
    init { require(sheetId.isNotBlank() && occurrenceId.isNotBlank()) }
}

data class AlignOccurrences(val sheetId: String, val occurrenceIds: List<String>, val axis: AlignmentAxis) : EditOperationBody {
    override val kind = EditOperationKind.ALIGN_OCCURRENCES
    init { require(sheetId.isNotBlank() && occurrenceIds.size >= 2 && occurrenceIds == occurrenceIds.sorted() && occurrenceIds.distinct().size == occurrenceIds.size) }
}

data class DistributeOccurrences(val sheetId: String, val occurrenceIds: List<String>, val axis: DistributionAxis) : EditOperationBody {
    override val kind = EditOperationKind.DISTRIBUTE_OCCURRENCES
    init { require(sheetId.isNotBlank() && occurrenceIds.size >= 3 && occurrenceIds == occurrenceIds.sorted() && occurrenceIds.distinct().size == occurrenceIds.size) }
}

data class SnapOccurrenceToGrid(val sheetId: String, val occurrenceId: String, val point: SheetPoint) : EditOperationBody {
    override val kind = EditOperationKind.SNAP_OCCURRENCE_TO_GRID
    init { require(sheetId.isNotBlank() && occurrenceId.isNotBlank()) }
}

data class SetStyle(val sheetId: String, val target: StyleTarget, val fields: StyleFields) : EditOperationBody {
    override val kind = EditOperationKind.SET_STYLE
    init { require(sheetId.isNotBlank()) }
}

enum class RepresentationPlaceholderValueKind { TEXT, NUMBER, BOOLEAN }

data class RepresentationPlaceholderEditValue(
    val kind: RepresentationPlaceholderValueKind,
    val value: String,
) {
    init {
        require(
            when (kind) {
                RepresentationPlaceholderValueKind.TEXT -> true
                RepresentationPlaceholderValueKind.NUMBER -> value.matches(Regex("^-?(?:0|[1-9][0-9]*)(?:\\.[0-9]+)?$"))
                RepresentationPlaceholderValueKind.BOOLEAN -> value == "true" || value == "false"
            },
        )
    }
}

data class ChangeSymbol(
    val occurrenceId: String,
    val representationRef: String,
    val variantRef: String? = null,
    val placeholderValues: Map<String, RepresentationPlaceholderEditValue> = emptyMap(),
) : EditOperationBody {
    override val kind = EditOperationKind.CHANGE_SYMBOL
    init {
        require(occurrenceId.isNotBlank() && representationRef.isNotBlank())
        require(variantRef == null || variantRef.isNotBlank())
        require(placeholderValues.keys.all { it.matches(Regex("^[a-z][a-z0-9]*(?:[._-][a-z0-9]+)*$")) })
    }
}

data class BindPart(
    val functionId: String,
    val implementationRole: String,
    val partRef: String,
) : EditOperationBody {
    override val kind = EditOperationKind.BIND_PART
    init {
        require(functionId.matches(Regex("^function:[A-Za-z][A-Za-z0-9._-]*(?:\\.[A-Za-z][A-Za-z0-9._-]*)+$")))
        require(implementationRole.matches(Regex("^[a-z][a-z0-9]*(?:[._-][a-z0-9]+)*$")))
        require(partRef.matches(Regex("^[a-z][a-z0-9]*(?:\\.[a-z][a-z0-9]*)*/[a-z][a-z0-9]*(?:[._-][a-z0-9]+)*@[A-Za-z0-9][A-Za-z0-9.+_-]*$")))
    }
}

data class AddPackageDependency(val packageName: String, val packageVersion: String) : EditOperationBody {
    override val kind = EditOperationKind.ADD_PACKAGE_DEPENDENCY
    init {
        require(packageName.matches(Regex("^[a-z][a-z0-9]*(\\.[a-z][a-z0-9]*)*$")))
        require(packageVersion.matches(Regex("^[A-Za-z0-9][A-Za-z0-9.+_-]*$")))
    }
}

data class InsertElementOccurrence(
    val sheetId: String,
    val functionId: String,
    val elementRef: String,
    val column: Int,
    val row: Int,
    val subGridX: Int = 1,
    val subGridY: Int = 1,
) : EditOperationBody {
    override val kind = EditOperationKind.INSERT_ELEMENT_OCCURRENCE
    init {
        require(sheetId.isNotBlank())
        require(functionId.matches(Regex("^function:[A-Za-z][A-Za-z0-9._-]*(?:\\.[A-Za-z][A-Za-z0-9._-]*)+$")))
        require(elementRef.matches(Regex("^[a-z][a-z0-9]*(?:\\.[a-z][a-z0-9]*)*/[a-z][a-z0-9]*(?:[._-][a-z0-9]+)*@[A-Za-z0-9][A-Za-z0-9.+_-]*$")))
        require(column > 0 && row > 0 && subGridX > 0 && subGridY > 0)
    }
}

data class InsertMacroOccurrences(
    val sheetId: String,
    val macroRef: String,
    val functionIdsBySlot: Map<String, String>,
    val column: Int,
    val row: Int,
) : EditOperationBody {
    override val kind = EditOperationKind.INSERT_MACRO_OCCURRENCES
    init {
        require(sheetId.isNotBlank())
        require(macroRef.matches(Regex("^[a-z][a-z0-9]*(?:\\.[a-z][a-z0-9]*)*/[a-z][a-z0-9]*(?:[._-][a-z0-9]+)*@[A-Za-z0-9][A-Za-z0-9.+_-]*$")))
        require(functionIdsBySlot.isNotEmpty())
        require(functionIdsBySlot.keys.all { it.matches(Regex("^[a-z][a-z0-9]*(?:[._-][a-z0-9]+)*$")) })
        require(functionIdsBySlot.values.all { it.matches(Regex("^function:[A-Za-z][A-Za-z0-9._-]*(?:\\.[A-Za-z][A-Za-z0-9._-]*)+$")) })
        require(functionIdsBySlot.values.distinct().size == functionIdsBySlot.size)
        require(column > 0 && row > 0)
    }
}

data class Undo(val journalEntryId: String) : EditOperationBody {
    override val kind = EditOperationKind.UNDO
    init { require(journalEntryId.startsWith("journal:sha256:")) }
}

data class Redo(val journalEntryId: String) : EditOperationBody {
    override val kind = EditOperationKind.REDO
    init { require(journalEntryId.startsWith("journal:sha256:")) }
}

data class EditOperationEnvelope(
    val schemaVersion: Int = 1,
    val operationId: String,
    val sceneId: SceneId,
    val sourceRevision: SourceRevision,
    val target: OperationTarget,
    val sourceTrace: SourceTraceContext,
    val requestedWritableFiles: List<String>,
    val body: EditOperationBody,
) {
    val kind: EditOperationKind get() = body.kind
    val authorityClass: EditAuthorityClass get() = kind.authorityClass

    init {
        require(schemaVersion == 1 && UUID_V4.matches(operationId))
        require(requestedWritableFiles.isNotEmpty())
        requestedWritableFiles.forEach(::requireRepositoryRelativePath)
        require(requestedWritableFiles == requestedWritableFiles.sorted())
        require(requestedWritableFiles.distinct().size == requestedWritableFiles.size)
    }
}

enum class EditOperationStatus { ACCEPTED, REJECTED }
enum class OperationRejectionReason { STALE, UNAVAILABLE, INVALID, CONFLICT, READ_ONLY, IO_FAILURE, COMPILATION_FAILURE, UNSUPPORTED }

data class OperationDiagnostic(val subject: String, val problem: String, val correction: String, val code: String) {
    init { require(subject.isNotBlank() && problem.isNotBlank() && correction.isNotBlank() && code.isNotBlank()) }
}

data class EditOperationAcceptance(
    val previousSourceRevision: SourceRevision,
    val resultingSourceRevision: SourceRevision,
    val acceptedPatchSet: SourcePatchSet,
    val publicationCorrelationId: String,
    val journalEntryId: String,
)

data class EditOperationRejection(val reason: OperationRejectionReason, val diagnostics: List<OperationDiagnostic>) {
    init { require(diagnostics.isNotEmpty()) }
}

data class EditOperationResult(
    val schemaVersion: Int = 1,
    val status: EditOperationStatus,
    val operationId: String,
    val currentSourceRevision: SourceRevision,
    val acceptance: EditOperationAcceptance? = null,
    val rejection: EditOperationRejection? = null,
) {
    init {
        require(schemaVersion == 1 && UUID_V4.matches(operationId))
        when (status) {
            EditOperationStatus.ACCEPTED -> require(acceptance != null && rejection == null)
            EditOperationStatus.REJECTED -> require(acceptance == null && rejection != null)
        }
    }

    companion object {
        fun rejected(operationId: String, revision: SourceRevision, reason: OperationRejectionReason, diagnostics: List<OperationDiagnostic>) =
            EditOperationResult(status = EditOperationStatus.REJECTED, operationId = operationId, currentSourceRevision = revision, rejection = EditOperationRejection(reason, diagnostics))
    }
}

internal fun requireRepositoryRelativePath(relativePath: String) {
    require(relativePath.isNotBlank())
    require(relativePath == relativePath.replace('\\', '/'))
    require(!relativePath.startsWith('/') && !Regex("^[A-Za-z]:").containsMatchIn(relativePath))
    require(relativePath.split('/').none { it.isBlank() || it == "." || it == ".." })
}

private fun MessageDigest.updateRecord(value: String) {
    update(value.toByteArray(StandardCharsets.UTF_8))
    update(0)
}

private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
