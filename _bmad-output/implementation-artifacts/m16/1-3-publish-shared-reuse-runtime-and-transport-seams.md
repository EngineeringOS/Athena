---
baseline_commit: 0f4912953312065542f67eec12f3c1fc4133f61c
---

# Story 1.3: Publish Shared Reuse Runtime And Transport Seams

Status: done

## Story

As a runtime engineer,
I want Semantic Macro operations to flow through shared runtime and transport seams,
so that reuse services remain platform-owned and reusable by catalog, forms, AI, or later APIs.

## FR Traceability

- FR-2: Athena can publish reuse contracts as dedicated platform models
- NFR-1: M16 introduces no second mutation path outside M8
- NFR-6: Workbench surfaces remain consumers of platform-owned reuse services

## Acceptance Criteria

1. Given the first M16 implementation boundaries are reviewed, when runtime ownership is inspected, then catalog lookup, parameter validation, preview generation, acceptance, and origin inspection are exposed through Athena-owned runtime and `ide/lsp` seams.
2. Given future reuse entrypoints are considered, when the service boundary is checked, then the contract does not depend on one specific Theia panel or graph widget.

## Tasks / Subtasks

- [x] Publish shared runtime-owned Semantic Macro seam types. (AC: 1, 2)
  - [x] Add typed runtime request/result models for catalog, validation, preview, acceptance, and origin inspection.
  - [x] Add `AthenaSemanticMacroRuntimeService` as the shared runtime seam.
  - [x] Keep the seam semantic-first and free of widget-local identifiers.
- [x] Wire the seam into shared runtime execution context. (AC: 1, 2)
  - [x] Add the runtime service to `AthenaServiceRegistry`.
  - [x] Expose the runtime service from `AthenaExecutionContext`.
- [x] Publish matching `ide/lsp` transport seams. (AC: 1, 2)
  - [x] Add typed LSP params and payloads for catalog, validation, preview, acceptance, and origin inspection.
  - [x] Add corresponding `@JsonRequest` methods in `AthenaLanguageServer`.
  - [x] Keep the transport boundary free of panel-specific or graph-widget-specific coupling.
- [x] Add focused verification. (AC: 1, 2)
  - [x] Add runtime tests covering the shared seam behavior.
  - [x] Add LSP request tests covering the transport seam behavior.
  - [x] Run sequential Gradle verification and encoding audit.

## Implementation Notes

- Added `AthenaSemanticMacroRuntimeService` with typed request/result contracts and explicit unavailable responses for later M16 implementation slices.
- Added `AthenaSemanticMacroProtocol.kt` and new LSP requests:
  - `athena/semanticMacroCatalog`
  - `athena/semanticMacroValidation`
  - `athena/semanticMacroPreview`
  - `athena/semanticMacroAccept`
  - `athena/semanticMacroOriginInspection`
- Kept the seam platform-owned: requests carry macro ids, instantiation ids, parameter values, preview ids, and canonical semantic ids only.
- Avoided any dependency on one specific Theia panel, graph widget, or renderer surface.

## References

- [Source: _bmad-output/planning-artifacts/epics-M16-2026-07-14.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-14-m16/prd.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m16/ARCHITECTURE-SPINE.md]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticMacroRuntimeService.kt]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticMacroProtocol.kt]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt]

## Story Completion Status

- Status: done
- Completion note: shared runtime and `ide/lsp` Semantic Macro seams are now implemented and tested, ready for later catalog, validation, preview, acceptance, and origin stories to populate.
