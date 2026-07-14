package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.compiler.AthenaCompiler
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.eclipse.lsp4j.InitializeParams

class AthenaReuseRequestTest {
    @Test
    @Suppress("DEPRECATION")
    fun `semantic macro runtime seams are exposed through lsp with typed payloads`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-reuse-runtime-",
            sourceFileName = "reuse-runtime.athena",
            sourceText = "system ReuseRuntime { }",
        )
        val repositoryRoot = repository.repositoryRoot
        try {
            AthenaCompiler().materializeRepositoryLock(repositoryRoot)

            val server = AthenaLanguageServer()
            try {
                server.initialize(
                    InitializeParams().apply {
                        rootUri = repositoryRoot.toUri().toString()
                    },
                ).get()

                val catalog = assertNotNull(
                    server.semanticMacroCatalog(AthenaSemanticMacroCatalogParams()).get(),
                )
                val validation = assertNotNull(
                    server.semanticMacroValidation(
                        AthenaSemanticMacroValidationParams(
                            macroId = "macro:dol-starter",
                            instantiationId = "instance:M1",
                            parameterValues = mapOf(
                                "starterTag" to AthenaSemanticMacroParameterValuePayload(
                                    kind = "text",
                                    text = "M1",
                                ),
                            ),
                        ),
                    ).get(),
                )
                val preview = assertNotNull(
                    server.semanticMacroPreview(
                        AthenaSemanticMacroPreviewParams(
                            macroId = "macro:dol-starter",
                            instantiationId = "instance:M1",
                            parameterValues = mapOf(
                                "starterTag" to AthenaSemanticMacroParameterValuePayload(
                                    kind = "text",
                                    text = "M1",
                                ),
                            ),
                        ),
                    ).get(),
                )
                val acceptance = assertNotNull(
                    server.semanticMacroAccept(
                        AthenaSemanticMacroAcceptanceParams(
                            previewId = "preview:dol-starter:M1",
                            macroId = "macro:dol-starter",
                            instantiationId = "instance:M1",
                        ),
                    ).get(),
                )
                val origin = assertNotNull(
                    server.semanticMacroOriginInspection(
                        AthenaSemanticMacroOriginInspectionParams(
                            subjectId = "component:M1",
                        ),
                    ).get(),
                )

                assertEquals("ready", catalog.status)
                assertEquals("factory-line", catalog.projectName)
                assertEquals("frontend -> LSP -> runtime/compiler", catalog.semanticPath)
                assertTrue(catalog.entries.isEmpty())
                assertTrue(catalog.diagnostics.isEmpty())
                assertEquals(null, catalog.reason)
                assertEquals("invalid", validation.status)
                assertTrue(validation.diagnostics.any { diagnostic ->
                    diagnostic.code == "semantic.macro.validation.macro.unresolved"
                })
                assertEquals("macro:dol-starter", validation.macroId)
                assertEquals("instance:M1", preview.instantiationId)
                assertEquals("preview:dol-starter:M1", acceptance.previewId)
                assertEquals("component:M1", origin.subjectId)
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `semantic macro catalog request returns governed entries from runtime owned repository context`() {
        val workspace = kotlin.io.path.createTempDirectory("athena-lsp-reuse-catalog-")
        val repositoryRoot = workspace.resolve("current")
        val alphaRoot = workspace.resolve("alpha-catalog")
        try {
            repositoryRoot.createDirectories()
            repositoryRoot.resolve("athena.yaml").writeText(
                """
                    primaryPackage:
                      name: com.engineeringood.root
                      version: 0.1.0
                      sourceRoot: src
                    dependencies:
                      - name: com.engineeringood.alpha
                        source: local-path
                        locator: ../alpha-catalog
                """.trimIndent(),
            )
            repositoryRoot.resolve("athena.lock").writeText("# lock")
            repositoryRoot.resolve("src").createDirectories().resolve("root.athena").writeText("system Root { }")

            alphaRoot.createDirectories()
            alphaRoot.resolve("athena.yaml").writeText(
                """
                    primaryPackage:
                      name: com.engineeringood.alpha
                      version: 0.1.0
                      sourceRoot: src
                """.trimIndent(),
            )
            alphaRoot.resolve("athena.lock").writeText("# lock")
            alphaRoot.resolve("src").createDirectories().resolve("alpha.athena").writeText("system Alpha { }")

            writeSemanticMacroManifest(
                packageRoot = repositoryRoot,
                body = """
                    package.format.version=1
                    macro.root.id=macro:root-starter
                    macro.root.displayName=Root Starter
                    macro.root.summary=Root governed starter
                    macro.root.definitionPath=macros/root-starter.macro
                """.trimIndent(),
                definitionPath = "macros/root-starter.macro",
            )
            writeSemanticMacroManifest(
                packageRoot = alphaRoot,
                body = """
                    package.format.version=1
                    macro.alpha.id=macro:alpha-starter
                    macro.alpha.displayName=Alpha Starter
                    macro.alpha.summary=Alpha governed starter
                    macro.alpha.definitionPath=macros/alpha-starter.macro
                """.trimIndent(),
                definitionPath = "macros/alpha-starter.macro",
            )

            AthenaCompiler().materializeRepositoryLock(repositoryRoot)

            val server = AthenaLanguageServer()
            try {
                server.initialize(
                    InitializeParams().apply {
                        rootUri = repositoryRoot.toUri().toString()
                    },
                ).get()

                val first = assertNotNull(
                    server.semanticMacroCatalog(AthenaSemanticMacroCatalogParams()).get(),
                )
                val second = assertNotNull(
                    server.semanticMacroCatalog(AthenaSemanticMacroCatalogParams()).get(),
                )

                assertEquals("ready", first.status)
                assertEquals(first, second)
                assertEquals(
                    listOf("macro:alpha-starter", "macro:root-starter"),
                    first.entries.map { entry -> entry.macroId },
                )
                assertTrue(first.diagnostics.isEmpty())
            } finally {
                server.shutdown().get()
            }
        } finally {
            workspace.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `semantic macro validation request returns schema defaults and invalid diagnostics through lsp`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-reuse-validation-",
            packageName = "com.engineeringood.root",
            sourceFileName = "root.athena",
            sourceText = "system Root { }",
        )
        val repositoryRoot = repository.repositoryRoot
        try {
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
            AthenaCompiler().materializeRepositoryLock(repositoryRoot)

            val server = AthenaLanguageServer()
            try {
                server.initialize(
                    InitializeParams().apply {
                        rootUri = repositoryRoot.toUri().toString()
                    },
                ).get()

                val valid = assertNotNull(
                    server.semanticMacroValidation(
                        AthenaSemanticMacroValidationParams(
                            macroId = "macro:dol-starter",
                            instantiationId = "instance:M1",
                            parameterValues = mapOf(
                                "motorPower" to AthenaSemanticMacroParameterValuePayload(
                                    kind = "symbol",
                                    text = "7.5kW",
                                ),
                                "tagPrefix" to AthenaSemanticMacroParameterValuePayload(
                                    kind = "symbol",
                                    text = "M1",
                                ),
                            ),
                        ),
                    ).get(),
                )
                val invalid = assertNotNull(
                    server.semanticMacroValidation(
                        AthenaSemanticMacroValidationParams(
                            macroId = "macro:dol-starter",
                            instantiationId = "instance:M1",
                            parameterValues = mapOf(
                                "motorPower" to AthenaSemanticMacroParameterValuePayload(
                                    kind = "symbol",
                                    text = "3kW",
                                ),
                                "tagPrefix" to AthenaSemanticMacroParameterValuePayload(
                                    kind = "symbol",
                                    text = "m1",
                                ),
                            ),
                        ),
                    ).get(),
                )

                assertEquals("valid", valid.status)
                assertEquals(
                    listOf("controlVoltage", "motorPower", "tagPrefix", "vendorFamily"),
                    valid.parameters.map { parameter -> parameter.name },
                )
                assertEquals("24VDC", valid.normalizedValues["controlVoltage"]?.text)
                assertEquals("Siemens", valid.normalizedValues["vendorFamily"]?.text)
                assertTrue(valid.diagnostics.isEmpty())

                assertEquals("invalid", invalid.status)
                assertEquals(
                    setOf(
                        "semantic.macro.validation.parameter.allowed-values",
                        "semantic.macro.validation.parameter.pattern",
                    ),
                    invalid.diagnostics.map { diagnostic -> diagnostic.code }.toSet(),
                )
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `semantic macro preview request returns deterministic semantic consequences through lsp`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-reuse-preview-",
            packageName = "com.engineeringood.root",
            sourceFileName = "root.athena",
            sourceText = "system Root { }",
        )
        val repositoryRoot = repository.repositoryRoot
        try {
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
            AthenaCompiler().materializeRepositoryLock(repositoryRoot)

            val server = AthenaLanguageServer()
            try {
                server.initialize(
                    InitializeParams().apply {
                        rootUri = repositoryRoot.toUri().toString()
                    },
                ).get()

                val first = assertNotNull(
                    server.semanticMacroPreview(
                        AthenaSemanticMacroPreviewParams(
                            macroId = "macro:dol-starter",
                            instantiationId = "instance:M1",
                            parameterValues = mapOf(
                                "motorPower" to AthenaSemanticMacroParameterValuePayload(
                                    kind = "symbol",
                                    text = "7.5kW",
                                ),
                                "tagPrefix" to AthenaSemanticMacroParameterValuePayload(
                                    kind = "symbol",
                                    text = "M1",
                                ),
                            ),
                        ),
                    ).get(),
                )
                val second = assertNotNull(
                    server.semanticMacroPreview(
                        AthenaSemanticMacroPreviewParams(
                            macroId = "macro:dol-starter",
                            instantiationId = "instance:M1",
                            parameterValues = mapOf(
                                "motorPower" to AthenaSemanticMacroParameterValuePayload(
                                    kind = "symbol",
                                    text = "7.5kW",
                                ),
                                "tagPrefix" to AthenaSemanticMacroParameterValuePayload(
                                    kind = "symbol",
                                    text = "M1",
                                ),
                            ),
                        ),
                    ).get(),
                )

                assertEquals(first, second)
                assertEquals("ready", first.status)
                assertEquals("preview:dol-starter:M1", first.previewId)
                assertEquals(
                    listOf("template:starter.contactor", "template:starter.overload"),
                    first.components.map { component -> component.templateId },
                )
                assertEquals(
                    listOf("template:starter.contactor:T1", "template:starter.overload:L1"),
                    first.ports.map { port -> "${port.componentTemplateId}:${port.portRoleId}" },
                )
                assertEquals(
                    listOf("template:starter.power-link"),
                    first.connections.map { connection -> connection.templateId },
                )
                assertEquals("M1", first.components.first().properties["tag"]?.text)
                assertTrue(first.originAnchors.any { anchor ->
                    anchor.subjectKind == "component" && anchor.derivedSubjectId == "component:instance:M1:template:starter.contactor"
                })
                assertTrue(first.presentationConsequences.any { consequence -> consequence.hintType == "preferred-symbol-family" })
                assertTrue(first.warnings.isEmpty())
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `semantic macro acceptance request returns one governed mutation bundle through lsp`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-reuse-acceptance-",
            packageName = "com.engineeringood.root",
            sourceFileName = "root.athena",
            sourceText = "system Root { }",
        )
        val repositoryRoot = repository.repositoryRoot
        try {
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
            AthenaCompiler().materializeRepositoryLock(repositoryRoot)

            val server = AthenaLanguageServer()
            try {
                server.initialize(
                    InitializeParams().apply {
                        rootUri = repositoryRoot.toUri().toString()
                    },
                ).get()

                val preview = assertNotNull(
                    server.semanticMacroPreview(
                        AthenaSemanticMacroPreviewParams(
                            macroId = "macro:dol-starter",
                            instantiationId = "instance:M1",
                            parameterValues = mapOf(
                                "motorPower" to AthenaSemanticMacroParameterValuePayload(
                                    kind = "symbol",
                                    text = "7.5kW",
                                ),
                                "tagPrefix" to AthenaSemanticMacroParameterValuePayload(
                                    kind = "symbol",
                                    text = "M1",
                                ),
                            ),
                        ),
                    ).get(),
                )
                val acceptance = assertNotNull(
                    server.semanticMacroAccept(
                        AthenaSemanticMacroAcceptanceParams(
                            previewId = preview.previewId ?: error("previewId missing"),
                            macroId = "macro:dol-starter",
                            instantiationId = "instance:M1",
                        ),
                    ).get(),
                )

                assertEquals("accepted", acceptance.status)
                assertEquals("bundle:dol-starter:M1", acceptance.bundleId)
                assertEquals("expansion:dol-starter:M1", acceptance.acceptedExpansion?.expansionId)
                assertEquals("command-0001", acceptance.execution?.commandId)
                assertEquals(6, acceptance.operations.size)
                assertEquals(5, acceptance.affectedSemanticIds.size)
                assertEquals(5, acceptance.execution?.changedSemanticIds?.size)
                assertEquals(
                    listOf("controlVoltage", "motorPower", "tagPrefix", "vendorFamily"),
                    acceptance.acceptedExpansion?.parameterValues?.keys?.toList(),
                )
                assertTrue(acceptance.inspection?.affectedSemanticIds?.size == 5)
                assertTrue(acceptance.semanticReview?.authoredChangeCount ?: 0 > 0)
                assertTrue(acceptance.reason?.contains("sole M8 mutation authority") == true)
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `semantic macro origin inspection request returns accepted expansion traceability through lsp`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-reuse-origin-",
            packageName = "com.engineeringood.root",
            sourceFileName = "root.athena",
            sourceText = "system Root { }",
        )
        val repositoryRoot = repository.repositoryRoot
        try {
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
            AthenaCompiler().materializeRepositoryLock(repositoryRoot)

            val server = AthenaLanguageServer()
            try {
                server.initialize(
                    InitializeParams().apply {
                        rootUri = repositoryRoot.toUri().toString()
                    },
                ).get()

                val preview = assertNotNull(
                    server.semanticMacroPreview(
                        AthenaSemanticMacroPreviewParams(
                            macroId = "macro:dol-starter",
                            instantiationId = "instance:M1",
                            parameterValues = mapOf(
                                "motorPower" to AthenaSemanticMacroParameterValuePayload(
                                    kind = "symbol",
                                    text = "7.5kW",
                                ),
                                "tagPrefix" to AthenaSemanticMacroParameterValuePayload(
                                    kind = "symbol",
                                    text = "M1",
                                ),
                            ),
                        ),
                    ).get(),
                )
                assertNotNull(
                    server.semanticMacroAccept(
                        AthenaSemanticMacroAcceptanceParams(
                            previewId = preview.previewId ?: error("previewId missing"),
                            macroId = "macro:dol-starter",
                            instantiationId = "instance:M1",
                        ),
                    ).get(),
                )

                val bySubject = assertNotNull(
                    server.semanticMacroOriginInspection(
                        AthenaSemanticMacroOriginInspectionParams(
                            subjectId = "component:instance:M1:template:starter.contactor",
                        ),
                    ).get(),
                )
                val byInstantiation = assertNotNull(
                    server.semanticMacroOriginInspection(
                        AthenaSemanticMacroOriginInspectionParams(
                            instantiationId = "instance:M1",
                        ),
                    ).get(),
                )

                assertEquals("ready", bySubject.status)
                assertEquals("component:instance:M1:template:starter.contactor", bySubject.subjectId)
                assertEquals("instance:M1", bySubject.instantiationId)
                assertEquals("command-0001", bySubject.commandId)
                assertEquals("bundle:dol-starter:M1", bySubject.bundleId)
                assertEquals("expansion:dol-starter:M1", bySubject.acceptedExpansion?.expansionId)
                assertEquals("macro:dol-starter", bySubject.acceptedExpansion?.macroId)
                assertEquals("com.engineeringood.root", bySubject.acceptedExpansion?.packageName)
                assertEquals("0.1.0", bySubject.acceptedExpansion?.packageVersion)
                assertEquals("7.5kW", bySubject.acceptedExpansion?.parameterValues?.get("motorPower")?.text)
                assertEquals(5, bySubject.acceptedExpansion?.memberships?.size)
                assertEquals("component:template:starter.contactor", bySubject.matchedMembership?.role)
                assertEquals("expansion:dol-starter:M1", byInstantiation.acceptedExpansion?.expansionId)
                assertEquals("instance:M1", byInstantiation.instantiationId)
                assertEquals(null, byInstantiation.subjectId)
                assertEquals(null, byInstantiation.matchedMembership?.role)
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }
}

private fun writeSemanticMacroManifest(
    packageRoot: java.nio.file.Path,
    body: String,
    definitionPath: String,
    definitionContent: String = "# semantic macro definition placeholder",
) {
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
