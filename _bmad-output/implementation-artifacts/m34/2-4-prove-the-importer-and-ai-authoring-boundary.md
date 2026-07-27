---
baseline_commit: 0311ad67c08779db3d1def271d0ac948a4683128
---

# Story 2.4: Prove The Importer And AI Authoring Boundary

Status: review

## Story

As an importer or AI agent,
I want every generated asset to become one canonical governed representation source,
so that foreign or generated materials use the same compiler contract as native work.

## Acceptance Criteria

1. **Given** a QET `.elmt`, vendor export, or AI-generated complex-visual fixture, **when** the M34
   import boundary is exercised, **then** output is canonical Athena representation source and, for
   complex geometry, one referenced governed annotated SVG resource in the same package directory;
   no foreign runtime schema, XML manifest, or duplicate metadata sidecar is produced.
2. **Given** the QET analysis fixture and existing reference mirror, **when** product/runtime
   dependencies and active Cabinet paths are inspected, **then** no QET runtime, full converter, or
   foreign schema dependency has been introduced in M34.
3. **Given** an imported connectable point without an explicit native-Athena or `data-athena-*`
   anchor contract, **when** generated source compiles, **then** it is rejected until
   role/direction/signal compatibility is authored and validated by the same compiler contracts as
   native assets.
4. **Given** one complex vendor-style generated Element under the Java-style package hierarchy,
   **when** it passes formatter/canonicalizer, lint, compiler, safe SVG metadata validation, and
   immutable package snapshot validation, **then** it is visible in the Epic 2 Cabinet proof with no
   foreign/runtime metadata dependency.
5. **Given** all previous criteria are green, **when** mandatory polish/purge runs, **then** source,
   tests, fixtures, docs, generated outputs, and workspace state are deeply reviewed; stale/duplicate
   artifacts are removed; and RED/GREEN, AC-to-evidence, and three-layer review are recorded.

**Implements:** FR-5..FR-8, FR-38..FR-42; NFR-1..NFR-4, NFR-7, NFR-9, NFR-12.

## Tasks / Subtasks

- [x] Add Story 2.4 RED contracts before production edits (AC: 1..4)
  - [x] Add a failing importer/AI boundary proof test that expects generated output to be ordinary
        `.athena` representation source plus one package-local governed SVG only when geometry is
        complex.
  - [x] Add a failing dependency/authority audit that rejects active QET runtime, `.elmt` runtime
        reads, foreign XML manifests, frontend schemas, raw SVG transport, and sidecar metadata.
  - [x] Add a failing invalid-generated-asset test where an imported visual point is present but no
        native or `data-athena-*` anchor contract declares role/direction/signal.
  - [x] Add a failing Cabinet proof assertion for one generated vendor-style Element compiled from
        the immutable snapshot.
- [x] Implement the narrow importer/AI proof boundary, not a full importer (AC: 1, 2)
  - [x] Add a compiler-owned proof fixture or support type that models importer/AI output admission as
        "candidate source files", not as runtime objects or foreign schemas.
  - [x] Require output layout to match the package declaration, for example
        `packages/representation/athena/generated/vendor-drive.athena` with
        `./vendor-drive.svg` beside it.
  - [x] Preserve the rule that simple generated visuals use native Athena `graphic { ... }`; complex
        generated visuals use `graphic svg "./asset.svg"` with `data-athena-*` only on contract nodes.
  - [x] Do not parse QET `.elmt` as product runtime input. If a QET fixture is used, treat it only as
        reference evidence for a documented generated Athena/SVG fixture.
- [x] Enforce generated-source admission through existing compilers (AC: 1, 3, 4)
  - [x] Route generated `.athena` files through `AthenaRepresentationSourceCompiler` and referenced
        SVG files through `AthenaSvgGraphicBodyCompiler` / `AthenaSvgGraphicBodySupport`.
  - [x] Route generated packages through `RepresentationPackageSnapshotStager` and
        `AthenaRepresentationPackageSnapshotCompiler` before any Cabinet proof consumes them.
  - [x] Reject missing explicit anchor contracts, wrong `data-athena-*` fields, SVG root identity,
        duplicate definition identity, and package/resource escape with stable diagnostics and zero
        partial output.
  - [x] Keep LSP/Electron transport structurally incapable of carrying raw markup.
- [x] Add the Epic 2 Cabinet-visible generated-asset proof (AC: 4)
  - [x] Add or extend the M34 sample with one generated vendor-style Element under
        `examples/m34/sample-project/packages/representation/athena/generated/`.
  - [x] Prove the generated Element binds/renders through compiled snapshot data only, using existing
        test-owned proof policy until Story 3.1 adds production Profile/Binding selection.
  - [x] Assert structured proof identifies Athena source provenance, SVG source provenance, snapshot
        id, generated-source boundary, anchors/slots/hotspots, and zero QET/XML/raw-markup authority.
