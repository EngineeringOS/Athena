---
baseline_commit: 0311ad67c08779db3d1def271d0ac948a4683128
---

# Story 1.2: Compile A Typed Symbol With Native Geometry

Status: done

## Story

As a Symbol library author,
I want to write a Symbol in ordinary Athena source with native typed geometry,
so that the compiler and IDE can validate it before it enters a package.

## Acceptance Criteria

1. **Given** a standalone representation source containing a valid `symbol` declaration with a typed
   Athena `graphic` body, **when** ANTLR4 parsing, authored-AST lowering, semantic type checking, and
   lint run, **then** exactly one canonical `RepresentationDefinition` with
   `GRAPHIC_PRIMITIVE` authority and one valid `GraphicPrimitiveDocument` are produced without a fake
   `system` declaration or legacy visual body.
2. **Given** the same valid source, **when** tree-sitter parses it and the generated grammar corpus
   runs, **then** declaration, identity/version, graphic/bounds/line/style, anchor/reference/point,
   role, direction, and signal nodes match the ANTLR4 syntax boundary and remain syntax UX only.
3. **Given** missing identity/version/required anchor fields, duplicate primitive or anchor ids,
   unresolved `ref`, non-finite or invalid coordinates/bounds/styles, unsupported geometry,
   or forbidden project/renderer/XML authority, **when** compilation runs, **then** stable
   source-spanned diagnostics are emitted and no definition is admitted.
4. **Given** human- or AI-authored Symbol source, **when** Symbol lint and formatting run twice,
   **then** output and ordered diagnostics are deterministic and formatting is idempotent.
5. **Given** the public M34 grammar addition, **when** its declarations and keywords are audited,
   **then** it adds only `symbol` plus minimum domain-facing nested constructs for identity, version,
   graphic primitives, anchors, and compatibility; it adds no Element/SVG/Profile/Binding,
   descriptor, occurrence, renderer, transport, DOM, XML, or IR declaration.
6. **Given** all previous criteria are green, **when** mandatory polish/purge runs, **then** source,
   tests, grammar outputs, WASM, fixtures, docs, generated output, encoding, and workspace state are
   deeply reviewed; stale or duplicate artifacts are removed; and RED/GREEN, AC-to-evidence, and
   three-layer review are recorded.

**Implements:** FR-1, FR-3, FR-6, FR-9..FR-10, FR-12, FR-41.

## Tasks / Subtasks

- [x] Add the Story 1.2 RED contract before production grammar edits (AC: 1..5)
  - [x] Add one valid standalone Symbol fixture using native `graphic`, explicit bounds, governed
        `line` primitives, explicit anchors, `ref`, and explicit anchor points.
  - [x] Add invalid fixtures for missing fields, duplicate ids, unresolved references, invalid
        numbers/bounds/styles, unsupported constructs, and forbidden authority vocabulary.
  - [x] Prove existing project `system` source still parses and compiles unchanged.
- [x] Extend the public Athena syntax and authored AST narrowly (AC: 1, 3, 5)
  - [x] Allow a source file to contain either one project `system` unit or representation
        declarations; do not synthesize a fake system and do not mix project instances into Symbol.
  - [x] Add source-spanned `SymbolDeclaration`, native graphic bounds/line models, and anchor
        compatibility models to `kernel/language`; use finite typed numbers, not generic expressions.
  - [x] Update every exhaustive `Declaration`/source-unit consumer deliberately and preserve existing
        package/import and project compilation behavior.
- [x] Compile and lint one canonical Symbol (AC: 1, 3, 4)
  - [x] Add a compiler-owned Symbol source compiler/lowerer that resolves a small governed style
        vocabulary and produces `RepresentationDefinition`, never Presentation Primitive or XML.
  - [x] Require explicit anchor point plus `ref`; validate the reference without deriving
        direction, signal, role, or point from geometry.
  - [x] Run `GraphicPrimitiveIrValidator` and representation validation before admission; map all
        failures to stable source-spanned Symbol diagnostics and return no partial definition.
  - [x] Add a Symbol-source formatter/linter with twice-equals-once and reversed-input diagnostic tests.
- [x] Add tree-sitter parity without moving semantic truth into the IDE (AC: 2, 5)
  - [x] Extend `grammar.js`, corpus, incomplete-source tests, and highlights for the exact Symbol subset.
  - [x] Regenerate committed tree-sitter parser artifacts and WASM with repository scripts.
  - [x] Keep full completion/outline/navigation behavior assigned to Story 2.3; only syntax parity and
        basic declaration highlighting are required here.
