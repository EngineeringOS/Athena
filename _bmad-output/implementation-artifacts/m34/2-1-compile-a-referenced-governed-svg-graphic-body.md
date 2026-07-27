---
baseline_commit: 0311ad67c08779db3d1def271d0ac948a4683128
---

# Story 2.1: Compile A Referenced Governed SVG Graphic Body

Status: review

## Story

As a vendor asset author,
I want an Athena Symbol or Element declaration to reference one governed annotated SVG graphic body,
so that complex professional visuals stay maintainable while Athena remains the sole definition
metadata authority.

## Acceptance Criteria

1. **Given** one representation source unit with an Athena `symbol` or `element` declaration containing
   `graphic svg "./asset.svg"`, Athena-owned identity/version/kind/lifecycle, and a referenced SVG
   whose root declares only `data-athena-schema="representation/v1"` plus valid namespace/viewBox,
   **when** ANTLR4 parsing, SVG acquisition, safe XML parsing, annotation validation, semantic lint,
   and canonical admission run, **then** exactly one `RepresentationDefinition` is produced with
   `GRAPHIC_PRIMITIVE` authority, source provenance for both files, and a renderer-valid
   `GraphicPrimitiveDocument`.
2. **Given** selected SVG nodes with `data-athena-anchor`, `data-athena-point`,
   `data-athena-role`, `data-athena-direction`, `data-athena-signal`, `data-athena-label-slot`, or
   `data-athena-hotspot`, **when** the SVG compiler lowers them, **then** only explicit node-local
   contracts are admitted; unmarked geometry, ids, CSS, DOM order, and SVG element type infer no
   Athena meaning.
3. **Given** SVG root identity/version/kind/lifecycle/profile/binding metadata, unknown
   `data-athena-*` fields, wrong-node annotations, duplicate SVG ids/contracts, malformed points,
   incompatible annotation combinations, missing referenced files, absolute/traversal paths,
   unsupported namespaces/elements/attributes, DTD/entities/XInclude, scripts/events,
   `foreignObject`, external/data/file URLs, unsafe CSS resources, cyclic `use`, excessive depth,
   excessive elements, excessive path segments, or excessive emitted primitives, **when** compilation
   runs, **then** stable source-spanned diagnostics are emitted and no definition or partial package
   admission value is produced.
4. **Given** the same Athena declaration attempts both native `graphic { ... }` and `graphic svg`,
   or duplicate Athena declarations share `(library, identity, version)`, **when** compilation runs,
   **then** the batch fails closed without format precedence or field merge. SVG root metadata never
   participates in identity resolution.
5. **Given** one complex M34 vendor-style SVG fixture, **when** it compiles and passes through the
   existing explicit test-owned Cabinet proof policy, `RepresentationBindingCompiler`,
   drawing-composition, `GraphicPrimitiveSvgAdapter`, and `GraphicPrimitiveSvgCanvasComposer`, **then**
   the output is visibly nonblank and structured proof identifies Athena definition provenance,
   SVG graphic-body provenance, anchors/slots/hotspots, renderer authority, and zero XML manifest/raw
   markup/runtime authority.
6. **Given** all previous criteria are green, **when** mandatory polish/purge runs, **then** source,
   tests, grammar outputs, fixtures, proof artifacts, docs, generated output, encoding, and workspace
   state are deeply reviewed; stale or duplicate artifacts are removed; and RED/GREEN,
   AC-to-evidence, and three-layer review are recorded.

**Implements:** FR-5..FR-8, FR-11, FR-24..FR-26, FR-28, FR-41.

## Tasks / Subtasks

- [x] Add Story 2.1 RED contracts before production edits (AC: 1..5)
  - [x] Add one valid `.athena` fixture using `graphic svg "./vendor-drive.svg"` and one SVG fixture
        with root `data-athena-schema="representation/v1"`, complex geometry, two governed anchors,
        one label slot, and unmarked ordinary geometry.
  - [x] Add invalid fixtures for every AC3 failure class and assert exact diagnostic code, file,
        complete span, subject, message, and zero admitted definitions.
  - [x] Add failing proof test that requires the referenced SVG body to bind, compose, and render
        through the existing typed Cabinet stack with no XML manifest, raw markup, or fallback.
- [x] Extend the frozen Athena representation syntax narrowly (AC: 1, 4)
  - [x] Add only `graphic svg "<relative-path>"` as an alternative graphic body inside Symbol/Element
        declarations; do not add standalone SVG declarations, XML declarations, Profile/Binding, DOM,
        descriptor, occurrence, renderer, or transport vocabulary.
  - [x] Preserve project `system` source disjointness and existing native Symbol/Element parsing.
  - [x] Reject declarations that contain both native graphic bodies and `graphic svg`.
