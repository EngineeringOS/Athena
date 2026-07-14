package com.engineeringood.athena.runtime

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.reuse.SemanticMacroId
import com.engineeringood.athena.reuse.SemanticMacroInstantiationId
import com.engineeringood.athena.reuse.SemanticMacroParameterName
import com.engineeringood.athena.reuse.SemanticMacroParameterValue
import com.engineeringood.athena.reuse.SemanticMacroPreviewId
import com.engineeringood.athena.reuse.SemanticMacroPreviewStatus
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AthenaSemanticMacroRuntimeServiceTest {
    @Test
    fun `shared semantic macro runtime seams stay platform owned and do not mutate canonical state`() {
        val sourcePath = writeProject(reuseFixture())

        try {
            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(sourcePath.parent).activateProject(
                projectName = "reuse-runtime",
                sourcePath = sourcePath,
            )
            val canonicalCompilation = context.compileActiveProject()

            val catalog = assertIs<AthenaSemanticMacroCatalogUnavailable>(
                context.reuseRuntime().catalog(
                    context = context,
                    request = AthenaSemanticMacroCatalogRequest(),
                ),
            )
            val validation = assertIs<AthenaSemanticMacroValidationUnavailable>(
                context.reuseRuntime().validate(
                    context = context,
                    request = AthenaSemanticMacroValidationRequest(
                        macroId = SemanticMacroId("macro:dol-starter"),
                        instantiationId = SemanticMacroInstantiationId("instance:M1"),
                        parameterValues = mapOf(
                            SemanticMacroParameterName("starterTag") to SemanticMacroParameterValue.Text("M1"),
                        ),
                    ),
                ),
            )
            val preview = assertIs<AthenaSemanticMacroPreviewUnavailable>(
                context.reuseRuntime().preview(
                    context = context,
                    request = AthenaSemanticMacroPreviewRequest(
                        macroId = SemanticMacroId("macro:dol-starter"),
                        instantiationId = SemanticMacroInstantiationId("instance:M1"),
                        parameterValues = mapOf(
                            SemanticMacroParameterName("starterTag") to SemanticMacroParameterValue.Text("M1"),
                        ),
                    ),
                ),
            )
            val acceptance = assertIs<AthenaSemanticMacroAcceptanceUnavailable>(
                context.reuseRuntime().accept(
                    context = context,
                    request = AthenaSemanticMacroAcceptanceRequest(
                        previewId = SemanticMacroPreviewId("preview:dol-starter:M1"),
                        macroId = SemanticMacroId("macro:dol-starter"),
                        instantiationId = SemanticMacroInstantiationId("instance:M1"),
                    ),
                ),
            )
            val originInspection = assertIs<AthenaSemanticMacroOriginInspectionUnavailable>(
                context.reuseRuntime().inspectOrigin(
                    context = context,
                    request = AthenaSemanticMacroOriginInspectionRequest(
                        subjectId = StableSemanticIdentity("component:M1"),
                    ),
                ),
            )

            assertTrue(catalog.reason.contains("athena.lock"))
            assertEquals("macro:dol-starter", validation.macroId.value)
            assertEquals("instance:M1", validation.instantiationId.value)
            assertEquals("preview:dol-starter:M1", acceptance.previewId.value)
            assertEquals("component:M1", originInspection.subjectId?.value)
            assertSame(canonicalCompilation, context.compileActiveProject())
            assertTrue(context.commandRuntime().history(context).records.isEmpty())
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }

    @Test
    fun `reuse runtime seam accepts semantic requests without widget specific identifiers`() {
        val request = AthenaSemanticMacroPreviewRequest(
            macroId = SemanticMacroId("macro:panel-meter"),
            instantiationId = SemanticMacroInstantiationId("instance:PM1"),
            parameterValues = mapOf(
                SemanticMacroParameterName("meterTag") to SemanticMacroParameterValue.Text("PM1"),
                SemanticMacroParameterName("scale") to SemanticMacroParameterValue.IntegerValue(100),
            ),
        )

        assertEquals("macro:panel-meter", request.macroId.value)
        assertEquals("instance:PM1", request.instantiationId.value)
        assertEquals(setOf("meterTag", "scale"), request.parameterValues.keys.map { parameter -> parameter.value }.toSet())
    }

    @Test
    fun `catalog resolves active semantic macros only from the governed package graph deterministically`() {
        val repositoryRoot = Files.createTempDirectory("athena-semantic-macro-catalog-")
        try {
            val sourcePath = writeGovernedRepository(
                repositoryRoot = repositoryRoot,
                packageName = "com.engineeringood.root",
                sourceFileName = "root.athena",
                manifestBody = """
                    primaryPackage:
                      name: com.engineeringood.root
                      version: 1.0.0
                      sourceRoot: src
                    dependencies:
                      - name: com.engineeringood.alpha
                        source: local-path
                        locator: vendor/alpha
                """.trimIndent(),
            )
            writeGovernedRepository(
                repositoryRoot = repositoryRoot.resolve("vendor").resolve("alpha"),
                packageName = "com.engineeringood.alpha",
                sourceFileName = "alpha.athena",
                manifestBody = """
                    primaryPackage:
                      name: com.engineeringood.alpha
                      version: 1.0.0
                      sourceRoot: src
                """.trimIndent(),
            )
            writeGovernedRepository(
                repositoryRoot = repositoryRoot.resolve("vendor").resolve("gamma"),
                packageName = "com.engineeringood.gamma",
                sourceFileName = "gamma.athena",
                manifestBody = """
                    primaryPackage:
                      name: com.engineeringood.gamma
                      version: 1.0.0
                      sourceRoot: src
                """.trimIndent(),
            )
            writeSemanticMacroManifest(
                packageRoot = repositoryRoot,
                body = """
                    package.format.version=1
                    macro.root.id=macro:root-starter
                    macro.root.displayName=Root Starter
                    macro.root.summary=Root governed starter
                    macro.root.definitionPath=macros/root-starter.macro
                    macro.root.classificationKeys=electrical,starter
                """.trimIndent(),
                definitionPath = "macros/root-starter.macro",
            )
            writeSemanticMacroManifest(
                packageRoot = repositoryRoot.resolve("vendor").resolve("alpha"),
                body = """
                    package.format.version=1
                    macro.alpha.id=macro:alpha-panel-meter
                    macro.alpha.displayName=Alpha Panel Meter
                    macro.alpha.summary=Alpha governed panel meter
                    macro.alpha.definitionPath=macros/alpha-panel-meter.macro
                    macro.alpha.classificationKeys=electrical,meter
                """.trimIndent(),
                definitionPath = "macros/alpha-panel-meter.macro",
            )
            writeSemanticMacroManifest(
                packageRoot = repositoryRoot.resolve("vendor").resolve("gamma"),
                body = """
                    package.format.version=1
                    macro.gamma.id=macro:gamma-shadow
                    macro.gamma.displayName=Gamma Shadow
                    macro.gamma.summary=Should not be admitted
                    macro.gamma.definitionPath=macros/gamma-shadow.macro
                """.trimIndent(),
                definitionPath = "macros/gamma-shadow.macro",
            )

            AthenaRuntime().openWorkspace(repositoryRoot)
            com.engineeringood.athena.compiler.AthenaCompiler().materializeRepositoryLock(repositoryRoot)

            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(repositoryRoot).activateProject(
                projectName = "root",
                sourcePath = sourcePath,
            )

            val first = assertIs<AthenaSemanticMacroCatalogReady>(
                context.reuseRuntime().catalog(
                    context = context,
                    request = AthenaSemanticMacroCatalogRequest(),
                ),
            )
            val second = assertIs<AthenaSemanticMacroCatalogReady>(
                context.reuseRuntime().catalog(
                    context = context,
                    request = AthenaSemanticMacroCatalogRequest(),
                ),
            )

            assertEquals(first, second)
            assertEquals(
                listOf("macro:alpha-panel-meter", "macro:root-starter"),
                first.entries.map { entry -> entry.macroId.value },
            )
            assertEquals(
                listOf("com.engineeringood.alpha", "com.engineeringood.root"),
                first.entries.map { entry -> entry.packageId.name },
            )
            assertTrue(first.entries.none { entry -> entry.macroId.value == "macro:gamma-shadow" })
            assertTrue(first.diagnostics.isEmpty())
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `validation publishes governed parameter schema defaults and blocking diagnostics`() {
        val repositoryRoot = Files.createTempDirectory("athena-semantic-macro-validation-")
        try {
            val sourcePath = writeGovernedRepository(
                repositoryRoot = repositoryRoot,
                packageName = "com.engineeringood.root",
                sourceFileName = "root.athena",
                manifestBody = """
                    primaryPackage:
                      name: com.engineeringood.root
                      version: 1.0.0
                      sourceRoot: src
                """.trimIndent(),
            )
            writeSemanticMacroManifest(
                packageRoot = repositoryRoot,
                body = """
                    package.format.version=1
                    macro.dol.id=macro:dol-starter
                    macro.dol.displayName=DOL Starter
                    macro.dol.summary=Governed DOL starter proof
                    macro.dol.definitionPath=macros/dol-starter.macro
                    macro.dol.classificationKeys=electrical,starter,dol
                    macro.dol.parameter.motorPower.kind=symbol
                    macro.dol.parameter.motorPower.label=Motor power
                    macro.dol.parameter.motorPower.required=true
                    macro.dol.parameter.motorPower.allowedValues=5.5kW,7.5kW,11kW
                    macro.dol.parameter.controlVoltage.kind=symbol
                    macro.dol.parameter.controlVoltage.label=Control voltage
                    macro.dol.parameter.controlVoltage.required=true
                    macro.dol.parameter.controlVoltage.defaultSymbol=24VDC
                    macro.dol.parameter.controlVoltage.allowedValues=24VDC,110VAC
                    macro.dol.parameter.vendorFamily.kind=symbol
                    macro.dol.parameter.vendorFamily.label=Vendor family
                    macro.dol.parameter.vendorFamily.required=true
                    macro.dol.parameter.vendorFamily.defaultSymbol=Siemens
                    macro.dol.parameter.vendorFamily.allowedValues=Siemens,Schneider
                    macro.dol.parameter.tagPrefix.kind=symbol
                    macro.dol.parameter.tagPrefix.label=Tag prefix
                    macro.dol.parameter.tagPrefix.required=true
                    macro.dol.parameter.tagPrefix.pattern=^[A-Z][A-Z0-9]{0,7}$
                """.trimIndent(),
                definitionPath = "macros/dol-starter.macro",
                definitionContent = dolStarterDefinition(),
            )

            com.engineeringood.athena.compiler.AthenaCompiler().materializeRepositoryLock(repositoryRoot)

            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(repositoryRoot).activateProject(
                projectName = "root",
                sourcePath = sourcePath,
            )

            val valid = assertIs<AthenaSemanticMacroValidationValid>(
                context.reuseRuntime().validate(
                    context = context,
                    request = AthenaSemanticMacroValidationRequest(
                        macroId = SemanticMacroId("macro:dol-starter"),
                        instantiationId = SemanticMacroInstantiationId("instance:M1"),
                        parameterValues = mapOf(
                            SemanticMacroParameterName("motorPower") to SemanticMacroParameterValue.Symbol("7.5kW"),
                            SemanticMacroParameterName("tagPrefix") to SemanticMacroParameterValue.Symbol("M1"),
                        ),
                    ),
                ),
            )
            val invalid = assertIs<AthenaSemanticMacroValidationInvalid>(
                context.reuseRuntime().validate(
                    context = context,
                    request = AthenaSemanticMacroValidationRequest(
                        macroId = SemanticMacroId("macro:dol-starter"),
                        instantiationId = SemanticMacroInstantiationId("instance:M1"),
                        parameterValues = mapOf(
                            SemanticMacroParameterName("motorPower") to SemanticMacroParameterValue.Symbol("3kW"),
                            SemanticMacroParameterName("tagPrefix") to SemanticMacroParameterValue.Symbol("m1"),
                            SemanticMacroParameterName("unknownToggle") to SemanticMacroParameterValue.BooleanValue(true),
                        ),
                    ),
                ),
            )
            val blockedPreview = assertIs<AthenaSemanticMacroPreviewUnavailable>(
                context.reuseRuntime().preview(
                    context = context,
                    request = AthenaSemanticMacroPreviewRequest(
                        macroId = SemanticMacroId("macro:dol-starter"),
                        instantiationId = SemanticMacroInstantiationId("instance:M1"),
                        parameterValues = mapOf(
                            SemanticMacroParameterName("motorPower") to SemanticMacroParameterValue.Symbol("3kW"),
                            SemanticMacroParameterName("tagPrefix") to SemanticMacroParameterValue.Symbol("m1"),
                        ),
                    ),
                ),
            )

            assertEquals(
                listOf("controlVoltage", "motorPower", "tagPrefix", "vendorFamily"),
                valid.parameters.map { parameter -> parameter.name.value },
            )
            assertEquals(
                "24VDC",
                (valid.normalizedValues[SemanticMacroParameterName("controlVoltage")] as SemanticMacroParameterValue.Symbol).text,
            )
            assertEquals(
                "Siemens",
                (valid.normalizedValues[SemanticMacroParameterName("vendorFamily")] as SemanticMacroParameterValue.Symbol).text,
            )
            assertTrue(valid.diagnostics.isEmpty())
            assertEquals(
                setOf(
                    "semantic.macro.validation.parameter.allowed-values",
                    "semantic.macro.validation.parameter.pattern",
                    "semantic.macro.validation.parameter.unknown",
                ),
                invalid.diagnostics.map { diagnostic -> diagnostic.code }.toSet(),
            )
            assertTrue(blockedPreview.reason.contains("blocked until validation succeeds"))
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `preview assembles deterministic semantic consequences from governed template definitions`() {
        val repositoryRoot = Files.createTempDirectory("athena-semantic-macro-preview-")
        try {
            val sourcePath = writeGovernedRepository(
                repositoryRoot = repositoryRoot,
                packageName = "com.engineeringood.root",
                sourceFileName = "root.athena",
                manifestBody = """
                    primaryPackage:
                      name: com.engineeringood.root
                      version: 1.0.0
                      sourceRoot: src
                """.trimIndent(),
            )
            writeSemanticMacroManifest(
                packageRoot = repositoryRoot,
                body = """
                    package.format.version=1
                    macro.dol.id=macro:dol-starter
                    macro.dol.displayName=DOL Starter
                    macro.dol.summary=Governed DOL starter proof
                    macro.dol.definitionPath=macros/dol-starter.macro
                    macro.dol.classificationKeys=electrical,starter,dol
                    macro.dol.parameter.motorPower.kind=symbol
                    macro.dol.parameter.motorPower.label=Motor power
                    macro.dol.parameter.motorPower.required=true
                    macro.dol.parameter.motorPower.allowedValues=5.5kW,7.5kW,11kW
                    macro.dol.parameter.controlVoltage.kind=symbol
                    macro.dol.parameter.controlVoltage.label=Control voltage
                    macro.dol.parameter.controlVoltage.required=true
                    macro.dol.parameter.controlVoltage.defaultSymbol=24VDC
                    macro.dol.parameter.controlVoltage.allowedValues=24VDC,110VAC
                    macro.dol.parameter.vendorFamily.kind=symbol
                    macro.dol.parameter.vendorFamily.label=Vendor family
                    macro.dol.parameter.vendorFamily.required=true
                    macro.dol.parameter.vendorFamily.defaultSymbol=Siemens
                    macro.dol.parameter.vendorFamily.allowedValues=Siemens,Schneider
                    macro.dol.parameter.tagPrefix.kind=symbol
                    macro.dol.parameter.tagPrefix.label=Tag prefix
                    macro.dol.parameter.tagPrefix.required=true
                    macro.dol.parameter.tagPrefix.pattern=^[A-Z][A-Z0-9]{0,7}$
                """.trimIndent(),
                definitionPath = "macros/dol-starter.macro",
                definitionContent = dolStarterDefinition(),
            )

            com.engineeringood.athena.compiler.AthenaCompiler().materializeRepositoryLock(repositoryRoot)

            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(repositoryRoot).activateProject(
                projectName = "root",
                sourcePath = sourcePath,
            )

            val first = assertIs<AthenaSemanticMacroPreviewReady>(
                context.reuseRuntime().preview(
                    context = context,
                    request = AthenaSemanticMacroPreviewRequest(
                        macroId = SemanticMacroId("macro:dol-starter"),
                        instantiationId = SemanticMacroInstantiationId("instance:M1"),
                        parameterValues = mapOf(
                            SemanticMacroParameterName("motorPower") to SemanticMacroParameterValue.Symbol("7.5kW"),
                            SemanticMacroParameterName("tagPrefix") to SemanticMacroParameterValue.Symbol("M1"),
                        ),
                    ),
                ),
            )
            val second = assertIs<AthenaSemanticMacroPreviewReady>(
                context.reuseRuntime().preview(
                    context = context,
                    request = AthenaSemanticMacroPreviewRequest(
                        macroId = SemanticMacroId("macro:dol-starter"),
                        instantiationId = SemanticMacroInstantiationId("instance:M1"),
                        parameterValues = mapOf(
                            SemanticMacroParameterName("motorPower") to SemanticMacroParameterValue.Symbol("7.5kW"),
                            SemanticMacroParameterName("tagPrefix") to SemanticMacroParameterValue.Symbol("M1"),
                        ),
                    ),
                ),
            )

            assertEquals(first, second)
            assertEquals("preview:dol-starter:M1", first.preview.previewId.value)
            assertEquals(
                listOf("template:starter.contactor", "template:starter.overload"),
                first.preview.components.map { component -> component.templateId },
            )
            assertEquals(
                listOf("template:starter.contactor:T1", "template:starter.overload:L1"),
                first.preview.ports.map { port -> "${port.componentTemplateId}:${port.portRoleId}" },
            )
            assertEquals(
                listOf("template:starter.power-link"),
                first.preview.connections.map { connection -> connection.templateId },
            )
            assertEquals(
                "M1",
                (first.preview.components.first().properties.getValue("tag") as SemanticMacroParameterValue.Symbol).text,
            )
            assertEquals(
                "24VDC",
                (first.preview.components.first().properties.getValue("controlVoltage") as SemanticMacroParameterValue.Symbol).text,
            )
            assertTrue(first.preview.originAnchors.any { anchor ->
                anchor.subjectKind == "component" && anchor.derivedSubjectIdentity?.value == "component:instance:M1:template:starter.contactor"
            })
            assertTrue(first.preview.originAnchors.any { anchor -> anchor.subjectKind == "connection" })
            assertTrue(first.preview.presentationConsequences.any { consequence -> consequence.hintType == "preferred-symbol-family" })
            assertTrue(first.preview.changes.any { change -> change.kind == com.engineeringood.athena.reuse.SemanticMacroPreviewChangeKind.CONNECT })
            assertTrue(first.preview.warnings.isEmpty())
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `acceptance commits approved preview through m8 and refreshes canonical state`() {
        val repositoryRoot = Files.createTempDirectory("athena-semantic-macro-acceptance-")
        try {
            val sourcePath = writeGovernedRepository(
                repositoryRoot = repositoryRoot,
                packageName = "com.engineeringood.root",
                sourceFileName = "root.athena",
                manifestBody = """
                    primaryPackage:
                      name: com.engineeringood.root
                      version: 1.0.0
                      sourceRoot: src
                """.trimIndent(),
            )
            writeSemanticMacroManifest(
                packageRoot = repositoryRoot,
                body = """
                    package.format.version=1
                    macro.dol.id=macro:dol-starter
                    macro.dol.displayName=DOL Starter
                    macro.dol.summary=Governed DOL starter proof
                    macro.dol.definitionPath=macros/dol-starter.macro
                    macro.dol.classificationKeys=electrical,starter,dol
                    macro.dol.parameter.motorPower.kind=symbol
                    macro.dol.parameter.motorPower.label=Motor power
                    macro.dol.parameter.motorPower.required=true
                    macro.dol.parameter.motorPower.allowedValues=5.5kW,7.5kW,11kW
                    macro.dol.parameter.controlVoltage.kind=symbol
                    macro.dol.parameter.controlVoltage.label=Control voltage
                    macro.dol.parameter.controlVoltage.required=true
                    macro.dol.parameter.controlVoltage.defaultSymbol=24VDC
                    macro.dol.parameter.controlVoltage.allowedValues=24VDC,110VAC
                    macro.dol.parameter.vendorFamily.kind=symbol
                    macro.dol.parameter.vendorFamily.label=Vendor family
                    macro.dol.parameter.vendorFamily.required=true
                    macro.dol.parameter.vendorFamily.defaultSymbol=Siemens
                    macro.dol.parameter.vendorFamily.allowedValues=Siemens,Schneider
                    macro.dol.parameter.tagPrefix.kind=symbol
                    macro.dol.parameter.tagPrefix.label=Tag prefix
                    macro.dol.parameter.tagPrefix.required=true
                    macro.dol.parameter.tagPrefix.pattern=^[A-Z][A-Z0-9]{0,7}$
                """.trimIndent(),
                definitionPath = "macros/dol-starter.macro",
                definitionContent = dolStarterDefinition(),
            )

            com.engineeringood.athena.compiler.AthenaCompiler().materializeRepositoryLock(repositoryRoot)

            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(repositoryRoot).activateProject(
                projectName = "root",
                sourcePath = sourcePath,
            )

            val preview = assertIs<AthenaSemanticMacroPreviewReady>(
                context.reuseRuntime().preview(
                    context = context,
                    request = AthenaSemanticMacroPreviewRequest(
                        macroId = SemanticMacroId("macro:dol-starter"),
                        instantiationId = SemanticMacroInstantiationId("instance:M1"),
                        parameterValues = mapOf(
                            SemanticMacroParameterName("motorPower") to SemanticMacroParameterValue.Symbol("7.5kW"),
                            SemanticMacroParameterName("tagPrefix") to SemanticMacroParameterValue.Symbol("M1"),
                        ),
                    ),
                ),
            )
            val acceptance = assertIs<AthenaSemanticMacroAcceptanceCommitted>(
                context.reuseRuntime().accept(
                    context = context,
                    request = AthenaSemanticMacroAcceptanceRequest(
                        previewId = preview.preview.previewId,
                        macroId = preview.preview.macroId,
                        instantiationId = preview.preview.instantiationId,
                    ),
                ),
            )

            assertEquals("bundle:dol-starter:M1", acceptance.bundle.bundleId)
            assertEquals("expansion:dol-starter:M1", acceptance.bundle.acceptedExpansion.expansionId.value)
            assertEquals(
                listOf("controlVoltage", "motorPower", "tagPrefix", "vendorFamily"),
                acceptance.bundle.acceptedExpansion.origin.parameterValues.keys.map { parameterName -> parameterName.value },
            )
            assertEquals("command-0001", acceptance.commandId)
            assertEquals("command", acceptance.inspection?.source?.name?.lowercase())
            assertEquals(6, acceptance.bundle.operations.size)
            assertEquals(5, acceptance.bundle.affectedSemanticIds.size)
            assertEquals(5, acceptance.changedSemanticIds.size)
            assertTrue(acceptance.bundle.operations.any { operation ->
                operation.kind == AthenaSemanticMacroMutationOperationKind.REGISTER_EXPANSION_TRACEABILITY
            })
            assertEquals(
                SemanticMacroPreviewStatus.ACCEPTED,
                context.semanticMacroPreviewSessionState().records.single().preview.status,
            )
            val activeDocument = assertIs<com.engineeringood.athena.compiler.CompilerCompilationSuccess>(
                context.compileActiveProject(),
            ).document
            assertEquals(2, activeDocument.components.size)
            assertEquals(2, activeDocument.ports.size)
            assertEquals(1, activeDocument.connections.size)
            assertEquals(AthenaCommandKind.APPLY_SEMANTIC_MACRO_BUNDLE, context.commandRuntime().history(context).records.single().commandKind)
            assertEquals(AthenaCommandOrigin.SEMANTIC_MACRO_ACCEPTED, context.commandRuntime().history(context).records.single().commandOrigin)
            assertTrue(acceptance.semanticReview != null)
            assertTrue(acceptance.reason.contains("sole M8 mutation authority"))
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `origin inspection resolves accepted expansion traceability from applied command history`() {
        val repositoryRoot = Files.createTempDirectory("athena-semantic-macro-origin-")
        try {
            val sourcePath = writeGovernedRepository(
                repositoryRoot = repositoryRoot,
                packageName = "com.engineeringood.root",
                sourceFileName = "root.athena",
                manifestBody = """
                    primaryPackage:
                      name: com.engineeringood.root
                      version: 1.0.0
                      sourceRoot: src
                """.trimIndent(),
            )
            writeSemanticMacroManifest(
                packageRoot = repositoryRoot,
                body = """
                    package.format.version=1
                    macro.dol.id=macro:dol-starter
                    macro.dol.displayName=DOL Starter
                    macro.dol.summary=Governed DOL starter proof
                    macro.dol.definitionPath=macros/dol-starter.macro
                    macro.dol.classificationKeys=electrical,starter,dol
                    macro.dol.parameter.motorPower.kind=symbol
                    macro.dol.parameter.motorPower.label=Motor power
                    macro.dol.parameter.motorPower.required=true
                    macro.dol.parameter.motorPower.allowedValues=5.5kW,7.5kW,11kW
                    macro.dol.parameter.controlVoltage.kind=symbol
                    macro.dol.parameter.controlVoltage.label=Control voltage
                    macro.dol.parameter.controlVoltage.required=true
                    macro.dol.parameter.controlVoltage.defaultSymbol=24VDC
                    macro.dol.parameter.controlVoltage.allowedValues=24VDC,110VAC
                    macro.dol.parameter.vendorFamily.kind=symbol
                    macro.dol.parameter.vendorFamily.label=Vendor family
                    macro.dol.parameter.vendorFamily.required=true
                    macro.dol.parameter.vendorFamily.defaultSymbol=Siemens
                    macro.dol.parameter.vendorFamily.allowedValues=Siemens,Schneider
                    macro.dol.parameter.tagPrefix.kind=symbol
                    macro.dol.parameter.tagPrefix.label=Tag prefix
                    macro.dol.parameter.tagPrefix.required=true
                    macro.dol.parameter.tagPrefix.pattern=^[A-Z][A-Z0-9]{0,7}$
                """.trimIndent(),
                definitionPath = "macros/dol-starter.macro",
                definitionContent = dolStarterDefinition(),
            )

            com.engineeringood.athena.compiler.AthenaCompiler().materializeRepositoryLock(repositoryRoot)

            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(repositoryRoot).activateProject(
                projectName = "root",
                sourcePath = sourcePath,
            )

            val preview = assertIs<AthenaSemanticMacroPreviewReady>(
                context.reuseRuntime().preview(
                    context = context,
                    request = AthenaSemanticMacroPreviewRequest(
                        macroId = SemanticMacroId("macro:dol-starter"),
                        instantiationId = SemanticMacroInstantiationId("instance:M1"),
                        parameterValues = mapOf(
                            SemanticMacroParameterName("motorPower") to SemanticMacroParameterValue.Symbol("7.5kW"),
                            SemanticMacroParameterName("tagPrefix") to SemanticMacroParameterValue.Symbol("M1"),
                        ),
                    ),
                ),
            )
            assertIs<AthenaSemanticMacroAcceptanceCommitted>(
                context.reuseRuntime().accept(
                    context = context,
                    request = AthenaSemanticMacroAcceptanceRequest(
                        previewId = preview.preview.previewId,
                        macroId = preview.preview.macroId,
                        instantiationId = preview.preview.instantiationId,
                    ),
                ),
            )

            val bySubject = assertIs<AthenaSemanticMacroOriginInspectionReady>(
                context.reuseRuntime().inspectOrigin(
                    context = context,
                    request = AthenaSemanticMacroOriginInspectionRequest(
                        subjectId = StableSemanticIdentity("component:instance:M1:template:starter.contactor"),
                    ),
                ),
            )
            val byInstantiation = assertIs<AthenaSemanticMacroOriginInspectionReady>(
                context.reuseRuntime().inspectOrigin(
                    context = context,
                    request = AthenaSemanticMacroOriginInspectionRequest(
                        instantiationId = SemanticMacroInstantiationId("instance:M1"),
                    ),
                ),
            )

            assertEquals("command-0001", bySubject.commandId)
            assertEquals("bundle:dol-starter:M1", bySubject.bundleId)
            assertEquals("expansion:dol-starter:M1", bySubject.acceptedExpansion.expansionId.value)
            assertEquals("macro:dol-starter", bySubject.acceptedExpansion.origin.macroId.value)
            assertEquals("instance:M1", bySubject.acceptedExpansion.origin.instantiationId.value)
            assertEquals("com.engineeringood.root", bySubject.acceptedExpansion.origin.packageBinding.packageId.name)
            assertEquals("1.0.0", bySubject.acceptedExpansion.origin.packageBinding.packageId.version)
            assertEquals("component:instance:M1:template:starter.contactor", bySubject.subjectId?.value)
            assertEquals("component:template:starter.contactor", bySubject.matchedMembership?.role)
            assertEquals(5, bySubject.acceptedExpansion.memberships.size)
            assertEquals(
                "7.5kW",
                (bySubject.acceptedExpansion.origin.parameterValues.getValue(SemanticMacroParameterName("motorPower")) as SemanticMacroParameterValue.Symbol).text,
            )
            assertEquals("expansion:dol-starter:M1", byInstantiation.acceptedExpansion.expansionId.value)
            assertEquals("instance:M1", byInstantiation.instantiationId?.value)
            assertEquals(null, byInstantiation.subjectId?.value)
            assertEquals(null, byInstantiation.matchedMembership?.role)

            context.commandRuntime().undo(context)

            val afterUndo = assertIs<AthenaSemanticMacroOriginInspectionUnavailable>(
                context.reuseRuntime().inspectOrigin(
                    context = context,
                    request = AthenaSemanticMacroOriginInspectionRequest(
                        subjectId = StableSemanticIdentity("component:instance:M1:template:starter.contactor"),
                    ),
                ),
            )
            assertTrue(afterUndo.reason.contains("No applied accepted Semantic Macro expansion"))
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    private fun writeProject(source: String): Path {
        val path = Files.createTempFile("athena-semantic-macro-runtime-", ".athena")
        Files.writeString(path, source)
        return path
    }

    private fun reuseFixture(): String {
        return """
            system ReuseRuntime {
              device PLC1 {
                type Switch
              }
            }
        """.trimIndent()
    }

    private fun writeGovernedRepository(
        repositoryRoot: Path,
        packageName: String,
        sourceFileName: String,
        manifestBody: String,
    ): Path {
        repositoryRoot.createDirectories()
        repositoryRoot.resolve("athena.yaml").writeText(manifestBody)
        repositoryRoot.resolve("athena.lock").writeText("# lock")
        val sourceRoot = repositoryRoot.resolve("src").createDirectories()
        val sourcePath = sourceRoot.resolve(sourceFileName)
        sourcePath.writeText("system ${sourceFileName.substringBefore('.').replaceFirstChar(Char::uppercase)} { }")
        return sourcePath
    }

    private fun writeSemanticMacroManifest(
        packageRoot: Path,
        body: String,
        definitionPath: String,
        definitionContent: String = "# semantic macro definition placeholder",
    ) {
        packageRoot.createDirectories()
        packageRoot.resolve("athena-semantic-macros.properties").writeText(body)
        val definitionFile = packageRoot.resolve(definitionPath)
        definitionFile.parent.createDirectories()
        definitionFile.writeText(definitionContent)
    }

    private fun dolStarterDefinition(): String {
        return """
            template.format.version=1
            component.contactor.id=template:starter.contactor
            component.contactor.conceptId=electrical.contactor
            component.contactor.implementationId=siemens.3rt2015
            component.contactor.displayName=Main contactor
            component.contactor.summary=Primary switching contactor
            component.contactor.tags=starter,power
            component.contactor.property.tag=param:tagPrefix
            component.contactor.property.motorPower=param:motorPower
            component.contactor.property.controlVoltage=param:controlVoltage
            component.contactor.presentation.symbol.type=preferred-symbol-family
            component.contactor.presentation.symbol.attribute.family=iec-contactor
            component.overload.id=template:starter.overload
            component.overload.conceptId=electrical.overload-relay
            component.overload.implementationId=siemens.3ru2116
            component.overload.displayName=Overload relay
            component.overload.summary=Thermal overload protection
            component.overload.tags=starter,protection
            component.overload.property.tag=symbol:OL-${'$'}{tagPrefix}
            component.overload.property.motorPower=param:motorPower
            connection.power.id=template:starter.power-link
            connection.power.from.componentTemplateId=template:starter.contactor
            connection.power.from.portRoleId=T1
            connection.power.to.componentTemplateId=template:starter.overload
            connection.power.to.portRoleId=L1
            connection.power.displayName=Motor power feed
            connection.power.summary=Feeds overload from contactor output
            connection.power.presentation.line.type=line-style
            connection.power.presentation.line.attribute.style=solid
            presentation.surface.type=preview-surface
            presentation.surface.attribute.panel=reuse-catalog
            documentation.review.type=review-note
            documentation.review.attribute.audience=engineer
        """.trimIndent()
    }
}