- [x] Run sequential verification (AC: 1..5)
  - [x] Run focused language/compiler tests, then `:kernel:language:test`,
        `:kernel:representation-model:test`, `:kernel:compiler:test`, and affected LSP tests.
  - [x] Run tree-sitter corpus/generated-artifact checks and existing frontend syntax tests.
  - [x] Run the full Gradle regression only after focused suites are green; never overlap Gradle runs.
- [x] Perform mandatory polish/purge and evidence review (AC: 6)
  - [x] Audit public keywords, AST/model ownership, generated files, fixtures, docs, encoding, diff, and
        dirty-worktree boundaries; remove stale or duplicate Story 1.2 artifacts.
  - [x] Confirm no new XML compatibility, legacy anatomy producer, Presentation Primitive producer,
        descriptor authoring path, raw SVG path, or renderer inference was introduced.
  - [x] Record RED/GREEN, AC-to-evidence, three-layer review, and every touched file.

### Review Findings

- [x] [Review][Patch] Compare zero-length line coordinates by value rather than source-spanned point
  object equality.
- [x] [Review][Patch] Format finite numeric values as grammar-compatible plain decimals rather than
  scientific notation.
- [x] [Review][Patch] Enforce strict SemVer prerelease identifiers while accepting valid build
  metadata.
- [x] [Review][Patch] Include complete source spans in the deterministic diagnostic ordering key.
- [x] [Review][Patch] Reject non-finite canonical anchor points at the representation contract boundary.
- [x] [Review][Patch] Strengthen invalid-source tests to assert complete diagnostic spans and messages.
- [x] [Review][Confirm] Keep source-unit root evolution and package-qualified duplicate identity
  behavior; both match the frozen M34 architecture and acceptance criteria.

## Dev Notes

### Frozen Story Syntax

