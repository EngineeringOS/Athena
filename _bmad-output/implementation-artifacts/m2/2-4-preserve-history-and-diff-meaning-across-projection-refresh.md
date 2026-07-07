---
baseline_commit: ad382d8a2d1841771c5b95e008f29f78a6f751cd
---

# Story 2.4: Preserve History And Diff Meaning Across Projection Refresh

Status: done

## Story

As a reviewer,
I want history and diff inspection to stay anchored in canonical semantics even when projections refresh,
so that projection consequences remain explainable without replacing semantic change review with geometry-only change review.

## Acceptance Criteria

1. Given a supported semantic mutation has refreshed layout, geometry, and rendered projection state, when a reviewer inspects the change through runtime-owned history or diff tools, then Athena still presents the semantic change as the primary explanation of what changed, and projection updates are represented as downstream consequences of that semantic mutation.
2. Given a command-backed semantic change affects objects visible across multiple views, when history or diff inspection is requested, then Athena can show which canonical semantic identities changed and how those identities map to refreshed projection consequences, and the inspection path remains rooted in runtime-owned command and diff facilities rather than UI-local state.
3. Given projection refresh occurs after the first supported mutation path, when a reviewer compares before and after state, then the system can distinguish semantic changes from layout or geometry consequences, and projection refresh does not create a second competing history system.
4. Given history and diff preservation is implemented, when the standard Java `25` build and runtime checks are executed, then the workspace builds successfully and reviewers can inspect semantic change plus projection consequence together, and the implementation preserves canonical ownership across projection refresh.

## Tasks / Subtasks

- [x] Extend runtime-owned diff inspection models with explicit projection consequence reporting. (AC: 1, 2, 3, 4)
  - [x] Add typed projection consequence models that describe refreshed views, affected semantic identities, and downstream layer modes without redefining semantic history.
  - [x] Keep semantic entries and command-linked history consequences as the primary explanation surface.
  - [x] Keep all new core Kotlin runtime classes documented with KDoc.
- [x] Feed scoped projection refresh evidence into semantic diff inspection for command and history operations. (AC: 1, 2, 3, 4)
  - [x] Reuse the existing runtime incremental refresh metadata emitted after `connect ports`, `undo`, `redo`, and `replay`.
  - [x] Attach projection consequences to runtime diff inspection without inventing a projection-local history log.
  - [x] Preserve canonical semantic ids as the bridge between semantic changes and projection consequences.
- [x] Expose the enriched inspection meaning through runtime and desktop consumption paths. (AC: 1, 2, 3, 4)
  - [x] Keep `AthenaCommandRuntimeService` inspection APIs rooted in canonical history and semantic diff.
  - [x] Surface projection consequence evidence in desktop/runtime diagnostics or inspector text without letting UI state become authoritative.
  - [x] Keep the explainability boundary honest: semantic change first, projection consequence second.
- [x] Add deterministic regression tests for history and diff preservation. (AC: 1, 2, 3, 4)
  - [x] Extend runtime tests to prove the latest inspection and command consequence inspection include projection consequence evidence after `connect ports`.
  - [x] Extend undo/redo/replay tests to prove history status changes remain semantic-primary while projection consequences stay attached and inspectable.
  - [x] Keep existing projection refresh, command history, and desktop session tests green.
- [x] Update module documentation for the preserved semantic-first review contract. (AC: 4)
  - [x] Update `:kernel:runtime` docs to describe semantic diff plus projection consequence inspection.
  - [x] Update desktop-facing docs only where the runtime-owned inspection contract actually changed.

## Dev Notes

### Story Intent

- Story `2.4` is not a new history system.
- It proves that projection refresh remains a downstream review consequence attached to the existing semantic diff and command history path.
- Success is not “inspect geometry changes in isolation.” Success is “semantic change remains primary, projection consequence becomes inspectable evidence.”

### Architecture Guardrails

- Align to `ARCHITECTURE-SPINE.md`:
  - `AD-3`: runtime owns projection refresh coordination and inspection surfaces.
  - `AD-5`: canonical semantic identity survives across semantic, layout, geometry, and projection consequence reporting.
  - `AD-6`: downstream surfaces consume runtime-owned projection state rather than rebuilding semantics privately.
  - `AD-7`: projection layers remain inspectable, not authoritative mutation sources.
  - `AD-8`: incremental work remains dependency-scoped and runtime-triggered.
- Align to `manifesto/docs/architecture/09-layout-and-geometry.md`: layout and geometry remain downstream consequences rather than semantic truth.

### Technical Requirements

- Reuse the current runtime-owned facilities already in place:
  - `AthenaSemanticDiffInspection`
  - `buildSemanticDiffInspection(...)`
  - `AthenaCommandRuntimeService.inspectCommandHistoryConsequence(...)`
  - `AthenaExecutionContext.incrementalUpdateReport()`
  - Story `2.3` scoped refresh metadata
- Keep the contract honest:
  - semantic diff entries remain the primary review artifact
  - history consequences remain command-linked
  - projection consequences are attached evidence, not a parallel diff or history model
- Cover all current history paths that already build semantic inspections:
  - command execution
  - undo
  - redo
  - replay

