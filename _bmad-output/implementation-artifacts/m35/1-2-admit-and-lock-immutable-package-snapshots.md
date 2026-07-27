---
baseline_commit: 0311ad67c08779db3d1def271d0ac948a4683128
---

# Story 1.2: Admit And Lock Immutable Package Snapshots

Status: review

## Story

As an engineering project owner,
I want package snapshots and lock evidence to be reproducible and fail closed,
so that the same project compiles from the same admitted material across machines and time.

## Acceptance Criteria

1. Given repository-declared package roots and dependencies, when two-phase package admission runs, then capture is no-follow, root-confined, immutable, AST-driven, race-checked, and governed by one `PackageAdmissionLimitsV1`; absolute paths, traversal, links/reparse points, overlapping roots, duplicate physical files, and budget excess fail closed.
2. Given admitted package content, when snapshot identity is computed, then canonical manifest intent, resolved coordinate, sorted source/resource hashes, and compiler/schema version determine the snapshot digest; generated lock bytes and validated lock state do not participate in logical resource or snapshot identity.
3. Given validate mode, when `athena.lock` is missing, stale, or schema-incompatible, then compilation fails with the specified stable lock diagnostic and performs no write.
4. Given explicit update mode, when `RepositoryLockV2` is materialized, then canonical bytes are written through a same-directory temporary file and atomic replacement, temporary output is cleaned on failure, and successful output is revalidated; lock v1, direct non-atomic writing, and the old lock-dependent snapshot algorithm are deleted without compatibility readers.
5. Given all previous acceptance criteria are green, when the mandatory story gate runs, then RED/GREEN commands, AC-to-evidence mapping, three-layer review, and touched/adjacent stale scanner/fixture/generated/XML/compatibility purge are recorded.

## Tasks / Subtasks

- [x] Add RED tests for immutable admission and lock v2 behavior (AC: 1, 2, 3, 4)
  - [x] Missing lock in validate mode fails with `repository.lock.missing` and does not write.
  - [x] Stale lock fails with `repository.lock.stale`.
  - [x] Schema-incompatible lock fails with `repository.lock.schema-incompatible`.
  - [x] Explicit update writes canonical `RepositoryLockV2` atomically and validates the bytes it wrote.
  - [x] Snapshot digest changes when admitted source bytes or manifest intent changes, but not when generated lock bytes are rewritten.
  - [x] Admission rejects absolute/traversal/link/reparse/overlap/duplicate-file/budget failures with stable diagnostics.
- [x] Replace lock v1 active contracts with `RepositoryLockV2` (AC: 2, 3, 4)
  - [x] Put authored intent in `RepositoryManifest`; put resolved coordinates, snapshot digests, resource hashes, dependency ids, compiler/schema identity, and validated state digest in lock evidence.
  - [x] Delete lock v1 reader/writer and old lock-dependent snapshot identity instead of adding compatibility readers.
  - [x] Keep lock bytes out of `AdmittedPackageSnapshotDigest` and `PackageResourceKey`.
- [x] Add two-phase immutable package admission scaffolding without resource syntax (AC: 1, 2)
  - [x] Capture governed `.athena` source units from resolved repository graph roots through no-follow, root-confined reads.
  - [x] Reuse ANTLR4/AST package facts from Story 1.1; do not add regex source scanners.
  - [x] Add one non-overridable `PackageAdmissionLimitsV1` value used by admission, diagnostics, proof, and tests.
  - [x] Keep declared resources empty until Story 1.3; do not invent resource declaration syntax here.
- [x] Wire validate/update modes through compiler/repository APIs (AC: 3, 4)
  - [x] Validate mode is read-only and fail-closed.
  - [x] Update/materialize mode writes through a same-directory temporary file and atomic replacement, cleans temp files on failure, then revalidates.
  - [x] `AthenaRepositoryGraphResolver` remains lock-independent; lock validation is a separate acceptance gate.
- [x] Migrate fixtures and stale tests to lock v2 (AC: 3, 4, 5)
  - [x] Update governed example `athena.lock` files that are in this story's verification path.
  - [x] Remove stale v1 expectations from compiler/LSP/runtime tests.
  - [x] Do not restore flat/default package compatibility.
- [x] Polish/purge and evidence gate (AC: 5)
  - [x] Audit touched/adjacent lock, package graph, fixture, generated output, XML/package compatibility, and scanner paths.
  - [x] Run sequential verification and record RED/GREEN evidence.
  - [x] Record AC-to-evidence mapping and three-layer review.

## Dev Notes

### Scope Boundary

Story 1.2 owns immutable package admission scaffolding and lock v2 evidence only. It must not implement typed `resource` syntax, SVG geometry hints, package-local resource resolution, physical installation source, Cabinet composition, routing, trace, or IDE visual work. Story 1.3 owns typed resources; Story 1.4 owns SVG-backed vendor geometry.

