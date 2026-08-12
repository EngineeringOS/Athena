package com.engineeringood.athena.language

import java.nio.file.Files
import java.nio.file.Path

/** Typed page intent authored in a colocated `*.sheet.athena` companion. */
data class SheetPageIntent(
    val format: String,
    val orientation: SheetOrientation,
    val span: SourceSpan,
)

enum class SheetOrientation { LANDSCAPE, PORTRAIT }

/** Presentation-only frame used for page navigation and ruler labels. */
data class SheetPlotFrameIntent(
    val columns: Int,
    val rows: Int,
    val span: SourceSpan,
) {
    init { require(columns > 0 && rows > 0) { "Sheet frame columns and rows must be positive." } }
}

/** Canonical page-space snap step. It never reinterprets persisted Sheet Points. */
data class SheetSnapIntent(
    val step: Int,
    val span: SourceSpan,
) {
    init { require(step > 0) { "Sheet snap step must be positive." } }
}

data class SheetTitleIntent(
    val text: String,
    val span: SourceSpan,
)

/** Canonical persisted occurrence placement in Sheet units. */
data class SheetPoint(
    val x: Int,
    val y: Int,
    val span: SourceSpan = SourceSpan(SourcePosition(0, 1, 1), SourcePosition(0, 1, 1)),
) {
    init { require(x > 0 && y > 0) { "Sheet point coordinates must be positive." } }
}

data class SheetPlacementIntent(
    val occurrence: String,
    val point: SheetPoint,
    val locked: Boolean,
    val span: SourceSpan,
)

/** Durable route waypoint tied to one stable Connection Projection. */
data class SheetRouteConstraintIntent(
    val projectionId: String,
    val targetId: String,
    val point: SheetPoint,
    val span: SourceSpan,
)

data class SheetCompanionSource(
    val name: String,
    val page: SheetPageIntent,
    val frame: SheetPlotFrameIntent,
    val snap: SheetSnapIntent,
    val title: SheetTitleIntent?,
    val placements: List<SheetPlacementIntent>,
    val routeConstraints: List<SheetRouteConstraintIntent> = emptyList(),
    val span: SourceSpan,
)

/** Ordered presentation index for one engineering project. It owns no package or engineering fact. */
data class FolioCompanionSource(
    val name: String,
    val pages: List<FolioPageIntent>,
    val span: SourceSpan,
)

data class FolioPageIntent(
    val name: String,
    val span: SourceSpan,
)

sealed interface FolioCompanionParseResult

data class FolioCompanionParseSuccess(val source: FolioCompanionSource) : FolioCompanionParseResult

data class FolioCompanionParseFailure(val diagnostics: List<SyntaxDiagnostic>) : FolioCompanionParseResult

sealed interface FolioCompanionLocation {
    val expectedPath: Path
}

data class FolioCompanionFound(override val expectedPath: Path, val path: Path) : FolioCompanionLocation

data class FolioCompanionMissing(override val expectedPath: Path) : FolioCompanionLocation

data class FolioCompanionAmbiguous(override val expectedPath: Path, val candidates: List<Path>) : FolioCompanionLocation

sealed interface PageCompanionLocation {
    val expectedPath: Path
}

data class PageCompanionFound(override val expectedPath: Path, val path: Path) : PageCompanionLocation

data class PageCompanionMissing(override val expectedPath: Path) : PageCompanionLocation

data class PageCompanionAmbiguous(override val expectedPath: Path, val candidates: List<Path>) : PageCompanionLocation

object FolioCompanionLocator {
    fun locate(projectSource: Path): FolioCompanionLocation {
        val sourceName = projectSource.fileName?.toString().orEmpty()
        require(sourceName.endsWith(".athena") && !sourceName.endsWith(".folio.athena") && !sourceName.endsWith(".sheet.athena")) {
            "Folio Companion discovery requires one project .athena source."
        }
        val expected = projectSource.resolveSibling(sourceName.removeSuffix(".athena") + ".folio.athena")
        return PageCompanionLocator.Support.locateExact(expected, { path ->
            when (path) {
                null -> FolioCompanionMissing(expected)
                else -> FolioCompanionFound(expected, path)
            }
        }, { candidates -> FolioCompanionAmbiguous(expected, candidates) })
    }
}

