package com.engineeringood.athena.language

enum class SheetPlacementLockIntent {
    PRESERVE,
    LOCK,
    UNLOCK,
}

data class SheetPlacementWrite(
    val occurrenceId: String,
    val point: SheetPoint,
    val lockIntent: SheetPlacementLockIntent = SheetPlacementLockIntent.PRESERVE,
) {
    init {
        require(occurrenceId.isNotBlank() && '"' !in occurrenceId && '\n' !in occurrenceId && '\r' !in occurrenceId) {
            "Occurrence identity must be a non-blank quoted-name-safe identity."
        }
    }
}

data class SheetPlacementWriteResult(
    val updatedSource: String,
    val changed: Boolean,
    val changedOccurrences: List<String>,
)

data class SheetRouteConstraintWrite(
    val projectionId: String,
    val targetId: String,
    val point: SheetPoint,
) {
    init {
        require(projectionId.isNotBlank() && '"' !in projectionId && '\n' !in projectionId && '\r' !in projectionId) {
            "Connection Projection identity must be a non-blank quoted-name-safe identity."
        }
        require(targetId.isNotBlank() && '"' !in targetId && '\n' !in targetId && '\r' !in targetId) {
            "Route target identity must be a non-blank quoted-name-safe identity."
        }
    }

    internal val key: Pair<String, String> get() = projectionId to targetId

    internal val displayKey: String get() = "$projectionId/$targetId"
}

data class SheetRouteConstraintWriteResult(
    val updatedSource: String,
    val changed: Boolean,
    val changedTargets: List<String>,
)

