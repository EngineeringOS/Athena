package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.renderer.svg.SvgRenderer

/** Runtime-owned typed registry for platform capabilities needed by the active execution context. */
class AthenaServiceRegistry(
    compilerProvider: (() -> AthenaCompiler)? = null,
    rendererProvider: () -> SvgRenderer = { SvgRenderer() },
    pluginRuntimeServicesProvider: (() -> AthenaPluginRuntimeServices)? = null,
) {
    private val pluginRuntimeServicesInstance by lazy(LazyThreadSafetyMode.NONE) {
        pluginRuntimeServicesProvider?.invoke() ?: AthenaHostedPluginRuntimeServices()
    }
    private val compilerInstance by lazy(LazyThreadSafetyMode.NONE) {
        compilerProvider?.invoke() ?: AthenaCompiler(
            hostedPluginDiscoveryReport = pluginRuntimeServicesInstance.discoveryReport(),
            hostedDomainPlugins = pluginRuntimeServicesInstance.domainSemanticsContributions()
                .map { contribution -> contribution.domainPlugin },
        )
    }
    private val rendererInstance by lazy(LazyThreadSafetyMode.NONE, rendererProvider)
    private val engineeringGraphInstance by lazy(LazyThreadSafetyMode.NONE) { AthenaEngineeringGraphService() }
    private val commandRuntimeInstance by lazy(LazyThreadSafetyMode.NONE) { AthenaCommandRuntimeService() }
    private val aiProposalRuntimeInstance by lazy(LazyThreadSafetyMode.NONE) { AthenaAiProposalRuntimeService() }

    /** Resolves the shared compiler capability for the current runtime. */
    fun compiler(): AthenaCompiler = compilerInstance

    /** Resolves the shared SVG renderer capability for the current runtime. */
    fun renderer(): SvgRenderer = rendererInstance

    /** Resolves the shared engineering-graph capability for the current runtime. */
    fun engineeringGraph(): AthenaEngineeringGraphService = engineeringGraphInstance

    /** Resolves the shared command-runtime capability for the current runtime. */
    fun commandRuntime(): AthenaCommandRuntimeService = commandRuntimeInstance

    /** Resolves the shared optional AI proposal capability for the current runtime. */
    fun aiProposalRuntime(): AthenaAiProposalRuntimeService = aiProposalRuntimeInstance

    /** Resolves shared plugin-related runtime services without exposing compiler-owned plugin internals. */
    fun pluginRuntimeServices(): AthenaPluginRuntimeServices = pluginRuntimeServicesInstance
}
