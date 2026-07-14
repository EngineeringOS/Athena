---
baseline_commit: 0f4912953312065542f67eec12f3c1fc4133f61c
---

# Story 3.1: Define Parameter Schema And Validation For Macro Instantiation

Status: done

## Story

As a platform engineer,
I want Athena to define explicit parameter schema and validation behavior for Semantic Macros,
so that engineers configure meaningful engineering values instead of editing generated output after the fact.

## FR Traceability

- FR-3: Athena can define stable, typed Semantic Macro parameters with defaults and validation before expansion
- FR-8: Reuse surfaces submit parameter values through shared runtime validation instead of local widget rules
- NFR-2: The same repository state and parameter set produce the same validation outcome

## Acceptance Criteria

1. Given one Semantic Macro declares parameters, when its schema is reviewed, then Athena can express required values, defaults, validation rules, and stable parameter ids.
2. Given a user provides invalid or incomplete values, when instantiation is attempted, then Athena blocks preview generation until validation succeeds.

## Tasks / Subtasks

- [x] Extend the shared Semantic Macro contract model with explicit validation metadata. (AC: 1)
  - [x] Publish allowed values, regex, string bounds, and integer bounds on parameter definitions.
  - [x] Keep stable parameter ids and typed default values in the shared reuse model.
- [x] Parse governed parameter schema from package-scoped macro manifests. (AC: 1)
  - [x] Load parameter kind, label, description, required flag, defaults, and validation rules from `athena-semantic-macros.properties`.
  - [x] Keep catalog resolution and validation anchored to the governed package graph.
- [x] Add runtime-owned validation outcomes that block preview on invalid input. (AC: 2)
  - [x] Return normalized values with defaults applied for valid requests.
  - [x] Return typed diagnostics for unresolved macros, unknown parameters, kind mismatches, and rule violations.
  - [x] Prevent preview generation until validation succeeds.
- [x] Expose parameter schema and validation diagnostics through the LSP transport seam. (AC: 1, 2)
  - [x] Publish parameter definitions, normalized values, diagnostics, and reason fields to frontend consumers.

## Implementation Notes

- Extended `SemanticMacroParameterDefinition` with `SemanticMacroParameterValidationRules` so the shared reuse contract can carry manifest-driven constraints without pushing UI-local rules into product surfaces.
- Reworked `AthenaSemanticMacroCatalogResolver` to parse governed Semantic Macro contracts from package manifests, then reuse those contracts both for catalog entry projection and runtime validation.
- Replaced the previous validation placeholder in `AthenaSemanticMacroRuntimeService` with deterministic validation results: valid requests return normalized values with defaults applied, invalid requests return stable diagnostic codes, and preview is blocked until validation passes.
- Updated the LSP protocol payloads so the frontend can render runtime-owned schema and diagnostics without inventing its own parameter contract.

## Verification

- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain clean"`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:reuse-model:test --tests *SemanticMacroContractTest"`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests *AthenaSemanticMacroRuntimeServiceTest"`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests *AthenaReuseRequestTest"`

## References

- [Source: _bmad-output/planning-artifacts/epics-M16-2026-07-14.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-14-m16/prd.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m16/ARCHITECTURE-SPINE.md]
- [Source: kernel/reuse-model/src/main/kotlin/com/engineeringood/athena/reuse/SemanticMacroModels.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticMacroCatalogResolver.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticMacroRuntimeService.kt]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticMacroProtocol.kt]
- [Source: kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSemanticMacroRuntimeServiceTest.kt]
- [Source: ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaReuseRequestTest.kt]

## Story Completion Status

- Status: done
- Completion note: Semantic Macro parameter schema is now governed, typed, validated in runtime, exposed over LSP, and enforced as a hard gate before preview generation.
