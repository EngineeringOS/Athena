---
story_key: 1-3-author-deterministic-connection-intent
epic: m37-e1
requirements: [FR-5]
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 1.3: Author Deterministic Connection Intent

Status: review

## Story

As an engineering author or AI agent,
I want to declare how a Connection should be treated without drawing geometry,
so that routing and presentation follow explicit engineering intent rather than guesses.

## Acceptance Criteria

1. Athena source admits typed Connection Intent declarations for a Connection, route group, Interface default, and selected profile default with source spans and provenance.
2. Intent supports class, priority, separation, preferred drawing region or physical channel, route-label policy, owner, and strength without adding geometry, renderer, SVG, or ECS product facts.
3. Intent resolution is deterministic: Connection intent wins over route-group intent, which wins over Interface default, which wins over selected profile default.
4. Same-level conflicts, missing required intent, invalid class/priority/separation/region/channel/label/owner/strength values, and incompatible intent emit `connectivity.intent.*` diagnostics through compiler and LSP with correct source spans.
5. Endpoint types, SVG geometry, visual appearance, renderer state, and `ElectricalConnectionIntentClassifier` inference do not create or repair active Connection Intent.
6. ANTLR grammar, AST, tree-sitter grammar/corpus/highlighting, compiler lowering, LSP diagnostics, valid fixtures, and invalid fixtures evolve together.
7. Focused language, connection-model, compiler, tree-sitter, LSP, source-set hygiene, encoding, and diff-check gates pass sequentially.

## Tasks / Subtasks

- [x] Define Connection Intent source syntax test-first (AC: 1, 2, 6)
  - [x] Add parser/AST tests for connection-owned intent, grouped route intent, Interface default intent, and profile default intent.
  - [x] Keep intent source-owned and projection-neutral; do not add coordinates, anchors, renderer state, SVG metadata, manufacturer facts, or vendor lifecycle fields.
  - [x] Preserve existing `connect <alias> A -> B` and grouped `connect <group> { ... }` syntax.
- [x] Extend the canonical Engineering Connectivity model (AC: 1, 2, 3, 5)
  - [x] Add typed `EngineeringConnectionIntentContract` or equivalent in the existing `EngineeringConnectivityContracts.kt` family.
  - [x] Preserve class, priority, separation, preferred drawing region or physical channel, label policy, owner, strength, source span, and provenance.
  - [x] Do not create a second module, adapter, compatibility shim, or milestone-named intent model.
- [x] Implement deterministic intent resolution (AC: 3, 5)
  - [x] Resolve precedence in order: Connection, route group, Interface default, selected profile default.
  - [x] Detect same-level duplicate or conflicting declarations before any routing or presentation lowering.
  - [x] Replace active endpoint-derived intent inference for this story path; `ElectricalConnectionIntentClassifier` may remain only where tests prove it is not active authority for authored intent.
- [x] Add validation and diagnostics (AC: 4, 5)
  - [x] Reject missing required intent for visible engineering Connections.
  - [x] Reject invalid class, priority, separation, preferred region/channel, label policy, owner, and strength values.
  - [x] Reject intent inferred from endpoint type, SVG geometry, visual appearance, or renderer state.
  - [x] Publish `connectivity.intent.*` diagnostics through compiler and LSP.
- [x] Update IDE syntax support (AC: 4, 6)
  - [x] Update tree-sitter grammar, node types, corpus, fixtures, and highlighting queries for intent syntax.
  - [x] Update LSP diagnostics tests for invalid and conflicting Connection Intent.
  - [x] Update formatter/highlighting tests only where existing syntax UX owns the changed surface.
- [x] Verify and record gates (AC: 7)
  - [x] Run focused language, connection-model, compiler, tree-sitter, and LSP tests sequentially.
  - [x] Run root `test`, source-set hygiene audit, encoding audit, and `git diff --check`.
  - [x] Update Dev Agent Record, File List, Change Log, and sprint status only after evidence exists.

## Dev Notes

### Authority And Scope

- Athena source is SSOT. Connection Intent is engineering treatment intent, not route geometry, endpoint inference, SVG metadata, renderer behavior, or planner proposal data.
- Direct refactor only. Athena is pre-public: remove stale inferred active authority instead of preserving compatibility.
- Do not add full Engineering Component System fields. No manufacturer, article number, lifecycle, procurement, BOM, datasheet, simulation, or replacement facts.
- Story 1.4 owns lowering validated intent into traceable RouteFacts. Story 2.2 owns full selected Projection Policy. Story 1.3 may add the minimum profile-default hook needed for precedence tests but must not implement drawing profiles broadly.

### Current Code Intelligence

- `kernel/connection-model/src/main/kotlin/com/engineeringood/athena/connection/EngineeringConnectivityContracts.kt` is the active connectivity contract family. Extend it in place.
- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeLowering.kt` currently lowers components, Ports, Connections, functions, and Story 1.2 grouped Interface Ports into canonical Engineering IR.
- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeValidation.kt` now has source-AST validation hooks for grouped Interface diagnostics; use the same source-spanned pattern for `connectivity.intent.*`.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaProfessionalDrawingCompiler.kt` still constructs `ElectricalConnectionIntentClassifier()` and classifies intent from endpoint Port facts inside `route(...)`. M37 addendum says this active endpoint-derived path must be replaced, not kept as fallback, once authored intent exists.
- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/ElectricalConnectionIntent.kt` and `ElectricalConnectionIntentClassifier` are routing-era intent artifacts. Do not let them own M37 source intent.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/RouteIntentLowerer.kt`, `ConnectionIrLowerer.kt`, and `ConnectionIrModels.kt` are likely downstream consumers after validated intent exists, but Story 1.4 owns full route-fact trace lowering.
- `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4`, `AthenaLanguageModel.kt`, and `AthenaAntlrParseAdapter.kt` are the syntax boundary.
- `ide/tree-sitter-athena/grammar.js`, corpus, fixtures, and `queries/highlights.scm` mirror accepted syntax for syntax UX only.
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagnosticsPublishingTest.kt` already publishes `connectivity.*` diagnostics.

