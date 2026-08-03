package com.engineeringood.athena.compiler

import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.packageplatform.ProjectionContextId
import com.engineeringood.athena.representation.RepresentationDefinitionKind
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AthenaM34ProfessionalDrawingMaterialResolutionTest {
    @Test
    fun `compiled package resolves every drawing occurrence without secondary authority`() {
        val projectRoot = repositoryRoot().resolve("examples/m34/professional-control-drawing")
        val source = projectRoot.resolve("src/com/engineeringood/m34/professional/01-control-drawing.athena")
        val compilation = assertIs<CompilerCompilationSuccess>(AthenaCompiler().compile(source))

        val result = AthenaRepresentationMaterialResolver().resolve(
            repositoryRoot = projectRoot,
            document = compilation.document,
            projectionContext = ProjectionContextId("schematic"),
        )

        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.joinToString("\n"))
        assertEquals(
            setOf(
                "iec.breaker.element",
                "iec.contactor.coil.element",
                "iec.contactor.main-contact.element",
                "iec.contactor.nc-contact.element",
                "iec.contactor.no-contact.element",
                "iec.fuse-disconnector.element",
                "iec.lamp.element",
                "iec.limit-switch.nc.element",
                "iec.motor.element",
                "iec.protective-earth.element",
                "iec.push-button.nc.element",
                "iec.push-button.no.element",
                "iec.source.element",
                "iec.terminal.element",
                "iec.transformer.element",
            ),
            result.definitions
                .filter { definition -> definition.definitionKind == RepresentationDefinitionKind.ELEMENT }
                .map { definition -> definition.symbolId.value }
                .toSet(),
        )
        assertEquals(22, result.materials.size)
        assertEquals(14, result.materials.count { material -> material.functionId == null })
        assertEquals(8, result.materials.count { material -> material.functionId != null })
        val expected = expectedMaterials()
        val actual = result.materials.associateBy { material -> material.semanticSubjectId }
        assertEquals(expected.keys, actual.keys)
        expected.forEach { (subjectId, contract) ->
            val material = requireNotNull(actual[subjectId])
            assertEquals(contract.physicalComponentId, material.physicalComponentId, subjectId)
            assertEquals(contract.functionId, material.functionId, subjectId)
            assertEquals(contract.packageId, material.definition.libraryId.value, subjectId)
            assertEquals(contract.packageId, material.resolution.representationPackageId.value, subjectId)
            assertEquals(contract.definitionId, material.definition.symbolId.value, subjectId)
            assertEquals(contract.definitionId, material.resolution.descriptorId.value, subjectId)
            assertTrue(material.definition.graphicBody.primitives.isNotEmpty(), subjectId)
            assertEquals(contract.ruleId, material.resolution.bindingRuleId?.value, subjectId)
            assertEquals("standard", material.resolution.variantId.value, subjectId)
            assertEquals(contract.terminals, material.terminalBindings, subjectId)
            assertEquals(
                contract.terminals.keys.associateWith { portId -> portId.substringAfterLast('.') },
                material.resolution.anchorMapping.mapValues { (_, anchor) -> anchor.value },
                subjectId,
            )
            assertTrue(material.definition.lifecycle.provenance.source.endsWith(".athena"), subjectId)
            assertTrue(material.definition.lifecycle.provenance.source.contains("${java.io.File.separator}.athena${java.io.File.separator}snapshots"), subjectId)
        }
        assertTrue(result.evidence.stagedSourcePaths.all { path ->
            path.contains("packages${java.io.File.separator}representation") &&
                !path.endsWith(".xml") &&
                !path.endsWith(".elmt") &&
                !path.contains("qelectrotech", ignoreCase = true)
        })
    }

    @Test
    fun `material subject inventory is derived from semantic decomposition instead of available bindings`() {
        val projectRoot = repositoryRoot().resolve("examples/m34/professional-control-drawing")
        val compilation = assertIs<CompilerCompilationSuccess>(
            AthenaCompiler().compile(projectRoot.resolve("src/com/engineeringood/m34/professional/01-control-drawing.athena")),
        )
        val diagnostics = mutableListOf<AthenaRepresentationMaterialDiagnostic>()

        val subjects = AthenaRepresentationMaterialSubjectDeriver.derive(
            document = compilation.document,
            diagnostics = diagnostics,
        )

        assertEquals(emptyList(), diagnostics)
        assertEquals(expectedMaterials().keys, subjects.map { subject -> subject.semanticSubjectId }.toSet())
    }

    @Test
    fun `binding cannot synthesize a variant absent from compiled definitions`() {
        val originalRoot = repositoryRoot().resolve("examples/m34/professional-control-drawing")
        val projectRoot = copyProject(originalRoot)
        val binding = projectRoot.resolve(
            "packages/representation/com/engineeringood/m34/control/power/power-bindings.athena",
        )
        binding.toFile().writeText(Files.readString(binding).replaceFirst("variant \"standard\"", "variant \"wide\""))
        val compilation = assertIs<CompilerCompilationSuccess>(
            AthenaCompiler().compile(projectRoot.resolve("src/com/engineeringood/m34/professional/01-control-drawing.athena")),
        )

        val result = resolveMaterial(projectRoot, compilation.document)

        assertTrue(result.materials.isEmpty())
        assertTrue(result.diagnostics.any { diagnostic ->
            diagnostic.code == "binding.resolution.variant.missing" && diagnostic.subject == "wide"
        }, result.diagnostics.joinToString("\n"))
    }

    @Test
    fun `same element identities in an unrelated package cannot contaminate selected material`() {
        val originalRoot = repositoryRoot().resolve("examples/m34/professional-control-drawing")
        val projectRoot = copyProject(originalRoot)
        val originalMaterial = projectRoot.resolve(
            "packages/representation/com/engineeringood/m34/control/power/power-material.athena",
        )
        val collisionRoot = projectRoot.resolve(
            "packages/representation/com/engineeringood/m34/collision/power",
        )
        Files.createDirectories(collisionRoot)
        collisionRoot.resolve("power-material.athena").toFile().writeText(
            Files.readString(originalMaterial).replace(
                "package com.engineeringood.m34.control.power",
                "package com.engineeringood.m34.collision.power",
            ),
        )
        val compilation = assertIs<CompilerCompilationSuccess>(
            AthenaCompiler().compile(projectRoot.resolve("src/com/engineeringood/m34/professional/01-control-drawing.athena")),
        )

        val result = resolveMaterial(projectRoot, compilation.document)

        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.joinToString("\n"))
        expectedMaterials().forEach { (subjectId, contract) ->
            val material = result.materials.single { candidate -> candidate.semanticSubjectId == subjectId }
            assertEquals(contract.packageId, material.definition.libraryId.value, subjectId)
        }
    }

    @Test
    fun `selection is stable across semantic names and package file order`() {
        val originalRoot = repositoryRoot().resolve("examples/m34/professional-control-drawing")
        val projectRoot = copyProject(originalRoot)
        val source = projectRoot.resolve("src/com/engineeringood/m34/professional/01-control-drawing.athena")
        source.toFile().writeText(Files.readString(source).replace("PowerSourceG34", "ZuluSourceG34"))
        val binding = projectRoot.resolve(
            "packages/representation/com/engineeringood/m34/control/power/power-bindings.athena",
        )
        Files.move(binding, binding.resolveSibling("zzz-power-bindings.athena"), StandardCopyOption.REPLACE_EXISTING)
        val compilation = assertIs<CompilerCompilationSuccess>(AthenaCompiler().compile(source))

        val result = resolveMaterial(projectRoot, compilation.document)

        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.joinToString("\n"))
        val renamed = result.materials.single { material -> material.semanticSubjectId == "component:ZuluSourceG34" }
        assertEquals("iec.source.element", renamed.definition.symbolId.value)
        assertEquals("IecPowerSource", renamed.resolution.bindingRuleId?.value)
        assertEquals("com.engineeringood.m34.control.power", renamed.definition.libraryId.value)
    }

    @Test
    fun `material subject derivation diagnoses unresolved function owners and ports`() {
        val projectRoot = repositoryRoot().resolve("examples/m34/professional-control-drawing")
        val source = projectRoot.resolve("src/com/engineeringood/m34/professional/01-control-drawing.athena")
        val compilation = assertIs<CompilerCompilationSuccess>(AthenaCompiler().compile(source))
        val function = compilation.document.functions.first { candidate -> candidate.portReferences.size >= 2 }
        val unresolvedOwner = function.copy(
            ownerReference = function.ownerReference.copy(resolvedIdentity = null),
        )
        val unresolvedPorts = function.copy(
            portReferences = listOf(
                function.portReferences[0].copy(resolvedIdentity = null),
                function.portReferences[1].copy(resolvedIdentity = StableSemanticIdentity("port:missing")),
            ),
        )

        val ownerResult = resolveMaterial(
            projectRoot,
            compilation.document.copy(
                functions = compilation.document.functions.replace(function, unresolvedOwner),
            ),
        )
        val portResult = resolveMaterial(
            projectRoot,
            compilation.document.copy(
                functions = compilation.document.functions.replace(function, unresolvedPorts),
            ),
        )

        assertTrue(ownerResult.diagnostics.any { diagnostic ->
            diagnostic.code == "material.function.owner.unresolved" && diagnostic.subject == function.id.value
        })
        assertEquals(
            2,
            portResult.diagnostics.count { diagnostic ->
                diagnostic.code == "material.function.port.unresolved" && diagnostic.subject.startsWith(function.id.value)
            },
            portResult.diagnostics.joinToString("\n"),
        )
    }

    @Test
    fun `material binding rejects project ports mapped to nonterminal anchors`() {
        val originalRoot = repositoryRoot().resolve("examples/m34/professional-control-drawing")
        val projectRoot = copyProject(originalRoot)
        val material = projectRoot.resolve(
            "packages/representation/com/engineeringood/m34/control/power/power-material.athena",
        )
        val sourceText = Files.readString(material)
        material.toFile().writeText(
            sourceText.replaceFirst(
                "role terminal direction out signal Power",
                "role hotspot direction out signal Power",
            ),
        )
        val source = projectRoot.resolve("src/com/engineeringood/m34/professional/01-control-drawing.athena")
        val compilation = assertIs<CompilerCompilationSuccess>(AthenaCompiler().compile(source))

        val result = resolveMaterial(projectRoot, compilation.document)

        assertTrue(result.diagnostics.any { diagnostic ->
            diagnostic.code == "material.anchor.role.incompatible" &&
                diagnostic.subject.endsWith(".phaseL")
        }, result.diagnostics.joinToString("\n"))
    }
}

