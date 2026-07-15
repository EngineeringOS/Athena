---
baseline_commit: 19fe0683da44bb12bb1fe64aca395338a06f624d
---

# Story 3.2: Link References Across Source Units

Status: done

## Story

As a package author,
I want authored references to bind across source units in the same governed workspace,
so that multi-file authored source behaves as one semantic workspace.

## Acceptance Criteria

1. A compiler-owned reference linker consumes a declaration-indexed `ProjectSemanticGraphSnapshot` and returns the same snapshot shape with canonical `ProjectSemanticBinding` records for supported authored references across source units in the same namespace.
2. The M18 proof slice links `ConnectionDeclaration` endpoint `QualifiedName` references to indexed `ProjectSemanticDeclaration` records without text search, AST paste, filesystem lookup, frontend heuristics, or LSP-only state.
3. Bindings include deterministic binding ids, source unit ids, authored reference spans, and resolved declaration ids; target provenance remains available through the referenced declaration record.
4. Same-workspace unresolved or ambiguous reference candidates emit deterministic Athena-owned diagnostics with stable `semantic.reference.*` codes and source/span provenance.
5. Binding records and diagnostics are immutable, validated by `ProjectSemanticGraphSnapshot.canonical`, and deterministically ordered independent of caller collection order.
6. `AthenaCompiler` exposes the shared reference-linking path without adding LSP payloads, cross-package linking, lowering behavior, frontend logic, canvas behavior, Kotlin Compose desktop-viewer logic, or new dependencies.

## Tasks / Subtasks

- [x] Add reference-linking tests first (AC: 1-5)
  - [x] Prove a connection endpoint in one source unit resolves to a port declaration in another source unit in the same semantic namespace.
  - [x] Prove binding ids and binding ordering are deterministic from reversed raw input order.
  - [x] Prove unresolved endpoint references emit a stable `semantic.reference.unresolved` diagnostic with the endpoint span.
  - [x] Prove ambiguous endpoint references emit a stable `semantic.reference.ambiguous` diagnostic instead of caller-order selection.
  - [x] Prove canonical snapshot validation rejects bindings outside known source units or declarations.
- [x] Implement compiler-owned same-namespace reference linking (AC: 1-6)
  - [x] Add a cohesive reference linker under `kernel/compiler/.../semantic`.
  - [x] Reuse indexed `ProjectSemanticDeclaration` records and namespace declaration indexes; do not rescan source text.
  - [x] Extract supported reference sites from structured `ConnectionDeclaration` endpoints only.
  - [x] Rebuild bindings and diagnostics through `ProjectSemanticGraphSnapshot.canonical`.
  - [x] Expose the path through `AthenaCompiler`.
- [x] Run scoped verification sequentially
  - [x] Run focused reference-linking tests, then `:kernel:compiler:test`.
  - [x] Run `:ide:lsp:test` to protect downstream compiler/LSP consumers.
  - [x] Run the encoding audit after text edits.

## Dev Notes

- Keep implementation in `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic`. This is compiler semantic logic only.
- Do not touch Theia frontend, EPLAN canvas behavior, or Kotlin Compose desktop-viewer code.
- Reuse `ProjectSemanticDeclarationIndexer`, `ProjectSemanticBinding`, `CanonicalSemanticIdentityBuilder.bindingId`, `ProjectSemanticGraphSnapshot.canonical`, and existing diagnostic model types.
- The current authored AST exposes `ConnectionDeclaration(from: QualifiedName, to: QualifiedName)` and `QualifiedName.span`; use those structured contracts for reference sites.
- The current declaration index supports `device` declarations by simple name and `port` declarations by qualified authored name. For this story, link connection endpoints against indexed `port` declarations first; device linking and richer reference kinds are later growth unless required by tests.
- Same-namespace means source units listed under the same `ProjectSemanticNamespace`. Cross-package/cross-governed-package reference linking belongs to Story 3.3.
- Diagnostics should use stable `semantic.reference.*` codes and source/span provenance:
  - `semantic.reference.unresolved`
  - `semantic.reference.ambiguous`
- Do not remove declaration ambiguity diagnostics from Story 3.1. If ambiguous declarations already exist, the linker should not silently pick one.
- Story 3.5 owns lowering linked meaning into Engineering IR. This story must not change lowering behavior.

### Previous Story Intelligence

- Story 3.1 added `ProjectSemanticDeclarationIndexer`, preserved parsed authored declarations on semantic source units, rebuilt namespace declaration-id indexes, and exposed `AthenaCompiler.indexProjectSemanticDeclarations`.
- Story 3.1 local review found cross-source declaration ambiguity must be diagnosed separately from declaration-id duplicates because declaration ids include source unit ids.
- Story 2.5 established stable diagnostic projection and idempotent top-level diagnostic canonicalization.
- Story 2.4 established import resolution records and package-aware namespace resolution.

### References

- [Source: `epics.md` - Epic 3, Story 3.2]
- [Source: `../../planning-artifacts/prds/prd-Athena-2026-07-15-m18/prd.md` - FR-5]
- [Source: `../../planning-artifacts/architecture/architecture-Athena-2026-07-15-m18/ARCHITECTURE-SPINE.md` - AD-5, AD-6, AD-7, AD-9, AD-12, AD-13]
- [Source: `3-1-index-declarations-into-semantic-namespaces.md`]
- [Source: `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt`]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticDeclarationIndexer.kt`]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticGraphModels.kt`]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticGraphSnapshot.kt`]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.semantic.ProjectSemanticReferenceLinkerTest` failed before implementation with missing linker/compiler API.
- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.semantic.ProjectSemanticReferenceLinkerTest` passed after implementation.
- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test` passed.
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test` passed.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Ultimate context engine analysis completed - same-namespace reference-linking scope prepared.
- Added compiler-owned `ProjectSemanticReferenceLinker` for same-namespace `ConnectionDeclaration` endpoint linking.
- Linked supported authored references to indexed `port` declarations using `ProjectSemanticBinding` records and canonical binding ids.
- Added stable `semantic.reference.unresolved` and `semantic.reference.ambiguous` diagnostics with endpoint source-span provenance.
- Rebuilt linked snapshots through `ProjectSemanticGraphSnapshot.canonical` and preserved idempotent binding publication.
- Exposed reference linking through `AthenaCompiler.linkProjectSemanticReferences`.

### File List

- `_bmad-output/implementation-artifacts/m18/3-2-link-references-across-source-units.md`
- `_bmad-output/implementation-artifacts/m18/sprint-status.yaml`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticReferenceLinker.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticReferenceLinkerTest.kt`

## Change Log

- 2026-07-15: Created Story 3.2 from Epic 3, PRD FR-5, architecture AD-5/6/7/9/12/13, and Story 3.1 implementation intelligence.
- 2026-07-15: Added compiler-owned same-namespace reference linking, stable reference diagnostics, compiler API, and verification evidence.
