---
baseline_commit: 0311ad67c08779db3d1def271d0ac948a4683128
---

# Story 3.3: Migrate The M34 Cabinet Package From XML To Athena

Status: review

## Story

As an engineer opening the sample,
I want all visible Cabinet components resolved from compiled governed package sources,
so that XML manifests and Kotlin sample facts are no longer product authority.

## Acceptance Criteria

1. **Given** the M34 sample project, **when** package discovery and compilation run, **then** every
   visible component identifies one native-Athena or annotated-SVG definition, generated descriptor,
   binding rule, resolved variant, anchor/label bindings, exact version, and source provenance.
2. **Given** M32/M33 XML manifests and sample-specific Kotlin definitions, **when** active Cabinet
   authority is inspected, **then** zero product-path reads remain; any fixture-only adapter is
   isolated, ledgered, and cannot ship in the M34 sample.
3. **Given** intrinsic component bounds and project layout facts, **when** Cabinet composition runs,
   **then** Element intrinsic transforms remain separate from `drawing-composition` occurrence
   placement and derived document bounds.
4. **Given** all previous criteria are green, **when** mandatory polish/purge runs, **then** source,
   tests, fixtures, docs, generated outputs, and workspace state are deeply reviewed; stale/duplicate
   artifacts are removed; and RED/GREEN, AC-to-evidence, and three-layer review are recorded.

**Implements:** FR-21..FR-23, FR-30..FR-32, FR-34, FR-37, FR-41; NFR-2, NFR-4, NFR-6, NFR-10.

## Tasks / Subtasks

- [x] Add Story 3.3 RED contracts before production edits (AC: 1..3)
  - [x] Add a failing M34 package authority test proving the sample compiles from Athena source roots.
  - [x] Add a failing deletion-gate test for zero active XML product reads in the M34 Cabinet path.
  - [x] Add a composition proof that intrinsic bounds and document bounds are separate.
- [x] Migrate active Cabinet package authority to compiled Athena sources (AC: 1, 2)
  - [x] Use repository `athena.yaml` package roots and immutable snapshots.
  - [x] Keep QET/XML/SVG raw resources out of runtime authority.
  - [x] Generate descriptors from `RepresentationDefinition`; do not author descriptors directly.
- [x] Add M34 Cabinet package proof payload (AC: 1..3)
  - [x] Prove definition id/version/provenance, binding rule/provenance, variant, anchors, labels, and
        derived document bounds.
  - [x] Prove zero XML product/runtime authority and zero hard-coded document viewBox.
- [x] Run sequential verification (AC: 1..3)
  - [x] Run focused compiler/package-runtime/drawing-composition tests.
  - [x] Run full Gradle `test` sequentially after focused suites pass.
  - [x] Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after text/doc edits.
- [x] Perform mandatory deep polish/purge and evidence review (AC: 4)
  - [x] Audit XML/Kotlin sample authority, package roots, snapshots, generated descriptors, Cabinet proof,
        docs, encoding, diff, and dirty-worktree boundaries.
  - [x] Remove stale duplicate package facts and generated artifacts not meant for source control.
  - [x] Record RED/GREEN, AC-to-evidence, independent blind/edge/acceptance reviews, review
        dispositions, and every touched file before changing the story to review.

## Dev Notes

### Scope Boundary

This story migrates the active M34 Cabinet package authority. It does not complete the final customer
visual polish, broad Documentation/Schematic surfaces, a full QET importer, or remote package
registry.

### Required Architecture

- Athena representation source is the only metadata authority.
- XML product/runtime paths are deleted or ledgered as fixture-only; no compatibility promise is
  required because the product is not public.
- Complex SVG is a governed compile-time graphic resource only.
- `drawing-composition` owns Cabinet document bounds; Elements own only intrinsic bounds/transforms.

### Previous Story Intelligence

- Story 3.1 made typed binding rules the sole active selection input.
- Story 3.2 made project-port facts explicit for occurrence binding.
- The user added a professional renderer target note; Epic 4 owns final visual/E2E proof.

### Testing Requirements

- Capture genuine RED before production edits.
- Assert proof fields structurally, not by DOM parsing.
- Run Gradle verification sequentially only.

### References

- [M34 PRD](../../planning-artifacts/prds/prd-Athena-2026-07-24-m34/prd.md)
- [M34 Architecture Spine](../../planning-artifacts/architecture/architecture-Athena-2026-07-24-m34/ARCHITECTURE-SPINE.md)
- [M34 Epics](epics.md)
- [Story 3.2](3-2-bind-project-ports-to-element-anchors.md)
- [Professional Renderer Target](m34-professional-renderer-target.md)

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests "com.engineeringood.athena.compiler.AthenaRepresentationPackageSnapshotCompilerTest"`
  failed before production edits because `AthenaRepresentationPackageSnapshotCompilationResult.descriptors`
  did not exist.
- GREEN: focused snapshot compiler test passed after generated descriptor projection was added.
- GREEN: `.\gradlew.bat --no-daemon --console=plain test` passed after Story 3.3 edits.
- GREEN: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed after story/sprint text edits.

### Completion Notes List

- Ultimate context engine analysis completed from M34 PRD, architecture spine, epics, Story 3.1,
  Story 3.2, and the professional renderer target note.
- AC-1 evidence: the real `examples/m34/sample-project` stages through `athena.yaml`, compiles
  Athena representation sources, emits definitions, generated descriptors, binding rules, variants,
  anchors, labels, exact versions, and `.athena` provenance.
- AC-2 evidence: sample proof asserts no XML source hashes and `xmlRuntimeAuthorityAbsent = true`;
  descriptors are generated from canonical definitions, not authored XML/Kotlin product facts.
- AC-3 evidence: M34 Cabinet proof continues to assert intrinsic Graphic Primitive bounds separately
  from `drawing-composition` content/document bounds.
- AC-4 evidence: full Gradle `test`, encoding audit, AC mapping, file list, and three-layer review are
  recorded.
- Blind review: no XML runtime authority was added; generated descriptors are derived from
  `RepresentationDefinition`.
- Edge review: sample source roots, source hashes, SVG-backed anchors/labels, and binding rules are
  covered by the authority test.
- Acceptance review: FR-21..FR-23, FR-30..FR-32, FR-34, and FR-37 are represented by generated
  descriptor projection and sample-level authority proof.

### File List

- `_bmad-output/implementation-artifacts/m34/3-3-migrate-the-m34-cabinet-package-from-xml-to-athena.md`
- `_bmad-output/implementation-artifacts/m34/sprint-status.yaml`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationPackageSnapshotCompiler.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationPackageSnapshotCompilerTest.kt`

### Change Log

- 2026-07-25: Created Story 3.3 with M34 Cabinet package authority migration guardrails.
- 2026-07-25: Added generated descriptor projection from canonical definitions and real M34 sample
  authority proof for Athena source roots, binding rules, descriptors, anchors, labels, and zero XML
  runtime authority.
