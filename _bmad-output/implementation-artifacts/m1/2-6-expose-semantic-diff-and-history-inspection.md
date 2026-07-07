---
baseline_commit: bdc3227afbefd6834923c325aa556eaa8a2f6d77
---

# Story 2.6: Expose Semantic Diff And History Inspection

Status: done

## Story

As a reviewer or operator,
I want Athena to expose semantic diffs and command-history consequences for project changes,
so that I can inspect what changed, why it changed, and how undo or replay affects canonical project state.

## Acceptance Criteria

1. Given a command-backed semantic mutation has executed over the active project, when a reviewer inspects the change, then Athena exposes a before-and-after semantic diff for the affected scope, and the diff is tied to stable semantic identity rather than only to transient UI state.
2. Given command history exists for one or more project changes, when the reviewer inspects command-history consequences, then Athena can show which command produced the inspected change and how that change relates to recorded history, and the inspection path remains runtime-owned and consistent with canonical semantic state.
3. Given an undo, redo, or replay operation is performed, when the reviewer inspects the resulting project state, then Athena can show the corresponding semantic diff and updated history consequences for that operation, and the inspection remains explainable without requiring private viewer-local or plugin-private state.
4. Given diff and history inspection support is implemented, when standard Java `25` build and runtime checks run, then the workspace builds successfully and reviewers can inspect command-linked semantic changes over the active project, and the implementation proves the M1 path `Engineering IR -> Diff/History -> Undo/Replay`.

## Tasks / Subtasks

- [x] Add a runtime-owned semantic diff inspection model. (AC: 1, 2, 3)
  - [x] Introduce documented runtime types for semantic diff entries, linked command consequences, and latest inspection results.
  - [x] Derive before/after summaries from canonical documents using stable semantic identities.
  - [x] Keep the inspection model runtime-owned and independent from CLI or Compose presentation formatting.
- [x] Surface command-linked history consequences through runtime services. (AC: 1, 2, 3)
  - [x] Capture the latest semantic diff inspection after command execution, undo, redo, and replay.
  - [x] Add runtime inspection access for command-linked history consequences against recorded command ids.
  - [x] Keep the inspection consistent with current history status and canonical active project state.
- [x] Expose the inspection path through CLI and Compose workbench surfaces. (AC: 2, 3, 4)
  - [x] Add CLI commands for latest diff inspection and command-linked history consequence inspection.
  - [x] Surface latest diff and history consequence summaries through existing Compose inspector, diagnostics, and console surfaces.
  - [x] Keep the GUI path runtime-owned and inspectable rather than introducing viewer-local semantic state.
- [x] Document and verify the diff/history inspection boundary. (AC: 1, 2, 3, 4)
  - [x] Add focused runtime tests for command, undo, redo, and replay inspection.
  - [x] Add CLI tests for latest diff and command-history consequence output.
  - [x] Add one Compose workbench proof that diff/history summaries refresh after a GUI-backed mutation.
  - [x] Add an architecture-facing note for the new runtime inspection boundary and verify all Gradle checks sequentially on Java `25`.

## Dev Notes

### Story Intent

- Story `2.6` should stay narrow and build directly on the command, history, and incremental recompute work already in M1.
- The proof target is reviewer-facing inspection over the current `CONNECT_PORTS` mutation path plus undo, redo, and replay.
- This story should not introduce a dedicated new Compose diff panel or wider editor UX system.

### Architecture Guardrails

- `Engineering IR` remains the only canonical semantic authority.
- Semantic diff and history consequence models are runtime-owned derived inspection artifacts.
- CLI and Compose workbench surfaces may format inspection data, but they must not calculate semantic diffs privately.
- History consequence inspection must explain command linkage using stable command ids and stable semantic ids.

### Technical Requirements

- Keep the implementation JVM-first, local, and deterministic on Java `25`.
- Preserve package root `com.engineeringood.athena`.
- Keep new core Kotlin contracts documented with KDoc.
- Prefer one runtime-owned inspection model reused by CLI and Compose surfaces.
- Avoid introducing a second persistence model, separate review database, or UX-heavy docking expansion.

### Previous Story Intelligence

