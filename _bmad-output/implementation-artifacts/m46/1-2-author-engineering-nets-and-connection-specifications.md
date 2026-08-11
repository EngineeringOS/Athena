---
story: 1.2
epic: 1
title: Author Engineering Nets And Connection Specifications
status: review
baseline_commit: 9ca7c80239d38d9e2a4880886e71e27e2923c41b
created: 2026-08-10
---

# Story 1.2: Author Engineering Nets And Connection Specifications

Status: review

## Story

As an engineer,
I want first-class multi-endpoint Nets and scoped connection facts,
so that shared electrical equivalence and physical requirements are explicit.

## Acceptance Criteria

1. **Given** a Net with two or more resolved Engineering Ports **When** source parses and lowers **Then**
   one stable `EngineeringNet` retains name, closed `ConnectionKind`, endpoint membership and explicit
   `SOURCE`/`SINK`/`PASS` roles, optional potential or signal reference, typed properties, and provenance
   **And** no pairwise `EngineeringConnection` is synthesized.
2. **Given** project, potential/signal, Net, and Connection specifications **When** specification values
   resolve **Then** precedence is `project < potential-or-signal < Net < Connection` **And** resolved
   values retain winning provenance **And** two different values for one property at the same scope identify
   both authored locations and stop downstream publication.
3. **Given** `CONDUCTOR`, `WIRE`, `CABLE_CORE`, `JUMPER`, `BUSBAR`, and `SIGNAL` source cases **When**
   semantic contracts validate **Then** each kind accepts its permitted typed physical facts and enforces
   required facts defined by this story **And** incomplete or unsupported facts fail closed with exact
   subject, problem, and correction through LSP-visible diagnostics.

## Tasks / Subtasks

- [x] Define first-class Engineering Net and scoped specification models (AC: 1, 2)
  - [x] Add `EngineeringNet`, `ConnectionSpecification`, scope/reference/value contracts, and
        `EngineeringDocument.nets`/specifications in `kernel/engineering-model`.
  - [x] Enforce two-or-more distinct Port paths, unique endpoint roles only where role cardinality requires,
        nonblank stable identities/names, typed values/provenance, and no renderer/layout fields.
  - [x] Keep Net membership independent from derived branch order and never flatten a Net into binary edges.
- [x] Admit human-first Net and specification source syntax (AC: 1-3)
  - [x] Add `net <name> <kind> { source|sink|pass <port>; potential|signal <reference>; <fact> <value> }`.
  - [x] Add `connection-spec <project|potential|signal|net> [<subject>] { <fact> <value> }`; Connection-local
        facts remain the existing `connect ... { ... }` block.
  - [x] Update ANTLR AST/adapter, Tree-sitter grammar/highlights/corpus/WASM, formatter, outline, semantic
        tokens, definition/reference navigation, and malformed-source diagnostics.
- [x] Lower, resolve, and validate Nets (AC: 1, 3)
  - [x] Resolve all Net Endpoint Port paths against canonical Ports and derive stable source-based Net identity.
  - [x] Reject unresolved/repeated endpoints, fewer than two endpoints, missing kind requirements, unknown
        facts, and illegal role/direction combinations without publishing a partial Net.
  - [x] Verify all six kinds and preserve generic Engineering Relationships as non-connectivity.
- [x] Resolve specification precedence deterministically (AC: 2, 3)
  - [x] Implement one compiler-owned resolver for project, potential/signal, Net, and Connection scopes.
  - [x] Preserve winning value and provenance; reject same-scope contradictory duplicates with diagnostics at
        both source locations; identical same-scope duplicates may canonicalize to one value.
  - [x] Keep line style, stroke, dash, bend, coordinates, SVG, DOM, Konva, and viewport state outside specs.
- [x] Author tests before implementation, then red-green-refactor (AC: 1-3)
  - [x] Model/language tests for Net invariants, syntax, provenance, all roles, all six kinds, and malformed input.
  - [x] Compiler/validation tests for no flattening, fail-closed admission, precedence, provenance, contradiction,
        required/permitted facts, and deterministic stable identity.
  - [x] LSP/Tree-sitter tests for diagnostics, formatter, outline, semantic tokens, navigation, and corpus parity.
  - [x] Run Gradle tasks strictly sequentially, then encoding and source-set hygiene audits.

## Dev Notes

### Authority And Scope

```text
Athena source
  -> EngineeringConnection / EngineeringNet
  -> resolved Connection Specification
  -> Story 1.3 Connection IR
```

This story owns authored Net meaning and specification resolution only. It must not create Connection IR,
topology operators, route geometry, projection connections, Scene contracts, or UI editing operations.
Potential/signal references are authored semantic references, not paint labels. SVG/package anchors never
define membership.

### Required Source Surface

```athena
connection-spec project {
  conductorType copper
}

connection-spec signal Control24V {
  colorCode black
}

net StartCircuit signal {
  source PLC1.out
  sink KM1.coil
  pass X1.p1
  signal Control24V
  crossSection 0.75 [mm2]
}

connect wire PLC1.out to KM1.coil {
  colorCode red
}
```

No compatibility aliases. If syntax changes during red-green work, update all parser/editor surfaces in one
story and delete retired forms.

### Kind Facts