object PageCompanionLocator {
    fun locate(projectSource: Path, pageName: String): PageCompanionLocation {
        require(Support.PAGE_NAME.matches(pageName)) { "Page Companion name must be a stable page identifier." }
        val sourceName = projectSource.fileName?.toString().orEmpty()
        require(sourceName.endsWith(".athena") && !sourceName.endsWith(".folio.athena") && !sourceName.endsWith(".sheet.athena")) {
            "Page Companion discovery requires one project .athena source."
        }
        val expected = projectSource.resolveSibling(sourceName.removeSuffix(".athena") + ".${pageName}.sheet.athena")
        return Support.locateExact(expected, { path ->
            when (path) {
                null -> PageCompanionMissing(expected)
                else -> PageCompanionFound(expected, path)
            }
        }, { candidates -> PageCompanionAmbiguous(expected, candidates) })
    }

    internal object Support {
        val PAGE_NAME = Regex("[A-Za-z][A-Za-z0-9_]*")

        fun <T> locateExact(expected: Path, found: (Path?) -> T, ambiguous: (List<Path>) -> T): T {
            val parent = expected.parent ?: return found(null)
            val candidates = Files.list(parent).use { paths ->
                paths.filter { candidate -> candidate.fileName.toString().equals(expected.fileName.toString(), ignoreCase = true) }
                    .sorted()
                    .toList()
            }
            return when {
                candidates.isEmpty() -> found(null)
                candidates.size == 1 && candidates.single().fileName.toString() == expected.fileName.toString() -> found(candidates.single())
                else -> ambiguous(candidates)
            }
        }
    }
}

/** Small, human-authored index over independently stored page companions. */
class AthenaFolioCompanionParser {
    fun parse(file: String, source: String): FolioCompanionParseResult {
        val lines = source.lines()
        val meaningful = lines.withIndex().filter { (_, line) -> line.trim().isNotEmpty() }
        if (meaningful.isEmpty()) return FolioCompanionParseFailure(listOf(diagnostic(file, lines, 1, "Folio Companion is empty.")))
        val header = FOLIO_HEADER.matchEntire(meaningful.first().value.trim())
            ?: return FolioCompanionParseFailure(listOf(diagnostic(file, lines, meaningful.first().index + 1, "Expected `folio <name> {`.")))
        if (meaningful.last().value.trim() != "}") {
            return FolioCompanionParseFailure(listOf(diagnostic(file, lines, meaningful.last().index + 1, "Expected closing `}` for Folio Companion.")))
        }
        val diagnostics = mutableListOf<SyntaxDiagnostic>()
        val pages = mutableListOf<FolioPageIntent>()
        val names = mutableSetOf<String>()
        meaningful.drop(1).dropLast(1).forEach { indexed ->
            val page = PAGE.matchEntire(indexed.value.trim())
            if (page == null) {
                diagnostics += diagnostic(file, lines, indexed.index + 1, "Unknown Folio Companion statement: `${indexed.value.trim()}`.")
            } else {
                val name = page.groupValues[1]
                if (!names.add(name)) diagnostics += diagnostic(file, lines, indexed.index + 1, "Folio page `$name` is declared more than once.")
                else pages += FolioPageIntent(name, lineSpan(lines, indexed.index + 1))
            }
        }
        if (pages.isEmpty()) diagnostics += diagnostic(file, lines, meaningful.first().index + 1, "Folio Companion requires at least one page declaration.")
        if (diagnostics.isNotEmpty()) return FolioCompanionParseFailure(diagnostics.sortedBy { it.span.start.offset })
        val name = header.groupValues[1].ifBlank { header.groupValues[2] }
        return FolioCompanionParseSuccess(FolioCompanionSource(name, pages, SourceSpan(
            SourcePosition(0, meaningful.first().index + 1, 1),
            SourcePosition(source.length, lines.size.coerceAtLeast(1), lines.lastOrNull()?.length?.plus(1) ?: 1),
        )))
    }