- Story `2.5` already introduced runtime-owned affected scope planning and incremental recompute reporting after command-backed semantic changes.
- `AthenaCommandHistory` already stores before and after canonical documents plus stable changed semantic ids for each recorded command.
- The Compose workbench currently surfaces runtime-managed diagnostics, inspector fields, command history count, and console output; this story should enrich those existing surfaces rather than inventing a large new UI contract.
- Sequential Gradle verification on Windows is mandatory in this repo. Do not run `build` and `test` concurrently.

### Testing Requirements

- Minimum verification commands for story completion:
  - `java25; .\\gradlew.bat --no-daemon --console=plain :runtime:test`
  - `java25; .\\gradlew.bat --no-daemon --console=plain :cli:test`
  - `java25; .\\gradlew.bat --no-daemon --console=plain :apps:compose-viewer:test`
  - `java25; .\\gradlew.bat --no-daemon --console=plain build`
  - `java25; .\\gradlew.bat --no-daemon --console=plain test`
- Follow red-green-refactor. No production diff/history inspection code should land before the first failing tests exist.
- Keep verification strictly sequential on Windows and preserve the root Java `25` build path.

### References

- `_bmad-output/planning-artifacts/epics.md`
  - `Story 2.6: Expose Semantic Diff And History Inspection`
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-03/prd.md`
  - `FR-20`, `FR-8`, `FR-9`
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-03/ARCHITECTURE-SPINE.md`
  - `AD-3`, `AD-4`, `AD-6`

## Story Completion Status

- Status: review

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Red tests were added first for runtime diff inspection, CLI diff/history commands, and Compose workbench summary refresh.
- Runtime now captures a latest semantic diff inspection after command execution, undo, redo, and replay, and can reconstruct command-linked history consequences for recorded command ids.
- CLI now exposes `diff` and `history-consequences <command-id>` as thin formatters over runtime-owned inspection data.
- Compose workbench now surfaces latest diff and history consequence summaries through existing inspector, diagnostics, and console surfaces without inventing a separate semantic cache.
- Added the architecture note `docs/compiler/m1-diff-history-inspection-boundary.md` and linked it from the runtime-host boundary.
- Sequential Java 25 verification passed for `:runtime:test`, `:cli:test`, `:apps:compose-viewer:test`, full `build`, full `test`, and `:apps:compose-viewer:bootstrapSmoke`.

### Completion Notes List

- Implemented runtime-owned semantic diff inspection over canonical before and after documents using stable semantic ids and command ids.
- Added command-history consequence reconstruction so reviewers can inspect the original consequence of a recorded command alongside its current history status.
- Exposed latest inspection summaries in CLI and Compose workbench surfaces while preserving runtime ownership of diff derivation.
- Verification remained strictly sequential on Windows and used `java25` for every Gradle command.

### File List

- `_bmad-output/implementation-artifacts/m1/2-6-expose-semantic-diff-and-history-inspection.md`
- `_bmad-output/implementation-artifacts/m1/sprint-status.yaml`
- `apps/compose-viewer/src/main/kotlin/com/engineeringood/athena/apps/composeviewer/AthenaComposeViewerWorkbenchSession.kt`
- `apps/compose-viewer/src/test/kotlin/com/engineeringood/athena/apps/composeviewer/AthenaComposeViewerWorkbenchSessionTest.kt`
- `cli/src/main/kotlin/com/engineeringood/athena/cli/BootstrapCli.kt`
- `cli/src/test/kotlin/com/engineeringood/athena/cli/CommandHistoryCliTest.kt`
- `docs/compiler/m1-diff-history-inspection-boundary.md`
- `docs/compiler/m1-runtime-host-boundary.md`
- `runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeService.kt`
- `runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt`
- `runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticDiffInspection.kt`
- `runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSemanticDiffInspectionTest.kt`

### Change Log

- Added runtime-owned semantic diff inspection and command-history consequence inspection over the current M1 mutation path.
- Exposed new CLI and Compose workbench review summaries without moving semantic diff logic outside runtime ownership.
- Documented the diff/history inspection boundary and verified the implementation sequentially on Java 25.
