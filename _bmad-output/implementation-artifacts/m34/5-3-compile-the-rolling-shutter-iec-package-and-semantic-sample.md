---
baseline_commit: 0311ad67c08779db3d1def271d0ac948a4683128
---

# Story 5.3: Compile The Rolling-Shutter IEC Package And Semantic Sample

Status: review

## Story

As an engineer opening the M34 example,
I want recognizable IEC-style symbols bound to a truthful rolling-shutter semantic model,
so that every visible occurrence is compiled from governed Athena material.

## Acceptance Criteria

1. **Given** native Athena graphic declarations, **when** line, polyline, arc, circle, rectangle,
   and dynamic label-slot forms compile, **then** they lower through the existing canonical Graphic
   Primitive IR with deterministic type, bounds, style, anchor, and label diagnostics.
2. **Given** the dedicated M34 sample, **when** repository and semantic compilation run, **then** it
   contains truthful source, breaker, fuse-disconnector, transformer, reversing contactors, coils,
   NO/NC contacts, push buttons, limit switches, lamps, terminals, motor, earth, and connections with
   preserved terminal identities and no fake drawing-structure devices.
3. **Given** profile and function-aware binding rules, **when** package resolution runs, **then**
   every required occurrence resolves exactly one package-local Element/Symbol and no name, file
   order, QET path, fallback box, or Kotlin fixture selects representation.
4. **Given** all previous acceptance criteria are green, **when** mandatory final polish/purge runs,
   **then** source, tests, fixtures, docs, generated outputs, and workspace state are deeply reviewed;
   stale or duplicate artifacts are removed; and RED/GREEN, AC-to-evidence, and three-layer review
   are recorded.

**Implements:** FR-44..FR-49.

## Tasks / Subtasks

- [x] Add Story 5.3 RED contracts before production edits (AC: 1..3)
  - [x] Add language/compiler tests for all six native geometry forms and one dynamic label slot,
        including exact AST values, source spans, canonical IR, and deterministic invalid cases.
  - [x] Add binding tests proving device and function selectors are distinct, highest-priority ties
        fail, and each sample device/function resolves exactly one compiled package-local definition.
  - [x] Expand the dedicated sample contract test to require the complete truthful device, function,
        port, terminal, connection, placement, profile, binding, and definition inventory.
  - [x] Run focused tests and record their pre-implementation failures before production edits.
- [x] Complete the minimal native drawing source surface (AC: 1)
  - [x] Extend ANTLR4 and Athena's syntax-only AST for `polyline`, `arc`, `circle`, `rectangle`, and
        dynamic `label` declarations while retaining existing `line` syntax.
  - [x] Use the frozen compact forms in Dev Notes; do not add path, arbitrary transforms, CAD
        constraints, renderer vocabulary, or literal project-label values to reusable definitions.
  - [x] Lower geometry to the existing `GraphicPrimitive` variants. Lower dynamic labels to typed
        `RepresentationLabelSlot` contracts with explicit local point/bounds/style; materialize
        `GraphicPrimitive.Text` only after an occurrence supplies a semantic label value.
  - [x] Validate finite coordinates, positive sizes/radii, polyline point count, arc sweep, unique
        primitive/slot ids, in-bounds geometry and anchors, known style/label roles, and complete
        source spans; reject the whole definition on error.
  - [x] Extend element composition so explicitly exported child label slots preserve transformed
        local placement and style instead of being dropped or inferred.
  - [x] Update the canonical formatter, Tree-sitter grammar/queries/corpus, Monaco fallback,
        semantic tokens, Outline, completion, and navigation from the same AST vocabulary; regenerate
        generated parser assets through repository tooling.
