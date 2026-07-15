---
baseline_commit: e4b0f4d
---

# Story 2.4: Resolve Imports Against The Semantic Graph

Status: done

## Story

As a package author,
I want imports to resolve against the project semantic graph,
so that imported package/source-unit availability is deterministic and inspectable.

## Acceptance Criteria

1. A compiler-owned resolver consumes one `ProjectSemanticGraphSnapshot` and returns the same snapshot shape with canonical import-resolution records derived from each source unit's authored imports.
2. Import availability is limited to the source package and its direct governed dependency keys; no transitive, filesystem, JVM classpath, frontend alias, or parser-internal fallback is consulted.
3. Resolution selects the longest available namespace prefix, preserves any remaining authored target suffix for later declaration linking, and records resolved, unavailable-package, unavailable-namespace, or ambiguous-namespace status.
4. Every resolution explains the source package, direct dependencies, available package keys, available source-unit ids, candidate namespace ids, selected namespace id, and unresolved target suffix.
5. Multiple package versions exposing the same authored namespace remain package-aware and produce deterministic ambiguity rather than caller-order selection.
6. Import records and nested explanation collections are immutable, validated against the snapshot, and deterministically ordered independent of caller collection order.
7. `AthenaCompiler` exposes the shared resolver path; no diagnostics projection, declaration linking, lowering, LSP payload, frontend, canvas, repository scan, remote service, or new dependency is added.

## Tasks / Subtasks

- [x] Add import resolution tests first (AC: 1-6)
  - [x] Prove exact namespace and namespace-plus-symbol-suffix resolution.
  - [x] Prove direct dependency, source-unit, and namespace explanation records.
  - [x] Prove unavailable package/namespace and multi-version ambiguity statuses.
  - [x] Prove deterministic ordering and snapshot validation of derived records.
- [x] Implement compiler-owned import resolution (AC: 1-7)
  - [x] Add status, explanation, and resolution contracts to source-unit semantic records.
  - [x] Add longest-prefix resolver over snapshot packages, source units, and namespaces.
  - [x] Rebuild through the canonical snapshot factory and expose the resolver through `AthenaCompiler`.
- [x] Run scoped verification sequentially
  - [x] Run focused import/builder/snapshot tests, then `:kernel:compiler:test`.
  - [x] Run `:ide:lsp:test` to protect downstream compiler consumers.
  - [x] Run the encoding audit after text edits.

## Dev Notes

- Keep resolution under `kernel/compiler/.../semantic`. The resolver accepts a snapshot only and performs no I/O.
- Add `resolvedImports` to `ProjectSemanticSourceUnit`; do not create an LSP or lowering graph payload.
- Available package keys are the source package plus its direct dependencies. Do not admit transitive packages implicitly.
- Match namespace qualified-name parts as a prefix of the authored import target. Keep only candidates at the longest matching prefix length.
- A selected namespace may leave a suffix such as `PLC1`; Story 3 declaration linking classifies and resolves that suffix later.
- If a known graph package prefix exists but its package key is unavailable to the source, report unavailable-package status. Otherwise, no candidate is unavailable-namespace.
- Story 2.5 converts statuses into stable diagnostics. This story records typed inspectable resolution state only.
- Theia/LSP consumes the snapshot later. EPLAN-style canvas and Kotlin Compose desktop viewer are not involved.

### Previous Story Intelligence

- Story 2.3 preserves canonical authored `ImportDeclaration` values on source units and admits packages/sources only through current repository publication state.
- Story 2.2 canonical snapshot validation owns collection ordering, identity relationships, import span containment, and total diagnostics ordering.
- Story 2.4 must rebuild through `ProjectSemanticGraphSnapshot.canonical` so graph identity remains tied only to governed package/source content.

### References

- [Source: `epics.md` - Epic 2, Story 2.4]
- [Source: `../../planning-artifacts/architecture/architecture-Athena-2026-07-15-m18/ARCHITECTURE-SPINE.md` - AD-3, AD-5, AD-9, AD-12]
- [Source: `../../planning-artifacts/prds/prd-Athena-2026-07-15-m18/prd.md` - FR-3, FR-4, FR-5]
- [Source: `2-3-build-semantic-graph-from-governed-repository-state.md`]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticGraphSnapshot.kt`]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.semantic.ProjectSemanticImportResolverTest` failed before fixes with package-version, longest-prefix, and validation regression tests red.
- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.semantic.ProjectSemanticImportResolverTest` passed after resolver and validation fixes.
- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test` passed.
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test` passed.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Ultimate context engine analysis completed - snapshot-derived import resolution scope prepared.
- Added canonical import-resolution records on semantic source units with immutable explanation payloads.
- Implemented `ProjectSemanticImportResolver` and exposed it through `AthenaCompiler`.
- Addressed review findings by resolving package ownership via the longest matching package-name prefix before namespace selection, preserving unavailable-package namespace candidates, and enforcing zero-or-one-per-authored-import publication.
- Added regression coverage for mixed available/unavailable package versions, longer unavailable package prefixes, longest package group selection, reversed raw collection determinism, and isolated validation mutations.

### File List

- `_bmad-output/implementation-artifacts/m18/2-4-resolve-imports-against-the-semantic-graph.md`
- `_bmad-output/implementation-artifacts/m18/sprint-status.yaml`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticGraphModels.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticGraphSnapshot.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticImportModels.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticImportResolver.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticImportResolverTest.kt`

## Change Log

- 2026-07-15: Added semantic import resolution contracts, resolver, compiler entrypoint, canonical validation, and focused tests.
- 2026-07-15: Addressed code review findings for package-version availability, longest package-prefix ownership, candidate explanations, comparator determinism, and validation isolation.
