---
baseline_commit: 9ca7c80239d38d9e2a4880886e71e27e2923c41b
---

# Story 1.1: Discover Direct Local Package Catalog

Status: done

## Story

As a compiler maintainer,
I want direct `packages/<package-name>` catalog discovery,
so that package identity, dependency intent, and resolved roots are deterministic.

## Acceptance Criteria

1. Given a governed repository containing immediate children under `packages/`, when repository loading
   runs, then each child containing one valid `package.yaml` is parsed as a local package candidate and
   selected dependencies become nodes in the existing compiler-owned `ResolvedPackageGraph`.
2. Given a package nested deeper than `packages/<package-name>`, a manifest `packageId` unequal to its
   directory name, an unsafe/symlink/path-escaping root, duplicate identity, or any dependency targeting
   `reference/elements` or `reference/elements_contrib`, when discovery runs, then resolution fails closed
   with deterministic plain-language diagnostics naming exact package/path, problem, and correction.
3. Given root `athena.yaml`, when local package dependencies resolve, then authored dependency intent
   remains in `athena.yaml`; discovery does not auto-add every catalog entry. A typed server operation for
   inserting an undeclared package dependency updates `athena.yaml` before lock materialization. This
   story defines/uses the dependency-intent operation contract but does not implement Lock V3 (Story 1.3).
4. Existing `ResolvedPackageGraph`, `PackageIdentifier`, repository loader/resolver/report surfaces remain
   the only graph authority. `representationPackageRoots`, `LocalPackageRegistry`, root-priority selection,
   and representation/engineering split resolver paths are removed from production and their stale tests
   are deleted or rewritten for current behavior. No compatibility adapter or fallback parser remains.
5. Existing non-package repository validation remains green: primary package/source-root validation,
   local dependency cycle/ambiguity diagnostics, package declarations, Sheet companions, and safe local
   path enforcement still work through the consolidated loader/resolver.

## Tasks / Subtasks

- [x] Task 1: Specify direct-child native package manifest discovery (AC: 1, 2)
  - [x] Add test fixtures for valid immediate children and each rejected path/identity case.
  - [x] Define minimal package catalog candidate using repository `PackageIdentifier`; do not add M45
        PackageItem payloads, admission states, or V3 lock fields yet.
  - [x] Parse `package.yaml` with structured YAML APIs and deterministic diagnostics.
- [x] Task 2: Merge catalog selection into existing repository resolution (AC: 1, 3, 5)
  - [x] Treat `athena.yaml` dependencies as selection intent over direct local catalog candidates.
  - [x] Feed selected candidates into `AthenaRepositoryGraphResolver`; prohibit a second graph/resolver.
  - [x] Preserve current local-path and local-package dependency validation where compatible with M45.
- [x] Task 3: Define undeclared package dependency intent operation seam (AC: 3)
  - [x] Add typed `ADD_PACKAGE_DEPENDENCY` operation and server mapping for `athena.yaml` dependency write.
  - [x] Reuse Source Revision/transaction conventions; frontend insertion UX remains deferred.
  - [x] Prove lock materialization runs only after `athena.yaml` transaction publication.
- [x] Task 4: Delete retired package discovery paths (AC: 4)
  - [x] Remove `representationPackageRoots` from manifest loader/validation/LSP feature code and tests.
  - [x] Remove production `LocalPackageRegistry`, `LocalPackageResolver`, root-priority models, and tests
        proving retired precedence/engineering-vs-representation behavior.
  - [x] Delete `RepresentationPackageSnapshotStager` and obsolete snapshot-only models/tests.
        if its only purpose is the retired root mechanism. Do not add an adapter.
- [x] Task 5: Verify regressions and hygiene (AC: 1-5)
  - [x] Run focused repository/compiler/package-runtime/LSP tests sequentially.
  - [x] Run affected broad test suite sequentially; never overlap Gradle invocations.
  - [x] Run encoding and source-set hygiene audits.
  - [x] Scan production/tests/examples for `representationPackageRoots`, `LocalPackageRegistry`, and
        forbidden reference-runtime dependencies; expected remaining hits must be documentation only.

## Dev Notes

### Authority and Scope

- This story establishes catalog discovery and graph convergence only. Story 1.2 owns common PackageItem
  authored/admitted contracts. Story 1.3 owns `athena-lock-v3`. Story 1.4 owns AdmissionReport/state.
- External catalog formats are not Athena inputs. No parser, converter, importer, package adapter, or
  runtime path for them may be added.
- Direct `packages/<package-name>` children are candidates. Root `athena.yaml` remains dependency intent.
  Do not silently resolve all packages merely because their folders exist.
- One graph only: `ResolvedPackageGraph`. Do not create `PackageCatalogGraph`, representation graph, or
  package-runtime resolver beside compiler repository resolution.

### Existing Code To Change

- `kernel/compiler/.../repository/AthenaRepositoryContractLoader.kt`
  - Current: reads `athena.yaml`, validates source roots, separately parses `representationPackageRoots`.
  - Change: remove representation roots; discover/parse direct package manifests; return catalog facts to
    the existing repository resolution pipeline.
  - Preserve: primary package, package declaration, Sheet companion, safe path, and source-root validation.
