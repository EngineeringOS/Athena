---
baseline_commit: 0f4912953312065542f67eec12f3c1fc4133f61c
---

# Story 3.2: Build Deterministic Expansion Preview For Configured Instantiations

Status: done

## Story

As a runtime engineer,
I want Athena to generate a deterministic preview of one configured Semantic Macro,
so that engineers can inspect semantic consequences before acceptance.

## FR Traceability

- FR-4: Athena can build a deterministic expansion preview
- NFR-2: The same repository state and parameter set produce the same preview outcome
- NFR-4: Preview remains deterministic and inspectable

## Acceptance Criteria

1. Given one Semantic Macro and one valid parameter set, when preview generation runs, then Athena returns the components, ports, connections, origin anchors, and presentation consequences that would result from acceptance.
2. Given the same repository state and parameter set, when preview generation runs again, then the same preview is produced deterministically.

## Tasks / Subtasks

- [x] Load reusable template definitions through the governed package graph. (AC: 1)
  - [x] Add runtime-owned template loading that stays anchored to the resolved package root and manifest contract.
  - [x] Keep template diagnostics and missing-definition handling inside runtime-owned preview generation.
- [x] Publish deterministic preview results from runtime. (AC: 1, 2)
  - [x] Build stable preview ids from macro id and instantiation id.
  - [x] Return deterministic components, ports, connections, origin anchors, presentation consequences, and warnings from runtime.
  - [x] Reuse validation as the hard gate before preview generation proceeds.
- [x] Expose preview payloads through the LSP seam. (AC: 1, 2)
  - [x] Publish typed preview request and payload contracts through `ide/lsp`.
  - [x] Keep the transport stable for frontend and later acceptance consumers.

## Implementation Notes

- Added runtime-owned Semantic Macro template loading and deterministic preview assembly under `kernel/runtime`, including governed package-root resolution and stable preview identifiers.
- Extended `AthenaSemanticMacroRuntimeService` so valid parameter sets now return typed preview consequences instead of an unavailable seam placeholder.
- Published preview transport payloads through `AthenaSemanticMacroProtocol.kt` and reused the same boundary from Theia-facing consumers.
- Kept preview read-only and inspectable: approval still remains a later M8-backed handoff, not a hidden write path.

## Verification

- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests *AthenaSemanticMacroRuntimeServiceTest"`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests *AthenaReuseRequestTest"`

## References

- [Source: _bmad-output/planning-artifacts/epics-M16-2026-07-14.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-14-m16/prd.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m16/ARCHITECTURE-SPINE.md]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticMacroDefinitionLoader.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticMacroRuntimeService.kt]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticMacroProtocol.kt]
- [Source: kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSemanticMacroRuntimeServiceTest.kt]
- [Source: ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaReuseRequestTest.kt]

## Story Completion Status

- Status: done
- Completion note: Athena now produces deterministic Semantic Macro preview consequences through runtime and LSP, with governed template loading and validation-owned preview gating.
