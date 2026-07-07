---
baseline_commit: ad382d8a2d1841771c5b95e008f29f78a6f751cd
---

# Story 2.3: Refresh Projection State After One Supported Semantic Mutation Path

Status: done

## Story

As an operator or reviewer,
I want Athena to refresh only affected projection scope after the existing `connect ports` command path,
so that M2 proves incremental multi-view behavior without overcommitting to arbitrary layout editing.

## Acceptance Criteria

1. Given the current runtime already supports the GUI-backed `connect ports` semantic command from M1, when that `connect ports` command path changes canonical semantic state, then Athena identifies affected semantic identities and relationships as the basis for projection refresh, and the first M2 proof is explicitly limited to the `connect ports` mutation path rather than generic all-command incremental guarantees.
2. Given affected semantic scope has been identified after the `connect ports` command path, when projection refresh is triggered, then Athena recomputes only the affected `Layout IR` and `Geometry IR` scope where dependency information allows it, and unaffected views or unaffected projection regions are not required to rebuild blindly in every case.
3. Given refreshed geometry has been derived after the supported semantic mutation, when downstream outputs are updated, then the runtime refreshes rendered output from the updated geometry-backed projection, and the refresh remains explainable through runtime-owned projection metadata rather than hidden viewer caches.
4. Given supported mutation refresh is implemented, when the standard Java `25` build and runtime checks are executed, then the workspace builds successfully and the `connect ports` command path demonstrates dependency-scoped projection refresh, and the implementation preserves the M2 limit that projection refresh is proven narrowly first against one concrete mutation path.

## Tasks / Subtasks

- [x] Extend the compiler/runtime incremental refresh contract for the first supported mutation proof. (AC: 1, 3, 4)
  - [x] Expand the inspectable incremental update report to describe scoped layout, geometry, and downstream refresh behavior in addition to semantic scope.
  - [x] Keep the public report honest: it must only report scoped refresh where the implementation actually reuses unaffected projection scope.
  - [x] Keep all newly introduced core Kotlin classes documented with KDoc.
- [x] Implement dependency-scoped `Layout IR` refresh for the `connect ports` path. (AC: 1, 2, 4)
  - [x] Reuse the existing `changedSemanticIds` command result and `planAffectedScope(...)` basis from runtime-owned mutation handling.
  - [x] Support scoped layout refresh only where the current dependency information is trustworthy for the `connect ports` mutation path.
  - [x] Fall back explicitly when scoped layout refresh is not safe rather than pretending all mutations are incremental.
- [x] Implement dependency-scoped `Geometry IR` and downstream render refresh for the same path. (AC: 2, 3, 4)
  - [x] Reuse unaffected geometry/render regions where dependency information allows it.
  - [x] Keep refreshed active projections and runtime viewer output aligned with the post-command canonical semantic state.
  - [x] Preserve runtime-owned projection session and desktop consumption contracts from Stories `2.1` and `2.2`.
- [x] Add deterministic regression tests for scoped projection refresh. (AC: 1, 2, 3, 4)
  - [x] Add runtime tests proving the `connect ports` command produces scoped refresh metadata and preserves unaffected projection objects where expected.
  - [x] Add tests proving runtime-owned projection/viewer outputs refresh from the updated geometry-backed state after the supported mutation.
  - [x] Keep existing command history, semantic diff, desktop viewer, and projection-session tests green.
- [x] Update module documentation for the first scoped refresh boundary. (AC: 4)
  - [x] Update `:kernel:compiler` docs to describe the narrow scoped refresh contract.
  - [x] Update `:kernel:runtime` docs to describe runtime-owned incremental refresh metadata and the connect-ports-only limit.

## Dev Notes

### Story Intent

- Story `2.3` is the first proof that runtime-owned semantic mutation can trigger scoped multi-view projection refresh.
- The proof is intentionally narrow: `connect ports` only.
- Success is not “everything is incremental”; success is a real, inspectable scoped refresh for one governed mutation path.

