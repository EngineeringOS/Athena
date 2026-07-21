package com.engineeringood.athena.representation

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
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
    fun load(path: Path): NativeRepresentationLibraryLoadResult {
        if (path.fileName.toString().endsWith(".elmt", ignoreCase = true)) {
            return failed("QET .elmt files are reference/import inputs only, not Athena runtime assets.")
        }
        if (!path.fileName.toString().endsWith(".properties", ignoreCase = true)) {
            return failed("Native representation library assets must use .properties in v0.")
        }

        val properties = Properties()
        runCatching {
            Files.newBufferedReader(path, StandardCharsets.UTF_8).use { reader -> properties.load(reader) }
        }.getOrElse { exception ->
            return failed("Could not read native representation library asset: ${exception.message}")
        }

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
    val bounds = PresentationBounds(
        width = GridUnit(required("$prefix.bounds.width").toInt()),
        height = GridUnit(required("$prefix.bounds.height").toInt()),
    )
    return RepresentationDefinition(
        symbolId = symbolId,
        libraryId = libraryId,
        version = RepresentationVersion(required("$prefix.version")),
        lifecycle = RepresentationLifecycle(
            state = enumValueOf(required("$prefix.lifecycle")),
            provenance = RepresentationProvenance("native-library:$symbolId"),
        ),
        kind = enumValueOf(required("$prefix.kind")),
        anatomy = PresentationAnatomy(
            representationId = RepresentationId(symbolId.value),
            context = RepresentationContext.ELECTRICAL_SCHEMATIC,
            bounds = bounds,
            hotspot = PresentationHotspot(PresentationPoint(GridUnit(0), GridUnit(0))),
            primitives = indexed("$prefix.primitive").map { primitiveIndex ->
                primitive("$prefix.primitive.$primitiveIndex")
            },
            terminals = indexed("$prefix.terminal").map { terminalIndex ->
                terminal("$prefix.terminal.$terminalIndex")
            },
            labelAnchors = indexed("$prefix.label-anchor").map { anchorIndex ->
                labelAnchor("$prefix.label-anchor.$anchorIndex")
            },
        ),
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
    )
}

private fun Properties.primitive(prefix: String): PresentationPrimitive {
    return when (val type = required("$prefix.type")) {
        "rectangle" -> PresentationPrimitive.Rectangle(
            primitiveId = PresentationPrimitiveId(required("$prefix.id")),
            origin = PresentationPoint(GridUnit(required("$prefix.x").toInt()), GridUnit(required("$prefix.y").toInt())),
            size = PresentationSize(
                width = GridUnit(required("$prefix.width").toInt()),
                height = GridUnit(required("$prefix.height").toInt()),
            ),
        )
        "line" -> PresentationPrimitive.Line(
            primitiveId = PresentationPrimitiveId(required("$prefix.id")),
            start = PresentationPoint(
                x = GridUnit(required("$prefix.x1").toInt()),
                y = GridUnit(required("$prefix.y1").toInt()),
            ),
            end = PresentationPoint(
                x = GridUnit(required("$prefix.x2").toInt()),
                y = GridUnit(required("$prefix.y2").toInt()),
            ),
        )
        "circle" -> PresentationPrimitive.Circle(
            primitiveId = PresentationPrimitiveId(required("$prefix.id")),
            center = PresentationPoint(
                x = GridUnit(required("$prefix.cx").toInt()),
                y = GridUnit(required("$prefix.cy").toInt()),
            ),
            radius = GridUnit(required("$prefix.r").toInt()),
        )
        else -> error("Unsupported representation primitive type `$type`.")
    }
}

private fun Properties.terminal(prefix: String): PresentationTerminalPoint {
    return PresentationTerminalPoint(
        terminalId = PresentationTerminalId(required("$prefix.id")),
        role = enumValueOf(required("$prefix.role")),
        localPoint = PresentationPoint(
            x = GridUnit(required("$prefix.x").toInt()),
            y = GridUnit(required("$prefix.y").toInt()),
        ),
        side = enumValueOf(required("$prefix.side")),
        notation = TerminalNotation(
            marker = enumValueOf(required("$prefix.marker")),
            number = TerminalNumber(required("$prefix.number")),
        ),
    )
}

private fun Properties.labelAnchor(prefix: String): PresentationLabelAnchor {
    return PresentationLabelAnchor(
        anchorId = PresentationLabelAnchorId(required("$prefix.id")),
        role = enumValueOf(required("$prefix.role")),
        point = PresentationPoint(
            x = GridUnit(required("$prefix.x").toInt()),
            y = GridUnit(required("$prefix.y").toInt()),
        ),
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