### Architecture Compliance

- `:kernel:runtime` owns the semantic diff and history inspection contract.
- `:kernel:compiler` should not gain a second review-oriented diff model for this story; it already exposes refresh metadata.
- Desktop/UI code may display the enriched runtime-owned inspection result, but it may not invent projection consequence meaning itself.
- Any new consequence model must remain anchored to canonical semantic ids and supported view ids.

### Library / Framework Requirements

- Use the existing repo-pinned stack:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- Keep dependency changes at zero unless an existing module contract truly cannot express the story.
- Reuse the current Kotlin/JUnit test approach.

### File Structure Requirements

- Likely update files:
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticDiffInspection.kt`
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt`
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeService.kt`
  - `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSemanticDiffInspectionTest.kt`
  - `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaCommandHistoryTest.kt`
  - `apps/desktop-viewer/src/main/kotlin/com/engineeringood/athena/apps/composeviewer/AthenaComposeViewerWorkbenchSession.kt`
  - `apps/desktop-viewer/src/test/kotlin/com/engineeringood/athena/apps/composeviewer/AthenaComposeViewerWorkbenchSessionTest.kt`
  - `kernel/runtime/README.md`
  - `kernel/runtime/README.zh-CN.md`
- Preserve existing behavior in:
  - runtime-owned command history status transitions
  - runtime-owned latest semantic diff inspection
  - projection refresh behavior proven in Story `2.3`

### Testing Requirements

- Minimum verification commands:
  - `java25`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test`
  - `.\gradlew.bat --no-daemon --console=plain :apps:desktop-viewer:test`
  - `.\gradlew.bat --no-daemon --console=plain build`
- Required proof checks:
  - latest semantic diff inspection includes semantic entries plus projection consequence evidence
  - command consequence inspection stays semantic-primary while exposing projection consequences
  - undo, redo, and replay inspections keep history meaning intact
  - desktop/runtime consumers continue reading inspection meaning through runtime-owned contracts
- Keep Gradle verification sequential on Windows.

### Previous Story Intelligence

- Story `2.3` already proved scoped refresh and expanded incremental runtime metadata with layout, geometry, and rendering modes.
- Current semantic diff inspection still centers semantic entries and command history consequences only.
- Story `2.4` should bridge those two facts by attaching projection consequence evidence to the existing runtime inspection artifact instead of inventing a separate review channel.

### Git Intelligence Summary

- `ad382d8 Complete M1 runtime workspace and regroup modules` remains the current baseline commit.
- The current M2 sequence already established explicit view definitions, runtime projection sessions, desktop inspection, and scoped refresh.
- Story `2.4` should preserve the semantic-first review model while making scoped projection refresh consequences visible and explainable.

### References

- `_bmad-output/planning-artifacts/epics-M2-2026-07-06.md`
  - `Story 2.4: Preserve History And Diff Meaning Across Projection Refresh`
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-06/prd.md`
  - `FR-7`
  - `FR-8`
  - `FR-9`
  - `NFR-3`
  - `NFR-4`
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-06/ARCHITECTURE-SPINE.md`
  - `AD-3`
  - `AD-5`
  - `AD-6`
  - `AD-7`
  - `AD-8`
- `manifesto/docs/architecture/09-layout-and-geometry.md`
- `_bmad-output/implementation-artifacts/m2/2-3-refresh-projection-state-after-one-supported-semantic-mutation-path.md`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticDiffInspection.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeService.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt`

## Story Completion Status

- Status: done
- Completion note: Semantic diff inspection now carries runtime-owned projection refresh consequences while keeping canonical semantic change and command history as the primary review model.

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

- Added typed runtime-owned projection refresh consequence models to `AthenaSemanticDiffInspection`.
- Wired command, undo, redo, replay, and history consequence inspection to attach downstream projection evidence without creating a second history system.
- Kept semantic entries and command-linked history consequences as the primary explanation path while exposing projection consequence summaries to the desktop session.
- Extended runtime tests to prove the enriched inspection contract across command execution, undo, and replay.
- Updated runtime module docs in English and Simplified Chinese for the semantic-first review contract.

## File List

- `_bmad-output/implementation-artifacts/m2/2-4-preserve-history-and-diff-meaning-across-projection-refresh.md`
- `_bmad-output/implementation-artifacts/m2/sprint-status.yaml`
- `apps/desktop-viewer/src/main/kotlin/com/engineeringood/athena/apps/composeviewer/AthenaComposeViewerWorkbenchSession.kt`
- `apps/desktop-viewer/src/test/kotlin/com/engineeringood/athena/apps/composeviewer/AthenaComposeViewerWorkbenchSessionTest.kt`
- `kernel/runtime/README.md`
- `kernel/runtime/README.zh-CN.md`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeService.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticDiffInspection.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSemanticDiffInspectionTest.kt`

## Change Log

- 2026-07-07: Created Story `2.4` and moved it directly into implementation against the current M2 runtime inspection path.
- 2026-07-07: Implemented semantic diff projection consequences, verified runtime and desktop tests, and completed the Java `25` full build.
