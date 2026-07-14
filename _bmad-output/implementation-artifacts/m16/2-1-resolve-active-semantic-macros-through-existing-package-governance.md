---
baseline_commit: 0f4912953312065542f67eec12f3c1fc4133f61c
---

# Story 2.1: Resolve Active Semantic Macros Through Existing Package Governance

Status: done

## Story

As a compiler engineer,
I want Athena to resolve active Semantic Macros only from the governed repository and package graph,
so that M16 reuses M5 authority instead of inventing a second package system.

## FR Traceability

- FR-7: Athena can expose available Semantic Macros through a governed reuse catalog
- FR-11: Athena can resolve Semantic Macros through the existing governed package and repository foundation
- NFR-2: The same repository state produces the same governed macro availability

## Acceptance Criteria

1. Given a repository has a locked package graph, when active Semantic Macros are resolved, then only packs available through that graph may contribute macro definitions.
2. Given M16 reproducibility is reviewed, when package-governance boundaries are inspected, then `athena.lock` remains the reproducibility anchor and no second lockfile or ad hoc macro resolver is introduced.

## Tasks / Subtasks

- [x] Reuse the existing repository graph publication as the only macro-discovery authority. (AC: 1, 2)
  - [x] Resolve macro manifests only from packages present in the active published graph.
  - [x] Keep repository-root validation and `athena.lock` publication as the source of reproducibility truth.
- [x] Add governed package-scoped macro manifest loading. (AC: 1, 2)
  - [x] Load `athena-semantic-macros.properties` only from allowed package roots.
  - [x] Validate manifest format version, required fields, and definition paths.
  - [x] Reject definition paths that escape the package root.
- [x] Add deterministic runtime verification. (AC: 1, 2)
  - [x] Prove graph-only packages contribute entries.
  - [x] Prove packages outside the locked graph do not appear in the active catalog.

## Implementation Notes

- Added `AthenaSemanticMacroCatalogResolver` and `AthenaSemanticMacroPackageLoader` under `kernel/runtime` to load governed Semantic Macro manifests strictly from the published package graph.
- Added typed catalog entries and diagnostics to the runtime seam so runtime can explain malformed package-scoped manifests without falling back to filesystem-wide discovery.
- Kept the reproducibility anchor unchanged: catalog resolution starts from `repositoryReports().publishRepositoryGraphReport(...)` and never introduces a second resolver, lockfile, or frontend-owned package scan.

## References

- [Source: _bmad-output/planning-artifacts/epics-M16-2026-07-14.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-14-m16/prd.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m16/ARCHITECTURE-SPINE.md]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticMacroCatalogResolver.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticMacroRuntimeService.kt]
- [Source: kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSemanticMacroRuntimeServiceTest.kt]

## Story Completion Status

- Status: done
- Completion note: active Semantic Macros now resolve only from the governed repository graph and package roots already admitted by `athena.lock`, with deterministic runtime tests covering inclusion and exclusion boundaries.
