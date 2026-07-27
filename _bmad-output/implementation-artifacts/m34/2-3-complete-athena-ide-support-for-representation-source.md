---
baseline_commit: 0311ad67c08779db3d1def271d0ac948a4683128
---

# Story 2.3: Complete Athena IDE Support For Representation Source

Status: review

## Story

As a human or AI representation author,
I want full IDE/compiler feedback for native Athena representation declarations and referenced governed SVG resources,
so that generated engineering materials can be corrected before package admission or Cabinet projection.

## Acceptance Criteria

1. **Given** valid and invalid native Athena representation source containing `symbol`, `element`,
   native `graphic { ... }`, `graphic svg "./asset.svg"`, child declarations, exported anchors,
   labels, and connectable anchor predicates, **when** ANTLR4 and tree-sitter parse it, **then** parser
   parity fixtures agree on accepted/rejected cases and preserve stable node names for IDE features.
2. **Given** an open representation `.athena` file, **when** LSP/IDE services run, **then** outline,
   semantic token highlighting, completion, formatter, lint diagnostics, and go-to identity cover
   packages, Symbols, Elements, children, anchors, labels, graphic bodies, `svg` graphic references,
   and package-local resource paths.
3. **Given** an open governed SVG resource referenced by Athena source, **when** compiler/IDE feedback
   runs, **then** diagnostics recognize only the compiler-owned `data-athena-schema="representation/v1"`
   and node-local `data-athena-*` profile; no XSD, frontend-only schema, SVG id inference, CSS
   inference, or raw DOM authority is introduced.
4. **Given** completion, highlighting, outline, grammar fixtures, and AI-generated corpus cases,
   **when** determinism tests run repeatedly, **then** accepted/rejected cases, source spans, canonical
   formatting, and generated tree-sitter artifacts are stable across clean builds.
5. **Given** compiler/runtime implementation type names such as descriptor, occurrence, renderer,
   transport, DOM, XML, package snapshot, and Graphic Primitive, **when** completion, highlighting,
   outline, and grammar fixtures run, **then** those names are not offered or accepted as Athena
   source declarations.
6. **Given** all previous criteria are green, **when** mandatory polish/purge runs, **then** source,
   tests, fixtures, docs, generated outputs, and workspace state are deeply reviewed; stale/duplicate
   artifacts are removed; and RED/GREEN, AC-to-evidence, and three-layer review are recorded.

**Implements:** FR-9..FR-12, FR-41; NFR-12.

## Tasks / Subtasks

- [x] Add Story 2.3 RED contracts before production edits (AC: 1..5)
  - [x] Add tree-sitter corpus/highlight/incomplete-source RED fixtures for mixed Symbol/Element
        representation source and `graphic svg`.
  - [x] Add LSP RED tests for outline, semantic tokens, completion, formatting, lint diagnostics, and
        go-to identity over representation declarations.
  - [x] Add governed SVG RED diagnostics/completion schema tests that require compiler-owned
        `data-athena-*` vocabulary only.
  - [x] Add negative vocabulary tests proving compiler/runtime implementation names are not accepted or
        offered as source declarations.
- [x] Complete parser and tree-sitter parity for the M34 representation subset (AC: 1, 4, 5)
  - [x] Keep ANTLR4 as compiler grammar authority and tree-sitter as editor grammar parity.
  - [x] Regenerate committed grammar JSON, node types, parser C, and WASM only through repository
        scripts.
  - [x] Keep public vocabulary domain-facing: `symbol`, `element`, `profile`, `binding`, `graphic`,
        `svg`, anchors, labels, children, transforms, selectors, and variants only where already
        planned.
- [x] Implement representation-source LSP diagnostics and formatter integration (AC: 2, 4)
  - [x] Route representation files through `AthenaRepresentationSourceCompiler.lint` and formatter
        without treating them as project `system` source.
  - [x] Publish stable source-spanned diagnostics for syntax, invalid package hierarchy, invalid
        anchors, invalid SVG references, and duplicate identities.
  - [x] Ensure formatting is deterministic and idempotent for native and `graphic svg` representation
        source.
- [x] Implement outline, completion, semantic tokens, and go-to identity (AC: 2, 5)
  - [x] Add outline entries for package, Symbol, Element, child, anchor, label slot, and graphic body.
  - [x] Add completions for legal representation keywords and values without exposing compiler/runtime
        model names.
  - [x] Add semantic token families for declaration keywords, graphic-body keywords, anchor predicates,
        string/resource paths, ids, numbers, and arrows/operators where applicable.
  - [x] Add go-to identity from Element child Symbol references and exported child anchors to their
        owning definitions in the same representation package.
