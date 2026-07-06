package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.boundary.AthenaBoundaryDescriptorSource
import com.engineeringood.athena.ir.EngineeringComponent
import com.engineeringood.athena.ir.EngineeringConnection
import com.engineeringood.athena.ir.EngineeringIrDocument
import com.engineeringood.athena.ir.EngineeringPort
import com.engineeringood.athena.ir.EngineeringProperty
import com.engineeringood.athena.ir.EngineeringPropertyValue
import com.engineeringood.athena.ir.EngineeringReference
import com.engineeringood.athena.ir.EngineeringSystem
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.renderer.svg.SvgRenderBox
import com.engineeringood.athena.renderer.svg.SvgRenderConnection
import com.engineeringood.athena.renderer.svg.SvgRenderModel
import com.engineeringood.athena.compiler.knowledge.AthenaKnowledgePackageSource
import com.engineeringood.athena.compiler.plugin.AthenaCoreRuntime
import com.engineeringood.athena.compiler.plugin.AthenaExtensionPoint
import com.engineeringood.athena.compiler.plugin.AthenaPluginDiscovery
import com.engineeringood.athena.semantics.core.SemanticContinuationDecision
import com.engineeringood.athena.semantics.core.SemanticDiagnosticCategory
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AthenaCompilerTest {
    @Test
    fun `compile reports declared pass execution for valid input`() {
        val examplePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")

        val result = AthenaCompiler().compile(examplePath)

        val success = assertIs<CompilerCompilationSuccess>(result)
        assertEquals(
            listOf(
                CompilerPassDescriptor(
                    id = CompilerPassId.PARSE,
                    responsibility = "Parse authored source into syntax-owned AST",
                    inputState = "authored source file",
                    outputState = "syntax-owned source document",
                ),
                CompilerPassDescriptor(
                    id = CompilerPassId.LOWER,
                    responsibility = "Lower syntax-owned source into canonical Engineering IR",
                    inputState = "syntax-owned source document",
                    outputState = "canonical Engineering IR",
                ),
                CompilerPassDescriptor(
                    id = CompilerPassId.VALIDATE,
                    responsibility = "Validate canonical Engineering IR and compute continuation policy",
                    inputState = "canonical Engineering IR",
                    outputState = "semantic validation result",
                ),
                CompilerPassDescriptor(
                    id = CompilerPassId.DOWNSTREAM_DERIVATION,
                    responsibility = "Derive the thin render-facing model and emit simple SVG when policy allows",
                    inputState = "semantic validation result",
                    outputState = "render result",
                ),
            ),
            success.pipeline.passes.map { it.pass },
        )
        assertEquals(
            listOf(
                CompilerPassExecutionStatus.SUCCEEDED,
                CompilerPassExecutionStatus.SUCCEEDED,
                CompilerPassExecutionStatus.SUCCEEDED,
                CompilerPassExecutionStatus.SUCCEEDED,
            ),
            success.pipeline.passes.map { it.status },
        )
        assertEquals(
            listOf(
                "system:DemoCabinet",
                "system:DemoCabinet",
                "semantic-valid",
                "svg-emitted",
            ),
            success.pipeline.passes.map { it.outputSummary },
        )
        assertEquals(
            CompilerRenderingSuccess(
                model = SvgRenderModel(
                    systemName = "DemoCabinet",
                    canvasWidth = 480,
                    canvasHeight = 172,
                    boxes = listOf(
                        SvgRenderBox(
                            semanticId = StableSemanticIdentity("component:PLC1"),
                            label = "PLC1",
                            x = 40,
                            y = 60,
                            width = 140,
                            height = 72,
                        ),
                        SvgRenderBox(
                            semanticId = StableSemanticIdentity("component:M1"),
                            label = "M1",
                            x = 300,
                            y = 60,
                            width = 140,
                            height = 72,
                        ),
                    ),
                    connections = listOf(
                        SvgRenderConnection(
                            semanticId = StableSemanticIdentity("connection:PLC1.out->M1.in"),
                            x1 = 180,
                            y1 = 96,
                            x2 = 300,
                            y2 = 96,
                        ),
                    ),
                ),
                svg = expectedDemoSvg(),
            ),
            success.rendering,
        )
    }

    @Test
    fun `compile preserves declared pass ordering when approved plugins are attached`() {
        val examplePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val compiler = AthenaCompiler(
            pluginDiscovery = AthenaPluginDiscovery(
                runtime = AthenaCoreRuntime(version = "0.0.1-SNAPSHOT"),
                source = FixedAthenaPluginSource(
                    listOf(
                        ZetaDomainTestPlugin(),
                        AlphaDomainTestPlugin(),
                    ),
                ),
            ),
        )

        val result = assertIs<CompilerCompilationSuccess>(compiler.compile(examplePath))

        assertEquals(
            listOf(
                CompilerPassId.PARSE,
                CompilerPassId.LOWER,
                CompilerPassId.VALIDATE,
                CompilerPassId.DOWNSTREAM_DERIVATION,
            ),
            result.pipeline.passes.map { it.pass.id },
        )
        assertEquals(
            listOf(
                "com.engineeringood.athena.domain.alpha",
                "com.engineeringood.athena.domain.zeta",
            ),
            compiler.pluginInventory.attachedPlugins(AthenaExtensionPoint.DOMAIN_SEMANTICS)
                .map { it.candidate.manifest.pluginId },
        )
    }

    @Test
    fun `compile can reuse a runtime hosted plugin discovery report without rediscovering plugins`() {
        val examplePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val hostedReport = AthenaPluginDiscovery(
            runtime = AthenaCoreRuntime(version = "0.0.1-SNAPSHOT"),
            source = FixedAthenaPluginSource(
                listOf(
                    ZetaDomainTestPlugin(),
                    AlphaDomainTestPlugin(),
                ),
            ),
        ).discover()
        val compiler = AthenaCompiler(
            pluginDiscovery = AthenaPluginDiscovery(
                runtime = AthenaCoreRuntime(version = "0.0.1-SNAPSHOT"),
                source = ThrowingAthenaPluginSource("compiler should not rediscover plugins when a hosted report is provided"),
            ),
            hostedPluginDiscoveryReport = hostedReport,
        )

        val result = assertIs<CompilerCompilationSuccess>(compiler.compile(examplePath))

        assertEquals(
            listOf(
                "com.engineeringood.athena.domain.alpha",
                "com.engineeringood.athena.domain.zeta",
            ),
            compiler.pluginInventory.approvedPlugins.map { plugin -> plugin.candidate.manifest.pluginId },
        )
        assertEquals(
            listOf(
                CompilerPassId.PARSE,
                CompilerPassId.LOWER,
                CompilerPassId.VALIDATE,
                CompilerPassId.DOWNSTREAM_DERIVATION,
            ),
            result.pipeline.passes.map { it.pass.id },
        )
    }

    @Test
    fun `lower can consume runtime hosted domain plugins through the explicit hosted domain contract`() {
        val examplePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val compiler = AthenaCompiler(
            pluginDiscovery = AthenaPluginDiscovery(
                runtime = AthenaCoreRuntime(version = "0.0.1-SNAPSHOT"),
                source = ThrowingAthenaPluginSource("compiler should use hosted domain plugins from runtime services"),
            ),
            hostedPluginDiscoveryReport = AthenaPluginDiscovery(
                runtime = AthenaCoreRuntime(version = "0.0.1-SNAPSHOT"),
                source = FixedAthenaPluginSource(emptyList()),
            ).discover(),
            hostedDomainPlugins = listOf(
                AlphaSemanticsTestPlugin(),
                ZetaSemanticsTestPlugin(),
            ),
        )

        val result = assertIs<CompilerLoweringSuccess>(compiler.lower(examplePath))

        assertTrue(compiler.pluginInventory.approvedPlugins.isEmpty())
        assertEquals(
            listOf("AlphaDevice", "ZetaDevice"),
            result.document.components.map { component -> component.name },
        )
    }

    @Test
    fun `compile exposes inspectable governed knowledge context without changing pass order`() {
        val repoRoot = resolveRepoRoot()
        val examplePath = repoRoot.resolve("examples/m0/demo-cabinet.athena")
        val compiler = AthenaCompiler(
            knowledgePackageSource = AthenaKnowledgePackageSource(
                packageRoots = listOf(
                    repoRoot.resolve("kernel/compiler/src/test/resources/knowledge-packages/valid-rule"),
                    repoRoot.resolve("kernel/compiler/src/test/resources/knowledge-packages/incompatible-core"),
                    repoRoot.resolve("kernel/compiler/src/test/resources/knowledge-packages/valid-ontology"),
                ),
            ),
        )

        val result = assertIs<CompilerCompilationSuccess>(compiler.compile(examplePath))

        assertEquals(
            listOf(
                CompilerPassId.PARSE,
                CompilerPassId.LOWER,
                CompilerPassId.VALIDATE,
                CompilerPassId.DOWNSTREAM_DERIVATION,
            ),
            result.pipeline.passes.map { it.pass.id },
        )
        assertEquals(
            listOf(
                "com.engineeringood.athena.knowledge.ontology.core",
                "com.engineeringood.athena.knowledge.rule.connection-safety",
            ),
            result.knowledgeContext.activeArtifacts.map { it.artifactId },
        )
        assertEquals(
            listOf("0.1.0", "0.1.0"),
            result.knowledgeContext.activeArtifacts.map { it.artifactVersion },
        )
        assertEquals(
            listOf(
                "Athena Architecture Council",
                "Athena Rule Maintainers",
            ),
            result.knowledgeContext.activeArtifacts.map { it.provenance.reviewedBy },
        )
        assertEquals(
            listOf("com.engineeringood.athena.knowledge.rule.future-only"),
            result.knowledgeContext.rejectedPackages.mapNotNull { it.artifactId },
        )
        assertEquals(
            listOf("knowledge.package.compatibility.unsupported-core"),
            result.knowledgeContext.rejectedPackages.flatMap { rejected -> rejected.diagnostics.map { it.ruleId.value } },
        )
        assertEquals(
            listOf(
                CompilerKnowledgeAttributionTarget.KNOWLEDGE_CONTEXT,
                CompilerKnowledgeAttributionTarget.SEMANTIC_RESULT,
                CompilerKnowledgeAttributionTarget.RENDERING,
            ),
            result.knowledgeAttributions.map { it.target },
        )
        assertEquals(
            listOf(
                "com.engineeringood.athena.knowledge.ontology.core",
                "com.engineeringood.athena.knowledge.rule.connection-safety",
            ),
            result.knowledgeAttributions.first { it.target == CompilerKnowledgeAttributionTarget.KNOWLEDGE_CONTEXT }
                .responsibleArtifacts.map { it.artifactId },
        )
        assertEquals(
            emptyList(),
            result.knowledgeAttributions.first { it.target == CompilerKnowledgeAttributionTarget.SEMANTIC_RESULT }
                .responsibleArtifacts,
        )
        assertEquals(
            emptyList(),
            result.knowledgeAttributions.first { it.target == CompilerKnowledgeAttributionTarget.RENDERING }
                .responsibleArtifacts,
        )
    }

    @Test
    fun `compile exposes inspectable external boundary validation without changing pass order or outputs`() {
        val repoRoot = resolveRepoRoot()
        val examplePath = repoRoot.resolve("examples/m0/demo-cabinet.athena")
        val baselineCompiler = AthenaCompiler()
        val compiler = AthenaCompiler(
            boundaryDescriptorSource = AthenaBoundaryDescriptorSource(
                descriptorRoots = listOf(
                    repoRoot.resolve("kernel/compiler/src/test/resources/boundary-descriptors/opc-ua-runtime"),
                    repoRoot.resolve("kernel/compiler/src/test/resources/boundary-descriptors/automationml-reference"),
                ),
            ),
        )

        val baseline = assertIs<CompilerCompilationSuccess>(baselineCompiler.compile(examplePath))
        val result = assertIs<CompilerCompilationSuccess>(compiler.compile(examplePath))

        assertEquals(
            listOf(
                CompilerPassId.PARSE,
                CompilerPassId.LOWER,
                CompilerPassId.VALIDATE,
                CompilerPassId.DOWNSTREAM_DERIVATION,
            ),
            result.pipeline.passes.map { it.pass.id },
        )
        assertEquals(
            listOf("automationml.reference", "opcua.runtime.bridge"),
            result.boundaryValidation.validDescriptors.map { it.descriptorId },
        )
        assertTrue(result.boundaryValidation.rejectedDescriptors.isEmpty())
        assertEquals(baseline.semanticResult, result.semanticResult)
        assertEquals(baseline.rendering, result.rendering)
        assertEquals(baseline.pipeline, result.pipeline)
    }

    @Test
    fun `invalid external boundary descriptors remain inspectable metadata and do not change compiler outputs`() {
        val repoRoot = resolveRepoRoot()
        val examplePath = repoRoot.resolve("examples/m0/demo-cabinet.athena")
        val baselineCompiler = AthenaCompiler()
        val compiler = AthenaCompiler(
            boundaryDescriptorSource = AthenaBoundaryDescriptorSource(
                descriptorRoots = listOf(
                    repoRoot.resolve("kernel/compiler/src/test/resources/boundary-descriptors/external-authority"),
                    repoRoot.resolve("kernel/compiler/src/test/resources/boundary-descriptors/invalid-assumption"),
                    repoRoot.resolve("kernel/compiler/src/test/resources/boundary-descriptors/operational-execution"),
                    repoRoot.resolve("kernel/compiler/src/test/resources/boundary-descriptors/automationml-outbound"),
                ),
            ),
        )

        val baseline = assertIs<CompilerCompilationSuccess>(baselineCompiler.compile(examplePath))
        val result = assertIs<CompilerCompilationSuccess>(compiler.compile(examplePath))

        assertEquals(
            listOf(
                "automationml.outbound",
                "external.authority.claim",
                "operational.connector.claim",
            ),
            result.boundaryValidation.rejectedDescriptors.mapNotNull { it.descriptorId },
        )
        assertEquals(
            listOf(
                "boundary.descriptor.standards.direction.unsupported",
                "boundary.descriptor.authority.external-canonical-forbidden",
                "boundary.descriptor.mode.operational-not-supported",
                "boundary.descriptor.compatibility.assumptions.invalid",
            ),
            result.boundaryValidation.rejectedDescriptors.flatMap { rejected -> rejected.diagnostics.map { it.ruleId.value } },
        )
        assertEquals(baseline.semanticResult, result.semanticResult)
        assertEquals(baseline.rendering, result.rendering)
        assertEquals(baseline.pipeline, result.pipeline)
    }

    @Test
    fun `incompatible governed knowledge packages do not change compiler behavior`() {
        val repoRoot = resolveRepoRoot()
        val examplePath = repoRoot.resolve("examples/m0/demo-cabinet.athena")
        val compatibleCompiler = AthenaCompiler(
            knowledgePackageSource = AthenaKnowledgePackageSource(
                packageRoots = listOf(
                    repoRoot.resolve("kernel/compiler/src/test/resources/knowledge-packages/valid-rule"),
                    repoRoot.resolve("kernel/compiler/src/test/resources/knowledge-packages/valid-ontology"),
                ),
            ),
        )
        val mixedCompiler = AthenaCompiler(
            knowledgePackageSource = AthenaKnowledgePackageSource(
                packageRoots = listOf(
                    repoRoot.resolve("kernel/compiler/src/test/resources/knowledge-packages/incompatible-core"),
                    repoRoot.resolve("kernel/compiler/src/test/resources/knowledge-packages/valid-rule"),
                    repoRoot.resolve("kernel/compiler/src/test/resources/knowledge-packages/valid-ontology"),
                ),
            ),
        )

        val compatibleResult = assertIs<CompilerCompilationSuccess>(compatibleCompiler.compile(examplePath))
        val mixedResult = assertIs<CompilerCompilationSuccess>(mixedCompiler.compile(examplePath))

        assertEquals(compatibleResult.semanticResult, mixedResult.semanticResult)
        assertEquals(compatibleResult.rendering, mixedResult.rendering)
        assertEquals(compatibleResult.pipeline, mixedResult.pipeline)
        assertEquals(compatibleResult.knowledgeContext.activeArtifacts, mixedResult.knowledgeContext.activeArtifacts)
        assertEquals(1, mixedResult.knowledgeContext.rejectedPackages.size)
    }

    @Test
    fun `compile without an approved domain plugin disables electrical runtime semantics but preserves pass ordering`() {
        val examplePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val compiler = AthenaCompiler(
            pluginDiscovery = AthenaPluginDiscovery(
                runtime = AthenaCoreRuntime(version = "0.0.1-SNAPSHOT"),
                source = FixedAthenaPluginSource(emptyList()),
            ),
        )

        val result = assertIs<CompilerCompilationSuccess>(compiler.compile(examplePath))

        assertEquals(
            listOf(
                CompilerPassId.PARSE,
                CompilerPassId.LOWER,
                CompilerPassId.VALIDATE,
                CompilerPassId.DOWNSTREAM_DERIVATION,
            ),
            result.pipeline.passes.map { it.pass.id },
        )
        assertEquals(
            listOf(
                CompilerPassExecutionStatus.SUCCEEDED,
                CompilerPassExecutionStatus.SUCCEEDED,
                CompilerPassExecutionStatus.SUCCEEDED,
                CompilerPassExecutionStatus.FAILED,
            ),
            result.pipeline.passes.map { it.status },
        )
        assertEquals(
            listOf("domain.semantics.unavailable"),
            result.semanticResult.diagnostics.map { it.ruleId.value },
        )
        assertEquals(
            listOf(SemanticDiagnosticCategory.DOMAIN),
            result.semanticResult.diagnostics.map { it.category },
        )
        assertEquals(emptyList(), result.document.components)
        assertEquals(emptyList(), result.document.ports)
        assertEquals(emptyList(), result.document.connections)
        assertEquals(
            CompilerRenderingBlocked(
                reason = "semantic validation requested STOP_DOWNSTREAM",
                blockedByPass = CompilerPassId.VALIDATE,
            ),
            result.rendering,
        )
    }

    @Test
    fun `lower without an approved domain plugin returns inspectable semantic failure`() {
        val examplePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val compiler = AthenaCompiler(
            pluginDiscovery = AthenaPluginDiscovery(
                runtime = AthenaCoreRuntime(version = "0.0.1-SNAPSHOT"),
                source = FixedAthenaPluginSource(emptyList()),
            ),
        )

        val result = compiler.lower(examplePath)

        val failure = assertIs<CompilerLoweringSemanticFailure>(result)
        assertEquals(
            listOf("domain.semantics.unavailable"),
            failure.diagnostics.map { it.ruleId.value },
        )
        assertEquals(emptyList(), failure.document.components)
        assertEquals(emptyList(), failure.document.ports)
        assertEquals(emptyList(), failure.document.connections)
    }

    @Test
    fun `compile produces identical pass reports for identical input`() {
        val examplePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val compiler = AthenaCompiler()

        val first = assertIs<CompilerCompilationSuccess>(compiler.compile(examplePath))
        val second = assertIs<CompilerCompilationSuccess>(compiler.compile(examplePath))

        assertEquals(first.pipeline, second.pipeline)
    }

    @Test
    fun `compile skips downstream derivation when semantic continuation stops`() {
        val examplePath = resolveRepoRoot().resolve("examples/m0/invalid-semantic-cabinet.athena")

        val result = AthenaCompiler().compile(examplePath)

        val success = assertIs<CompilerCompilationSuccess>(result)
        assertEquals(
            listOf(
                CompilerPassExecutionStatus.SUCCEEDED,
                CompilerPassExecutionStatus.SUCCEEDED,
                CompilerPassExecutionStatus.SUCCEEDED,
                CompilerPassExecutionStatus.FAILED,
            ),
            success.pipeline.passes.map { it.status },
        )
        assertEquals(
            "render-blocked",
            success.pipeline.passes.last().outputSummary,
        )
        assertEquals(
            CompilerRenderingBlocked(
                reason = "semantic validation requested STOP_DOWNSTREAM",
                blockedByPass = CompilerPassId.VALIDATE,
            ),
            success.rendering,
        )
    }

    @Test
    fun `compile stops later passes after syntax failure`() {
        val malformedSource = """
            system Broken {
              device PLC1 {
                type PLC
              }
        """.trimIndent()
        val malformedPath = Files.createTempFile("athena-malformed-", ".athena")
        Files.writeString(malformedPath, malformedSource)

        try {
            val result = AthenaCompiler().compile(malformedPath)

            val failure = assertIs<CompilerCompilationParseFailure>(result)
            assertEquals(
                listOf(
                    CompilerPassExecutionStatus.FAILED,
                    CompilerPassExecutionStatus.SKIPPED,
                    CompilerPassExecutionStatus.SKIPPED,
                    CompilerPassExecutionStatus.SKIPPED,
                ),
                failure.pipeline.passes.map { it.status },
            )
            assertContains(failure.pipeline.passes.first().outputSummary, "syntax diagnostics")
        } finally {
            Files.deleteIfExists(malformedPath)
        }
    }

    @Test
    fun `compile returns semantically valid result for the demo cabinet example`() {
        val examplePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")

        val result = AthenaCompiler().compile(examplePath)

        val success = assertIs<CompilerCompilationSuccess>(result)
        assertTrue(success.semanticResult.isSemanticallyValid)
        assertEquals(emptyList(), success.semanticResult.diagnostics)
        assertEquals(SemanticContinuationDecision.CONTINUE, success.semanticResult.continuationDecision)
        assertEquals("DemoCabinet", success.source.ast.system.name)
        assertEquals("system:DemoCabinet", success.document.system.id.value)
        assertEquals(expectedDemoSvg(), assertIs<CompilerRenderingSuccess>(success.rendering).svg)
    }

    @Test
    fun `compile exposes semantic diagnostics and blocks downstream continuation for invalid examples`() {
        val examplePath = resolveRepoRoot().resolve("examples/m0/invalid-semantic-cabinet.athena")

        val result = AthenaCompiler().compile(examplePath)

        val success = assertIs<CompilerCompilationSuccess>(result)
        assertTrue(!success.semanticResult.isSemanticallyValid)
        assertEquals(SemanticContinuationDecision.STOP_DOWNSTREAM, success.semanticResult.continuationDecision)
        assertEquals(
            listOf(
                "reference.connection-endpoint.unresolved",
                "property.component.type.missing",
                "connection.signal.incompatible",
            ),
            success.semanticResult.diagnostics.map { it.ruleId.value },
        )
        assertEquals(
            listOf(
                SemanticDiagnosticCategory.REFERENCE,
                SemanticDiagnosticCategory.PROPERTY,
                SemanticDiagnosticCategory.CONNECTION,
            ),
            success.semanticResult.diagnostics.map { it.category },
        )
        assertEquals(
            listOf(
                StableSemanticIdentity("connection:PLC1.out->Missing.in"),
                StableSemanticIdentity("component:PLC1"),
                StableSemanticIdentity("connection:PLC1.out->M1.in"),
            ),
            success.semanticResult.diagnostics.map { it.subjectIdentity },
        )
        assertEquals(
            listOf(
                SourceProvenance(examplePath.toString(), 21, 23, 21, 33),
                SourceProvenance(examplePath.toString(), 2, 3, 4, 4),
                SourceProvenance(examplePath.toString(), 20, 3, 20, 28),
            ),
            success.semanticResult.diagnostics.map { it.provenance },
        )
    }

    @Test
    fun `compile reports duplicate connection keys as semantic uniqueness diagnostics`() {
        val duplicateSource = """
            system DuplicateConnections {
              device PLC1 {
                type PLC
              }

              port PLC1.out {
                direction out
                signal Digital
              }

              connect PLC1.out -> PLC1.out
              connect PLC1.out -> PLC1.out
            }
        """.trimIndent()
        val duplicatePath = Files.createTempFile("athena-duplicate-connections-", ".athena")
        Files.writeString(duplicatePath, duplicateSource)

        try {
            val result = AthenaCompiler().compile(duplicatePath)

            val success = assertIs<CompilerCompilationSuccess>(result)
            assertContains(success.semanticResult.diagnostics.map { it.ruleId.value }, "uniqueness.connection.duplicate-authored-key")
        } finally {
            Files.deleteIfExists(duplicatePath)
        }
    }

    @Test
    fun `compile classifies quoted type and direction values as invalid semantic properties`() {
        val quotedSource = """
            system QuotedProperties {
              device PLC1 {
                type "PLC"
              }

              port PLC1.out {
                direction "out"
                signal "Digital"
              }
            }
        """.trimIndent()
        val quotedPath = Files.createTempFile("athena-quoted-properties-", ".athena")
        Files.writeString(quotedPath, quotedSource)

        try {
            val result = AthenaCompiler().compile(quotedPath)

            val success = assertIs<CompilerCompilationSuccess>(result)
            assertEquals(
                listOf(
                    "property.component.type.invalid",
                    "property.port.direction.invalid",
                    "property.port.signal.invalid",
                ),
                success.semanticResult.diagnostics.map { it.ruleId.value },
            )
        } finally {
            Files.deleteIfExists(quotedPath)
        }
    }

    @Test
    fun `lowers the demo cabinet example into canonical engineering ir`() {
        val repoRoot = resolveRepoRoot()
        val examplePath = repoRoot.resolve("examples/m0/demo-cabinet.athena")

        val result = AthenaCompiler().lower(examplePath)

        val success = assertIs<CompilerLoweringSuccess>(result)
        assertEquals(
            EngineeringIrDocument(
                system = EngineeringSystem(
                    id = StableSemanticIdentity("system:DemoCabinet"),
                    name = "DemoCabinet",
                    provenance = SourceProvenance(
                        file = examplePath.toString(),
                        startLine = 1,
                        startColumn = 1,
                        endLine = 22,
                        endColumn = 2,
                    ),
                ),
                components = listOf(
                    EngineeringComponent(
                        id = StableSemanticIdentity("component:PLC1"),
                        name = "PLC1",
                        kind = "device",
                        properties = listOf(
                            EngineeringProperty("type", EngineeringPropertyValue.Symbol("PLC")),
                            EngineeringProperty("model", EngineeringPropertyValue.Text("S7-1200")),
                        ),
                        provenance = SourceProvenance(
                            file = examplePath.toString(),
                            startLine = 2,
                            startColumn = 3,
                            endLine = 5,
                            endColumn = 4,
                        ),
                    ),
                    EngineeringComponent(
                        id = StableSemanticIdentity("component:M1"),
                        name = "M1",
                        kind = "device",
                        properties = listOf(
                            EngineeringProperty("type", EngineeringPropertyValue.Symbol("Motor")),
                        ),
                        provenance = SourceProvenance(
                            file = examplePath.toString(),
                            startLine = 7,
                            startColumn = 3,
                            endLine = 9,
                            endColumn = 4,
                        ),
                    ),
                ),
                ports = listOf(
                    EngineeringPort(
                        id = StableSemanticIdentity("port:PLC1.out"),
                        ownerReference = EngineeringReference(
                            authoredPath = listOf("PLC1"),
                            resolvedIdentity = StableSemanticIdentity("component:PLC1"),
                            provenance = SourceProvenance(
                                file = examplePath.toString(),
                                startLine = 11,
                                startColumn = 8,
                                endLine = 11,
                                endColumn = 16,
                            ),
                        ),
                        name = "out",
                        properties = listOf(
                            EngineeringProperty("direction", EngineeringPropertyValue.Symbol("out")),
                            EngineeringProperty("signal", EngineeringPropertyValue.Symbol("Digital")),
                        ),
                        provenance = SourceProvenance(
                            file = examplePath.toString(),
                            startLine = 11,
                            startColumn = 3,
                            endLine = 14,
                            endColumn = 4,
                        ),
                    ),
                    EngineeringPort(
                        id = StableSemanticIdentity("port:M1.in"),
                        ownerReference = EngineeringReference(
                            authoredPath = listOf("M1"),
                            resolvedIdentity = StableSemanticIdentity("component:M1"),
                            provenance = SourceProvenance(
                                file = examplePath.toString(),
                                startLine = 16,
                                startColumn = 8,
                                endLine = 16,
                                endColumn = 13,
                            ),
                        ),
                        name = "in",
                        properties = listOf(
                            EngineeringProperty("direction", EngineeringPropertyValue.Symbol("in")),
                            EngineeringProperty("signal", EngineeringPropertyValue.Symbol("Digital")),
                        ),
                        provenance = SourceProvenance(
                            file = examplePath.toString(),
                            startLine = 16,
                            startColumn = 3,
                            endLine = 19,
                            endColumn = 4,
                        ),
                    ),
                ),
                connections = listOf(
                    EngineeringConnection(
                        id = StableSemanticIdentity("connection:PLC1.out->M1.in"),
                        from = EngineeringReference(
                            authoredPath = listOf("PLC1", "out"),
                            resolvedIdentity = StableSemanticIdentity("port:PLC1.out"),
                            provenance = SourceProvenance(
                                file = examplePath.toString(),
                                startLine = 21,
                                startColumn = 11,
                                endLine = 21,
                                endColumn = 19,
                            ),
                        ),
                        to = EngineeringReference(
                            authoredPath = listOf("M1", "in"),
                            resolvedIdentity = StableSemanticIdentity("port:M1.in"),
                            provenance = SourceProvenance(
                                file = examplePath.toString(),
                                startLine = 21,
                                startColumn = 23,
                                endLine = 21,
                                endColumn = 28,
                            ),
                        ),
                        provenance = SourceProvenance(
                            file = examplePath.toString(),
                            startLine = 21,
                            startColumn = 3,
                            endLine = 21,
                            endColumn = 28,
                        ),
                    ),
                ),
            ),
            success.document,
        )
    }

    @Test
    fun `matches the published engineering ir conformance artifact`() {
        val repoRoot = resolveRepoRoot()
        val examplePath = repoRoot.resolve("examples/m0/demo-cabinet.athena")
        val expectedArtifact = Files.readString(repoRoot.resolve("examples/m0/demo-cabinet.engineering-ir.txt")).trimEnd()

        val result = AthenaCompiler().lower(examplePath)

        val success = assertIs<CompilerLoweringSuccess>(result)
        assertEquals(expectedArtifact, renderConformanceArtifact(success.document, repoRoot))
    }

    @Test
    fun `matches the published svg conformance artifact`() {
        val repoRoot = resolveRepoRoot()
        val examplePath = repoRoot.resolve("examples/m0/demo-cabinet.athena")
        val expectedArtifact = Files.readString(repoRoot.resolve("examples/m0/demo-cabinet.svg")).trimEnd()

        val result = AthenaCompiler().compile(examplePath)

        val success = assertIs<CompilerCompilationSuccess>(result)
        assertEquals(expectedArtifact, assertIs<CompilerRenderingSuccess>(success.rendering).svg)
    }

    @Test
    fun `lowering is deterministic for identical input`() {
        val examplePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val compiler = AthenaCompiler()

        val first = assertIs<CompilerLoweringSuccess>(compiler.lower(examplePath))
        val second = assertIs<CompilerLoweringSuccess>(compiler.lower(examplePath))

        assertEquals(first.document, second.document)
    }

    @Test
    fun `preserves mixed endpoint resolution for later validation`() {
        val unresolvedSource = """
            system MissingEndpoint {
              device PLC1 {
                type PLC
              }

              port PLC1.out {
                direction out
              }

              connect PLC1.out -> Missing.in
            }
        """.trimIndent()
        val unresolvedPath = Files.createTempFile("athena-unresolved-", ".athena")
        Files.writeString(unresolvedPath, unresolvedSource)

        try {
            val result = AthenaCompiler().lower(unresolvedPath)

            val success = assertIs<CompilerLoweringSuccess>(result)
            assertEquals(1, success.document.components.size)
            assertEquals(1, success.document.ports.size)
            assertEquals(1, success.document.connections.size)
            assertEquals(StableSemanticIdentity("port:PLC1.out"), success.document.connections.single().from.resolvedIdentity)
            assertNull(success.document.connections.single().to.resolvedIdentity)
            assertEquals(listOf("PLC1", "out"), success.document.connections.single().from.authoredPath)
            assertEquals(listOf("Missing", "in"), success.document.connections.single().to.authoredPath)
        } finally {
            Files.deleteIfExists(unresolvedPath)
        }
    }

    @Test
    fun `synthesizes distinct fallback identities for duplicate declarations and repeated connections`() {
        val duplicateSource = """
            system DuplicateIdentity {
              device PLC1 {
                type PLC
              }

              device PLC1 {
                type PLC
              }

              port PLC1.out {
                direction out
              }

              port PLC1.out {
                direction out
              }

              connect PLC1.out -> PLC1.out
              connect PLC1.out -> PLC1.out
            }
        """.trimIndent()
        val duplicatePath = Files.createTempFile("athena-duplicate-", ".athena")
        Files.writeString(duplicatePath, duplicateSource)

        try {
            val result = AthenaCompiler().lower(duplicatePath)

            val success = assertIs<CompilerLoweringSuccess>(result)
            assertEquals(
                listOf(
                    StableSemanticIdentity("component:PLC1"),
                    StableSemanticIdentity("component:PLC1#2"),
                ),
                success.document.components.map { it.id },
            )
            assertEquals(
                listOf(
                    StableSemanticIdentity("port:PLC1.out"),
                    StableSemanticIdentity("port:PLC1.out#2"),
                ),
                success.document.ports.map { it.id },
            )
            assertEquals(
                listOf(
                    StableSemanticIdentity("connection:PLC1.out->PLC1.out"),
                    StableSemanticIdentity("connection:PLC1.out->PLC1.out#2"),
                ),
                success.document.connections.map { it.id },
            )
            assertTrue(success.document.ports.all { it.ownerReference.resolvedIdentity == null })
            assertTrue(success.document.connections.all { it.from.resolvedIdentity == null && it.to.resolvedIdentity == null })
        } finally {
            Files.deleteIfExists(duplicatePath)
        }
    }

    @Test
    fun `compile diagnoses duplicate signal properties instead of silently skipping compatibility checks`() {
        val duplicateSignalSource = """
            system DuplicateSignal {
              device PLC1 {
                type PLC
              }

              device M1 {
                type Motor
              }

              port PLC1.out {
                direction out
                signal Digital
                signal Analog
              }

              port M1.in {
                direction in
                signal Digital
              }

              connect PLC1.out -> M1.in
            }
        """.trimIndent()
        val duplicateSignalPath = Files.createTempFile("athena-duplicate-signal-", ".athena")
        Files.writeString(duplicateSignalPath, duplicateSignalSource)

        try {
            val result = AthenaCompiler().compile(duplicateSignalPath)

            val success = assertIs<CompilerCompilationSuccess>(result)
            assertEquals(
                listOf("property.port.signal.duplicate"),
                success.semanticResult.diagnostics.map { it.ruleId.value },
            )
        } finally {
            Files.deleteIfExists(duplicateSignalPath)
        }
    }

    @Test
    fun `parse returns diagnostics for unreadable sources`() {
        val unreadablePath = Files.createTempDirectory("athena-parse-directory-")

        try {
            val result = AthenaCompiler().parse(unreadablePath)

            val failure = assertIs<CompilerParseFailure>(result)
            assertEquals(unreadablePath.toString(), failure.diagnostics.single().file)
            assertContains(failure.diagnostics.single().message, "Could not read source file")
        } finally {
            Files.deleteIfExists(unreadablePath)
        }
    }

    @Test
    fun `lower returns diagnostics for unreadable sources`() {
        val unreadablePath = Files.createTempDirectory("athena-lower-directory-")

        try {
            val result = AthenaCompiler().lower(unreadablePath)

            val failure = assertIs<CompilerLoweringFailure>(result)
            assertEquals(unreadablePath.toString(), failure.diagnostics.single().file)
            assertContains(failure.diagnostics.single().message, "Could not read source file")
        } finally {
            Files.deleteIfExists(unreadablePath)
        }
    }

    @Test
    fun `parse result preserves the full ast for downstream passes`() {
        val examplePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")

        val result = AthenaCompiler().parse(examplePath)

        val success = assertIs<CompilerParseSuccess>(result)
        assertEquals("DemoCabinet", success.source.ast.system.name)
        assertEquals(5, success.source.ast.declarations.size)
        assertEquals(examplePath.toString(), success.source.file)
        assertTrue(success.source.ast.system.span.end.line >= 22)
    }

    private fun renderConformanceArtifact(document: EngineeringIrDocument, repoRoot: Path): String {
        fun renderPath(file: String): String {
            val path = Path.of(file)
            return runCatching { repoRoot.relativize(path).toString().replace('\\', '/') }
                .getOrElse { path.toString().replace('\\', '/') }
        }

        fun renderProvenance(provenance: SourceProvenance): String {
            return "${renderPath(provenance.file)}:${provenance.startLine}:${provenance.startColumn}-${provenance.endLine}:${provenance.endColumn}"
        }

        fun renderValue(value: EngineeringPropertyValue): String {
            return when (value) {
                is EngineeringPropertyValue.Symbol -> "symbol:${value.text}"
                is EngineeringPropertyValue.Text -> "text:${value.text}"
            }
        }

        return buildString {
            appendLine("system|id=${document.system.id}|name=${document.system.name}|provenance=${renderProvenance(document.system.provenance)}")
            document.components.forEach { component ->
                appendLine("component|id=${component.id}|name=${component.name}|kind=${component.kind}|provenance=${renderProvenance(component.provenance)}")
                component.properties.forEach { property ->
                    appendLine("property|name=${property.name}|value=${renderValue(property.value)}")
                }
            }
            document.ports.forEach { port ->
                appendLine(
                    "port|id=${port.id}|owner=${port.ownerReference.authoredPath.joinToString(".")}|ownerResolved=${port.ownerReference.resolvedIdentity}|ownerProvenance=${renderProvenance(port.ownerReference.provenance)}|name=${port.name}|provenance=${renderProvenance(port.provenance)}",
                )
                port.properties.forEach { property ->
                    appendLine("property|name=${property.name}|value=${renderValue(property.value)}")
                }
            }
            document.connections.forEach { connection ->
                appendLine(
                    "connection|id=${connection.id}|from=${connection.from.authoredPath.joinToString(".")}|fromResolved=${connection.from.resolvedIdentity}|fromProvenance=${renderProvenance(connection.from.provenance)}|to=${connection.to.authoredPath.joinToString(".")}|toResolved=${connection.to.resolvedIdentity}|toProvenance=${renderProvenance(connection.to.provenance)}|provenance=${renderProvenance(connection.provenance)}",
                )
            }
        }.trimEnd()
    }

    private fun expectedDemoSvg(): String {
        return Files.readString(resolveRepoRoot().resolve("examples/m0/demo-cabinet.svg")).trimEnd()
    }

    private fun resolveRepoRoot(): Path {
        var current = Path.of("").toAbsolutePath()
        while (current.parent != null && !Files.exists(current.resolve("settings.gradle.kts"))) {
            current = current.parent
        }
        assertTrue(Files.exists(current.resolve("settings.gradle.kts")), "Could not locate repository root")
        return current
    }
}
