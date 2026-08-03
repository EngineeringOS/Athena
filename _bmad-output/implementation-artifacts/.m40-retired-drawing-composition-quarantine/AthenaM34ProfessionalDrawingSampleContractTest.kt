package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.semantic.CanonicalSemanticIdentityBuilder
import com.engineeringood.athena.compiler.semantic.GraphPackageIdentity
import com.engineeringood.athena.compiler.semantic.ProjectSemanticDeclarationIndexer
import com.engineeringood.athena.compiler.semantic.ProjectSemanticGraphSnapshot
import com.engineeringood.athena.compiler.semantic.ProjectSemanticLayoutHintBinder
import com.engineeringood.athena.compiler.semantic.ProjectSemanticNamespace
import com.engineeringood.athena.compiler.semantic.ProjectSemanticPackage
import com.engineeringood.athena.compiler.semantic.ProjectSemanticSchematicLayoutFactDeriver
import com.engineeringood.athena.compiler.semantic.ProjectSemanticSourceUnit
import com.engineeringood.athena.layout.LayoutOrientation
import com.engineeringood.athena.repository.PackageIdentifier
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AthenaM34ProfessionalDrawingSampleContractTest {
    @Test
    fun `dedicated professional control drawing sample compiles through the repository contract`() {
        val source = repositoryRoot()
            .resolve("examples/m34/professional-control-drawing/src/com/engineeringood/m34/professional/01-control-drawing.athena")

        val result = AthenaCompiler().compile(source)

        val success = assertIs<CompilerCompilationSuccess>(result)
        assertTrue(success.diagnosticMessages().isEmpty(), success.diagnosticMessages().joinToString("\n"))
        assertEquals(
            setOf(
                "PowerSourceG34",
                "MainBreakerQF34",
                "FuseDisconnectorQS34",
                "ControlTransformerT34",
                "ForwardContactorKM1",
                "ReverseContactorKM2",
                "StopButtonS0",
                "RaiseButtonS1",
                "LowerButtonS2",
                "UpperLimitSQ1",
                "LowerLimitSQ2",
                "ForwardLampHL1",
                "ReverseLampHL2",
                "FieldTerminalXT1",
                "ShutterMotorM34",
                "ProtectiveEarthPE34",
            ),
            success.document.components.map { component -> component.name }.toSet(),
        )
        assertEquals(
            setOf(
                "ForwardContactorKM1.coil",
                "ForwardContactorKM1.mainContact",
                "ForwardContactorKM1.noContact",
                "ForwardContactorKM1.ncContact",
                "ReverseContactorKM2.coil",
                "ReverseContactorKM2.mainContact",
                "ReverseContactorKM2.noContact",
                "ReverseContactorKM2.ncContact",
            ),
            success.document.functions.map { function -> function.id.value.removePrefix("function:") }.toSet(),
        )
        assertEquals(expectedPorts(), success.document.ports.associate { port ->
            val qualifiedName = (port.ownerReference.authoredPath + port.name).joinToString(".")
            qualifiedName to PortContract(
                direction = port.property("direction"),
                signal = port.property("signal"),
                terminal = port.property("terminal"),
            )
        })
        assertEquals(expectedConnections(), success.document.connections.map { connection ->
            "${connection.from.authoredPath.joinToString(".")} -> ${connection.to.authoredPath.joinToString(".")}"
        }.toSet())
        assertEquals(expectedPlacements(), semanticPlacementFacts(success, Files.readString(source)))
        assertTrue(success.document.components.none { component ->
            component.name.contains(Regex("Frame|Rail|Zone|Route|Title|Label"))
        })
    }
}

private data class PortContract(val direction: String, val signal: String, val terminal: String)

private data class PlacementContract(val column: Int, val row: Int, val orientation: LayoutOrientation)

