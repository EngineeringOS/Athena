---
baseline_commit: 4b09cacc3435a1c902dc5be72ca30a3c596f784e
---

# Story 2.3: Prove The First Projection Mutation Path From The Graph Workbench

Status: done

## Story

As an engineer,  
I want one supported graph-originated projection edit to execute through Athena runtime,  
so that layout or projection changes remain governed without being confused with engineering truth.

## FR Traceability

- FR-1: route all meaningful changes through Athena commands
- FR-2: classify meaningful changes explicitly
- FR-3: keep graph-originated editing downstream of Athena-owned meaning
- FR-5: accepted projection mutation refreshes graph state from runtime-owned projection metadata
- FR-7: projection ownership contracts define what a view may emit
- NFR-1: meaningful changes route through one Athena-owned mutation path
- NFR-2: canonical engineering meaning remains upstream of any renderer or editor client
- NFR-4: command intents, mutation outcomes, and rejection paths remain inspectable
- NFR-6: graph stack must not own command meaning or durable mutation semantics

## Acceptance Criteria

1. Given a supported graph-originated projection mutation such as node position or grouping adjustment, when the user performs that action in the graph workbench, then Athena routes the request through governed command semantics into projection/layout metadata update, and projection refresh reflects the accepted metadata change deterministically.
2. Given a projection mutation is unsupported, invalid, or outside the projection ownership contract, when Athena refreshes the affected view, then the unapproved local state snaps back or is discarded, and the rejection path remains inspectable and renderer-neutral.

## Tasks / Subtasks

- [x] Publish one runtime-owned projection metadata state for the first governed graph placement proof. (AC: 1, 2)
  - [x] Keep the proof target narrow: cabinet component placement only.
  - [x] Store accepted placement overrides in runtime-owned projection/layout metadata rather than frontend-local canvas state.
  - [x] Invalidate and rebuild projection sessions from that metadata after accepted placement updates.
- [x] Turn the existing `adjust-layout-placement` intent path into a real projection mutation path. (AC: 1, 2)
  - [x] Reuse the existing graph intent contract from Story `2.1` instead of inventing a second projection edit protocol.
  - [x] Validate interactive ownership, supported command family, subject kind, and target existence before accepting placement mutation.
  - [x] Keep inspect-only or invalid placement requests rejected with renderer-neutral feedback.
- [x] Apply accepted placement metadata during projection refresh without mutating canonical engineering truth. (AC: 1, 2)
  - [x] Refresh the graph from runtime-owned projection state after accepted mutation.
  - [x] Preserve canonical semantic document equality before and after accepted projection mutation.
  - [x] Keep unsupported or rejected local drag state disposable so the view snaps back on refresh.
- [x] Wire the Theia graph workbench to prove the end-to-end projection mutation behavior. (AC: 1, 2)
  - [x] Refresh the active graph diagram after accepted placement intent.
  - [x] Keep the existing semantic connect path from Story `2.2` intact.
  - [x] Surface the accepted or rejected placement result through Athena-owned workbench feedback.
- [x] Verify the proof with focused runtime, LSP, and frontend regression coverage plus sequential Windows verification. (AC: 1, 2)
  - [x] Add tests for accepted cabinet placement refresh and rejected inspect-only or invalid placement attempts.
  - [x] Run Gradle and Node verification sequentially on Windows with Java 25.

## Dev Notes

### Story Intent

- Story `2.1` published the typed placement intent.
- Story `2.2` proved the first semantic graph mutation path through `CONNECT_PORTS`.
- Story `2.3` now needs the first real projection mutation path behind the existing `adjust-layout-placement` intent.

### Architecture Guardrails

- Preserve AD-34, AD-36, and AD-37: the graph workbench remains downstream, runtime owns the accepted mutation, and refresh remains deterministic.
- Preserve AD-40: only the `cabinet` projection may emit this first placement mutation path; `wiring` stays inspect-only.
- Do not blur projection mutation into semantic mutation. Accepted placement must not alter the canonical engineering document.

### Technical Notes

- The current runtime gap is explicit: projection sessions rebuild from compilation, but no runtime-owned projection metadata state exists yet for placement overrides.
- The smallest correct proof is a runtime-owned placement-override state keyed by projection view and semantic identity, then applied during projection-session rebuild.
- Keep the proof narrow and coherent enough that connected labels and lines remain visually aligned after a moved component is refreshed.

### Testing Requirements

- Minimum verification commands:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test :ide:lsp:test"`
  - `yarn --cwd integrations/graph-glsp test`
  - `yarn --cwd ide/theia-frontend test`
- Keep all Gradle and Node verification sequential on Windows. Do not overlap build, test, or run commands in parallel shells.

## References

- [Source: _bmad-output/planning-artifacts/epics-M8-2026-07-10.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-10-m8/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-10-m8/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-10-m8/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/implementation-artifacts/m8/2-1-translate-supported-graph-gestures-into-athena-command-intents.md]
- [Source: _bmad-output/implementation-artifacts/m8/2-2-prove-the-first-semantic-mutation-path-from-the-graph-workbench.md]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaGraphCommandIntentRuntimeService.kt]

## Story Completion Status

- Status: done
- Completion note: Cabinet drag placement now executes as a runtime-owned projection mutation, persists in runtime projection metadata, and refreshes the graph deterministically without changing canonical engineering semantics.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test :ide:lsp:test --tests com.engineeringood.athena.runtime.AthenaGraphCommandIntentServiceTest --tests com.engineeringood.athena.runtime.AthenaRuntimeProjectionSessionTest --tests com.engineeringood.athena.ide.lsp.AthenaProjectionRequestTest"`
- `yarn --cwd integrations/graph-glsp test`
- `yarn --cwd ide/theia-frontend test`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test :ide:lsp:test"`

### Completion Notes List

- Added runtime-owned projection placement override state on the execution context so accepted graph placement edits remain governed projection metadata rather than frontend-local canvas truth.
- Extended projection session rebuild to apply placement overrides to component boxes and related label/connection coordinates while preserving canonical engineering-document equality.
- Turned `adjust-layout-placement` from validation-only intent handling into the first accepted projection mutation path with explicit target-existence rejection.
- Updated the Theia graph workbench to refresh the active diagram after accepted placement mutation instead of leaving the drag result as transient local state.
- Added focused runtime and LSP coverage for accepted placement refresh, rejected inspect-only requests, and missing-target rejection.

### File List

- _bmad-output/implementation-artifacts/m8/2-3-prove-the-first-projection-mutation-path-from-the-graph-workbench.md
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaGraphCommandIntentRuntimeService.kt
- kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaGraphCommandIntentServiceTest.kt
- kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSessionTest.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionRequestTest.kt
- ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx
- ide/theia-frontend/lib/browser/athena-graph-workbench-widget.d.ts
- ide/theia-frontend/lib/browser/athena-graph-workbench-widget.d.ts.map
- ide/theia-frontend/lib/browser/athena-graph-workbench-widget.js
- ide/theia-frontend/lib/browser/athena-graph-workbench-widget.js.map
- ide/theia-frontend/tsconfig.tsbuildinfo