- [x] Add governed SVG editor feedback without second schema authority (AC: 3)
  - [x] Reuse compiler-owned SVG metadata vocabulary from `AthenaSvgGraphicBodySupport`; do not add XSD,
        frontend schema files, or independent SVG metadata validators.
  - [x] Surface stable diagnostics for unsupported `data-athena-*`, forbidden root metadata, invalid
        points, invalid role/direction/signal, duplicate ids/contracts, and unsafe SVG content.
  - [x] Keep raw SVG markup out of LSP/Electron transport payloads; editor feedback may use file text
        locally only for diagnostics.
- [x] Run sequential verification (AC: 1..5)
  - [x] Run focused language/compiler/LSP/tree-sitter tests.
  - [x] Run `yarn build` and `yarn test` in `ide/tree-sitter-athena` if grammar artifacts change.
  - [x] Run affected Gradle module tests and full Gradle `test` after focused suites pass; never overlap
        Gradle runs.
  - [x] Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after text/doc edits.
- [x] Perform mandatory deep polish/purge and evidence review (AC: 6)
  - [x] Audit grammar vocabulary, LSP boundaries, source/compiler/editor ownership, generated grammar
        outputs, fixtures, docs, encoding, diff, and dirty-worktree boundaries.
  - [x] Remove stale duplicate grammar fixtures, frontend-only schemas, raw markup transport, XML
        compatibility, renderer inference, and generated artifacts not meant for source control.
  - [x] Record RED/GREEN, AC-to-evidence, independent blind/edge/acceptance reviews, review
        dispositions, and every touched file before changing the story to review.

## Dev Notes

### Scope Boundary

This story is IDE/compiler feedback only. It does not create new runtime package authority, renderer
behavior, Cabinet visual polish, Profile/Binding production selection, QET import, AI authoring, or a
symbol editor. The active product surface remains Cabinet, but this story improves authoring feedback
for the representation materials that later feed Cabinet.

### Non-Negotiable Boundaries

- ANTLR4 compiler parsing remains the authority. Tree-sitter must match it for editor parsing, not
  define a different language.
- Athena representation source remains the only reusable metadata authority. SVG annotations are
  compiler-owned node-local contracts only.
- Do not add XSD, JSON schema, frontend schema, XML manifest, or standalone SVG metadata authority.
- Do not expose compiler/runtime names as user language vocabulary.
- LSP/Electron payloads must not carry raw SVG/HTML markup.
- Keep Gradle verification sequential.

### Existing Code To Extend

- `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationSourceCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSymbolSourceFormatter.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSvgGraphicBodySupport.kt`
- `ide/tree-sitter-athena/grammar.js`
- `ide/tree-sitter-athena/queries/highlights.scm`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationSessionProtocol.kt`

### Previous Story Intelligence

- Story 1.2 and 1.3 already added native Symbol/Element grammar, compiler lowering, formatter, and
  tree-sitter parity. Reuse those fixtures and generated-artifact scripts.
- Story 2.1 added `graphic svg "./asset.svg"` and compiler-owned SVG diagnostics. Do not add a
  competing SVG editor schema.
- Story 2.2 added repository-declared package roots and package-local SVG staging. IDE resource
  completions/diagnostics should respect package-local paths.
- Current workspace is intentionally dirty with M33/M34 work. Preserve unrelated changes.

### References

- [M34 PRD](../../planning-artifacts/prds/prd-Athena-2026-07-24-m34/prd.md)
- [M34 Architecture Spine](../../planning-artifacts/architecture/architecture-Athena-2026-07-24-m34/ARCHITECTURE-SPINE.md)
- [M34 Epics](epics.md)
- [Story 2.1](2-1-compile-a-referenced-governed-svg-graphic-body.md)
- [Story 2.2](2-2-stage-external-assets-into-an-immutable-package-snapshot.md)

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: focused LSP coverage failed before representation package roots were routed as representation
  documents instead of project `system` source; repository-contract validation rejected declared
  representation roots under the workspace.
- RED: focused LSP coverage failed before formatting, semantic token advertisement, outline,
  completion, go-to identity, and representation diagnostics were wired through
  `AthenaLanguageServer` and `AthenaLanguageFeatures`.
- RED: governed SVG editor diagnostics failed before `.svg` package-resource routing and compiler-owned
  `AthenaRepresentationSourceCompiler.lintSvg` exposed the existing `data-athena-*` validator.
- GREEN: `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests "com.engineeringood.athena.compiler.repository.AthenaRepositoryContractLoaderTest.allows representation package sources under declared package roots"` passed.
- GREEN: `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests "com.engineeringood.athena.ide.lsp.AthenaRepresentationSourceLspSupportTest"` passed.
- GREEN: `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test --tests "com.engineeringood.athena.packageruntime.RepresentationPackageSnapshotStagerTest"` passed after replacing the inline path-mutation fixture with an explicit invalid source.
- GREEN: `yarn build` in `ide/tree-sitter-athena` passed and rebuilt `tree-sitter-athena.wasm`.
- GREEN: `yarn test` in `ide/tree-sitter-athena` passed with 15 corpus parses and 57 node/script tests.
- GREEN: `.\gradlew.bat --no-daemon --console=plain test` passed with 151 actionable tasks.

### Completion Notes List

- Ultimate context engine analysis completed from M34 PRD, architecture spine, epics, Story 2.1,
  Story 2.2, sprint status, and current tree-sitter/LSP ownership boundaries.
- Added representation-source LSP routing for declared representation package roots and package-local
  SVG resources without classifying those files as project source.
- Added outline, completions, formatter, diagnostics, semantic tokens, and go-to identity for
  `symbol`, `element`, `graphic`, `graphic svg`, child references, exported anchors, and resource paths.
- Reused compiler-owned governed SVG diagnostics; no XSD, frontend schema, raw SVG transport, XML
  authority, or renderer inference was introduced.
- Added tree-sitter corpus, highlight, incomplete-source, generated artifact, and forbidden-vocabulary
  coverage for the M34 representation subset.
- Preserved Java-style package/resource hierarchy: package-local resources live beside or below their
  owning `.athena` source, and escape paths are an explicit invalid fixture rather than a mutated
  valid-source shortcut.
- AC-to-evidence: AC1 covered by tree-sitter corpus/fixture parity and `AthenaRepresentationSourceLspSupportTest`;
  AC2 by LSP outline/completion/formatting/semantic-token/go-to tests; AC3 by `.svg` diagnostics
  through `lintSvg`; AC4 by deterministic formatter and regenerated tree-sitter artifact checks; AC5
  by forbidden runtime/compiler vocabulary tests; AC6 by polish/purge review, encoding audit, and full
  sequential regression.
- Three-layer review: blind boundary review checked that representation source remains Athena-owned
  and frontend/schema-free; edge-case review checked package-local SVG routing, root exclusion, escape
  paths, and invalid SVG diagnostics; acceptance review checked every AC against tests and no raw SVG
  or XML authority entered the LSP/Electron path.

### File List

- `_bmad-output/implementation-artifacts/m34/2-3-complete-athena-ide-support-for-representation-source.md`
- `_bmad-output/implementation-artifacts/m34/sprint-status.yaml`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepresentationSourceLspSupportTest.kt`
- `ide/tree-sitter-athena/grammar.js`
- `ide/tree-sitter-athena/queries/highlights.scm`
- `ide/tree-sitter-athena/scripts/athena-tree-sitter-grammar-corpus.test.mjs`
- `ide/tree-sitter-athena/scripts/athena-tree-sitter-incomplete-source.test.mjs`
- `ide/tree-sitter-athena/scripts/athena-tree-sitter-symbol-highlights.test.mjs`
- `ide/tree-sitter-athena/src/grammar.json`
- `ide/tree-sitter-athena/src/node-types.json`
- `ide/tree-sitter-athena/src/parser.c`
- `ide/tree-sitter-athena/test/corpus/symbol.txt`
- `ide/tree-sitter-athena/test/fixtures/m34-svg-graphic.athena`
- `ide/tree-sitter-athena/test/incomplete/unclosed-svg-graphic.athena.txt`
- `ide/tree-sitter-athena/tree-sitter-athena.wasm`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationSourceCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryContractLoader.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryContractLoaderTest.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/RepresentationPackageSnapshotStagerTest.kt`

### Change Log

- 2026-07-24: Created Story 2.3 with IDE/compiler feedback scope and M34 source-authority boundaries.
- 2026-07-24: Completed Story 2.3 representation-source IDE support, governed SVG diagnostics,
  tree-sitter parity, package-local resource fixture cleanup, and full sequential verification.
