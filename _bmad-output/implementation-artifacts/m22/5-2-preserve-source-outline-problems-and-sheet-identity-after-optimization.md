---
baseline_commit: 0b43cbe
---

# Story 5.2: Preserve Source, Outline, Problems, and Sheet Identity After Optimization

Status: done

## Story

As an engineer,
I want IDE navigation to remain coherent while layout optimization is active,
so that source, outline, diagnostics, and rendered sheet occurrences refer to the same identities.

## Acceptance Criteria

1. Given a source subject, outline node, diagnostic, or rendered occurrence in the M22 sample, when I reveal or select the subject across IDE surfaces, then the same canonical subject and occurrence identity is used.
2. Given outline navigation is used on a `.athena` file, when an outline node is selected, then navigation keeps the same `.athena` editor tab.
3. Given layout optimization is active, when Theia surfaces render or reveal subjects, then frontend-owned semantic resolution is not introduced.

## Tasks / Subtasks

- [x] Add identity coherence regression coverage (AC: 1, 2, 3)
  - [x] Add failing frontend/static test for canonical semantic id propagation across graph, selection, bridge, and usage docs.
  - [x] Include same-tab outline navigation guard.
- [x] Preserve canonical identity contracts (AC: 1, 3)
  - [x] Confirm graph selection and reveal use canonical semantic ids from projection payloads.
  - [x] Avoid frontend semantic reconstruction from labels, DOM text, or canvas position.
- [x] Preserve same-tab outline navigation (AC: 2)
  - [x] Confirm document symbols flow through the active editor model and do not open duplicate `.athena` tabs.
- [x] Run validation (AC: 1, 2, 3)
  - [x] Run affected frontend test.
  - [x] Run frontend build.
  - [x] Run encoding audit.

## Dev Notes

### Current State

- M20/M21 established source, outline, Problems, and Graphical View coherence as a user-visible product rule.
- M22 optimization adds layout facts and round-trip preview state, but identity must still flow through canonical semantic ids and occurrence ids.

### Guardrails

- Do not resolve engineering meaning from canvas labels, DOM text, or visual positions.
- Do not add a second frontend identity model.
- Keep outline navigation on the existing editor tab for `.athena` files.

### Testing Requirements

- `node --test ide/theia-frontend/scripts/athena-m22-identity-coherence.test.mjs`
- `yarn workspace @engineeringood/athena-theia-frontend build`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

### References

- [Source: `_bmad-output/implementation-artifacts/m22/epics.md` - Epic 5, Story 5.2]
- [Source: `ide/theia-frontend/src/browser/athena-semantic-selection-model.ts`]
- [Source: `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`]
- [Source: `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `node --test ide/theia-frontend/scripts/athena-m22-identity-coherence.test.mjs`
- `yarn workspace @engineeringood/athena-theia-frontend build`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

### Completion Notes List

- Added M22-specific regression coverage for canonical semantic id propagation across graph selection, source reveal, Problem reveal, and outline/document-symbol navigation.
- Confirmed rendered selections resolve from projection semantic ids and occurrence ids, not DOM text, labels, or canvas positions.
- Confirmed same-tab `.athena` outline behavior remains enforced through Theia preferences and active-model document-symbol requests.

### File List

- `_bmad-output/implementation-artifacts/m22/5-2-preserve-source-outline-problems-and-sheet-identity-after-optimization.md`
- `_bmad-output/implementation-artifacts/m22/sprint-status.yaml`
- `docs/usages/m22-proof-usage.md`
- `ide/theia-frontend/scripts/athena-m22-identity-coherence.test.mjs`

## Change Log

- 2026-07-18: Created M22 Story 5.2 with IDE identity coherence requirements.
- 2026-07-18: Added M22 identity coherence guard and documented canonical subject/occurrence behavior.
