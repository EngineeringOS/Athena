package com.engineeringood.athena.compiler

import com.engineeringood.athena.renderer.svg.SvgRenderModel

/** Compiler-visible render contribution attribution selected for one downstream render target. */
data class CompilerRenderContributionAttribution(
    val pluginId: String,
    val contributionId: String,
    val viewIds: Set<String> = emptySet(),
    val rendererTargets: Set<String> = emptySet(),
)

/** Compiler-facing rendering outcome derived from the downstream rendering stage. */
sealed interface CompilerRenderingResult

/** Successful rendering result that exposes both the thin render model and emitted SVG. */
data class CompilerRenderingSuccess(
    val model: SvgRenderModel,
    val svg: String,
    val viewId: String = "",
    val rendererTarget: String = "",
    val activeRenderContributions: List<CompilerRenderContributionAttribution> = emptyList(),
) : CompilerRenderingResult

/** Blocked rendering result that points back to the upstream pass that prevented emission. */
data class CompilerRenderingBlocked(
    val reason: String,
    val blockedByPass: CompilerPassId,
) : CompilerRenderingResult
