# `:kernel:runtime`

English | [Chinese (Simplified)](README.zh-CN.md)

The `:kernel:runtime` module owns Athena's long-lived execution boundary. It manages workspace lifecycle, active project context, runtime service resolution, projection sessions, command execution, history, graph projection, hosted plugin lifecycle inspection and execution, and optional AI proposal review without becoming a second semantic authority.

## Responsibilities

- Open and close workspaces through `AthenaRuntime`.
- Activate projects into a shared `AthenaExecutionContext`.
- Resolve runtime-owned services such as graph, command, plugin, and renderer coordination.
- Resolve runtime-owned semantic baseline, semantic diff, semantic review, and semantic commit services over the active `RepositoryGraphSession`.
- Resolve runtime-owned semantic SCM projection state that combines baseline diagnostics, semantic review, and commit-preparation output for downstream IDE seams.
- Resolve runtime-owned semantic history projection state that combines baseline-sequence diagnostics and package-evolution summaries for downstream IDE seams.
- Carry current fixed knowledge-pack activation into runtime-facing compiler sessions so IDE and mutation flows can see M9 knowledge outputs.
- Consume the governed approved plugin inventory from `:kernel:plugins:plugin-host`.
- Expose runtime-visible plugin lifecycle inspection without handing orchestration ownership to plugins.
- Apply hosted semantic review enrichments after core review generation while preserving core review entries as the semantic authority.
- Host runtime-owned projection sessions with supported-view discovery and active-view switching.
- Consume compiler-derived `:kernel:projection-model` documents as the primary graphical projection input.
- Publish projection-family ids, sheet state, notation packs, and cross references through the same runtime-owned projection session.
- Keep canonical runtime state aligned with `Engineering IR`.
- Host command history, undo, redo, replay, diff inspection, and accepted AI proposal flow.
- Publish runtime-visible incremental refresh metadata after supported semantic mutations.
- Publish additive engineering sufficiency diagnostics and impact consequences through existing runtime-owned mutation inspection surfaces.

## Main Types

- `AthenaRuntime`
- `AthenaWorkspace`
- `AthenaExecutionContext`
- `AthenaServiceRegistry`
- `AthenaSemanticBaselineService`
- `AthenaSemanticDiffService`
- `AthenaSemanticReviewService`
- `AthenaSemanticCommitService`
- `AthenaSemanticScmStateService`
- `AthenaSemanticHistoryStateService`
- `AthenaRuntimeProjectionSession`
- `AthenaCommandRuntimeService`
- `AthenaEngineeringGraphService`

## Dependencies

- `:kernel:compiler`
- `:kernel:projection-model`
- `:kernel:plugins:plugin-host`
- `:kernel:engineering-model`
- `:kernel:svg-renderer`

## Boundaries

This module does not parse DSL source text directly, define the canonical IR schema, or own domain semantics. It owns runtime lifecycle and orchestration above those lower layers. Projection sessions remain runtime-owned state over compiler-derived `:kernel:projection-model` artifacts; switching views does not mutate canonical engineering semantics. Repeated references and cross references remain downstream projection evidence over canonical semantic ids; runtime transports them but does not become their semantic owner.

## Incremental Refresh Boundary

Story `2.3` keeps the runtime contract intentionally narrow:

- The first scoped refresh proof is limited to the existing `connect ports` command path.
- `AthenaExecutionContext.incrementalUpdateReport()` exposes semantic scope plus layout, geometry, and rendering refresh modes.
- Runtime owns refresh coordination and active projection replacement, but compiler still owns every derivation rule.
- Desktop and other consumers read refreshed projection state through runtime-owned projections rather than maintaining private view caches.
- If compiler reuse is not safe, runtime surfaces the fallback mode instead of masking it.

## Review Contract

Story `2.4` keeps semantic review primary while making projection refresh inspectable:

- `AthenaSemanticDiffInspection` remains anchored in canonical semantic ids and command-linked history consequences.
- Projection refresh evidence is attached as downstream consequence metadata, not as a second history or diff system.
- Runtime inspection may explain affected views and downstream layers, but it does not replace semantic change review with geometry-only review.
- Runtime source-mutation inspection may now carry engineering sufficiency diagnostics plus typed impact consequences, but those remain additive evidence and not a second review subsystem.
- Accepted mutation review now reuses the same semantic review and commit vocabulary to distinguish direct engineering edits from downstream affected subjects through typed engineering-impact entries.
- Repository baseline comparison now stays on the runtime-owned JVM path and publishes compiler-derived validation plus repository-contract consequences instead of frontend-guessed fallout.
- Runtime review publication now reuses the same baseline/diff path and emits typed review entries for affected packages, authored intent categories, derived consequences, validation impact, and degraded input warnings.
- Runtime review publication now also carries typed engineering-impact consequences so SCM and accepted-mutation review can inspect the same affected-subject consequence contract as Problems and semantic inspection.
- Hosted review enrichment now stays additive on the same JVM path: approved plugins can append deterministic labels, hints, and summaries, but they cannot suppress or rewrite core review entries.
- Runtime commit publication now reuses the same baseline/diff/review path and emits typed commit-intent entries for adapter-ready commit preparation without leaking staging or provider nouns into the kernel.
- Runtime semantic SCM projection now reuses the same baseline/diff/review/commit path and publishes one typed state object for LSP and workbench consumers instead of making frontend code reconstruct review/commit meaning.
- Runtime semantic history projection now reuses the same baseline/diff path plus the kernel history summarizer and publishes one typed package-history state object for LSP and workbench consumers.
- Incomplete comparison inputs remain inspectable through typed semantic consequence records and attached diagnostics rather than opaque UI-only failure text.

## Verification

```bash
./gradlew :kernel:runtime:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test
```

