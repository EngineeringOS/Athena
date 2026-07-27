---
baseline_commit: 0311ad67c08779db3d1def271d0ac948a4683128
---

# Story 2.2: Stage External Assets Into An Immutable Package Snapshot

Status: review

## Story

As an Athena package consumer,
I want external geometry acquired into an immutable repository-confined snapshot,
so that packages cannot escape roots, collide with other packages, or change during compilation.

## Acceptance Criteria

1. **Given** repository-declared package roots and valid regular package-local `.athena` and SVG files,
   **when** package staging runs, **then** no-follow acquisition, path component checks, pre/post file
   identity checks, hashing, and private snapshot copy succeed before any representation parse reads
   source bytes.
2. **Given** `package athena.vendor` and a source file under
   `packages/representation/athena/vendor/`, **when** `graphic svg "./vendor-drive.svg"` is
   resolved, **then** the SVG is resolved beside the owning `.athena` source inside the same package
   filesystem hierarchy; `../` path tricks, absolute paths, traversal, duplicate global resource
   pools, and renderer filesystem reads are rejected.
3. **Given** archive traversal, symlinks, junctions/reparse points, changed file identity, duplicate
   `(package, identity, version)` or `(package, definitionId)` values, unsupported secure-open
   guarantees, oversized package bytes/files/work units, or stale cache identity, **when** staging
   runs, **then** the package is rejected before parse or cache publication with stable
   source-spanned diagnostics and zero partial package admission.
4. **Given** the same source bytes, compiler/schema version, dependency-lock digest, and package
   hierarchy, **when** compilation repeats offline, **then** snapshot identity, cache key, and compiled
   output are reproducible; deleting generated caches and rerunning produces the same proof payload.
5. **Given** the active M34 Cabinet proof consumes a staged package snapshot, **when** it binds the
   Story 2.1 SVG-backed Element, **then** structured evidence identifies snapshot id, staged source
   provenance, dependency-lock digest, definition provenance, and confirms zero XML manifest, raw SVG
   transport, or renderer file access authority.
6. **Given** all previous criteria are green, **when** mandatory polish/purge runs, **then** source,
   tests, fixtures, docs, generated outputs, caches, and workspace state are deeply reviewed;
   stale/duplicate artifacts are removed; and RED/GREEN, AC-to-evidence, and three-layer review are
   recorded.

**Implements:** FR-11, FR-26..FR-27, FR-41; NFR-10..NFR-11.

## Tasks / Subtasks

- [x] Add Story 2.2 RED contracts before production edits (AC: 1..5)
  - [x] Add tests proving package roots from `athena.yaml` stage `.athena` plus package-local SVG
        resources into one immutable snapshot before representation parsing.
  - [x] Add failing tests for `package athena.vendor` mapping to
        `packages/representation/athena/vendor/` and for `graphic svg "./vendor-drive.svg"` resolving
        beside the owning source file.
  - [x] Add failing rejection tests for absolute paths, traversal, `../` escape attempts, symlinks,
        junctions/reparse points where detectable, changed file identity, duplicate identities,
        oversized aggregate budgets, and unsupported secure-open guarantees.
  - [x] Add a failing Cabinet proof assertion that no renderer or runtime path rereads source SVG or
        XML and that proof evidence carries snapshot/cache provenance.
- [x] Implement immutable package snapshot acquisition (AC: 1, 3)
  - [x] Discover representation package roots only from the repository contract and dependency lock;
        do not scan arbitrary workspace folders.
  - [x] Traverse roots with no-follow semantics; reject non-regular files, absolute paths, traversal,
        Windows reparse points/junctions/symlinks, and unsupported secure-open guarantees.
  - [x] Record pre/post stable file identity, size, mtime, normalized relative path, package root,
        SHA-256 content hash, compiler/schema version, and dependency-lock digest.
  - [x] Copy verified bytes into a private immutable snapshot directory and make all later compile/hash
        reads use staged files only.
