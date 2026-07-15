---
baseline_commit: 5ce5805c1193b9662b4ec9dbd32c83618147f3f9
---

# Story 3.6: Add Linking And Lowering Proof Fixtures

Status: done

## Story

As a maintainer,
I want executable fixtures for successful and failing package-aware linking,
so that cross-package semantics are proven before IDE integration.

## Acceptance Criteria

1. `examples/m18/linking-lowering-proof/` contains local governed proof fixtures for single-package success, cross-source-unit success, cross-package success, unresolved symbol, and invalid availability.
2. Compiler tests execute the fixtures through project semantic graph construction shape, import resolution where needed, declaration indexing, reference linking, and linked lowering.
3. Successful fixture cases produce deterministic bindings and linked lowering results without AST paste, source include, frontend lookup, canvas lookup, or remote registry behavior.
4. Failing fixture cases produce stable `semantic.reference.*` diagnostics with source/span provenance.
5. The proof README documents the local governed scope and explicitly excludes marketplace, registry, publish, multi-root, frontend-owned semantic resolution, and desktop-viewer behavior.
6. Verification runs sequentially through focused proof tests, `:kernel:compiler:test`, `:ide:lsp:test`, and encoding audit.

## Tasks / Subtasks

- [x] Add M18 linking/lowering proof fixtures (AC: 1, 5)
  - [x] Add single-package success fixture.
  - [x] Add cross-source-unit success fixtures.
  - [x] Add cross-package success fixtures.
  - [x] Add unresolved symbol and invalid availability fixtures.
  - [x] Add proof README with scope boundaries.
- [x] Add executable compiler proof tests (AC: 2-4)
  - [x] Execute successful fixtures through import/declaration/link/lowering semantic passes.
  - [x] Assert deterministic bindings and linked lowering results.
  - [x] Assert failure fixtures emit stable diagnostics.
  - [x] Keep tests compiler-owned and independent of frontend/canvas/desktop-viewer behavior.
- [x] Run scoped verification sequentially
  - [x] Run focused proof tests, then `:kernel:compiler:test`.
  - [x] Run `:ide:lsp:test`.
  - [x] Run the encoding audit after text edits.

## Dev Notes

- Use `examples/m18/linking-lowering-proof/` to keep this proof slice separate from syntax-only fixtures.
- Keep all fixture paths relative in docs.
- Reuse existing compiler semantic passes from Stories 3.1-3.5.
- Do not add repository marketplace, registry, publish, multi-root, frontend semantic resolution, canvas, or Kotlin Compose desktop-viewer behavior.

### Previous Story Intelligence

- Story 3.5 added `ProjectSemanticLinkedLowerer`.
- Story 3.4 added namespace capability provenance projection.
- Story 3.3 added cross-package reference linking through resolved imports.
- Story 3.2 added same-namespace reference linking.
- Story 3.1 added declaration indexing.

### References

- [Source: `epics.md` - Epic 3, Story 3.6]
- [Source: `../../planning-artifacts/prds/prd-Athena-2026-07-15-m18/prd.md` - FR-10]
- [Source: `../../planning-artifacts/architecture/architecture-Athena-2026-07-15-m18/ARCHITECTURE-SPINE.md` - AD-10]
- [Source: `3-5-lower-linked-meaning-into-engineering-ir.md`]
- [Source: `examples/m18/README.md`]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.semantic.AthenaM18LinkingLoweringProofTest` failed before fixture-root resolution was made robust for module test working directories.
- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.semantic.AthenaM18LinkingLoweringProofTest` passed after implementation.
- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test` passed.
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test` passed.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Ultimate context engine analysis completed - linking/lowering proof fixture scope prepared.
- Added `examples/m18/linking-lowering-proof/` fixtures for single-package, cross-source, cross-package, unresolved symbol, and invalid availability behavior.
- Added proof README documenting local governed scope boundaries and excluded frontend/canvas/desktop-viewer/registry behavior.
- Added `AthenaM18LinkingLoweringProofTest` to execute fixtures through import resolution, declaration indexing, reference linking, and linked lowering.
- Verified successful fixtures produce bindings/lowering results and failing fixtures produce stable `semantic.reference.unresolved` diagnostics.

### File List

- `_bmad-output/implementation-artifacts/m18/3-6-add-linking-and-lowering-proof-fixtures.md`
- `_bmad-output/implementation-artifacts/m18/sprint-status.yaml`
- `examples/m18/linking-lowering-proof/README.md`
- `examples/m18/linking-lowering-proof/single-package-success.athena`
- `examples/m18/linking-lowering-proof/cross-source-provider.athena`
- `examples/m18/linking-lowering-proof/cross-source-consumer.athena`
- `examples/m18/linking-lowering-proof/cross-package-vendor.athena`
- `examples/m18/linking-lowering-proof/cross-package-consumer.athena`
- `examples/m18/linking-lowering-proof/unresolved-symbol.athena`
- `examples/m18/linking-lowering-proof/invalid-availability-consumer.athena`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/AthenaM18LinkingLoweringProofTest.kt`

## Change Log

- 2026-07-15: Created Story 3.6 from Epic 3, PRD FR-10, architecture AD-10, and Story 3.5 implementation intelligence.
- 2026-07-15: Added M18 linking/lowering proof fixtures, executable compiler proof tests, and verification evidence.
