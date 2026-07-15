---
baseline_commit: 4bad0d3c9faa36a1bb71f71f7fc7e522f60518aa
---

# Story 3.5: Lower Linked Meaning Into Engineering IR

Status: done

## Story

As a compiler user,
I want linked package-aware authored meaning to lower through the canonical compiler path,
so that Engineering IR remains the canonical truth after linking.

## Acceptance Criteria

1. A compiler-owned linked lowering bridge consumes a linked `ProjectSemanticGraphSnapshot` plus source-unit documents and returns deterministic lowering results tied to the snapshot graph id.
2. Each source unit is lowered through the existing `EngineeringIrLowerer` using its authored AST; no AST paste, hidden include expansion, import text merge, filesystem lookup, frontend lookup, or LSP-only state is introduced.
3. Lowering results preserve linked binding provenance by carrying the binding ids associated with the lowered source unit beside the canonical `EngineeringDocument`.
4. Missing source documents for linked source units fail deterministically with Athena-owned diagnostics instead of falling back to reparsing files or caller-local lookup.
5. Results and diagnostics are deterministic independent of caller collection order.
6. `AthenaCompiler` exposes the shared linked-lowering path without adding frontend logic, canvas behavior, Kotlin Compose desktop-viewer logic, package publishing behavior, or new dependencies.

## Tasks / Subtasks

- [x] Add linked-lowering tests first (AC: 1-5)
  - [x] Prove linked source units lower through `EngineeringIrLowerer` and carry the semantic graph id.
  - [x] Prove source-unit binding ids are preserved beside the lowered document.
  - [x] Prove missing linked source documents emit stable `semantic.lowering.source.missing` diagnostics.
  - [x] Prove no AST paste/include behavior by checking source-unit documents are lowered independently.
  - [x] Prove output remains deterministic from reversed raw input order.
- [x] Implement compiler-owned linked lowering bridge (AC: 1-6)
  - [x] Add cohesive linked-lowering models and bridge under `kernel/compiler/.../semantic`.
  - [x] Reuse `EngineeringIrLowerer` and `CompilerSourceDocument`.
  - [x] Carry graph id, source unit id, binding ids, and canonical `EngineeringDocument`.
  - [x] Emit stable semantic diagnostics for missing source documents.
  - [x] Expose the path through `AthenaCompiler`.
- [x] Run scoped verification sequentially
  - [x] Run focused linked-lowering tests, then `:kernel:compiler:test`.
  - [x] Run `:ide:lsp:test` to protect downstream compiler/LSP consumers.
  - [x] Run the encoding audit after text edits.

## Dev Notes

- Keep implementation compiler-owned. Prefer `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic` unless the compiler facade needs imports.
- Do not touch Theia frontend, EPLAN canvas behavior, or Kotlin Compose desktop-viewer code.
- Do not modify `EngineeringIrLowerer` behavior for M18. Story 3.5 proves orchestration through the existing canonical lowerer.
- Reuse `ProjectSemanticBinding`, `GraphId`, `SourceUnitId`, `CompilerSourceDocument`, `EngineeringIrLowerer`, and `ProjectSemanticDiagnostic`.
- Missing source documents should be diagnostics, not file reads. The caller must provide the source-unit documents it wants lowered.

### Previous Story Intelligence

- Story 3.4 added capability provenance projection without changing lowering.
- Story 3.3 added cross-package reference linking through resolved import-selected namespaces.
- Story 3.2 added source-unit binding records and stable `semantic.reference.*` diagnostics.
- The existing `EngineeringIrLowerer` lowers one `CompilerSourceDocument` from authored AST into canonical `EngineeringDocument`.

### References

- [Source: `epics.md` - Epic 3, Story 3.5]
- [Source: `../../planning-artifacts/prds/prd-Athena-2026-07-15-m18/prd.md` - FR-7]
- [Source: `../../planning-artifacts/architecture/architecture-Athena-2026-07-15-m18/ARCHITECTURE-SPINE.md` - AD-1, AD-5, AD-9, AD-12, AD-13]
- [Source: `3-4-preserve-governed-capability-provenance.md`]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt`]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt`]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.semantic.ProjectSemanticLinkedLowererTest` failed before implementation with missing linked lowerer/compiler API.
- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.semantic.ProjectSemanticLinkedLowererTest` passed after implementation.
- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test` passed.
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test` passed.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Ultimate context engine analysis completed - linked lowering bridge scope prepared.
- Added compiler-owned `ProjectSemanticLinkedLowerer` and linked-lowering result models.
- Lowered source-unit documents through `EngineeringIrLowerer` while carrying graph id, source unit id, and binding ids.
- Added stable `semantic.lowering.source.missing` diagnostics for caller-missing source documents.
- Exposed linked project semantic lowering through `AthenaCompiler.lowerLinkedProjectSemanticSources`.

### File List

- `_bmad-output/implementation-artifacts/m18/3-5-lower-linked-meaning-into-engineering-ir.md`
- `_bmad-output/implementation-artifacts/m18/sprint-status.yaml`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticLinkedLowerer.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticLinkedLowererTest.kt`

## Change Log

- 2026-07-15: Created Story 3.5 from Epic 3, PRD FR-7, architecture AD-1/5/9/12/13, and Story 3.4 implementation intelligence.
- 2026-07-15: Added linked semantic lowering bridge, binding provenance results, missing-source diagnostics, compiler API, and verification evidence.