    private fun diagnostic(file: String, lines: List<String>, line: Int, message: String) =
        SyntaxDiagnostic(file, line, 1, message, lineSpan(lines, line))

    private fun lineSpan(lines: List<String>, line: Int): SourceSpan {
        val safeLine = line.coerceAtLeast(1)
        val offset = if (lines.isEmpty()) 0 else lines.take(safeLine - 1).sumOf { it.length + 1 }
        val length = lines.getOrNull(safeLine - 1)?.length ?: 0
        return SourceSpan(SourcePosition(offset, safeLine, 1), SourcePosition(offset + length, safeLine, length + 1))
    }

    private companion object {
        val FOLIO_HEADER = Regex("folio\\s+(?:\\\"([^\\\"]+)\\\"|([A-Za-z][A-Za-z0-9_-]*))\\s*\\{")
        val PAGE = Regex("page\\s+([A-Za-z][A-Za-z0-9_]*)")
    }
}

data class SheetStyleIntent(
    val name: String,
    val strokeRgba: String? = null,
    val fillRgba: String? = null,
    val strokeWidth: Int? = null,
    val dash: List<Int>? = null,
    val lineCap: String? = null,
    val lineJoin: String? = null,
    val opacity: Int? = null,
    val fontSize: Int? = null,
    val fontWeight: Int? = null,
    val routeMarker: String? = null,
    val portDisplay: String? = null,
    val annotations: List<SheetStyleAnnotationIntent> = emptyList(),
    val span: SourceSpan,
)

data class SheetStyleAnnotationIntent(
    val subjectName: String,
    val displayRole: String,
)

data class SheetStyleCompanionSource(
    val styles: List<SheetStyleIntent>,
    val span: SourceSpan,
)

sealed interface SheetStyleCompanionParseResult

data class SheetStyleCompanionParseSuccess(val source: SheetStyleCompanionSource) : SheetStyleCompanionParseResult

data class SheetStyleCompanionParseFailure(val diagnostics: List<SyntaxDiagnostic>) : SheetStyleCompanionParseResult

sealed interface SheetCompanionParseResult

data class SheetCompanionParseSuccess(val source: SheetCompanionSource) : SheetCompanionParseResult

data class SheetCompanionParseFailure(val diagnostics: List<SyntaxDiagnostic>) : SheetCompanionParseResult

object PageStyleCompanionLocator {
    fun locate(sheetCompanion: Path): PageCompanionLocation {
        val sheetName = sheetCompanion.fileName?.toString().orEmpty()
        require(sheetName.endsWith(".sheet.athena")) {
            "Style Companion discovery requires one .sheet.athena companion."
        }
        val expected = sheetCompanion.resolveSibling(sheetName.removeSuffix(".sheet.athena") + ".sheet.style.athena")
        return PageCompanionLocator.Support.locateExact(expected, { path ->
            when (path) {
                null -> PageCompanionMissing(expected)
                else -> PageCompanionFound(expected, path)
            }
        }, { candidates -> PageCompanionAmbiguous(expected, candidates) })
    }
}