private fun resolveMaterial(projectRoot: Path, document: EngineeringDocument): AthenaRepresentationMaterialResolutionResult =
    AthenaRepresentationMaterialResolver().resolve(
        repositoryRoot = projectRoot,
        document = document,
        projectionContext = ProjectionContextId("schematic"),
    )

private data class ExpectedMaterial(
    val physicalComponentId: String,
    val functionId: String?,
    val packageId: String,
    val definitionId: String,
    val ruleId: String,
    val terminals: Map<String, String>,
)

private fun expectedMaterials(): Map<String, ExpectedMaterial> = listOf(
    expectedDevice("PowerSourceG34", "power", "iec.source.element", "IecPowerSource", "phaseL" to "L", "neutralN" to "N", "protectivePE" to "PE"),
    expectedDevice("MainBreakerQF34", "power", "iec.breaker.element", "IecBreaker", "lineL" to "1", "loadT" to "2", "neutralInN" to "N1", "neutralOutN" to "N2"),
    expectedDevice("FuseDisconnectorQS34", "power", "iec.fuse-disconnector.element", "IecFuseDisconnector", "lineL" to "1", "loadT" to "2", "neutralInN" to "3", "neutralOutN" to "4"),
    expectedDevice("ControlTransformerT34", "power", "iec.transformer.element", "IecTransformer", "primaryL" to "1", "primaryN" to "2", "secondaryL" to "X1", "secondaryN" to "X2"),
    expectedFunction("ForwardContactorKM1", "coil", "iec.contactor.coil.element", "IecContactorCoil", "coilA1" to "A1", "coilA2" to "A2"),
    expectedFunction("ForwardContactorKM1", "mainContact", "iec.contactor.main-contact.element", "IecContactorMainContact", "mainL1" to "1L1", "mainT1" to "2T1"),
    expectedFunction("ForwardContactorKM1", "noContact", "iec.contactor.no-contact.element", "IecContactorNoContact", "auxNo13" to "13", "auxNo14" to "14", "statusNo53" to "53", "statusNo54" to "54"),
    expectedFunction("ForwardContactorKM1", "ncContact", "iec.contactor.nc-contact.element", "IecContactorNcContact", "auxNc21" to "21", "auxNc22" to "22"),
    expectedFunction("ReverseContactorKM2", "coil", "iec.contactor.coil.element", "IecContactorCoil", "coilA1" to "A1", "coilA2" to "A2"),
    expectedFunction("ReverseContactorKM2", "mainContact", "iec.contactor.main-contact.element", "IecContactorMainContact", "mainL1" to "1L1", "mainT1" to "2T1"),
    expectedFunction("ReverseContactorKM2", "noContact", "iec.contactor.no-contact.element", "IecContactorNoContact", "auxNo13" to "13", "auxNo14" to "14", "statusNo53" to "53", "statusNo54" to "54"),
    expectedFunction("ReverseContactorKM2", "ncContact", "iec.contactor.nc-contact.element", "IecContactorNcContact", "auxNc21" to "21", "auxNc22" to "22"),
    expectedDevice("StopButtonS0", "control", "iec.push-button.nc.element", "IecPushButtonNc", "common" to "11", "switched" to "12"),
    expectedDevice("RaiseButtonS1", "control", "iec.push-button.no.element", "IecPushButtonNo", "common" to "13", "switched" to "14"),
    expectedDevice("LowerButtonS2", "control", "iec.push-button.no.element", "IecPushButtonNo", "common" to "13", "switched" to "14"),
    expectedDevice("UpperLimitSQ1", "control", "iec.limit-switch.nc.element", "IecLimitSwitchNc", "common" to "21", "switched" to "22"),
    expectedDevice("LowerLimitSQ2", "control", "iec.limit-switch.nc.element", "IecLimitSwitchNc", "common" to "21", "switched" to "22"),
    expectedDevice("ForwardLampHL1", "control", "iec.lamp.element", "IecLamp", "signal" to "X1", "return" to "X2"),
    expectedDevice("ReverseLampHL2", "control", "iec.lamp.element", "IecLamp", "signal" to "X1", "return" to "X2"),
    expectedDevice(
        "FieldTerminalXT1",
        "field",
        "iec.terminal.element",
        "IecTerminal",
        "controlUpIn" to "1-IN",
        "controlUpOut" to "1-OUT",
        "controlDownIn" to "2-IN",
        "controlDownOut" to "2-OUT",
        "motorUpIn" to "3-IN",
        "motorUpOut" to "3-OUT",
        "motorDownIn" to "4-IN",
        "motorDownOut" to "4-OUT",
    ),
    expectedDevice("ShutterMotorM34", "field", "iec.motor.element", "IecMotor", "up" to "U", "down" to "D", "neutral" to "N", "earth" to "PE"),
    expectedDevice("ProtectiveEarthPE34", "field", "iec.protective-earth.element", "IecProtectiveEarth", "bond" to "PE"),
).toMap()

