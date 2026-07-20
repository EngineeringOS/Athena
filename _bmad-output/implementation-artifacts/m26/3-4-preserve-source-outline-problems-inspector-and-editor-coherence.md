---
baseline_commit: f407e9fc48ec28b40b909a326af28993d3edb3c2
---

# Story 3.4: Preserve Source, Outline, Problems, Inspector, And Editor Coherence

Status: done

## Story

As an Athena user,
I want document projection navigation to stay coherent with source and IDE side panels,
so that selecting projected occurrences does not create stale views or duplicate editor tabs.

## Acceptance Criteria

1. Source reveal continues to use canonical subject identity and source ranges where available.
2. Outline and Problems behavior remains tied to active project/source identity, not sheet names or
   canvas labels.
3. Inspector data exposes document location, related occurrences, relation type, compact notation,
   and canonical identity for document references.
4. Navigating to another occurrence in the same `.athena` file reuses the existing editor tab.
5. Regression tests cover stale-state prevention for switching active files and sheet views.

## Tasks / Subtasks

- [x] Add document-reference inspection model data (AC: 2, 3)
  - [x] Build inspection facts from reference marker payloads and selected canonical identity.
  - [x] Include relation type, compact notation, document location, source/target occurrences, and
        canonical identity.
- [x] Integrate inspection data into existing Graphical View info path (AC: 2, 3)
  - [x] Surface compact document reference rows without creating canvas-owned reference state.
  - [x] Preserve existing route and representation inspection behavior.
- [x] Add coherence regression coverage (AC: 1, 4, 5)
  - [x] Verify source reveal remains canonical and same-tab.
  - [x] Verify sheet-view switching does not treat stale inactive metadata as active selection.

## Dev Notes

- Active frontend is Theia only.
- Do not touch `apps:desktop-viewer`, `ui:compose-workbench`, or deprecated KMP frontend modules.
- Build on `resolveAthenaGraphReferenceMarkerNavigation()` from Story 3.3.
- Do not add new `.athena` syntax.
- Verification must run sequentially on Windows.

### References

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-20-m26/prd.md`
  - FR-10
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-20-m26/ARCHITECTURE-SPINE.md`
  - AD-11: Theia navigates through the occurrence index
- Previous story:
  - `_bmad-output/implementation-artifacts/m26/3-3-navigate-continuation-and-cross-reference-clicks-through-occurrence-index.md`

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- Story started after Story 3.3 marker navigation verification.
- `yarn test` in `ide/theia-frontend` failed in red phase because
  `buildAthenaGraphDocumentReferenceInspection` did not exist.
- `yarn test` in `ide/theia-frontend` passed after adding document-reference inspection and
  Graphical View info rows.

### Completion Notes List

- Added `buildAthenaGraphDocumentReferenceInspection()` for selected canonical identities.
- Added inspector-facing document reference rows with relation type, compact notation, target
  occurrence, and document location.
- Preserved existing canonical source reveal, Problems, route inspection, and representation
  inspection coverage.
- Confirmed existing stale inactive sheet metadata and same-tab editor reveal tests remain green.

### File List

- `_bmad-output/implementation-artifacts/m26/3-4-preserve-source-outline-problems-inspector-and-editor-coherence.md`
- `_bmad-output/implementation-artifacts/m26/sprint-status.yaml`
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`
- `ide/theia-frontend/scripts/athena-graph-workbench-model.test.mjs`
- `ide/theia-frontend/scripts/athena-m24-graph-workbench-preservation.test.mjs`

## Change Log

- 2026-07-20: Created Story 3.4 from M26 Epic 3.
- 2026-07-20: Implemented document reference inspection coherence in the Theia Graphical View.
- 2026-07-20: Marked Story 3.4 done after frontend verification.
