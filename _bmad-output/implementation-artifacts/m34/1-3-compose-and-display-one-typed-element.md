---
baseline_commit: 0311ad67c08779db3d1def271d0ac948a4683128
---

# Story 1.3: Compose And Display One Typed Element

Status: review

## Story

As a Cabinet library author,
I want to compose Symbols into one typed Element and display it in Cabinet,
so that Athena proves a reusable visual component end to end.

## Acceptance Criteria

1. **Given** explicit positive intrinsic bounds, valid same-library child Symbols, unique child ids
   and integer z-orders, explicit finite child transforms, and explicit exported child-anchor mappings,
   **when** one `element` declaration
   passes ANTLR4 parsing, authored-AST lowering, semantic validation, lint, and compilation, **then**
   exactly one canonical `RepresentationDefinition` of kind `ELEMENT` is produced with deterministic
   `RepresentationIntrinsicComposition`, transformed exported anchors, and one renderer-valid
   `GraphicPrimitiveDocument` using `GRAPHIC_PRIMITIVE` authority.
2. **Given** a missing or non-Symbol child, a nested Element, direct or indirect composition cycle,
   duplicate child id or z-order, missing/duplicate/invalid transform or z-order, style/id collision,
   missing child anchor, duplicate export, or unexported connectable child anchor, **when** lint and
   compilation run, **then** stable fully source-spanned diagnostics are emitted and no Element or
   partial package-admission value is produced.
3. **Given** the Epic 1 M34 Cabinet fixture, **when** its Athena representation library compiles and
   the resulting Element passes an explicit proof-only policy through the existing
   `RepresentationBindingCompiler`, drawing-composition, Graphic Primitive, and SVG renderer path,
   **then** the composed Element is visibly nonblank, normal chrome is transparent, document bounds
   are derived, and structured proof identifies the source definition, children, transforms, z-order,
   occurrence, exported anchors, and renderer authority.
4. **Given** human- or AI-authored mixed Symbol/Element representation sources in different input
   orders, **when** compile, lint, and format run repeatedly, **then** definition order, composition,
   transformed bounds/anchors, diagnostics, and canonical formatting are deterministic and formatting
   is idempotent.
5. **Given** the Story 1.3 public grammar addition, **when** ANTLR4/tree-sitter parity and vocabulary
   are audited, **then** it adds only `element` plus the minimum domain-facing child, transform,
   z-order, and anchor-export constructs; it adds no SVG/Profile/Binding, descriptor, occurrence,
   renderer, transport, DOM, XML, package-resolution, or engineering-component declaration.
6. **Given** all previous criteria are green, **when** mandatory polish/purge runs, **then** source,
   tests, grammar outputs, WASM, fixtures, proof artifacts, docs, generated output, encoding, and
   workspace state are deeply reviewed; stale or duplicate artifacts are removed; and RED/GREEN,
   AC-to-evidence, and three-layer review are recorded.

**Implements:** FR-2..FR-3, FR-9, FR-14, FR-30, FR-41..FR-42; NFR-3..NFR-4,
NFR-7..NFR-9, NFR-12.

## Tasks / Subtasks

- [x] Add Story 1.3 RED contracts before production edits (AC: 1..5)
  - [x] Add one mixed Symbol/Element fixture using two child occurrences, explicit identity transforms,
        distinct z-orders, and exports for every connectable child anchor.
  - [x] Add invalid compiler fixtures for every AC2 failure class and assert exact diagnostic code,
        file, complete span, subject, message, and zero admitted definitions.
  - [x] Add a failing end-to-end proof test that requires one compiled Element to bind, compose, and
        render through the existing typed Cabinet stack with no XML or legacy visual producer.
