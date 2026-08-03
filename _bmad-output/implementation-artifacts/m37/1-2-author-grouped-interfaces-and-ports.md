---
story_key: 1-2-author-grouped-interfaces-and-ports
epic: m37-e1
requirements: [FR-2, FR-4]
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 1.2: Author Grouped Interfaces And Ports

Status: review

## Story

As an engineering author or AI agent,
I want to declare related Ports inside typed Interfaces,
so that connection surfaces are readable, reusable, and validated as coherent groups.

## Acceptance Criteria

1. Athena source admits grouped connectivity Interface declarations on governed engineering objects, with stable Interface identity, source span, type/class, defaults, member Ports, and provenance.
2. Interface defaults for direction, signal, role, multiplicity, compatibility owner, and compatibility strength apply only to member Ports that do not explicitly override that field.
3. Port overrides are source-spanned, type checked, and preserved in `EngineeringConnectivityContract` without projecting visual Anchor or SVG geometry meaning into Port semantics.
4. Duplicate Interface names, duplicate member Ports, unknown Port references, invalid defaults, invalid overrides, and incompatible group defaults emit `connectivity.interface.*` or `connectivity.port.*` diagnostics through compiler and LSP with correct source spans.
5. The old component-level `interface <id>` property is removed from active Engineering Connectivity compilation; no adapter, alias, fallback, or dual Interface authority remains.
6. ANTLR grammar, AST, tree-sitter grammar/corpus/highlighting, formatter support where applicable, compiler lowering, LSP diagnostics, valid fixtures, and invalid fixtures evolve together.
7. Focused language, connection-model, compiler, tree-sitter, LSP, source-set hygiene, encoding, and diff-check gates pass sequentially.

## Tasks / Subtasks

- [x] Define grouped Interface source syntax test-first (AC: 1, 2, 3, 6)
  - [x] Add parser/AST tests for `interface <name> { ... ports { ... } }` under a connectivity-enabled `device`.
  - [x] Support Interface type/class and default compatibility fields without adding ECS product facts.
  - [x] Support member Port declarations and references with explicit per-Port overrides.
  - [x] Keep existing `port Owner.name { ... }` syntax valid for non-grouped cases, but do not let component-level `interface` property assign every Port to one group.
- [x] Lower grouped Interfaces into canonical Engineering IR and connectivity contracts (AC: 1, 2, 3, 5)
  - [x] Extend language model and parser visitor to carry Interface declarations with source spans.
  - [x] Extend domain lowering or compiler support so member Ports retain owner, Interface membership, defaults, overrides, and provenance.
  - [x] Update `EngineeringConnectivityContractCompiler` to build `EngineeringConnectivityInterfaceContract` from grouped declarations, not component properties.
  - [x] Preserve typed compatibility owner/strength and existing semantic IDs for components, ports, connections, and networks.
- [x] Add validation and diagnostics (AC: 2, 3, 4, 5)
  - [x] Reject duplicate Interface names on one owner.
  - [x] Reject duplicate member Port names or duplicated references within one Interface.
  - [x] Reject unknown member Port references and invalid direction/multiplicity/owner/strength values.
  - [x] Reject incompatible group defaults when a required default conflicts with a required member override.
  - [x] Prove removed component-level `interface` property no longer creates Interface membership in active connectivity compilation.
- [x] Update IDE syntax support (AC: 4, 6)
  - [x] Update tree-sitter grammar, node types, corpus, incomplete-source fixtures, and highlighting queries for grouped Interface syntax.
  - [x] Update LSP diagnostics tests to publish grouped Interface and Port errors with `connectivity.interface.*` / `connectivity.port.*` codes.
  - [x] Update formatter/highlighting tests only where existing formatter or syntax UX owns the changed source surface.
- [x] Verify and record gates (AC: 7)
  - [x] Run focused language, connection-model, compiler, tree-sitter, and LSP tests sequentially.
  - [x] Run root `test`, source-set hygiene audit, encoding audit, and `git diff --check`.
  - [x] Update Dev Agent Record, File List, Change Log, and sprint status only after evidence exists.

## Dev Notes

### Authority And Scope

- Athena source is SSOT. Interface grouping is engineering connectivity meaning, not SVG, Anchor, renderer, planner, or projection meaning.
- Direct refactor only. Athena is pre-public: remove stale component-level Interface authority instead of preserving compatibility.
- Do not add full Engineering Component System fields. No manufacturer, article number, lifecycle, procurement, BOM, datasheet, simulation, or replacement facts.
- Story 1.3 owns Connection Intent. Story 2.1 owns full External Evidence Mapping syntax. Story 1.2 may keep narrow reference hooks created in Story 1.1 but must not pre-implement later stories.

### Current Code Intelligence

- `kernel/connection-model/src/main/kotlin/com/engineeringood/athena/connection/EngineeringConnectivityContracts.kt` currently derives Interface IDs from `component.properties.symbolValues("interface")` and assigns the same Interface set to every Port. Story 1.2 replaces that active authority.
- Current Port compatibility reads Port properties such as `direction`, `signal`, `role`, `multiplicity`, owner, and strength. Reuse this vocabulary for Interface defaults and Port overrides; do not invent a second compatibility schema.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt` lowers canonical components, ports, connections, and connection groups. Preserve canonical component/port/connection identity shape.
- `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4`, `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt`, and `AthenaLanguageParser.kt` are the syntax boundary.
- `ide/tree-sitter-athena/grammar.js`, `test/corpus`, `test/fixtures`, and `queries/highlights.scm` mirror accepted syntax for Theia syntax UX.
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagnosticsPublishingTest.kt` already asserts `connectivity.*` diagnostics; extend from there.

