---
baseline_commit: 0f4912953312065542f67eec12f3c1fc4133f61c
---

# Story 2.2: Publish The Runtime-Owned Macro Catalog

Status: done

## Story

As a runtime engineer,
I want Athena to publish one active Semantic Macro catalog from repository context,
so that the workbench consumes governed availability instead of hardcoded menus.

## FR Traceability

- FR-1: Athena can define Semantic Macro as a governed reusable assembly contract
- FR-7: Athena can expose available Semantic Macros through a governed reuse catalog
- FR-11: Athena can resolve Semantic Macros through the existing governed package and repository foundation
- NFR-2: The same repository state produces the same governed macro availability
- NFR-6: Workbench surfaces remain consumers of platform-owned reuse services

## Acceptance Criteria

1. Given one governed repository session is active, when the macro catalog is requested, then Athena returns available Semantic Macros from runtime-owned repository context rather than frontend-local discovery.
2. Given the same repository state is reopened, when catalog generation runs again, then the same catalog entries are produced deterministically.

## Tasks / Subtasks

- [x] Populate the runtime-owned catalog seam with governed entries. (AC: 1, 2)
  - [x] Replace the placeholder catalog-only unavailable seam with governed ready/unavailable results.
  - [x] Include stable entry metadata and inspectable diagnostics in the runtime payload.
- [x] Publish the catalog through the existing LSP transport seam. (AC: 1, 2)
  - [x] Extend the typed LSP payload with catalog entries and diagnostics.
  - [x] Keep catalog generation runtime-owned and deterministic across repeated requests.
- [x] Add LSP verification. (AC: 1, 2)
  - [x] Prove a valid governed session returns `ready` with stable empty results when no macro manifests exist.
  - [x] Prove repeated requests over the same governed repository return identical catalog payloads.

## Implementation Notes

- Extended `AthenaSemanticMacroRuntimeService.catalog(...)` to return `AthenaSemanticMacroCatalogReady` or `AthenaSemanticMacroCatalogUnavailable` based on repository graph publication validity.
- Extended `AthenaSemanticMacroProtocol.kt` so `athena/semanticMacroCatalog` now carries typed entry and diagnostic payloads across the LSP boundary.
- Added LSP tests covering both the runtime-owned empty-ready case and a governed multi-package catalog case using one root repository plus one allowed sibling package.

## References

- [Source: _bmad-output/planning-artifacts/epics-M16-2026-07-14.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-14-m16/prd.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m16/ARCHITECTURE-SPINE.md]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticMacroRuntimeService.kt]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticMacroProtocol.kt]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt]
- [Source: ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaReuseRequestTest.kt]

## Story Completion Status

- Status: done
- Completion note: the runtime-owned Semantic Macro catalog is now published through the shared LSP seam with deterministic entry payloads and inspectable diagnostics, and repeated governed requests return stable results.