- [x] Extend Athena syntax and authored AST with the frozen Element subset (AC: 1, 2, 5)
  - [x] Allow a representation source unit to contain Symbols and Elements while keeping project
        `system` source disjoint and rejecting mixed project/representation roots.
  - [x] Add source-spanned Element bounds, child, transform, z-order, and exported-anchor models; use typed
        finite numbers and bounded integers rather than generic expressions or property maps.
  - [x] Update exhaustive declaration/source-unit consumers and preserve existing project and Symbol
        parsing/compilation behavior.
- [x] Evolve one representation-source compiler instead of adding a second authority (AC: 1, 2, 4)
  - [x] Refactor the Story 1.2 Symbol-only pipeline into one `AthenaRepresentationSourceCompiler`
        compile/lint/format pipeline for Symbols and Elements; remove the old Symbol-only compiler
        name and duplicate result models because no product caller requires compatibility.
  - [x] Resolve child identity by `(libraryId, identity)` from the same compilation batch, compile
        Symbols before Elements, and reject unresolved/cross-library/nested Element targets without
        filename or input-order fallback.
  - [x] Reject duplicate identity across Symbol and Element definitions within one library and return
        no definitions when any batch diagnostic exists.
- [x] Lower and validate deterministic intrinsic composition (AC: 1, 2, 4)
  - [x] Require explicit positive Element bounds; normalize each child transform to scale-about-origin,
        rotate-about-origin, then translate; namespace child primitive ids, derive transformed child
        bounds, and merge identical governed style tokens without renderer interpretation.
  - [x] Store child id, canonical Symbol identity, normalized transforms, and z-order in
        `RepresentationIntrinsicComposition`; sort output by z-order then child id.
  - [x] Map every exported Element anchor to exactly one child anchor, transform its explicit
        Athena-authored point, retain its role/direction/signal compatibility, and reject missing,
        repeated, or unexported connectable anchors.
  - [x] Extend canonical validation for empty Elements, duplicate z-order, child resolution/kind,
        cycles, export integrity, children outside Element bounds, ids, styles, and renderer-valid
        Graphic Primitive output. Do not create an Element IR beside `RepresentationDefinition`.
- [x] Add the Epic 1 Cabinet-visible proof without premature product authority (AC: 3)
  - [x] Add an `examples/m34` Epic 1 fixture with ordinary Athena Symbol/Element source and no XML,
        SVG, Profile, Binding, or duplicated project-port metadata.
  - [x] Use an explicit test-owned policy and semantic subject only to exercise the existing
        `RepresentationBindingCompiler`; do not add a production selector before Story 3.1.
  - [x] Pass the compiled Element body through existing drawing-composition,
        `GraphicPrimitiveSvgAdapter`, and `GraphicPrimitiveSvgCanvasComposer`; assert nonblank output,
        derived bounds, transparent normal chrome, occurrence/anchor/source proof, and no fallback.
  - [x] Do not extend `M33CabinetPackageSet`, `M33CabinetPresentationFactDeriver`, the M33 Kotlin
        catalog service, Presentation Primitive conversion, direct markup transport, or frontend
        inference. Full active Electron package binding belongs to Stories 3.1..3.4 and 4.3.
- [x] Add tree-sitter parity for the exact Element subset (AC: 4, 5)
  - [x] Extend grammar, corpus, incomplete-source fixtures, and basic declaration highlighting.
  - [x] Add ANTLR4/tree-sitter acceptance and rejection parity for numbers, strings, child references,
        transforms, integer z-order, and exported anchors.
  - [x] Regenerate committed grammar JSON, node types, parser C, and WASM through repository scripts;
        leave completion, outline, navigation, and full semantic tokens to Story 2.3.
- [x] Run sequential verification (AC: 1..5)
  - [x] Run focused language/compiler/representation/renderer proof tests, then affected module tests.
  - [x] Run tree-sitter corpus, incomplete-source, highlight, generated-artifact, and WASM checks.
  - [x] Run the full Gradle `test` task only after focused suites pass; never overlap Gradle runs.
