---
baseline_commit: dd2a463
---

# Story 2.3: Build Semantic Graph From Governed Repository State

Status: done

## Story

As a package author,
I want Athena to build semantic workspace meaning from governed repository state,
so that imports resolve through `athena.yaml`, `athena.lock`, and the resolved package graph.

## Acceptance Criteria

1. A compiler-owned builder consumes one valid `AthenaRepositoryReportPublicationResult` plus explicit package/source-root-relative source inputs and returns a `ProjectSemanticGraphBuildResult`.
2. Snapshot package admission comes only from the publication's canonical `ResolvedPackageGraph`; source admission requires an exact governed `PackageIdentifier`/`PackageKey` match.
3. Source inputs are parsed only through `AthenaLanguageParser`, use canonical source/content identities, and contribute package namespaces only when their authored package declaration matches the admitted package.
4. The built snapshot carries a graph id derived from canonical resolved package nodes and admitted source content identities, with deterministic output independent of caller source order.
5. Invalid publication state, unknown packages, duplicate source identities, package declaration mismatch/missing state, unsupported/nonportable package source roots, and syntax failures produce typed compiler-owned diagnostics.
6. Raw-path and frontend-style import attempts remain syntax failures; the builder performs no fallback filesystem, JVM classpath, frontend alias, or parser-internal resolution. Syntactically valid imports remain unresolved authored intent for Story 2.4.
7. `AthenaCompiler` exposes the shared builder path; no LSP payload, linking, lowering, frontend, canvas, remote service, repository rescan, or new dependency is added.

## Tasks / Subtasks

- [x] Add governed semantic graph builder tests first (AC: 1-6)
  - [x] Prove deterministic single/cross-package package, source-unit, namespace, and graph-id construction.
  - [x] Prove only publication-admitted packages and package-relative source inputs enter the snapshot.
  - [x] Prove invalid publication, duplicate/unknown source, package mismatch/missing, source-root, and syntax diagnostics.
  - [x] Prove raw-path/frontend syntax attempts do not trigger fallback resolution.
- [x] Implement the compiler-owned builder flow (AC: 1-7)
  - [x] Add source input and build result contracts.
  - [x] Map governed resolved packages and source inputs into Story 2.2 snapshot records.
  - [x] Aggregate matching authored package namespaces and preserve syntactically valid imports for Story 2.4.
  - [x] Expose the builder through `AthenaCompiler` without changing existing single-source compilation.
- [x] Run scoped verification sequentially
  - [x] Run focused builder/repository tests, then `:kernel:compiler:test`.
  - [x] Run `:ide:lsp:test` to protect downstream compiler consumers.
  - [x] Run the encoding audit after text edits.

### Review Findings

- [x] [Review][Patch] Preserve the existing positional `AthenaCompiler` lowerer parameter [kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt:82]
- [x] [Review][Patch] Make authored import ordering total and require target spans inside import spans [kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticGraphSnapshot.kt:255]
- [x] [Review][Patch] Distinguish malformed graph identities from nonportable package roots [kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/GovernedProjectSemanticGraphBuilder.kt:27]
- [x] [Review][Patch] Require exact governed package identifiers for source admission [kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/GovernedProjectSemanticGraphBuilder.kt:219]
- [x] [Review][Patch] Canonicalize rejected-build diagnostics with the complete snapshot ordering [kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/GovernedProjectSemanticGraphBuilder.kt:261]
- [x] [Review][Patch] Isolate and strengthen deterministic, duplicate, namespace, syntax, publication, and source-root tests [kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/GovernedProjectSemanticGraphBuilderTest.kt:22]

## Dev Notes

- Place builder contracts and behavior under `kernel/compiler/.../semantic`; accept compiler repository publication types rather than paths.
- `ProjectSemanticSourceInput` carries governed `PackageIdentifier`, source-root-relative path, and source content. It must never carry or resolve arbitrary filesystem paths.
- Treat `publication.isValid` and its canonical graph/lock state as the admission authority. Do not call repository loaders, materializers, scanners, `Files.walk`, class loaders, or frontend services.
- Convert repository diagnostics to transport-neutral `ProjectSemanticDiagnostic` values when publication admission fails.
- Parse source content through the public `AthenaLanguageParser` facade only. Preserve valid `ImportDeclaration` values in source records for Story 2.4; do not classify package-versus-symbol targets here.
- Extend `ProjectSemanticSourceUnit` with immutable authored package/import syntax needed by later semantic resolution; snapshot canonicalization must keep import order deterministic by target and span.
- Build namespace records from matching package declarations with empty declaration ids until Story 3.1 indexes declarations.
- A resolved package source root must remain canonical and relative. Reject machine-specific absolute roots with a typed diagnostic instead of embedding them in graph identity.
- Theia/LSP consumes the snapshot later. EPLAN-style canvas and Kotlin Compose desktop viewer are not involved.

### Previous Story Intelligence

- Story 2.2 added an immutable canonical snapshot factory that validates graph-id/content agreement, package/source/namespace relationships, source spans, duplicates, and total diagnostic ordering.
- Story 2.1/2.2 source identity normalization rejects absolute, escaping, drive-relative, and directory-shaped source-unit paths.
- Snapshot package and source records must be fully canonical before `ProjectSemanticGraphSnapshot.canonical` is invoked.

### References

- [Source: `epics.md` - Epic 2, Story 2.3]
- [Source: `../../planning-artifacts/architecture/architecture-Athena-2026-07-15-m18/ARCHITECTURE-SPINE.md` - AD-1, AD-3, AD-5, AD-9, AD-12]
- [Source: `../../planning-artifacts/prds/prd-Athena-2026-07-15-m18/prd.md` - FR-3, FR-4]
- [Source: `2-2-define-project-semantic-graph-snapshot-contracts.md`]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryReportPublicationModel.kt`]
- [Source: `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageParser.kt`]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- RED: focused builder tests failed compilation because the governed builder contracts and compiler entry point did not exist.
- GREEN: focused builder tests passed after implementing governed package/source admission and snapshot construction.
- Test-fixture correction: namespace assertions now follow canonical namespace-id ordering rather than display-name ordering.
- Review RED: exact package collisions and import ordering/containment tests failed before review fixes.

### Completion Notes List

- Ultimate context engine analysis completed - governed builder boundary prepared.
- Added explicit package-relative source inputs and deterministic build results over current valid repository publication state.
- Added compiler-owned package/source/namespace construction, parser-facade usage, graph-id derivation, and authored import preservation.
- Added typed invalid-publication, graph, package, source, duplicate, package-header, path, and syntax diagnostics without fallback resolution.
- Exposed the same builder through `AthenaCompiler` while preserving existing positional constructor behavior.
- Resolved all 6 review patch groups; one fallback-interaction test request was dismissed because the builder has no resolver dependency or I/O capability to observe.
- Verified focused builder/snapshot tests, full `:kernel:compiler:test`, downstream `:ide:lsp:test`, and the encoding audit sequentially.

### File List

- `_bmad-output/implementation-artifacts/m18/2-3-build-semantic-graph-from-governed-repository-state.md`
- `_bmad-output/implementation-artifacts/m18/sprint-status.yaml`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/GovernedProjectSemanticGraphBuilder.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/GovernedProjectSemanticGraphModels.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticGraphModels.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticGraphSnapshot.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/GovernedProjectSemanticGraphBuilderTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticGraphSnapshotTest.kt`

## Change Log

- 2026-07-15: Added governed repository-backed semantic graph construction, compiler entry point, diagnostics, tests, and review hardening.