- [x] Enforce Java-style package hierarchy and package-local resources (AC: 2)
  - [x] Map Athena package names to filesystem hierarchy, for example `athena.vendor` ->
        `athena/vendor/`, under declared package roots.
  - [x] Require referenced graphics to be package-local siblings or descendants of the owning
        `.athena` source directory; reject `../` and global shared resource pools.
  - [x] Remove any test or production pattern like
        `VALID_SOURCE.replaceFirst("./vendor-drive.svg", "../vendor-drive.svg")`.
  - [x] Keep simple native symbols pure `.athena`; keep complex shapes as `.athena` metadata plus one
        package-local governed SVG body.
- [x] Integrate snapshot reads with the existing representation compiler (AC: 1, 4)
  - [x] Route `AthenaRepresentationSourceCompiler` and `AthenaSvgGraphicBodyCompiler` through staged
        snapshot source handles without adding XML manifests, SVG IR, renderer authority, or runtime
        package scanning.
  - [x] Fail closed when any staged source diagnostic exists; publish no partial definitions.
  - [x] Ensure duplicate `(library, identity, version)` and duplicate definition ids are detected
        across the snapshot, independent of file order.
  - [x] Make cache identity deterministic from staged source bytes, compiler/schema version, and
        dependency-lock digest.
- [x] Extend Epic 2 Cabinet-visible proof (AC: 5)
  - [x] Update the M34 sample package to use package-hierarchy-local `.athena` and SVG resources.
  - [x] Prove the Story 2.1 SVG-backed Element binds and renders from compiled snapshot data only.
  - [x] Assert structured proof contains snapshot id, cache key inputs, staged paths, Athena/SVG
        provenance, and no XML/raw-markup/renderer-file-access authority.
- [x] Run sequential verification (AC: 1..5)
  - [x] Run focused package-runtime/compiler snapshot tests.
  - [x] Run focused SVG graphic-body and M34 Cabinet proof tests.
  - [x] Run affected module tests and full Gradle `test` only after focused suites pass; never overlap
        Gradle runs.
  - [x] Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after text/doc edits.
- [x] Perform mandatory deep polish/purge and evidence review (AC: 6)
  - [x] Audit public vocabulary, filesystem/package hierarchy, source/compiler/runtime/renderer
        boundaries, generated caches, fixtures, docs, encoding, diff, and dirty-worktree boundaries.
  - [x] Remove stale XML manifest/runtime compatibility, duplicate global resource locations,
        temporary `../` fixture hacks, renderer file reads, sidecar metadata, and generated cache
        artifacts not meant for source control.
  - [x] Record RED/GREEN, AC-to-evidence, independent blind/edge/acceptance reviews, review
        dispositions, and every touched file before changing the story to review.

## Dev Notes

### Frozen Story Rule

Package resources are organized like Java package resources. The package declaration maps to a real
directory hierarchy under a repository-declared package root:

```text
packages/representation/
  athena/
    vendor/
      vendor-drive.athena
      vendor-drive.svg
```

```athena
package athena.vendor

element vendor_drive {
  identity "athena.vendor.drive"
  version "1.0.0"

  graphic svg "./vendor-drive.svg"
}
```

`./vendor-drive.svg` resolves relative to the staged copy of `vendor-drive.athena`. It must not resolve
from the project root, renderer root, process working directory, a global resources directory, or any
parallel package directory. `../` is rejected for M34 even if it would stay under the repository root,
because it weakens package locality and makes package artifacts hard to move, publish, and review.

### Non-Negotiable Boundaries

- Athena source remains the only metadata authority. The snapshot is derived bytes and provenance, not
  a second model.
- SVG remains untrusted compile-time geometry input. It is never runtime metadata, renderer input, or
  project semantic truth.
- XML package manifests from M32/M33 must not be extended or used in active M34 paths. No compatibility
  adapter is required for product runtime because the product is not public.