- [x] Perform mandatory deep polish/purge and evidence review (AC: 6)
  - [x] Audit public vocabulary, source/compiler/model ownership, ids/transforms/bounds, generated files,
        fixtures, proof artifacts, docs, encoding, diff, and dirty-worktree boundaries.
  - [x] Remove stale Symbol-only compiler duplication and any temporary selector, renderer, legacy
        Presentation Primitive producer, XML compatibility, raw markup, or fake package authority.
  - [x] Record RED/GREEN, AC-to-evidence, independent blind/edge/acceptance reviews, review
        dispositions, and every touched file before changing the story to done.

## Dev Notes

### Frozen Story Syntax

The valid proof uses this shape or its formatter-normalized equivalent:

```athena
package athena.iec

symbol iec_switch_contact {
  identity "iec.switch_contact"
  version "1.0.0"

  graphic {
    bounds (0, 0, 80, 80)
    line line from (40, 0) to (40, 20) style conductor
    line load from (40, 60) to (40, 80) style conductor
  }

  anchor line {
    ref line
    point (40, 0)
    role terminal
    direction in
    signal Power
  }

  anchor load {
    ref load
    point (40, 80)
    role terminal
    direction out
    signal Power
  }
}

element iec_switch_module {
  identity "iec.switch_module"
  version "1.0.0"
  bounds (0, 0, 180, 80)

  child primary {
    symbol "iec.switch_contact"
    translate (0, 0)
    rotate 0
    scale (1, 1)
    zOrder 0
  }

  child auxiliary {
    symbol "iec.switch_contact"
    translate (100, 0)
    rotate 0
    scale (1, 1)
    zOrder 1
  }

  export anchor primaryLine from primary.line
  export anchor primaryLoad from primary.load
  export anchor auxiliaryLine from auxiliary.line
  export anchor auxiliaryLoad from auxiliary.load
}
```

Element identity is separate from declaration name. Element `bounds` is the explicit reusable local
coordinate boundary; every transformed child must remain inside it. `child.symbol` resolves the authored Symbol
`identity` within the Element's library/package, not a filename, declaration name, descriptor, or
renderer resource. Element V1 requires exactly one translate, rotate, scale, and z-order per child.
Scale components are finite and positive; z-order is a signed 32-bit integer. Semantic transform
order is scale about `(0,0)`, rotate about `(0,0)`, then translate, independent of member order; the
formatter emits the frozen order shown above.

Resolution is two-phase and input-order independent: index all Symbol and Element identities first,
compile Symbols, then compile Elements. A child edge targeting an Element emits a nested-Element
diagnostic at that reference. Each self-cycle or strongly connected Element component also emits one
cycle diagnostic per participating Element declaration; both diagnostics are retained and sorted by
the complete source span.

An exported anchor derives its local point and compatibility contract only from the referenced
Athena child-anchor contract plus the normalized child transform. That is typed Athena-to-Athena
derivation, not geometry inference. Every connectable child anchor must be exported exactly once in
Element V1. Intrinsic child placement belongs to Element; Cabinet occurrence placement, sheet/frame,
rails, lanes, routing, and document bounds remain spatial/drawing-composition authority.

### Non-Negotiable Boundaries

- `RepresentationDefinition` remains the only compiled Symbol/Element definition. Do not add
  `ElementDefinition`, an Element scene model, an SVG IR, or another composition authority.
- One representation-source compiler parses, validates, formats, resolves, and lowers mixed
  Symbol/Element batches. Do not copy the Story 1.2 compiler into an Element compiler.
- Symbol remains atomic. Element V1 references Symbols only; Element-to-Element nesting and cycles
  fail semantically even if enough syntax is present to diagnose the attempted reference.
- Story 1.3 Elements contain one or more Symbol children and no Element-owned `graphic` body. Native
  Element-local geometry can be added only when a later accepted story proves it is required; this
  story does not broaden syntax beyond its composition proof.
- Element owns reusable visual composition only. It cannot own project devices, actual project ports,
  connections, classification, authored Cabinet placement, profile policy, binding policy, or vendor
  engineering parameters.