- [x] Make binding rules explicitly function-aware (AC: 3)
  - [x] Extend the nested binding selector from device-only to typed `select device where` and
        `select function where`; preserve selector kind in AST and `RepresentationBindingRule`.
  - [x] Extend `BindingSubject`/`BindingResolver` with a default-compatible typed subject kind so a
        function rule cannot match a physical-device request and vice versa.
  - [x] Build function semantic facts from authored physical-device type/model plus the compiled
        `EngineeringFunction.role`; never infer role from device names, terminal strings, or file order.
  - [x] Preserve canonical physical component identity separately from function identity in every
        resolved occurrence and cross-reference proof established by Story 5.2.
- [x] Build the truthful rolling-shutter semantic sample (AC: 2)
  - [x] Expand the electrical plugin's governed type registry/schema with the minimum real concepts
        required by this sample (source, breaker, fuse-disconnector, transformer, contactor,
        push button, limit switch, terminal, lamp, motor, and protective earth); do not encode these
        as model-name conventions or generic `Switch` aliases.
  - [x] Replace the two-device sample skeleton with real devices, typed ports, authored terminal
        identities, KM1/KM2 coil/main/NO/NC function partitions, semantic connections, and explicit
        17x8 grid placement intent.
  - [x] Keep frame, coordinate bands, title block, zones, rails, route channels, labels, junctions,
        and conductors-as-geometry out of project devices. Story 5.4 derives those composition facts.
  - [x] Keep project source as engineering SSOT: package Symbol/Element definitions may declare only
        reusable visual geometry, anchor compatibility, and dynamic label slots.
- [x] Compile the package-local IEC material and deterministic resolution proof (AC: 1, 3)
  - [x] Add ordinary Athena Symbol definitions for the required atomic IEC-style glyphs using native
        geometry where concise; use governed package-local SVG only if a shape is materially clearer
        there. Do not read QET `.elmt` or copy QET XML at runtime.
  - [x] Add one-layer Element compositions with explicit child transforms/z-order and explicit anchor
        and label exports for source, breaker, fuse-disconnector, transformer, contactor functions,
        push buttons, limit switches, lamps, terminals, motor, and earth.
  - [x] Add one schematic Presentation Profile plus typed device/function Binding declarations in the
        same package hierarchy; exact package/definition/version identity must derive from the staged
        Athena snapshot.
  - [x] Add a compiler-owned, sample-independent material resolution result that uses the existing
        snapshot compiler and `BindingResolver`, returns compiled definitions/selections/diagnostics,
        and is consumable by Story 5.4 without Kotlin fixture selection.
  - [x] Prove package-local provenance, unique selection, required anchor/label compatibility, exact
        terminal preservation, zero fallback, zero XML/QET/raw-markup authority, and no renderer file reads.
- [x] Perform mandatory final polish/purge and evidence review (AC: 4)
  - [x] Review all changed source, tests, package assets, sample files, generated parser assets, docs,
        snapshots, and workspace state; delete stale/duplicate Story 5.3 artifacts without reverting
        unrelated user work.
  - [x] Purge generated `.athena/snapshots` and prove package compilation recreates them; do not commit
        staged cache copies as source authority.
  - [x] Search active code and sample material for QET/XML runtime reads, Kotlin selection fixtures,
        filename/name-prefix inference, fallback boxes, fake drawing devices, duplicate terminal truth,
        raw markup transport, or a second primitive/occurrence pipeline.
  - [x] Run focused and full affected tests with Gradle strictly sequential, Tree-sitter/frontend
        tests, IDE build when generated assets change, and the UTF-8 encoding audit.
  - [x] Record RED/GREEN commands, AC-to-evidence, three-layer review, completion notes, and exact File
        List before moving the story to `review`.

## Dev Notes

### Frozen Native Drawing Syntax

Use these minimum domain-facing forms unless a failing parser/IR constraint proves one impossible:

