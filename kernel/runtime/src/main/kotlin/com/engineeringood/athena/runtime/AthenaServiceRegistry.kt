package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.compiler.defaultAthenaKnowledgePackageSource

/** Runtime-owned typed registry for platform capabilities needed by the active execution context. */
class AthenaServiceRegistry(
    compilerProvider: (() -> AthenaCompiler)? = null,
    pluginRuntimeServicesProvider: (() -> AthenaPluginRuntimeServices)? = null,
    semanticBaselineServiceProvider: (() -> AthenaSemanticBaselineService)? = null,
    semanticDiffServiceProvider: (() -> AthenaSemanticDiffService)? = null,
    semanticReviewServiceProvider: (() -> AthenaSemanticReviewService)? = null,
    semanticCommitServiceProvider: (() -> AthenaSemanticCommitService)? = null,
    semanticScmStateServiceProvider: (() -> AthenaSemanticScmStateService)? = null,
    semanticHistoryStateServiceProvider: (() -> AthenaSemanticHistoryStateService)? = null,
) {
    private val pluginRuntimeServicesInstance by lazy(LazyThreadSafetyMode.NONE) {
        pluginRuntimeServicesProvider?.invoke() ?: AthenaHostedPluginRuntimeServices()
    }
    private val compilerInstance by lazy(LazyThreadSafetyMode.NONE) {
        compilerProvider?.invoke() ?: AthenaCompiler(
            knowledgePackageSource = defaultAthenaKnowledgePackageSource(),
            hostedPluginDiscoveryReport = pluginRuntimeServicesInstance.discoveryReport(),
            hostedDomainPlugins = pluginRuntimeServicesInstance.domainSemanticsContributions()
                .map { contribution -> contribution.domainPlugin },
        )
    }
    private val engineeringGraphInstance by lazy(LazyThreadSafetyMode.NONE) { AthenaEngineeringGraphService() }
    private val repositoryReportInstance by lazy(LazyThreadSafetyMode.NONE) {
        AthenaRepositoryReportService(::compiler)
    }
    private val semanticBaselineServiceInstance by lazy(LazyThreadSafetyMode.NONE) {
        semanticBaselineServiceProvider?.invoke() ?: AthenaSemanticBaselineService()
    }
    private val semanticDiffServiceInstance by lazy(LazyThreadSafetyMode.NONE) {
        semanticDiffServiceProvider?.invoke() ?: AthenaSemanticDiffService()
    }
    private val semanticReviewServiceInstance by lazy(LazyThreadSafetyMode.NONE) {
        semanticReviewServiceProvider?.invoke() ?: AthenaSemanticReviewService(
            diffService = semanticDiffServiceInstance,
            pluginRuntimeServices = pluginRuntimeServicesInstance,
        )
    }
    private val semanticCommitServiceInstance by lazy(LazyThreadSafetyMode.NONE) {
        semanticCommitServiceProvider?.invoke() ?: AthenaSemanticCommitService(
            reviewService = semanticReviewServiceInstance,
        )
    }
    private val semanticScmStateServiceInstance by lazy(LazyThreadSafetyMode.NONE) {
        semanticScmStateServiceProvider?.invoke() ?: AthenaSemanticScmStateService(
            baselineService = semanticBaselineServiceInstance,
            reviewService = semanticReviewServiceInstance,
            commitService = semanticCommitServiceInstance,
        )
    }
    private val semanticHistoryStateServiceInstance by lazy(LazyThreadSafetyMode.NONE) {
        semanticHistoryStateServiceProvider?.invoke() ?: AthenaSemanticHistoryStateService(
            baselineService = semanticBaselineServiceInstance,
            diffService = semanticDiffServiceInstance,
        )
    }

    /** Resolves the shared compiler capability for the current runtime. */
    fun compiler(): AthenaCompiler = compilerInstance

    /** Resolves the shared engineering-graph capability for the current runtime. */
    fun engineeringGraph(): AthenaEngineeringGraphService = engineeringGraphInstance

    /** Resolves the shared repository-report capability for the current runtime. */
    fun repositoryReports(): AthenaRepositoryReportService = repositoryReportInstance

    /** Resolves the shared semantic baseline capability for the current runtime. */
    fun semanticBaselines(): AthenaSemanticBaselineService = semanticBaselineServiceInstance

    /** Resolves the shared semantic diff capability for the current runtime. */
    fun semanticDiffs(): AthenaSemanticDiffService = semanticDiffServiceInstance

    /** Resolves the shared semantic review capability for the current runtime. */
    fun semanticReviews(): AthenaSemanticReviewService = semanticReviewServiceInstance

    /** Resolves the shared semantic commit capability for the current runtime. */
    fun semanticCommits(): AthenaSemanticCommitService = semanticCommitServiceInstance

    /** Resolves the shared semantic SCM projection capability for the current runtime. */
    fun semanticScmStates(): AthenaSemanticScmStateService = semanticScmStateServiceInstance

    /** Resolves the shared semantic history projection capability for the current runtime. */
    fun semanticHistoryStates(): AthenaSemanticHistoryStateService = semanticHistoryStateServiceInstance

    /** Resolves shared plugin-related runtime services without exposing compiler-owned plugin internals. */
    fun pluginRuntimeServices(): AthenaPluginRuntimeServices = pluginRuntimeServicesInstance
}