### Architecture Requirements

- `RepositoryManifest` remains the only authored descriptor contract. Do not add `package.athena`, XML manifests, package JSON equivalents, or runtime semantic interpretation of descriptor metadata.
- `AthenaRepositoryGraphResolver` resolves local package graph without touching lock state. Keep graph resolution lock-independent.
- Validate mode must be read-only. Missing/stale/schema-incompatible lock blocks acceptance and writes nothing.
- Explicit update mode is the only writer. It must write canonical bytes to a temp file in the same directory, atomically replace `athena.lock`, clean temporary output on failure, then revalidate.
- Because Athena is unreleased, delete v1 compatibility readers/writers and stale fixture assumptions instead of wrapping them.
- Snapshot identity is content-bound and lock-independent: normalized descriptor intent, resolved coordinate, sorted admitted source/resource hashes, and compiler/schema version. Generated lock bytes and `ValidatedLockStateDigest` are not source identity.
- Admission is two-phase and immutable: capture governed sources first, then parse staged bytes. It is no-follow/root-confined, rejects traversal and link/reparse escapes, and is governed by one non-overridable `PackageAdmissionLimitsV1`.
- ANTLR4 remains parser authority. Do not add regex scanners for package or source admission.
- Namespace remains transport identity only; no manufacturer/article/revision/rating/lifecycle facts.

### Previous Story Intelligence

- Story 1.1 established strict Java-style source hierarchy and stable package/path diagnostics.
- Existing governed examples were migrated to package-relative directories; do not weaken validation for old flat fixtures.
- Full compiler and LSP regressions exposed stale flat M17/M18 fixtures; expect more stale lock v1 fixtures and purge them.
- Active `athena-industrial-control-v0` fallback was removed from `PresentationModelDeriver`; do not reintroduce fallback authority while migrating tests.

### Likely Code Areas

- `kernel/repository-model/src/main/kotlin/com/engineeringood/athena/repository/RepositoryContracts.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryContractLoader.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryGraphResolver.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryLockMaterializerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryContractLoaderTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryGraphResolverTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/*Repository*Test.kt`
- Governed example `athena.lock` files under `examples/m*/...` only when tests require them.

Use CodeGraph before reading or editing source files because the repo is indexed.

### Testing Requirements

- Write RED tests before implementation.
- Run Gradle sequentially on Windows. Never run Gradle tasks in parallel.
- Minimum expected verification:
  - targeted compiler repository lock/admission tests;
  - targeted LSP repository diagnostics tests if LSP lock diagnostics change;
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`;
  - `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test`;
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`;
  - `git diff --check`.

### References

- `_bmad-output/implementation-artifacts/m35/epics.md` - Epic 1, Story 1.2.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-27-m35/prd.md` - FR-6, FR-8, FR-10..FR-11, FR-41..FR-42.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-27-m35/addendum.md` - Package Hierarchy Rule, Package-Local Resource Resolution.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-27-m35/ARCHITECTURE-SPINE.md` - AD-7, AD-8, AD-9, AD-16, AD-17.
- `_bmad-output/implementation-artifacts/m35/1-1-enforce-governed-package-hierarchy.md` - previous story evidence and purge notes.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References
- Sequential targeted lock/admission tests passed.
- `:kernel:compiler:test` passed after migrating active proof locks to `RepositoryLockV2`.
- `:ide:lsp:test` passed after aligning nested governed repository handling and replacing v1 lock fixtures.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.
- `git diff --check` passed.

### Completion Notes List
- Implemented immutable package admission and lock v2 canonicalization with fail-closed validate/update flows.
- Deleted lock v1 active contract behavior and migrated active proof/sample lock fixtures to v2.
- Tightened repository admission to reject escaping `local-path` locators and enforce package/source budgets.
- Aligned LSP repository resolution with governed nested package roots and updated tests/fixtures to current contract behavior.

### File List
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryLockMaterializer.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryContractLoader.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryReportPublisher.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryLockMaterializerTest.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryGraphResolverTest.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryReportPublisherTest.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepositoryResolver.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaLspTestFixtures.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagnosticsPublishingTest.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepositoryGraphSessionRequestTest.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaReuseRequestTest.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticHistoryStateRequestTest.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticScmStateRequestTest.kt
- examples/m10/reasoning-proof/baseline/athena.lock
- examples/m10/reasoning-proof/current/athena.lock
- examples/m17/repository-parity-proof/athena.lock
- examples/m18/repository-proof/valid-workspace/athena.lock
- examples/m18/repository-proof/valid-workspace/vendor/controls/athena.lock
- examples/m34/sample-project/athena.lock

### Change Log
- 2026-07-27: Implemented lock v2 canonical materialization, fail-closed admission checks, and migrated active proof/sample locks to the new schema.