```athena
symbol iec_contactor_coil {
  identity "iec.contactor.coil"
  version "1.0.0"

  graphic {
    bounds (0, 0, 80, 80)
    line terminalA1 from (40, 0) to (40, 14) style conductor
    rectangle coilBody at (20, 14) size (40, 52) style symbol
    arc coilMark center (40, 40) radius 12 from 180 sweep 180 style symbol
    circle terminalMark center (40, 0) radius 2 style terminal
    polyline reference points ((56, 28), (64, 40), (56, 52)) style reference
    label deviceTag at (0, -14) size (80, 12) role device-tag style device-label
  }

  anchor A1 {
    ref terminalA1
    point (40, 0)
    role terminal
    direction in
    signal Control
  }
}
```

- `polyline` requires at least two explicit point tuples.
- `arc` uses finite center/radius/start/sweep values; radius is positive and absolute sweep is in
  `(0, 360]`.
- `rectangle at/size` uses positive width/height and lowers with zero corner radius in V1.
- `label` declares a dynamic slot, not literal project text. Point, bounds, role, and style are
  intrinsic representation facts; the occurrence binds the value later.
- Initial label roles are `device-tag`, `terminal-label`, `reference`, and `model`. Keep the list
  mapped to existing presentation label roles; do not create free-form renderer roles.
- Add only the restrained style tokens required by the control drawing (symbol/conductor/terminal,
  device/terminal/reference label). Style lookup remains compiler-owned and fail-closed.

### Function-Aware Binding Shape

```athena
profile ControlDrawingIEC {
  projection schematic
  standard IEC
  style athena-industrial-iec-v1
  fallback fail-closed
}

binding ContactorCoilIEC {
  profile ControlDrawingIEC
  priority 200

  select function where {
    type Contactor
    role coil
  }

  use element "iec.contactor.coil.element" version "1.0.0"
  variant "standard"
}
```

- Selector kind is a typed rule field, not a magic selector fact.
- Function requests use the physical device type/model plus function role; they expose both canonical
  physical id and function id. Device requests do not acquire a fake role.
- `BindingResolver` remains sole selector. Equal top priority is an error; no lexical/file-order
  tie-break is allowed. `RepresentationBindingCompiler` remains sole occurrence constructor.
- Package metadata may be derived generically from repository contract and compiled rules, but Kotlin
  must not choose a sample symbol or element.

### Semantic Sample Boundary

- Use real electrical concepts in the electrical plugin schema, not one broad `Switch` type plus model
  strings. Keep the type additions specific to generic electrical meaning, never M34 ids.
- Ports own terminal, direction, signal, and connectivity facts. Functions only partition existing
  physical-device ports. Symbols/Elements only own compatible anchor predicates.
- KM1/KM2 each remain one physical contactor with coil, main contact group, NO auxiliary, and NC
  auxiliary functions. Add `53`/`54` where the target circuit needs the second NO contact; do not
  create duplicate KM devices for each occurrence.
- `ProtectiveEarth` is a real semantic electrical endpoint, not sheet decoration. Sheet frame, zones,
  title block, route channels, and labels are not devices.
- Explicit `place <subject-or-function> at (<column>, <row>) orientation <...>` remains 1-based grid
  projection intent from Story 5.2. No pixel coordinates enter Engineering IR.

### Architecture Guardrails

- `.athena` remains the only metadata authority. Package-local SVG, if any, contributes governed
  geometry/node-local contracts only and never identity, version, device type, function role, or policy.
- Extend `RepresentationDefinition`, `RepresentationLabelSlot`, `RepresentationBindingRule`,
  `BindingResolver`, and the existing snapshot compiler. Do not add a fourth representation IR,
  alternate package loader, sample catalog, direct SVG renderer, or new product view.
- Existing `GraphicPrimitive` already contains Line, Polyline, Arc, Circle, Rectangle, and Text.
  Story 5.3 admits missing Athena source forms and label-slot placement; it does not redesign the IR.
- Story 5.3 ends at truthful semantic/package material and deterministic resolution. Story 5.4 owns
  the 17x8 sheet composition, conductors, junction/crossing behavior, and workbench rendering. Story
  5.5 owns real Electron screenshots and visual target acceptance.
