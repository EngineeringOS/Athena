---
baseline_commit: 9ca7c80239d38d9e2a4880886e71e27e2923c41b
---

# Story 1.2: Compile Traceable Function Symbol Occurrences

Status: done

## Story

As an engineering author,
I want one Entity to publish multiple Function/Symbol occurrences with stable traces,
so that EPLAN-like function separation survives without making symbols own engineering identity.

## Acceptance Criteria

1. Given the active M44 rolling-shutter example has one Entity with at least two Functions and compatible
   library Symbol references, when Athena compiles the Canonical Scene, then each Function occurrence has
   stable `semanticId`, `occurrenceId`, `traceId`, Function-before-Entity source trace, resolved ports,
   labels, and package item reference.
2. Given an Entity-level occurrence and Function-level occurrences exist for the same Entity, when Source
   Trace is queried, then Function-level source origin is primary for the Function occurrence and Entity
   origin remains related trace, not the other way around.
3. Given a Symbol reference changes for a Function occurrence, when the scene is recompiled, then
   Engineering Entity, Function, Occurrence, Relationship, and Port identities remain stable while only
   representation binding and package trace change.
4. Given a Part replacement is modeled in the compile fixture, when the scene is recompiled, then Entity,
   Function, Occurrence, and Relationship identities remain stable while implementation/library trace
   changes.
5. Given library descriptor port metadata conflicts with authored Engineering Port identity, direction,
   domain, or flow kind, when compilation runs, then publication fails closed with a plain
   subject/problem/correction diagnostic and no partial scene.

## Tasks / Subtasks

- [x] Add red tests for Function-level scene occurrence contracts (AC: 1, 2)
  - [x] Active M44 fixture compiles one Entity with at least two Functions into multiple selectable
        `SceneOccurrence`s.
  - [x] Each Function occurrence exposes stable `semanticId`, `occurrenceId`, `traceId`, `assetId`,
        package item reference, labels, and resolved ports.
  - [x] Trace order proves Function source origin primary and Entity source origin related.
- [x] Add red tests for identity preservation under representation and implementation changes (AC: 3, 4)
  - [x] Change Symbol binding for one Function and prove Entity, Function, Occurrence, Relationship, and
        Port identities remain unchanged.
  - [x] Change Part binding in the fixture/model path and prove implementation trace changes without
        rewriting engineering identity.
- [x] Add red tests for library/Engineering Port authority conflict (AC: 5)
  - [x] Descriptor direction/domain/flow mismatch against authored Port rejects publication.
  - [x] Diagnostic names exact subject, problem, and correction in human engineering language.
  - [x] No partial `AthenaDiagramScene` is published on conflict.
- [x] Implement Function-level representation binding and scene publication (AC: 1, 2, 3)
  - [x] Extend current representation binding model instead of inventing a second resolver.
  - [x] Replace old `DEVICE` naming in touched M44 path with current `ENTITY` / `FUNCTION` naming; no
        compatibility alias or shim.
  - [x] Preserve source truth: Symbol metadata supplies geometry/package trace only, never Engineering
        Port semantics.
- [x] Update presentation contract/schema/generated frontend types as needed (AC: 1)
  - [x] Add or expose explicit `semanticId` if the current `subjectId` field is insufficient for M44 trace
        and selection semantics.
  - [x] Keep schema deterministic and regenerate Theia generated contracts if schema changes.
- [x] Create active M44 fixture/example ownership (AC: all)
  - [x] Create `examples/m44/rolling-shutter` or test fixture equivalent under M44 ownership.
  - [x] Do not keep active proof tied to `examples/m43/rolling-shutter`.
  - [x] Package path must obey repository contract: governed `.athena` source declares a package matching
        its source-root-relative directory.