private fun expectedDevice(
    name: String,
    packageSuffix: String,
    definitionId: String,
    ruleId: String,
    vararg terminals: Pair<String, String>,
): Pair<String, ExpectedMaterial> {
    val subjectId = "component:$name"
    return subjectId to ExpectedMaterial(
        physicalComponentId = subjectId,
        functionId = null,
        packageId = "com.engineeringood.m34.control.$packageSuffix",
        definitionId = definitionId,
        ruleId = ruleId,
        terminals = terminals.associate { (port, terminal) -> "port:$name.$port" to terminal },
    )
}

private fun expectedFunction(
    owner: String,
    function: String,
    definitionId: String,
    ruleId: String,
    vararg terminals: Pair<String, String>,
): Pair<String, ExpectedMaterial> {
    val functionId = "function:$owner.$function"
    return functionId to ExpectedMaterial(
        physicalComponentId = "component:$owner",
        functionId = functionId,
        packageId = "com.engineeringood.m34.control.control",
        definitionId = definitionId,
        ruleId = ruleId,
        terminals = terminals.associate { (port, terminal) -> "port:$owner.$port" to terminal },
    )
}

private fun <T> List<T>.replace(existing: T, replacement: T): List<T> = map { item ->
    if (item == existing) replacement else item
}

private fun copyProject(sourceRoot: Path): Path {
    val targetRoot = Files.createTempDirectory("athena-m34-material-project")
    Files.walk(sourceRoot).use { paths ->
        paths.forEach { source ->
            val target = targetRoot.resolve(sourceRoot.relativize(source).toString())
            if (Files.isDirectory(source)) {
                Files.createDirectories(target)
            } else {
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }
    return targetRoot
}

private fun repositoryRoot(): Path {
    var current = Path.of("").toAbsolutePath().normalize()
    while (current.parent != null) {
        if (Files.exists(current.resolve("settings.gradle.kts"))) return current
        current = current.parent
    }
    error("Could not locate Athena repository root.")
}