### Suggested Source Shape

```athena
device Drive {
  type MotorDrive
  connectivity enabled

  interface powerInput {
    type power
    direction in
    signal PowerAC
    role line
    multiplicity single

    ports {
      L1
      L2
      L3
      PE {
        signal ProtectiveEarth
        role protective_earth
        direction passive
      }
    }
  }
}
```

Allowed implementation may lower member names into canonical owned Ports equivalent to `port Drive.L1 { ... }`, as long as source span, Interface membership, defaults, and overrides remain traceable.

### TDD And Verification

- RED first: parser/AST and connection-model tests should fail against missing grouped Interface model.
- GREEN: implement minimal syntax, lowering, and validation. Do not touch routing or renderer for this story.
- Required sequential commands:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:language:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:connection-model:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
  - `npm test` or configured script under `ide/tree-sitter-athena` after grammar changes
  - `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test`
  - `.\gradlew.bat --no-daemon --console=plain test`
  - `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  - `git diff --check`

### Previous Story Learnings

- Story 1.1 completed direct replacement from `Connectable*` to `EngineeringConnectivity*`; build on that family only.
- Connectivity compiles once during validation and the same successful `EngineeringConnectivityCompilation` flows into backend lowering.
- `ConnectionIrLowerer` no longer accepts raw `EngineeringDocument`; do not reintroduce raw lowering or orphan route facts.
- Maintained samples explicitly use `connectivity enabled`; any sample touched by this story must remain source-owned and locks regenerated through compiler-owned materialization.
- Production `src/main` currently audits clean for legacy connectivity vocabulary, milestone labels, `Proof`, `Demo`, `V0`, and `V1` in active main paths. Keep it clean.

### References

- [Source: `_bmad-output/implementation-artifacts/m37/epics.md` - Story 1.2]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-30-m37/prd.md` - FR-2, FR-4, NFR-1, NFR-2, NFR-8]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-30-m37/addendum.md` - Engineering Connectivity Contract Boundary]
- [Source: `_bmad-output/implementation-artifacts/m37/1-1-evolve-the-connectivity-contract-in-place.md` - Previous Story Learnings]

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: `:kernel:language:test` initially failed because `DeviceDeclaration` lacked grouped Interface AST support.
- RED: `:kernel:connection-model:test` exposed component-level `interface` as stale authority.
- RED: `:kernel:compiler:test --tests com.engineeringood.athena.compiler.EngineeringConnectivityCompilationTest` exposed missing grouped Interface lowering.
- RED: grouped Interface validation test exposed missing `connectivity.interface.*` diagnostics.
- GREEN: focused and full gates passed sequentially.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Added grouped `interface { ports { ... } }` syntax to ANTLR AST and tree-sitter syntax UX.
- Lowered grouped Interface members into canonical owned Ports with Interface membership, defaults, member overrides, owner/strength compatibility, and provenance.
- Removed component-level `interface` property as active Interface authority in Engineering Connectivity compilation.
- Added grouped Interface validation diagnostics for duplicate Interfaces, duplicate member Ports, invalid fields, and required default conflicts.
- Verified focused language, connection-model, compiler, tree-sitter, LSP, root test, source-set hygiene, encoding, and diff-check gates.

### File List

- `_bmad-output/implementation-artifacts/m37/1-2-author-grouped-interfaces-and-ports.md`
- `_bmad-output/implementation-artifacts/m37/sprint-status.yaml`
- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeLowering.kt`
- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeValidation.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagnosticsPublishingTest.kt`
- `ide/tree-sitter-athena/grammar.js`
- `ide/tree-sitter-athena/queries/highlights.scm`
- `ide/tree-sitter-athena/scripts/athena-tree-sitter-symbol-highlights.test.mjs`
- `ide/tree-sitter-athena/src/grammar.json`
- `ide/tree-sitter-athena/src/node-types.json`
- `ide/tree-sitter-athena/src/parser.c`
- `ide/tree-sitter-athena/test/corpus/device.txt`
- `ide/tree-sitter-athena/test/fixtures/m37-grouped-interface.athena`
- `ide/tree-sitter-athena/tree-sitter-athena.wasm`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/EngineeringConnectivityCompilationTest.kt`
- `kernel/connection-model/src/main/kotlin/com/engineeringood/athena/connection/EngineeringConnectivityContracts.kt`
- `kernel/connection-model/src/test/kotlin/com/engineeringood/athena/connection/EngineeringConnectivityContractCompilerTest.kt`
- `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageParserTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/LanguageFacadeBoundaryTest.kt`

## Change Log

- 2026-07-30: Created implementation-ready Story 1.2 from finalized M37 PRD, addendum, epics, current code intelligence, and Story 1.1 learnings.
- 2026-07-30: Implemented grouped connectivity Interfaces and Ports with syntax, lowering, validation, tree-sitter, LSP diagnostics, and sequential verification gates.