/** Deterministically patches authored placement statements without changing engineering source. */
class SheetCompanionEditor(
    private val parser: AthenaSheetCompanionParser = AthenaSheetCompanionParser(),
) {
    fun writePlacements(existingSource: String, placements: List<SheetPlacementWrite>): SheetPlacementWriteResult {
        require(placements.isNotEmpty()) { "At least one Sheet placement write is required." }
        val source = parse(existingSource)
        val writes = placements.sortedBy(SheetPlacementWrite::occurrenceId)
        require(writes.map(SheetPlacementWrite::occurrenceId).distinct().size == writes.size) {
            "Each occurrence may receive one placement write per operation."
        }

        val currentByOccurrence = source.placements.associateBy(SheetPlacementIntent::occurrence)
        val desired = writes.associateWith { write ->
            validate(write)
            val current = currentByOccurrence[write.occurrenceId]
            SheetPlacementState(
                x = write.point.x,
                y = write.point.y,
                locked = when (write.lockIntent) {
                    SheetPlacementLockIntent.PRESERVE -> current?.locked ?: false
                    SheetPlacementLockIntent.LOCK -> true
                    SheetPlacementLockIntent.UNLOCK -> false
                },
            )
        }
        val changedWrites = writes.filter { write ->
            desired.getValue(write) != currentByOccurrence[write.occurrenceId]?.toState()
        }
        if (changedWrites.isEmpty()) {
            return SheetPlacementWriteResult(existingSource, changed = false, changedOccurrences = emptyList())
        }

        val lines = existingSource.replace("\r\n", "\n").replace('\r', '\n').trimEnd().lines().toMutableList()
        val occurrenceLines = source.placements.associate { it.occurrence to (it.span.start.line - 1) }.toMutableMap()
        changedWrites.filter { it.occurrenceId in occurrenceLines }.forEach { write ->
            val lineIndex = occurrenceLines.getValue(write.occurrenceId)
            val indentation = lines[lineIndex].takeWhile(Char::isWhitespace)
            lines[lineIndex] = indentation + render(write.occurrenceId, desired.getValue(write))
        }

        changedWrites
            .filterNot { it.occurrenceId in occurrenceLines }
            .sortedBy(SheetPlacementWrite::occurrenceId)
            .forEach { write ->
                val insertion = insertionIndex(write.occurrenceId, occurrenceLines, source)
                lines.add(insertion, "  " + render(write.occurrenceId, desired.getValue(write)))
                occurrenceLines.entries
                    .filter { it.value >= insertion }
                    .forEach { it.setValue(it.value + 1) }
                occurrenceLines[write.occurrenceId] = insertion
            }

        val updated = lines.joinToString("\n").trimEnd() + "\n"
        check(parser.parse("sheet-companion", updated) is SheetCompanionParseSuccess) {
            "Generated Sheet Companion must parse successfully."
        }
        return SheetPlacementWriteResult(
            updatedSource = updated,
            changed = true,
            changedOccurrences = changedWrites.map(SheetPlacementWrite::occurrenceId),
        )
    }

    fun writeRouteConstraints(
        existingSource: String,
        constraints: List<SheetRouteConstraintWrite>,
    ): SheetRouteConstraintWriteResult {
        require(constraints.isNotEmpty()) { "At least one Sheet route-constraint write is required." }
        val source = parse(existingSource)
        val writes = constraints.sortedWith(compareBy(SheetRouteConstraintWrite::projectionId, SheetRouteConstraintWrite::targetId))
        require(writes.map(SheetRouteConstraintWrite::key).distinct().size == writes.size) {
            "Each route target may receive one write per operation."
        }
        val currentByKey = source.routeConstraints.associateBy { route -> route.projectionId to route.targetId }
        val changedWrites = writes.filter { write ->
            val current = currentByKey[write.key]
            current == null || current.point.x != write.point.x || current.point.y != write.point.y
        }
        if (changedWrites.isEmpty()) {
            return SheetRouteConstraintWriteResult(existingSource, changed = false, changedTargets = emptyList())
        }

        val lines = existingSource.replace("\r\n", "\n").replace('\r', '\n').trimEnd().lines().toMutableList()
        val routeLines = source.routeConstraints.associate { route ->
            (route.projectionId to route.targetId) to (route.span.start.line - 1)
        }.toMutableMap()
        changedWrites.filter { it.key in routeLines }.forEach { write ->
            val lineIndex = routeLines.getValue(write.key)
            val indentation = lines[lineIndex].takeWhile(Char::isWhitespace)
            lines[lineIndex] = indentation + renderRoute(write)
        }
        changedWrites.filterNot { it.key in routeLines }.forEach { write ->
            val insertion = routeInsertionIndex(write.key, routeLines, source)
            lines.add(insertion, "  " + renderRoute(write))
            routeLines.entries.filter { it.value >= insertion }.forEach { it.setValue(it.value + 1) }
            routeLines[write.key] = insertion
        }

        val updated = lines.joinToString("\n").trimEnd() + "\n"
        check(parser.parse("sheet-companion", updated) is SheetCompanionParseSuccess) {
            "Generated Sheet Companion must parse successfully."
        }
        return SheetRouteConstraintWriteResult(
            updatedSource = updated,
            changed = true,
            changedTargets = changedWrites.map(SheetRouteConstraintWrite::displayKey),
        )
    }

    private fun parse(source: String): SheetCompanionSource = when (val result = parser.parse("sheet-companion", source)) {
        is SheetCompanionParseSuccess -> result.source
        is SheetCompanionParseFailure -> throw IllegalArgumentException(result.diagnostics.first().message)
    }

    private fun validate(write: SheetPlacementWrite) {
        require(write.point.x > 0 && write.point.y > 0) {
            "Occurrence `${write.occurrenceId}` must use a positive Sheet point."
        }
    }

    private fun insertionIndex(
        occurrenceId: String,
        occurrenceLines: Map<String, Int>,
        source: SheetCompanionSource,
    ): Int {
        val later = occurrenceLines
            .filterKeys { it > occurrenceId }
            .minByOrNull { it.key }
            ?.value
        if (later != null) return later
        return occurrenceLines.values.maxOrNull()?.plus(1)
            ?: (source.title?.span?.start?.line ?: source.snap.span.start.line)
    }

    private fun render(occurrenceId: String, placement: SheetPlacementState): String = buildString {
        append('"').append(occurrenceId).append("\" at (").append(placement.x).append(", ").append(placement.y).append(')')
        if (placement.locked) append(" lock")
    }

    private fun routeInsertionIndex(
        key: Pair<String, String>,
        routeLines: Map<Pair<String, String>, Int>,
        source: SheetCompanionSource,
    ): Int {
        val later = routeLines.entries
            .filter { entry -> compareValuesBy(entry.key, key, Pair<String, String>::first, Pair<String, String>::second) > 0 }
            .minWithOrNull(compareBy({ entry -> entry.key.first }, { entry -> entry.key.second }))
            ?.value
        if (later != null) return later
        return routeLines.values.maxOrNull()?.plus(1)
            ?: source.placements.maxOfOrNull { it.span.start.line }
            ?: (source.title?.span?.start?.line ?: source.snap.span.start.line)
    }

    private fun renderRoute(write: SheetRouteConstraintWrite): String =
        "route \"${write.projectionId}\" via \"${write.targetId}\" at (${write.point.x}, ${write.point.y})"

    private fun SheetPlacementIntent.toState() = SheetPlacementState(
        x = point.x,
        y = point.y,
        locked = locked,
    )

    private data class SheetPlacementState(
        val x: Int,
        val y: Int,
        val locked: Boolean,
    )
}