- [x] Implement a safe SVG graphic-body compiler frontend (AC: 1..3)
  - [x] Resolve SVG paths only relative to the representation source/package root; reject absolute
        paths, traversal, symlinks/junctions/reparse points where detectable, missing files, and
        unsupported secure-open guarantees.
  - [x] Configure namespace-aware XML parsing that disables DTD, entities, external access, XInclude,
        scripts/events, `foreignObject`, external/data/file URLs, and unsafe CSS/resource features.
  - [x] Implement a closed geometry subset lowering into existing `GraphicPrimitive` only; add
        minimal ellipse/path support only if required by the valid fixture and protected by tests.
  - [x] Enforce deterministic per-file budgets for bytes, elements, DOM depth, transform depth,
        `use` expansion, normalized path segments, emitted primitives, and work units.
- [x] Validate governed `data-athena-*` node annotations (AC: 2, 3)
  - [x] Allow root only `data-athena-schema="representation/v1"`; reject identity, version, kind,
        lifecycle, profile, binding, or project truth on SVG root or nodes.
  - [x] Allow selected node annotations only for anchor, point, role, direction, signal, label slot,
        and hotspot contracts with exhaustive typed values.
  - [x] Normalize anchor/slot/hotspot points into root coordinates across supported viewBox, group,
        transform, `defs`, and acyclic `use` constructs.
  - [x] Emit stable source-spanned diagnostics for unknown fields, wrong-node fields, duplicate
        contracts, malformed values, unresolved references, and ambiguous transforms.
- [x] Lower through the existing canonical representation contract (AC: 1, 4)
  - [x] Reuse `RepresentationDefinition` and `GraphicPrimitiveDocument`; do not add an SVG IR,
        Element IR, descriptor authority, raw markup transport, or renderer-side inference path.
  - [x] Add source provenance that distinguishes Athena definition file from SVG graphic body file.
  - [x] Run `GraphicPrimitiveIrValidator` and `RepresentationContractValidator` before admission.
  - [x] Preserve no-partial-output behavior for any batch diagnostic.
- [x] Add the Epic 2 Cabinet-visible proof without premature product authority (AC: 5)
  - [x] Add `examples/m34/sample-project/packages/representation` complex SVG fixture(s) and Athena
        definition references without XML manifests.
  - [x] Use an explicit test-owned policy only; do not add production BindingResolver/Profile logic
        before Story 3.1.
  - [x] Assert nonblank SVG adapter output, transparent normal chrome, derived bounds, structured
        definition/SVG provenance, anchors/slots/hotspots, and no fallback/raw markup/XML authority.
- [x] Add tree-sitter and IDE syntax parity for `graphic svg` only (AC: 1, 4)
  - [x] Extend tree-sitter grammar, corpus, incomplete-source fixtures, and highlight captures for
        `graphic svg`.
  - [x] Regenerate committed grammar JSON, node types, parser C, and WASM through repository scripts.
  - [x] Leave full SVG XML editor completion/outline to Story 2.3 unless required for compiler tests.
- [x] Run sequential verification (AC: 1..5)
  - [x] Run focused SVG compiler, representation, proof, and language tests.
  - [x] Run tree-sitter `yarn build` and `yarn test`.
  - [x] Run affected module tests and full Gradle `test` only after focused suites pass; never overlap
        Gradle runs.
- [x] Perform mandatory deep polish/purge and evidence review (AC: 6)
  - [x] Audit public vocabulary, SVG field ownership, source/compiler/model boundaries, resource
        safety, generated files, fixtures, proof artifacts, docs, encoding, diff, and dirty-worktree
        boundaries.
  - [x] Remove any XML manifest/runtime compatibility, standalone SVG definition authority,
        duplicate sidecar metadata, raw markup transport, renderer inference, or temporary parser path.
  - [x] Record RED/GREEN, AC-to-evidence, independent blind/edge/acceptance reviews, review
        dispositions, and every touched file before changing the story to review.

## Dev Notes

### Frozen Story Syntax

Simple native graphics stay as Story 1.2/1.3 defined them. Complex graphics use this form:

```athena
package com.abb.cabinet

element abb_acs380_cabinet {
  identity "com.abb.acs380.cabinet"
  version "1.0.0"

  graphic svg "./abb-acs380-cabinet.svg"
}
```

The referenced SVG owns only geometry plus node-local representation contracts:

