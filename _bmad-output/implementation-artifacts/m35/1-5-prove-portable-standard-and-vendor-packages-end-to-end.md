---
baseline_commit: c007cd140cd0fcaffdc5ccb66bf2f917d69294cf
---

# Story 1.5: Prove Portable Standard And Vendor Packages End To End

Status: review

## Story

As an engineering library consumer,
I want one standard package and one vendor/user package to compile reproducibly,
so that I can trust package-backed visual material before physical Cabinet authoring depends on it.

## Acceptance Criteria

1. Given package-hierarchical standard and vendor/user fixtures, when the repository is compiled from a clean workspace, then both packages are discovered only through `athena.yaml`, their exported definitions resolve, their internal resources are admitted, and zero path rewriting is used.
2. Given two clean compilations of identical source, package material, and validated lock state, when structured evidence is compared, then resolved coordinates, resource hashes, snapshot digests, definition ids, and ordered proof are byte-stable.
3. Given a changed source/resource/lock fact or malicious path/SVG fixture, when compilation runs, then the relevant identity changes or admission fails with the expected stable diagnostic, and no renderer/runtime filesystem fallback occurs.
4. Given all previous acceptance criteria are green, when the mandatory story gate runs, then RED/GREEN commands, AC-to-evidence mapping, and three-layer review are recorded, and touched/adjacent duplicate fixtures, old flat paths, generated evidence, XML logic, and compatibility names are purged.

## Tasks / Subtasks

- [x] Add RED tests for portable standard and vendor package E2E behavior (AC: 1, 2, 3)
  - [x] Cover a clean workspace with one standard package and one vendor/user package discovered only through `athena.yaml`.
  - [x] Cover exported Symbol/Element resolution, package-local resource admission, complex SVG geometry lowering, and no path rewriting.
  - [x] Cover byte-stable structured evidence across two clean compilations.
  - [x] Cover changed source/resource/lock facts and malicious path/SVG fixtures with stable diagnostics.
- [x] Implement or extend package proof assembly without adding a second authority (AC: 1, 2, 3)
  - [x] Use the existing `RepositoryManifest`, `RepositoryLockV2`, package snapshot, typed resource, and SVG compiler paths.
  - [x] Keep resource lookup package-local and classpath-like; do not add remote URI support in M35.
  - [x] Ensure dependencies expose compiled definitions, never raw resource keys or physical filesystem paths.
  - [x] Keep renderer/runtime fallback impossible for missing or rejected resources.
- [x] Migrate or add the dedicated standard/vendor package fixtures (AC: 1, 2)
  - [x] Keep source files under package-matching filesystem hierarchy.
  - [x] Include at least one native standard Element and one complex SVG-backed vendor/user Element.
  - [x] Ensure the sample lock validates from the same resolver authority used by compilation.
- [x] Add deterministic proof and negative identity/admission gates (AC: 2, 3)
  - [x] Prove resolved coordinates, resource hashes, snapshot digest, definition ids, and proof ordering are stable.
  - [x] Prove source/resource mutation changes identity or fails validation as expected.
  - [x] Prove malicious SVG/path inputs fail closed before renderer or runtime access.
- [x] Polish/purge and evidence gate (AC: 4)
  - [x] Audit touched and adjacent paths for duplicate fixtures, old flat paths, XML logic, generated evidence, path rewrites, and compatibility names.
  - [x] Run sequential verification and record RED/GREEN evidence.
  - [x] Record AC-to-evidence mapping and three-layer review.

## Dev Notes

### Scope Boundary

Story 1.5 closes Epic 1 by proving that governed package material is portable and reproducible end to end. It does not introduce physical installation syntax, Cabinet composition, routing, editable occurrence trace, remote resource fetching, a second manifest, XML runtime authority, or compatibility shims. M35 default resource lookup remains package-local and classpath-like; future resource URI schema work is recorded in deferred work.

### Architecture Requirements

- `athena.yaml` / `RepositoryManifest` is the only authored repository/package descriptor authority.
- `RepositoryLockV2` is compiler-generated deterministic evidence. It is not source truth and must not feed package resource keys.
- Standard and vendor/user package source must follow Java-style package hierarchy.
- Package namespace remains transport identity only and cannot stand in for manufacturer, article, revision, rating, lifecycle, or product identity.
- Package admission is immutable, root-confined, no-follow, AST-driven, and bounded by `PackageAdmissionLimitsV1`.
- `resource <id> { kind svg path "./asset.svg" }` plus `graphic svg resource <id>` remains the only public SVG resource surface for this story.
- Bare resource ids are lexical to one source unit. Dependencies expose compiled Symbol/Element definitions, not raw resource keys or physical paths.
- Complex SVG bodies are geometry only. SVG may provide `id` and optional matching `data-athena-geometry-ref` hints, but Athena source owns identity, anchors, labels, roles, direction, signal, compatibility, profile, binding, and lifecycle.
- Raw SVG/XML markup, DOM nodes, filesystem handles, and staged snapshot objects must not cross compiler/LSP/Electron transport.
- Because Athena is unreleased, stale XML/package/render compatibility paths should be removed instead of wrapped.

