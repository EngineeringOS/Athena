---
status: review
baseline_commit: 8d12ba03c05b373b98fbc513e6c7f3f6fce962d6
story_id: 7.6
epic: 7
title: Correct Cabinet-First Graph Authoring UX
---

# Story 7.6: Correct Cabinet-First Graph Authoring UX

## Status

Review

## Story

As a controls engineer,
I want Cabinet to remain the stable primary Graph View and Create Device to complete directly from
that surface,
so that the M32 customer demo behaves like an engineering authoring product instead of exposing
incomplete proof controls.

## Acceptance Criteria

1. Given a new M32 IDE session, when Graph View opens, then `cabinet` is the visible and active
   primary customer projection, while unfinished projections remain explicit compatibility
   surfaces rather than hidden active state.
2. Given Documentation is activated through a compatibility or programmatic path, when sheet and
   cross-reference navigation is available, then it renders in a contextual navigation region and
   does not add elongated controls to the global tool group.
3. Given Graph View is active and no Athena source editor has been opened, when the user selects a
   concept, enters a tag/model, previews, and accepts Create Device, then LSP and Mutation Authority
   derive the canonical source context, validate the Revision Guard, persist the backend-authored
   edit, reproject the new device, and keep Theia free of source serialization authority.
4. Given Electron E2E runs against a temporary M32 workspace copy, when the create transaction
   completes and the IDE reopens, then structured proof verifies preview eligibility, acceptance,
   source persistence, projected semantic identity, and reopen persistence without first opening
   an Athena editor.
5. Given the story implementation is complete, when touched and adjacent source, generated
   bundles, tests, docs, samples, screenshots, status, and cleanup ledger are deeply reviewed, then
   stale documentation-first and shell-only proof assumptions are removed or ledgered, `.tools` is
   excluded, and AC-to-evidence is recorded.
6. Mandatory Polish/Purge Gate complete.

## Tasks / Subtasks

- [x] Establish RED product-contract tests for Cabinet-first projection visibility. (AC: 1)
  - [x] Assert the frontend visibility policy returns `cabinet`, not `documentation`.
  - [x] Assert M32 product smoke requires visible and active Cabinet with compatibility count.
  - [x] Run the focused frontend tests and record the expected documentation-first failures.
- [x] Implement the Cabinet-first primary projection contract. (AC: 1)
  - [x] Change only frontend visibility policy and M32 proof expectations; preserve runtime ids and
    programmatic switching.
  - [x] Verify focused tests pass and runtime Cabinet-default tests remain unchanged.
- [x] Establish RED toolbar-composition tests for contextual Documentation navigation. (AC: 2)
  - [x] Assert sheet selector and cross-reference controls are absent from the global tool group.
  - [x] Assert a separate contextual navigation region owns Documentation navigation.
  - [x] Run the focused tests and record the expected failure against the current mixed toolbar.
- [x] Move Documentation navigation out of the global tool group. (AC: 2)
  - [x] Render sheet and cross-reference controls in one stable contextual region.
  - [x] Keep single-sheet/no-marker behavior intentionally absent without placeholder chrome.
  - [x] Add compact-width CSS and verify no toolbar overflow or nested card treatment.
- [x] Establish RED LSP and frontend tests for graph-first Create Device. (AC: 3)
  - [x] Add an LSP test that requests create preview without `didOpen` and expects governed,
    acceptance-eligible source evidence for the canonical project source.
  - [x] Assert Graph View preview no longer requires `editorManager.currentEditor` to be Athena.
  - [x] Assert local revision checks remain fail-closed when a target editor/model is open, while an
    unopened source delegates final validation to backend Mutation Authority.
  - [x] Run tests and record the expected blocked/disabled failures.
- [x] Implement backend-governed source activation and graph-origin acceptance. (AC: 3)
  - [x] Lazily track the canonical project source in LSP authoring preview when no document was
    opened; do not create frontend source text or bypass repository contract validation.
  - [x] Remove the Graph View current-editor preview gate.
  - [x] Allow acceptance while Graph View is active; preserve backend Revision Guard, capability,
    source planner, workspace mutation, and reprojection authority.
  - [x] Verify LSP and frontend focused tests pass.
- [x] Add complete temporary-workspace Electron E2E. (AC: 4)
  - [x] Copy the M32 sample to a verified OS temporary directory and never mutate the repository
    sample during create proof.
  - [x] Launch without Outline/source-editor activation, open Create Device, set a unique tag,
    preview, accept, and wait for mutation/reprojection proof.
  - [x] Assert the temp `.athena` source contains the backend-authored nested device and ports.
  - [x] Reopen the temp project and assert the created semantic identity is present in Graph View.
  - [x] Remove only the verified temporary workspace after proof completes.
- [x] Rebuild and run regression verification sequentially. (AC: 1..6)
  - [x] Stop only the Athena Electron session launched for manual M32 review before rebuilding.
  - [x] Run focused frontend tests, full frontend tests, and graph-glsp tests.
  - [x] Run LSP/JVM tests and root Gradle `check` strictly sequentially.
  - [x] Rebuild the Theia product and run M32 product smoke plus graph-authoring E2E.
  - [x] Run encoding audit and verify `git status --short -- .tools` is empty.