```xml
<svg xmlns="http://www.w3.org/2000/svg"
     viewBox="0 0 240 360"
     data-athena-schema="representation/v1">
  <rect id="body" x="8" y="8" width="224" height="344"/>

  <circle id="power-in-dot" cx="24" cy="48" r="4"
          data-athena-anchor="powerIn"
          data-athena-point="24 48"
          data-athena-role="terminal"
          data-athena-direction="in"
          data-athena-signal="AC"/>

  <text id="tag" x="120" y="32"
        data-athena-label-slot="deviceTag"
        data-athena-point="120 32">ACS380</text>
</svg>
```

SVG root must not declare `data-athena-identity`, `data-athena-version`, `data-athena-kind`,
`data-athena-lifecycle`, profile, binding, device, port, connection, or project layout. Those fields
belong to Athena source or project source.

### Non-Negotiable Boundaries

- Athena declaration owns definition identity/version/kind and chooses exactly one graphic body.
- SVG owns complex geometry and node-local anchor/slot/hotspot annotations only.
- No XML package manifest, XML runtime authority, raw SVG transport, DOM parsing in Electron, or
  renderer-side metadata inference is allowed.
- The compiler combines Athena + SVG into `RepresentationDefinition` and `GraphicPrimitiveDocument`;
  raw XML is discarded at the compiler boundary.
- `graphic svg` is not an importer and not QET support. Importer/AI boundaries remain Story 2.4.
- Story 2.1 does not add production profile/binding selection; that remains Story 3.1.

### Existing Code To Extend

- `Athena.g4`, `AthenaLanguageModel.kt`, and `AthenaAntlrParseAdapter.kt` own source-spanned Athena
  syntax and authored AST.
- `AthenaRepresentationSourceCompiler` is the single mixed representation compiler; extend it rather
  than adding a standalone SVG definition authority.
- `AthenaElementSourceLowerer`, `AthenaSymbolSourceLowerer`, and validators should share canonical
  `GraphicPrimitiveDocument` lowering/admission.
- `GraphicPrimitiveModels.kt`, `GraphicPrimitiveValidation.kt`, and `GraphicPrimitiveSvgAdapter.kt`
  are the active visual vocabulary and renderer path.
- Story 1.3 split Element validation into cohesive files; preserve that organization.

### Previous Story Intelligence

- Story 1.3 proved native Element composition, tree-sitter Element parity, example package compile
  proof, and full Gradle regression.
- A stale architecture path briefly made annotated SVG an independent definition source. That is
  rejected. Story 2.1 must implement `.athena -> graphic svg -> governed SVG body`, not standalone
  SVG identity ownership.
- Gradle tests may run with module-relative working directories. Tests reading repository fixtures
  should resolve the repository root explicitly.
- Tree-sitter node tests load committed WASM; after grammar changes run `yarn build` before `yarn test`.

### References

- [M34 PRD](../../planning-artifacts/prds/prd-Athena-2026-07-24-m34/prd.md)
- [M34 Addendum](../../planning-artifacts/prds/prd-Athena-2026-07-24-m34/addendum.md)
- [M34 Architecture Spine](../../planning-artifacts/architecture/architecture-Athena-2026-07-24-m34/ARCHITECTURE-SPINE.md)
- [M34 SVG Graphic Body Course Correction](../../planning-artifacts/sprint-change-proposal-2026-07-24-m34-annotated-svg-authority.md)
- [M34 Epics](epics.md)
- [Story 1.3](1-3-compose-and-display-one-typed-element.md)
- `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationSourceCompiler.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/GraphicPrimitiveModels.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/GraphicPrimitiveValidation.kt`
- `kernel/svg-renderer/src/main/kotlin/com/engineeringood/athena/renderer/svg/GraphicPrimitiveSvgAdapter.kt`
- `ide/tree-sitter-athena/grammar.js`

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: `:kernel:compiler:test --tests "com.engineeringood.athena.compiler.AthenaReferencedSvgGraphicCompilerTest"` failed after adding hotspot lowering assertions; `:kernel:compiler:test --tests "com.engineeringood.athena.compiler.AthenaReferencedSvgGraphicCompilerTest" --tests "com.engineeringood.athena.compiler.AthenaM34ElementCabinetProofTest"` failed after adding `GraphicPrimitiveDocument.provenanceSources` assertions.
- GREEN: `:kernel:compiler:test --tests "com.engineeringood.athena.compiler.AthenaReferencedSvgGraphicCompilerTest"` passed; `:kernel:compiler:test --tests "com.engineeringood.athena.compiler.AthenaM34ElementCabinetProofTest"` passed; `:kernel:representation-model:test` passed; `:kernel:compiler:test` passed; `ide/tree-sitter-athena` `yarn build` and `yarn test` passed; full `.\gradlew.bat --no-daemon --console=plain test` passed; `tools/encoding-audit.ps1` passed.