- `kernel/compiler/.../repository/AthenaRepositoryGraphResolver.kt`
  - Current: recursively resolves `LOCAL_PATH` and `LOCAL_PACKAGE` dependencies into one graph.
  - Change: resolve selected direct-child candidates through same nodes/diagnostics/order.
  - Preserve: cycle detection, ambiguity rejection, stable ordering, path normalization, root-first graph.
- `kernel/compiler/.../repository/AthenaRepositoryContractValidationModel.kt`
  - Remove `representationPackageRoots`; add only catalog facts actually required by loader/resolver.
- `kernel/package-runtime/.../LocalPackageRegistry.kt`, `LocalPackageResolver.kt`, registry/resolution models
  - Retired second authority. Delete if no current non-retired caller remains; fold any reusable value into
    repository contracts without compatibility wrappers.
- `kernel/package-runtime/.../RepresentationPackageSnapshotStager.kt`
  - Remove parsing of `representationPackageRoots`; consume canonical graph/snapshot input or delete.
- `ide/lsp/.../AthenaLanguageFeatures.kt`
  - Remove manifest completion/symbol handling for retired `representationPackageRoots`.

### Contract Rules

- Reuse `com.engineeringood.athena.repository.PackageIdentifier`; do not use or retain duplicate
  `EngineeringPackageId` / `RepresentationPackageId` for graph identity.
- Package directory name equals manifest `packageId.name`. Version remains manifest identity data, not
  part of directory name.
- Discovery is immediate-child only. Use normalized/real paths and containment checks. Symlink escape,
  nested manifest, missing manifest, duplicate identity, malformed YAML, or ambiguous selection fails.
- Diagnostics lead with human correction; internal diagnostic code remains stable but secondary.
- Structured YAML parsing only. No regex/string ad hoc manifest parser for new `package.yaml` contract.
- No backward compatibility. Rewrite/delete stale tests and examples rather than preserving retired YAML.

### Testing Requirements

- Red tests first for immediate-child success, nested rejection, id mismatch, missing manifest, duplicate
  id, symlink/path escape, reference-tree dependency rejection, selection by `athena.yaml`, and stable graph
  ordering.
- Regression tests for existing cycles, ambiguous `LOCAL_PACKAGE`, unsafe locator, missing dependency,
  source-root contract, package declarations, and Sheet companion validation.
- Tests prove absent retired production symbols/path parsing, not merely unused code.
- Gradle commands run strictly sequentially. If cache corruption symptoms occur, run full clean once, then
  rerun sequentially.

### Project Structure Notes

- Keep common repository identity/graph data in `kernel/repository-model`.
- Keep filesystem/YAML discovery and graph orchestration in `kernel/compiler/.../repository`.
- Do not move package-authoring payloads into repository-model during Story 1.1.
- Production Kotlin files follow role grouping; split files only when responsibilities exceed local rule.

### References

- [M45 Epic 1](../../planning-artifacts/m45/epics.md#epic-1-package-authority-and-resolution)
- [M45 Architecture AD-1/AD-2/AD-19](../../planning-artifacts/architecture/architecture-Athena-2026-08-08-m45/ARCHITECTURE-SPINE.md)
- [M45 PRD FR-1](../../planning-artifacts/prds/prd-Athena-2026-08-08-m45/prd.md#fr-1-admit-local-packages)
- [Repository rules](../../../AGENTS.md)

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Initial focused Gradle invocation exceeded shell timeout after test reports were written; rerun completed cleanly.
- CodeGraph confirmed deleted package-runtime authority paths had no production callers.

### Completion Notes List

- Direct `packages/<package-name>/package.yaml` discovery uses structured SnakeYAML Engine admission with deterministic diagnostics.
- Selected `athena.yaml` `local-package` dependencies enter canonical `ResolvedPackageGraph`; unused catalog entries remain unused.
- Deleted retired representation-root, registry/resolver, cache, and snapshot-stager authorities. No compatibility adapters remain.
- Three-layer coordinate authority preserved: semantic placement, logical Sheet coordinate, compiler-derived physical geometry; Snap remains independent.
- Verification passed sequentially: `:kernel:package-runtime:test`, focused compiler repository tests, `:ide:lsp:test`.
- Typed `ADD_PACKAGE_DEPENDENCY` operation validates admitted direct catalog candidate, stages `athena.yaml`, resolves canonical graph, publishes transaction, then materializes lock.
- Verification passed after operation addition: `:kernel:interaction-model:test`, focused `EditOperationWireMapperTest`/`SourceTransactionEngineTest`, full `:ide:lsp:test`.

### File List

- `gradle/libs.versions.toml`
- `kernel/compiler/build.gradle.kts`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/LocalPackageManifestParser.kt`
- Updated repository loader, graph resolver, validation model, LSP feature, and focused repository tests.
- Deleted obsolete package-runtime registry/resolver/cache/snapshot production and test files.

### Change Log

- 2026-08-08: Replaced ad hoc package manifest parsing with structured native YAML admission and canonical graph selection.
- 2026-08-08: Removed retired `representationPackageRoots` and second package-runtime resolver authorities.
