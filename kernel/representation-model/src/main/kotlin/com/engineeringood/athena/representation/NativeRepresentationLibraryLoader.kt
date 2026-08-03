package com.engineeringood.athena.representation

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.io.InputStreamReader
import java.util.Properties

data class NativeRepresentationLibrary(
    val libraryId: RepresentationLibraryId,
    val definitions: List<RepresentationDefinition>,
)

data class NativeRepresentationLibraryLoadResult(
    val libraryOrNull: NativeRepresentationLibrary?,
    val diagnostics: List<RepresentationDiagnostic>,
) {
    val library: NativeRepresentationLibrary
        get() = requireNotNull(libraryOrNull) { "Native representation library was not loaded." }
}

class NativeRepresentationLibraryLoader {
    fun loadBundled(
        resourcePath: String = "representation-libraries/athena-native-iec.properties",
    ): NativeRepresentationLibraryLoadResult {
        val stream = javaClass.classLoader.getResourceAsStream(resourcePath)
            ?: return failed("Bundled native representation library `$resourcePath` was not found.")
        val properties = Properties()
        runCatching {
            InputStreamReader(stream, StandardCharsets.UTF_8).use(properties::load)
        }.getOrElse { exception ->
            return failed("Could not read bundled native representation library: ${exception.message}")
        }
        return load(properties)
    }

    fun load(path: Path): NativeRepresentationLibraryLoadResult {
        if (path.fileName.toString().endsWith(".elmt", ignoreCase = true)) {
            return failed("QET .elmt files are reference/import inputs only, not Athena runtime assets.")
        }
        if (!path.fileName.toString().endsWith(".properties", ignoreCase = true)) {
            return failed("Native representation library assets must use .properties files.")
        }

        val properties = Properties()
        runCatching {
            Files.newBufferedReader(path, StandardCharsets.UTF_8).use { reader -> properties.load(reader) }
        }.getOrElse { exception ->
            return failed("Could not read native representation library asset: ${exception.message}")
        }

        return load(properties)
    }

    private fun load(properties: Properties): NativeRepresentationLibraryLoadResult {
        return runCatching {
            val libraryId = RepresentationLibraryId(properties.required("library.id"))
            val definitions = generateSequence(0) { index -> index + 1 }
                .takeWhile { index -> properties.has("symbol.$index.id") }
                .map { index -> properties.definition(index, libraryId) }
                .toList()
            if (definitions.isEmpty()) {
                return failed("Native representation library must declare at least one symbol.")
            }
            val validation = RepresentationContractValidator.validate(
                RepresentationValidationInput(
                    allowedLibraries = setOf(libraryId),
                    policies = emptyList(),
                    definitions = definitions,
                    occurrences = emptyList(),
                ),
            )
            NativeRepresentationLibraryLoadResult(
                libraryOrNull = NativeRepresentationLibrary(libraryId, definitions),
                diagnostics = validation.diagnostics,
            )
        }.getOrElse { exception ->
            failed(exception.message ?: "Native representation library asset is invalid.")
        }
    }

    private fun failed(message: String): NativeRepresentationLibraryLoadResult {
        return NativeRepresentationLibraryLoadResult(
            libraryOrNull = null,
            diagnostics = listOf(
                RepresentationDiagnostic(
                    code = RepresentationDiagnosticCode.LIBRARY_INVALID,
                    message = message,
                ),
            ),
        )
    }
}