### Completion Notes List

- Ultimate context engine analysis completed from corrected M34 PRD/addendum, architecture spine,
  epics, sprint-change proposal, and Story 1.3 completion evidence.
- Implemented `graphic svg "./asset.svg"` for Symbol and Element declarations without adding SVG,
  XML, descriptor, profile, binding, renderer, or transport vocabulary.
- Added governed package-local SVG compilation: paths resolve only inside the source directory, so
  `package athena.vendor` resources live beside the `.athena` file under
  `packages/representation/athena/vendor/`, Java-package style.
- Lowered allowed SVG `rect`, `line`, `circle`, and `text` geometry into `GraphicPrimitiveDocument`;
  unsupported `path`, `use`, transforms, namespaces, scripts, events, `foreignObject`, resource URLs,
  unsafe CSS URL values, and DTD/entities fail closed with stable diagnostics.
- Admitted only explicit `data-athena-*` node-local contracts for anchors, label slots, and hotspots;
  SVG ids, DOM order, CSS, and element type carry no Athena semantic authority.
- Added `GraphicPrimitiveDocument.provenanceSources` so compiled SVG graphic bodies carry both the
  Athena definition source and the package-local SVG body source.
- AC evidence: AC1 `AthenaM34SymbolSyntaxTest` and `AthenaReferencedSvgGraphicCompilerTest`; AC2
  hotspot/anchor/label assertions in `AthenaReferencedSvgGraphicCompilerTest`; AC3 invalid SVG/path
  diagnostic table and byte-budget test; AC4 native/SVG grammar and validator tests; AC5
  `AthenaM34ElementCabinetProofTest`; AC6 final rg audit, full Gradle test, tree-sitter test, and
  encoding audit.
- Three-layer review: blind boundary review found missing SVG provenance and was fixed; edge review
  found unreachable primitive budget and was fixed; acceptance review found stale flat package path
  in Story 1.3 evidence and M34 sample layout and was fixed.

### File List

- `_bmad-output/implementation-artifacts/m34/2-1-compile-a-referenced-governed-svg-graphic-body.md`
- `_bmad-output/implementation-artifacts/m34/1-3-compose-and-display-one-typed-element.md`
- `_bmad-output/implementation-artifacts/m34/sprint-status.yaml`
- `examples/m34/sample-project/README.md`
- `examples/m34/sample-project/packages/representation/athena/iec/epic1-native-elements.athena`
- `examples/m34/sample-project/packages/representation/athena/vendor/epic2-svg-elements.athena`
- `examples/m34/sample-project/packages/representation/athena/vendor/vendor-drive.svg`
- `examples/m34/sample-project/packages/representation/epic1-native-elements.athena` (deleted)
- `ide/tree-sitter-athena/grammar.js`
- `ide/tree-sitter-athena/queries/highlights.scm`
- `ide/tree-sitter-athena/scripts/athena-tree-sitter-grammar-corpus.test.mjs`
- `ide/tree-sitter-athena/scripts/athena-tree-sitter-incomplete-source.test.mjs`
- `ide/tree-sitter-athena/src/grammar.json`
- `ide/tree-sitter-athena/src/node-types.json`
- `ide/tree-sitter-athena/src/parser.c`
- `ide/tree-sitter-athena/test/corpus/symbol.txt`
- `ide/tree-sitter-athena/test/fixtures/m34-svg-graphic.athena`
- `ide/tree-sitter-athena/test/incomplete/unclosed-svg-graphic.athena.txt`
- `ide/tree-sitter-athena/tree-sitter-athena.wasm`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaElementBasicValidator.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaElementReferenceValidator.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaElementSourceLowerer.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSvgGraphicBodyCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSvgGraphicBodySupport.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSymbolSourceFormatter.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSymbolSourceLowerer.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSymbolSourceModels.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSymbolSourceValidator.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaElementSourceCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM34ElementCabinetProofTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaReferencedSvgGraphicCompilerTest.kt`
- `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaM34SymbolSyntaxTest.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/GraphicPrimitiveModels.kt`

### Change Log

- 2026-07-24: Added governed referenced SVG graphic body compilation for M34 Story 2.1 and marked ready for review.
