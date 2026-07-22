package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.boundary.AthenaBoundaryDescriptorSource
import com.engineeringood.athena.ir.EngineeringComponent
import com.engineeringood.athena.ir.EngineeringConnection
import com.engineeringood.athena.ir.EngineeringDocument
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
import com.engineeringood.athena.geometry.GeometryBounds
import com.engineeringood.athena.geometry.GeometryElementKind
import com.engineeringood.athena.geometry.GeometryDocument
import com.engineeringood.athena.geometry.GeometryElement
import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.geometry.GeometryPoint
import com.engineeringood.athena.semantics.core.SemanticContinuationDecision
import com.engineeringood.athena.semantics.core.SemanticDiagnosticCategory
import com.engineeringood.athena.layout.LayoutAxis
import com.engineeringood.athena.layout.LayoutIntent
import com.engineeringood.athena.layout.LayoutDocument
import com.engineeringood.athena.layout.LayoutGroup
import com.engineeringood.athena.layout.LayoutGroupId
import com.engineeringood.athena.layout.LayoutNode
import com.engineeringood.athena.layout.LayoutNodeId
import com.engineeringood.athena.layout.LayoutRelationship
import com.engineeringood.athena.layout.LayoutRelationshipId
import com.engineeringood.athena.layout.LayoutPlacementRelation
import com.engineeringood.athena.layout.LayoutRelationshipKind
import com.engineeringood.athena.layout.LayoutRelativePlacement
import com.engineeringood.athena.layout.ElectricalProjectionDescriptor
import com.engineeringood.athena.layout.ElectricalProjectionFamily
import com.engineeringood.athena.layout.ProjectionInteractivity
import com.engineeringood.athena.layout.ProjectionOwnershipContract
import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.layout.ViewEmphasis
import com.engineeringood.athena.domain.dummyruntime.DummyRuntimeDomainPlugin
import com.engineeringood.athena.domain.electricalruntime.ElectricalRuntimeDomainPlugin
import com.engineeringood.athena.plugin.AthenaCoreRuntime
import com.engineeringood.athena.plugin.AthenaExtensionPoint
import com.engineeringood.athena.plugin.AthenaRenderSurface
import com.engineeringood.athena.plugin.AthenaRenderSurfaceMapping
import com.engineeringood.athena.plugin.host.AthenaPluginDiscovery
import com.engineeringood.athena.projection.ProjectionBounds
import com.engineeringood.athena.projection.ProjectionConnection
import com.engineeringood.athena.projection.ProjectionConnectionId
import com.engineeringood.athena.projection.ElectricalAnchor
import com.engineeringood.athena.projection.ElectricalAnchorId
import com.engineeringood.athena.projection.ElectricalAnchorSide
import com.engineeringood.athena.projection.ElectricalConnectionEndpoint
import com.engineeringood.athena.projection.ElectricalConnectionEndpointId
import com.engineeringood.athena.projection.ElectricalConnectionEndpointRole
import com.engineeringood.athena.projection.ElectricalRoutingCorridor
import com.engineeringood.athena.projection.ElectricalRoutingCorridorId
import com.engineeringood.athena.projection.ElectricalRoutingStyle
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.projection.ProjectionLabel
import com.engineeringood.athena.projection.ProjectionLabelId
import com.engineeringood.athena.projection.ProjectionNode
import com.engineeringood.athena.projection.ProjectionNodeId
import com.engineeringood.athena.projection.ProjectionNotationPack
import com.engineeringood.athena.projection.ProjectionNotationPackId
import com.engineeringood.athena.projection.ProjectionNotationSubject
import com.engineeringood.athena.projection.ProjectionLabelPolicy
import com.engineeringood.athena.projection.ProjectionPoint
import com.engineeringood.athena.projection.ProjectionSheet
import com.engineeringood.athena.projection.ProjectionSheetId
import com.engineeringood.athena.projection.ProjectionSheetSubject
import com.engineeringood.athena.projection.ProjectionSymbolKey
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
            expectedM3PassDescriptors(),
            success.pipeline.passes.map { it.pass },
        )
        assertEquals(
            expectedSuccessfulM3Statuses(),
            success.pipeline.passes.map { it.status },
        )
        assertEquals(
            listOf(
                "system:DemoCabinet",
                "system:DemoCabinet",
                "no semantic enrichers",
                "semantic-valid (kernel=0, domain=0, enrichment=0)",
                "geometry-prepared",
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
                            x1 = 104,
                            y1 = 86,
                            x2 = 316,
                            y2 = 86,
                        ),
                    ),
                ),
                svg = expectedDemoSvg(),
                viewId = "cabinet",
                rendererTarget = "svg",
                activeRenderContributions = listOf(
                    CompilerRenderContributionAttribution(
                        pluginId = "com.engineeringood.athena.domain.electrical-runtime",
                        contributionId = "electrical-runtime.render.cabinet",
                        displayName = "Electrical cabinet rendering intent",
                        description = "Publishes cabinet-view visual intent for hosted electrical structure without taking renderer ownership.",
                        viewIds = setOf("cabinet"),
                        rendererTargets = setOf("svg", "graph-workbench"),
                        surfaceMappings = expectedCabinetSurfaceMappings(),
                    ),
                ),
            ),
            success.rendering,
        )
        assertEquals(
            listOf("cabinet", "wiring", "schematic", "documentation"),
            success.layouts.map { layout -> layout.view.id },
        )
        assertEquals(
            expectedCabinetLayout(),
            success.layouts.first { layout -> layout.view.id == "cabinet" },
        )
        assertEquals(
            expectedWiringLayout(),
            success.layouts.first { layout -> layout.view.id == "wiring" },
        )
        assertEquals(
            listOf("cabinet", "wiring", "schematic", "documentation"),
            success.geometries.map { geometry -> geometry.viewId },
        )
        assertEquals(
            expectedCabinetGeometry(),
            success.geometries.first { geometry -> geometry.viewId == "cabinet" },
        )
        assertEquals(
            expectedWiringGeometry(),
            success.geometries.first { geometry -> geometry.viewId == "wiring" },
        )
        assertEquals(
            listOf("cabinet", "wiring", "schematic", "documentation"),
            success.projections.map { projection -> projection.view.id },
        )
        assertEquals(
            expectedCabinetProjection(),
            success.projections.first { projection -> projection.view.id == "cabinet" },
        )
        assertEquals(
            expectedWiringProjection(),
            success.projections.first { projection -> projection.view.id == "wiring" },
        )
    }

    @Test
    fun `compile derives deterministic cabinet and wiring layouts from one semantic source`() {
        val examplePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val compiler = AthenaCompiler()

        val first = assertIs<CompilerCompilationSuccess>(compiler.compile(examplePath))
        val second = assertIs<CompilerCompilationSuccess>(compiler.compile(examplePath))

        assertEquals(
            listOf("cabinet", "wiring", "schematic", "documentation"),
            compiler.supportedViewDefinitions().map { definition -> definition.id },
        )
        assertEquals(first.layouts, second.layouts)
        assertEquals(first.geometries, second.geometries)
        assertEquals(first.projections, second.projections)
        assertEquals(expectedCabinetLayout(), first.layouts.first { layout -> layout.view.id == "cabinet" })
        assertEquals(expectedWiringLayout(), first.layouts.first { layout -> layout.view.id == "wiring" })
        assertEquals(expectedCabinetGeometry(), first.geometries.first { geometry -> geometry.viewId == "cabinet" })
        assertEquals(expectedWiringGeometry(), first.geometries.first { geometry -> geometry.viewId == "wiring" })
        assertEquals(expectedCabinetProjection(), first.projections.first { projection -> projection.view.id == "cabinet" })
        assertEquals(expectedWiringProjection(), first.projections.first { projection -> projection.view.id == "wiring" })
    }

    @Test
    fun `compile keeps render contribution selection downstream of the emitted view and target`() {
        val examplePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val compiler = AthenaCompiler()

        val result = assertIs<CompilerCompilationSuccess>(compiler.compile(examplePath))
        val rendering = assertIs<CompilerRenderingSuccess>(result.rendering)

        assertEquals(
            listOf(
                CompilerRenderContributionAttribution(
                    pluginId = "com.engineeringood.athena.domain.dummy-runtime",
                    contributionId = "dummy-runtime.render.synthetic-panel",
                    displayName = "Dummy render intent",
                    description = "Publishes synthetic renderer-facing intent without widening the default global view-definition set.",
                    viewIds = setOf("dummy-panel"),
                    rendererTargets = setOf("svg"),
                ),
                CompilerRenderContributionAttribution(
                    pluginId = "com.engineeringood.athena.domain.electrical-runtime",
                    contributionId = "electrical-runtime.render.cabinet",
                    displayName = "Electrical cabinet rendering intent",
                    description = "Publishes cabinet-view visual intent for hosted electrical structure without taking renderer ownership.",
                    viewIds = setOf("cabinet"),
                    rendererTargets = setOf("svg", "graph-workbench"),
                    surfaceMappings = expectedCabinetSurfaceMappings(),
                ),
                CompilerRenderContributionAttribution(
                    pluginId = "com.engineeringood.athena.domain.electrical-runtime",
                    contributionId = "electrical-runtime.render.wiring",
                    displayName = "Electrical wiring rendering intent",
                    description = "Publishes wiring-view visual intent for hosted electrical connectivity without taking renderer ownership.",
                    viewIds = setOf("wiring"),
                    rendererTargets = setOf("svg", "graph-workbench"),
                    surfaceMappings = expectedWiringSurfaceMappings(),
                ),
            ),
            compiler.supportedRenderContributions(),
        )
        assertEquals("cabinet", rendering.viewId)
        assertEquals("svg", rendering.rendererTarget)
        assertEquals(
            listOf("electrical-runtime.render.cabinet"),
            rendering.activeRenderContributions.map { contribution -> contribution.contributionId },
        )
    }

    @Test
    fun `compile selects the first svg-renderable plugin view without a cabinet fallback`() {
        val examplePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val compiler = AthenaCompiler(
            pluginDiscovery = AthenaPluginDiscovery(
                runtime = AthenaCoreRuntime(version = "0.0.1-SNAPSHOT"),
                source = FixedAthenaPluginSource(listOf(SingleViewRenderTestPlugin())),
            ),
        )

        val result = assertIs<CompilerCompilationSuccess>(compiler.compile(examplePath))
        val rendering = assertIs<CompilerRenderingSuccess>(result.rendering)

        assertEquals(
            listOf("operator-console"),
            compiler.supportedViewDefinitions().map { definition -> definition.id },
        )
        assertEquals(
            listOf("operator-console"),
            result.geometries.map { geometry -> geometry.viewId },
        )
        assertEquals("operator-console", rendering.viewId)
        assertEquals(
            listOf("single-view-render.render.operator-console"),
            rendering.activeRenderContributions.map { contribution -> contribution.contributionId },
        )
    }

    @Test
    fun `deriveLayout exposes one supported layout by view id without changing semantic authority`() {
        val examplePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val compiler = AthenaCompiler()
        val lowering = assertIs<CompilerLoweringSuccess>(compiler.lower(examplePath))

        assertEquals(expectedCabinetLayout(), compiler.deriveLayout(lowering.document, "cabinet"))
        assertEquals(expectedWiringLayout(), compiler.deriveLayout(lowering.document, "wiring"))
    }

    @Test
    fun `deriveGeometry exposes one supported geometry by layout without changing semantic authority`() {
        val examplePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val compiler = AthenaCompiler()
        val lowering = assertIs<CompilerLoweringSuccess>(compiler.lower(examplePath))
        val cabinetLayout = compiler.deriveLayout(lowering.document, "cabinet")
        val wiringLayout = compiler.deriveLayout(lowering.document, "wiring")

        assertEquals(expectedCabinetGeometry(), compiler.deriveGeometry(cabinetLayout))
        assertEquals(expectedWiringGeometry(), compiler.deriveGeometry(wiringLayout))
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
            expectedM3PassIds(),
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
            expectedM3PassIds(),
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
            expectedM3PassIds(),
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
            expectedM3PassIds(),
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
            expectedM3PassIds(),
            result.pipeline.passes.map { it.pass.id },
        )
        assertEquals(
            expectedValidationBlockedM3Statuses(),
            result.pipeline.passes.map { it.status },
        )
        assertEquals(
            listOf("domain.semantics.unavailable"),
            result.semanticResult.diagnostics.map { it.ruleId.value },
        )
        assertEquals(emptyList(), result.validationBreakdown.kernelDiagnostics)
        assertEquals(
            listOf("domain.semantics.unavailable"),
            result.validationBreakdown.domainDiagnostics.map { it.ruleId.value },
        )
        assertEquals(emptyList(), result.validationBreakdown.domainValidationAttributions)
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
    fun `compile with only non-lowering domain plugins still reports unavailable domain semantics`() {
        val examplePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val compiler = AthenaCompiler(
            pluginDiscovery = AthenaPluginDiscovery(
                runtime = AthenaCoreRuntime(version = "0.0.1-SNAPSHOT"),
                source = FixedAthenaPluginSource(
                    listOf(
                        ValidateOnlySemanticsTestPlugin(),
                    ),
                ),
            ),
        )

        val result = assertIs<CompilerCompilationSuccess>(compiler.compile(examplePath))

        assertContains(result.semanticResult.diagnostics.map { it.ruleId.value }, "domain.semantics.unavailable")
        assertEquals(emptyList(), result.validationBreakdown.kernelDiagnostics)
        assertContains(
            result.validationBreakdown.domainDiagnostics.map { it.ruleId.value },
            "domain.semantics.unavailable",
        )
        assertContains(
            result.validationBreakdown.domainDiagnostics.map { it.ruleId.value },
            "domain.validation.validate-only",
        )
        assertEquals(
            listOf("validate-only.validation.rules"),
            result.validationBreakdown.domainValidationAttributions.map { attribution -> attribution.contributionId },
        )
        assertEquals(
            CompilerRenderingBlocked(
                reason = "semantic validation requested STOP_DOWNSTREAM",
                blockedByPass = CompilerPassId.VALIDATE,
            ),
            result.rendering,
        )
    }

    @Test
    fun `compile reports unavailable domain semantics when hosted plugins claim none of the authored declarations`() {
        val examplePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val compiler = AthenaCompiler(
            pluginDiscovery = AthenaPluginDiscovery(
                runtime = AthenaCoreRuntime(version = "0.0.1-SNAPSHOT"),
                source = FixedAthenaPluginSource(
                    listOf(
                        DummyRuntimeDomainPlugin(),
                    ),
                ),
            ),
        )

        val result = assertIs<CompilerCompilationSuccess>(compiler.compile(examplePath))

        assertContains(result.semanticResult.diagnostics.map { it.ruleId.value }, "domain.semantics.unavailable")
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
    fun `compile executes semantic enrichment contributions through the governed stage`() {
        val examplePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val compiler = AthenaCompiler(
            pluginDiscovery = AthenaPluginDiscovery(
                runtime = AthenaCoreRuntime(version = "0.0.1-SNAPSHOT"),
                source = FixedAthenaPluginSource(
                    listOf(
                        ElectricalRuntimeDomainPlugin(),
                        SemanticEnrichmentOnlyTestPlugin(),
                    ),
                ),
            ),
        )

        val result = assertIs<CompilerCompilationSuccess>(compiler.compile(examplePath))
        val semanticEnrichmentPass = result.pipeline.passes.first { pass -> pass.pass.id == CompilerPassId.SEMANTIC_ENRICHMENT }

        assertTrue(result.semanticResult.isSemanticallyValid)
        assertEquals(
            listOf("domain.enrichment.synthetic"),
            result.semanticResult.diagnostics.map { it.ruleId.value },
        )
        assertEquals(
            listOf("domain.enrichment.synthetic"),
            result.validationBreakdown.semanticEnrichmentDiagnostics.map { it.ruleId.value },
        )
        assertEquals(emptyList(), result.validationBreakdown.kernelDiagnostics)
        assertEquals(emptyList(), result.validationBreakdown.domainDiagnostics)
        assertEquals(emptyList(), result.validationBreakdown.domainValidationAttributions)
        assertContains(semanticEnrichmentPass.outputSummary, "semantic-enrichment-only.enrich")
        assertContains(semanticEnrichmentPass.outputSummary, "synthetic semantic enrichment note")
        assertContains(semanticEnrichmentPass.outputSummary, "diagnostics=1")
    }

    @Test
    fun `compile preserves kernel validation when no domain validation contributors are present`() {
        val duplicateSource = """
            system DuplicateConnections {
              device PLC1 {
                type Switch
              }

              port PLC1.out {
                direction out
                signal Digital
              }

              connect PLC1.out -> PLC1.out
              connect PLC1.out -> PLC1.out
            }
        """.trimIndent()
        val duplicatePath = Files.createTempFile("athena-kernel-validation-without-plugins-", ".athena")
        Files.writeString(duplicatePath, duplicateSource)

        try {
            val compiler = AthenaCompiler(
                pluginDiscovery = AthenaPluginDiscovery(
                    runtime = AthenaCoreRuntime(version = "0.0.1-SNAPSHOT"),
                    source = FixedAthenaPluginSource(listOf(GenericLoweringOnlyTestPlugin())),
                ),
            )

            val result = assertIs<CompilerCompilationSuccess>(compiler.compile(duplicatePath))

            assertContains(
                result.validationBreakdown.kernelDiagnostics.map { it.ruleId.value },
                "uniqueness.connection.duplicate-authored-key",
            )
            assertEquals(emptyList(), result.validationBreakdown.domainDiagnostics.map { it.ruleId.value })
            assertEquals(emptyList(), result.validationBreakdown.domainValidationAttributions)
            assertEquals(
                listOf(
                    "uniqueness.connection.duplicate-authored-key",
                    "uniqueness.connection.duplicate-authored-key",
                ),
                result.semanticResult.diagnostics.map { it.ruleId.value },
            )
        } finally {
            Files.deleteIfExists(duplicatePath)
        }
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
    fun `compile skips backend stages when semantic continuation stops`() {
        val examplePath = resolveRepoRoot().resolve("examples/m0/invalid-semantic-cabinet.athena")

        val result = AthenaCompiler().compile(examplePath)

        val success = assertIs<CompilerCompilationSuccess>(result)
        assertEquals(
            expectedValidationBlockedM3Statuses(),
            success.pipeline.passes.map { it.status },
        )
        assertEquals(
            "backend-emission-skipped (validation stopped downstream)",
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
                type Switch
              }
        """.trimIndent()
        val malformedPath = Files.createTempFile("athena-malformed-", ".athena")
        Files.writeString(malformedPath, malformedSource)

        try {
            val result = AthenaCompiler().compile(malformedPath)

            val failure = assertIs<CompilerCompilationParseFailure>(result)
            assertEquals(
                expectedParseFailureM3Statuses(),
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
        assertEquals(CompilerValidationBreakdown(), success.validationBreakdown)
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
        assertEquals(emptyList(), success.layouts)
        assertEquals(emptyList(), success.geometries)
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
        assertEquals(
            listOf("reference.connection-endpoint.unresolved"),
            success.validationBreakdown.kernelDiagnostics.map { it.ruleId.value },
        )
        assertEquals(
            listOf(
                "property.component.type.missing",
                "connection.signal.incompatible",
            ),
            success.validationBreakdown.domainDiagnostics.map { it.ruleId.value },
        )
        assertEquals(
            listOf("com.engineeringood.athena.domain.electrical-runtime"),
            success.validationBreakdown.domainValidationAttributions.map { attribution -> attribution.pluginId },
        )
        assertEquals(
            listOf("electrical-runtime.validation.component-and-port-rules"),
            success.validationBreakdown.domainValidationAttributions.map { attribution -> attribution.contributionId },
        )
        assertEquals(
            listOf(
                listOf(
                    "property.component.type.missing",
                    "connection.signal.incompatible",
                ),
            ),
            success.validationBreakdown.domainValidationAttributions.map { attribution ->
                attribution.ruleIds.map { ruleId -> ruleId.value }
            },
        )
        assertEquals(emptyList(), success.validationBreakdown.semanticEnrichmentDiagnostics)
    }

    @Test
    fun `compile removes only plugin owned domain validation attribution when the electrical plugin is absent`() {
        val examplePath = resolveRepoRoot().resolve("examples/m0/invalid-semantic-cabinet.athena")
        val electricalCompiler = AthenaCompiler(
            pluginDiscovery = AthenaPluginDiscovery(
                runtime = AthenaCoreRuntime(version = "0.0.1-SNAPSHOT"),
                source = FixedAthenaPluginSource(listOf(ElectricalRuntimeDomainPlugin())),
            ),
        )
        val genericCompiler = AthenaCompiler(
            pluginDiscovery = AthenaPluginDiscovery(
                runtime = AthenaCoreRuntime(version = "0.0.1-SNAPSHOT"),
                source = FixedAthenaPluginSource(listOf(GenericLoweringOnlyTestPlugin())),
            ),
        )

        val electricalResult = assertIs<CompilerCompilationSuccess>(electricalCompiler.compile(examplePath))
        val genericResult = assertIs<CompilerCompilationSuccess>(genericCompiler.compile(examplePath))

        assertEquals(
            electricalResult.validationBreakdown.kernelDiagnostics,
            genericResult.validationBreakdown.kernelDiagnostics,
        )
        assertEquals(
            listOf(
                "property.component.type.missing",
                "connection.signal.incompatible",
            ),
            electricalResult.validationBreakdown.domainDiagnostics.map { diagnostic -> diagnostic.ruleId.value },
        )
        assertEquals(emptyList(), genericResult.validationBreakdown.domainDiagnostics)
        assertEquals(
            listOf("electrical-runtime.validation.component-and-port-rules"),
            electricalResult.validationBreakdown.domainValidationAttributions.map { attribution -> attribution.contributionId },
        )
        assertEquals(emptyList(), genericResult.validationBreakdown.domainValidationAttributions)
    }

    @Test
    fun `compile reports duplicate connection keys as semantic uniqueness diagnostics`() {
        val duplicateSource = """
            system DuplicateConnections {
              device PLC1 {
                type Switch
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
                type "Switch"
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
            EngineeringDocument(
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
                            EngineeringProperty("type", EngineeringPropertyValue.Symbol("Switch")),
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
                type Switch
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
                type Switch
              }

              device PLC1 {
                type Switch
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
                type Switch
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

    private fun renderConformanceArtifact(document: EngineeringDocument, repoRoot: Path): String {
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

    private fun expectedM3PassIds(): List<CompilerPassId> = expectedM3PassDescriptors().map { it.id }

    private fun expectedSuccessfulM3Statuses(): List<CompilerPassExecutionStatus> {
        return List(expectedM3PassDescriptors().size) { CompilerPassExecutionStatus.SUCCEEDED }
    }

    private fun expectedValidationBlockedM3Statuses(): List<CompilerPassExecutionStatus> {
        return listOf(
            CompilerPassExecutionStatus.SUCCEEDED,
            CompilerPassExecutionStatus.SUCCEEDED,
            CompilerPassExecutionStatus.SUCCEEDED,
            CompilerPassExecutionStatus.SUCCEEDED,
            CompilerPassExecutionStatus.SKIPPED,
            CompilerPassExecutionStatus.SKIPPED,
        )
    }

    private fun expectedParseFailureM3Statuses(): List<CompilerPassExecutionStatus> {
        return listOf(
            CompilerPassExecutionStatus.FAILED,
            CompilerPassExecutionStatus.SKIPPED,
            CompilerPassExecutionStatus.SKIPPED,
            CompilerPassExecutionStatus.SKIPPED,
            CompilerPassExecutionStatus.SKIPPED,
            CompilerPassExecutionStatus.SKIPPED,
        )
    }

    private fun expectedM3PassDescriptors(): List<CompilerPassDescriptor> {
        return listOf(
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
                id = CompilerPassId.SEMANTIC_ENRICHMENT,
                responsibility = "Coordinate governed semantic enrichment participation over canonical Engineering IR",
                inputState = "canonical Engineering IR",
                outputState = "semantic enrichment coordination result",
            ),
            CompilerPassDescriptor(
                id = CompilerPassId.VALIDATE,
                responsibility = "Validate canonical Engineering IR and compute continuation policy",
                inputState = "canonical Engineering IR plus semantic enrichment context",
                outputState = "semantic validation result",
            ),
            CompilerPassDescriptor(
                id = CompilerPassId.BACKEND_PREPARATION,
                responsibility = "Prepare downstream backend input from validated canonical semantics and supported projections",
                inputState = "semantic validation result plus canonical Engineering IR",
                outputState = "geometry-backed backend input or block reason",
            ),
            CompilerPassDescriptor(
                id = CompilerPassId.BACKEND_EMISSION,
                responsibility = "Emit downstream backend output from prepared backend input",
                inputState = "prepared backend input",
                outputState = "backend emission result",
            ),
        )
    }

    private fun expectedDemoSvg(): String {
        return Files.readString(resolveRepoRoot().resolve("examples/m0/demo-cabinet.svg")).trimEnd()
    }

    private fun expectedCabinetSurfaceMappings(): List<AthenaRenderSurfaceMapping> {
        return listOf(
            AthenaRenderSurfaceMapping(
                surface = AthenaRenderSurface.CANVAS,
                tokens = mapOf(
                    "canvasTint" to "var(--athena-graph-cabinet-canvas-tint)",
                    "gridMajor" to "var(--athena-graph-cabinet-grid-major)",
                    "gridMinor" to "var(--athena-graph-cabinet-grid-minor)",
                ),
            ),
            AthenaRenderSurfaceMapping(
                surface = AthenaRenderSurface.NODE,
                tokens = mapOf(
                    "fill" to "var(--athena-graph-cabinet-node-fill)",
                    "stroke" to "var(--athena-graph-cabinet-node-stroke)",
                    "label" to "var(--athena-graph-cabinet-node-label)",
                    "meta" to "var(--athena-graph-cabinet-node-meta)",
                ),
            ),
            AthenaRenderSurfaceMapping(
                surface = AthenaRenderSurface.EDGE,
                tokens = mapOf(
                    "stroke" to "var(--athena-graph-cabinet-edge-stroke)",
                ),
            ),
        )
    }

    private fun expectedWiringSurfaceMappings(): List<AthenaRenderSurfaceMapping> {
        return listOf(
            AthenaRenderSurfaceMapping(
                surface = AthenaRenderSurface.CANVAS,
                tokens = mapOf(
                    "canvasTint" to "var(--athena-graph-wiring-canvas-tint)",
                    "gridMajor" to "var(--athena-graph-wiring-grid-major)",
                    "gridMinor" to "var(--athena-graph-wiring-grid-minor)",
                ),
            ),
            AthenaRenderSurfaceMapping(
                surface = AthenaRenderSurface.NODE,
                tokens = mapOf(
                    "fill" to "var(--athena-graph-wiring-node-fill)",
                    "stroke" to "var(--athena-graph-wiring-node-stroke)",
                    "label" to "var(--athena-graph-wiring-node-label)",
                    "meta" to "var(--athena-graph-wiring-node-meta)",
                ),
            ),
            AthenaRenderSurfaceMapping(
                surface = AthenaRenderSurface.EDGE,
                tokens = mapOf(
                    "stroke" to "var(--athena-graph-wiring-edge-stroke)",
                ),
            ),
        )
    }

    private fun expectedCabinetLayout(): LayoutDocument {
        val plcComponentLayoutId = LayoutNodeId("cabinet/node/component_PLC1")
        val plcPortLayoutId = LayoutNodeId("cabinet/node/port_PLC1_out")
        val motorComponentLayoutId = LayoutNodeId("cabinet/node/component_M1")
        val motorPortLayoutId = LayoutNodeId("cabinet/node/port_M1_in")
        return LayoutDocument(
            view = cabinetViewDefinition(),
            groups = listOf(
                LayoutGroup(
                    groupId = LayoutGroupId("cabinet/group/component_PLC1"),
                    label = "PLC1",
                    kind = "component-group",
                    semanticIds = listOf(
                        StableSemanticIdentity("component:PLC1"),
                        StableSemanticIdentity("port:PLC1.out"),
                    ),
                    memberLayoutIds = listOf(plcComponentLayoutId, plcPortLayoutId),
                ),
                LayoutGroup(
                    groupId = LayoutGroupId("cabinet/group/component_M1"),
                    label = "M1",
                    kind = "component-group",
                    semanticIds = listOf(
                        StableSemanticIdentity("component:M1"),
                        StableSemanticIdentity("port:M1.in"),
                    ),
                    memberLayoutIds = listOf(motorComponentLayoutId, motorPortLayoutId),
                ),
            ),
            nodes = listOf(
                LayoutNode(
                    layoutId = plcComponentLayoutId,
                    semanticId = StableSemanticIdentity("component:PLC1"),
                    label = "PLC1",
                    kind = "component",
                    groupId = LayoutGroupId("cabinet/group/component_PLC1"),
                    order = 0,
                    emphasis = listOf(ViewEmphasis.OWNERSHIP, ViewEmphasis.PLACEMENT),
                ),
                LayoutNode(
                    layoutId = plcPortLayoutId,
                    semanticId = StableSemanticIdentity("port:PLC1.out"),
                    label = "out",
                    kind = "port",
                    groupId = LayoutGroupId("cabinet/group/component_PLC1"),
                    order = 0,
                    relativePlacement = LayoutRelativePlacement(
                        axis = LayoutAxis.VERTICAL,
                        relation = LayoutPlacementRelation.WITHIN,
                        referenceLayoutId = plcComponentLayoutId,
                    ),
                    emphasis = listOf(ViewEmphasis.PLACEMENT),
                ),
                LayoutNode(
                    layoutId = motorComponentLayoutId,
                    semanticId = StableSemanticIdentity("component:M1"),
                    label = "M1",
                    kind = "component",
                    groupId = LayoutGroupId("cabinet/group/component_M1"),
                    order = 1,
                    relativePlacement = LayoutRelativePlacement(
                        axis = LayoutAxis.HORIZONTAL,
                        relation = LayoutPlacementRelation.AFTER,
                        referenceLayoutId = plcComponentLayoutId,
                    ),
                    emphasis = listOf(ViewEmphasis.OWNERSHIP, ViewEmphasis.PLACEMENT),
                ),
                LayoutNode(
                    layoutId = motorPortLayoutId,
                    semanticId = StableSemanticIdentity("port:M1.in"),
                    label = "in",
                    kind = "port",
                    groupId = LayoutGroupId("cabinet/group/component_M1"),
                    order = 0,
                    relativePlacement = LayoutRelativePlacement(
                        axis = LayoutAxis.VERTICAL,
                        relation = LayoutPlacementRelation.WITHIN,
                        referenceLayoutId = motorComponentLayoutId,
                    ),
                    emphasis = listOf(ViewEmphasis.PLACEMENT),
                ),
            ),
            relationships = listOf(
                LayoutRelationship(
                    relationshipId = LayoutRelationshipId("cabinet/relationship/ownership/port_PLC1_out"),
                    semanticId = StableSemanticIdentity("port:PLC1.out"),
                    kind = LayoutRelationshipKind.OWNERSHIP,
                    sourceLayoutId = plcComponentLayoutId,
                    targetLayoutId = plcPortLayoutId,
                    emphasis = listOf(ViewEmphasis.OWNERSHIP),
                ),
                LayoutRelationship(
                    relationshipId = LayoutRelationshipId("cabinet/relationship/ownership/port_M1_in"),
                    semanticId = StableSemanticIdentity("port:M1.in"),
                    kind = LayoutRelationshipKind.OWNERSHIP,
                    sourceLayoutId = motorComponentLayoutId,
                    targetLayoutId = motorPortLayoutId,
                    emphasis = listOf(ViewEmphasis.OWNERSHIP),
                ),
                LayoutRelationship(
                    relationshipId = LayoutRelationshipId(
                        "cabinet/relationship/connectivity/connection_PLC1_out_M1_in",
                    ),
                    semanticId = StableSemanticIdentity("connection:PLC1.out->M1.in"),
                    kind = LayoutRelationshipKind.CONNECTIVITY,
                    sourceLayoutId = plcPortLayoutId,
                    targetLayoutId = motorPortLayoutId,
                ),
            ),
        )
    }

    private fun expectedWiringLayout(): LayoutDocument {
        val plcComponentLayoutId = LayoutNodeId("wiring/node/component_PLC1")
        val motorComponentLayoutId = LayoutNodeId("wiring/node/component_M1")
        val plcPortLayoutId = LayoutNodeId("wiring/node/port_PLC1_out")
        val motorPortLayoutId = LayoutNodeId("wiring/node/port_M1_in")
        return LayoutDocument(
            view = wiringViewDefinition(),
            groups = listOf(
                LayoutGroup(
                    groupId = LayoutGroupId("wiring/group/signal/Digital"),
                    label = "Digital",
                    kind = "signal-group",
                    semanticIds = listOf(
                        StableSemanticIdentity("port:PLC1.out"),
                        StableSemanticIdentity("port:M1.in"),
                        StableSemanticIdentity("connection:PLC1.out->M1.in"),
                    ),
                    memberLayoutIds = listOf(plcPortLayoutId, motorPortLayoutId),
                ),
            ),
            nodes = listOf(
                LayoutNode(
                    layoutId = plcComponentLayoutId,
                    semanticId = StableSemanticIdentity("component:PLC1"),
                    label = "PLC1",
                    kind = "component",
                    order = 0,
                ),
                LayoutNode(
                    layoutId = motorComponentLayoutId,
                    semanticId = StableSemanticIdentity("component:M1"),
                    label = "M1",
                    kind = "component",
                    order = 1,
                    relativePlacement = LayoutRelativePlacement(
                        axis = LayoutAxis.VERTICAL,
                        relation = LayoutPlacementRelation.AFTER,
                        referenceLayoutId = plcComponentLayoutId,
                    ),
                ),
                LayoutNode(
                    layoutId = plcPortLayoutId,
                    semanticId = StableSemanticIdentity("port:PLC1.out"),
                    label = "out",
                    kind = "port",
                    groupId = LayoutGroupId("wiring/group/signal/Digital"),
                    order = 0,
                    relativePlacement = LayoutRelativePlacement(
                        axis = LayoutAxis.HORIZONTAL,
                        relation = LayoutPlacementRelation.WITHIN,
                    ),
                    emphasis = listOf(ViewEmphasis.CONNECTIVITY, ViewEmphasis.SIGNAL_FLOW),
                ),
                LayoutNode(
                    layoutId = motorPortLayoutId,
                    semanticId = StableSemanticIdentity("port:M1.in"),
                    label = "in",
                    kind = "port",
                    groupId = LayoutGroupId("wiring/group/signal/Digital"),
                    order = 1,
                    relativePlacement = LayoutRelativePlacement(
                        axis = LayoutAxis.HORIZONTAL,
                        relation = LayoutPlacementRelation.AFTER,
                        referenceLayoutId = plcPortLayoutId,
                    ),
                    emphasis = listOf(ViewEmphasis.CONNECTIVITY, ViewEmphasis.SIGNAL_FLOW),
                ),
            ),
            relationships = listOf(
                LayoutRelationship(
                    relationshipId = LayoutRelationshipId("wiring/relationship/ownership/port_PLC1_out"),
                    semanticId = StableSemanticIdentity("port:PLC1.out"),
                    kind = LayoutRelationshipKind.OWNERSHIP,
                    sourceLayoutId = plcComponentLayoutId,
                    targetLayoutId = plcPortLayoutId,
                ),
                LayoutRelationship(
                    relationshipId = LayoutRelationshipId("wiring/relationship/ownership/port_M1_in"),
                    semanticId = StableSemanticIdentity("port:M1.in"),
                    kind = LayoutRelationshipKind.OWNERSHIP,
                    sourceLayoutId = motorComponentLayoutId,
                    targetLayoutId = motorPortLayoutId,
                ),
                LayoutRelationship(
                    relationshipId = LayoutRelationshipId(
                        "wiring/relationship/connectivity/connection_PLC1_out_M1_in",
                    ),
                    semanticId = StableSemanticIdentity("connection:PLC1.out->M1.in"),
                    kind = LayoutRelationshipKind.CONNECTIVITY,
                    sourceLayoutId = plcPortLayoutId,
                    targetLayoutId = motorPortLayoutId,
                    emphasis = listOf(ViewEmphasis.CONNECTIVITY, ViewEmphasis.SIGNAL_FLOW),
                ),
            ),
        )
    }

    private fun expectedCabinetGeometry(): GeometryDocument {
        return GeometryDocument(
            viewId = "cabinet",
            canvasWidth = 480,
            canvasHeight = 172,
            elements = listOf(
                GeometryElement(
                    elementId = GeometryElementId("cabinet/geometry/box/component_PLC1"),
                    semanticId = StableSemanticIdentity("component:PLC1"),
                    kind = GeometryElementKind.BOX,
                    bounds = GeometryBounds(x = 40, y = 60, width = 140, height = 72),
                    label = "PLC1",
                ),
                GeometryElement(
                    elementId = GeometryElementId("cabinet/geometry/label/port_PLC1_out"),
                    semanticId = StableSemanticIdentity("port:PLC1.out"),
                    kind = GeometryElementKind.LABEL,
                    bounds = GeometryBounds(x = 56, y = 78, width = 48, height = 16),
                    label = "out",
                ),
                GeometryElement(
                    elementId = GeometryElementId("cabinet/geometry/box/component_M1"),
                    semanticId = StableSemanticIdentity("component:M1"),
                    kind = GeometryElementKind.BOX,
                    bounds = GeometryBounds(x = 300, y = 60, width = 140, height = 72),
                    label = "M1",
                ),
                GeometryElement(
                    elementId = GeometryElementId("cabinet/geometry/label/port_M1_in"),
                    semanticId = StableSemanticIdentity("port:M1.in"),
                    kind = GeometryElementKind.LABEL,
                    bounds = GeometryBounds(x = 316, y = 78, width = 48, height = 16),
                    label = "in",
                ),
                GeometryElement(
                    elementId = GeometryElementId("cabinet/geometry/path/connection_PLC1_out_M1_in"),
                    semanticId = StableSemanticIdentity("connection:PLC1.out->M1.in"),
                    kind = GeometryElementKind.PATH,
                    bounds = GeometryBounds(x = 104, y = 86, width = 212, height = 1),
                    points = listOf(
                        GeometryPoint(x = 104, y = 86),
                        GeometryPoint(x = 210, y = 86),
                        GeometryPoint(x = 210, y = 86),
                        GeometryPoint(x = 316, y = 86),
                    ),
                ),
            ),
        )
    }

    private fun expectedWiringGeometry(): GeometryDocument {
        return GeometryDocument(
            viewId = "wiring",
            canvasWidth = 490,
            canvasHeight = 244,
            elements = listOf(
                GeometryElement(
                    elementId = GeometryElementId("wiring/geometry/box/component_PLC1"),
                    semanticId = StableSemanticIdentity("component:PLC1"),
                    kind = GeometryElementKind.BOX,
                    bounds = GeometryBounds(x = 40, y = 40, width = 110, height = 44),
                    label = "PLC1",
                ),
                GeometryElement(
                    elementId = GeometryElementId("wiring/geometry/box/component_M1"),
                    semanticId = StableSemanticIdentity("component:M1"),
                    kind = GeometryElementKind.BOX,
                    bounds = GeometryBounds(x = 40, y = 160, width = 110, height = 44),
                    label = "M1",
                ),
                GeometryElement(
                    elementId = GeometryElementId("wiring/geometry/label/port_PLC1_out"),
                    semanticId = StableSemanticIdentity("port:PLC1.out"),
                    kind = GeometryElementKind.LABEL,
                    bounds = GeometryBounds(x = 240, y = 72, width = 60, height = 18),
                    label = "out",
                ),
                GeometryElement(
                    elementId = GeometryElementId("wiring/geometry/label/port_M1_in"),
                    semanticId = StableSemanticIdentity("port:M1.in"),
                    kind = GeometryElementKind.LABEL,
                    bounds = GeometryBounds(x = 390, y = 72, width = 60, height = 18),
                    label = "in",
                ),
                GeometryElement(
                    elementId = GeometryElementId("wiring/geometry/path/connection_PLC1_out_M1_in"),
                    semanticId = StableSemanticIdentity("connection:PLC1.out->M1.in"),
                    kind = GeometryElementKind.PATH,
                    bounds = GeometryBounds(x = 300, y = 81, width = 90, height = 1),
                    points = listOf(
                        GeometryPoint(x = 300, y = 81),
                        GeometryPoint(x = 345, y = 81),
                        GeometryPoint(x = 345, y = 81),
                        GeometryPoint(x = 390, y = 81),
                    ),
                ),
            ),
        )
    }

    private fun expectedCabinetProjection(): ProjectionDocument {
        return ProjectionDocument(
            view = cabinetViewDefinition(),
            canvasWidth = 480,
            canvasHeight = 172,
            nodes = listOf(
                ProjectionNode(
                    projectionId = ProjectionNodeId("cabinet/projection/node/component_PLC1"),
                    semanticId = StableSemanticIdentity("component:PLC1"),
                    label = "PLC1",
                    bounds = ProjectionBounds(x = 40, y = 60, width = 140, height = 72),
                    originGeometryElementId = GeometryElementId("cabinet/geometry/box/component_PLC1"),
                ),
                ProjectionNode(
                    projectionId = ProjectionNodeId("cabinet/projection/node/component_M1"),
                    semanticId = StableSemanticIdentity("component:M1"),
                    label = "M1",
                    bounds = ProjectionBounds(x = 300, y = 60, width = 140, height = 72),
                    originGeometryElementId = GeometryElementId("cabinet/geometry/box/component_M1"),
                ),
            ),
            connections = listOf(
                ProjectionConnection(
                    projectionId = ProjectionConnectionId("cabinet/projection/connection/connection_PLC1_out_M1_in"),
                    semanticId = StableSemanticIdentity("connection:PLC1.out->M1.in"),
                    start = ProjectionPoint(x = 104, y = 86),
                    end = ProjectionPoint(x = 316, y = 86),
                    originGeometryElementId = GeometryElementId("cabinet/geometry/path/connection_PLC1_out_M1_in"),
                ),
            ),
            labels = listOf(
                ProjectionLabel(
                    projectionId = ProjectionLabelId("cabinet/projection/label/port_PLC1_out"),
                    semanticId = StableSemanticIdentity("port:PLC1.out"),
                    label = "out",
                    bounds = ProjectionBounds(x = 56, y = 78, width = 48, height = 16),
                    originGeometryElementId = GeometryElementId("cabinet/geometry/label/port_PLC1_out"),
                ),
                ProjectionLabel(
                    projectionId = ProjectionLabelId("cabinet/projection/label/port_M1_in"),
                    semanticId = StableSemanticIdentity("port:M1.in"),
                    label = "in",
                    bounds = ProjectionBounds(x = 316, y = 78, width = 48, height = 16),
                    originGeometryElementId = GeometryElementId("cabinet/geometry/label/port_M1_in"),
                ),
            ),
            sheets = listOf(
                ProjectionSheet(
                    sheetId = ProjectionSheetId("cabinet/sheet/01-main"),
                    displayName = "Cabinet Main",
                    order = 0,
                    subjects = listOf(
                        ProjectionSheetSubject(
                            semanticId = StableSemanticIdentity("component:M1"),
                            nodeIds = listOf(ProjectionNodeId("cabinet/projection/node/component_M1")),
                        ),
                        ProjectionSheetSubject(
                            semanticId = StableSemanticIdentity("component:PLC1"),
                            nodeIds = listOf(ProjectionNodeId("cabinet/projection/node/component_PLC1")),
                        ),
                        ProjectionSheetSubject(
                            semanticId = StableSemanticIdentity("connection:PLC1.out->M1.in"),
                            connectionIds = listOf(
                                ProjectionConnectionId("cabinet/projection/connection/connection_PLC1_out_M1_in"),
                            ),
                        ),
                        ProjectionSheetSubject(
                            semanticId = StableSemanticIdentity("port:M1.in"),
                            labelIds = listOf(ProjectionLabelId("cabinet/projection/label/port_M1_in")),
                        ),
                        ProjectionSheetSubject(
                            semanticId = StableSemanticIdentity("port:PLC1.out"),
                            labelIds = listOf(ProjectionLabelId("cabinet/projection/label/port_PLC1_out")),
                        ),
                    ),
                ),
            ),
            notationPack = ProjectionNotationPack(
                packId = ProjectionNotationPackId("electrical-notation/cabinet/default-v1"),
                displayName = "Electrical Cabinet Default",
                subjects = listOf(
                    ProjectionNotationSubject(
                        semanticId = StableSemanticIdentity("component:M1"),
                        symbolKey = ProjectionSymbolKey("device.cabinet.default"),
                        labelPolicy = ProjectionLabelPolicy.SUBJECT_LABEL,
                        markerKeys = listOf("owned-device"),
                    ),
                    ProjectionNotationSubject(
                        semanticId = StableSemanticIdentity("component:PLC1"),
                        symbolKey = ProjectionSymbolKey("device.cabinet.default"),
                        labelPolicy = ProjectionLabelPolicy.SUBJECT_LABEL,
                        markerKeys = listOf("owned-device"),
                    ),
                    ProjectionNotationSubject(
                        semanticId = StableSemanticIdentity("connection:PLC1.out->M1.in"),
                        symbolKey = ProjectionSymbolKey("connection.cabinet.default"),
                        labelPolicy = ProjectionLabelPolicy.HIDDEN,
                    ),
                    ProjectionNotationSubject(
                        semanticId = StableSemanticIdentity("port:M1.in"),
                        symbolKey = ProjectionSymbolKey("port.cabinet.default"),
                        labelPolicy = ProjectionLabelPolicy.TERMINAL_LABEL,
                    ),
                    ProjectionNotationSubject(
                        semanticId = StableSemanticIdentity("port:PLC1.out"),
                        symbolKey = ProjectionSymbolKey("port.cabinet.default"),
                        labelPolicy = ProjectionLabelPolicy.TERMINAL_LABEL,
                    ),
                ),
            ),
            electricalAnchors = listOf(
                ElectricalAnchor(
                    anchorId = ElectricalAnchorId("cabinet/projection/label/port_PLC1_out/anchor"),
                    portSemanticId = StableSemanticIdentity("port:PLC1.out"),
                    ownerSemanticId = StableSemanticIdentity("component:PLC1"),
                    nodeId = ProjectionNodeId("cabinet/projection/node/component_PLC1"),
                    labelId = ProjectionLabelId("cabinet/projection/label/port_PLC1_out"),
                    position = ProjectionPoint(x = 80, y = 60),
                    side = ElectricalAnchorSide.TOP,
                ),
                ElectricalAnchor(
                    anchorId = ElectricalAnchorId("cabinet/projection/label/port_M1_in/anchor"),
                    portSemanticId = StableSemanticIdentity("port:M1.in"),
                    ownerSemanticId = StableSemanticIdentity("component:M1"),
                    nodeId = ProjectionNodeId("cabinet/projection/node/component_M1"),
                    labelId = ProjectionLabelId("cabinet/projection/label/port_M1_in"),
                    position = ProjectionPoint(x = 340, y = 60),
                    side = ElectricalAnchorSide.TOP,
                ),
            ),
            electricalConnectionEndpoints = listOf(
                ElectricalConnectionEndpoint(
                    endpointId = ElectricalConnectionEndpointId(
                        "cabinet/projection/connection/connection_PLC1_out_M1_in/endpoint/source",
                    ),
                    projectionConnectionId = ProjectionConnectionId(
                        "cabinet/projection/connection/connection_PLC1_out_M1_in",
                    ),
                    connectionSemanticId = StableSemanticIdentity("connection:PLC1.out->M1.in"),
                    endpointRole = ElectricalConnectionEndpointRole.SOURCE,
                    portSemanticId = StableSemanticIdentity("port:PLC1.out"),
                    anchorId = ElectricalAnchorId("cabinet/projection/label/port_PLC1_out/anchor"),
                ),
                ElectricalConnectionEndpoint(
                    endpointId = ElectricalConnectionEndpointId(
                        "cabinet/projection/connection/connection_PLC1_out_M1_in/endpoint/target",
                    ),
                    projectionConnectionId = ProjectionConnectionId(
                        "cabinet/projection/connection/connection_PLC1_out_M1_in",
                    ),
                    connectionSemanticId = StableSemanticIdentity("connection:PLC1.out->M1.in"),
                    endpointRole = ElectricalConnectionEndpointRole.TARGET,
                    portSemanticId = StableSemanticIdentity("port:M1.in"),
                    anchorId = ElectricalAnchorId("cabinet/projection/label/port_M1_in/anchor"),
                ),
            ),
            electricalRoutingCorridors = listOf(
                ElectricalRoutingCorridor(
                    corridorId = ElectricalRoutingCorridorId(
                        "cabinet/projection/connection/connection_PLC1_out_M1_in/corridor",
                    ),
                    projectionConnectionId = ProjectionConnectionId(
                        "cabinet/projection/connection/connection_PLC1_out_M1_in",
                    ),
                    connectionSemanticId = StableSemanticIdentity("connection:PLC1.out->M1.in"),
                    sourceAnchorId = ElectricalAnchorId("cabinet/projection/label/port_PLC1_out/anchor"),
                    targetAnchorId = ElectricalAnchorId("cabinet/projection/label/port_M1_in/anchor"),
                    routingStyle = ElectricalRoutingStyle.ORTHOGONAL,
                ),
            ),
        )
    }

    private fun expectedWiringProjection(): ProjectionDocument {
        return ProjectionDocument(
            view = wiringViewDefinition(),
            canvasWidth = 490,
            canvasHeight = 244,
            nodes = listOf(
                ProjectionNode(
                    projectionId = ProjectionNodeId("wiring/projection/node/component_PLC1"),
                    semanticId = StableSemanticIdentity("component:PLC1"),
                    label = "PLC1",
                    bounds = ProjectionBounds(x = 40, y = 40, width = 110, height = 44),
                    originGeometryElementId = GeometryElementId("wiring/geometry/box/component_PLC1"),
                ),
                ProjectionNode(
                    projectionId = ProjectionNodeId("wiring/projection/node/component_M1"),
                    semanticId = StableSemanticIdentity("component:M1"),
                    label = "M1",
                    bounds = ProjectionBounds(x = 40, y = 160, width = 110, height = 44),
                    originGeometryElementId = GeometryElementId("wiring/geometry/box/component_M1"),
                ),
            ),
            connections = listOf(
                ProjectionConnection(
                    projectionId = ProjectionConnectionId("wiring/projection/connection/connection_PLC1_out_M1_in"),
                    semanticId = StableSemanticIdentity("connection:PLC1.out->M1.in"),
                    start = ProjectionPoint(x = 300, y = 81),
                    end = ProjectionPoint(x = 390, y = 81),
                    originGeometryElementId = GeometryElementId("wiring/geometry/path/connection_PLC1_out_M1_in"),
                ),
            ),
            labels = listOf(
                ProjectionLabel(
                    projectionId = ProjectionLabelId("wiring/projection/label/port_PLC1_out"),
                    semanticId = StableSemanticIdentity("port:PLC1.out"),
                    label = "out",
                    bounds = ProjectionBounds(x = 240, y = 72, width = 60, height = 18),
                    originGeometryElementId = GeometryElementId("wiring/geometry/label/port_PLC1_out"),
                ),
                ProjectionLabel(
                    projectionId = ProjectionLabelId("wiring/projection/label/port_M1_in"),
                    semanticId = StableSemanticIdentity("port:M1.in"),
                    label = "in",
                    bounds = ProjectionBounds(x = 390, y = 72, width = 60, height = 18),
                    originGeometryElementId = GeometryElementId("wiring/geometry/label/port_M1_in"),
                ),
            ),
            sheets = listOf(
                ProjectionSheet(
                    sheetId = ProjectionSheetId("wiring/sheet/01-main"),
                    displayName = "Wiring Main",
                    order = 0,
                    subjects = listOf(
                        ProjectionSheetSubject(
                            semanticId = StableSemanticIdentity("component:M1"),
                            nodeIds = listOf(ProjectionNodeId("wiring/projection/node/component_M1")),
                        ),
                        ProjectionSheetSubject(
                            semanticId = StableSemanticIdentity("component:PLC1"),
                            nodeIds = listOf(ProjectionNodeId("wiring/projection/node/component_PLC1")),
                        ),
                        ProjectionSheetSubject(
                            semanticId = StableSemanticIdentity("connection:PLC1.out->M1.in"),
                            connectionIds = listOf(
                                ProjectionConnectionId("wiring/projection/connection/connection_PLC1_out_M1_in"),
                            ),
                        ),
                        ProjectionSheetSubject(
                            semanticId = StableSemanticIdentity("port:M1.in"),
                            labelIds = listOf(ProjectionLabelId("wiring/projection/label/port_M1_in")),
                        ),
                        ProjectionSheetSubject(
                            semanticId = StableSemanticIdentity("port:PLC1.out"),
                            labelIds = listOf(ProjectionLabelId("wiring/projection/label/port_PLC1_out")),
                        ),
                    ),
                ),
            ),
            notationPack = ProjectionNotationPack(
                packId = ProjectionNotationPackId("electrical-notation/wiring/default-v1"),
                displayName = "Electrical Wiring Default",
                subjects = listOf(
                    ProjectionNotationSubject(
                        semanticId = StableSemanticIdentity("component:M1"),
                        symbolKey = ProjectionSymbolKey("device.wiring.default"),
                        labelPolicy = ProjectionLabelPolicy.SUBJECT_LABEL,
                        markerKeys = listOf("connectivity-device"),
                    ),
                    ProjectionNotationSubject(
                        semanticId = StableSemanticIdentity("component:PLC1"),
                        symbolKey = ProjectionSymbolKey("device.wiring.default"),
                        labelPolicy = ProjectionLabelPolicy.SUBJECT_LABEL,
                        markerKeys = listOf("connectivity-device"),
                    ),
                    ProjectionNotationSubject(
                        semanticId = StableSemanticIdentity("connection:PLC1.out->M1.in"),
                        symbolKey = ProjectionSymbolKey("connection.wiring.default"),
                        labelPolicy = ProjectionLabelPolicy.HIDDEN,
                        markerKeys = listOf("signal-flow"),
                    ),
                    ProjectionNotationSubject(
                        semanticId = StableSemanticIdentity("port:M1.in"),
                        symbolKey = ProjectionSymbolKey("port.wiring.default"),
                        labelPolicy = ProjectionLabelPolicy.TERMINAL_LABEL,
                    ),
                    ProjectionNotationSubject(
                        semanticId = StableSemanticIdentity("port:PLC1.out"),
                        symbolKey = ProjectionSymbolKey("port.wiring.default"),
                        labelPolicy = ProjectionLabelPolicy.TERMINAL_LABEL,
                    ),
                ),
            ),
            electricalAnchors = listOf(
                ElectricalAnchor(
                    anchorId = ElectricalAnchorId("wiring/projection/label/port_PLC1_out/anchor"),
                    portSemanticId = StableSemanticIdentity("port:PLC1.out"),
                    ownerSemanticId = StableSemanticIdentity("component:PLC1"),
                    nodeId = ProjectionNodeId("wiring/projection/node/component_PLC1"),
                    labelId = ProjectionLabelId("wiring/projection/label/port_PLC1_out"),
                    position = ProjectionPoint(x = 150, y = 81),
                    side = ElectricalAnchorSide.RIGHT,
                ),
                ElectricalAnchor(
                    anchorId = ElectricalAnchorId("wiring/projection/label/port_M1_in/anchor"),
                    portSemanticId = StableSemanticIdentity("port:M1.in"),
                    ownerSemanticId = StableSemanticIdentity("component:M1"),
                    nodeId = ProjectionNodeId("wiring/projection/node/component_M1"),
                    labelId = ProjectionLabelId("wiring/projection/label/port_M1_in"),
                    position = ProjectionPoint(x = 150, y = 160),
                    side = ElectricalAnchorSide.RIGHT,
                ),
            ),
            electricalConnectionEndpoints = listOf(
                ElectricalConnectionEndpoint(
                    endpointId = ElectricalConnectionEndpointId(
                        "wiring/projection/connection/connection_PLC1_out_M1_in/endpoint/source",
                    ),
                    projectionConnectionId = ProjectionConnectionId(
                        "wiring/projection/connection/connection_PLC1_out_M1_in",
                    ),
                    connectionSemanticId = StableSemanticIdentity("connection:PLC1.out->M1.in"),
                    endpointRole = ElectricalConnectionEndpointRole.SOURCE,
                    portSemanticId = StableSemanticIdentity("port:PLC1.out"),
                    anchorId = ElectricalAnchorId("wiring/projection/label/port_PLC1_out/anchor"),
                ),
                ElectricalConnectionEndpoint(
                    endpointId = ElectricalConnectionEndpointId(
                        "wiring/projection/connection/connection_PLC1_out_M1_in/endpoint/target",
                    ),
                    projectionConnectionId = ProjectionConnectionId(
                        "wiring/projection/connection/connection_PLC1_out_M1_in",
                    ),
                    connectionSemanticId = StableSemanticIdentity("connection:PLC1.out->M1.in"),
                    endpointRole = ElectricalConnectionEndpointRole.TARGET,
                    portSemanticId = StableSemanticIdentity("port:M1.in"),
                    anchorId = ElectricalAnchorId("wiring/projection/label/port_M1_in/anchor"),
                ),
            ),
            electricalRoutingCorridors = listOf(
                ElectricalRoutingCorridor(
                    corridorId = ElectricalRoutingCorridorId(
                        "wiring/projection/connection/connection_PLC1_out_M1_in/corridor",
                    ),
                    projectionConnectionId = ProjectionConnectionId(
                        "wiring/projection/connection/connection_PLC1_out_M1_in",
                    ),
                    connectionSemanticId = StableSemanticIdentity("connection:PLC1.out->M1.in"),
                    sourceAnchorId = ElectricalAnchorId("wiring/projection/label/port_PLC1_out/anchor"),
                    targetAnchorId = ElectricalAnchorId("wiring/projection/label/port_M1_in/anchor"),
                    routingStyle = ElectricalRoutingStyle.ORTHOGONAL,
                ),
            ),
        )
    }

    private fun cabinetViewDefinition(): ViewDefinition {
        return ViewDefinition(
            id = "cabinet",
            displayName = "Cabinet",
            layoutIntent = LayoutIntent.STRUCTURAL,
            groupingRules = listOf("group-by-owner", "group-by-component"),
            viewEmphasis = listOf(ViewEmphasis.OWNERSHIP, ViewEmphasis.PLACEMENT),
            description = "Highlights structural placement and ownership relationships for electrical devices.",
            ownershipContract = ProjectionOwnershipContract(
                interactivity = ProjectionInteractivity.INTERACTIVE,
                displayScopes = listOf(
                    "devices",
                    "ports",
                    "ownership-relationships",
                    "connectivity-relationships",
                    "grouped-placement",
                    "electrical-anchors",
                    "electrical-routing-corridors",
                ),
                semanticCommandIds = listOf(
                    "create-semantic-relationship",
                ),
                projectionCommandIds = listOf(
                    "adjust-layout-placement",
                    "adjust-layout-grouping",
                ),
                transientInteractionKinds = listOf(
                    "navigate-view",
                    "inspect-selection",
                    "preview-related-elements",
                ),
                persistedProjectionMetadataKeys = listOf(
                    "layout-placement",
                    "layout-group-membership",
                ),
            ),
            familyContract = ElectricalProjectionDescriptor(
                family = ElectricalProjectionFamily.CABINET,
            ),
        )
    }

    private fun wiringViewDefinition(): ViewDefinition {
        return ViewDefinition(
            id = "wiring",
            displayName = "Wiring",
            layoutIntent = LayoutIntent.CONNECTIVITY,
            groupingRules = listOf("group-by-signal", "group-by-connection-path"),
            viewEmphasis = listOf(ViewEmphasis.CONNECTIVITY, ViewEmphasis.SIGNAL_FLOW),
            description = "Highlights compatible signal flow and connection relationships between ports.",
            ownershipContract = ProjectionOwnershipContract(
                interactivity = ProjectionInteractivity.INSPECT_ONLY,
                displayScopes = listOf(
                    "devices",
                    "ports",
                    "signal-groups",
                    "connectivity-relationships",
                    "electrical-anchors",
                    "electrical-routing-corridors",
                ),
                transientInteractionKinds = listOf(
                    "navigate-view",
                    "inspect-selection",
                    "preview-related-elements",
                ),
            ),
            familyContract = ElectricalProjectionDescriptor(
                family = ElectricalProjectionFamily.WIRING,
            ),
        )
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