- QET sources are offline visual/domain evidence only. Do not load `.qet`/`.elmt`, copy XML page
  ownership, Master/Slave classes, terminal ids, or label linking.

### Existing Code To Extend

- `kernel/language/.../Athena.g4`, `AthenaLanguageModel.kt`, and
  `AthenaAntlrParseAdapter.kt` own authoritative syntax and syntax-only AST.
- `AthenaSymbolSourceValidator`, `AthenaSymbolSourceLowerer`,
  `AthenaElementSourceLowerer`, and `AthenaRepresentationSourceFormatter` own native representation
  validation/lowering/canonical formatting.
- `RepresentationDefinitionModels.kt` owns canonical label slots; add default-compatible placement
  facts there rather than a separate label IR.
- `RepresentationBindingRuleModels.kt`, `BindingResolverModels.kt`, and `BindingResolver.kt` own typed
  selector authority. Existing generic semantic-fact matching should be extended, not replaced.
- `AthenaRepresentationPackageSnapshotCompiler` owns staged package compilation and generated
  descriptors. Descriptor label projections must preserve compiled slot facts needed downstream.
- `ElectricalRuntimeContracts.kt` owns the electrical type registry/schema; validation must consume
  that same set.
- `examples/m34/professional-control-drawing` is the only Epic 5 product sample. Representation
  resources stay beside their Athena source in a real package/file-system hierarchy.
- Tree-sitter and LSP are syntax UX adapters only. They must consume the same authored vocabulary and
  cannot infer electrical meaning from text.

### Testing And Build Rules

- Use RED/GREEN. Do not edit production grammar, semantic types, package assets, or sample source
  before focused failing contracts exist and their failures are recorded.
- Gradle verification is strictly sequential on Windows. Never overlap Gradle invocations.
- Regenerate ANTLR and Tree-sitter outputs through existing Gradle/yarn scripts; never hand-edit
  generated parser files.
- New model fields require defaults where needed to keep existing M0-M34 callers source-compatible.
- No new dependency or web research is required; use pinned ANTLR4, Tree-sitter, Kotlin, Theia, and
  existing test frameworks.
- Story 5.3 makes no screenshot-quality claim. Structured package/semantic proof is required; the
  real rendered visual gate remains Story 5.5.

### Previous Story Intelligence

- Story 5.2 already added nested Engineering Functions, function-aware occurrence identities,
  cross-reference evidence, and explicit grid placement. Reuse those contracts; do not duplicate them.
- Cell-collision diagnostics must deduplicate repeated evidence for the same subject while preserving
  real collisions and duplicate-placement diagnostics.
- Function roles remain extensible typed values in the generic kernel. Electrical role interpretation
  belongs in package binding facts, not a core electrical enum.
- Story 5.1 froze `Control Drawing` as one product surface backed by existing `schematic`; do not add
  another projection or restore Cabinet/Documentation selector competition.
- The worktree contains accumulated M33/M34/user changes. Never reset or revert unrelated work.

### Git Intelligence

- Baseline is `0311ad6 feat(m32): add engineering package platform`; all M33/M34 work is accumulated
  in the dirty worktree and must be preserved.
- Existing M34 files are mostly untracked relative to the baseline. File List and evidence must name
  exact Story 5.3 ownership without attributing earlier story changes.

### References

- [Epic 5 / Story 5.3](epics.md#story-53-compile-the-rolling-shutter-iec-package-and-semantic-sample)
- [M34 corrective PRD](../../planning-artifacts/prds/prd-Athena-2026-07-24-m34/prd.md#corrective-product-acceptance---professional-control-drawing)
- [M34 corrective architecture](../../planning-artifacts/architecture/architecture-Athena-2026-07-24-m34/ARCHITECTURE-SPINE.md#corrective-architecture-decisions---professional-control-drawing)
- [Approved sprint change](../../planning-artifacts/sprint-change-proposal-2026-07-26-m34-professional-control-drawing.md)
- [Story 5.2](5-2-model-functions-and-deterministic-drawing-placement.md)
- [Professional target](m34-professional-renderer-target.md)

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: `:kernel:compiler:test --tests AthenaRepositoryContractLoaderTest` failed because the canonical
  repository result did not expose `representationPackageRoots`.
- RED: `:kernel:compiler:test --tests AthenaM34CabinetRenderPathDeletionGateTest` failed while the
  active material resolver still called the compatibility `stageRepository` manifest parser.
- GREEN: focused language, package snapshot, binding, material-resolution, exact sample inventory,
  generated-boundary, anchor-side, and deletion-gate tests passed after implementation.
- GREEN: `:kernel:language:test`, `:kernel:package-runtime:test`, `:kernel:compiler:test`, and full
  `test` passed sequentially; full repository result was `BUILD SUCCESSFUL` with 151 actionable tasks.
- GREEN: `yarn --cwd ide/tree-sitter-athena test` passed 16 corpus parses and 60 Node tests.
- GREEN: `yarn --cwd ide/theia-frontend test` passed 237 tests; `yarn --cwd ide build` completed all
  browser, node, Electron, Tree-sitter asset, and LSP runtime builds with zero errors.
- GREEN: `tools/encoding-audit.ps1` passed and `git diff --check` reported no whitespace errors.
- PURGE: deleted every generated file below
  `examples/m34/professional-control-drawing/.athena/snapshots`; `rg --files` confirms no staged cache
  remains in the product sample.

### AC-To-Evidence Review

- AC1: `AthenaM34DrawingPrimitiveSyntaxTest`, `AthenaM34DrawingPrimitiveCompilerTest`, symbol/element
  compiler tests, Tree-sitter corpus, semantic-token tests, and the full build prove all six native
  geometry forms plus dynamic label slots lower through canonical Graphic Primitive IR.
- AC2: `AthenaM34ProfessionalDrawingSampleContractTest` proves the exact 16-device, 8-function,
  62-port, 34-connection, 22-placement semantic inventory with authored terminal identities and no
  drawing-structure devices.
- AC3: `AthenaM34ProfessionalDrawingMaterialResolutionTest`, binding resolver tests, snapshot compiler
  tests, and the deletion gate prove 22 exact package-local selections with typed device/function
  selectors, package provenance, no fallback, and no renderer-side selection.
- AC4: focused and full tests, package-authority search, cache purge, encoding audit, diff check, and
  this evidence record complete the mandatory polish/purge gate.

### Three-Layer Review

- Contract layer: source grammar, spans, formatter, IDE vocabulary, semantic inventory, terminal
  ownership, and function/device identity remain typed and deterministic.
- Compiler/package layer: repository roots now come from the canonical repository loader; snapshots
  are content-addressed from captured bytes; material resolution preserves package-local definition,
  rule, anchor, and terminal provenance without synthetic variants.
- Product-boundary layer: the active M34 material path contains no raw markup, XML/QET authority,
  compatibility occurrence factory, descriptor-bounds box, filename/name inference, or renderer file
  read. XML parsing found by the audit is restricted to the fail-closed SVG geometry compiler; legacy
  M32 compatibility remains outside the active path and is covered by the deletion ledger.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Added the complete native drawing source vocabulary and dynamic label-slot lowering without adding
  CAD/path syntax or a parallel representation IR.
- Added function-aware binding, truthful rolling-shutter electrical semantics, and deterministic
  package-local IEC material resolution for every required device/function occurrence.
- Hardened immutable snapshots against hash/content races, preserved old content-addressed snapshots,
  derived proof from real compiled material, and made the canonical repository loader the active
  package-root authority.
- Split snapshot orchestration from capture/validation to keep Kotlin responsibilities readable.
- Completed full regression, IDE build, authority audit, and generated-cache purge. Story is ready for
  independent code review; visual composition and Electron screenshot acceptance remain Stories 5.4
  and 5.5 by design.

### File List

- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeContracts.kt`
- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeLowering.kt`
- `extensions/domain-electrical/src/test/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPluginTest.kt`
- `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaM34DrawingPrimitiveSyntaxTest.kt`
- `kernel/package-model/src/main/kotlin/com/engineeringood/athena/packageplatform/RepresentationBindingRuleModels.kt`
- `kernel/package-model/src/main/kotlin/com/engineeringood/athena/packageplatform/RepresentationDescriptorModels.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/BindingResolver.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/BindingResolverModels.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/RepresentationPackageSnapshotCapture.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/RepresentationPackageSnapshotModels.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/RepresentationPackageSnapshotStager.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/BindingResolverSelectionTest.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/RepresentationPackageSnapshotStagerTest.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/RepresentationDefinitionModels.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaElementSourceLowerer.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaProfileBindingSourceValidator.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationMaterialBinder.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationMaterialContracts.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationMaterialModels.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationMaterialResolver.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationMaterialSubjects.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationPackageSnapshotCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSymbolSourceFormatter.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSymbolSourceLowerer.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSymbolSourceModels.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSymbolSourceValidator.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryContractLoader.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryContractValidationModel.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM34CabinetRenderPathDeletionGateTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM34DrawingPrimitiveCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM34ProfessionalDrawingMaterialResolutionTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM34ProfessionalDrawingSampleContractTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaProfileBindingSourceValidatorTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationPackageSnapshotCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaSymbolSourceCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryContractLoaderTest.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepresentationSourceLspSupportTest.kt`
- `ide/tree-sitter-athena/grammar.js`
- `ide/tree-sitter-athena/queries/highlights.scm`
- `ide/tree-sitter-athena/src/grammar.json`
- `ide/tree-sitter-athena/src/node-types.json`
- `ide/tree-sitter-athena/src/parser.c`
- `ide/tree-sitter-athena/test/corpus/symbol.txt`
- `ide/tree-sitter-athena/scripts/athena-tree-sitter-grammar-corpus.test.mjs`
- `ide/tree-sitter-athena/scripts/athena-tree-sitter-symbol-highlights.test.mjs`
- `ide/theia-frontend/src/browser/athena-language-definition.ts`
- `ide/theia-frontend/src/browser/athena-tree-sitter-highlighting-service.ts`
- `ide/theia-frontend/scripts/athena-language-highlighting-definition.test.mjs`
- `examples/m34/professional-control-drawing/athena.yaml`
- `examples/m34/professional-control-drawing/athena.lock`
- `examples/m34/professional-control-drawing/src/01-control-drawing.athena`
- `examples/m34/professional-control-drawing/packages/representation/com/engineeringood/m34/control/control-drawing-bindings.athena`
- `examples/m34/professional-control-drawing/packages/representation/com/engineeringood/m34/control/power/power-material.athena`
- `examples/m34/professional-control-drawing/packages/representation/com/engineeringood/m34/control/power/power-bindings.athena`
- `examples/m34/professional-control-drawing/packages/representation/com/engineeringood/m34/control/control/contactor-material.athena`
- `examples/m34/professional-control-drawing/packages/representation/com/engineeringood/m34/control/control/operator-material.athena`
- `examples/m34/professional-control-drawing/packages/representation/com/engineeringood/m34/control/control/control-bindings.athena`
- `examples/m34/professional-control-drawing/packages/representation/com/engineeringood/m34/control/field/field-material.athena`
- `examples/m34/professional-control-drawing/packages/representation/com/engineeringood/m34/control/field/field-bindings.athena`

### Change Log

- 2026-07-26: Created through the BMAD create-story workflow from approved corrective Epic 5.
- 2026-07-26: Implemented typed drawing material, truthful sample semantics, function-aware package
  binding, immutable package snapshots, full IDE support, deep review, and purge; moved to review.
