---
baseline_commit: 9ca7c80239d38d9e2a4880886e71e27e2923c41b
---

# Story 3.1: Admit Safe Package-Local SVG

Status: done

## Story

As a package author,
I want copied package-local SVG resources admitted through one safe profile,
so real symbols render without active content, path escape, or external dependencies.

## Acceptance Criteria

1. Only lock-admitted SVG under direct package `resources/` is read; `reference/`, external URLs,
   symlinks, traversal, absolute paths, and unsupported resource roots fail closed.
2. `svg-safe-1` rejects scripts, event attributes, DTD/entities, `foreignObject`, CSS/style, external
   or data URLs, foreign namespaces, malformed UTF-8/XML, invalid viewBox, and resource-limit excess.
3. Admission enforces deterministic byte/element/depth/path limits from `PackageAdmissionLimits`,
   canonicalizes allowed SVG, and emits stable SHA-256 asset identity plus package-relative provenance.
4. Invalid SVG never enters package snapshot, lock, binding, preview, Canonical Scene, or runtime.
5. Valid copied SVG retains geometry only; it cannot author EngineeringPort, Function, Relationship, Part,
   or source placement meaning.

## Tasks / Subtasks

- [x] Task 1: Add native safe SVG admission limit/validation coverage.
- [x] Task 2: Enforce package-root/resource containment and lock digest before asset admission.
- [x] Task 3: Add rejection and deterministic canonicalization tests; run sequential audits.

## Dev Notes

- Extend `PresentationAssetCompiler` and `PresentationAssetPackageCompiler`; do not create a second
  SVG parser or package registry.
- Reuse `PackageAdmissionLimits.STANDARD` and existing `svg-safe-1` contract/profile.
- SVG is geometry/provenance input only. `.elmt`, HTML, XML, and reference trees remain reference-only.
- Existing lock/resource path and symlink checks are authoritative; strengthen, do not duplicate.
- Run `:kernel:presentation-model:test`, `:kernel:compiler:test`, then relevant package/LSP tests
  sequentially. Run encoding and source-set hygiene audits.

### References

- `_bmad-output/planning-artifacts/m45/epics.md` Epic 3 / Story 3.1.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-08-m45/prd.md` Sections 2, 4, 7, 9.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-08-m45/ARCHITECTURE-SPINE.md` AD-4, AD-9, AD-16.
- `AGENTS.md` pre-1.0 and visual/source-set rules.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Existing `PresentationAssetCompiler` owned `svg-safe-1` canonicalization; added explicit DOM depth and
  element budget enforcement while preserving module boundaries.
- Sequential presentation-model and compiler tests passed. Encoding and source-set hygiene audits passed.

### Completion Notes List

- Added bounded inert SVG traversal with deterministic rejection for excessive depth/element count.
- Existing lock digest, package-root containment, symlink, UTF-8, XML, URL, and canonicalization paths remain authoritative.

### File List

- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationAssetCompiler.kt`
- `kernel/presentation-model/src/test/kotlin/com/engineeringood/athena/presentation/PresentationAssetCompilerTest.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/PresentationAssetPackageCompiler.kt`
- `_bmad-output/implementation-artifacts/m45/3-1-admit-safe-package-local-svg.md`

### Change Log

- 2026-08-09: Created Story 3.1 context; status `ready-for-dev`.
- 2026-08-09: Added SVG depth/element safety bounds and regression proof; status `review`.