private fun expectedPorts(): Map<String, PortContract> = buildMap {
    putAll(portGroup("PowerSourceG34", "Power", Triple("phaseL", "out", "L"), Triple("neutralN", "out", "N")))
    putAll(portGroup("PowerSourceG34", "Ground", Triple("protectivePE", "out", "PE")))
    putAll(portGroup("MainBreakerQF34", "Power", Triple("lineL", "in", "1"), Triple("loadT", "out", "2"), Triple("neutralInN", "in", "N1"), Triple("neutralOutN", "out", "N2")))
    putAll(portGroup("FuseDisconnectorQS34", "Power", Triple("lineL", "in", "1"), Triple("loadT", "out", "2"), Triple("neutralInN", "in", "3"), Triple("neutralOutN", "out", "4")))
    putAll(portGroup("ControlTransformerT34", "Power", Triple("primaryL", "in", "1"), Triple("primaryN", "in", "2")))
    putAll(portGroup("ControlTransformerT34", "Control", Triple("secondaryL", "out", "X1"), Triple("secondaryN", "in", "X2")))
    listOf("ForwardContactorKM1", "ReverseContactorKM2").forEach { owner ->
        putAll(
            portGroup(
                owner,
                "Control",
                Triple("coilA1", "in", "A1"),
                Triple("coilA2", "out", "A2"),
                Triple("auxNo13", "in", "13"),
                Triple("auxNo14", "out", "14"),
                Triple("auxNc21", "in", "21"),
                Triple("auxNc22", "out", "22"),
                Triple("statusNo53", "in", "53"),
                Triple("statusNo54", "out", "54"),
            ),
        )
        putAll(portGroup(owner, "Power", Triple("mainL1", "in", "1L1"), Triple("mainT1", "out", "2T1")))
    }
    putAll(portGroup("StopButtonS0", "Control", Triple("common", "in", "11"), Triple("switched", "out", "12")))
    listOf("RaiseButtonS1", "LowerButtonS2").forEach { owner ->
        putAll(portGroup(owner, "Control", Triple("common", "in", "13"), Triple("switched", "out", "14")))
    }
    listOf("UpperLimitSQ1", "LowerLimitSQ2").forEach { owner ->
        putAll(portGroup(owner, "Control", Triple("common", "in", "21"), Triple("switched", "out", "22")))
    }
    listOf("ForwardLampHL1", "ReverseLampHL2").forEach { owner ->
        putAll(portGroup(owner, "Control", Triple("signal", "in", "X1"), Triple("return", "out", "X2")))
    }
    putAll(
        portGroup(
            "FieldTerminalXT1",
            "Control",
            Triple("controlUpIn", "in", "1-IN"),
            Triple("controlUpOut", "out", "1-OUT"),
            Triple("controlDownIn", "in", "2-IN"),
            Triple("controlDownOut", "out", "2-OUT"),
        ),
    )
    putAll(
        portGroup(
            "FieldTerminalXT1",
            "Power",
            Triple("motorUpIn", "in", "3-IN"),
            Triple("motorUpOut", "out", "3-OUT"),
            Triple("motorDownIn", "in", "4-IN"),
            Triple("motorDownOut", "out", "4-OUT"),
        ),
    )
    putAll(portGroup("ShutterMotorM34", "Power", Triple("up", "in", "U"), Triple("down", "in", "D"), Triple("neutral", "in", "N")))
    putAll(portGroup("ShutterMotorM34", "Ground", Triple("earth", "in", "PE")))
    putAll(portGroup("ProtectiveEarthPE34", "Ground", Triple("bond", "in", "PE")))
}.also { ports -> check(ports.size == 62) }

private fun portGroup(
    owner: String,
    signal: String,
    vararg ports: Triple<String, String, String>,
): Map<String, PortContract> = ports.associate { (name, direction, terminal) ->
    "$owner.$name" to PortContract(direction, signal, terminal)
}

