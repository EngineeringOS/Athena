---
status: done
story_id: 1.2
epic: 1
title: Preserve Canonical Port Identity In Lowering And Semantic Index
baseline_commit: c7fda3877a76fd8db52a65510e1b123aed4304f1
---

# Story 1.2: Preserve Canonical Port Identity In Lowering And Semantic Index

## Story

As an Athena compiler engineer, I want nested ports to lower to the existing `port:Device.port`
identity, so that routing, presentation, references, and diagnostics do not learn a second identity
scheme.

## Acceptance Criteria

- Nested `device D { port p { ... } }` lowers to canonical identity `port:D.p`.
- Source provenance points to the nested port block/name span.
- References to `D.p` resolve to the nested declaration.
- Duplicate nested/top-level declarations for the same canonical port emit a governed diagnostic.

## Tasks/Subtasks

- [x] Add failing compiler/semantic tests for nested port lowering and references.
- [x] Update lowering so nested ports join the same canonical path as legacy ports.
- [x] Update declaration indexing to include nested port declarations.
- [x] Update reference linking and duplicate diagnostics for nested/legacy collisions.
- [x] Run focused compiler semantic tests sequentially.

## Dev Notes

- Architecture: M28 AD-2 and AD-3 are binding.
- Likely files:
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticDeclarationIndexer.kt`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticReferenceLinker.kt`

## Dev Agent Record

### Debug Log

- RED: focused `:kernel:compiler:test` failed because nested ports produced zero lowered ports and semantic index omitted `port:PLC1.out`.
- GREEN: focused `:kernel:compiler:test` passed for nested port lowering, semantic declaration indexing, and nested/top-level duplicate identity diagnostics.
- Regression: `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test` passed.

### Completion Notes

- Added M28 compiler test proving nested ports lower to `StableSemanticIdentity("port:Device.port")`.
- Updated electrical runtime lowering to include `DeviceDeclaration.nestedPorts` in the existing port blueprint path.
- Updated project semantic declaration indexing to flatten nested ports as `port` declarations.
- Added duplicate diagnostic coverage for nested and top-level declarations of the same canonical port.

## File List

- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeLowering.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticDeclarationIndexer.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM28NestedPortCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticDeclarationIndexerTest.kt`

## Change Log

- 2026-07-21: Story created for M28.
- 2026-07-21: Implemented canonical lowering/indexing for nested ports and marked story ready for review.
