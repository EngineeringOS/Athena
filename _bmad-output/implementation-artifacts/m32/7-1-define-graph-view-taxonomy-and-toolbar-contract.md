---
status: review
baseline_commit: 8d12ba03c05b373b98fbc513e6c7f3f6fce962d6
story_id: 7.1
epic: 7
title: Define Graph View Taxonomy And Toolbar Contract
---

# Story 7.1: Define Graph View Taxonomy And Toolbar Contract

## Status

Review

## Story

As an Athena product owner,
I want Graph View modes and controls named by user-facing engineering concepts,
so that the workbench no longer exposes mixed architecture vocabulary as peer UI concepts.

## Required Context

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-22-m32/prd.md`
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-22-m32/ARCHITECTURE-SPINE.md`
- Epics: `_bmad-output/implementation-artifacts/m32/epics.md`
- Cleanup ledger: `_bmad-output/implementation-artifacts/m32/cleanup-ledger.md`
- Previous closeout: `_bmad-output/implementation-artifacts/m32/6-4-resolve-projection-compatibility-fallbacks-and-close-m32.md`

## Acceptance Criteria

1. Given Graph View exposes view-mode controls, when the taxonomy contract is reviewed, then the
   contract separates projection families, document sheet navigation, presentation profile choice,
   and authoring actions as different UI concepts.
2. Given current labels include `cabinet`, `documentation`, `schematic`, and `Document projection
   sheet view`, when the story completes, then user-facing labels and tests use product language
   such as View, Sheet, Profile, and Create, while internal ids remain transport-safe.
3. Given future stories consume this contract, when Stories 7.2 through 7.5 run, then they must not
   rename controls independently or reintroduce architecture vocabulary into visible UI.
4. Mandatory Polish/Purge Gate complete.

## Tasks/Subtasks

- [x] Use CodeGraph to inspect Graph View view/sheet/profile/action label flow before editing. (AC: 1,2)
- [x] Add a taxonomy contract doc or focused section in existing M32 usage docs that defines:
  projection family, sheet navigation, presentation profile, and authoring action. (AC: 1)
- [x] Add frontend/model tests that fail on visible architecture labels for normal toolbar text,
  aria labels, titles, and smoke proof names. (AC: 2,3)
- [x] Update visible labels and test fixtures only; do not change runtime ids or payload ids in
  this story unless a test proves user-visible leakage. (AC: 2)
- [x] Update Epic 7 cleanup ledger with any retained compatibility label. (AC: 3,4)
- [x] Run focused frontend tests and encoding audit after text/doc edits. (AC: 1..4)
- [x] Complete mandatory polish/purge review and record AC-to-evidence. (AC: 4)

## Dev Notes

- Do not implement sheet visibility or package rendering in this story. Those belong to Stories
  7.2 and 7.4.
- Preserve internal ids like `cabinet`, `documentation`, or `schematic` if they are protocol ids;
  this story is about product-facing text and explicit taxonomy.
- Theia remains an adapter. Do not let UI labels become package/profile authority.
- Use product language. Avoid exposing `projection`, `governed`, `semantic`, or `descriptor` in
  normal controls unless it is inspection/debug evidence.

## Testing Requirements

- TDD required: add failing label/taxonomy tests before UI/doc changes.
- Focused commands:
  - `yarn build` in `ide/theia-frontend`
  - relevant `node --test scripts\*.test.mjs` in `ide/theia-frontend`
- Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after docs/text edits.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- CodeGraph inspection: `athena-graph-workbench-widget.tsx` `renderStageChrome`, `renderCreateEntityActionButton`, `renderSheetViewSelector`, `renderCabinetMainPopover`, and `viewAriaLabel`; `athena-graph-workbench-model.ts` `resolveVisibleAthenaGraphSheetViewSelector`.
- RED proof: `node --test scripts\athena-m32-graph-view-taxonomy.test.mjs` failed on `Cabinet Main information` before label cleanup.
- GREEN proof: `node --test scripts\athena-m32-graph-view-taxonomy.test.mjs scripts\athena-ide-density-contract.test.mjs scripts\athena-m32-create-entity-panel.test.mjs` passed 7/7.
- Build proof: `yarn build` in `ide/theia-frontend` passed.
- Encoding proof: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Added `graph-view-taxonomy-contract.md` defining View, Sheet, Profile, Authoring Action, and Information vocabulary for Epic 7.
- Replaced visible toolbar/panel labels that exposed `Cabinet Main information`, `Document projection sheet view`, and `Create governed entity` with product labels.
- Preserved runtime ids and `data-athena-*` payload ids; raw ids remain allowed only in transport and diagnostic evidence.
- AC-to-evidence: AC1 covered by taxonomy contract doc; AC2 covered by taxonomy/density tests and label update; AC3 covered by the contract and cleanup ledger entry; AC4 covered by focused tests, build, encoding audit, and purge scan through the taxonomy test.

### File List

- _bmad-output/implementation-artifacts/m32/7-1-define-graph-view-taxonomy-and-toolbar-contract.md
- _bmad-output/implementation-artifacts/m32/cleanup-ledger.md
- _bmad-output/implementation-artifacts/m32/graph-view-taxonomy-contract.md
- _bmad-output/implementation-artifacts/m32/sprint-status.yaml
- ide/theia-frontend/scripts/athena-ide-density-contract.test.mjs
- ide/theia-frontend/scripts/athena-m32-graph-view-taxonomy.test.mjs
- ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx

### Change Log

- 2026-07-22: Defined Graph View taxonomy contract, added label regression test, updated product-facing toolbar labels, and recorded verification evidence.

## Mandatory Final Polish/Purge Gate

- Review touched labels, tests, docs, smoke proof names, and adjacent toolbar code.
- Remove or ledger stale terminology.
- Record AC-to-evidence before moving the story to `review`.