class AthenaSheetStyleCompanionParser {
    fun parse(file: String, source: String): SheetStyleCompanionParseResult {
        val lines = source.lines()
        val diagnostics = mutableListOf<SyntaxDiagnostic>()
        val styles = mutableListOf<SheetStyleIntent>()
        var index = 0
        while (index < lines.size) {
            val line = lines[index].trim()
            if (line.isBlank()) {
                index += 1
                continue
            }
            val header = STYLE_HEADER.matchEntire(line)
            if (header == null) {
                diagnostics += diagnostic(file, lines, index + 1, "Expected `style <name> {`.")
                index += 1
                continue
            }
            val name = header.groupValues[1].ifBlank { header.groupValues[2] }
            var stroke: String? = null
            var fill: String? = null
            var width: Int? = null
            var dash: List<Int>? = null
            var lineCap: String? = null
            var lineJoin: String? = null
            var opacity: Int? = null
            var fontSize: Int? = null
            var fontWeight: Int? = null
            var routeMarker: String? = null
            var portDisplay: String? = null
            val annotations = mutableListOf<SheetStyleAnnotationIntent>()
            index += 1
            var closed = false
            while (index < lines.size) {
                val body = lines[index].trim()
                if (body.isBlank()) {
                    index += 1
                    continue
                }
                if (body == "}") {
                    closed = true
                    index += 1
                    break
                }
                val separator = body.indexOf(':')
                if (separator <= 0) {
                    diagnostics += diagnostic(file, lines, index + 1, "Style field must use `name: value`.")
                    index += 1
                    continue
                }
                val key = body.substring(0, separator).trim()
                val value = body.substring(separator + 1).trim()
                when (key) {
                    "stroke" -> stroke = parseColor(file, lines, index + 1, value, diagnostics)
                    "fill" -> fill = parseColor(file, lines, index + 1, value, diagnostics)
                    "width" -> width = parsePositiveInt(file, lines, index + 1, value, "width", diagnostics)
                    "dash" -> dash = parseDash(file, lines, index + 1, value, diagnostics)
                    "cap" -> lineCap = parseChoice(file, lines, index + 1, value, "cap", setOf("butt", "round", "square"), diagnostics)
                    "join" -> lineJoin = parseChoice(file, lines, index + 1, value, "join", setOf("miter", "round", "bevel"), diagnostics)
                    "opacity" -> opacity = parseIntRange(file, lines, index + 1, value, "opacity", 0..255, diagnostics)
                    "font-size" -> fontSize = parsePositiveInt(file, lines, index + 1, value, "font-size", diagnostics)
                    "font-weight" -> fontWeight = parseIntRange(file, lines, index + 1, value, "font-weight", 1..1000, diagnostics)
                    "route-marker" -> routeMarker = parseChoice(file, lines, index + 1, value, "route-marker", setOf("none", "end-arrow"), diagnostics)
                    "port-display" -> portDisplay = parseChoice(file, lines, index + 1, value, "port-display", setOf("hidden", "marker", "marker-and-label"), diagnostics)
                    "annotation" -> parseAnnotation(file, lines, index + 1, value, diagnostics)?.let(annotations::add)
                    else -> diagnostics += diagnostic(file, lines, index + 1, "Unknown Style Companion field `$key`.")
                }
                index += 1
            }
            if (!closed) diagnostics += diagnostic(file, lines, index.coerceAtLeast(1), "Expected closing `}` for Style Companion style block.")
            styles += SheetStyleIntent(
                name = name,
                strokeRgba = stroke,
                fillRgba = fill,
                strokeWidth = width,
                dash = dash,
                lineCap = lineCap,
                lineJoin = lineJoin,
                opacity = opacity,
                fontSize = fontSize,
                fontWeight = fontWeight,
                routeMarker = routeMarker,
                portDisplay = portDisplay,
                annotations = annotations.toList(),
                span = lineSpan(lines, index.coerceAtLeast(1)),
            )
        }
        if (styles.isEmpty()) diagnostics += diagnostic(file, lines, 1, "Style Companion requires at least one style block.")
        val duplicate = styles.groupBy { it.name }.filterValues { it.size > 1 }.keys.firstOrNull()
        if (duplicate != null) diagnostics += diagnostic(file, lines, 1, "Style `$duplicate` is declared more than once.")
        if (diagnostics.isNotEmpty()) return SheetStyleCompanionParseFailure(diagnostics.sortedBy { it.span.start.offset })
        return SheetStyleCompanionParseSuccess(SheetStyleCompanionSource(styles, lineSpan(lines, 1)))
    }

    private fun parseColor(file: String, lines: List<String>, line: Int, value: String, diagnostics: MutableList<SyntaxDiagnostic>) =
        value.lowercase().takeIf { Regex("^#[0-9a-f]{8}$").matches(it) } ?: run {
            diagnostics += diagnostic(file, lines, line, "Style color must be `#rrggbbaa`.")
            null
        }

