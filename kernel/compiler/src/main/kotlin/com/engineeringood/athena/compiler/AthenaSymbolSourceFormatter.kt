package com.engineeringood.athena.compiler

import com.engineeringood.athena.language.RepresentationSourceUnit
import com.engineeringood.athena.language.SourceFileAst
import com.engineeringood.athena.language.BindingDeclaration
import com.engineeringood.athena.language.BindingSelectorKind
import com.engineeringood.athena.language.ElementDeclaration
import com.engineeringood.athena.language.ProfileDeclaration
import com.engineeringood.athena.language.RepresentationResourceDeclaration
import com.engineeringood.athena.language.RepresentationResourceKind
import com.engineeringood.athena.language.SymbolAnchorDeclaration
import com.engineeringood.athena.language.SymbolDeclaration
import com.engineeringood.athena.language.SymbolGraphicPrimitiveDeclaration
import java.math.BigDecimal

internal object AthenaRepresentationSourceFormatter {
    fun format(ast: SourceFileAst): String {
        val unit = ast.unit as RepresentationSourceUnit
        return buildString {
            ast.packageDeclaration?.let { declaration ->
                append("package ")
                appendLine(declaration.name.parts.joinToString("."))
                appendLine()
            }
            ast.imports.forEach { declaration ->
                append("import ")
                appendLine(declaration.target.parts.joinToString("."))
            }
            if (ast.imports.isNotEmpty()) appendLine()
            unit.declarations.forEachIndexed { index, declaration ->
                if (index > 0) appendLine()
                when (declaration) {
                    is SymbolDeclaration -> appendSymbol(declaration)
                    is ElementDeclaration -> appendElement(declaration)
                    is ProfileDeclaration -> appendProfile(declaration)
                    is BindingDeclaration -> appendBinding(declaration)
                }
            }
        }.trimEnd() + "\n"
    }

    private fun StringBuilder.appendProfile(profile: ProfileDeclaration) {
        appendLine("profile ${profile.name} {")
        profile.projection?.let { field -> appendLine("  projection ${field.value}") }
        profile.standard?.let { field -> appendLine("  standard ${field.value}") }
        profile.style?.let { field -> appendLine("  style ${field.value}") }
        profile.fallback?.let { field -> appendLine("  fallback ${field.value}") }
        appendLine("}")
    }

    private fun StringBuilder.appendBinding(binding: BindingDeclaration) {
        appendLine("binding ${binding.name} {")
        binding.profile?.let { field -> appendLine("  profile ${field.value}") }
        binding.priority?.let { field -> appendLine("  priority ${field.value.renderNumber()}") }
        if (binding.selectorFacts.isNotEmpty()) {
            appendLine()
            val subject = when (binding.selectorKind) {
                BindingSelectorKind.Function -> "function"
                BindingSelectorKind.Device,
                null,
                    -> "device"
            }
            appendLine("  select $subject where {")
            binding.selectorFacts.forEach { fact ->
                val value = when (val scalar = fact.value) {
                    is com.engineeringood.athena.language.ScalarValue.Identifier -> scalar.text
                    is com.engineeringood.athena.language.ScalarValue.StringLiteral -> "\"${scalar.text}\""
                }
                appendLine("    ${fact.name} $value")
            }
            appendLine("  }")
        }
        binding.useElement?.let { element ->
            val version = binding.useVersion?.value ?: "1.0.0"
            appendLine()
            appendLine("  use element \"${element.value}\" version \"$version\"")
        }
        binding.variant?.let { field -> appendLine("  variant \"${field.value}\"") }
        appendLine("}")
    }

    private fun StringBuilder.appendElement(element: ElementDeclaration) {
        appendLine("element ${element.name} {")
        element.identity?.let { field -> appendLine("  identity \"${field.value}\"") }
        element.version?.let { field -> appendLine("  version \"${field.value}\"") }
        element.resources.forEach { resource -> appendResource(resource) }
        element.graphic?.svgResource?.let { field ->
            appendLine("  graphic svg resource ${field.value}")
        }
        element.bounds?.let { bounds -> 
            appendLine(
                "  bounds (${bounds.x.renderNumber()}, ${bounds.y.renderNumber()}, " +
                    "${bounds.width.renderNumber()}, ${bounds.height.renderNumber()})",
            )
        }
        element.children.sortedWith(compareBy({ it.zOrder?.value ?: Double.MAX_VALUE }, { it.id })).forEach { child ->
            appendLine()
            appendLine("  child ${child.id} {")
            child.symbolIdentity?.let { field -> appendLine("    symbol \"${field.value}\"") }
            child.translate?.let { point -> appendLine("    translate (${point.x.renderNumber()}, ${point.y.renderNumber()})") }
            child.rotate?.let { field -> appendLine("    rotate ${field.value.renderNumber()}") }
            child.scale?.let { point -> appendLine("    scale (${point.x.renderNumber()}, ${point.y.renderNumber()})") }
            child.zOrder?.let { field -> appendLine("    zOrder ${field.value.renderNumber()}") }
            appendLine("  }")
        }
        element.exportedAnchors.sortedBy { export -> export.id }.forEachIndexed { index, export ->
            if (index == 0) appendLine()
            appendLine("  export anchor ${export.id} from ${export.childId.value}.${export.childAnchorId.value}")
        }
        element.exportedLabels.sortedBy { export -> export.id }.forEachIndexed { index, export ->
            if (index == 0 && element.exportedAnchors.isEmpty()) appendLine()
            appendLine("  export label ${export.id} from ${export.childId.value}.${export.childLabelId.value}")
        }
        appendLine("}")
    }