- [x] Perform mandatory final polish/purge and record AC-to-evidence. (AC: 5,6)
  - [x] Review touched and adjacent widget, LSP, smoke, generated bundle, docs, sample, and status
    paths for stale Story 7.3/7.5 assumptions.
  - [x] Remove stale code/tests/docs or add a complete cleanup-ledger entry with owner and target.
  - [x] Confirm no shell-only “usable create panel” claim remains.
  - [x] Update Dev Agent Record, File List, Change Log, cleanup ledger, and sprint status; stop at
    `review` until code review.

### Review Findings

- [x] [Review][Patch] Fail closed when an unopened canonical source changes on disk after preview.
- [x] [Review][Patch] Return governed rejection when canonical source activation cannot read the file.
- [x] [Review][Patch] Bind nested-port persistence checks to the created device block.
- [x] [Review][Patch] Preserve every source projection occurrence id in package-backed facts.
- [x] [Review][Patch] Reveal and activate Graph View before smoke-only compatibility switching.
- [x] [Review][Patch] Close the stale Documentation-first cleanup-ledger policy.

## Dev Notes

### Architecture Guardrails

- `.athena` remains canonical semantic persistence. Theia submits intent and consumes returned
  proof/edit payloads; it must not serialize a device declaration.
- Mutation Authority and backend source planning remain the only accepted source-change authority.
- LSP may create a tracked snapshot from the activated project's canonical source when no editor
  sent `didOpen`; this is repository-backed source activation, not frontend ownership.
- Existing `applyAuthoringWorkspaceMutation` Revision Guard and workspace edit path must remain
  authoritative and fail closed.
- Runtime projection order already prefers Cabinet. Do not change kernel/runtime semantics merely
  to compensate for frontend visibility.
- `cabinet`, `documentation`, `wiring`, and `schematic` remain transport ids. Product visibility is
  an adapter policy, not semantic truth.
- Documentation navigation is contextual projection UI. It must not share the global command group
  and must not become a floating card.
- No new dependencies are required.

### Current State And Required Changes

- `AthenaGraphWorkbenchWidget.resolveVisibleProjectionViews` currently returns only
  `documentation`; change it to expose Cabinet as the M32 primary surface.
- `renderStageChrome` currently places sheet selector and reference-marker controls inside the
  global tool group; move them to a separate contextual region.
- `renderCreateEntityControls` and `previewCreateEntityTransaction` require the current editor to
  be Athena; remove this frontend prerequisite.
- `acceptCreateEntityPreview` requires a matching active Athena editor and local model revision.
  Preserve local stale detection only when the source is open; otherwise rely on backend Revision
  Guard and Mutation Authority.
- `AthenaLanguageServer.authoringPreview` currently creates a fallback Revision Guard from disk but
  does not create `trackedDocument`, so governed capability discovery is blocked. Reuse
  `AthenaLanguageFeatures.trackDocument` for the canonical activated source.
- Story 7.3 smoke proves panel geometry only. Story 7.6 E2E must mutate a temporary workspace and
  prove source, reprojected identity, and reopen persistence.

### Project Structure Notes

- Primary implementation:
  - `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`
  - `ide/theia-frontend/src/browser/style/index.css`
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`
- Tests/proof:
  - `ide/theia-frontend/scripts/athena-m32-graph-view-taxonomy.test.mjs`
  - new focused Story 7.6 frontend contract test if existing files would mix responsibilities
  - `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringRequestTest.kt`
  - `ide/theia-product/scripts/athena-electron-open-workspace-main.js`
  - `ide/theia-product/scripts/verify-athena-m32-sample-project.js`
  - new graph-first authoring smoke script if separation keeps the product smoke readable
- Process artifacts:
  - this story, `epics.md`, `sprint-status.yaml`, and `cleanup-ledger.md`
- Do not edit generated `integrations/graph-glsp/lib` for this story unless source adapter behavior
  actually changes; generated Theia product output is rebuilt mechanically.

### Testing Requirements

- TDD is mandatory. Every production behavior change must first have a focused failing test.
- Gradle commands run sequentially only.
- Required final commands include:
  - `yarn test` in `ide/theia-frontend`
  - `yarn test` in `integrations/graph-glsp`
  - focused `:ide:lsp:test` for graph-first authoring, then root `gradlew check`
  - `yarn build` and both M32 Electron smoke paths in `ide/theia-product`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  - `git status --short -- .tools`
- E2E acceptance requires structured proof. Screenshot evidence remains secondary.

### Previous Story Intelligence

- Story 7.3 established reliable panel geometry but explicitly retained the active-source-editor
  limitation and did not test mutation.
- Story 7.5 made Documentation the single visible projection and encoded that decision into smoke;
  the runtime still defaults to Cabinet, creating hidden active state.
- The existing M32 product smoke opens the source through Outline before inspecting Create Device,
  so it cannot prove graph-first operation.
- Generated product bundles must be rebuilt before manual or Electron validation.

### References

- [Source: `_bmad-output/planning-artifacts/sprint-change-proposal-2026-07-22-m32-graph-view-correction.md`]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-22-m32/prd.md`]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-22-m32/ARCHITECTURE-SPINE.md`]
- [Source: `_bmad-output/implementation-artifacts/m32/7-3-make-create-entity-control-understandable-and-usable.md`]
- [Source: `_bmad-output/implementation-artifacts/m32/7-5-final-graph-view-product-polish-and-purge.md`]
- [Source: `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt`]
- [Source: `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`]
- [Source: `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-07-22: Live M32 proof showed Create Device Preview disabled without an active Athena editor.
- 2026-07-22: Source trace confirmed runtime Cabinet default conflicts with frontend
  Documentation-only visibility and smoke enforcement.
