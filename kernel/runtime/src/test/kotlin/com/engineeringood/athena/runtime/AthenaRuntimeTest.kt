package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.compiler.CompilerCompilationParseFailure
import com.engineeringood.athena.compiler.CompilerParseSuccess
import com.engineeringood.athena.plugin.AthenaCoreRuntime
import com.engineeringood.athena.plugin.host.AthenaHostedPluginInventorySnapshot
import com.engineeringood.athena.plugin.host.AthenaHostedPluginLifecycleSnapshot
import com.engineeringood.athena.plugin.host.AthenaHostedPluginLifecycleState
import com.engineeringood.athena.plugin.host.AthenaApprovedPluginInventory
import com.engineeringood.athena.plugin.host.AthenaPluginDiscoveryReport
import com.engineeringood.athena.renderer.svg.SvgRenderer
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AthenaRuntimeTest {
    @Test
    fun `opens a workspace without instantiating compiler or renderer services`() {
        var compilerCreations = 0
        var rendererCreations = 0
        val runtime = AthenaRuntime(
            serviceRegistry = AthenaServiceRegistry(
                compilerProvider = {
                    compilerCreations += 1
                    AthenaCompiler()
                },
                rendererProvider = {
                    rendererCreations += 1
                    SvgRenderer()
                },
            ),
        )

        val workspace = runtime.openWorkspace(Path.of("examples"))

        assertEquals(Path.of("examples"), workspace.rootPath)
        assertNull(workspace.activeProject)
        assertEquals(0, compilerCreations)
        assertEquals(0, rendererCreations)
    }

    @Test
    fun `activates a project and exposes a shared execution context`() {
        val compiler = AthenaCompiler()
        val renderer = SvgRenderer()
        val runtime = AthenaRuntime(
            serviceRegistry = AthenaServiceRegistry(
                compilerProvider = { compiler },
                rendererProvider = { renderer },
            ),
        )

        val workspace = runtime.openWorkspace(Path.of("examples"))
        val context = workspace.activateProject(
            projectName = "demo-cabinet",
            sourcePath = Path.of("examples/m0/demo-cabinet.athena"),
        )

        assertEquals("demo-cabinet", context.project.name)
        assertEquals(Path.of("examples/m0/demo-cabinet.athena"), context.project.sourcePath)
        assertSame(context, runtime.activeExecutionContext)
        assertSame(compiler, context.compiler())
        assertSame(renderer, context.renderer())
    }

    @Test
    fun `service registry resolves typed services deterministically`() {
        val compiler = AthenaCompiler()
        val renderer = SvgRenderer()
        val pluginServices = object : AthenaPluginRuntimeServices {
            override fun discoveryReport(): AthenaPluginDiscoveryReport {
                return AthenaPluginDiscoveryReport(
                    runtime = AthenaCoreRuntime.current(),
                    candidates = emptyList(),
                    rejectedCandidates = emptyList(),
                    approvedInventory = AthenaApprovedPluginInventory.EMPTY,
                )
            }

            override fun hostedPlugins(): List<AthenaHostedRuntimePlugin> = emptyList()

            override fun hostedLifecycle(): AthenaHostedPluginLifecycleSnapshot {
                return AthenaHostedPluginLifecycleSnapshot(
                    state = AthenaHostedPluginLifecycleState.INITIALIZED,
                    inventory = AthenaHostedPluginInventorySnapshot(
                        lifecycleState = AthenaHostedPluginLifecycleState.INITIALIZED,
                        candidateCount = 0,
                        approvedPluginCount = 0,
                        rejectedPluginCount = 0,
                        approvedPlugins = emptyList(),
                        rejectedCandidates = emptyList(),
                    ),
                )
            }

            override fun initializeHostedPlugins(): AthenaHostedPluginLifecycleSnapshot = hostedLifecycle()

            override fun shutdownHostedPlugins(): AthenaHostedPluginLifecycleSnapshot {
                return AthenaHostedPluginLifecycleSnapshot(
                    state = AthenaHostedPluginLifecycleState.SHUTDOWN,
                    inventory = hostedLifecycle().inventory.copy(
                        lifecycleState = AthenaHostedPluginLifecycleState.SHUTDOWN,
                    ),
                )
            }

            override fun domainSemanticsContributions(): List<AthenaRuntimePluginDomainSemanticsContribution> = emptyList()

            override fun commandContributions(): List<AthenaRuntimePluginCommandContribution> = emptyList()

            override fun renderContributions(): List<AthenaRuntimePluginRenderContribution> = emptyList()

            override fun viewDefinitionContributions(): List<AthenaRuntimePluginViewDefinitionContribution> = emptyList()

            override fun executeCommandContribution(
                context: AthenaExecutionContext,
                contributionId: String,
            ): AthenaRuntimePluginCommandExecution {
                return AthenaRuntimePluginCommandExecutionUnavailable(
                    contributionId = contributionId,
                    pluginId = "",
                    reason = "stub",
                )
            }

            override fun viewContributions(context: AthenaExecutionContext): List<AthenaRuntimePluginViewContribution> = emptyList()
        }
        val services = AthenaServiceRegistry(
            compilerProvider = { compiler },
            rendererProvider = { renderer },
            pluginRuntimeServicesProvider = { pluginServices },
        )

        assertSame(compiler, services.compiler())
        assertSame(compiler, services.compiler())
        assertSame(renderer, services.renderer())
        assertSame(services.engineeringGraph(), services.engineeringGraph())
        assertSame(services.commandRuntime(), services.commandRuntime())
        assertSame(services.aiProposalRuntime(), services.aiProposalRuntime())
        assertSame(pluginServices, services.pluginRuntimeServices())
    }

    @Test
    fun `active execution context resolves the shared engineering graph service`() {
        val runtime = AthenaRuntime()
        val workspace = runtime.openWorkspace(Path.of("examples"))
        val context = workspace.activateProject(
            projectName = "demo-cabinet",
            sourcePath = Path.of("examples/m0/demo-cabinet.athena"),
        )

        assertSame(runtime.serviceRegistry.engineeringGraph(), context.engineeringGraph())
        assertSame(runtime.serviceRegistry.commandRuntime(), context.commandRuntime())
        assertSame(runtime.serviceRegistry.aiProposalRuntime(), context.aiProposalRuntime())
    }

    @Test
    fun `active project can parse through runtime-owned context`() {
        val examplePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val runtime = AthenaRuntime()
        val workspace = runtime.openWorkspace(examplePath.parent)
        val context = workspace.activateProject(
            projectName = "demo-cabinet",
            sourcePath = examplePath,
        )

        val result = context.parseActiveProject()

        assertIs<CompilerParseSuccess>(result)
        assertSame(context, runtime.activeExecutionContext)
    }

    @Test
    fun `runtime-routed compilation surfaces syntax diagnostics deterministically`() {
        val brokenPath = Files.createTempFile("athena-runtime-broken-", ".athena")
        Files.writeString(
            brokenPath,
            """
                system Broken {
                  connect P1.out P2.in
                }
            """.trimIndent(),
        )

        try {
            val runtime = AthenaRuntime()
            val workspace = runtime.openWorkspace(brokenPath.parent)
            val context = workspace.activateProject(
                projectName = "broken",
                sourcePath = brokenPath,
            )

            val result = context.compileActiveProject()

            val failure = assertIs<CompilerCompilationParseFailure>(result)
            assertTrue(failure.diagnostics.isNotEmpty())
            assertTrue(failure.diagnostics.first().file.endsWith(brokenPath.fileName.toString()))
        } finally {
            Files.deleteIfExists(brokenPath)
        }
    }

    @Test
    fun `closing the runtime invalidates the previously opened workspace`() {
        val runtime = AthenaRuntime()
        val workspace = runtime.openWorkspace(Path.of("examples"))

        runtime.closeWorkspace()

        assertNull(runtime.activeWorkspace)
        assertNull(runtime.activeExecutionContext)
        assertFailsWith<IllegalStateException> {
            workspace.activateProject(
                projectName = "demo-cabinet",
                sourcePath = Path.of("examples/m0/demo-cabinet.athena"),
            )
        }
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
