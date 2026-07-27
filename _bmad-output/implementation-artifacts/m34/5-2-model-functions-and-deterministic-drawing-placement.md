---
baseline_commit: 0311ad67c08779db3d1def271d0ac948a4683128
---

# Story 5.2: Model Functions And Deterministic Drawing Placement

Status: review

## Story

As an engineering author,
I want one physical device to expose typed functional units and each occurrence to have governed
drawing placement,
so that coils, contacts, and terminals are semantically correct and spatially deterministic.

## Acceptance Criteria

1. **Given** a device with existing authored ports, **when** function declarations compile, **then**
   stable Engineering Functions reference those ports, preserve physical component identity and
   terminal numbers, and reject missing, duplicate, cross-device, or multiply-owned port references.
2. **Given** a control-drawing layout, **when** function/component occurrences use typed `at` grid
   placement and orientation, **then** ANTLR4, Athena AST, Tree-sitter, LSP, semantic lowering, and
   layout constraint compilation agree deterministically and diagnose conflicts with source spans.
3. **Given** KM1/KM2 coil, main-contact, NO-contact, and NC-contact functions, **when** projection and
   binding run, **then** multiple occurrences share one canonical physical device identity while
   each binds only its declared ports and publishes inspectable cross-reference evidence.
4. **Given** all previous acceptance criteria are green, **when** mandatory final polish/purge runs,
   **then** source, tests, fixtures, docs, generated outputs, and workspace state are deeply reviewed;
   stale or duplicate artifacts are removed; and RED/GREEN, AC-to-evidence, and three-layer review
   are recorded.

**Implements:** FR-45..FR-46, FR-48..FR-49, NFR-14.

## Tasks / Subtasks

- [x] Add Story 5.2 RED contracts before production edits (AC: 1..3)
  - [x] Add language tests for nested functions and fixed grid placement, including exact AST values
        and source spans.
  - [x] Add Engineering IR validation tests for missing, duplicate, cross-device, and multiply-owned
        function port references.
  - [x] Add layout tests for deterministic hard grid constraints, function occurrence identity, and
        conflicting authored placement diagnostics.
  - [x] Add representation tests proving KM1/KM2 function occurrences preserve physical identity,
        isolate terminal bindings, and expose coil/contact cross-reference evidence.
  - [x] Run focused tests and record their pre-implementation failures before production edits.
- [x] Admit the minimal typed function and placement syntax (AC: 1, 2)
  - [x] Add nested `function <name> { role <role>; ports (<port-ref>, ...) }` syntax to ANTLR4 and the
        syntax-only AST; functions remain device children and cannot be top-level declarations.
  - [x] Add `place <device-or-function-ref> at (<positive-column>, <positive-row>) orientation
        <horizontal|vertical>` while preserving existing relative layout syntax.
  - [x] Keep function roles extensible through one typed Athena value contract; do not hardcode an
        electrical role enum into the generic engineering kernel.
  - [x] Regenerate ANTLR output through Gradle and Tree-sitter parser/wasm through existing scripts.
  - [x] Update Tree-sitter grammar, queries, corpus, incomplete-source fixtures, fallback Monaco
        tokenization, LSP Outline/navigation, and focused syntax UX tests.
- [x] Lower and validate Engineering Functions without duplicating project truth (AC: 1)
  - [x] Extend `EngineeringDocument` with default-empty `functions` and add an
        `EngineeringFunction` carrying stable identity, physical owner reference, typed role,
        references to existing ports, and provenance only.
  - [x] Extend plugin lowering blueprints/context and active domain lowerers; normalize local `A1`
        and explicit `KM1.A1` references to the same authored port path.
  - [x] Keep terminal identity and direction/signal facts on `EngineeringPort`; functions cannot copy,
        invent, or reclassify those facts.
  - [x] Add core validation diagnostics for duplicate function names, unknown/ambiguous owners,
        unresolved or cross-device ports, duplicate references inside one function, and one port
        assigned to multiple functions.
  - [x] Preserve current EngineeringDocument constructors and non-function projects through defaults
        and regression tests.
- [x] Compile deterministic hard drawing-grid placement (AC: 2)
  - [x] Extend existing authored layout intent and `LayoutConstraint` models with typed 1-based grid
        coordinates, typed orientation, hard priority, optional function identity, and source span.
  - [x] Index nested functions as semantic declarations and resolve qualified layout subjects such as
        `KM1.coil` without changing their physical owner identity.
  - [x] Lower fixed placement through the existing layout constraint snapshot; do not create a second
        placement pipeline or put grid/pixel facts in Engineering IR.
  - [x] Diagnose duplicate hard placement, cell collision, and provable hard-vs-relative conflicts
        deterministically at authored source spans.
  - [x] Preserve grid placement and orientation in governed placement facts for Story 5.4 composition;
        keep legacy relative layout behavior green.
- [x] Prove function-aware occurrence and cross-reference contracts (AC: 3)
  - [x] Extend representation subject/request/occurrence contracts so one occurrence identifies both
        canonical physical component and optional Engineering Function.
  - [x] Ensure occurrence identity remains unique per function/projection and terminal bindings accept
        only ports referenced by that function.
  - [x] Reuse existing `RepresentationReferenceBinding` with `COIL_CONTACT` for inspectable
        cross-reference evidence; do not introduce QET Master/Slave semantics.
  - [x] Keep package selection and actual rolling-shutter package content in Story 5.3.
- [x] Perform mandatory final polish/purge and evidence review (AC: 4)
  - [x] Review all changed source, tests, fixtures, generated parser assets, docs, and workspace state;
        remove stale or duplicate Story 5.2 artifacts without touching unrelated user changes.
  - [x] Search active code for duplicate function/placement authority, identifier-prefix inference,
        QET classes, pixel coordinates in Engineering IR, and frontend semantic inference.
  - [x] Run focused and full affected tests with Gradle strictly sequential, frontend/Tree-sitter
        tests, IDE build when generated assets change, and UTF-8 encoding audit.
  - [x] Record RED/GREEN commands, AC-to-evidence, three-layer review, completion notes, and exact File
        List before moving the story to `review`.

## Dev Notes

### Frozen Source Surface

Use this minimal public shape unless a failing compiler constraint proves it impossible:

```athena
device KM1 {
  type Contactor

  port A1 {
    direction in
    signal Control
    terminal "A1"
  }
  port A2 {
    direction out
    signal Control
    terminal "A2"
  }

  function coil {
    role coil
    ports (A1, A2)
  }
}

layout schematic {
  place KM1.coil at (7, 4) orientation vertical
  place KM1 at (5, 2) orientation horizontal
}
```

- Grid coordinates are positive, 1-based integer column/row indexes, never pixels.
- `horizontal|vertical` reuses existing typed orientation vocabulary. Mirroring, arbitrary angles,
  CAD constraints, and drag persistence are out of scope.
- Function names are device-local. Port references may be local (`A1`) or explicitly owner-qualified
  (`KM1.A1`) so cross-device references parse and fail with semantic diagnostics.
- Minimum proof roles are `coil`, `main-contact`, `normally-open-contact`, and
  `normally-closed-contact`. Core stores a typed role value; electrical interpretation remains
  outside the generic kernel.

### Architecture Guardrails

- `EngineeringFunction` is a project-owned child partition of one physical
  `EngineeringComponent`, not a reusable component definition, package product, duplicate device,
  representation element, or QET Master/Slave object.
- Physical component identity remains canonical for interaction and cross-reference. Function
  identity selects one projected occurrence and its allowed ports.
- Existing semantic ports remain SSOT for terminal number, direction, signal, and connectivity.
- Explicit placement is projection intent. It may enter language AST, semantic declaration binding,
  layout intent/constraint/fact models, and presentation compilation; it must not become a device
  property, Engineering IR geometry, renderer guess, DOM state, or persisted canvas coordinate.
- Extend existing layout, representation binding, repeated-subject, interaction, and reference
  contracts. Do not add a parallel function graph, placement store, occurrence IR, or fifth view.
- QET is offline evidence only. Do not load `.qet`/`.elmt`, copy XML runtime, or adopt page ownership.
- Story 5.2 establishes contracts and focused KM1/KM2 proof. Story 5.3 owns complete package/sample;
  Story 5.4 owns final sheet composition/routing; Story 5.5 owns Electron screenshots.

### Existing Code To Extend

- `kernel/language/.../Athena.g4`, `AthenaLanguageModel.kt`, and `AthenaAntlrParseAdapter.kt` own
  authoritative syntax and AST adaptation.
- `ide/tree-sitter-athena/grammar.js` and `queries/highlights.scm` own syntax UX only.
- `ide/lsp/.../AthenaLanguageFeatures.kt` owns Outline, references, navigation, completion, and
  semantic-token fallback; it must consume AST facts, not infer meaning from text.
- `kernel/engineering-model/.../EngineeringModel.kt`, plugin API lowering contracts, active domain
  lowerers, `EngineeringIrLowerer`, and `EngineeringIrValidator` own canonical function lowering and
  validation.
- `ProjectSemanticDeclarationIndexer`, `ProjectSemanticLayoutHintBinder`, and
  `ProjectSemanticLayoutConstraintLowerer` own function declaration binding and hard-placement
  lowering.
- `kernel/layout-model/.../LayoutModel.kt` and the existing schematic layout engine own governed
  constraints/facts. Preserve old constructors with defaults where practical.
- `RepresentationBindingRequest`, `RepresentationOccurrence`, and
  `PackageBackedRepresentationOccurrenceFactory` are the existing occurrence path to extend.

### Testing And Build Rules

- Use RED/GREEN. Do not write production syntax or semantic code before focused failing contracts.
- Gradle verification is strictly sequential on Windows. Never overlap Gradle invocations.
- Run generated grammar/parser commands through repository scripts or Gradle, never manual edits to
  generated ANTLR/Tree-sitter artifacts.
- New data fields use defaults when required to keep existing M0-M34 constructors source-compatible.
- No new dependency or web research is required; use pinned ANTLR4, Tree-sitter, Kotlin, Theia, and
  existing test frameworks in this repository.
- Every task ends with direct AC evidence. Final review covers semantic authority, compiler/projection
  authority, and frontend adapter/workspace cleanliness.

### Previous Story Intelligence

- Story 5.1 froze `Control Drawing` as one product surface backed by existing `schematic`; do not add
  a fifth projection or restore peer selectors.
- The dedicated sample skeleton compiles and contains only real engineering devices. Do not add
  frame, rail, zone, route-channel, or title-label devices.
- Layout role classification now uses authored `type` facts and defaults unknown types to
  `ANNOTATION`; never reintroduce name-prefix classification.
- Story 5.1 intentionally made no Electron visual claim. Keep Story 5.2 evidence structural.
- Worktree contains accumulated M33/M34/user changes. Never reset or revert unrelated work.

### References

- [Epic 5 / Story 5.2](epics.md#story-52-model-functions-and-deterministic-drawing-placement)
- [M34 corrective PRD](../../planning-artifacts/prds/prd-Athena-2026-07-24-m34/prd.md#corrective-product-acceptance---professional-control-drawing)
- [M34 corrective architecture](../../planning-artifacts/architecture/architecture-Athena-2026-07-24-m34/ARCHITECTURE-SPINE.md#corrective-architecture-decisions---professional-control-drawing)
- [Approved sprint change](../../planning-artifacts/sprint-change-proposal-2026-07-26-m34-professional-control-drawing.md)
- [Story 5.1](5-1-freeze-the-professional-drawing-product-contract.md)
- [Professional target](m34-professional-renderer-target.md)

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: `:ide:lsp:test --tests "*AthenaAuthoringSupportTest.document symbols and definitions expose nested function and fixed placement subjects"` failed because `PlaceAt` was not handled by the LSP Outline formatter.
- RED: `node --test ide/theia-frontend/scripts/athena-language-highlighting-definition.test.mjs` failed two contracts because fixed-placement and function keywords were absent from Monaco/theme fallback.
- RED: the first full affected run failed `LanguageFacadeBoundaryTest`; the facade allow-list was missing only `EngineeringFunctionDeclaration`, `DrawingGridPosition`, and `LayoutOrientation`.
- RED: the added duplicate/collision test exposed a false extra cell-collision diagnostic for a repeated placement of the same subject. Collision reporting now deduplicates by subject while preserving the dedicated duplicate diagnostic.
- GREEN: focused language, validation, compiler, representation, package-runtime, and LSP Story 5.2 tests passed.
- GREEN: `yarn --cwd ide/tree-sitter-athena test` passed all 59 tests; `yarn --cwd ide workspace @engineeringood/athena-theia-frontend test` compiled TypeScript and passed all 237 tests.
- GREEN: `yarn --cwd ide build` completed with zero Theia browser/node/electron build errors; `yarn --cwd ide prepare:dev-runtime` refreshed the packaged LSP runtime after the final compiler correction.
- GREEN: final `gradlew.bat --no-daemon --console=plain test` passed all 151 Gradle tasks after the collision fix.
- PURGE: `git diff --check` reported no whitespace errors; authority scans found no active QET semantics, project-truth duplication, identifier-prefix role inference, pixel-owned Engineering IR, or frontend semantic inference; `tools/encoding-audit.ps1` passed.

### Completion Notes List

- AC1: nested functions lower through plugin blueprints into default-compatible `EngineeringDocument.functions`; ports remain the sole owner of terminal, direction, signal, and connectivity facts. Validation covers duplicate names, unresolved/ambiguous owners, unresolved/cross-owner/duplicate ports, and multiply-owned ports.
- AC2: typed positive 1-based grid placement and orientation flow through the existing AST, semantic binding, layout constraint, placement fact, Tree-sitter, LSP, and Monaco paths. Duplicate, conflicting, cell-collision, and provable relative-conflict diagnostics retain authored spans.
- AC3: function-aware representation requests and occurrences preserve canonical physical identity plus optional function identity. KM1/KM2 coil, main, NO, and NC proofs isolate terminal bindings, keep occurrence ids unique, and reuse `COIL_CONTACT` reference evidence. Package-backed occurrence creation preserves function identity.
- Three-layer review, semantic authority: Engineering ports remain SSOT; extensible function roles partition existing device ports without copying facts or introducing an electrical enum into the generic kernel.
- Three-layer review, compiler/projection authority: fixed placement remains authored projection intent and uses the existing constraint/fact pipeline; no pixels or renderer state entered Engineering IR.
- Three-layer review, frontend/workspace: Outline/navigation consume AST spans, highlighting remains syntax-only, and the frontend gained no source planner or engineering inference. Generated parser assets were regenerated through repository tooling and verified.
- Deep polish removed redundant collision evidence and stale facade assumptions. Unrelated accumulated M33/M34/user worktree changes were not reverted or attributed to this story.

### File List

- `_bmad-output/implementation-artifacts/m34/5-2-model-functions-and-deterministic-drawing-placement.md`
- `_bmad-output/implementation-artifacts/m34/sprint-status.yaml`
- `extensions/domain-dummy/src/main/kotlin/com/engineeringood/athena/domain/dummyruntime/DummyRuntimeDomainPlugin.kt`
- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeLowering.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringSupportTest.kt`
- `ide/theia-frontend/scripts/athena-language-highlighting-definition.test.mjs`
- `ide/theia-frontend/src/browser/athena-language-definition.ts`
- `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`
- `ide/tree-sitter-athena/grammar.js`
- `ide/tree-sitter-athena/queries/highlights.scm`
- `ide/tree-sitter-athena/scripts/athena-tree-sitter-grammar-corpus.test.mjs`
- `ide/tree-sitter-athena/src/grammar.json`
- `ide/tree-sitter-athena/src/node-types.json`
- `ide/tree-sitter-athena/src/parser.c`
- `ide/tree-sitter-athena/test/fixtures/m34-function-placement.athena`
- `ide/tree-sitter-athena/tree-sitter-athena.wasm`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AuthoredLayoutIntentMapper.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AuthoredLayoutIntentSourceSerializer.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaDomainSemanticsCoordinator.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticDeclarationIndexer.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticLayoutConstraintLowerer.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticLayoutHintBinder.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticSchematicLayoutFactDeriver.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM34FunctionCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/AthenaM34FunctionPlacementCompilerTest.kt`
- `kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringModel.kt`
- `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaM34FunctionPlacementSyntaxTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/LanguageFacadeBoundaryTest.kt`
- `kernel/layout-engine/src/main/kotlin/com/engineeringood/athena/layout/engine/SchematicLayoutEngine.kt`
- `kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/PackageBackedRepresentationOccurrenceFactory.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/PackageBackedRepresentationOccurrenceModels.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/PackageBackedRepresentationOccurrenceFactoryTest.kt`
- `kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaDomainSemanticsModel.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/RepresentationBindingCompiler.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/RepresentationContracts.kt`
- `kernel/representation-model/src/test/kotlin/com/engineeringood/athena/representation/FunctionAwareRepresentationBindingTest.kt`
- `kernel/validation/src/main/kotlin/com/engineeringood/athena/semantics/core/EngineeringIrValidator.kt`
- `kernel/validation/src/test/kotlin/com/engineeringood/athena/semantics/core/EngineeringFunctionValidationTest.kt`

### Change Log

- 2026-07-26: Created through the BMAD create-story workflow from approved corrective Epic 5.
- 2026-07-26: Implemented typed engineering functions, deterministic hard grid placement, function-aware occurrences, IDE syntax/navigation support, full regression evidence, and mandatory deep polish/purge; moved to review.