private fun expectedConnections(): Set<String> = setOf(
    "PowerSourceG34.phaseL -> MainBreakerQF34.lineL",
    "PowerSourceG34.neutralN -> MainBreakerQF34.neutralInN",
    "MainBreakerQF34.loadT -> FuseDisconnectorQS34.lineL",
    "MainBreakerQF34.neutralOutN -> FuseDisconnectorQS34.neutralInN",
    "FuseDisconnectorQS34.loadT -> ControlTransformerT34.primaryL",
    "FuseDisconnectorQS34.neutralOutN -> ControlTransformerT34.primaryN",
    "ControlTransformerT34.secondaryL -> StopButtonS0.common",
    "StopButtonS0.switched -> ReverseContactorKM2.auxNc21",
    "ReverseContactorKM2.auxNc22 -> RaiseButtonS1.common",
    "RaiseButtonS1.switched -> UpperLimitSQ1.common",
    "UpperLimitSQ1.switched -> FieldTerminalXT1.controlUpIn",
    "FieldTerminalXT1.controlUpOut -> ForwardContactorKM1.coilA1",
    "ForwardContactorKM1.coilA2 -> ControlTransformerT34.secondaryN",
    "StopButtonS0.switched -> ForwardContactorKM1.auxNc21",
    "ForwardContactorKM1.auxNc22 -> LowerButtonS2.common",
    "LowerButtonS2.switched -> LowerLimitSQ2.common",
    "LowerLimitSQ2.switched -> FieldTerminalXT1.controlDownIn",
    "FieldTerminalXT1.controlDownOut -> ReverseContactorKM2.coilA1",
    "ReverseContactorKM2.coilA2 -> ControlTransformerT34.secondaryN",
    "FuseDisconnectorQS34.loadT -> ForwardContactorKM1.mainL1",
    "FuseDisconnectorQS34.loadT -> ReverseContactorKM2.mainL1",
    "ForwardContactorKM1.mainT1 -> FieldTerminalXT1.motorUpIn",
    "FieldTerminalXT1.motorUpOut -> ShutterMotorM34.up",
    "ReverseContactorKM2.mainT1 -> FieldTerminalXT1.motorDownIn",
    "FieldTerminalXT1.motorDownOut -> ShutterMotorM34.down",
    "FuseDisconnectorQS34.neutralOutN -> ShutterMotorM34.neutral",
    "ControlTransformerT34.secondaryL -> ForwardContactorKM1.statusNo53",
    "ForwardContactorKM1.statusNo54 -> ForwardLampHL1.signal",
    "ForwardLampHL1.return -> ControlTransformerT34.secondaryN",
    "ControlTransformerT34.secondaryL -> ReverseContactorKM2.statusNo53",
    "ReverseContactorKM2.statusNo54 -> ReverseLampHL2.signal",
    "ReverseLampHL2.return -> ControlTransformerT34.secondaryN",
    "PowerSourceG34.protectivePE -> ShutterMotorM34.earth",
    "PowerSourceG34.protectivePE -> ProtectiveEarthPE34.bond",
).also { connections -> check(connections.size == 34) }

private fun expectedPlacements(): Map<String, PlacementContract> = mapOf(
    "PowerSourceG34" to PlacementContract(2, 1, LayoutOrientation.HORIZONTAL),
    "MainBreakerQF34" to PlacementContract(4, 1, LayoutOrientation.HORIZONTAL),
    "FuseDisconnectorQS34" to PlacementContract(6, 1, LayoutOrientation.HORIZONTAL),
    "ControlTransformerT34" to PlacementContract(8, 1, LayoutOrientation.HORIZONTAL),
    "ForwardContactorKM1.mainContact" to PlacementContract(10, 1, LayoutOrientation.HORIZONTAL),
    "ReverseContactorKM2.mainContact" to PlacementContract(12, 1, LayoutOrientation.HORIZONTAL),
    "ShutterMotorM34" to PlacementContract(15, 1, LayoutOrientation.HORIZONTAL),
    "StopButtonS0" to PlacementContract(2, 3, LayoutOrientation.VERTICAL),
    "RaiseButtonS1" to PlacementContract(4, 3, LayoutOrientation.VERTICAL),
    "LowerButtonS2" to PlacementContract(6, 3, LayoutOrientation.VERTICAL),
    "UpperLimitSQ1" to PlacementContract(8, 3, LayoutOrientation.VERTICAL),
    "LowerLimitSQ2" to PlacementContract(10, 3, LayoutOrientation.VERTICAL),
    "ForwardContactorKM1.coil" to PlacementContract(12, 3, LayoutOrientation.VERTICAL),
    "ReverseContactorKM2.coil" to PlacementContract(14, 3, LayoutOrientation.VERTICAL),
    "ForwardContactorKM1.ncContact" to PlacementContract(7, 4, LayoutOrientation.VERTICAL),
    "ReverseContactorKM2.ncContact" to PlacementContract(9, 4, LayoutOrientation.VERTICAL),
    "ForwardContactorKM1.noContact" to PlacementContract(12, 5, LayoutOrientation.VERTICAL),
    "ReverseContactorKM2.noContact" to PlacementContract(14, 5, LayoutOrientation.VERTICAL),
    "ForwardLampHL1" to PlacementContract(16, 5, LayoutOrientation.VERTICAL),
    "ReverseLampHL2" to PlacementContract(16, 6, LayoutOrientation.VERTICAL),
    "FieldTerminalXT1" to PlacementContract(15, 7, LayoutOrientation.HORIZONTAL),
    "ProtectiveEarthPE34" to PlacementContract(17, 8, LayoutOrientation.VERTICAL),
).also { placements -> check(placements.size == 22) }

