---
baseline_commit: ef77e67098a0140fe33db16f9978ae337a0324a1
---

# Story 4.1: Preserve source, outline, Problems, and sheet identity

Status: done

## Story

As an engineer,
I want layout intelligence to preserve canonical IDE navigation,
so that source, outline, Problems, and sheet selection remain one coherent workflow.

## Acceptance Criteria

1. Given a source subject, outline entry, diagnostic, or sheet occurrence in the M21 sample project, when reveal or selection payloads are inspected, then the same canonical subject and occurrence identity is used across source, outline, Problems, and sheet.
2. Given outline navigation in the M21 IDE path, when a `.athena` outline entry is activated, then navigation keeps the same `.athena` editor tab and does not open a duplicate editor for the same file.
3. Given M21 layout, route, and label facts, when identity payloads are inspected, then they do not introduce frontend-owned semantic resolution.
4. Given M21 proof checks, when they run, then accepted M20/M21 graph-workbench behavior remains intact.

## Tasks / Subtasks

- [x] Add identity coherence checks (AC: 1, 3)
  - [x] Verify layout region, route, and label facts preserve canonical subject, occurrence, snapshot, endpoint, and route identities where applicable.
  - [x] Verify source/outline/problem/sheet proof fixtures use the same canonical identities rather than frontend-derived ids.
  - [x] Keep Theia and renderer as consumers of identity payloads only.
- [x] Preserve same-tab outline navigation proof (AC: 2, 4)
  - [x] Reuse or extend existing Theia editor-navigation proof coverage for the M21 sample project.
  - [x] Ensure the proof does not depend on desktop-viewer behavior.
- [x] Add focused tests (AC: 1, 2, 3, 4)
  - [x] Add or extend Node/IDE proof scripts for same-tab `.athena` navigation and canonical identity payloads.
  - [x] Add Kotlin tests if needed for identity preservation in layout/route/label facts.
  - [x] Prove frontend-owned semantic resolution, duplicate editor tabs, desktop-viewer scope, AI layout, and final stack terms stay out of implementation contracts.
- [x] Validate and update story status (AC: 1, 2, 3, 4)
  - [x] Run `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test`.
  - [x] Run `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test`.
  - [x] Run `node --test ide/theia-frontend/scripts/athena-theia-editor-navigation.test.mjs`.
  - [x] Run `node --test ide/theia-frontend/scripts/athena-m21-sample-project.test.mjs`.
  - [x] Run `node --test ide/theia-frontend/scripts/athena-m21-graph-workbench-visual-proof.test.mjs`.
  - [x] Run `yarn workspace @engineeringood/athena-theia-product start:smoke:m21` from `ide/`.
  - [x] Run the encoding audit after touching docs or text assets.
  - [x] Update this story's Dev Agent Record and File List.

## Dev Notes

### Current State

- Epic 3 added region, route, and label facts with canonical identity fields.
- The M21 sample project and graph-workbench smoke path already exist and pass.
- Existing editor navigation proof script is `ide/theia-frontend/scripts/athena-theia-editor-navigation.test.mjs`.
- The user previously rejected duplicate editor tabs during outline navigation; Story 4.1 must keep that behavior covered.

### Architectural Guardrails

- Follow M21 AD-1, AD-4, AD-8, AD-9, AD-10, and AD-11.
- Theia, renderer, and proof scripts may inspect identity but may not become identity authority.
- Do not involve desktop-viewer.
- Do not add frontend-owned semantic resolution.

### Implementation Guidance

Likely update targets:

- `ide/theia-frontend/scripts/athena-theia-editor-navigation.test.mjs`
- `ide/theia-frontend/scripts/athena-m21-sample-project.test.mjs`
- `kernel/layout-engine/src/test/kotlin/com/engineeringood/athena/layout/engine/SchematicLayoutEngineTest.kt`
- `kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/SchematicRoutingModelTest.kt`
- `kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/SchematicLabelModelTest.kt`
- this story file and `sprint-status.yaml`

### Previous Story Intelligence

- Region, route, and label ids are snapshot-scoped; tests should assert snapshot id preservation where meaningful.
- Duplicate route/label identities were caught during review; keep exact identity uniqueness assumptions in tests.
- Keep M21 proof visible through Theia smoke and graph-workbench scripts.

### Testing Requirements

Run checks sequentially on Windows:

- `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test`
- `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test`
- `node --test ide/theia-frontend/scripts/athena-theia-editor-navigation.test.mjs`
- `node --test ide/theia-frontend/scripts/athena-m21-sample-project.test.mjs`
- `node --test ide/theia-frontend/scripts/athena-m21-graph-workbench-visual-proof.test.mjs`
- `yarn workspace @engineeringood/athena-theia-product start:smoke:m21`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Verification:
  - `node --test ide/theia-frontend/scripts/athena-theia-editor-navigation.test.mjs`
  - `node --test ide/theia-frontend/scripts/athena-m21-sample-project.test.mjs`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test`
  - `node --test ide/theia-frontend/scripts/athena-m21-graph-workbench-visual-proof.test.mjs`
  - `yarn workspace @engineeringood/athena-theia-product start:smoke:m21` from `ide/`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

### Completion Notes List

- Extended the Theia editor-navigation proof to assert M21 usage keeps outline navigation in the same `.athena` editor tab and source/Problems tied to the same canonical subject.
- Extended the M21 sample-project proof to assert routing/readability source identities remain stable across device, port, signal, and connection declarations.
- Reused existing kernel route/label identity tests from Epic 3 as Story 4.1's upstream identity proof.
- Review found no additional code changes required.

### File List

- `_bmad-output/implementation-artifacts/m21/4-1-preserve-source-outline-problems-and-sheet-identity.md`
- `_bmad-output/implementation-artifacts/m21/sprint-status.yaml`
- `ide/theia-frontend/scripts/athena-theia-editor-navigation.test.mjs`
- `ide/theia-frontend/scripts/athena-m21-sample-project.test.mjs`

## Change Log

- 2026-07-17: Created M21 Story 4.1 for IDE identity coherence.
- 2026-07-17: Added M21 identity and same-tab navigation proof coverage and marked Story 4.1 done.

## Senior Developer Review (AI)

### Review Outcome

Approved.

### Findings Addressed

- No code changes required after review.

### Verification

- `node --test ide/theia-frontend/scripts/athena-theia-editor-navigation.test.mjs` passed.
- `node --test ide/theia-frontend/scripts/athena-m21-sample-project.test.mjs` passed.
- `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test` passed.
- `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test` passed.
- `node --test ide/theia-frontend/scripts/athena-m21-graph-workbench-visual-proof.test.mjs` passed.
- `yarn workspace @engineeringood/athena-theia-product start:smoke:m21` passed from `ide/`.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.
