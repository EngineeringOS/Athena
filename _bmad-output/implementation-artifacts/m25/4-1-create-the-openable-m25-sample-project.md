---
baseline_commit: 616271a8721cf9fd6538bf8823eaf57a0392074a
status: ready-for-dev
epic: 4
story: 4.1
title: Create the openable M25 sample project
---

# Story 4.1: Create the openable M25 sample project

## Story

As Aaron,
I want a real M25 sample project with `.athena` sources,
So that I can present M25 in the IDE without explaining scripts.

## Acceptance Criteria

- `examples/m25/sample-project` contains real `.athena` files for the six sample families.
- Mandatory path includes PLC/controller, terminal block, power supply, and load/actuator.
- Source syntax is accepted by the existing language stack.
- The sample can be opened through normal Athena Theia IDE workflow.

## Tasks/Subtasks

- [x] Create sample project structure and `.athena` sources.
- [x] Keep syntax limited to already-supported language constructs.
- [x] Add project launch script or package entry if the repo pattern requires it.
- [x] Verify the sample opens in the product path.

## Dev Notes

- Governed by AD-7, AD-9, AD-10.
- No `.mjs`-only proof.

## Dev Agent Record

### Debug Log

- Red: `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests "com.engineeringood.athena.compiler.AthenaM25SampleProjectCompilerTest"` failed because the M25 sample source path was missing.
- Green: the same command passed after creating `examples/m25/sample-project` with real `.athena` sources.

### Completion Notes

- Created the M25 sample project with three source files covering the mandatory PLC/controller, terminal block, power supply, and load/actuator path plus the six-family acceptance slice.
- Kept all sample syntax inside already-admitted constructs: `package`, `system`, `device`, `port`, `connect`, and system-scoped `layout schematic-sheet`.
- Added `start:m25` package entries for the normal Theia product open-workspace path.

### File List

- `_bmad-output/implementation-artifacts/m25/4-1-create-the-openable-m25-sample-project.md`
- `_bmad-output/implementation-artifacts/m25/sprint-status.yaml`
- `examples/m25/sample-project/README.md`
- `examples/m25/sample-project/athena.lock`
- `examples/m25/sample-project/athena.yaml`
- `examples/m25/sample-project/src/01-professional-symbol-sheet.athena`
- `examples/m25/sample-project/src/02-terminal-labels-and-routes.athena`
- `examples/m25/sample-project/src/03-six-family-acceptance.athena`
- `ide/package.json`
- `ide/theia-product/package.json`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM25SampleProjectCompilerTest.kt`

## Change Log

- 2026-07-19: Added openable M25 sample project, Theia launch entry, and compiler coverage.

## Status

done