### Architecture Guardrails

- Align to `ARCHITECTURE-SPINE.md`:
  - `AD-2`: deterministic projection derivation remains a compiler concern.
  - `AD-3`: runtime owns projection refresh coordination and explainable refresh metadata.
  - `AD-5`: canonical semantic identity must remain stable across semantic, layout, geometry, and refresh inspection.
  - `AD-6`: downstream viewers/renderers consume updated geometry-backed projection output rather than re-deriving semantics privately.
  - `AD-7`: projection refresh remains an inspection consequence, not a geometry-authoring path.
  - `AD-8`: incremental work remains dependency-scoped and runtime-triggered.
- Align to `manifesto/docs/architecture/09-layout-and-geometry.md`: semantic truth stays upstream, layout remains presentation intent, and geometry remains renderable consequence.

### Technical Requirements

- Reuse the existing mutation basis already present in runtime:
  - `AthenaConnectPortsCommand`
  - `changedSemanticIds`
  - `EngineeringDocument.planAffectedScope(...)`
- Strengthen the current proof so scoped refresh is visible at all relevant layers:
  - semantic scope
  - layout scope
  - geometry scope
  - downstream runtime/render refresh mode
- Keep the proof honest:
  - scoped only when safe
  - fallback when unsafe
  - no claim of generic arbitrary-command incrementality
- Preserve the current command history and semantic diff capture model.

### Architecture Compliance

- `:kernel:runtime` coordinates refresh after a command mutation but does not take over compiler-owned derivation rules.
- `:kernel:compiler` may add scoped layout/geometry derivation or reuse helpers, but canonical semantic state still comes from the runtime-owned document plus compiler derivation.
- Desktop/UI modules must continue consuming runtime-owned refreshed outputs instead of inventing viewer-local refresh logic.
- Any scoped reuse must preserve canonical semantic ids for reused nodes/elements and for newly created projection consequences.

### Library / Framework Requirements

- Use the existing repo-pinned stack:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- Keep dependency changes minimal and local to existing compiler/runtime tests unless absolutely required.
- Reuse the current Kotlin/JUnit test approach.

### File Structure Requirements

- Likely update files:
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/LayoutIrDeriver.kt`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/GeometryIrDeriver.kt`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SvgRenderModelDeriver.kt`
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt`
  - `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeTest.kt`
  - `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSessionTest.kt`
  - `apps/desktop-viewer/src/test/kotlin/com/engineeringood/athena/apps/composeviewer/AthenaComposeViewerWorkbenchSessionTest.kt`
  - `kernel/compiler/README.md`
  - `kernel/compiler/README.zh-CN.md`
  - `kernel/runtime/README.md`
  - `kernel/runtime/README.zh-CN.md`
- Likely add files:
  - no new production module is expected for this story
- Preserve existing behavior in:
  - `AthenaCommandRuntimeService` command history and diff hooks
  - `AthenaRuntimeProjectionSession` ownership
  - desktop view switching and viewer inspection from Story `2.2`

### Testing Requirements