- No XML compatibility is required because the product is not public. XML may appear later only
  inside the isolated governed annotated-SVG compiler frontend in Story 2.1. Story 1.3 adds no XML.
- No raw SVG, annotated SVG metadata, Profile, Binding, package snapshot, network fetch, QET importer,
  LSP transport expansion, or full Electron product binding enters this story.
- Story 1.3 proves the `NATIVE_ATHENA` simple-representation path. The approved complex path is one
  Athena declaration with `graphic svg "..."` referencing a governed SVG resource using
  `data-athena-schema="representation/v1"`; Story 2.1 owns it. Native graphic bodies and referenced
  SVG graphic bodies may never merge one definition and both must reuse canonical admission.
- The renderer consumes only validated `GraphicPrimitiveDocument`. It must not resolve children,
  calculate anchor meaning, choose a Symbol, infer bounds, or inspect source.

### Existing Code To Extend

- `Athena.g4` currently admits either one `system` or one-or-more Symbols. Extend that representation
  branch with Element declarations; do not weaken the project/representation source-unit split.
- `AthenaLanguageModel.kt` and `AthenaAntlrParseAdapter.kt` own source-spanned authored models and
  lowering. Add cohesive Element types beside the existing Symbol types and update public facade tests.
- `AthenaSymbolSourceCompiler`, lowerer, validator, formatter, styles, and models are the Story 1.2
  pipeline. Refactor toward one representation compiler rather than layering a competing pipeline.
- `RepresentationDefinitionModels.kt` already owns `RepresentationCompositionChild`,
  `RepresentationExportedAnchor`, and `RepresentationIntrinsicComposition`. Extend these canonical
  contracts only where child-resolution provenance or validation evidence is actually required.
- `RepresentationContractValidator` currently checks duplicate child ids only. Story 1.3 owns the
  complete intrinsic-composition validation described in AC2.
- `GraphicPrimitive.Group`, `GraphicPrimitive.Transformed`, `GraphicTransform`,
  `GraphicPrimitiveIrValidator`, `GraphicPrimitiveSvgAdapter`, and the canvas composer already support
  typed grouping/transform rendering. Reuse them; do not emit handcrafted SVG.
- `RepresentationBindingCompiler` can construct the explicit proof occurrence. `BindingResolver` and
  typed Profile/Binding compilation remain Story 3.1.
- `DrawingSheetCompositionCompiler` derives sheet/frame/document bounds. Reuse it for proof rather
  than hard-coding a document viewBox.
- `M33CabinetPresentationFactDeriver` still reads `M33CabinetPackageSet` XML and converts Graphic
  Primitive back to Presentation Primitive. Do not extend it. Stories 3.3/3.4 remove that authority.

### Testing Requirements

- Capture genuine RED for parser/AST/compiler, invalid composition, deterministic formatting, and the
  end-to-end Cabinet proof before implementation.
- Assert authored AST values and complete source spans, not source substrings alone.
- Assert exact ordered diagnostics and zero definitions for each invalid batch. Reversing files and
  declarations must not alter diagnostics or output.
- Test same Symbol used by multiple children, primitive-id namespacing, transform order, transformed
  bounds/anchor points, style-token deduplication/conflict, and z-order rendering order.
- Test unresolved child, cross-library child, Symbol/Element identity collision, nested Element,
  direct/indirect cycle, duplicate child id/z-order, missing/duplicate transform member, zero/negative
  scale, non-integer/out-of-range z-order, empty Element, transformed child outside explicit bounds,
  missing child anchor, duplicate export, and unexported connectable anchor.
- Prove existing standalone Symbol and project `system` sources compile unchanged.
- Prove the proof SVG is nonblank, contains the compiled/namespaced child primitive ids in z-order,
  reports `normalChromeVisible=false`, uses derived content/document bounds, and has no fallback/XML/
  raw-markup authority in its structured evidence.
- Regenerate tree-sitter outputs only through package scripts. Run Gradle tasks strictly sequentially.

