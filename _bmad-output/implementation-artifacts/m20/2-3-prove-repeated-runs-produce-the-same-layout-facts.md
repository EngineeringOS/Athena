---
baseline_commit: b699dda601e216033ed0728d610042887aa82561
---

# Story 2.3: Prove repeated runs produce the same layout facts

Status: done

## Story

As a reviewer,
I want the same input to produce the same layout facts,
so that M20 proves deterministic presentation rather than one-off layout luck.

## Acceptance Criteria

1. Given the same governed input state, when layout runs repeatedly, the emitted layout facts remain stable.
2. Determinism covers sheet ids, occurrence ids, placement, bounds, label facts, routing guidance, and rule metadata exposed by M20.
3. Tests fail on unordered or run-dependent layout output.
4. Determinism coverage is fixture-driven and small.
5. No test assumes random, AI-generated, or browser-layout-dependent placement.

## Tasks / Subtasks

- [x] Add deterministic layout snapshot assertions (AC: 1, 2, 3)
  - [x] Normalize output ordering where the model requires stable order.
  - [x] Assert layout fact equality across repeated runs.
- [x] Cover the dense and baseline fixtures (AC: 2, 4)
  - [x] Include a baseline schematic fixture.
  - [x] Include the dense fixture introduced for M20.
- [x] Guard against frontend/browser dependence (AC: 3, 5)
  - [x] Assert layout facts before renderer output.
  - [x] Avoid viewport-specific measurements in kernel determinism tests.

## Dev Notes

### Current State

- M19 already proved deterministic sheet identity and publication metadata.
- M20 extends determinism to layout facts and drawing-rule output.
- Browser viewport behavior is covered in Epic 3; this story is about model determinism.

### Architectural Guardrails

- Follow M20 AD-3, AD-4, and AD-7.
- Layout is a projection output, not a renderer decision.
- Do not use snapshot tests that accept unstable field ordering.

### Project Structure Notes

- Likely update targets:
  - `kernel/projection-model/src/test/kotlin/com/engineeringood/athena/projection/ProjectionModelContractTest.kt`
  - `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/*Test.kt`
  - `examples/m20/`
- Add helpers only if they reduce repeated canonicalization logic.

### Testing Requirements

- Run projection-model tests first.
- Add runtime tests if layout facts cross into runtime payloads.
- Keep Gradle invocations sequential.

### References

- [Source: `_bmad-output/implementation-artifacts/m20/epics.md` - Epic 2, Story 2.3]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-16-m20/prd.md` - FR-3, FR-4, FR-7]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-16-m20/ARCHITECTURE-SPINE.md` - AD-3, AD-4, AD-7]
- [Source: `_bmad-output/implementation-artifacts/m19/1-4-prove-sheet-determinism-with-executable-fixtures.md`]

## Dev Agent Record

### Agent Model Used

GPT-5

### Debug Log References

- Deterministic layout equality is covered in the projection-model contract test and the runtime projection session test.
- The dense M20 proof fixture repeats the same workbench build and validates stable node, edge, and rule metadata output.
- Verification: `:kernel:projection-model:test`, `:kernel:runtime:test`, `:ide:lsp:test`, `yarn workspace @engineeringood/athena-theia-frontend test`, and `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.

### Completion Notes List

- Layout facts are stable across repeated projection-model runs.
- Baseline and dense fixtures both stay governed and deterministic.
- Browser and viewport dependence are kept out of the kernel determinism checks.

### File List

- `_bmad-output/implementation-artifacts/m20/2-3-prove-repeated-runs-produce-the-same-layout-facts.md`
- `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionSheetLayout.kt`
- `kernel/projection-model/src/test/kotlin/com/engineeringood/athena/projection/ProjectionModelContractTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSessionTest.kt`
- `examples/m20/dense-sheet-proof/README.md`
- `examples/m20/dense-sheet-proof/ready-sheet.diagram.mjs`
- `ide/theia-frontend/scripts/athena-m20-dense-sheet-proof.test.mjs`

## Change Log

- 2026-07-17: Confirmed repeated layout runs stay deterministic across the governed projection, runtime, and dense proof fixtures.