    private fun parseAnnotation(
        file: String,
        lines: List<String>,
        line: Int,
        value: String,
        diagnostics: MutableList<SyntaxDiagnostic>,
    ): SheetStyleAnnotationIntent? {
        val parts = value.split(Regex("\\s+"), limit = 2)
        if (parts.size != 2 || parts[0].isBlank() || parts[1] !in setOf("kind", "potential-or-signal", "specification")) {
            diagnostics += diagnostic(
                file,
                lines,
                line,
                "Style annotation must be `<connection-or-net-name> <kind|potential-or-signal|specification>.",
            )
            return null
        }
        return SheetStyleAnnotationIntent(parts[0], parts[1])
    }

    private fun parsePositiveInt(file: String, lines: List<String>, line: Int, value: String, name: String, diagnostics: MutableList<SyntaxDiagnostic>) =
        value.toIntOrNull()?.takeIf { it > 0 } ?: run {
            diagnostics += diagnostic(file, lines, line, "Style `$name` must be a positive integer.")
            null
        }

    private fun parseIntRange(file: String, lines: List<String>, line: Int, value: String, name: String, range: IntRange, diagnostics: MutableList<SyntaxDiagnostic>) =
        value.toIntOrNull()?.takeIf { it in range } ?: run {
            diagnostics += diagnostic(file, lines, line, "Style `$name` must be in ${range.first}..${range.last}.")
            null
        }

    private fun parseChoice(file: String, lines: List<String>, line: Int, value: String, name: String, choices: Set<String>, diagnostics: MutableList<SyntaxDiagnostic>) =
        value.lowercase().takeIf { it in choices } ?: run {
            diagnostics += diagnostic(file, lines, line, "Style `$name` must be one of ${choices.sorted().joinToString(", ")}.")
            null
        }

    private fun parseDash(file: String, lines: List<String>, line: Int, value: String, diagnostics: MutableList<SyntaxDiagnostic>): List<Int>? {
        if (value == "[]") return emptyList()
        val match = Regex("\\[([0-9,\\s]+)]").matchEntire(value)
        if (match == null) {
            diagnostics += diagnostic(file, lines, line, "Style `dash` must be [] or a positive integer list.")
            return null
        }
        val values = match.groupValues[1].split(',').map { it.trim().toIntOrNull() }
        if (values.any { it == null || it <= 0 }) {
            diagnostics += diagnostic(file, lines, line, "Style `dash` must contain positive integers.")
            return null
        }
        return values.filterNotNull()
    }

    private fun diagnostic(file: String, lines: List<String>, line: Int, message: String) =
        SyntaxDiagnostic(file, line, 1, message, lineSpan(lines, line))

    private fun lineSpan(lines: List<String>, line: Int): SourceSpan {
        val safeLine = line.coerceAtLeast(1)
        val offset = if (lines.isEmpty()) 0 else lines.take(safeLine - 1).sumOf { it.length + 1 }
        val length = lines.getOrNull(safeLine - 1)?.length ?: 0
        return SourceSpan(SourcePosition(offset, safeLine, 1), SourcePosition(offset + length, safeLine, length + 1))
    }

    private companion object {
        val STYLE_HEADER = Regex("style\\s+(?:\\\"([^\\\"]+)\\\"|([A-Za-z][A-Za-z0-9_-]*))\\s*\\{")
    }
}

/**
 * Small, line-oriented parser for the dedicated Sheet Companion surface.
 * It intentionally does not parse project engineering syntax or expose renderer fields.
 */
