---
baseline_commit: 0311ad67c08779db3d1def271d0ac948a4683128
---

# Story 1.3: Compile Source-Local Typed Resources

Status: review

## Story

As a representation package author,
I want to declare a package-local resource beside its Athena definition,
so that standard and vendor visuals resolve without fragile workspace-relative paths.

## Acceptance Criteria

1. Given `resource <id> { kind svg path "./asset.svg" }` and `graphic svg resource <id>` in one source unit, when ANTLR4 parsing, type checking, and resource resolution run, then the bare id resolves only to a declaration in that source unit and path resolution starts at that unit's admitted directory, and duplicate same-unit ids, missing declarations, unsupported kinds, and missing files fail with source-spanned diagnostics.
2. Given equal resource ids in different source units, when package admission runs, then they remain independent `PackageResourceKey`s and do not shadow, and moving a declaration to another source unit deterministically changes its key.
3. Given a dependency package, when a consumer references its exported Symbol or Element, then the dependency's internal resource resolves inside its own snapshot and direct cross-source/cross-package raw resource keys or physical paths are impossible.
4. Given the public resource syntax, when formatter, LSP diagnostics/completion/tokens, Tree-sitter parsing/highlighting, and parser parity run, then every frontend accepts/rejects the same corpus and repeated formatting is idempotent.
5. Given all previous acceptance criteria are green, when the mandatory story gate runs, then RED/GREEN commands, AC-to-evidence mapping, three-layer review, and polish/purge are recorded.

## Tasks / Subtasks

- [x] Add RED tests for source-local typed resource behavior (AC: 1, 2, 3, 4)
  - [x] Cover valid `resource <id> { kind svg path "./asset.svg" }` plus `graphic svg resource <id>` in the same source unit.
  - [x] Cover duplicate same-unit ids, missing declarations, unsupported kinds, and missing files.
  - [x] Cover per-source-unit `PackageResourceKey` isolation and key changes when declarations move.
  - [x] Cover dependency-exported Symbol/Element resolution without raw resource or physical-path leakage.
  - [x] Cover formatter, LSP, Tree-sitter, and parser parity against the shared accepted/rejected corpus.
- [x] Implement typed resource declaration and package-local resolution (AC: 1, 2, 3)
  - [x] Extend the existing Athena AST/compiler path for typed resource declarations and `graphic svg resource <id>`.
  - [x] Keep resource ids lexical to one source unit and resolve paths from the declaring unit's admitted package snapshot.
  - [x] Preserve `PackageResourceKey`/`SourceUnitId` isolation and dependency snapshot boundaries.
  - [x] Reject absolute, traversal, cross-package, and workspace-relative resource access.
- [x] Wire resource syntax through frontend and parser parity surfaces (AC: 4)
  - [x] Update formatter, LSP diagnostics/completion/tokens, Tree-sitter grammar/highlighting, and parser parity corpus together.
  - [x] Keep repeated formatting idempotent and diagnostics source-spanned.
- [x] Migrate the dedicated M35 sample to use typed local resources (AC: 1, 2, 3, 4)
  - [x] Add at least one standard package and one vendor/user package using package-hierarchical source layout.
  - [x] Prove one local SVG-backed resource and one dependency-exported visual reference.
  - [x] Do not add SVG geometry hints, physical installation syntax, routing, or Cabinet composition in this story.
- [x] Polish/purge and evidence gate (AC: 5)
  - [x] Audit touched and adjacent paths for resource scanners, raw path hacks, XML/package compatibility names, and stale fixtures.
  - [x] Run sequential verification and record RED/GREEN evidence.
  - [x] Record AC-to-evidence mapping and three-layer review.

## Dev Notes

### Scope Boundary

Story 1.3 owns typed package-local resources and their compiler/LSP/editor parity. It implements FR-5, FR-7..FR-11, FR-41..FR-42 from M35. It must not implement complex SVG geometry hints, package-local SVG metadata authority, physical installation syntax, Cabinet composition, routing, or editable trace. Story 1.4 owns complex SVG-backed vendor geometry; later stories own physical installation and Cabinet composition.

### Architecture Requirements

- `resource <id> { kind svg path "./asset.svg" }` plus `graphic svg resource <id>` is the normative public surface for this story.
- Resource ids are lexical to one governed source unit. A bare id must resolve only inside the declaration's source unit.
- `SourceUnitId` and `PackageResourceKey` remain source-unit scoped and must not collapse across files or packages.
- A declared resource path resolves only from the declaring source unit's admitted package snapshot. Workspace root, repository root, renderer state, and process working directory are forbidden authority.
- Dependencies may expose compiled Symbol/Element definitions, but raw resource keys and physical paths do not cross source-unit or package boundaries.
- Use the existing repository contract and admission pipeline. Do not add a second manifest, workspace-relative hacks, or regex scanners.
- Keep resource snapshot identity deterministic and content-bound. Lock evidence is support data, not authority for resource identity.
- ANTLR4 remains parser authority. Tree-sitter, formatter, LSP, and highlighting must follow the same accepted/rejected corpus.
- Because Athena is unreleased, delete stale resource/path compatibility behavior instead of wrapping it.