### Previous Story Intelligence

- Story 1.2 established the project/representation source-unit split, canonical Symbol grammar,
  compiler-owned style registry, strict SemVer, plain-decimal formatter, complete-span diagnostic sort,
  no-partial-definition admission, and ANTLR4/tree-sitter parity.
- Review defects fixed in Story 1.2 must remain guarded: coordinate equality cannot include source
  spans, formatter output must remain grammar-compatible, SemVer must remain strict, non-finite points
  are rejected at canonical boundaries, and diagnostics sort by complete span.
- Duplicate identity is library-qualified. Story 1.3 extends collision checks across Symbol and Element
  kinds within that same qualified namespace.
- Story 1.2 intentionally did not add Element, SVG, Profile, Binding, full LSP features, renderer
  inference, or XML compatibility. Preserve that sequencing.

### Git And Workspace Intelligence

- Baseline commit is `0311ad6` (`feat(m32): add engineering package platform`); M33 and M34 work is
  intentionally uncommitted in the current dirty worktree.
- Preserve all unrelated M32/M33 changes. Do not reset, checkout, rewrite screenshots, or absorb
  unrelated files into Story 1.3 evidence.
- Recent milestone code groups cohesive Kotlin models/support types and keeps Gradle verification
  sequential on Windows. Follow `AGENTS.md` Kotlin organization and UTF-8 rules.
- No new external library is required; use existing Kotlin, ANTLR4, tree-sitter, representation,
  drawing-composition, and SVG renderer contracts. Web version research is not applicable.

### References

- [M34 PRD](../../planning-artifacts/prds/prd-Athena-2026-07-24-m34/prd.md)
- [M34 Addendum](../../planning-artifacts/prds/prd-Athena-2026-07-24-m34/addendum.md)
- [M34 Architecture Spine](../../planning-artifacts/architecture/architecture-Athena-2026-07-24-m34/ARCHITECTURE-SPINE.md)
- [M34 Annotated SVG Course Correction](../../planning-artifacts/sprint-change-proposal-2026-07-24-m34-annotated-svg-authority.md)
- [M34 Epics](epics.md)
- [Story 1.2](1-2-compile-a-typed-symbol-with-native-geometry.md)
- `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSymbolSourceCompiler.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/RepresentationDefinitionModels.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/RepresentationValidation.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/GraphicPrimitiveModels.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/GraphicPrimitiveValidation.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/RepresentationBindingCompiler.kt`
- `kernel/drawing-composition/src/main/kotlin/com/engineeringood/athena/drawing/composition/DrawingSheetCompositionCompiler.kt`
- `kernel/svg-renderer/src/main/kotlin/com/engineeringood/athena/renderer/svg/GraphicPrimitiveSvgAdapter.kt`
- `kernel/svg-renderer/src/main/kotlin/com/engineeringood/athena/renderer/svg/GraphicPrimitiveSvgCanvasComposer.kt`

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: `yarn test` in `ide/tree-sitter-athena` failed after adding M34 Element syntax fixtures because
  `element_declaration`, `element_child`, and Element highlight captures did not exist in the checked-in
  tree-sitter grammar/WASM.
- GREEN: focused compiler tests passed before and after validator split; tree-sitter `yarn build` and
  `yarn test` passed with 14 corpus parses and 51 node tests; `:kernel:language:test`,
  `:kernel:representation-model:test`, `:kernel:compiler:test`, full Gradle `test`, and encoding audit passed.

### Completion Notes List

- Ultimate context engine analysis completed from M34 PRD/addendum, architecture, complete epics,
  completed Story 1.2 evidence, CodeGraph call-path analysis, current worktree state, and independent
  architecture/product/edge-case review.
- Latest product direction is binding: remove legacy XML product/runtime logic rather than preserving
  compatibility; XML exists only inside the later governed annotated-SVG compiler frontend.
