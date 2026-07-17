---
baseline_commit: ef77e67098a0140fe33db16f9978ae337a0324a1
---

# Story 4.3: Keep M21 deferred boundaries explicit

Status: done

## Story

As a product reviewer,
I want M21 scope boundaries to stay executable and visible,
so that layout intelligence does not drift into repository/import, IEC breadth, cabinet authoring, or physical routing.

## Acceptance Criteria

1. Given the M21 PRD, architecture, epics, stories, and proof corpus, when boundary checks run, then they confirm public repository/import ecosystem work, full IEC breadth, cabinet authoring, full EPLAN parity, AI layout, final layout-stack selection, desktop viewer scope, and physical routing are deferred.
2. Given M21 implementation artifacts, when boundary checks inspect stories and tests, then no M21 story persists arbitrary canvas edits as semantic truth.
3. Given M21 proof and usage docs, when reviewed, then deferred boundaries are documented in the usage and retrospective handoff material.
4. Given the M21 proof baseline, when checks run, then existing acceptance, sample, graph-workbench, navigation, layout, and routing checks still pass.

## Tasks / Subtasks

- [x] Add executable M21 boundary check (AC: 1, 2, 3)
  - [x] Add `athena-m21-boundary.test.mjs` following the M19/M20 boundary-test pattern.
  - [x] Verify PRD, architecture, epics, usage docs, and sprint status preserve M21 deferred boundaries.
  - [x] Verify active M21 stories do not require repository/import, full IEC breadth, cabinet authoring, desktop-viewer scope, physical routing, AI layout, final stack selection, or sheet-local drag-save truth.
- [x] Wire boundary check into M21 proof coverage (AC: 3, 4)
  - [x] Add the boundary check to M21 usage verification path.
  - [x] Add the boundary check to `athena-m21-acceptance-coverage.test.mjs`.
- [x] Validate and update story status (AC: 1, 2, 3, 4)
  - [x] Run `node --test ide/theia-frontend/scripts/athena-m21-boundary.test.mjs`.
  - [x] Run `node --test ide/theia-frontend/scripts/athena-m21-acceptance-coverage.test.mjs`.
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

- M19 and M20 already use executable boundary tests in `ide/theia-frontend/scripts/`.
- M21 PRD, architecture, and epics all explicitly defer ecosystem expansion, cabinet authoring, physical routing, AI layout, final stack selection, desktop-viewer scope, and sheet-local drag-save truth.
- `docs/usages/m21-proof-usage.md` documents supported and unsupported M21 workflow slices.

### Architectural Guardrails

- Follow M21 AD-6 and AD-11.
- Do not add desktop-viewer scope.
- Do not weaken the M21 sample project or graph-workbench proof path.

### Implementation Guidance

Likely update targets:

- `ide/theia-frontend/scripts/athena-m21-boundary.test.mjs`
- `ide/theia-frontend/scripts/athena-m21-acceptance-coverage.test.mjs`
- `docs/usages/m21-proof-usage.md`
- this story file and `sprint-status.yaml`

### Testing Requirements

Run checks sequentially on Windows as listed in the tasks.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Verification:
  - `node --test ide/theia-frontend/scripts/athena-m21-boundary.test.mjs`
  - `node --test ide/theia-frontend/scripts/athena-m21-acceptance-coverage.test.mjs`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test`
  - `node --test ide/theia-frontend/scripts/athena-theia-editor-navigation.test.mjs`
  - `node --test ide/theia-frontend/scripts/athena-m21-sample-project.test.mjs`
  - `node --test ide/theia-frontend/scripts/athena-m21-graph-workbench-visual-proof.test.mjs`
  - `yarn workspace @engineeringood/athena-theia-product start:smoke:m21` from `ide/`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

### Completion Notes List

- Added `athena-m21-boundary.test.mjs` to verify M21 deferred boundaries across PRD, architecture, epics, usage, sprint status, and checked-in contracts.
- Wired the M21 boundary test into acceptance coverage and usage verification.
- Review fixed assertion wording to match the actual PRD phrasing for AI-driven layout, final ELK/layout-stack decision, and direct canvas drag-save authoring.

### File List

- `_bmad-output/implementation-artifacts/m21/4-3-keep-m21-deferred-boundaries-explicit.md`
- `_bmad-output/implementation-artifacts/m21/sprint-status.yaml`
- `docs/usages/m21-proof-usage.md`
- `ide/theia-frontend/scripts/athena-m21-acceptance-coverage.test.mjs`
- `ide/theia-frontend/scripts/athena-m21-boundary.test.mjs`

## Change Log

- 2026-07-17: Created M21 Story 4.3 for deferred boundary guardrails.
- 2026-07-17: Added M21 deferred boundary test and marked Story 4.3 done.

## Senior Developer Review (AI)

### Review Outcome

Approved after assertion fixes.

### Findings Addressed

- [x] Boundary assertions initially used stricter wording than the PRD; fixed to match documented M21 phrases without weakening deferred-scope checks.

### Verification

- `node --test ide/theia-frontend/scripts/athena-m21-boundary.test.mjs` passed.
- `node --test ide/theia-frontend/scripts/athena-m21-acceptance-coverage.test.mjs` passed.
- `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test` passed.
- `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test` passed.
- `node --test ide/theia-frontend/scripts/athena-theia-editor-navigation.test.mjs` passed.
- `node --test ide/theia-frontend/scripts/athena-m21-sample-project.test.mjs` passed.
- `node --test ide/theia-frontend/scripts/athena-m21-graph-workbench-visual-proof.test.mjs` passed.
- `yarn workspace @engineeringood/athena-theia-product start:smoke:m21` passed from `ide/`.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.