private fun semanticPlacementFacts(
    compilation: CompilerCompilationSuccess,
    sourceContent: String,
): Map<String, PlacementContract> {
    val packageId = PackageIdentifier("com.engineeringood.m34.professional", "1.0.0")
    val packageKey = CanonicalSemanticIdentityBuilder.packageKey(packageId)
    val sourceUnitId = CanonicalSemanticIdentityBuilder.sourceUnitId(packageKey, "01-control-drawing.athena")
    val source = ProjectSemanticSourceUnit(
        sourceUnitId = sourceUnitId,
        packageKey = packageKey,
        sourceRootRelativePath = "01-control-drawing.athena",
        contentIdentity = CanonicalSemanticIdentityBuilder.sourceContentIdentity(sourceUnitId, sourceContent),
        authoredDeclarations = compilation.source.ast.declarations,
    )
    val snapshot = ProjectSemanticGraphSnapshot.canonical(
        graphId = CanonicalSemanticIdentityBuilder.graphId(
            packageKey,
            listOf(GraphPackageIdentity(packageKey, "src", emptyList())),
            listOf(source.contentIdentity),
        ),
        rootPackageId = packageKey,
        packages = listOf(ProjectSemanticPackage(packageId, packageKey, "src", emptyList())),
        sourceUnits = listOf(source),
        namespaces = listOf(
            ProjectSemanticNamespace(
                namespaceId = CanonicalSemanticIdentityBuilder.namespaceId(
                    packageKey,
                    listOf("com", "engineeringood", "m34", "professional"),
                ),
                packageKey = packageKey,
                qualifiedName = listOf("com", "engineeringood", "m34", "professional"),
                sourceUnitIds = listOf(sourceUnitId),
                declarationIds = emptyList(),
            ),
        ),
        declarations = emptyList(),
        bindings = emptyList(),
        diagnostics = emptyList(),
    )
    val indexed = ProjectSemanticDeclarationIndexer().index(snapshot)
    val bound = ProjectSemanticLayoutHintBinder().bind(indexed)
    check(bound.diagnostics.isEmpty()) { bound.diagnostics.joinToString("\n") }
    return ProjectSemanticSchematicLayoutFactDeriver().derive(bound).placementFacts.associate { fact ->
        val grid = requireNotNull(fact.gridPosition)
        val orientation = requireNotNull(fact.orientation)
        fact.intentId.value.removePrefix("intent:layout:schematic:") to
            PlacementContract(grid.column, grid.row, orientation)
    }
}

private fun com.engineeringood.athena.ir.EngineeringPort.property(name: String): String {
    return when (val value = properties.single { property -> property.name == name }.value) {
        is com.engineeringood.athena.ir.EngineeringPropertyValue.Symbol -> value.text
        is com.engineeringood.athena.ir.EngineeringPropertyValue.Text -> value.text
    }
}

private fun repositoryRoot(): Path {
    var current = Path.of("").toAbsolutePath().normalize()
    while (current.parent != null) {
        if (Files.exists(current.resolve("settings.gradle.kts"))) return current
        current = current.parent
    }
    error("Could not locate Athena repository root.")
}