- Renderer, Electron, and LSP consume typed compiled payloads only. They must not scan package folders,
  open SVG files, parse XML/HTML, or infer meaning from paths, DOM ids, CSS, or geometry.
- Do not introduce a remote registry, network fetch, archive installer, package marketplace, QET
  importer, or dependency resolver beyond declared local roots and the existing lock file.
- Do not add a new package IR if existing package-model, package-runtime, RepresentationDefinition,
  GraphicPrimitiveDocument, and proof payload contracts can be extended.

### Existing Code To Extend

- `examples/m34/sample-project/athena.yaml` and `athena.lock` are the repository contract inputs used
  by the sample package.
- `examples/m34/sample-project/packages/representation/athena/vendor/epic2-svg-elements.athena` and
  `vendor-drive.svg` are the current package-local SVG proof fixtures from Story 2.1.
- `AthenaRepresentationSourceCompiler` is the mixed representation compiler; integrate staged source
  handles here instead of adding a second compiler authority.
- `AthenaSvgGraphicBodyCompiler` currently resolves SVG beside the source path. Story 2.2 should move
  that read behind snapshot acquisition and keep the package-local rule.
- `GraphicPrimitiveDocument.provenanceSources` carries source provenance. Extend evidence if needed,
  but do not transport raw source text or SVG markup.
- `kernel/package-model` and `kernel/package-runtime` already contain M32/M33 package contracts and
  legacy M33 package runtime files. Reuse only the pieces that support immutable snapshot contracts;
  delete or quarantine XML runtime authority in later M34 stories where scoped.

### Testing Requirements

- Capture genuine RED before production edits.
- Tests must assert exact diagnostic code, file, span, subject, message, and zero output for invalid
  acquisition and duplicate identity cases.
- Use temporary directories for symlink/junction/path-race tests. On Windows, if a junction/symlink
  cannot be created without privileges, assert the detection helper behavior separately and record the
  environment limitation honestly.
- Prove reproducibility by compiling the same staged package twice after deleting generated caches and
  comparing snapshot/cache identity plus compiled output.
- Prove offline behavior: no network, no renderer filesystem reads, and no XML/raw SVG transport in the
  active Cabinet proof.
- Run Gradle verification sequentially only.

### Previous Story Intelligence

- Story 2.1 implemented `graphic svg "./asset.svg"` and currently resolves resources inside the
  owning source directory. Keep that user-approved package-local shape; do not replace it with root
  relative paths, `../` test hacks, or a shared resource bucket.
- Story 2.1 lowered allowed SVG `rect`, `line`, `circle`, and `text` into `GraphicPrimitiveDocument`
  and rejected unsupported geometry and unsafe SVG features fail-closed.
- Story 2.1 added `GraphicPrimitiveDocument.provenanceSources`; Story 2.2 should enrich package
  snapshot provenance without creating raw-markup transport.
- Story 1.3 and 2.1 both used explicit test-owned policies for Cabinet proofs. Production
  Profile/Binding selection remains Story 3.1.
- Current workspace is intentionally dirty with M33/M34 work. Preserve unrelated changes; stage and
  document only files touched for this story.

### References