- [x] Run sequential verification (AC: 1..4)
  - [x] Run focused compiler/package-runtime/boundary proof tests.
  - [x] Run focused referenced-SVG and M34 Cabinet proof tests.
  - [x] Run affected module tests and full Gradle `test` after focused suites pass; never overlap
        Gradle runs.
  - [x] Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after text/doc edits.
- [x] Perform mandatory deep polish/purge and evidence review (AC: 5)
  - [x] Audit importer vocabulary, package hierarchy, QET boundary, AI-generated fixtures,
        source/compiler/runtime/renderer ownership, generated outputs, docs, encoding, diff, and
        dirty-worktree boundaries.
  - [x] Remove stale converter stubs, `.elmt` runtime hooks, XML compatibility, sidecar metadata,
        global resource pools, raw markup transport, renderer inference, and generated artifacts not
        meant for source control.
  - [x] Record RED/GREEN, AC-to-evidence, independent blind/edge/acceptance reviews, review
        dispositions, and every touched file before changing the story to review.

## Dev Notes

### Scope Boundary

This story proves the boundary for importer and AI authoring. It does not implement a QET converter,
vendor marketplace, remote package fetch, AI generator, symbol editor, product authoring workflow, or
production Profile/Binding selection. The product path remains Cabinet-only and consumes compiled
Athena package snapshots.

### Required Output Shape

Generated material must look like normal authored package source:

```text
packages/representation/
  athena/
    generated/
      vendor-drive.athena
      vendor-drive.svg
```

```athena
package athena.generated

element generated_vendor_drive {
  identity "athena.generated.vendor_drive"
  version "1.0.0"

  graphic svg "./vendor-drive.svg"
}
```

The SVG may use `data-athena-schema="representation/v1"` on the root and `data-athena-*` only on
contract-bearing nodes. Definition identity, version, kind, lifecycle, project devices, actual ports,
Profile, Binding, and package policy remain in Athena source or project source, never in SVG or QET.

### Non-Negotiable Boundaries

- Importers and AI produce source. They do not produce `RepresentationDefinition`,
  `RepresentationDescriptor`, occurrences, Graphic Primitive IR, renderer payloads, or runtime package
  metadata directly.
- QET `.elmt` is reference/import input only. It is not runtime schema, package dependency, product
  manifest, LSP payload, renderer input, or Cabinet authority.
- Do not create XSD, JSON schema, XML manifest, sidecar metadata, or a standalone generated-asset IR.
- No generated connectable point is trusted until native Athena or governed `data-athena-*` declares
  explicit anchor id, point, role, direction, and signal compatibility.
- Package resources stay beside or below the owning `.athena` source directory. `../`, absolute paths,
  shared global resource pools, and renderer filesystem reads remain invalid.
- Preserve the existing M34 rule: native Athena for simple visuals; referenced governed SVG for
  complex visuals; both lower to one canonical `RepresentationDefinition` and `GraphicPrimitiveDocument`.

### Existing Code To Extend

- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationSourceCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSvgGraphicBodyCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSvgGraphicBodySupport.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationPackageSnapshotCompiler.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/RepresentationPackageSnapshotStager.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/RepresentationPackageSnapshotStagerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaReferencedSvgGraphicCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM34ElementCabinetProofTest.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/NativeRepresentationLibraryLoader.kt`
- `examples/m34/sample-project/packages/representation/athena/vendor/`

### Previous Story Intelligence

- Story 2.1 implemented `graphic svg "./asset.svg"` and compiler-owned SVG `data-athena-*`
  diagnostics. Reuse that compiler; do not add another SVG schema.
- Story 2.2 implemented immutable package snapshots and Java-style package/resource hierarchy.
  Generated resources must live beside their owning `.athena` source and pass snapshot staging.
- Story 2.3 added IDE diagnostics/completion/formatting/semantic tokens for representation sources
  and SVG resources. Any new generated fixture should remain compatible with that support.
- `RepresentationPackageSnapshotStagerTest` now uses explicit invalid source fixtures instead of
  mutating a valid source string to test `../` escapes. Keep tests readable this way.
- Current workspace is intentionally dirty with M33/M34 work. Preserve unrelated changes and record
  every file touched by this story.

### Testing Requirements

- Capture genuine RED for boundary proof, QET/runtime dependency audit, invalid generated anchor
  contract, and generated Cabinet proof before implementation.
- Assert exact diagnostic codes and zero output for invalid generated assets.
- Use repository-local fixtures only. Do not fetch QET, npm, Maven, or network resources.
- If inspecting the QET reference mirror, treat it as read-only reference evidence. Do not import its
  schema into product code.
- Prove active product/runtime code has no QET `.elmt` read path and no XML manifest authority for
  the M34 Cabinet path.
- Run Gradle verification sequentially only.

### References

- [M34 PRD](../../planning-artifacts/prds/prd-Athena-2026-07-24-m34/prd.md)
- [M34 Addendum](../../planning-artifacts/prds/prd-Athena-2026-07-24-m34/addendum.md)
- [M34 Architecture Spine](../../planning-artifacts/architecture/architecture-Athena-2026-07-24-m34/ARCHITECTURE-SPINE.md)
- [M34 Annotated SVG Course Correction](../../planning-artifacts/sprint-change-proposal-2026-07-24-m34-annotated-svg-authority.md)
- [M34 Epics](epics.md)
- [Story 2.1](2-1-compile-a-referenced-governed-svg-graphic-body.md)
- [Story 2.2](2-2-stage-external-assets-into-an-immutable-package-snapshot.md)
- [Story 2.3](2-3-complete-athena-ide-support-for-representation-source.md)

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests "com.engineeringood.athena.compiler.AthenaM34ImporterAiBoundaryTest"` failed before production code with unresolved `AthenaGeneratedRepresentationBoundaryVerifier` and `AthenaGeneratedRepresentationBoundaryRequest`.
- RED: the same focused test then failed on `.elmt` rejection because snapshot staging intentionally ignored non-`.athena` / non-`.svg` files, exposing the need for generated-boundary preflight before staging.
- GREEN: `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests "com.engineeringood.athena.compiler.AthenaM34ImporterAiBoundaryTest"` passed.
- GREEN: `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests "com.engineeringood.athena.compiler.AthenaM34ImporterAiBoundaryTest" --tests "com.engineeringood.athena.compiler.AthenaReferencedSvgGraphicCompilerTest" --tests "com.engineeringood.athena.compiler.AthenaRepresentationPackageSnapshotCompilerTest" --tests "com.engineeringood.athena.compiler.AthenaM34ElementCabinetProofTest"` passed.
- GREEN: `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test --tests "com.engineeringood.athena.packageruntime.RepresentationPackageSnapshotStagerTest"` passed.
- GREEN: `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test` passed.
- GREEN: `.\gradlew.bat --no-daemon --console=plain test` passed with 151 actionable tasks.

### Completion Notes List

- Ultimate context engine analysis completed from M34 PRD/addendum, architecture spine, epics,
  sprint-change proposal, Stories 2.1-2.3, recent git history, and current package-local resource
  hierarchy correction.
- Added `AthenaGeneratedRepresentationBoundaryVerifier` as a narrow proof boundary: importer/AI
  output is admitted only as candidate Athena package source, staged through immutable package
  snapshots, then compiled through existing representation and SVG compilers.
- Added preflight rejection for `.elmt`, `.qet`, `.xsd`, and `.xml` files inside generated
  representation package roots before staging.
- Added required-anchor verification so an unannotated generated visual point cannot become a
  connectable contract by shape/id inference; missing anchors emit `generated.anchor-contract.missing`
  and return zero definitions.
- Added generated M34 sample files under `packages/representation/athena/generated/` with the SVG
  resource beside its owning `.athena` source.
- Updated the M34 sample README to describe the generated package and state that Story 2.4 adds no
  runtime importer.
- AC-to-evidence: AC1 covered by generated-output admission test; AC2 by `.elmt` / foreign-schema
  preflight test and authority proof flags; AC3 by missing generated-anchor contract test; AC4 by
  generated vendor-style sample render proof; AC5 by audits, README cleanup, sequential tests, and
  encoding audit.
- Three-layer review: blind boundary review checked no importer creates definitions/descriptors/IR
  directly; edge-case review found `.elmt` files were invisible after snapshot filtering and moved the
  check to preflight; acceptance review checked generated fixture hierarchy, zero raw markup/QET/XML
  proof flags, no partial definitions on diagnostics, and full Gradle regression.

### File List

- `_bmad-output/implementation-artifacts/m34/2-4-prove-the-importer-and-ai-authoring-boundary.md`
- `_bmad-output/implementation-artifacts/m34/sprint-status.yaml`
- `examples/m34/sample-project/README.md`
- `examples/m34/sample-project/packages/representation/athena/generated/generated-drive.athena`
- `examples/m34/sample-project/packages/representation/athena/generated/generated-drive.svg`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaGeneratedRepresentationBoundaryVerifier.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM34ImporterAiBoundaryTest.kt`

### Change Log

- 2026-07-24: Created Story 2.4 with importer/AI boundary proof scope and M34 source-authority guardrails.
- 2026-07-24: Implemented generated representation boundary proof, foreign schema preflight,
  required-anchor validation, generated sample fixture, and full sequential regression.
