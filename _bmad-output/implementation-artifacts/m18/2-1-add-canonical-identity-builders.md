---
baseline_commit: 7edefed
---

# Story 2.1: Add Canonical Identity Builders

Status: done

## Story

As an IDE and compiler engineer,
I want canonical identity builders for package-aware semantic subjects,
so that diagnostics, navigation, linking, and lowering join the same objects.

## Acceptance Criteria

1. Compiler-owned typed values exist for package keys, source unit ids, declaration ids, namespace ids, binding ids, source content identities, and graph ids.
2. Package keys reuse the existing governed `PackageIdentifier` name/version normalization rather than introducing a parallel package vocabulary.
3. Source unit ids combine package key plus normalized source-root-relative path, use `/`, preserve extension/case, collapse `.`/internal `..`, and reject absolute or escaping paths.
4. Declaration ids combine source unit id, normalized declaration kind, and qualified authored name; namespace ids combine package key and qualified namespace name.
5. Binding ids combine source unit id, exact reference span offsets, and resolved declaration id.
6. Graph ids deterministically hash the root package, canonical resolved package nodes/dependency edges, and canonical ordered source-unit content identities; caller collection order does not affect identity, while semantic graph/content changes do.
7. Builders add no repository scan, graph construction, import resolution, linking, lowering, LSP, frontend, canvas, remote service, or new dependency.

## Tasks / Subtasks

- [x] Add identity tests first (AC: 1-6)
  - [x] Prove package key compatibility and deterministic typed renderings.
  - [x] Prove path normalization and absolute/escaping rejection.
  - [x] Prove declaration, namespace, and binding component sensitivity.
  - [x] Prove graph id order independence and package/content sensitivity.
- [x] Implement compiler-owned canonical identity contracts (AC: 1-7)
  - [x] Group small related value/data types in a cohesive models file.
  - [x] Implement one builder object using deterministic canonical serialization and SHA-256.
  - [x] Route the existing repository graph resolver package-key helper through the canonical builder.
- [x] Run scoped verification sequentially
  - [x] Run focused identity/resolver tests, then `:kernel:compiler:test`.
  - [x] Run `:ide:lsp:test` to protect downstream compiler integration.
  - [x] Run the encoding audit after text edits.

### Review Findings

- [x] [Review][Patch] Reject Windows drive-relative source paths [kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/CanonicalSemanticIdentityBuilder.kt:117]
- [x] [Review][Patch] Reject dependency edges outside the resolved package graph [kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/CanonicalSemanticIdentityBuilder.kt:71]
- [x] [Review][Patch] Complete deterministic rendering and component-sensitivity coverage [kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/CanonicalSemanticIdentityBuilderTest.kt:14]

## Dev Notes

- Place contracts under `kernel/compiler/.../semantic`; Story 2.2 builds the snapshot model on them.
- Existing repository graph code renders package keys as `name|version-or-empty`; preserve that compatibility and replace its private duplicate helper.
- Canonical serialization must be collision-resistant: length-prefix variable text before hashing/combining rather than relying on ambiguous delimiter concatenation outside the package-key compatibility value.
- Source path normalization is lexical and repository-relative. Do not call `toRealPath`, inspect disk, lowercase paths, remove extensions, or apply host-specific separators.
- Graph package nodes include package key, normalized source root, and canonical direct dependency keys. Graph source content uses SHA-256 identities, never raw source in public ids.
- Use Java `MessageDigest`; add no hashing dependency.
- Theia/LSP, EPLAN-style canvas, and Kotlin Compose desktop viewer are not involved.

### References

- [Source: `epics.md` - Epic 2, Story 2.1]
- [Source: `../../planning-artifacts/architecture/architecture-Athena-2026-07-15-m18/ARCHITECTURE-SPINE.md` - AD-12, AD-13]
- [Source: `../../planning-artifacts/prds/prd-Athena-2026-07-15-m18/prd.md` - FR-3, NFR-3, NFR-4]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryGraphResolver.kt` - existing package-key behavior]
- [Source: `kernel/repository-model/src/main/kotlin/com/engineeringood/athena/repository/RepositoryContracts.kt` - `PackageIdentifier`]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- RED: focused identity tests rejected neither `C:outside.athena` nor dangling graph dependency edges (2 expected failures).
- GREEN: focused identity tests passed after adding drive-prefix and resolved-edge guards.

### Completion Notes List

- Ultimate context engine analysis completed - canonical identity contract prepared.
- Added compiler-owned typed package, source, declaration, namespace, binding, content, and graph identities.
- Added deterministic length-prefixed serialization, lexical source path normalization, SHA-256 content/graph identities, and resolved-edge validation.
- Reused the canonical package-key builder from the existing repository graph resolver.
- Resolved all 3 review patches; 5 findings were dismissed as governed compatibility, snapshot-contract scope, or unsupported assumptions.
- Verified focused identity/resolver tests, full `:kernel:compiler:test`, downstream `:ide:lsp:test`, and the encoding audit sequentially.

### File List

- `_bmad-output/implementation-artifacts/m18/2-1-add-canonical-identity-builders.md`
- `_bmad-output/implementation-artifacts/m18/sprint-status.yaml`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryGraphResolver.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/CanonicalSemanticIdentityBuilder.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/SemanticIdentityModels.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/CanonicalSemanticIdentityBuilderTest.kt`

## Change Log

- 2026-07-15: Added canonical semantic identity contracts/builders, repository package-key reuse, tests, and review hardening.