class AthenaSheetCompanionParser {
    fun parse(file: String, source: String): SheetCompanionParseResult {
        val lines = source.lines()
        val diagnostics = mutableListOf<SyntaxDiagnostic>()
        val meaningful = lines.withIndex().filter { (_, line) -> line.trim().isNotEmpty() }
        if (meaningful.isEmpty()) return failure(file, 1, "Sheet Companion is empty.", diagnostics)

        val header = meaningful.first()
        val headerMatch = HEADER.matchEntire(header.value.trim())
        if (headerMatch == null) {
            return failure(file, header.index + 1, "Expected `sheet <name> {`.", diagnostics)
        }
        val name = headerMatch.groupValues[1].ifBlank { headerMatch.groupValues[2] }
        if (meaningful.last().value.trim() != "}") {
            return failure(file, meaningful.last().index + 1, "Expected closing `}` for Sheet Companion.", diagnostics)
        }

        var page: SheetPageIntent? = null
        var frame: SheetPlotFrameIntent? = null
        var snap: SheetSnapIntent? = null
        var title: SheetTitleIntent? = null
        val placements = mutableListOf<SheetPlacementIntent>()
        val placementNames = mutableSetOf<String>()
        val routeConstraints = mutableListOf<SheetRouteConstraintIntent>()
        val routeTargets = mutableSetOf<Pair<String, String>>()
        meaningful.drop(1).dropLast(1).forEach { indexed ->
            val lineNumber = indexed.index + 1
            val line = indexed.value.trim()
            when {
                PAGE.matches(line) -> {
                    if (page != null) diagnostics += diagnostic(file, lines, lineNumber, "Sheet Companion may contain one page declaration.")
                    else {
                        val match = PAGE.matchEntire(line)!!
                        page = SheetPageIntent(
                            format = match.groupValues[1],
                            orientation = if (match.groupValues[2] == "landscape") SheetOrientation.LANDSCAPE else SheetOrientation.PORTRAIT,
                            span = lineSpan(lines, lineNumber),
                        )
                    }
                }
                FRAME.matches(line) -> {
                    if (frame != null) diagnostics += diagnostic(file, lines, lineNumber, "Sheet Companion may contain one frame declaration.")
                    else {
                        val match = FRAME.matchEntire(line)!!
                        val columns = match.groupValues[1].toIntOrNull()
                        val rows = match.groupValues[2].toIntOrNull()
                        if (rows == null || columns == null || rows <= 0 || columns <= 0) {
                            diagnostics += diagnostic(file, lines, lineNumber, "Frame columns and rows must be positive.")
                        } else {
                            frame = SheetPlotFrameIntent(columns, rows, lineSpan(lines, lineNumber))
                        }
                    }
                }
                SNAP.matches(line) -> {
                    if (snap != null) diagnostics += diagnostic(file, lines, lineNumber, "Sheet Companion may contain one snap declaration.")
                    else {
                        val step = SNAP.matchEntire(line)!!.groupValues[1].toIntOrNull()
                        if (step == null || step <= 0) diagnostics += diagnostic(file, lines, lineNumber, "Sheet snap step must be positive.")
                        else snap = SheetSnapIntent(step, lineSpan(lines, lineNumber))
                    }
                }
                TITLE.matches(line) -> {
                    if (title != null) diagnostics += diagnostic(file, lines, lineNumber, "Sheet Companion may contain one title declaration.")
                    else title = SheetTitleIntent(TITLE.matchEntire(line)!!.groupValues[1], lineSpan(lines, lineNumber))
                }
                PLACE.matches(line) -> {
                    val match = PLACE.matchEntire(line)!!
                    val occurrence = match.groupValues[1].ifBlank { match.groupValues[2] }
                    if (!placementNames.add(occurrence)) {
                        diagnostics += diagnostic(file, lines, lineNumber, "Occurrence `$occurrence` has more than one authored placement.")
                    } else {
                        val x = match.groupValues[3].toIntOrNull()
                        val y = match.groupValues[4].toIntOrNull()
                        if (x == null || y == null || x <= 0 || y <= 0) {
                            diagnostics += diagnostic(file, lines, lineNumber, "Occurrence `$occurrence` references an unknown Sheet point.")
                        } else {
                            val pointSpan = lineSpan(lines, lineNumber)
                            placements += SheetPlacementIntent(
                                occurrence = occurrence,
                                point = SheetPoint(x, y, pointSpan),
                                locked = match.groupValues[5].isNotBlank(),
                                span = pointSpan,
                            )
                        }
                    }
                }
                ROUTE.matches(line) -> {
                    val match = ROUTE.matchEntire(line)!!
                    val projectionId = match.groupValues[1].ifBlank { match.groupValues[2] }
                    val targetId = match.groupValues[3].ifBlank { match.groupValues[4] }
                    if (!routeTargets.add(projectionId to targetId)) {
                        diagnostics += diagnostic(
                            file,
                            lines,
                            lineNumber,
                            "Connection projection `$projectionId` has more than one route target `$targetId`.",
                        )
                    } else {
                        val column = match.groupValues[5].toIntOrNull()
                        val row = match.groupValues[6].toIntOrNull()
                        if (column == null || row == null || column <= 0 || row <= 0) {
                            diagnostics += diagnostic(file, lines, lineNumber, "Route target `$targetId` references an unknown Sheet point.")
                        } else {
                            val routeSpan = lineSpan(lines, lineNumber)
                            routeConstraints += SheetRouteConstraintIntent(
                                projectionId = projectionId,
                                targetId = targetId,
                                point = SheetPoint(column, row, routeSpan),
                                span = routeSpan,
                            )
                        }
                    }
                }
                else -> diagnostics += diagnostic(file, lines, lineNumber, "Unknown Sheet Companion statement: `$line`.")
            }
        }
        if (page == null) diagnostics += diagnostic(file, lines, header.index + 1, "Sheet Companion requires a page declaration.")
        if (frame == null) diagnostics += diagnostic(file, lines, header.index + 1, "Sheet Companion requires a frame declaration.")
        if (snap == null) diagnostics += diagnostic(file, lines, header.index + 1, "Sheet Companion requires a snap declaration.")
        if (diagnostics.isNotEmpty()) return SheetCompanionParseFailure(diagnostics.sortedBy { it.span.start.offset })
        val endLine = lines.size.coerceAtLeast(1)
        return SheetCompanionParseSuccess(
            SheetCompanionSource(
                name = name,
                page = requireNotNull(page),
                frame = requireNotNull(frame),
                snap = requireNotNull(snap),
                title = title,
                placements = placements,
                routeConstraints = routeConstraints,
                span = SourceSpan(
                    start = SourcePosition(0, header.index + 1, header.value.indexOf(header.value.trim()) + 1),
                    end = SourcePosition(source.length, endLine, lines.lastOrNull()?.length?.plus(1) ?: 1),
                ),
            ),
        )
    }

