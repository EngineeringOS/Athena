---
baseline_commit: c007cd140cd0fcaffdc5ccb66bf2f917d69294cf
---

# Story 1.4: Compile Complex SVG Geometry Without Metadata Authority

Status: review

## Story

As a vendor asset author,
I want a complex SVG body to provide geometry while Athena source provides all meaning,
so that realistic assets remain maintainable without creating a second language or authority.

## Acceptance Criteria

1. Given a complex package-local SVG referenced by an Athena Element, when safe compilation runs, then namespace-aware no-I/O parsing, closed-subset validation, fixed limits, transform normalization, and `GraphicPrimitive` lowering apply, and raw markup never crosses compiler/LSP/Electron transport.
2. Given Athena geometry references and SVG XML ids, when safe acyclic `use` expansion completes, then each referenced id materializes exactly once and returns normalized geometry plus accumulated transform, and zero or multiple materializations fail as missing or ambiguous while unreferenced geometry may repeat.
3. Given SVG attributes other than optional matching `data-athena-geometry-ref`, when metadata validation runs, then SVG cannot declare identity, lifecycle, anchors, labels, roles, direction, signal, compatibility, profile, binding, physical size, or project ports, and unmarked geometry remains visual-only.
4. Given a native Athena body and an SVG-backed body, when both compile, then both produce the same canonical representation contracts and downstream Cabinet path, and duplicate/conflicting metadata and missing geometry references fail closed.
5. Given all previous acceptance criteria are green, when the mandatory story gate runs, then RED/GREEN commands, AC-to-evidence mapping, and three-layer review are recorded, and legacy SVG semantic attributes, XML authority, stale fixtures, generated output, and compatibility adapters are deleted.

## Tasks / Subtasks

- [x] Add RED tests for complex SVG geometry and metadata-authority behavior (AC: 1, 2, 3, 4)
  - [x] Cover valid complex SVG with geometry refs and `use` expansion.
  - [x] Cover duplicate ids, missing refs, ambiguous refs, forbidden metadata, unsupported namespace, unsafe URLs, and raw transport boundaries.
  - [x] Cover native-vs-SVG contract equivalence and downstream representation path.
- [x] Implement complex SVG body compilation and geometry-ref lowering (AC: 1, 2, 3, 4)
  - [x] Extend safe parser/validator for geometry references and acyclic `use` expansion.
  - [x] Normalize transforms and accumulate geometry from referenced nodes.
  - [x] Preserve source-authority boundaries and fail closed on any unresolved or ambiguous geometry.
- [x] Wire diagnostics and evidence surfaces through LSP and transport (AC: 1, 4)
  - [x] Publish source-spanned diagnostics for SVG bodies.
  - [x] Keep raw markup out of transport payloads.
- [x] Migrate the dedicated sample to use complex SVG-backed vendor material (AC: 1, 2, 3, 4)
  - [x] Update the sample vendor asset to use geometry refs where helpful.
  - [x] Prove native and SVG-backed bodies compile through the same contracts.
- [x] Polish/purge and evidence gate (AC: 5)
  - [x] Audit touched and adjacent paths for legacy SVG semantic attributes, XML authority, and stale fixtures.
  - [x] Run sequential verification and record RED/GREEN evidence.
  - [x] Record AC-to-evidence mapping and three-layer review.

## Dev Notes

### Scope Boundary

Story 1.4 owns complex SVG body compilation and geometry authority. It does not introduce new Athena source syntax. The only authored source surface should remain the existing `resource <id> { kind svg path "./asset.svg" }` package-local resource path from Story 1.3, plus SVG-local geometry hints where needed. `data-athena-geometry-ref` may exist as an optional SVG-side reference hint, but it never becomes a second authority.

### Architecture Requirements

- `AthenaSvgGraphicBodyCompiler` is the active entry point for complex SVG body compilation.
- `AthenaSvgGraphicBodySupport` owns safe parse/validation helpers, DOM budget checks, and namespace filtering.
- SVG compilation must stay no-I/O beyond the package-local asset that Athena already admitted.
- `data-athena-*` remains restricted. Complex SVG may not declare identity, lifecycle, anchors, labels, roles, direction, signal, compatibility, profile, binding, physical size, or project ports.
- `use` expansion must be safe, acyclic, and deterministic. Missing, duplicate, or ambiguous geometry references fail closed.
- `GraphicPrimitive` lowering remains the output contract. Raw markup, DOM objects, and transport-authority objects stay out of downstream payloads.
- The Athena source model remains the single authority. SVG ids are geometry lookup keys only.
- Because Athena is unreleased, delete stale SVG semantic attributes and compatibility adapters instead of preserving them.

