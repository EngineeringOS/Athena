package com.engineeringood.athena.compiler

import com.engineeringood.athena.renderer.svg.SvgRenderModel

/** Compiler-facing rendering outcome derived from the downstream rendering stage. */
sealed interface CompilerRenderingResult

/** Successful rendering result that exposes both the thin render model and emitted SVG. */
data class CompilerRenderingSuccess(
    val model: SvgRenderModel,
    val svg: String,
) : CompilerRenderingResult

/** Blocked rendering result that points back to the upstream pass that prevented emission. */
data class CompilerRenderingBlocked(
    val reason: String,
    val blockedByPass: CompilerPassId,
) : CompilerRenderingResult