- Approved course correction keeps this story native and simple while assigning complex one-file
  `data-athena-*` representation authoring to Story 2.1; no sidecar or duplicate authority is added.
- Course correction was tightened again after review: Athena declarations own definition
  identity/version/kind; complex graphics use `graphic svg "..."` to reference one governed SVG
  geometry resource whose `data-athena-*` scope is node-local anchors/slots/hotspots only.
- Added Element tree-sitter parity, corpus, incomplete-source recovery, highlight coverage, generated
  grammar artifacts, and WASM rebuild.
- Split the oversized Element validator into basic, reference/export/bounds, cycle, and support files
  while preserving diagnostics and canonical ordering.
- Added `examples/m34/sample-project` with repository contract, project source, and a native
  Symbol/Element representation package; compiler test now proves the sample package compiles without
  XML authority.
- AC-to-evidence: AC1/AC4 covered by `AthenaElementSourceCompilerTest`; AC2 by invalid composition
  diagnostics; AC3 by `AthenaM34ElementCabinetProofTest`; AC5 by tree-sitter corpus/highlight/parity
  tests and forbidden SVG/Profile/Binding syntax checks; AC6 by validator split, stale-doc correction,
  encoding audit, and full regression.
- Three-layer review: blind review found stale standalone-SVG authority text and an oversized validator;
  edge-case review found old WASM could mask grammar changes and Gradle test cwd could break example
  loading; acceptance audit required example compile proof, tree-sitter corpus coverage, full Gradle
  regression, and encoding audit. All findings were addressed.

### File List

- `_bmad-output/implementation-artifacts/m34/1-3-compose-and-display-one-typed-element.md`
- `_bmad-output/implementation-artifacts/m34/epics.md`
- `_bmad-output/implementation-artifacts/m34/sprint-status.yaml`
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-24-m34/ARCHITECTURE-SPINE.md`
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-24-m34/addendum.md`
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-24-m34/prd.md`
- `_bmad-output/planning-artifacts/sprint-change-proposal-2026-07-24-m34-annotated-svg-authority.md`
- `examples/m34/sample-project/README.md`
- `examples/m34/sample-project/athena.lock`
- `examples/m34/sample-project/athena.yaml`
- `examples/m34/sample-project/packages/representation/athena/iec/epic1-native-elements.athena`
- `examples/m34/sample-project/src/01-native-cabinet-proof.athena`
- `ide/tree-sitter-athena/grammar.js`
- `ide/tree-sitter-athena/queries/highlights.scm`
- `ide/tree-sitter-athena/scripts/athena-tree-sitter-grammar-corpus.test.mjs`
- `ide/tree-sitter-athena/scripts/athena-tree-sitter-incomplete-source.test.mjs`
- `ide/tree-sitter-athena/scripts/athena-tree-sitter-symbol-highlights.test.mjs`
- `ide/tree-sitter-athena/src/grammar.json`
- `ide/tree-sitter-athena/src/node-types.json`
- `ide/tree-sitter-athena/src/parser.c`
- `ide/tree-sitter-athena/test/corpus/symbol.txt`
- `ide/tree-sitter-athena/test/fixtures/m34-element.athena`
- `ide/tree-sitter-athena/test/fixtures/m34-symbol.athena`
- `ide/tree-sitter-athena/test/incomplete/unclosed-element-child.athena.txt`
- `ide/tree-sitter-athena/test/incomplete/unclosed-symbol-anchor.athena.txt`
- `ide/tree-sitter-athena/tree-sitter-athena.wasm`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaElementBasicValidator.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaElementCycleValidator.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaElementReferenceValidator.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaElementSourceLowerer.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaElementSourceValidator.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaElementValidationSupport.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationSourceCompiler.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaElementSourceCompilerTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/LanguageFacadeBoundaryTest.kt`

## Change Log

- 2026-07-24: Completed Story 1.3 Element composition, Cabinet proof coverage, M34 sample fixture,
  tree-sitter parity, validator split, SVG authority correction, and full sequential regression.