- [x] Run sequential validation (AC: all)
  - [x] Targeted tests for package model/runtime if binding enums or admission contracts change.
  - [x] Targeted compiler/presentation-model tests for scene occurrence and trace contracts.
  - [x] Frontend contract generation/check if schema or generated TypeScript changes.
  - [x] `.\gradlew.bat --no-daemon --console=plain test`
  - [x] `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  - [x] `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`

## Dev Notes

### PRD / Architecture Guardrails

- M44 is the first complete engineering authoring transaction loop milestone, not a canvas-only milestone.
- `AD-1`: Canonical Scene is the only renderer-neutral publication authority. The frontend cannot invent or
  mutate engineering meaning.
- `AD-3`: Engineering source owns Port identity, direction, domain, flow kind, and semantic properties.
  Library metadata owns keyed geometry, orientation, label/hit zones, and compatibility envelope only.
- `AD-4`: Function-level Representation Binding is first-class. One Entity can publish multiple Function
  occurrences. Function trace wins over Entity trace for Function occurrences.
- `AD-5`: Representation instance identity is separate from engineering identity.
- `AD-12`: This story is foundational for the golden authoring loop because later selection/edit operations
  need stable semantic, occurrence, trace, and package identities.

### EPLAN Lesson Applied

- EPLAN separates Device, Function, Symbol, Part, and Connection. Athena M44 maps this as Entity,
  Function, Symbol/Representation, Part/Implementation, and Relationship.
- Do not let Symbol or SVG metadata define engineering truth.
- Do not collapse one Entity into one Symbol. Example: `KM1` may need coil/contact Function occurrences.
- Do not treat route paint as Relationship truth. Relationship/domain/flow/direction remain engineering
  facts.

### Current Code Reality

- Current `SceneOccurrence` has `occurrenceId`, `subjectId`, `traceId`, optional `assetId`, `ports`, and
  `labels` in
  `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationContracts.kt`.
  M44 AC asks for explicit `semanticId`; decide whether to add it or rename/clarify `subjectId`, then update
  schema and generated frontend contracts consistently.
- Current compiler lowers `SpatialOccurrenceGeometry` in
  `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaDiagramSceneCompiler.kt`.
  It currently sets `subjectId = subjectId.value`, labels from projection nodes, `assetId = null`, and traces
  mostly from semantic/port/relationship/sheet roles.
- Current compiler test still uses `examples/m43/rolling-shutter` in
  `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaDiagramSceneCompilerTest.kt`.
  Story 1.2 must move active proof to M44 ownership or add an M44-specific fixture. Do not keep M44 proof
  dependent on M43 example paths.
- Story 1.1 added:
  - `kernel/package-model/src/main/kotlin/com/engineeringood/athena/packageplatform/SymbolYamlDescriptorAdmission.kt`
  - `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/SymbolLibraryAssetAdmission.kt`
  Use those admission contracts. Do not add another YAML parser, runtime reference importer, or QElectroTech
  parser.
- Existing binding resolver is in `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/BindingResolver.kt`.
  Existing tests show `RepresentationBindingSubjectKind.DEVICE` and `FUNCTION`. If this story touches that
  API, rename `DEVICE` to `ENTITY` and update tests; Athena is pre-1.0, no backward compatibility alias.
- Existing Theia generated contract lives under
  `ide/theia-frontend/src/browser/diagram/generated/`. If scene schema changes, regenerate and update
  contract tests. The frontend should continue using published IDs only; no UI engineering validation.

### File Structure Requirements

- Expected production areas:
  - `kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/`
  - `kernel/package-model/src/main/kotlin/com/engineeringood/athena/packageplatform/`
  - `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/`
  - `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/`
  - `kernel/presentation-model/src/main/resources/schema/`
  - `ide/theia-frontend/src/browser/diagram/generated/`
- Expected tests:
  - `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/`
  - `kernel/presentation-model/src/test/kotlin/com/engineeringood/athena/presentation/`
  - `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/`
  - frontend contract tests only if generated contract changes.
- Expected fixture:
  - `examples/m44/rolling-shutter/...` or a clearly owned M44 test fixture.
- Do not put proof/demo/sample classes in production `src/main`.
- Do not use production class names containing `M44`, `Demo`, `Proof`, `Sample`, `V0`, or `V1`.

### Testing Requirements

- Start with failing tests before implementation.
- Run Gradle sequentially only.
- Minimum targeted validation:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:package-model:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:presentation-model:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
  - `.\gradlew.bat --no-daemon --console=plain test`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  - `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`
- If schema/generated frontend changes, also run the existing Theia contract generation/check command found
  in `ide/theia-frontend/scripts/`.

### Previous Story Intelligence

- Story 1.1 proved strict `symbol.yaml` / `athena-symbol-v1` admission without adding YAML dependencies.
- Descriptor digest uses canonical JSON with sorted keys and normalized numeric scalars.
- Runtime packages must not depend on `reference/elements` or `reference/elements_contrib`.
- Port compatibility envelope is not authority. Engineering source remains authoritative for Port semantics.
- File patterns established:
  - keep small related admission models together;
  - use package model for pure contracts;
  - use package runtime for resolver/admission orchestration;
  - tests mirror package model/runtime ownership.

### References

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-07-m44/prd.md`
- PRD review correction: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-07-m44/review-authoring-loop-correction.md`
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-07-m44/ARCHITECTURE-SPINE.md`
- Epics: `_bmad-output/planning-artifacts/m44/epics.md`
- Sprint status: `_bmad-output/implementation-artifacts/m44/sprint-status.yaml`
- Previous story: `_bmad-output/implementation-artifacts/m44/1-1-admit-locked-symbol-library-assets.md`