The valid proof must use this shape or an equivalent formatting-normalized form:

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
```

`ref` identifies the governed geometry target; `point` is explicit and authoritative for
the representation anchor. The compiler must not guess an endpoint from primitive order or ids.

### Non-Negotiable Boundaries

- A file is either a project source unit with one `system`, or a representation source unit. Story
  1.2 supports Symbol declarations only; Story 1.3 adds Element.
- Existing project syntax (`system`, `device`, `port`, `connect`, `layout`) remains public. AC5 limits
  only the new M34 representation vocabulary; it does not remove established language features.
- Symbols with no connectable anchors may be valid. Once an `anchor` is authored, all required anchor
  fields must be present and typed.
- `style conductor` resolves through a compiler-owned, versioned built-in style registry. Unknown
  styles fail; renderer CSS/class names are not accepted as source authority.
- No generic expression language, arbitrary properties, inline SVG/XML, renderer names, DOM terms,
  package snapshot stager, Element composition, Profile, or Binding behavior enters this story.
- XML compatibility is not preserved for product logic. XML parsing later exists only inside the
  safe SVG geometry compiler because SVG itself is XML syntax.

### Existing Code To Reuse

- `Athena.g4`, `AthenaAntlrParseEngine`, and `AthenaAntlrAstAdapter` own parsing and source spans.
- `Declaration` and `SourceFileAst` are the current public AST contracts. Evolve the source-unit root
  explicitly; do not add an unrelated second parser API.
- `GraphicPrimitiveDocument`, `GraphicPrimitive.Line`, `GraphicStyleToken`, and
  `GraphicPrimitiveIrValidator` are canonical; do not duplicate their geometry model in compiler.
- Story 1.1 added `RepresentationBodyAuthority.GRAPHIC_PRIMITIVE` and a non-authoritative presentation
  compatibility shell. Story 1.2 must not author legacy visual primitives.
- Tree-sitter owns syntax UX only. ANTLR4/authored AST/compiler remain semantic authority.

### Testing Requirements

- Capture a genuine RED from the new valid/invalid Symbol fixtures before grammar implementation.
- Assert AST values and source spans, not source-text substring presence alone.
- Assert exact ordered diagnostic code, source file, span, subject, and message fields.
- Prove invalid compile results contain no definition and no package-admission value.
- Prove formatter idempotence and deterministic diagnostics across repeated/reordered inputs.
- Regenerate parser artifacts through package scripts; do not hand-edit `parser.c`, `grammar.json`,
  `node-types.json`, or the WASM binary.
- Run Gradle commands strictly sequentially on Windows.

### References

- [M34 PRD](../../planning-artifacts/prds/prd-Athena-2026-07-24-m34/prd.md)
- [M34 Addendum](../../planning-artifacts/prds/prd-Athena-2026-07-24-m34/addendum.md)
- [M34 Architecture Spine](../../planning-artifacts/architecture/architecture-Athena-2026-07-24-m34/ARCHITECTURE-SPINE.md)
- [M34 Epics](epics.md)
- [Story 1.1](1-1-establish-the-canonical-representation-contract.md)
- `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/GraphicPrimitiveModels.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/GraphicPrimitiveValidation.kt`
- `ide/tree-sitter-athena/grammar.js`

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: focused `AthenaM34SymbolSyntaxTest`, `AthenaSymbolSourceCompilerTest`, and tree-sitter Symbol
  corpus checks failed before production edits because standalone `symbol` syntax, authored models,
  compiler admission, and syntax-UX nodes did not exist.
- GREEN: focused Symbol tests, `:kernel:language:test`, `:kernel:representation-model:test`,
  `:kernel:compiler:test`, and `:ide:lsp:test` passed in sequential Gradle runs. Tree-sitter `yarn
  test` passed all 50 corpus/generated checks and `yarn build` regenerated the committed parser and
  WASM. The post-review full Gradle `test` run passed 151 tasks with no failures in 2m58s.

### Completion Notes List

- Story context created from M34 PRD/addendum, architecture, epics, Story 1.1 review lessons,
  CodeGraph analysis, and parallel BMAD artifact/codebase review.
- Latest product direction overrides legacy XML-fixture compatibility: no XML product/runtime logic is
  preserved; safe SVG parsing remains a later compiler-only concern.
- Added the minimum typed `symbol` language surface, explicit project/representation source-unit split,
  native line geometry, explicit anchors, compatibility predicates, and complete source spans without
  introducing Element, SVG, Profile, Binding, renderer, transport, or XML vocabulary.
- Added compiler-owned lowering, governed style resolution, strict validation, canonical admission,
  deterministic lint/format behavior, and typed rejection at the generic project compiler boundary.
- Added ANTLR4/tree-sitter parity, generated parser/WASM artifacts, syntax highlighting, corpus,
  malformed-number/string checks, and project-source regression coverage. Tree-sitter remains syntax
  UX only.
- AC-to-evidence: AC1 is covered by the valid language/compiler tests and canonical definition
  assertions; AC2 by tree-sitter corpus, highlights, generated parser, and WASM checks; AC3 by exact
  invalid-source diagnostics and no-partial-definition assertions; AC4 by repeat/reversed-input and
  formatter idempotence tests; AC5 by grammar vocabulary audit and forbidden-authority tests; AC6 by
  the sequential full regression, generated-output review, encoding/diff audit, and this evidence.
- Three-layer review: blind source review found coordinate-equality and formatter defects; edge-case
  review found SemVer, non-finite anchor, and diagnostic-ordering gaps; acceptance audit required exact
  span/message evidence. All six findings were fixed and covered by regression tests.
- Polish/purge confirmed no new XML compatibility, legacy anatomy or Presentation Primitive producer,
  independently authored descriptor, raw SVG path, or renderer inference. Unrelated dirty M32/M33
  work remains untouched.

### File List

- `_bmad-output/implementation-artifacts/m34/1-2-compile-a-typed-symbol-with-native-geometry.md`
- `_bmad-output/implementation-artifacts/m34/sprint-status.yaml`
- `ide/tree-sitter-athena/grammar.js`
- `ide/tree-sitter-athena/queries/highlights.scm`
- `ide/tree-sitter-athena/scripts/athena-tree-sitter-grammar-corpus.test.mjs`
- `ide/tree-sitter-athena/scripts/athena-tree-sitter-incomplete-source.test.mjs`
- `ide/tree-sitter-athena/scripts/athena-tree-sitter-symbol-highlights.test.mjs`
- `ide/tree-sitter-athena/src/grammar.json`
- `ide/tree-sitter-athena/src/node-types.json`
- `ide/tree-sitter-athena/src/parser.c`
- `ide/tree-sitter-athena/test/corpus/symbol.txt`
- `ide/tree-sitter-athena/test/fixtures/m34-symbol.athena`
- `ide/tree-sitter-athena/test/incomplete/unclosed-symbol-anchor.athena.txt`
- `ide/tree-sitter-athena/tree-sitter-athena.wasm`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompilerCompilationSupport.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSymbolGraphicStyles.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSymbolSourceCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSymbolSourceFormatter.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSymbolSourceLowerer.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSymbolSourceModels.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSymbolSourceValidator.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaSymbolSourceCompilerTest.kt`
- `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaM34SymbolSyntaxTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/LanguageFacadeBoundaryTest.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/GraphicPrimitiveModels.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/GraphicPrimitiveValidation.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/RepresentationDefinitionModels.kt`

## Change Log

- 2026-07-24: Added typed native Symbol syntax, compilation, validation, formatting, ANTLR4/tree-sitter
  parity, generated parser artifacts, and sequential regression evidence.
- 2026-07-24: Addressed six three-layer review findings covering geometry equality, numeric formatting,
  SemVer, diagnostics, finite anchors, and exact evidence; all regressions passed and the story moved
  to done.