    private fun StringBuilder.appendSymbol(symbol: SymbolDeclaration) {
        append("symbol ")
        append(symbol.name)
        appendLine(" {")
        symbol.identity?.let { field -> appendLine("  identity \"${field.value}\"") }
        symbol.version?.let { field -> appendLine("  version \"${field.value}\"") }
        symbol.resources.forEach { resource -> appendResource(resource) }
        symbol.graphic?.let { graphic ->
            appendLine()
            val svgResource = graphic.svgResource
            if (svgResource != null) {
                appendLine("  graphic svg resource ${svgResource.value}")
            } else {
                appendLine("  graphic {")
                graphic.bounds?.let { bounds ->
                    appendLine(
                        "    bounds (${bounds.x.renderNumber()}, ${bounds.y.renderNumber()}, " +
                            "${bounds.width.renderNumber()}, ${bounds.height.renderNumber()})",
                    )
                }
                graphic.primitives.forEach { primitive -> appendPrimitive(primitive) }
                graphic.labels.forEach { label ->
                    appendLine(
                        "    label ${label.id} at (${label.origin.x.renderNumber()}, ${label.origin.y.renderNumber()}) " +
                            "size (${label.size.width.renderNumber()}, ${label.size.height.renderNumber()}) " +
                            "role ${label.role.value} style ${label.style}",
                    )
                }
                appendLine("  }")
            }
        }
        symbol.anchors.forEach { anchor ->
            appendLine()
            appendAnchor(anchor)
        }
        appendLine("}")
    }

    private fun StringBuilder.appendResource(resource: RepresentationResourceDeclaration) {
        appendLine("  resource ${resource.id} {")
        appendLine("    kind ${resource.kind.render()}")
        appendLine("    path \"${resource.path.value}\"")
        appendLine("  }")
    }

    private fun RepresentationResourceKind.render(): String = when (this) {
        RepresentationResourceKind.SVG -> "svg"
    }

    private fun StringBuilder.appendPrimitive(primitive: SymbolGraphicPrimitiveDeclaration) {
        when (primitive) {
            is SymbolGraphicPrimitiveDeclaration.Line -> appendLine(
                "    line ${primitive.id} from (${primitive.from.x.renderNumber()}, ${primitive.from.y.renderNumber()}) " +
                    "to (${primitive.to.x.renderNumber()}, ${primitive.to.y.renderNumber()}) style ${primitive.style}",
            )
            is SymbolGraphicPrimitiveDeclaration.Polyline -> appendLine(
                "    polyline ${primitive.id} points (" +
                    primitive.points.joinToString(", ") { point ->
                        "(${point.x.renderNumber()}, ${point.y.renderNumber()})"
                    } + ") style ${primitive.style}",
            )
            is SymbolGraphicPrimitiveDeclaration.Arc -> appendLine(
                "    arc ${primitive.id} center (${primitive.center.x.renderNumber()}, ${primitive.center.y.renderNumber()}) " +
                    "radius ${primitive.radius.renderNumber()} from ${primitive.startAngleDegrees.renderNumber()} " +
                    "sweep ${primitive.sweepAngleDegrees.renderNumber()} style ${primitive.style}",
            )
            is SymbolGraphicPrimitiveDeclaration.Circle -> appendLine(
                "    circle ${primitive.id} center (${primitive.center.x.renderNumber()}, ${primitive.center.y.renderNumber()}) " +
                    "radius ${primitive.radius.renderNumber()} style ${primitive.style}",
            )
            is SymbolGraphicPrimitiveDeclaration.Rectangle -> appendLine(
                "    rectangle ${primitive.id} at (${primitive.origin.x.renderNumber()}, ${primitive.origin.y.renderNumber()}) " +
                    "size (${primitive.size.width.renderNumber()}, ${primitive.size.height.renderNumber()}) style ${primitive.style}",
            )
        }
    }

    private fun StringBuilder.appendAnchor(anchor: SymbolAnchorDeclaration) {
        appendLine("  anchor ${anchor.id} {")
        anchor.ref?.let { field -> appendLine("    ref \"${field.value}\"") }
        anchor.port?.let { field -> appendLine("    port ${field.parts.joinToString(".")}") }
        anchor.directions.forEach { field -> appendLine("    direction ${field.value}") }
        anchor.signals.forEach { field -> appendLine("    signal ${field.parts.joinToString(".")}") }
        anchor.point?.let { point ->
            appendLine("    point (${point.x.renderNumber()}, ${point.y.renderNumber()})")
        }
        anchor.role?.let { field -> appendLine("    role ${field.value}") }
        appendLine("  }")
    }

    private fun Double.renderNumber(): String = BigDecimal.valueOf(this).stripTrailingZeros().toPlainString()
}