## Dev Agent Record

### Agent Model Used

GPT-5 Codex.

### Debug Log References

- Starting implementation: sprint status and story set to `in-progress`.
- CodeGraph exploration: current Function lowering and projection/spatial path goes through
  `EngineeringIrLowerer`, `AuthoredProjectionViewCompiler`, `ProjectionSpatialCoverageInventory`, and
  `AthenaDiagramSceneCompiler`.
- Red phase planned: add contract/compiler tests before changing production scene, binding, or fixture code.
- Current implementation hazards found:
  - `SceneOccurrence` has `subjectId` but no explicit `semanticId`.
  - `AthenaDiagramSceneCompiler` still emits `assetId = null`.
  - `AthenaDiagramSceneCompilerTest` still uses `examples/m43/rolling-shutter`.
  - `RepresentationBindingSubjectKind` still contains `DEVICE`; M44 must use `ENTITY` / `FUNCTION` if touched.
- Implementation: added Function-qualified occurrence parsing, Function-first trace origins, explicit
  `semanticId`, representation references, descriptor port authority conflict diagnostics, and stable
  identity tests.
- Fixture hygiene: created `examples/m44/rolling-shutter`, moved compiler and contract proof paths from
  M43 to M44, and rematerialized M44 lock digests from current source bytes.
- Validation commands passed sequentially:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:language:test --tests com.engineeringood.athena.language.ViewDeclarationParserTest --rerun-tasks`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaDiagramSceneCompilerTest`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:package-model:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:presentation-model:test`
  - `yarn workspace @engineeringood/athena-theia-frontend contracts:generate`
  - `yarn workspace @engineeringood/athena-theia-frontend contracts:check`
  - `yarn workspace @engineeringood/athena-theia-frontend build`
  - `yarn workspace @engineeringood/athena-theia-frontend test`
  - `.\gradlew.bat --no-daemon --console=plain test`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  - `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`

### Completion Notes List

- Function-level occurrences now publish explicit semantic identity and stable occurrence/trace
  identity while preserving Entity ownership as related trace.
- Symbol descriptors contribute representation references and keyed geometry compatibility only;
  authored Engineering Port direction conflicts fail closed with `symbol.port.authority.conflict`.
- `DEVICE` binding subject naming was replaced with `ENTITY` in the touched M44 package path without a
  compatibility alias.
- M44 compiler and contract fixtures no longer depend on `examples/m43`.

### File List

- `_bmad-output/implementation-artifacts/m44/1-2-compile-traceable-function-symbol-occurrences.md`
- `contracts/presentation/v1/scene/rolling-shutter.json`
- `examples/m44/rolling-shutter/README.md`
- `examples/m44/rolling-shutter/athena.yaml`
- `examples/m44/rolling-shutter/athena.lock`
- `examples/m44/rolling-shutter/src/com/engineeringood/m44/rollingshutter/rolling-shutter.athena`
- `examples/m44/rolling-shutter/src/com/engineeringood/m44/rollingshutter/rolling-shutter.sheet.athena`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaDiagramSceneCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AuthoredProjectionViewCompiler.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaDiagramSceneCompilerTest.kt`
- `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/ViewDeclarationParserTest.kt`
- `kernel/package-model/src/main/kotlin/com/engineeringood/athena/packageplatform/RepresentationBindingRuleModels.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/BindingResolverModels.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/BindingResolverSelectionTest.kt`
- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationContracts.kt`
- `kernel/presentation-model/src/main/resources/schema/athena-diagram-scene.schema.json`
- `ide/theia-frontend/scripts/generate-diagram-contracts.mjs`
- `ide/theia-frontend/src/browser/diagram/generated/schema-hash.ts`
- `ide/theia-frontend/src/browser/diagram/generated/types.ts`
- `ide/theia-frontend/src/browser/diagram/generated/validators.ts`
- `ide/theia-frontend/src/browser/diagram/generated/schema/athena-diagram-command.schema.json`
- `ide/theia-frontend/src/browser/diagram/generated/schema/athena-diagram-scene.schema.json`
- `ide/theia-frontend/src/browser/diagram/generated/schema/athena-scene-publication.schema.json`
- `ide/theia-frontend/src/browser/diagram/generated/schema/svg-safe-1.json`
- `ide/theia-frontend/src/browser/diagram/konva-diagram-adapter.ts`
- `ide/theia-frontend/src/browser/diagram/scale-benchmark.ts`

## Change Log

- 2026-08-07: Story created via BMad create-story flow for M44 Story 1.2.
- 2026-08-07: Implemented Function/Symbol occurrence identity, trace precedence, descriptor compatibility
  fail-closed behavior, and M44-owned fixture paths.
