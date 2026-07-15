---
baseline_commit: 6dd7312fe15bb7cd54ce90845c4390c058710424
---

# Story 3.1: Index Declarations Into Semantic Namespaces

Status: done

## Story

As a compiler engineer,
I want authored declarations indexed into semantic namespaces,
so that imports can expose declaration availability across source units and packages.

## Acceptance Criteria

1. A compiler-owned declaration indexer consumes one `ProjectSemanticGraphSnapshot` and returns the same snapshot shape with canonical declaration records derived from admitted source units.
2. Indexed declarations include deterministic declaration ids, namespace ids, package ids through the owning namespace/source unit, source unit ids, declaration kind, qualified authored names, and authored source spans.
3. Namespaces publish deterministic declaration-id indexes for declarations authored in their source units.
4. Duplicate or ambiguous declaration availability is represented deterministically with Athena-owned diagnostics rather than caller-order selection.
5. Declaration records, namespace declaration indexes, and diagnostics are immutable, validated against the snapshot, and deterministically ordered independent of caller collection order.
6. `AthenaCompiler` exposes the shared declaration-indexing path without adding LSP payloads, reference linking, lowering behavior, frontend logic, canvas behavior, Kotlin Compose desktop-viewer logic, or new dependencies.

## Tasks / Subtasks

- [x] Add declaration indexing tests first (AC: 1-5)
  - [x] Prove declarations are derived from parsed source units and attached to their semantic namespace.
  - [x] Prove declaration ids and namespace declaration indexes are deterministic from reversed raw input order.
  - [x] Prove duplicate authored declarations emit stable diagnostics instead of caller-order selection.
  - [x] Prove canonical snapshot validation rejects declarations outside known namespaces/source units.
- [x] Implement compiler-owned declaration indexing (AC: 1-6)
  - [x] Add a cohesive declaration indexer under `kernel/compiler/.../semantic`.
  - [x] Parse admitted source-unit content through the existing compiler parser and derive declaration records for the current M18 proof slice.
  - [x] Rebuild namespaces with declaration ids and rebuild through `ProjectSemanticGraphSnapshot.canonical`.
  - [x] Expose the path through `AthenaCompiler`.
- [x] Run scoped verification sequentially
  - [x] Run focused declaration-indexing tests, then `:kernel:compiler:test`.
  - [x] Run `:ide:lsp:test` to protect downstream compiler consumers.
  - [x] Run the encoding audit after text edits.

## Dev Notes

- Keep implementation in `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic`. This is compiler semantic logic only.
- Do not touch Theia frontend, EPLAN canvas behavior, or Kotlin Compose desktop-viewer code.
- Reuse `ProjectSemanticDeclaration`, `ProjectSemanticNamespace.declarationIds`, `CanonicalSemanticIdentityBuilder.declarationId`, and `ProjectSemanticGraphSnapshot.canonical`.
- The current language AST already parses source units for package/import syntax and authored engineering declarations. Use structured AST contracts, not text search, for declaration extraction.
- Declaration ids are source-unit id plus normalized declaration kind plus qualified authored name within the M18 proof slice.
- Duplicate declaration diagnostics should use stable `semantic.declaration.*` codes and source/span provenance.
- Story 3.2 owns reference linking. This story must not resolve references or create bindings.

### Previous Story Intelligence

- Story 2.5 added `ProjectSemanticDiagnosticProjector` and stable import-resolution diagnostic codes, and made top-level diagnostics distinct during canonicalization.
- Story 2.4 added resolved import records and package-aware namespace resolution.
- Story 2.3 established source units and namespaces from governed repository state and parsed package declarations.

### References

- [Source: `epics.md` - Epic 3, Story 3.1]
- [Source: `../../planning-artifacts/prds/prd-Athena-2026-07-15-m18/prd.md` - FR-5]
- [Source: `../../planning-artifacts/architecture/architecture-Athena-2026-07-15-m18/ARCHITECTURE-SPINE.md` - AD-5, AD-6, AD-7, AD-9, AD-12, AD-13]
- [Source: `2-5-emit-typed-package-aware-diagnostics.md`]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticGraphModels.kt`]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticGraphSnapshot.kt`]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Local adversarial code review found cross-source declaration ambiguity was not diagnosed by declaration-id duplicate grouping; added/fixed the `semantic.declaration.ambiguous` path before closeout.
- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.semantic.ProjectSemanticDeclarationIndexerTest` failed before the ambiguity fix on the cross-source ambiguity regression.
- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.semantic.ProjectSemanticDeclarationIndexerTest` passed after implementation.
- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test` passed.
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test` passed.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Ultimate context engine analysis completed - compiler-owned declaration indexing scope prepared.
- Added compiler-owned `ProjectSemanticDeclarationIndexer` for authored device and port declarations.
- Preserved parsed authored declarations on semantic source units and validated authored declaration spans during snapshot canonicalization.
- Rebuilt namespace declaration-id indexes through `ProjectSemanticGraphSnapshot.canonical`.
- Added deterministic duplicate and ambiguous declaration diagnostics using stable `semantic.declaration.*` codes.
- Exposed declaration indexing through `AthenaCompiler.indexProjectSemanticDeclarations`.

### File List

- `_bmad-output/implementation-artifacts/m18/3-1-index-declarations-into-semantic-namespaces.md`
- `_bmad-output/implementation-artifacts/m18/sprint-status.yaml`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/GovernedProjectSemanticGraphBuilder.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticDeclarationIndexer.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticGraphModels.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticGraphSnapshot.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticDeclarationIndexerTest.kt`

## Change Log

- 2026-07-15: Created Story 3.1 from Epic 3, PRD FR-5, architecture AD-5/6/7/9/12/13, and Epic 2 implementation intelligence.
- 2026-07-15: Added compiler-owned semantic declaration indexing, authored declaration preservation, deterministic diagnostics, compiler API, and verification evidence.