### Suggested Source Shape

Allowed source shape may be adjusted if parser fit demands it, but it must keep intent explicit and non-geometric:

```athena
device Drive {
  type MotorDrive
  connectivity enabled

  interface powerInput {
    intent default {
      class power
      priority high
      separation power
      region left_bus
      label policy source_tag
      owner semantic
      strength preferred
    }

    ports {
      L1 { direction in signal PowerAC role line }
      PE { direction passive signal ProtectiveEarth role protective_earth }
    }
  }
}

connect supply {
  intent {
    class power
    priority high
    channel main_power_duct
    label policy terminal_pair
    owner semantic
    strength required
  }

  drive_l1 Supply.L1 -> Drive.L1
}
```

Intent on a single flat Connection can use either a source block adjacent to the Connection or a clearly typed follow-up declaration, as long as precedence and provenance remain deterministic.

### TDD And Verification

- RED first: parser/AST, connection-model, compiler, LSP, and tree-sitter tests should fail before implementation.
- GREEN: implement minimal syntax, model, resolution, and diagnostics. Do not touch route lanes, line presentation, or renderer behavior for this story.
- Required sequential commands:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:language:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:connection-model:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
  - `npm test` under `ide/tree-sitter-athena`
  - `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test`
  - `.\gradlew.bat --no-daemon --console=plain test`
  - `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  - `git diff --check`

### Previous Story Learnings

- Story 1.2 added grouped Interface AST, lowering, validation, tree-sitter support, and LSP diagnostics.
- Component-level `interface <id>` is no longer active Interface authority; intent must not recreate that dual-authority pattern.
- Grouped Interface member Ports lower to canonical owned Ports with Interface membership and compatibility defaults/overrides.
- `EngineeringConnectivityContractCompiler` derives Interface contracts from Port membership, not component properties.
- Keep source-set hygiene clean for production `src/main`: no `Proof`, `Demo`, `Sample`, milestone names, `V0`, `V1`, or test fixtures.

### References

- [Source: `_bmad-output/implementation-artifacts/m37/epics.md` - Story 1.3]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-30-m37/prd.md` - FR-5, NFR-1, NFR-2, NFR-8]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-30-m37/addendum.md` - Connection Intent Boundary, Direct Refactor Targets]
- [Source: `_bmad-output/implementation-artifacts/m37/1-2-author-grouped-interfaces-and-ports.md` - Previous Story Learnings]

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- 2026-07-30: RED `:kernel:language:test` failed on missing Connection Intent AST fields before implementation.
- 2026-07-30: GREEN gates passed sequentially: `:kernel:language:test`, `:kernel:connection-model:test`, `:kernel:compiler:test`, `npm test` in `ide/tree-sitter-athena`, `:ide:lsp:test`, root `test`, source-set hygiene audit, encoding audit, and `git diff --check`.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Added source-owned `intent` syntax for flat Connections, grouped route intent, Interface defaults, and profile defaults.
- Added typed Engineering Connection Intent contracts and deterministic precedence: Connection, route group, Interface default, selected profile default.
- Added `connectivity.intent.*` compiler and LSP diagnostics for missing, invalid, and conflicting intent with source provenance.
- Added authored-intent branch in professional drawing routing so endpoint classifier is not active authority when source intent exists.
- Updated Tree-sitter syntax UX, corpus, generated parser artifacts, and highlighting for intent syntax.

### File List

- _bmad-output/implementation-artifacts/m37/1-3-author-deterministic-connection-intent.md
- _bmad-output/implementation-artifacts/m37/sprint-status.yaml
- extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeLowering.kt
- extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeValidation.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagnosticsPublishingTest.kt
- ide/tree-sitter-athena/grammar.js
- ide/tree-sitter-athena/queries/highlights.scm
- ide/tree-sitter-athena/src/grammar.json
- ide/tree-sitter-athena/src/node-types.json
- ide/tree-sitter-athena/src/parser.c
- ide/tree-sitter-athena/test/corpus/connect.txt
- ide/tree-sitter-athena/tree-sitter-athena.wasm
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaProfessionalDrawingCompiler.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/EngineeringConnectivityCompilationTest.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/EngineeringConnectivityNetworkCompilationTest.kt
- kernel/connection-model/src/main/kotlin/com/engineeringood/athena/connection/EngineeringConnectivityContracts.kt
- kernel/connection-model/src/test/kotlin/com/engineeringood/athena/connection/EngineeringConnectivityContractCompilerTest.kt
- kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringModel.kt
- kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4
- kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt
- kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt
- kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageParserTest.kt
- kernel/language/src/test/kotlin/com/engineeringood/athena/language/LanguageFacadeBoundaryTest.kt
- kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaDomainSemanticsModel.kt

## Change Log

- 2026-07-30: Created implementation-ready Story 1.3 from finalized M37 PRD, addendum, epics, current code intelligence, and Story 1.2 learnings.
- 2026-07-30: Implemented deterministic source-owned Connection Intent syntax, typed contract resolution, compiler/LSP diagnostics, Tree-sitter support, and verification gates.
