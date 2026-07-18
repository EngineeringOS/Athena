---
story_id: 1.1
story_key: 1-1-admit-system-scoped-layout-blocks-in-antlr
epic: 1
epic_title: Parser Parity And Source Fixtures
title: Admit system-scoped layout blocks in ANTLR
status: review
created: 2026-07-18
updated: 2026-07-18
baseline_commit: bdbe0295684bd70da89aa436cc302a59157c111f
source_epics: epics.md
source_prd: ../../planning-artifacts/prds/prd-Athena-2026-07-18-m23/prd.md
source_architecture: ../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m23/ARCHITECTURE-SPINE.md
---

# Story 1.1: Admit system-scoped layout blocks in ANTLR

## Story

As a language implementer,
I want ANTLR4 to parse system-scoped layout blocks,
So that compiler and LSP parsing can accept the M23 source contract.

## Acceptance Criteria

**Given** a `.athena` source file with `layout schematic-sheet { ... }` inside `system { ... }`
**When** the ANTLR parser runs
**Then** it accepts `place SUBJECT near TARGET`, `place SUBJECT below TARGET`, `align SUBJECT aligned-with TARGET axis horizontal|vertical`, and `group SUBJECT grouped-with TARGET`
**And** it rejects file-global layout blocks
**And** existing M0-M22 parser fixtures still pass
**And** package/import syntax remains unchanged

## Developer Context

M23 corrects the M22 overclaim: the layout block was selected and previewed, but not admitted into
the real `.athena` language. This story starts with ANTLR because ANTLR is the compiler/LSP parser
path. Do not create a user-facing sample proof in this story unless it is explicitly marked as a
fixture; the visible sample becomes honest only after Tree-sitter parity and compiler admission.

The admitted syntax is system-scoped first:

```athena
system MachineNo000 {
  device PLC1 {
    type Switch
  }

  layout schematic-sheet {
    place HMI1 near PLC1
    place XT1 below PLC1
    align HMI1 aligned-with PLC1 axis vertical
    group HMI1 grouped-with PLC1
  }
}
```

File-global layout blocks are not admitted in M23.

## Architecture Guardrails

- Follow `AD-1`: `layout <view-family> { ... }` is allowed inside `system { ... }` only.
- Follow `AD-2`: this story starts ANTLR parity, but M23 is not done until Tree-sitter is updated in Story 1.2.
- Follow `AD-3`: generated parser types stay internal. This story may expose parse-tree shape only to the parser adapter/tests.
- Follow `AD-8`: preserve package/import/system/device/port/connect behavior.
- Follow `AD-10`: do not add route/label syntax, EPLAN parity, raw coordinates, AI layout, or repository/library scope.

## Likely Files

- `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4`
- Existing parser tests under `kernel/language/src/test`
- Existing compiler or parser smoke tests that currently cover package/import/system syntax
- Generated ANTLR outputs, if this repo commits generated parser sources for the language module

CodeGraph showed the current layout model already has relationship concepts such as `NEAR`,
`BELOW`, `ALIGNED_WITH`, `GROUPED_WITH`, `LayoutAxis`, and layout constraints. Do not use this story
to lower into those models yet; that belongs to Epic 2 and Epic 3.

## Implementation Notes

- Prefer grammar rules that make the admitted statement forms explicit rather than a generic free-form command list.
- Treat `schematic-sheet` as a view-family token shape compatible with lower-hyphen names.
- Treat subjects and targets as authored identifiers for now; semantic binding happens later.
- Keep `horizontal` and `vertical` as the only admitted axis values.
- Do not admit raw position syntax such as `x`, `y`, `position`, `at`, or coordinate tuples.
- Add negative coverage for file-global `layout schematic-sheet { ... }`.

## Test Requirements

Run verification sequentially. Do not run Gradle tasks concurrently on Windows.

Minimum expected verification:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:language:test
```

If language tests are wired under a different module name, use the nearest existing sequential parser
test task and record the exact command in the story dev notes.

After touching docs or text assets, run:

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
```

## Done Evidence Required

- Valid system-scoped layout block fixture parses through ANTLR.
- Invalid file-global layout block fixture is rejected.
- Existing package/import parser tests still pass.
- No M23 sample or usage doc claims IDE acceptance until Tree-sitter and LSP stories complete.

## Tasks/Subtasks

- [x] Add failing grammar smoke tests for valid system-scoped layout blocks and rejected file-global layout blocks.
- [x] Extend `Athena.g4` to admit only the M23 ANTLR layout statement vocabulary inside `system`.
- [x] Verify existing package/import/system/device/port/connect grammar behavior remains green.
- [x] Update Dev Agent Record, File List, Change Log, and story status after verification.

## Dev Agent Record

### Debug Log

- 2026-07-18: Started Story 1.1 from `ready-for-dev`; baseline commit `bdbe0295684bd70da89aa436cc302a59157c111f`.
- 2026-07-18: Red phase verified with `.\gradlew.bat --no-daemon --console=plain :kernel:language:test`; failed on unresolved generated parser method `layoutDecl`.
- 2026-07-18: Green phase verified with the same task after grammar admission; build succeeded.

### Completion Notes

- Added grammar-level smoke coverage for valid system-scoped M23 layout blocks and rejected file-global layout blocks.
- Extended ANTLR grammar with explicit `layoutDecl`, `placeStatement`, `alignStatement`, and `groupStatement` rules.
- Kept this story scoped to ANTLR grammar admission only; authored AST, Tree-sitter parity, compiler lowering, LSP, and sample project proof remain later M23 stories.

### File List

- `_bmad-output/implementation-artifacts/m23/1-1-admit-system-scoped-layout-blocks-in-antlr.md`
- `_bmad-output/implementation-artifacts/m23/sprint-status.yaml`
- `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/antlr/AthenaGrammarSmokeTest.kt`

### Change Log

- 2026-07-18: Admitted system-scoped M23 layout block grammar in ANTLR and added red/green grammar smoke tests.

## Status

Review. Story 1.1 is ready for code review in `sprint-status.yaml`.