    private fun failure(file: String, line: Int, message: String, diagnostics: MutableList<SyntaxDiagnostic>): SheetCompanionParseFailure {
        diagnostics += diagnostic(file, emptyList(), line, message)
        return SheetCompanionParseFailure(diagnostics)
    }

    private fun diagnostic(file: String, lines: List<String>, line: Int, message: String): SyntaxDiagnostic = SyntaxDiagnostic(
        file = file,
        line = line,
        column = 1,
        message = message,
        span = lineSpan(lines, line),
    )

    private fun lineSpan(lines: List<String>, line: Int): SourceSpan {
        val safeLine = line.coerceAtLeast(1)
        val offset = if (lines.isEmpty()) 0 else lines.take(safeLine - 1).sumOf { it.length + 1 }
        val length = lines.getOrNull(safeLine - 1)?.length ?: 0
        return SourceSpan(SourcePosition(offset, safeLine, 1), SourcePosition(offset + length, safeLine, length + 1))
    }

    private companion object {
        val HEADER = Regex("sheet\\s+(?:\\\"([^\\\"]+)\\\"|([A-Za-z][A-Za-z0-9_-]*))\\s*\\{")
        val PAGE = Regex("page\\s+format\\s+([A-Za-z0-9_-]+)\\s+(landscape|portrait)")
        val FRAME = Regex("frame:\\s*([0-9]+)\\s*\\*\\s*([0-9]+)")
        val SNAP = Regex("snap:\\s*([0-9]+)")
        val TITLE = Regex("title\\s+\\\"([^\\\"]*)\\\"")
        val PLACE = Regex("(?:\\\"([^\\\"]+)\\\"|([A-Za-z][A-Za-z0-9_.:-]*))\\s+at\\s+\\(\\s*([0-9]+)\\s*,\\s*([0-9]+)\\s*\\)(?:\\s+(lock))?")
        val ROUTE = Regex("route\\s+(?:\\\"([^\\\"]+)\\\"|([A-Za-z][A-Za-z0-9_./:-]*))\\s+via\\s+(?:\\\"([^\\\"]+)\\\"|([A-Za-z][A-Za-z0-9_.:-]*))\\s+at\\s+\\(\\s*([0-9]+)\\s*,\\s*([0-9]+)\\s*\\)")
    }
}
