---
baseline_commit: f3f81d6
---

# Story 2.2: Define Project Semantic Graph Snapshot Contracts

Status: done

## Story

As a compiler engineer,
I want a compiler-owned project semantic graph snapshot contract,
so that import resolution, linking, LSP, lowering, and tests share one semantic workspace shape.

## Acceptance Criteria

1. `:kernel:compiler` owns one immutable `ProjectSemanticGraphSnapshot` contract exposing `graphId`, `rootPackageId`, ordered packages, source units, namespaces, declarations, bindings, and diagnostics.
2. Package and source-unit records reuse `PackageIdentifier`, `PackageKey`, `SourceUnitId`, and `SourceUnitContentIdentity`; they do not introduce parallel package or source identity vocabularies.
3. Namespace, declaration, and binding records reuse Story 2.1 canonical ids and carry the minimum authored source/span relationships required by later import resolution and linking stories.
4. Snapshot construction canonicalizes every top-level collection and nested dependency/reference collection deterministically, independent of caller collection order.
5. Snapshot construction rejects duplicate identities, a missing root package, dangling package/source/namespace/declaration relationships, and diagnostics that claim unknown source units.
6. Package-aware diagnostic contracts remain compiler-owned, typed, ordered, and transport-neutral; Story 2.5 may populate stable codes without changing the snapshot shape.
7. No repository scan, graph construction, import resolution, linking, lowering, LSP protocol, frontend, canvas, remote service, or new dependency is added.

## Tasks / Subtasks

- [x] Add snapshot contract tests first (AC: 1-6)
  - [x] Prove all required snapshot collections are exposed through compiler-owned types.
  - [x] Prove top-level and nested caller order does not affect canonical snapshot ordering.
  - [x] Prove duplicate and dangling relationships are rejected.
  - [x] Prove diagnostics and related locations are typed and deterministically ordered.
- [x] Implement cohesive semantic graph contracts (AC: 1-7)
  - [x] Add package, source-unit, namespace, declaration, and binding records reusing canonical identities.
  - [x] Add transport-neutral package-aware diagnostic and related-location records.
  - [x] Add one canonical snapshot factory that validates relationships and publishes stable ordering.
- [x] Run scoped verification sequentially
  - [x] Run focused snapshot tests, then `:kernel:compiler:test`.
  - [x] Run `:ide:lsp:test` to protect downstream compiler consumers.
  - [x] Run the encoding audit after text edits.

### Review Findings

- [x] [Review][Patch] Make diagnostic and related-location ordering total and caller-order-independent [kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticGraphSnapshot.kt:218]
- [x] [Review][Patch] Require declarations to use source units listed by their namespace [kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticGraphSnapshot.kt:140]
- [x] [Review][Patch] Reject snapshots whose graph id does not match canonical package and source content [kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticGraphSnapshot.kt:76]
- [x] [Review][Patch] Correct dangling-source coverage and cover every duplicate identity family [kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticGraphSnapshotTest.kt:54]
- [x] [Review][Patch] Prove namespace, declaration, and binding top-level ordering [kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticGraphSnapshotTest.kt:128]
- [x] [Review][Patch] Reject malformed declaration, binding, diagnostic, and related-location spans [kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticGraphSnapshot.kt:132]
- [x] [Review][Patch] Reject directory-shaped source-unit terminal segments and canonicalize blank related messages [kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/CanonicalSemanticIdentityBuilder.kt:125]

## Dev Notes

- Keep all contracts under `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic`.
- Use `ProjectSemanticGraphModels.kt` for closely related graph records, `ProjectSemanticDiagnosticModels.kt` for diagnostics, and `ProjectSemanticGraphSnapshot.kt` for canonical construction/validation.
- Reuse `PackageIdentifier`, `SourceSpan`, and the Story 2.1 identity types. Do not parse identity strings to recover relationships; records must carry typed relationships explicitly.
- Canonical ordering keys are package/source/namespace/declaration/binding id values. Diagnostics sort by code, source id, start/end offsets, and message. Nested collections are distinct and sorted.
- The snapshot is read/analysis state. It must not expose mutable collections or perform I/O.
- This story defines the shared shape only. Story 2.3 constructs it from governed repository state; Story 2.4 resolves imports; Story 2.5 populates package-aware diagnostics.
- Theia/LSP consumes this compiler contract later. EPLAN-style canvas and Kotlin Compose desktop viewer are not involved.

### Previous Story Intelligence

- Story 2.1 established canonical `PackageKey`, `SourceUnitId`, `DeclarationId`, `NamespaceId`, `BindingId`, `GraphId`, package graph identities, source content identities, and deterministic builders.
- Graph identity construction now rejects dangling package dependency edges and source paths with absolute, escaping, or Windows drive-relative forms.
- Package keys preserve existing `name|version-or-empty` repository compatibility; do not redefine or parse that rendering.

### References

- [Source: `epics.md` - Epic 2, Story 2.2]
- [Source: `../../planning-artifacts/architecture/architecture-Athena-2026-07-15-m18/ARCHITECTURE-SPINE.md` - AD-9, AD-12, AD-13]
- [Source: `../../planning-artifacts/prds/prd-Athena-2026-07-15-m18/prd.md` - FR-3, FR-4, FR-5, FR-7, FR-8]
- [Source: `2-1-add-canonical-identity-builders.md`]
- [Source: `kernel/repository-model/src/main/kotlin/com/engineeringood/athena/repository/RepositoryContracts.kt`]
- [Source: `kernel/validation/src/main/kotlin/com/engineeringood/athena/semantics/core/SemanticValidationModel.kt`]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- RED: snapshot tests initially failed compilation because the compiler-owned contracts did not exist.
- GREEN: focused snapshot tests passed after implementing canonical models, diagnostics, and relationship validation.
- Review RED: graph-id mismatch, diagnostic total ordering, malformed spans, and directory-shaped source paths produced expected failures before review fixes.
- Test-fixture correction: removed a dependency package source unit that had been incorrectly assigned to the root namespace.

### Completion Notes List

- Ultimate context engine analysis completed - compiler-owned snapshot contract scope prepared.
- Added one immutable compiler-owned project semantic graph snapshot with packages, source units, namespaces, declarations, bindings, and diagnostics.
- Added deterministic top-level and nested ordering, unmodifiable published lists, and typed relationship validation.
- Enforced snapshot graph-id/content agreement, canonical source paths, namespace membership, duplicate rejection, and valid source spans.
- Resolved all 7 review patch groups from blind, edge-case, and acceptance review layers.
- Verified focused identity/snapshot tests, full `:kernel:compiler:test`, downstream `:ide:lsp:test`, and the encoding audit sequentially.

### File List

- `_bmad-output/implementation-artifacts/m18/2-2-define-project-semantic-graph-snapshot-contracts.md`
- `_bmad-output/implementation-artifacts/m18/sprint-status.yaml`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/CanonicalSemanticIdentityBuilder.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticDiagnosticModels.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticGraphModels.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticGraphSnapshot.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/CanonicalSemanticIdentityBuilderTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticGraphSnapshotTest.kt`

## Change Log

- 2026-07-15: Added compiler-owned canonical project semantic graph snapshot contracts, invariants, tests, and review hardening.