Use one closed typed vocabulary. Story 1.2 must at minimum recognize `crossSection`, `colorCode`,
`conductorType`, `shielding`, `sourceTermination`, `targetTermination`, and `requiredLength` as engineering
facts. Required facts stay minimal: `WIRE` and `CABLE_CORE` require `crossSection`; `CABLE_CORE` also
requires `conductorType`; other kinds permit facts but do not invent procurement requirements. Unknown facts
fail closed rather than becoming arbitrary paint/style properties.

### Existing Code To Reuse/Update

- `EngineeringConnectionModels.kt` and `EngineeringDocument`: extend current source-owned connection model.
- `ConnectionDeclaration` and its property block: reuse typed `PropertyAssignment`; do not duplicate value AST.
- `AthenaDomainLoweringContext.lowerProperties`: reuse exact scalar-to-`EngineeringValue` conversion.
- `EngineeringIrLowerer`: extend current Port resolution and portable source identity rules; do not add a
  second lowerer.
- `EngineeringAnatomySourceValidator` and `EngineeringIrValidator`: keep source admission separate from
  canonical invariant validation.
- `AthenaProjectSourceFormatter`, `AthenaLanguageFeatures`, Tree-sitter grammar/query/corpus: update together.

### Story 1.1 Intelligence

- Unresolved connectivity must not survive as a partial canonical fact.
- Generic `EngineeringRelationship` must never project as connection truth.
- Parser and canonical model both enforce path/identity invariants.
- Tests must prove exact semantic token classes and Tree-sitter forms, not only nonempty output.
- Every diagnostic names exact subject, problem, and correction.
- Applicable Tree-sitter test command is `ide/tree-sitter-athena/yarn test`; root `ide` has no `test` script.

### Architecture Guardrails

- Precedence is fixed: `project < potential-or-signal < Net < Connection`.
- Endpoint role stays independent from Port direction.
- Net identity derives from source unit and authored Net name, never endpoint order or route geometry.
- Same-scope contradiction is an error with both provenances. Cross-scope override is valid.
- No `.elmt`, HTML, XML, renderer, layout, compatibility, milestone, demo, proof, or sample production path.
- Story 1.3 owns canonical Connection IR and STALE/UNAVAILABLE accepted publication retention.

### Technical Stack And Verification

Kotlin 2.4.0, ANTLR 4.13.2, LSP4J 0.23.1, Tree-sitter 0.26, JUnit/Kotlin test. No new dependency.

Run sequentially:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:engineering-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:language:test
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
.\gradlew.bat --no-daemon --console=plain :kernel:validation:test
.\gradlew.bat --no-daemon --console=plain :ide:lsp:test
Set-Location ide/tree-sitter-athena
yarn test
Set-Location ../..
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
```

## References

- [Source: `_bmad-output/planning-artifacts/m46/epics.md` - Story 1.2]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-10-m46/prd.md` - FR-2, FR-4, FR-5, FR-6]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-10-m46/ARCHITECTURE-SPINE.md` - AD-1 through AD-6, AD-17]
- [Source: `_bmad-output/implementation-artifacts/m46/1-1-author-typed-binary-engineering-connections.md`]
- [Source: `draft/20260803-confuse/connections-details.md`]
- [Source: `AGENTS.md`]

## Dev Agent Record

### Agent Model Used

Codex

### Debug Log References

- Restored independent binary Connection, Net, and specification lowering pipelines after an interrupted `.map` block.
- RED compiler tests initially failed for missing/unknown connection facts; shared typed fact rules then made admission fail closed.
- Tree-sitter generation exposed optional-brace conflicts; explicit grammar conflict declarations and corpus shape update restored parity.

### Completion Notes List

- Added closed typed physical fact vocabulary and exact required/unknown/type diagnostics.
- Added source validation and canonical validation for Net endpoint resolution, role/direction, cardinality, and duplicate identity.
- Added compiler-owned specification resolver with `project < potential/signal < Net < Connection` precedence, effective properties, provenance, and contradiction blocking.
- Added formatter, document symbols, completion, semantic token classification, Tree-sitter grammar/highlights/corpus/WASM.
- Verification passed sequentially: `:kernel:engineering-model:test`, `:kernel:language:test`, `:kernel:compiler:test`, `:kernel:validation:test`, `:ide:lsp:test`, Tree-sitter `yarn test`, encoding audit, source-set hygiene audit.

Ultimate BMad context created from M46 PRD, architecture, full Epic 1 plan, current code via CodeGraph,
Story 1.1 implementation/review lessons, git context, repository rules, and EPLAN connection lessons.

### File List

- `kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringConnectionModels.kt`
- `kernel/engineering-model/src/test/kotlin/com/engineeringood/athena/ir/EngineeringNetModelsTest.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringConnectionFactRules.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ConnectionSpecificationResolver.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringAnatomySourceValidator.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/EngineeringConnectionLoweringTest.kt`
- `kernel/validation/src/main/kotlin/com/engineeringood/athena/semantics/core/EngineeringIrValidator.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectSourceFormatter.kt`
- `ide/tree-sitter-athena/grammar.js`
- `ide/tree-sitter-athena/queries/highlights.scm`
- `ide/tree-sitter-athena/test/corpus/connections.txt`
- regenerated `ide/tree-sitter-athena/src/grammar.json`, `src/node-types.json`, `src/parser.c`, and `tree-sitter-athena.wasm`

### Change Log

- 2026-08-10: Created Story 1.2 context; status `ready-for-dev`.
- 2026-08-10: Implemented Net/specification contracts, validation, precedence resolution, and editor/parser surfaces; status `review`.
