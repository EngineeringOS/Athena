---
baseline_commit: 93e83289929feb7c391b676bb666c5beed0ebaf4
---

# Story 2.1: Round-trip selection through canonical identity

Status: done

## Story

As an engineer,
I want clicking a rendered subject to update source and inspector state,
so that the sheet and the IDE stay synchronized.

## Acceptance Criteria

1. Given a rendered schematic subject on the Theia sheet, when it is selected by click or keyboard, the selection service receives the canonical semantic subject id.
2. The selected subject resolves through the current semantic inspection payload to source uri, source range, label, and kind when those facts are available.
3. The rendered selection state uses the same canonical subject id and occurrence/anchor aliases already published by the projection payload.
4. Selection retention across refreshed projection snapshots keeps a selection only while the canonical subject remains published in the active sheet, graph, or governed endpoint aliases.
5. The implementation does not add frontend-local semantic inference, renderer-owned meaning, cabinet preview behavior, or a new protocol/layout-stack decision.
6. Executable frontend tests cover click/keyboard selection intent, source resolution, occurrence alias preservation, and retention behavior.

## Tasks / Subtasks

- [x] Lock sheet-to-selection canonical subject flow (AC: 1, 3, 5)
  - [x] Add or update frontend tests proving sheet nodes and connectors select by canonical semantic id.
  - [x] Preserve the existing `AthenaSemanticSelectionService.selectSemanticId` path instead of creating a second selection channel.
  - [x] Keep selection payloads read-only and projection-owned.
- [x] Prove source and inspector round-trip resolution (AC: 2, 4, 6)
  - [x] Add or update tests for `resolveSemanticSelectionFromInspection` and `resolveSemanticSelectionFromSourceRange`.
  - [x] Assert source uri, source range, label, and kind survive the selection round trip.
  - [x] Assert refreshed projection retention uses graph subjects, active-sheet subjects, endpoint aliases, and anchor aliases.
- [x] Preserve canonical occurrence and alias targeting (AC: 3, 4, 6)
  - [x] Assert repeated occurrences and port aliases resolve through governed projection payloads.
  - [x] Assert stale selections are dropped when no governed projection path contains the canonical subject id.
  - [x] Avoid deriving identity from DOM text, CSS classes, or rendered pixel state.
- [x] Keep M19 bounded (AC: 5)
  - [x] Do not add cabinet preview behavior.
  - [x] Do not choose or hard-code the final protocol/layout engine stack.
  - [x] Do not move semantic authority into frontend-local state.

## Dev Notes

### Current State

- `AthenaGraphWorkbenchWidget.handleNodeSelection` already calls `AthenaSemanticSelectionService.selectSemanticId(semanticId)`.
- `AthenaGraphWorkbenchEdgeLayer` already receives `onSelectSemanticId` and can select connector semantic ids.
- `athena-semantic-selection-model.ts` already resolves semantic inspection, source range selection, projection occurrences, cross references, endpoint aliases, related subjects, and selection retention.
- Story 1.3 added governed `sheetChrome`; Story 1.4 added the local M19 schematic proof fixture.

### Architectural Guardrails

- Canonical subject identity remains the only selection currency.
- The frontend may synchronize selection and reveal, but it may not resolve engineering meaning locally.
- Selection must preserve projection-published occurrence ids, endpoint ids, anchor ids, and cross-reference ids.
- Theia remains projection-only; no new renderer, cabinet preview, protocol selector, or layout engine decision belongs in this story.

### Project Structure Notes

- Likely update targets:
  - `ide/theia-frontend/src/browser/athena-semantic-selection-model.ts`
  - `ide/theia-frontend/src/browser/athena-semantic-selection-service.ts`
  - `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`
  - `ide/theia-frontend/scripts/athena-semantic-selection-model.test.mjs`
  - `ide/theia-frontend/scripts/athena-m19-sheet-proof.test.mjs`
- Prefer focused tests over broad UI rewrites. If existing behavior already satisfies an AC, add tests rather than changing code.

### Testing Requirements

- Use the existing Theia frontend test stack: `yarn build` and `node --test scripts/*.test.mjs`.
- Keep tests deterministic and fixture-driven.
- Reuse the local M19 fixture in `examples/m19/schematic-sheet-proof/ready-sheet.diagram.mjs` when useful.
- If text files change, run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.

### References

- [Source: `_bmad-output/implementation-artifacts/m19/epics.md` - Epic 2, Story 2.1]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-16-m19/prd.md` - FR-5]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-16-m19/ARCHITECTURE-SPINE.md` - AD-4]
- [Source: `_bmad-output/implementation-artifacts/m19/1-3-render-credible-schematic-elements.md` - sheet chrome and rendered subject baseline]
- [Source: `_bmad-output/implementation-artifacts/m19/1-4-prove-sheet-determinism-with-executable-fixtures.md` - local M19 fixture baseline]
- [Source: `ide/theia-frontend/src/browser/athena-semantic-selection-model.ts`]
- [Source: `ide/theia-frontend/src/browser/athena-semantic-selection-service.ts`]
- [Source: `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`]
- [Source: `ide/theia-frontend/scripts/athena-semantic-selection-model.test.mjs`]
- [Source: `ide/theia-frontend/scripts/athena-m19-sheet-proof.test.mjs`]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Story created from M19 Epic 2, PRD FR-5, architecture AD-4, and current Theia selection code.
- Existing code already has canonical selection service and projection alias helpers; implementation should prefer tests unless a gap is proven.
- Red phase: `yarn build; if ($LASTEXITCODE -eq 0) { node --test scripts/athena-semantic-selection-model.test.mjs }` failed because `resolveRenderedSelectionTarget` did not exist.
- Focused verification: `yarn build; if ($LASTEXITCODE -eq 0) { node --test scripts/athena-semantic-selection-model.test.mjs scripts/athena-m19-sheet-proof.test.mjs }` passed 12 tests.
- Regression verification: `yarn test` in `ide/theia-frontend` passed 54 tests.
- Encoding verification: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Added `resolveRenderedSelectionTarget` to convert projection-published rendered nodes, edges, and terminals into canonical selection targets.
- Extended semantic selection tests to prove rendered subject selection preserves canonical semantic id, occurrence id, endpoint id, and anchor id without DOM inference.
- Extended the M19 schematic proof test to validate rendered fixture nodes, conductors, and terminals resolve to canonical selection targets.

### File List

- `_bmad-output/implementation-artifacts/m19/2-1-round-trip-selection-through-canonical-identity.md`
- `_bmad-output/implementation-artifacts/m19/sprint-status.yaml`
- `ide/theia-frontend/src/browser/athena-semantic-selection-model.ts`
- `ide/theia-frontend/scripts/athena-semantic-selection-model.test.mjs`
- `ide/theia-frontend/scripts/athena-m19-sheet-proof.test.mjs`

### Change Log

- 2026-07-16: Added rendered-sheet selection target resolver and deterministic frontend tests for canonical selection round-trip behavior.