- Minimum verification commands:
  - `java25`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test`
  - `.\gradlew.bat --no-daemon --console=plain :apps:desktop-viewer:test`
  - `.\gradlew.bat --no-daemon --console=plain build`
- Required proof checks:
  - `connect ports` reports changed semantic ids and scoped refresh metadata
  - unaffected layout or geometry regions are reused where the current proof allows it
  - refreshed runtime projection/viewer output reflects the new connection
  - command history and semantic diff remain intact
  - desktop/runtime consumers continue to read refreshed outputs through runtime-owned contracts
- Keep Gradle verification sequential on Windows.

### Previous Story Intelligence

- Story `2.1` introduced runtime-owned projection sessions and active-view switching.
- Story `2.2` moved desktop consumption onto runtime-owned projection sessions and preserved canonical semantic selection across views.
- Current runtime already computes `changedSemanticIds`, `planAffectedScope(...)`, and a scoped validation/rendering report after `connect ports`, but `Layout IR` and `Geometry IR` derivation still need a real scoped proof instead of broad regeneration.
- The current `SvgRenderModelDeriver.deriveIncremental(...)` already reuses unaffected render-model regions when geometry is stable enough. Story `2.3` should extend that honesty backward into layout and geometry scope.

### Git Intelligence Summary

- `ad382d8 Complete M1 runtime workspace and regroup modules` remains the current baseline commit.
- The M2 sequence already established explicit view definitions, layout derivation, geometry derivation, runtime projection sessions, and desktop projection inspection.
- Story `2.3` should connect the existing `connect ports` command path to an explainable scoped projection refresh proof without pretending the whole platform is now a fully incremental editor.

### References

- `_bmad-output/planning-artifacts/epics-M2-2026-07-06.md`
  - `Story 2.3: Refresh Projection State After One Supported Semantic Mutation Path`
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-06/prd.md`
  - `FR-7`
  - `FR-8`
  - `FR-9`
  - `NFR-5`
  - `NFR-6`
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-06/ARCHITECTURE-SPINE.md`
  - `AD-2`
  - `AD-3`
  - `AD-5`
  - `AD-6`
  - `AD-7`
  - `AD-8`
- `manifesto/docs/architecture/09-layout-and-geometry.md`
- `_bmad-output/implementation-artifacts/m2/2-1-host-runtime-projection-sessions-and-supported-view-switching.md`
- `_bmad-output/implementation-artifacts/m2/2-2-expose-runtime-owned-projection-snapshots-to-the-desktop-surface.md`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeService.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/LayoutIrDeriver.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/GeometryIrDeriver.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SvgRenderModelDeriver.kt`

## Story Completion Status

- Status: done
- Completion note: Scoped projection refresh now proves the `connect ports` mutation path end to end across layout, geometry, render-model reuse, runtime reports, and desktop/runtime consumers.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Story context creation inputs:
  - `git rev-parse HEAD`
  - `git log -5 --pretty=format:"%h %s"`
- Verification:
  - `java25; .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test`
  - `java25; .\gradlew.bat --no-daemon --console=plain :apps:desktop-viewer:test`
  - `java25; .\gradlew.bat --no-daemon --console=plain build`

### Completion Notes List

- Extended compiler and runtime incremental reports with explicit layout, geometry, and rendering scope metadata.
- Added scoped layout and geometry merge paths that reuse unchanged projection objects and fall back honestly when scoped reuse is not safe.
- Relaxed the render-model incremental path so the `cabinet` view can stay scoped when a new connection is introduced.
- Kept runtime projection sessions and desktop session diagnostics aligned with the post-command canonical state after `connect ports`.
- Rewrote the Chinese compiler/runtime README files to remove mojibake and document the Story `2.3` boundary cleanly.

## File List

- `_bmad-output/implementation-artifacts/m2/2-3-refresh-projection-state-after-one-supported-semantic-mutation-path.md`
- `_bmad-output/implementation-artifacts/m2/sprint-status.yaml`
- `apps/desktop-viewer/src/main/kotlin/com/engineeringood/athena/apps/composeviewer/AthenaComposeViewerWorkbenchSession.kt`
- `kernel/compiler/README.md`
- `kernel/compiler/README.zh-CN.md`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/GeometryIrDeriver.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/LayoutIrDeriver.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SvgRenderModelDeriver.kt`
- `kernel/runtime/README.md`
- `kernel/runtime/README.zh-CN.md`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeTest.kt`

## Change Log

- 2026-07-07: Created Story `2.3` with scoped projection refresh implementation guidance.
- 2026-07-07: Implemented scoped layout, geometry, and render refresh for the `connect ports` runtime mutation path and verified the workspace on Java `25`.
