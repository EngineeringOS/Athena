# Athena M1 Diff And History Inspection Boundary

## Purpose

Story `2.6` adds the first runtime-owned review surface for semantic change inspection.

Athena already had command execution, history, undo, redo, replay, and incremental recompute. This slice adds the explainability layer on top of that path:

- runtime derives semantic diff inspection from canonical before and after documents
- history consequences stay linked to stable command ids and stable semantic ids
- CLI and Compose workbench surfaces consume runtime-owned inspection data instead of recalculating diffs locally

This proves the M1 review path `Engineering IR -> Diff/History -> Undo/Replay`.

## Boundary Split

- `AthenaCommandRuntimeService`
  - still owns command execution and command-history transitions
  - now captures the latest semantic diff inspection after command execution, undo, redo, and replay
  - can reconstruct command-linked history consequences for a recorded command id
- `AthenaExecutionContext`
  - exposes the latest runtime-owned semantic diff inspection to app and CLI layers
- `AthenaSemanticDiffInspection`
  - is a runtime-owned derived inspection artifact
  - does not become a second semantic authority
- `BootstrapCli`
  - formats runtime-owned diff and history consequence inspection for shell output
- `AthenaComposeViewerWorkbenchSession`
  - surfaces runtime-owned inspection summaries through existing inspector, diagnostics, and console surfaces

## Inspection Model

The current M1 inspection model is intentionally compact.

- `AthenaSemanticDiffInspection`
  - source of inspection such as `COMMAND`, `UNDO`, `REDO`, or `REPLAY`
  - linked command ids
  - affected semantic ids
  - diff entries tied to stable semantic ids
  - current history consequences for linked command ids
- `AthenaSemanticDiffEntry`
  - semantic id
  - semantic kind
  - `ADDED`, `REMOVED`, `MODIFIED`, or `CONTEXT`
  - before and after summaries derived from canonical documents
- `AthenaSemanticHistoryConsequence`
  - command id
  - command kind
  - current history status such as `APPLIED` or `UNDONE`
  - changed semantic ids

For the current `CONNECT_PORTS` mutation slice, this is enough to explain:

- which connection was added or removed
- which ports formed the affected scope context
- which command produced the inspected change
- whether that command is currently applied or undone

## Surface Rules

- CLI
  - may render the latest diff inspection
  - may render command-linked history consequences for a specific command id
  - must not compute semantic diffs independently
- Compose workbench
  - may show the latest diff summary in inspector, diagnostics, and console surfaces
  - must remain a consumer of runtime-owned inspection data
  - does not introduce a sovereign review model or a full editor-side diff cache

## Non-Goals

Story `2.6` does not introduce:

- a dedicated Compose diff panel system
- multi-change diff filtering, grouping, or navigation UX
- persisted review sessions
- semantic blame beyond current command-history linkage
- a generalized patch language for all future semantic mutation kinds

Those remain later work.

## Verification Path

From the repo root:

```powershell
java25; .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test
java25; .\gradlew.bat --no-daemon --console=plain :apps:cli:test
java25; .\gradlew.bat --no-daemon --console=plain :apps:desktop-viewer:test
java25; .\gradlew.bat --no-daemon --console=plain build
java25; .\gradlew.bat --no-daemon --console=plain test
```

Keep these checks sequential on Windows. Do not run Gradle verification tasks concurrently in this repository.