private fun Properties.definition(
    index: Int,
    libraryId: RepresentationLibraryId,
): RepresentationDefinition {
    val prefix = "symbol.$index"
    val symbolId = RepresentationSymbolId(required("$prefix.id"))
    val bounds = GraphicBounds(
        x = 0.0,
        y = 0.0,
        width = required("$prefix.bounds.width").toDouble(),
        height = required("$prefix.bounds.height").toDouble(),
    )
    val styleTokenId = GraphicStyleTokenId("native.stroke")
    val primitiveIndexes = indexed("$prefix.primitive")
    val primitives = primitiveIndexes.map { primitiveIndex ->
        primitive("$prefix.primitive.$primitiveIndex", styleTokenId)
    }
    val anchorPrimitiveId = primitives.firstOrNull()?.primitiveId
        ?: error("Native representation `$symbolId` must declare at least one primitive before terminals.")
    return RepresentationDefinition(
        symbolId = symbolId,
        libraryId = libraryId,
        version = RepresentationVersion(required("$prefix.version")),
        lifecycle = RepresentationLifecycle(
            state = enumValueOf(required("$prefix.lifecycle")),
            provenance = RepresentationProvenance("native-library:$symbolId"),
        ),
        kind = enumValueOf(required("$prefix.kind")),
        labelSlots = indexed("$prefix.label-slot").map { slotIndex ->
            RepresentationLabelSlot(
                slotId = RepresentationLabelSlotId(required("$prefix.label-slot.$slotIndex.id")),
                role = enumValueOf(required("$prefix.label-slot.$slotIndex.role")),
            )
        },
        variants = indexed("$prefix.variant").map { variantIndex ->
            RepresentationVariantId(required("$prefix.variant.$variantIndex.id"))
        },
        styleTokens = indexed("$prefix.style-token", keyField = "name").map { tokenIndex ->
            RepresentationStyleToken(
                name = required("$prefix.style-token.$tokenIndex.name"),
                value = required("$prefix.style-token.$tokenIndex.value"),
            )
        },
        graphicBody = GraphicPrimitiveDocument(
            documentId = GraphicPrimitiveDocumentId(symbolId.value),
            bounds = bounds,
            primitives = primitives,
            styleTokens = listOf(
                GraphicStyleToken(
                    styleTokenId = styleTokenId,
                    stroke = GraphicPaintToken("foreground"),
                    strokeWidth = 1.0,
                    fill = GraphicFill.TRANSPARENT,
                    lineCap = GraphicLineCap.BUTT,
                    lineJoin = GraphicLineJoin.MITER,
                ),
            ),
            provenanceSources = listOf("native-library:$symbolId"),
        ),
        anchors = indexed("$prefix.terminal").map { terminalIndex ->
            terminalAnchor("$prefix.terminal.$terminalIndex", anchorPrimitiveId)
        },
    )
}

private fun Properties.primitive(prefix: String, styleTokenId: GraphicStyleTokenId): GraphicPrimitive {
    return when (val type = required("$prefix.type")) {
        "rectangle" -> GraphicPrimitive.Rectangle(
            primitiveId = GraphicPrimitiveId(required("$prefix.id")),
            bounds = GraphicBounds(
                required("$prefix.x").toDouble(),
                required("$prefix.y").toDouble(),
                required("$prefix.width").toDouble(),
                required("$prefix.height").toDouble(),
            ),
            cornerRadius = 0.0,
            styleTokenId = styleTokenId,
        )
        "line" -> {
            val start = GraphicPoint(required("$prefix.x1").toDouble(), required("$prefix.y1").toDouble())
            val end = GraphicPoint(required("$prefix.x2").toDouble(), required("$prefix.y2").toDouble())
            GraphicPrimitive.Line(
                primitiveId = GraphicPrimitiveId(required("$prefix.id")),
                bounds = GraphicBounds(
                    minOf(start.x, end.x),
                    minOf(start.y, end.y),
                    (maxOf(start.x, end.x) - minOf(start.x, end.x)).coerceAtLeast(0.001),
                    (maxOf(start.y, end.y) - minOf(start.y, end.y)).coerceAtLeast(0.001),
                ),
                start = start,
                end = end,
                styleTokenId = styleTokenId,
            )
        }
        "circle" -> {
            val center = GraphicPoint(required("$prefix.cx").toDouble(), required("$prefix.cy").toDouble())
            val radius = required("$prefix.r").toDouble()
            GraphicPrimitive.Circle(
                primitiveId = GraphicPrimitiveId(required("$prefix.id")),
                bounds = GraphicBounds(center.x - radius, center.y - radius, radius * 2.0, radius * 2.0),
                center = center,
                radius = radius,
                styleTokenId = styleTokenId,
            )
        }
        else -> error("Unsupported representation primitive type `$type`.")
    }
}

private fun Properties.terminalAnchor(
    prefix: String,
    primitiveId: GraphicPrimitiveId,
): RepresentationAnchorContract {
    return RepresentationAnchorContract(
        anchorId = RepresentationAnchorId(required("$prefix.id")),
        geometryRef = required("$prefix.id"),
        primitiveId = primitiveId,
        point = GraphicPoint(
            x = required("$prefix.x").toDouble(),
            y = required("$prefix.y").toDouble(),
        ),
        role = RepresentationAnchorRole.TERMINAL,
        required = true,
    )
}

private fun Properties.indexed(prefix: String, keyField: String = "id"): List<Int> {
    return generateSequence(0) { index -> index + 1 }
        .takeWhile { index -> has("$prefix.$index.$keyField") }
        .toList()
}

private fun Properties.has(key: String): Boolean = getProperty(key) != null

private fun Properties.required(key: String): String {
    return requireNotNull(getProperty(key)?.trim()?.takeIf(String::isNotBlank)) {
        "Missing native representation library field `$key`."
    }
}
