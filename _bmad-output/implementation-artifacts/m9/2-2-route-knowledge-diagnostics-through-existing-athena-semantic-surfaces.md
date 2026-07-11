---
baseline_commit: e5f5ef7fb0fbf10f583f0cf9acef52eb6a0e914d
---

# Story 2.2: Route Knowledge Diagnostics Through Existing Athena Semantic Surfaces

Status: done

## Story

As an engineer,
I want knowledge-runtime outputs to appear through existing Athena semantic surfaces,
so that I can consume engineering consequence without a new renderer or editor mode.

## FR Traceability

- FR-6: route engineering impact into existing semantic product paths
- FR-7: surface the first knowledge-runtime proof through existing semantic delivery surfaces
- FR-8: keep graph and UI layers downstream consumers rather than engineering authorities
- NFR-1: keep knowledge delivery upstream of renderer, IDE widgets, and vendor adapters
- NFR-3: knowledge diagnostics and impact consequences remain inspectable at the transport boundary

## Acceptance Criteria

1. Given engineering sufficiency diagnostics and impact consequences are available, when Athena publishes them to product-facing surfaces, then they flow through existing runtime, `ide/lsp`, Problems, semantic inspection, or equivalent governed semantic paths, and M9 does not require a new graphical editor mode, symbol palette, or sheet-management surface.
2. Given renderer and workbench boundaries are reviewed, when delivery responsibilities are checked, then graph and UI layers remain downstream consumers of knowledge outputs, and engineering knowledge authority remains upstream of notation, layout, and renderer state.

## Tasks / Subtasks

- [x] Activate the fixed M9 knowledge-pack source in default runtime-facing JVM compiler sessions. (AC: 1, 2)
  - [x] Add one compiler default helper that resolves reviewed `extensions/knowledge-*` package roots.
  - [x] Reuse that helper in runtime, LSP, and baseline-adapter default compiler construction.
- [x] Publish engineering sufficiency diagnostics through existing Problems-facing LSP diagnostics. (AC: 1)
  - [x] Extend the existing `CompilerCompilationSuccess -> LSP Diagnostic` path to include `engineeringSufficiencyDiagnostics`.
  - [x] Keep knowledge diagnostics additive to the current semantic diagnostics path instead of opening a new request or UI-only warning channel.
- [x] Enrich semantic inspection with current knowledge-runtime state. (AC: 1)
  - [x] Add additive read-only payloads for knowledge diagnostics and current derived/context/fact/evaluation counts.
  - [x] Keep semantic inspection JVM-owned and read-only.
- [x] Expose impact consequences through an existing governed mutation surface. (AC: 1, 2)
  - [x] Carry typed impact consequences and engineering sufficiency diagnostics in runtime-owned source-mutation inspection.
  - [x] Project those additive fields through the existing Athena LSP source-mutation request.
- [x] Add regression-safe tests and update affected docs. (AC: 1, 2)
  - [x] Add runtime tests for knowledge diagnostics and impact on accepted source mutation inspection.
  - [x] Add LSP tests for Problems publication, semantic inspection, and source-mutation transport.
  - [x] Update affected module README files in English and Chinese.
  - [x] Run Gradle verification sequentially on Windows with Java 25.

## Dev Notes

### Story Intent

- Story `2.2` keeps M9 delivery narrow: no new knowledge-only request family, no frontend-owned reconstruction, and no renderer-first surface.
- Engineering sufficiency now reaches the same Problems flow as existing Athena diagnostics.
- Before/after engineering impact now rides the existing runtime-owned source-mutation inspection path, which is already the governed LSP seam for accepted dirty-buffer comparison.

### Completion Notes

- Added `AthenaCompilerDefaults.kt` so runtime-facing compiler sessions can activate reviewed `extensions/knowledge-*` packages without hard-coding frontend paths.
- Extended `AthenaSemanticDiffInspection` with additive `knowledgeDiagnostics` and `impactConsequences`.
- Extended `ide/lsp` semantic inspection with a read-only `knowledgeInspection` payload and extended normal diagnostics publication with `KNOWLEDGE` diagnostics.
- Kept review vocabulary unchanged; Story `2.3` still owns semantic review and SCM wording changes.

## Testing

- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test :ide:lsp:test"`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain test"`

## File List

- `_bmad-output/implementation-artifacts/m9/2-2-route-knowledge-diagnostics-through-existing-athena-semantic-surfaces.md`
- `ide/lsp/README.md`
- `ide/lsp/README.zh-CN.md`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaKnowledgeProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLspSessionHost.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaSourceMutationProtocol.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagnosticsPublishingTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticInspectionTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaSourceMutationRequestTest.kt`
- `integrations/scm-git/src/main/kotlin/com/engineeringood/athena/integrations/scm/git/GitSemanticBaselineAdapter.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompilerDefaults.kt`
- `kernel/runtime/README.md`
- `kernel/runtime/README.zh-CN.md`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticDiffInspection.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaServiceRegistry.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSourceMutationRuntimeService.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSourceMutationRuntimeServiceTest.kt`
