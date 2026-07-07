package com.engineeringood.athena.plugin.host

import com.engineeringood.athena.plugin.AthenaCoreRuntime

/** Hosted plugin discovery facade that composes source enumeration with governed approval. */
class AthenaPluginDiscovery(
    private val candidateSource: AthenaPluginCandidateSource,
    private val approvalService: AthenaPluginApprovalService,
) {
    constructor(
        source: AthenaPluginSource = ServiceLoaderAthenaPluginSource(),
        validator: AthenaPluginValidator = AthenaPluginValidator(),
        runtime: AthenaCoreRuntime = AthenaCoreRuntime.current(),
    ) : this(
        candidateSource = AthenaPluginCandidateSource(source),
        approvalService = AthenaPluginApprovalService(validator, runtime),
    )

    /** Discovers local plugin candidates, approves compatible candidates, and returns the hosted inventory. */
    fun discover(): AthenaPluginDiscoveryReport {
        val sourceResult = candidateSource.enumerateCandidates()
        val approvalResult = approvalService.approve(
            candidates = sourceResult.candidates,
            rejectedCandidates = sourceResult.rejectedCandidates,
        )
        return AthenaPluginDiscoveryReport(
            runtime = approvalResult.runtime,
            candidates = sourceResult.candidates,
            rejectedCandidates = approvalResult.rejectedCandidates,
            approvedInventory = approvalResult.approvedInventory,
        )
    }
}
