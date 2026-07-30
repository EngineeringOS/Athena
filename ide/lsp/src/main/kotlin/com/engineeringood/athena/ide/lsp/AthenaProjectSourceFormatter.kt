package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.language.ConnectionDeclaration
import com.engineeringood.athena.language.ConnectionGroupDeclaration
import com.engineeringood.athena.language.Declaration
import com.engineeringood.athena.language.DeviceDeclaration
import com.engineeringood.athena.language.EngineeringFunctionDeclaration
import com.engineeringood.athena.language.InstallationDeclaration
import com.engineeringood.athena.language.InstallationLengthLiteral
import com.engineeringood.athena.language.InstallationMountOrientation
import com.engineeringood.athena.language.InstallationOrientation
import com.engineeringood.athena.language.LayoutAxis
import com.engineeringood.athena.language.LayoutDeclaration
import com.engineeringood.athena.language.LayoutOrientation
import com.engineeringood.athena.language.LayoutStatement
import com.engineeringood.athena.language.PortDeclaration
import com.engineeringood.athena.language.PropertyAssignment
import com.engineeringood.athena.language.QualifiedName
import com.engineeringood.athena.language.ScalarValue
import com.engineeringood.athena.language.SourceFileAst
import java.math.BigDecimal

/**
 * Canonical syntax printer for authored project source.
 *
 * It prints only source AST facts. It does not derive physical placement, rendering, or package facts.
 */
internal object AthenaProjectSourceFormatter {
    fun format(ast: SourceFileAst): String? {
        val system = ast.systemOrNull ?: return null
        return buildString {
            ast.packageDeclaration?.let { declaration ->
                appendLine("package ${declaration.name.render()}")
                appendLine()
            }
            ast.imports.forEach { declaration -> appendLine("import ${declaration.target.render()}") }
            if (ast.imports.isNotEmpty()) {
                appendLine()
            }
            appendLine("system ${system.name} {")
            ast.declarations.forEachIndexed { index, declaration ->
                appendDeclaration(declaration)
                if (index != ast.declarations.lastIndex) {
                    appendLine()
                }
            }
            appendLine("}")
        }
    }

    private fun StringBuilder.appendDeclaration(declaration: Declaration) {
        when (declaration) {
            is DeviceDeclaration -> appendDevice(declaration)
            is PortDeclaration -> appendPort(declaration)
            is ConnectionDeclaration -> appendConnection(declaration)
            is ConnectionGroupDeclaration -> appendConnectionGroup(declaration)
            is LayoutDeclaration -> appendLayout(declaration)
            is InstallationDeclaration -> appendInstallation(declaration)
        }
    }

    private fun StringBuilder.appendDevice(declaration: DeviceDeclaration) {
        appendLine("  device ${declaration.name} {")
        declaration.fields.forEach { field -> appendLine("    ${field.render()}") }
        declaration.nestedPorts.forEach { port -> appendNestedPort(port) }
        declaration.nestedFunctions.forEach { function -> appendFunction(function) }
        appendLine("  }")
    }

    private fun StringBuilder.appendNestedPort(declaration: PortDeclaration) {
        appendLine("    port ${declaration.qualifiedName.parts.last()} {")
        declaration.fields.forEach { field -> appendLine("      ${field.render()}") }
        appendLine("    }")
    }

    private fun StringBuilder.appendFunction(declaration: EngineeringFunctionDeclaration) {
        appendLine("    function ${declaration.name} {")
        appendLine("      role ${declaration.role.value}")
        appendLine("      ports (${declaration.portReferences.joinToString(", ") { reference -> reference.render() }})")
        appendLine("    }")
    }

    private fun StringBuilder.appendPort(declaration: PortDeclaration) {
        appendLine("  port ${declaration.qualifiedName.render()} {")
        declaration.fields.forEach { field -> appendLine("    ${field.render()}") }
        appendLine("  }")
    }

    private fun StringBuilder.appendConnection(declaration: ConnectionDeclaration) {
        appendLine("  connect ${declaration.alias} ${declaration.from.render()} -> ${declaration.to.render()}")
    }

    private fun StringBuilder.appendConnectionGroup(declaration: ConnectionGroupDeclaration) {
        appendLine("  connect ${declaration.name} {")
        declaration.connections.forEach { connection ->
            appendLine("    ${connection.alias} ${connection.from.render()} -> ${connection.to.render()}")
        }
        appendLine("  }")
    }

    private fun StringBuilder.appendLayout(declaration: LayoutDeclaration) {
        appendLine("  layout ${declaration.viewFamily} {")
        declaration.statements.forEach { statement ->
            appendLine("    ${statement.render()}")
        }
        appendLine("  }")
    }