### Previous Story Intelligence

- Story 1.3 established source-local typed resources, package-local resource resolution, and source-unit-scoped `PackageResourceKey` behavior.
- Story 1.3 verified that raw workspace-relative and traversal resource access is rejected and that `.athena` / LSP / tree-sitter surfaces stay aligned on the public resource syntax.
- The current code already has a safe SVG subset compiler path and negative tests for forbidden metadata and unsafe transport. Prefer extending that path rather than creating a parallel SVG parser.

### Likely Code Areas

- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSvgGraphicBodyCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSvgGraphicBodySupport.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationSourceCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationPackageSnapshotCompiler.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaReferencedSvgGraphicCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationPackageSnapshotCompilerTest.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepresentationSourceLspSupportTest.kt`
- `examples/m34/sample-project/packages/representation/athena/vendor/epic2-svg-elements.athena`
- `examples/m34/sample-project/packages/representation/athena/vendor/vendor-drive.svg`

### Testing Requirements

- Follow RED/GREEN. Write failing tests before production code.
- Run Gradle sequentially on Windows. Never run Gradle tasks in parallel.
- Minimum expected verification for this story:
  - targeted compiler SVG geometry tests;
  - targeted LSP diagnostics tests for SVG-backed assets if diagnostics change;
  - `encoding-audit.ps1` after any doc/source text edits;
  - `git diff --check`.

### References

- `_bmad-output/implementation-artifacts/m35/epics.md` - Epic 1, Story 1.4.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-27-m35/prd.md` - FR-13..FR-17, FR-41..FR-42.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-27-m35/addendum.md` - SVG boundary, metadata boundary, and package-local SVG guidance.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-27-m35/ARCHITECTURE-SPINE.md` - SVG safety and authority separation requirements.
- `_bmad-output/implementation-artifacts/m35/1-3-compile-source-local-typed-resources.md` - source-local resource predecessor and package-local path isolation.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- RED: `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaReferencedSvgGraphicCompilerTest` initially failed on the missing `containerBounds` SVG primitive lowering and canonical primitive comparison.
- GREEN: targeted SVG geometry test passed after `AthenaSvgGraphicBodyCompiler` lowered referenced SVG geometry with container bounds and the test compared canonical geometry contracts.
- Regression: `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test` then exposed stale M34 SVG metadata authority expectations.

### Completion Notes List

- Verified complex package-local SVG geometry compilation with safe parsing, geometry reference lowering, and closed-set validation.
- Verified LSP and transport surfaces keep raw SVG markup out of proof payloads while still publishing source-spanned diagnostics.
- Verified the dedicated M34 sample compiles both native and SVG-backed representations through the same downstream contract path.
- Purged stale SVG semantic authority, removed the legacy cabinet-label SVG fixture, and kept SVG geometry-only.

### File List

- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSvgGraphicBodyCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSvgGraphicBodySupport.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaGeneratedRepresentationBoundaryVerifier.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaReferencedSvgGraphicCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM34ElementCabinetProofTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationPackageSnapshotCompilerTest.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaDrawingProofPayloads.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationSessionProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionSessionProtocol.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDrawingProofPayloadMapperTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepresentationSourceLspSupportTest.kt`
- `examples/m34/sample-project/packages/representation/athena/vendor/epic2-svg-elements.athena`
- `examples/m34/sample-project/packages/representation/athena/vendor/vendor-drive.svg`
- `examples/m34/sample-project/packages/representation/athena/iec/cabinet-element-set.athena`
- `examples/m34/sample-project/packages/representation/athena/iec/cabinet-label.svg`
- `_bmad-output/implementation-artifacts/deferred-work.md`
- `_bmad-output/implementation-artifacts/m35/sprint-status.yaml`

## Change Log

- 2026-07-27: Completed the complex SVG geometry story, including LSP/transport evidence, sample migration, and purge of stale SVG semantic authority.
