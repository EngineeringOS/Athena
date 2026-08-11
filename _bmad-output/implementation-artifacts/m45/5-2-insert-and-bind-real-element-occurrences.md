---
baseline_commit: 9ca7c80239d38d9e2a4880886e71e27e2923c41b
---

# Story 5.2: Insert and Bind Real Element Occurrences

Status: done

## Story

As an engineer,
I want to insert package-backed Elements,
so that source and scene stay synchronized.

## Acceptance Criteria

1. Typed insertion intent requires full Source Revision, admitted Element identity, existing Function id, Sheet id, and logical placement.
2. Server stages source/Sheet/binding/lock writes, validates, recompiles, journals, and publishes only after durable commit.
3. Invalid package, Function, placement, or revision leaves source and previous scene unchanged.
4. Persisted placement uses semantic/logical coordinates, never raw canvas X/Y.

## Tasks / Subtasks

- [x] Task 1 (AC: 1, 4)
  - [x] Add typed insert intent contract with logical placement and binding identity.
- [x] Task 2 (AC: 2, 3)
  - [x] Route insert through existing Source Revision transaction service and compiler publication gate.
  - [x] Add rejection/no-partial-write tests.
- [x] Task 3 (AC: 1, 3, 4)
  - [x] Run focused LSP/compiler tests and audits sequentially.

## Dev Notes

- Reuse existing `EditOperation`, `SourceRevision`, `PlacementOperationHandler`, `RepresentationBindingCompanion`, and transaction journal. Do not create a frontend source writer.
- Placement authority is `SemanticPlacementIntent -> LogicalLayoutCoordinate -> compiler-derived geometry`; raw X/Y is forbidden in source.
- Package browser emits intent only. No `.elmt`, HTML, XML, reference-tree, fallback, or compatibility path.

### References

- [Source: _bmad-output/planning-artifacts/m45/epics.md#Story 5.2]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-08-m45/ARCHITECTURE-SPINE.md#AD-12]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-08-m45/ARCHITECTURE-SPINE.md#AD-20]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Removed transitional native-source scanning from insertion handler. Element admission now comes only from compiler-materialized `athena-lock-v3` Package Index entries.
- Curated M45 package SVGs to `svg-safe-1`; prior namespace/`use`/`text` content correctly blocked publication.

### Completion Notes List

- `InsertElementOccurrence` requires `function:<Entity>.<Function>`, admitted `ELEMENT` identity/version, Sheet id, logical coordinates, and full Source Revision.
- Compiler parses native package Athena sources and materializes typed Package Index entries (`kind`, identity/version, digest, source path, resource references) into canonical lock output.
- Accepted insertion stages binding Companion and Sheet Companion writes, recompiles, preserves occurrence identity, and commits through existing transaction/journal publication gate.
- Invalid package identity test proves source, Sheet, lock, scene, binding, and journal remain unchanged.
- Verification passed: focused handler tests, compiler lock tests, M45 package catalog test, encoding audit, source-set hygiene audit.

### File List

- `kernel/repository-model/src/main/kotlin/com/engineeringood/athena/repository/RepositoryContracts.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryLockMaterializer.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/LocalPackageManifestParser.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/NativePackageItemIndexCompiler.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/RepresentationAndEngineeringOperationHandler.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/RepresentationAndEngineeringOperationHandlerTest.kt`
- `examples/m45/rolling-shutter/athena.lock`
- `examples/m45/rolling-shutter/packages/` native manifests, Athena declarations, and curated SVG resources

### Change Log

- 2026-08-09: Created BMad story context; implementation started.
- 2026-08-09: Implemented compiler-owned Package Index admission and transactional Element insertion; status moved to review.