    private fun StringBuilder.appendInstallation(declaration: InstallationDeclaration) {
        appendLine("  installation cabinet ${declaration.name} {")
        declaration.enclosures.forEach { enclosure ->
            appendLine("    enclosure ${enclosure.id} size (${enclosure.size.width.render()}, ${enclosure.size.height.render()}, ${enclosure.size.depth.render()})")
        }
        declaration.surfaces.forEach { surface ->
            appendLine("    surface ${surface.id} in ${surface.enclosureId} at ${surface.at.render()} size ${surface.size.render()} accepts ${surface.acceptedMountingTypes.renderList()}")
        }
        declaration.rails.forEach { rail ->
            appendLine("    rail ${rail.id} on ${rail.surfaceId} at ${rail.at.render()} length ${rail.length.render()} orientation ${rail.orientation.render()} mounting ${rail.mountingType}")
        }
        declaration.ducts.forEach { duct ->
            appendLine("    duct ${duct.id} in ${duct.enclosureId} at ${duct.at.render()} size ${duct.size.render()} orientation ${duct.orientation.render()} wall ${duct.wall.render()}")
        }
        declaration.channels.forEach { channel ->
            appendLine("    channel ${channel.id} in ${channel.ductId} at ${channel.at.render()} size ${channel.size.render()} lanes ${channel.lanes} margin ${channel.margin.render()}")
        }
        declaration.terminalGroups.forEach { group ->
            appendLine("    terminal-group ${group.id} in ${group.enclosureId} at ${group.at.render()} size ${group.size.render()} orientation ${group.orientation.render()} accepts ${group.acceptedMountingTypes.renderList()}")
        }
        declaration.mounts.forEach { mount ->
            appendLine("    mount ${mount.deviceId} as ${mount.id} on ${mount.targetId} at ${mount.at.render()} {")
            appendLine("      footprint ${mount.footprint.render()}")
            appendLine("      mounting ${mount.mountingType}")
            appendLine("      orientation ${mount.orientation.render()}")
            appendLine("      allowed-orientations ${mount.allowedOrientations.joinToString(prefix = "[", postfix = "]") { orientation -> orientation.render() }}")
            appendLine("      clearance ${mount.clearance.render()}")
            appendLine("      compatible-containers ${mount.compatibleContainerKinds.renderList()}")
            appendLine("    }")
        }
        declaration.routes.forEach { route ->
            appendLine("    route ${route.connectionAlias} through ${route.channelIds.renderList()}")
        }
        appendLine("  }")
    }
}

private fun PropertyAssignment.render(): String = "$name ${value.render()}"

private fun ScalarValue.render(): String =
    when (this) {
        is ScalarValue.Identifier -> text
        is ScalarValue.StringLiteral -> "\"$text\""
    }

private fun LayoutStatement.render(): String =
    when (this) {
        is LayoutStatement.PlaceNear -> "place $subject near $target"
        is LayoutStatement.PlaceBelow -> "place $subject below $target"
        is LayoutStatement.AlignWith -> "align $subject aligned-with $target axis ${axis.render()}"
        is LayoutStatement.GroupWith -> "group $subject grouped-with $target"
        is LayoutStatement.PlaceAt -> "place ${subject.render()} at (${position.column}, ${position.row}) orientation ${orientation.render()}"
    }

private fun LayoutOrientation.render(): String =
    when (this) {
        LayoutOrientation.Horizontal -> "horizontal"
        LayoutOrientation.Vertical -> "vertical"
    }

private fun LayoutAxis.render(): String =
    when (this) {
        LayoutAxis.Horizontal -> "horizontal"
        LayoutAxis.Vertical -> "vertical"
    }

private fun InstallationOrientation.render(): String =
    when (this) {
        InstallationOrientation.Horizontal -> "horizontal"
        InstallationOrientation.Vertical -> "vertical"
    }

private fun InstallationMountOrientation.render(): String = name.lowercase()

private fun InstallationLengthLiteral.render(): String = "${value.renderNumber()}$unit"

private fun com.engineeringood.athena.language.InstallationPointLiteral.render(): String =
    "(${x.render()}, ${y.render()})"

private fun com.engineeringood.athena.language.InstallationSizeLiteral.render(): String =
    "(${width.render()}, ${height.render()})"

private fun com.engineeringood.athena.language.InstallationSize3Literal.render(): String =
    "(${width.render()}, ${height.render()}, ${depth.render()})"

private fun com.engineeringood.athena.language.InstallationClearanceLiteral.render(): String =
    "(${top.render()}, ${right.render()}, ${bottom.render()}, ${left.render()})"

private fun QualifiedName.render(): String = parts.joinToString(".")

private fun List<String>.renderList(): String = joinToString(prefix = "[", postfix = "]", separator = ", ")

private fun Double.renderNumber(): String = BigDecimal.valueOf(this).stripTrailingZeros().toPlainString()
