package com.engineeringood.athena.compiler

import com.engineeringood.athena.packageruntime.RepresentationPackageSnapshotStager
import com.engineeringood.athena.representation.RepresentationAnchorRole
import com.engineeringood.athena.representation.RepresentationDefinitionKind
import com.engineeringood.athena.representation.toM34CabinetRenderPathProof
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AthenaM34CabinetElementSetTest {
    @Test
    fun `m34 sample package provides governed cabinet element set`() {
        val sampleRoot = Files.createTempDirectory("athena-m34-element-set")
        copyElementSetTree(elementSetRepositoryRoot().resolve("examples/m34/sample-project"), sampleRoot)
        val staged = RepresentationPackageSnapshotStager().stageRepository(
            repositoryRoot = sampleRoot,
            snapshotDirectory = sampleRoot.resolve(".athena/snapshots/m34-element-set"),
        )
        assertTrue(staged.diagnostics.isEmpty(), staged.diagnostics.toString())

        val compiled = AthenaRepresentationPackageSnapshotCompiler().compile(assertNotNull(staged.snapshot))
        assertTrue(compiled.diagnostics.isEmpty(), compiled.diagnostics.toString())

        val elements = compiled.definitions
            .filter { it.definitionKind == RepresentationDefinitionKind.ELEMENT }
            .associateBy { it.symbolId.value }
        assertTrue(elements.keys.containsAll(REQUIRED_CABINET_ELEMENTS), elements.keys.sorted().toString())

        REQUIRED_CABINET_ELEMENTS.forEach { elementId ->
            val element = assertNotNull(elements[elementId], "Missing governed Cabinet element $elementId")
            assertEquals("1.0.0", element.version.value)
            assertTrue(element.lifecycle.provenance.source.contains(".athena"))
            assertNotNull(element.graphicBody.bounds, "Element $elementId needs intrinsic bounds.")
            assertTrue(element.graphicBody.primitives.isNotEmpty(), "Element $elementId needs typed primitives.")
            val evidence = element.graphicBody.toM34CabinetRenderPathProof(
                documentViewBoxAuthority = "graphic-primitive-ir",
                adapterAuthority = "story-4.1-structural-evidence",
                xmlRuntimeAuthorityAbsent = compiled.evidence.xmlRuntimeAuthorityAbsent,
                rawMarkupAuthorityAbsent = compiled.evidence.rawSvgTransportAbsent,
                fallbackAuthorityAbsent = true,
                hardCodedDocumentBoundsAbsent = true,
                presentationPrimitiveActiveProducerAbsent = true,
            )
            assertTrue(evidence.accepted, evidence.toString())
        }

        CONNECTABLE_CABINET_ELEMENTS.forEach { elementId ->
            val element = assertNotNull(elements[elementId])
            assertTrue(element.anchors.isNotEmpty(), "Connectable element $elementId needs anchors.")
            assertTrue(element.anchors.all { it.role == RepresentationAnchorRole.TERMINAL })
            assertTrue(element.anchors.all { it.acceptedDirections.isNotEmpty() })
            assertTrue(element.anchors.all { it.acceptedSignals.isNotEmpty() })
        }
        assertTrue(assertNotNull(elements["cabinet.label.element"]).labelSlots.map { it.slotId.value }.contains("deviceTag"))
        assertTrue(assertNotNull(elements["cabinet.route_channel.element"]).anchors.isEmpty())
    }
}

private val REQUIRED_CABINET_ELEMENTS = setOf(
    "cabinet.enclosure.element",
    "cabinet.din_rail.element",
    "cabinet.protective_device.element",
    "cabinet.switch_control.element",
    "cabinet.relay_contactor.element",
    "cabinet.terminal_block.element",
    "cabinet.power_supply.element",
    "cabinet.actuator_load.element",
    "cabinet.label.element",
    "cabinet.route_channel.element",
)

private val CONNECTABLE_CABINET_ELEMENTS = setOf(
    "cabinet.protective_device.element",
    "cabinet.switch_control.element",
    "cabinet.relay_contactor.element",
    "cabinet.terminal_block.element",
    "cabinet.power_supply.element",
    "cabinet.actuator_load.element",
)

private fun elementSetRepositoryRoot(): Path {
    var current = Path.of("").toAbsolutePath()
    while (current.parent != null && !current.resolve("settings.gradle.kts").toFile().exists()) {
        current = current.parent
    }
    return current
}

private fun copyElementSetTree(sourceRoot: Path, targetRoot: Path) {
    Files.walk(sourceRoot).use { paths ->
        paths.forEach { source ->
            val target = targetRoot.resolve(sourceRoot.relativize(source).toString())
            if (Files.isDirectory(source)) {
                Files.createDirectories(target)
            } else {
                target.parent?.let(Files::createDirectories)
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }
}
