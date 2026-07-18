---
story_id: 1.2
story_key: 1-2-add-tree-sitter-layout-block-parity
epic: 1
epic_title: Parser Parity And Source Fixtures
title: Add Tree-sitter layout-block parity
status: review
created: 2026-07-18
updated: 2026-07-18
baseline_commit: bdbe0295684bd70da89aa436cc302a59157c111f
source_epics: epics.md
source_prd: ../../planning-artifacts/prds/prd-Athena-2026-07-18-m23/prd.md
source_architecture: ../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m23/ARCHITECTURE-SPINE.md
---

# Story 1.2: Add Tree-sitter layout-block parity

## Story

As an IDE user,
I want Tree-sitter to parse the same layout block shape as ANTLR,
So that the editor does not mark valid compiler syntax as broken.

## Acceptance Criteria

**Given** the valid and invalid M23 parser fixtures
**When** Tree-sitter tests run
**Then** valid system-scoped layout blocks parse without error nodes in the admitted syntax
**And** invalid `place`, `align`, `group`, axis, and file-global fixture families recover predictably
**And** Tree-sitter node names are documented for downstream highlighting and structural feedback

## Developer Context

Story 1.1 admitted the syntax into ANTLR only. This story updates the IDE syntax UX parser path.
Tree-sitter remains syntax UX only; it must not perform semantic binding or compiler diagnostics.

Admitted Tree-sitter syntax must mirror the ANTLR shape:

```athena
system MachineNo000 {
  layout schematic-sheet {
    place HMI1 near PLC1
    place XT1 below PLC1
    align HMI1 aligned-with PLC1 axis vertical
    align HMI2 aligned-with PLC1 axis horizontal
    group HMI1 grouped-with PLC1
  }
}
```

File-global `layout` remains invalid.

## Architecture Guardrails

- Follow `AD-2`: Tree-sitter parity is mandatory with the ANTLR syntax from Story 1.1.
- Follow `AD-7`: Tree-sitter may parse, recover, and highlight; it must not resolve subjects or emit semantic truth.
- Follow `AD-8`: existing package/import/system/device/port/connect corpus behavior must remain green.
- Follow `AD-10`: do not add route/label syntax, raw coordinates, EPLAN parity, AI layout, or ecosystem/library scope.

## Tasks/Subtasks

- [x] Add failing Tree-sitter corpus coverage for valid system-scoped layout blocks and invalid layout syntax families.
- [x] Extend `ide/tree-sitter-athena/grammar.js` with layout declarations and statement nodes.
- [x] Update highlight queries and README node documentation for syntax UX only.
- [x] Regenerate Tree-sitter generated parser artifacts and wasm if the local toolchain supports it.
- [x] Run Tree-sitter verification and update Dev Agent Record, File List, Change Log, and story status.

## Dev Agent Record

### Debug Log

- 2026-07-18: Started Story 1.2 from `backlog`; baseline commit `bdbe0295684bd70da89aa436cc302a59157c111f`.
- 2026-07-18: Red phase verified with `yarn --cwd ide/tree-sitter-athena test`; layout corpus failed because the grammar did not parse `layout_declaration`.
- 2026-07-18: First green attempt exposed a generator ambiguity for optional layout closing brace; root cause was conflict between layout `}` and surrounding system `}`.
- 2026-07-18: Fixed by requiring the layout block closing brace, matching ANTLR admission.
- 2026-07-18: `yarn --cwd ide/tree-sitter-athena test` passed after grammar/corpus updates.
- 2026-07-18: `yarn --cwd ide/tree-sitter-athena build` regenerated `tree-sitter-athena.wasm`; command succeeded with existing `wasm-ld` shared-library stability warning.
- 2026-07-18: Re-ran `yarn --cwd ide/tree-sitter-athena test` after WASM rebuild; passed.

### Completion Notes

- Added Tree-sitter layout syntax nodes for system-scoped `layout` declarations, placement, alignment, grouping, view family, relation, and axis.
- Added corpus coverage for valid system-scoped layout and invalid file-global layout recovery.
- Updated syntax highlighting and README node documentation while preserving Tree-sitter as syntax UX only.
- Regenerated Tree-sitter generated sources and the WASM artifact used by Theia.

### File List

- `_bmad-output/implementation-artifacts/m23/1-2-add-tree-sitter-layout-block-parity.md`
- `_bmad-output/implementation-artifacts/m23/sprint-status.yaml`
- `ide/tree-sitter-athena/README.md`
- `ide/tree-sitter-athena/grammar.js`
- `ide/tree-sitter-athena/queries/highlights.scm`
- `ide/tree-sitter-athena/src/grammar.json`
- `ide/tree-sitter-athena/src/node-types.json`
- `ide/tree-sitter-athena/src/parser.c`
- `ide/tree-sitter-athena/test/corpus/layout.txt`
- `ide/tree-sitter-athena/tree-sitter-athena.wasm`

### Change Log

- 2026-07-18: Added Tree-sitter M23 layout-block parity and regenerated parser/WASM artifacts.

## Status

Review. Story 1.2 is ready for code review in `sprint-status.yaml`.
