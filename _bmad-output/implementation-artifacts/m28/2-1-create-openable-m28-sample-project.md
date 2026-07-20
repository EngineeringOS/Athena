---
status: done
story_id: 2.1
epic: 2
title: Create Openable M28 Sample Project
baseline_commit: c7fda3877a76fd8db52a65510e1b123aed4304f1
---

# Story 2.1: Create Openable M28 Sample Project

## Story

As an engineer, I want an M28 sample project with nested ports and candidate relationships, so that
authoring can be proven through the product path.

## Acceptance Criteria

- `examples/m28/sample-project` opens in Theia Graphical View without projection errors.
- Sample uses nested device-owned ports.
- Sample contains one compatible unconnected electrical endpoint pair.
- Sample contains at least two invalid relationship pairs for rejection proof.
- Sample preserves M27 visual density expectations.

## Tasks/Subtasks

- [x] Create `examples/m28/sample-project`.
- [x] Add source files using nested ports.
- [x] Add compatible and invalid relationship candidates.
- [x] Add compiler/sample test.
- [x] Verify Theia can open the project path.

## Dev Notes

- Architecture: M28 AD-2, AD-3, AD-6, AD-7 are binding.
- Do not treat `.athena` files as sheets.

## Dev Agent Record

### Debug Log

- RED: `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests "com.engineeringood.athena.compiler.AthenaM28SampleProjectCompilerTest"` failed because M28 sample sources were missing.
- GREEN: same test passed after creating `examples/m28/sample-project`.
- Documentation/text verification: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes

- Created M28 sample workspace with nested device-owned ports.
- Added one valid unconnected electrical relationship candidate in the primary source.
- Added invalid output/output and input/input candidate subjects in the second source.
- Added compiler/linking test proving source files compile and authored references link without ambiguity.

## File List

- `examples/m28/sample-project/athena.yaml`
- `examples/m28/sample-project/athena.lock`
- `examples/m28/sample-project/README.md`
- `examples/m28/sample-project/src/01-relationship-authoring-source.athena`
- `examples/m28/sample-project/src/02-relationship-candidates.athena`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM28SampleProjectCompilerTest.kt`

## Change Log

- 2026-07-21: Story created for M28.
- 2026-07-21: Created openable M28 sample project and marked story ready for review.