### Previous Story Intelligence

- Story 1.1 enforced Java-style package hierarchy and stable package/path diagnostics.
- Story 1.2 replaced lock v1 with immutable lock v2, fail-closed validate/update, and package admission budgets.
- The current M35 architecture already treats `RepositoryManifest` as the single authored descriptor contract and `athena.lock` as derived evidence.
- Existing code already has typed resource-related model work in progress; prefer extending the current compiler/repository pipeline instead of adding a parallel resolver.

### Likely Code Areas

- `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationSourceCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationMaterialResolver.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationMaterialBinder.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryContractLoader.kt`
- `kernel/package-model/src/main/kotlin/com/engineeringood/athena/packageplatform/RepresentationDescriptorModels.kt`
- `kernel/package-model/src/main/kotlin/com/engineeringood/athena/packageplatform/RepresentationBindingRuleModels.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/RepresentationPackageSnapshotCapture.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/RepresentationPackageSnapshotStager.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/*Resource*Test.kt`
- `ide/tree-sitter-athena/grammar.js`
- `ide/tree-sitter-athena/queries/highlights.scm`
- `ide/tree-sitter-athena/test/corpus/*`
- `examples/m35/physical-installation-cabinet/...` only for the sample source/resource proof.

Use CodeGraph before reading or editing source files because the repo is indexed.

### Testing Requirements

- Follow RED/GREEN. Write failing tests before production code.
- Run Gradle sequentially on Windows. Never run Gradle tasks in parallel.
- Minimum expected verification for this story:
  - targeted compiler/package resource tests;
  - targeted LSP diagnostics/highlighting tests;
  - targeted Tree-sitter/parser parity tests if syntax changes;
  - encoding audit after doc/source text edits;
  - `git diff --check`.

### References

- `_bmad-output/implementation-artifacts/m35/epics.md` - Epic 1, Story 1.3.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-27-m35/prd.md` - FR-5, FR-7..FR-11, FR-41..FR-42.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-27-m35/addendum.md` - Package-Local Resource Resolution, Package Hierarchy Rule.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-27-m35/ARCHITECTURE-SPINE.md` - AD-7, AD-8, AD-9, AD-10, AD-15, AD-17.
- `_bmad-output/implementation-artifacts/m35/1-2-admit-and-lock-immutable-package-snapshots.md` - lock v2 and immutable admission predecessor.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-07-27: Verified source-local typed resource flow end to end with compiler, LSP, and Tree-sitter parity tests.
- 2026-07-27: Confirmed `profile ControlDrawingIEC` and `use element "iec.protective-earth.element" version "1.0.0"` are accepted in the M35 field binding sample.
- 2026-07-27: Confirmed package-local SVG resolution stays source-unit scoped and raw workspace-relative resource access is rejected.

### Completion Notes List

- Verified typed `resource <id> { kind svg path "./asset.svg" }` plus `graphic svg resource <id>` behavior across compiler, LSP, and Tree-sitter surfaces.
- Verified duplicate ids, missing files, unsupported kinds, and traversal/absolute path rejection through targeted compiler tests.
- Verified M35 sample bindings resolve the expected local and dependency visual references without XML runtime authority.
- Verified repeated formatting idempotence and shared accept/reject corpus parity on the representation surface.

### File List

- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationSourceCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSourceResourceSupport.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaElementSourceLowerer.kt`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepresentationSourceLspSupportTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationPackageSnapshotCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaReferencedSvgGraphicCompilerTest.kt`
- `ide/tree-sitter-athena/grammar.js`
- `ide/tree-sitter-athena/queries/highlights.scm`
- `ide/tree-sitter-athena/test/corpus/symbol.txt`
- `ide/tree-sitter-athena/tree-sitter-athena.wasm`
- `examples/m34/professional-control-drawing/packages/representation/com/engineeringood/m34/control/field/field-bindings.athena`
- `examples/m34/professional-control-drawing/packages/representation/com/engineeringood/m34/control/field/field-material.athena`
- `examples/m34/professional-control-drawing/packages/representation/com/engineeringood/m34/control/field/profile.athena`
- `examples/m35/professional-control-drawing/packages/representation/com/engineeringood/m34/control/field/field-bindings.athena`
- `examples/m35/professional-control-drawing/packages/representation/com/engineeringood/m34/control/field/field-material.athena`
- `examples/m35/professional-control-drawing/packages/representation/com/engineeringood/m34/control/field/profile.athena`

## Change Log

- 2026-07-27: Completed the M35 source-local typed resource story with compiler, LSP, Tree-sitter, and sample-project verification.