- [M34 PRD](../../planning-artifacts/prds/prd-Athena-2026-07-24-m34/prd.md)
- [M34 Addendum](../../planning-artifacts/prds/prd-Athena-2026-07-24-m34/addendum.md)
- [M34 Architecture Spine](../../planning-artifacts/architecture/architecture-Athena-2026-07-24-m34/ARCHITECTURE-SPINE.md)
- [M34 Epics](epics.md)
- [Story 2.1](2-1-compile-a-referenced-governed-svg-graphic-body.md)
- `kernel/package-model/src/main/kotlin/com/engineeringood/athena/packageplatform/RepresentationPackageModels.kt`
- `kernel/package-model/src/main/kotlin/com/engineeringood/athena/packageplatform/RepresentationPackageValidation.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/BindingResolver.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationSourceCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSvgGraphicBodyCompiler.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/GraphicPrimitiveModels.kt`
- `examples/m34/sample-project/athena.yaml`
- `examples/m34/sample-project/athena.lock`

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: `:kernel:package-runtime:test --tests "com.engineeringood.athena.packageruntime.RepresentationPackageSnapshotStagerTest"` failed before implementation on missing `RepresentationPackageSnapshotStager` / `RepresentationPackageSnapshotRequest`; the tightened hierarchy/locality RED failed on missing `package.snapshot.package-path.mismatch` and `package.snapshot.graphic-svg.path.invalid`; repository-contract RED failed on missing `stageRepository`.
- GREEN: `:kernel:package-runtime:test --tests "com.engineeringood.athena.packageruntime.RepresentationPackageSnapshotStagerTest"` passed; `:kernel:compiler:test --tests "com.engineeringood.athena.compiler.AthenaRepresentationPackageSnapshotCompilerTest" --tests "com.engineeringood.athena.compiler.AthenaM34ElementCabinetProofTest"` passed; `:kernel:package-runtime:test` passed; `:kernel:compiler:test` passed; full `.\gradlew.bat --no-daemon --console=plain test` passed after the final snapshot-root guard; `tools/encoding-audit.ps1` passed.

### Completion Notes List

- Ultimate context engine analysis completed from M34 PRD, architecture spine, epics, Story 1.3,
  Story 2.1, sprint status, recent git history, and current package-local resource design correction.
- Added immutable representation package snapshot contracts in package-runtime with repository-root confinement, no-follow traversal, package-root budget checks, stable file identity capture, SHA-256 content hashes, dependency-lock digest, and deterministic snapshot id.
- Added repository-contract staging through `athena.yaml` `representationPackageRoots` plus `athena.lock` digest; updated the M34 sample to declare `packages/representation`.
- Enforced Java-style package hierarchy: `package athena.vendor` must live under `athena/vendor/`, and `graphic svg "./asset.svg"` must stay beside or below the owning `.athena` source directory. `../` remains only as an intentional invalid test case.
- Added `AthenaRepresentationPackageSnapshotCompiler` so staged `.athena` files feed the existing representation compiler and SVG graphic-body compiler using staged paths only.
- Extended the M34 referenced SVG Cabinet proof to copy the sample into a temp repository, stage it, compile from the snapshot, bind/render the SVG-backed Element, and assert snapshot id, lock digest, staged provenance, no XML runtime authority, no raw SVG transport, and no renderer file access authority.
- AC evidence: AC1 `RepresentationPackageSnapshotStagerTest` valid/repository-contract tests; AC2 package-path and graphic-svg locality tests plus M34 sample hierarchy; AC3 root escape, symlink, package mismatch, invalid SVG locality, duplicate identity tests; AC4 repeat snapshot id and snapshot compiler proof; AC5 `AthenaM34ElementCabinetProofTest`; AC6 final rg audits, no generated sample snapshots, full Gradle test, and encoding audit.
- Three-layer review: blind boundary review found direct unstaged proof compilation and was fixed; edge review found snapshot directories could be placed under package roots and was fixed; acceptance review found the sample contract lacked `representationPackageRoots` and was fixed.

### File List

- `_bmad-output/implementation-artifacts/m34/2-2-stage-external-assets-into-an-immutable-package-snapshot.md`
- `_bmad-output/implementation-artifacts/m34/sprint-status.yaml`
- `examples/m34/sample-project/athena.yaml`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/RepresentationPackageSnapshotModels.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/RepresentationPackageSnapshotStager.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/RepresentationPackageSnapshotStagerTest.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationPackageSnapshotCompiler.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationPackageSnapshotCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM34ElementCabinetProofTest.kt`

### Change Log

- 2026-07-24: Created Story 2.2 with package-hierarchy-local immutable snapshot guidance.
- 2026-07-24: Implemented immutable package snapshots, repository-declared package roots, staged snapshot compilation, and Cabinet proof evidence; marked ready for review.