- 2026-07-22: Course correction approved by user with "move on" after evidence-backed diagnosis.
- 2026-07-23: Independent blind, edge-case, and acceptance review found stale unopened-source
  persistence, unreadable-source failure, loose nested-port proof, repeated projection-id loss, and
  a smoke widget-attachment race; all Story 7.6 findings were patched and retested.
- 2026-07-23: Graph-first smoke intentionally skips LSP diagnostic-publication evidence because it
  never sends `didOpen`; acceptance is compiler-gated, while the initial repository smoke separately
  proves zero published diagnostics and reopen proves the committed semantic identity.

### Completion Notes List

- Cabinet is the only visible/default customer Graph View; Documentation remains an explicit
  programmatic compatibility surface with sheet/reference controls in contextual navigation.
- Create Device now previews and accepts directly from Graph View. LSP lazily activates the
  canonical repository source, Mutation Authority owns persistence, open documents use
  `workspace/applyEdit`, and unopened documents use direct persistence guarded against disk drift.
- Electron smoke creates `GraphMotorM32` in a verified temporary copy, proves the generated ports
  are nested inside that device block, reopens the project, proves projected semantic identity, and
  removes only the verified temporary parent in `finally`.
- Package-backed representation facts now retain every source projection occurrence id; Cabinet and
  Documentation continue to consume package authority without renderer fallback.
- Independent review resolved every in-scope patch finding. Older relationship normalization and
  package-invariant observations were not introduced by Story 7.6 and remain outside this correction.
- AC 1 evidence: frontend Cabinet visibility contracts plus Electron
  `activeViewId=cabinet`, `visibleViewIds=[cabinet]`, and `compatibilityViewCount=3`.
- AC 2 evidence: contextual-navigation frontend contract plus Electron
  `globalToolbarDocumentControlCount=0`, sheet restoration, and Cabinet restoration.
- AC 3 evidence: LSP graph-first, stale-disk, unreadable-source, and open-editor mutation tests;
  frontend source-authority contracts; Electron preview/accept/project proof without Outline.
- AC 4 evidence: `ATHENA_M32_GRAPH_AUTHORING_PROOF` reports preview eligibility, acceptance,
  source/nested-port persistence, projection, and reopen persistence from the temporary workspace.
- AC 5-6 evidence: cleanup-ledger closure, stale-string audit, `git diff --check`, encoding audit,
  empty `.tools` status, frontend `202/202`, graph-glsp `7/7`, root Gradle `check` (147 tasks),
  Theia build, and M32 Electron smoke.

### File List

- _bmad-output/implementation-artifacts/m32/7-6-correct-cabinet-first-graph-authoring-ux.md
- _bmad-output/implementation-artifacts/m32/cleanup-ledger.md
- _bmad-output/implementation-artifacts/m32/screenshots/m32-graph-workbench-smoke.png
- _bmad-output/implementation-artifacts/m32/sprint-status.yaml
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringWorkspaceMutation.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringRequestTest.kt
- ide/theia-frontend/scripts/athena-m32-cabinet-first-authoring-ux.test.mjs
- ide/theia-frontend/scripts/athena-m32-create-entity-panel.test.mjs
- ide/theia-frontend/scripts/athena-m32-graph-view-taxonomy.test.mjs
- ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx
- ide/theia-frontend/src/browser/athena-product-contribution.ts
- ide/theia-frontend/src/browser/style/index.css
- ide/theia-product/scripts/athena-electron-open-workspace-main.js
- ide/theia-product/scripts/verify-athena-m32-sample-project.js
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/M32PackageBackedPresentationFactDeriver.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM32SampleProjectCompilerTest.kt

## Change Log

- 2026-07-22: Created Story 7.6 from approved M32 Graph View course correction.
- 2026-07-23: Implemented Cabinet-first UX, contextual Documentation navigation, and graph-first
  governed Create Device persistence with temporary-workspace reopen E2E.
- 2026-07-23: Addressed independent review findings, completed polish/purge and AC-to-evidence, and
  moved Story 7.6 to `review`.

## Mandatory Final Polish/Purge Gate

- Deep-review touched and adjacent frontend, LSP, product smoke, sample, generated bundles, docs,
  status, and cleanup ledger.
- Remove stale documentation-first and shell-only create proof assumptions or ledger retained
  compatibility with owner, reason, target milestone, and verification.
- Record AC-to-evidence before moving the story to `review`.
