package com.engineeringood.athena.compiler

import com.engineeringood.athena.packageruntime.RepresentationPackageSnapshotRequest
import com.engineeringood.athena.packageruntime.RepresentationPackageSnapshotStager
import com.engineeringood.athena.packageplatform.RepresentationAnchorSide
import com.engineeringood.athena.representation.RepresentationDefinitionKind
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AthenaRepresentationPackageSnapshotCompilerTest {
    @Test
    fun `compiles package hierarchy local svg from staged snapshot only`() {
        val repositoryRoot = Files.createTempDirectory("athena-m34-snapshot-compiler")
        val packageRoot = repositoryRoot.resolve("packages/representation").createDirectories()
        val vendorRoot = packageRoot.resolve("athena/vendor").createDirectories()
        val source = vendorRoot.resolve("vendor-drive.athena")
        val svg = vendorRoot.resolve("vendor-drive.svg")
        source.writeText(VALID_SOURCE)
        svg.writeText(VALID_SVG)
        val staged = RepresentationPackageSnapshotStager().stage(
            RepresentationPackageSnapshotRequest(
                repositoryRoot = repositoryRoot,
                packageRoots = listOf(packageRoot),
                dependencyLockDigest = "lock:abc",
                snapshotDirectory = repositoryRoot.resolve(".athena/snapshots/compile"),
            ),
        )

        val result = AthenaRepresentationPackageSnapshotCompiler().compile(assertNotNull(staged.snapshot))

        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        assertEquals(listOf("athena.vendor.drive"), result.definitions.map { it.symbolId.value })
        val definition = result.definitions.single()
        assertEquals(listOf("standard"), definition.variants.map { it.value })
        assertEquals(listOf("standard"), result.descriptors.single().variants.map { it.value })
        assertTrue(definition.graphicBody.provenanceSources.all { it.contains(".athena${java.io.File.separator}snapshots") })
        assertEquals(staged.snapshot?.snapshotId, result.evidence.snapshotId)
        assertEquals("lock:abc", result.evidence.dependencyLockDigest)
        assertEquals(setOf("athena", "svg"), result.evidence.stagedSourceExtensions)
        assertTrue(result.evidence.generatedResourceIds.all { resourceId -> resourceId.startsWith("generated:athena.vendor:") })
        assertEquals(setOf("GRAPHIC_PRIMITIVE"), result.evidence.compiledBodyAuthorities)
        assertTrue(result.evidence.rendererFileAccessAuthorityAbsent)
        assertTrue(result.evidence.xmlRuntimeAuthorityAbsent)
        assertTrue(result.evidence.rawSvgTransportAbsent)
    }

    @Test
    fun `same definition identity in separate packages keeps generated descriptor resources package local`() {
        val repositoryRoot = Files.createTempDirectory("athena-m34-snapshot-package-identity")
        val packageRoot = repositoryRoot.resolve("packages/representation").createDirectories()
        val firstRoot = packageRoot.resolve("athena/vendor").createDirectories()
        val secondRoot = packageRoot.resolve("other/vendor").createDirectories()
        firstRoot.resolve("vendor-drive.athena").writeText(VALID_SOURCE)
        firstRoot.resolve("vendor-drive.svg").writeText(VALID_SVG)
        secondRoot.resolve("vendor-drive.athena").writeText(VALID_SOURCE.replace("package athena.vendor", "package other.vendor"))
        secondRoot.resolve("vendor-drive.svg").writeText(VALID_SVG)
        val staged = RepresentationPackageSnapshotStager().stage(
            RepresentationPackageSnapshotRequest(
                repositoryRoot = repositoryRoot,
                packageRoots = listOf(packageRoot),
                dependencyLockDigest = "lock:package-identity",
                snapshotDirectory = repositoryRoot.resolve(".athena/snapshots/package-identity"),
            ),
        )

        val result = AthenaRepresentationPackageSnapshotCompiler().compile(assertNotNull(staged.snapshot))

        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        assertEquals(setOf("athena.vendor", "other.vendor"), result.definitions.map { it.libraryId.value }.toSet())
        assertEquals(2, result.descriptors.size)
        assertEquals(2, result.descriptors.map { it.resource.resourceId }.distinct().size)
        assertTrue(result.descriptors.all { descriptor -> descriptor.variants.map { it.value } == listOf("standard") })
        assertEquals(2, result.resources.size)
        assertEquals(2, result.resources.map { it.key }.distinct().size)
    }

    @Test
    fun `rejects duplicate representation identities across snapshot independent of file order`() {
        val repositoryRoot = Files.createTempDirectory("athena-m34-snapshot-duplicate")
        val packageRoot = repositoryRoot.resolve("packages/representation").createDirectories()
        val vendorRoot = packageRoot.resolve("athena/vendor").createDirectories()
        vendorRoot.resolve("a.athena").writeText(VALID_SOURCE)
        vendorRoot.resolve("b.athena").writeText(VALID_SOURCE.replace("vendor_drive", "vendor_drive_copy"))
        vendorRoot.resolve("vendor-drive.svg").writeText(VALID_SVG)
        val staged = RepresentationPackageSnapshotStager().stage(
            RepresentationPackageSnapshotRequest(
                repositoryRoot = repositoryRoot,
                packageRoots = listOf(packageRoot),
                dependencyLockDigest = "lock:abc",
                snapshotDirectory = repositoryRoot.resolve(".athena/snapshots/duplicate"),
            ),
        )

        val result = AthenaRepresentationPackageSnapshotCompiler().compile(assertNotNull(staged.snapshot))

        assertTrue(result.definitions.isEmpty())
        val diagnostics = result.diagnostics.filter { it.code == "representation.identity.duplicate" }
        assertEquals(2, diagnostics.size, result.diagnostics.toString())
        assertEquals(diagnostics.map { it.file }.sorted(), diagnostics.map { it.file })
    }

    @Test
    fun `compiles profile and binding declarations into typed package-model values`() {
        val repositoryRoot = Files.createTempDirectory("athena-m34-snapshot-binding")
        val packageRoot = repositoryRoot.resolve("packages/representation").createDirectories()
        val vendorRoot = packageRoot.resolve("athena/vendor").createDirectories()
        vendorRoot.resolve("device.athena").writeText(VALID_SOURCE)
        vendorRoot.resolve("vendor-drive.svg").writeText(VALID_SVG)
        vendorRoot.resolve("bindings.athena").writeText(VALID_BINDING_SOURCE)
        val staged = RepresentationPackageSnapshotStager().stage(
            RepresentationPackageSnapshotRequest(
                repositoryRoot = repositoryRoot,
                packageRoots = listOf(packageRoot),
                dependencyLockDigest = "lock:binding",
                snapshotDirectory = repositoryRoot.resolve(".athena/snapshots/binding"),
            ),
        )

        assertTrue(staged.diagnostics.isEmpty(), staged.diagnostics.toString())
        val result = AthenaRepresentationPackageSnapshotCompiler().compile(assertNotNull(staged.snapshot))

        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        assertEquals(listOf("CabinetIEC"), result.profiles.map { it.profileId.value })
        val rule = result.bindingRules.single()
        assertEquals("M34DriveCabinet", rule.ruleId.value)
        assertEquals("CabinetIEC", rule.profileId.value)
        assertEquals("cabinet", rule.projectionContext.value)
        assertEquals("Drive", rule.conceptId.value)
        assertEquals(listOf("model" to "ACS380", "type" to "Drive"), rule.selectorFacts.map { it.name to it.value }.sortedBy { it.first })
        assertEquals("athena.vendor", rule.target.representationPackageId.value)
        assertEquals("athena.vendor.drive", rule.target.descriptorId.value)
        assertEquals("1.0.0", rule.target.packageVersion.value)
        assertEquals("standard", rule.target.variantId?.value)
        assertEquals(100, rule.priority.value)
    }

    @Test
    fun `m34 sample package compiles from athena source with generated descriptors and no xml authority`() {
        val sampleRoot = Files.createTempDirectory("athena-m34-sample-authority")
        copyTree(repositoryRoot().resolve("examples/m34/sample-project"), sampleRoot)
        val staged = RepresentationPackageSnapshotStager().stageRepository(
            repositoryRoot = sampleRoot,
            snapshotDirectory = sampleRoot.resolve(".athena/snapshots/m34-authority"),
        )

        assertTrue(staged.diagnostics.isEmpty(), staged.diagnostics.toString())
        val result = AthenaRepresentationPackageSnapshotCompiler().compile(assertNotNull(staged.snapshot))

        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        assertTrue(result.definitions.isNotEmpty())
        assertEquals(result.definitions.map { it.symbolId.value }.sorted(), result.descriptors.map { it.descriptorId.value }.sorted())
        assertTrue(result.bindingRules.isNotEmpty())
        assertTrue(result.definitions.all { definition -> definition.lifecycle.provenance.source.endsWith(".athena") })
        assertTrue(result.evidence.sourceHashes.keys.none { path -> path.endsWith(".xml") })
        assertTrue(result.evidence.xmlRuntimeAuthorityAbsent)
        val driveDescriptor = result.descriptors.single { descriptor -> descriptor.descriptorId.value == "vendor.drive.element" }
        assertTrue(driveDescriptor.anchors.isEmpty())
        assertEquals(setOf("deviceTag"), driveDescriptor.labelSlots.map { it.slotId.value }.toSet())
    }

    @Test
    fun `m35 standard and vendor package evidence compiles reproducibly from manifest authority`() {
        val sampleRoot = Files.createTempDirectory("athena-m35-package-evidence")
        copyTree(repositoryRoot().resolve("examples/m35/package-platform-proof"), sampleRoot)
        val lockValidation = AthenaCompiler().validateRepositoryLock(sampleRoot)

        assertTrue(
            lockValidation.isValid,
            lockValidation.diagnostics.joinToString(separator = "\n") { diagnostic -> "${diagnostic.code}: ${diagnostic.message}" },
        )
        val firstStage = RepresentationPackageSnapshotStager().stageRepository(
            repositoryRoot = sampleRoot,
            snapshotDirectory = sampleRoot.resolve(".athena/snapshots/package-evidence-first"),
        )
        val secondStage = RepresentationPackageSnapshotStager().stageRepository(
            repositoryRoot = sampleRoot,
            snapshotDirectory = sampleRoot.resolve(".athena/snapshots/package-evidence-second"),
        )

        assertTrue(firstStage.diagnostics.isEmpty(), firstStage.diagnostics.toString())
        assertTrue(secondStage.diagnostics.isEmpty(), secondStage.diagnostics.toString())
        val firstSnapshot = assertNotNull(firstStage.snapshot)
        val secondSnapshot = assertNotNull(secondStage.snapshot)
        assertEquals(firstSnapshot.snapshotId, secondSnapshot.snapshotId)
        assertEquals(firstSnapshot.files.map { it.repositoryRelativePath }, secondSnapshot.files.map { it.repositoryRelativePath })
        assertEquals(firstSnapshot.files.map { it.contentHash }, secondSnapshot.files.map { it.contentHash })
        assertEquals(
            listOf(
                "packages/representation/com/engineeringood/m35/standard/iec/standard-elements.athena",
                "packages/representation/com/engineeringood/m35/vendor/abb/pfea112/pfea112.svg",
                "packages/representation/com/engineeringood/m35/vendor/abb/pfea112/vendor-elements.athena",
            ),
            firstSnapshot.files.map { it.repositoryRelativePath },
        )

        val compiler = AthenaRepresentationPackageSnapshotCompiler()
        val first = compiler.compile(firstSnapshot)
        val second = compiler.compile(secondSnapshot)

        assertTrue(first.diagnostics.isEmpty(), first.diagnostics.toString())
        assertTrue(second.diagnostics.isEmpty(), second.diagnostics.toString())
        assertEquals(first.evidence.snapshotId, second.evidence.snapshotId)
        assertEquals(first.evidence.dependencyLockDigest, second.evidence.dependencyLockDigest)
        assertEquals(first.evidence.compilerSchemaVersion, second.evidence.compilerSchemaVersion)
        assertEquals(first.evidence.sourceHashes, second.evidence.sourceHashes)
        assertEquals(first.evidence.generatedResourceIds, second.evidence.generatedResourceIds)
        assertEquals(first.evidence.compiledBodyAuthorities, second.evidence.compiledBodyAuthorities)
        assertEquals(
            listOf("iec.protective-earth.element", "vendor.abb.pfea112.element"),
            first.definitions
                .filter { definition -> definition.definitionKind == RepresentationDefinitionKind.ELEMENT }
                .map { it.symbolId.value }
                .sorted(),
        )
        assertEquals(first.definitions.map { it.symbolId.value }.sorted(), first.descriptors.map { it.descriptorId.value }.sorted())
        assertTrue(first.evidence.sourceHashes.keys.none { path -> path.endsWith(".xml") || path.endsWith(".html") })
        assertTrue(first.evidence.xmlRuntimeAuthorityAbsent)
        assertTrue(first.evidence.rawSvgTransportAbsent)
        assertTrue(first.evidence.rendererFileAccessAuthorityAbsent)
        assertEquals(setOf("GRAPHIC_PRIMITIVE"), first.evidence.compiledBodyAuthorities)
    }

    @Test
    fun `m35 package evidence changes identity for resource edits and rejects malicious svg paths`() {
        val sampleRoot = Files.createTempDirectory("athena-m35-package-evidence-negative")
        copyTree(repositoryRoot().resolve("examples/m35/package-platform-proof"), sampleRoot)
        val clean = RepresentationPackageSnapshotStager().stageRepository(
            repositoryRoot = sampleRoot,
            snapshotDirectory = sampleRoot.resolve(".athena/snapshots/package-evidence-clean"),
        )
        assertTrue(clean.diagnostics.isEmpty(), clean.diagnostics.toString())
        val cleanSnapshot = assertNotNull(clean.snapshot)
        val vendorSvg = sampleRoot.resolve("packages/representation/com/engineeringood/m35/vendor/abb/pfea112/pfea112.svg")
        Files.writeString(vendorSvg, Files.readString(vendorSvg).replace("PFEA112", "PFEA112-CHANGED"))
        val changed = RepresentationPackageSnapshotStager().stageRepository(
            repositoryRoot = sampleRoot,
            snapshotDirectory = sampleRoot.resolve(".athena/snapshots/package-evidence-changed"),
        )

        assertTrue(changed.diagnostics.isEmpty(), changed.diagnostics.toString())
        assertTrue(cleanSnapshot.snapshotId != assertNotNull(changed.snapshot).snapshotId)

        val vendorSource = sampleRoot.resolve("packages/representation/com/engineeringood/m35/vendor/abb/pfea112/vendor-elements.athena")
        Files.writeString(vendorSource, Files.readString(vendorSource).replace("path \"./pfea112.svg\"", "path \"../pfea112.svg\""))
        val maliciousStage = RepresentationPackageSnapshotStager().stageRepository(
            repositoryRoot = sampleRoot,
            snapshotDirectory = sampleRoot.resolve(".athena/snapshots/package-evidence-malicious"),
        )
        assertTrue(maliciousStage.diagnostics.isEmpty(), maliciousStage.diagnostics.toString())
        val malicious = AthenaRepresentationPackageSnapshotCompiler().compile(assertNotNull(maliciousStage.snapshot))

        assertTrue(
            malicious.diagnostics.any { diagnostic -> diagnostic.code == "resource.path.invalid" },
            malicious.diagnostics.toString(),
        )
        assertTrue(malicious.definitions.none { definition -> definition.symbolId.value == "vendor.abb.pfea112.element" })
        assertTrue(malicious.descriptors.none { descriptor -> descriptor.descriptorId.value == "vendor.abb.pfea112.element" })
    }

    @Test
    fun `generated descriptor preserves native dynamic label placement and style`() {
        val repositoryRoot = Files.createTempDirectory("athena-m34-label-descriptor")
        val packageRoot = repositoryRoot.resolve("packages/representation").createDirectories()
        val materialRoot = packageRoot.resolve("athena/labels").createDirectories()
        materialRoot.resolve("label.athena").writeText(VALID_NATIVE_LABEL_SOURCE)
        val staged = RepresentationPackageSnapshotStager().stage(
            RepresentationPackageSnapshotRequest(
                repositoryRoot = repositoryRoot,
                packageRoots = listOf(packageRoot),
                dependencyLockDigest = "lock:label",
                snapshotDirectory = repositoryRoot.resolve(".athena/snapshots/label"),
            ),
        )

        val result = AthenaRepresentationPackageSnapshotCompiler().compile(assertNotNull(staged.snapshot))

        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        val label = result.descriptors.single { descriptor -> descriptor.descriptorId.value == "iec.label.element" }
            .labelSlots.single()
        assertEquals(4.0, label.placement?.originX)
        assertEquals(6.0, label.placement?.originY)
        assertEquals(4.0, label.placement?.boundsX)
        assertEquals(6.0, label.placement?.boundsY)
        assertEquals(72.0, label.placement?.width)
        assertEquals(12.0, label.placement?.height)
        assertEquals("device-label", label.styleTokenRef?.value)
    }

    @Test
    fun `generated descriptor derives anchor sides from canonical boundary points`() {
        val repositoryRoot = Files.createTempDirectory("athena-m34-anchor-sides")
        val packageRoot = repositoryRoot.resolve("packages/representation").createDirectories()
        val materialRoot = packageRoot.resolve("athena/sides").createDirectories()
        materialRoot.resolve("sides.athena").writeText(VALID_SIDED_ANCHOR_SOURCE)
        val staged = RepresentationPackageSnapshotStager().stage(
            RepresentationPackageSnapshotRequest(
                repositoryRoot = repositoryRoot,
                packageRoots = listOf(packageRoot),
                dependencyLockDigest = "lock:sides",
                snapshotDirectory = repositoryRoot.resolve(".athena/snapshots/sides"),
            ),
        )

        val result = AthenaRepresentationPackageSnapshotCompiler().compile(assertNotNull(staged.snapshot))

        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        assertEquals(
            mapOf(
                "left" to RepresentationAnchorSide.LEFT,
                "right" to RepresentationAnchorSide.RIGHT,
                "top" to RepresentationAnchorSide.TOP,
                "bottom" to RepresentationAnchorSide.BOTTOM,
            ),
            result.descriptors.single().anchors.associate { anchor -> anchor.anchorId.value to anchor.side },
        )
    }

    companion object {
        private val VALID_SOURCE = """
            package athena.vendor

            element vendor_drive {
              identity "athena.vendor.drive"
              version "1.0.0"

              resource vendor_drive_svg {
                kind svg
                path "./vendor-drive.svg"
              }

              graphic svg resource vendor_drive_svg
            }
        """.trimIndent()

        private val VALID_SVG = """
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 240 360">
              <rect id="body" x="8" y="8" width="224" height="344" data-athena-ref="anchor:body"/>
              <circle id="power-in-dot" cx="24" cy="48" r="4" data-athena-ref="anchor:power-in-dot"/>
              <text id="tag" x="120" y="32" data-athena-ref="anchor:tag">ACS380</text>
            </svg>
        """.trimIndent()

        private val VALID_BINDING_SOURCE = """
            package athena.vendor

            profile CabinetIEC {
              projection cabinet
              standard IEC
              style athena-industrial-iec-v1
              fallback fail-closed
            }

            binding M34DriveCabinet {
              profile CabinetIEC
              priority 100

              select device where {
                type Drive
                model "ACS380"
              }

              use element "athena.vendor.drive" version "1.0.0"
              variant "standard"
            }
        """.trimIndent()

        private val VALID_NATIVE_LABEL_SOURCE = """
            package athena.labels

            symbol label_symbol {
              identity "iec.label.symbol"
              version "1.0.0"
              graphic {
                bounds (0, 0, 80, 40)
                rectangle body at (0, 0) size (80, 40) style symbol
                label deviceTag at (4, 6) size (72, 12) role device-tag style device-label
              }
            }

            element label_element {
              identity "iec.label.element"
              version "1.0.0"
              bounds (0, 0, 80, 40)
              child glyph { symbol "iec.label.symbol" translate (0, 0) rotate 0 scale (1, 1) zOrder 0 }
              export label deviceTag from glyph.deviceTag
            }
        """.trimIndent()

        private val VALID_SIDED_ANCHOR_SOURCE = """
            package athena.sides

            symbol sided_symbol {
              identity "iec.sided.symbol"
              version "1.0.0"
              graphic {
                bounds (0, 0, 80, 40)
                line horizontal from (0, 20) to (80, 20) style conductor
                line vertical from (40, 0) to (40, 40) style conductor
              }
              anchor left { ref "horizontal" point (0, 20) role terminal direction in signal Control.family }
              anchor right { ref "horizontal" point (80, 20) role terminal direction out signal Control.family }
              anchor top { ref "vertical" point (40, 0) role terminal direction in signal Control.family }
              anchor bottom { ref "vertical" point (40, 40) role terminal direction out signal Control.family }
            }
        """.trimIndent()
    }
}

private fun repositoryRoot(): Path {
    var current = Path.of("").toAbsolutePath()
    while (current.parent != null && !current.resolve("settings.gradle.kts").toFile().exists()) {
        current = current.parent
    }
    return current
}

private fun copyTree(sourceRoot: Path, targetRoot: Path) {
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