### Previous Story Intelligence

- Story 1.1 enforced package/path parity and default-package rejection.
- Story 1.2 replaced lock v1 with `RepositoryLockV2`, fail-closed validation, and explicit atomic materialization.
- Story 1.3 established source-local typed resources, source-unit-scoped `PackageResourceKey`, same-unit resource references, and package-local path confinement.
- Story 1.4 established complex SVG geometry compilation, `use` expansion, geometry-only SVG authority, native-vs-SVG contract equivalence, and LSP transport without raw markup.
- Story 1.4 deliberately deferred remote URI/resource schema work; do not implement it here.

### Likely Code Areas

- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryContractLoader.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryLockMaterializer.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationPackageSnapshotCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSvgGraphicBodyCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSvgGraphicBodySupport.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/RepresentationPackageSnapshotStager.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/RepresentationPackageSnapshotCapture.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/RepresentationPackageSnapshotStagerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryLockMaterializerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationPackageSnapshotCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaReferencedSvgGraphicCompilerTest.kt`
- `examples/m35/...` for the dedicated package proof fixture.

### Testing Requirements

- Follow RED/GREEN. Write failing tests before production code.
- Run Gradle sequentially on Windows. Never run Gradle tasks in parallel.
- Minimum expected verification for this story:
  - targeted repository/package lock tests;
  - targeted representation package snapshot/compiler tests;
  - targeted SVG-backed vendor proof tests;
  - `encoding-audit.ps1` after text edits;
  - `git diff --check`.

### References

- `_bmad-output/implementation-artifacts/m35/epics.md` - Epic 1, Story 1.5.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-27-m35/prd.md` - FR-3, FR-7, FR-10, FR-13..FR-17, FR-41..FR-42.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-27-m35/addendum.md` - package-local resource and SVG authority boundaries.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-27-m35/ARCHITECTURE-SPINE.md` - AD-7..AD-10, AD-15, AD-17.
- `_bmad-output/implementation-artifacts/m35/1-4-compile-complex-svg-geometry-without-metadata-authority.md` - immediate predecessor and SVG authority proof.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- RED: `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaRepresentationPackageSnapshotCompilerTest` failed on missing `examples/m35/package-platform-proof`.
- GREEN: the same targeted compiler suite passed after adding the M35 package proof fixture, checked-in validated lock evidence, and stable proof assertions.
- Regression: `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test --tests com.engineeringood.athena.packageruntime.RepresentationPackageSnapshotStagerTest` passed after purging stale SVG metadata from the adjacent stager fixture.
- Regression: `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.repository.AthenaRepositoryLockMaterializerTest --tests com.engineeringood.athena.compiler.AthenaReferencedSvgGraphicCompilerTest` passed.

### Completion Notes List

- Added `examples/m35/package-platform-proof` with manifest-only package discovery, validated lock evidence, package-hierarchical project source, native standard IEC material, and a complex SVG-backed ABB vendor element.
- Proved package snapshot and compiler evidence are stable across clean compilations by comparing snapshot id, dependency lock digest, compiler schema, source/resource hashes, generated resource ids, and body authorities.
- Proved resource edits change package snapshot identity and malicious resource path rewrites fail closed with stable diagnostics before descriptor/renderer fallback.
- Removed a stale `data-athena-schema` SVG metadata marker from adjacent snapshot staging test data.

### File List

- `_bmad-output/implementation-artifacts/m35/1-5-prove-portable-standard-and-vendor-packages-end-to-end.md`
- `_bmad-output/implementation-artifacts/m35/sprint-status.yaml`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationPackageSnapshotCompilerTest.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/RepresentationPackageSnapshotStagerTest.kt`
- `examples/m35/package-platform-proof/athena.yaml`
- `examples/m35/package-platform-proof/athena.lock`
- `examples/m35/package-platform-proof/src/com/engineeringood/m35/packageproof/package-proof.athena`
- `examples/m35/package-platform-proof/packages/representation/com/engineeringood/m35/standard/iec/standard-elements.athena`
- `examples/m35/package-platform-proof/packages/representation/com/engineeringood/m35/vendor/abb/pfea112/vendor-elements.athena`
- `examples/m35/package-platform-proof/packages/representation/com/engineeringood/m35/vendor/abb/pfea112/pfea112.svg`

## Change Log

- 2026-07-27: Completed the portable standard/vendor package E2E proof for Epic 1 with deterministic package snapshot, lock, SVG, and authority tests.
