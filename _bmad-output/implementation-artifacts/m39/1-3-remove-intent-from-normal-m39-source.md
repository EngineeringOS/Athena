---
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 1.3: Remove `intent` From Normal M39 Source

Status: done

Post-story correction: M39 later approved `->` as an exact alias for `to`. This story remains binding for `intent`: normal source must not restore connection `intent` blocks.

## Story

As an engineer,
I want engineering rules and drawing defaults to come from their proper owners,
so that project source does not repeat compiler policy.

## Acceptance Criteria

1. Active Athena source no longer accepts connection-level `intent` blocks on `connect` statements, grouped connect edges, relation statements, or maintained M39 examples.
2. Normal M39 source has no `intent`, route priority, separation, channel, label policy, owner, strength, bend-point, endpoint-fact, or renderer/paint vocabulary.
3. Relation declarations keep using `<relation> <source> to <target-or-group>` and still lower to canonical `EngineeringConnection` objects with endpoint identity, source spans, target spans, and trace intact.
4. Electrical relation contracts own compatibility, medium, separation, and physical restrictions through domain-owned properties or schema contracts, not repeated source blocks.
5. Drawing profile/package defaults own line appearance and label convention. Drawing defaults must not change engineering truth.
6. Diagnostics remain plain when a relation contract or drawing profile default is incomplete.
7. Formatter, LSP document symbols, Tree-sitter grammar/highlights, maintained tests, and active examples contain no connection `intent` syntax.
8. No compatibility path is added. Stale `intent` fixtures, docs, examples, and parser branches are deleted or rewritten to current M39 source.
9. Internal compiler/routing data may keep product concepts only if not user-authored syntax and not exposed in user-facing docs; any stale `ConnectionIntentDeclaration` source AST path must be removed or renamed to a current concept.

## Tasks / Subtasks

- [x] Establish red removal tests (AC: 1, 7, 8)
  - [x] Add parser rejection tests proving connection `intent` blocks are no longer active syntax.
  - [x] Add Tree-sitter corpus/fixture coverage proving connection `intent` no longer parses as a valid relationship child.
  - [x] Add docs/examples audit test or script assertion for active M39 paths.
- [x] Remove user-facing `intent` authoring syntax (AC: 1, 7, 8, 9)
  - [x] Remove `intentDecl` from connection and grouped connection grammar paths in `Athena.g4`.
  - [x] Remove `ConnectionIntentDeclaration` from authored connection AST if it only exists for source syntax.
  - [x] Remove adapter/formatter/LSP handling for connection-authored intent.
  - [x] Remove Tree-sitter connection intent grammar/highlight support and regenerate checked-in outputs.
- [x] Move policy ownership to domain relation contract and drawing profile defaults (AC: 4, 5, 6)
  - [x] Ensure electrical relation words expose contract evidence through the domain package/schema, not source blocks.
  - [x] Ensure line/label defaults are represented as drawing profile/package defaults or explicit deferred diagnostics, not per-connection source.
  - [x] Keep engineering truth separate from drawing profile defaults.
- [x] Rewrite maintained source/docs/tests to M39 form (AC: 2, 3, 7, 8)
  - [x] Rewrite active examples and maintained user-facing docs from connection `intent` to relation verbs or profile defaults.
  - [x] Delete stale fixtures/tests that only prove old source syntax.
  - [x] Keep only intentional negative tests proving `intent` is rejected.
- [x] Verify no compatibility residue (AC: 1-9)
  - [x] Run focused parser/compiler/LSP tests for relation source and intent rejection.
  - [x] Run Tree-sitter corpus/highlight tests and regenerate generated outputs.
  - [x] Run affected Gradle suites sequentially.
  - [x] Run source-set hygiene, encoding audit, and `git diff --check`.

## Dev Notes

### Governing Boundary

M39 source must read like engineering language:

```athena
power Supply.L1 to Breaker.1
control Drive.DO1 to Terminal.1
earth EarthBar.PE to [Motor.PE, Cabinet.PE]
drawing IECControl
```

It must not read like compiler policy:

```athena
connect x A.p to B.p intent {
  class power
  priority high
  separation power
  channel power_wireway
  label policy terminal_pair
  owner semantic
  strength required
}
```

### Current Implementation Map

| Responsibility | Current authority | Story change |
| --- | --- | --- |
| ANTLR grammar | `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4` | Remove connection-level `intentDecl` from `connectDecl`, `connectGroupEdge`, and any normal source relationship path. |
| AST | `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt` | Remove stale authored connection intent field if it only exists to preserve old source syntax. Keep relation declarations from Story 1.2. |
| Adapter | `kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt` | Stop adapting connection `intent`; keep plain parse errors. |
| Domain contracts | `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeContracts.kt` | Electrical relation words own relation contract metadata. Do not hardcode electrical words into kernel. |
| Domain lowering | `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeLowering.kt` | Relation lowering should carry `relation.kind` and any minimal domain-owned contract property needed by this story. |
| Formatter/LSP | `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectSourceFormatter.kt`, `AthenaLanguageFeatures.kt` | No formatting or symbol path should emit old connection `intent`. |
| Tree-sitter | `ide/tree-sitter-athena/grammar.js`, `queries/highlights.scm`, corpus | Remove connection `intent` validity; keep syntax UX aligned with ANTLR. |
| Examples/docs | `examples/m39/**`, active user-facing docs | Rewrite to relation verbs/profile defaults; do not keep old syntax for compatibility. |

### Previous Story Intelligence

Story 1.2 added `RelationDeclaration`, electrical domain relation words `power`, `control`, `earth`, relation lowering to canonical connections, grouped relation networks, formatter/LSP symbols, and Tree-sitter relation support.

Story 1.2 self-review found one real bug: unknown grouped relations could produce empty network shells. The fix made generic network lowering emit a network only when every domain-derived member connection exists. Preserve that guard.

### Implementation Guardrails

- `to` is preferred; `->` is allowed only as the same relation alias through one compiler path.
- No connection `intent` compatibility.
- No Java2D, second renderer, XML runtime authority, or Theia repair.
- No new route/layout/line-style source DSL.
- Do not implement M39-E2 Reality Graph here except where needed to keep ownership names clean.
- Delete stale source/tests/docs when they only exist for old syntax.
- Do not add dependencies.
- Do not put milestone names, `Proof`, `Demo`, `Sample`, `V0`, or `V1` in production names.

### Test Requirements

Run Gradle commands sequentially on Windows:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:language:test
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test
.\gradlew.bat --no-daemon --console=plain :ide:lsp:test
```

Run Tree-sitter tests if grammar/highlights/generated files changed:

```powershell
yarn --cwd ide/tree-sitter-athena test
```

Final audits:

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
git diff --check
```

### References

- [Source: `_bmad-output/implementation-artifacts/m39/epics.md` - M39-E1 Story 1.3]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-01-m39/prd.md` - FR-4 through FR-7, FR-22, NFR-1 through NFR-4]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-01-m39/addendum.md` - Language Rule and M39 Pre-Mortem Correction]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-01-m39/ARCHITECTURE-SPINE.md` - AD-1, AD-2, AD-4, AD-6]
- [Source: `_bmad-output/implementation-artifacts/m39/1-2-add-domain-connection-verbs.md` - Previous Story Intelligence]
- [Source: `AGENTS.md` - Pre-1.0 Architecture Rule, Source-Set Hygiene Rule, Build Verification Rule]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Created from M39 sprint status in order after Story 1.2 was marked done.
- Started implementation from sprint status; baseline commit preserved.
- Removed source-authored connection intent from ANTLR, AST, parse adapter, formatter/LSP, Tree-sitter, and active fixtures.
- Replaced route/presentation intent residue with `lineKind` and domain/profile-owned route facts.
- Fixed LSP warning in drawing composition payload mapping after verification exposed an unnecessary safe call.
- Fixed frontend tests that still expected stale `->` highlight captures and the old M22 flat sample path.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Connection `intent { ... }` is no longer accepted as normal source syntax; kept only negative rejection coverage.
- Relation lowering still produces canonical engineering connections with endpoint identity, spans, and trace.
- Electrical relation role/compatibility ownership stays in domain relation contracts and route/profile facts, not per-connection source blocks.
- Drawing presentation naming now uses `lineKind` instead of user-facing intent vocabulary.
- Verification passed fresh on 2026-08-01: language, connection-model, routing-model, compiler, runtime, presentation-model, svg-renderer, LSP, Tree-sitter, GLSP, Theia frontend, source-set hygiene, encoding audit, and `git diff --check`.

### File List

- AGENTS.md
- _bmad-output/implementation-artifacts/m39/1-3-remove-intent-from-normal-m39-source.md
- _bmad-output/implementation-artifacts/m39/sprint-status.yaml
- extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeContracts.kt
- extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeLowering.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaDrawingCompositionPayloads.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationPayloads.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectSourceFormatter.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagnosticsPublishingTest.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationConnectorPayloadTest.kt
- ide/theia-frontend/scripts/athena-graph-workbench-model.test.mjs
- ide/theia-frontend/scripts/athena-language-highlighting-definition.test.mjs
- ide/theia-frontend/scripts/athena-m22-baseline-proof.test.mjs
- ide/theia-frontend/scripts/athena-tree-sitter-highlighting-service.test.mjs
- ide/theia-frontend/src/browser/athena-graph-presentation-model.ts
- ide/theia-frontend/src/browser/athena-graph-workbench-edge-layer.tsx
- ide/theia-frontend/src/browser/athena-graph-workbench-model.ts
- ide/tree-sitter-athena/grammar.js
- ide/tree-sitter-athena/queries/highlights.scm
- ide/tree-sitter-athena/src/grammar.json
- ide/tree-sitter-athena/src/node-types.json
- ide/tree-sitter-athena/src/parser.c
- ide/tree-sitter-athena/test/corpus/connect.txt
- ide/tree-sitter-athena/tree-sitter-athena.wasm
- integrations/graph-glsp/src/athena-glsp-projection-adapter.ts
- integrations/graph-glsp/src/athena-glsp-projection-source.ts
- integrations/graph-glsp/lib/athena-glsp-projection-adapter.js
- integrations/graph-glsp/lib/athena-glsp-projection-source.d.ts
- integrations/graph-glsp/test/athena-graph-glsp-adapter.test.mjs
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCabinetProjectionCompiler.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ConnectionIrLowerer.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ConnectionIrModels.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/PresentationConnectorCompiler.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/PresentationModelDeriver.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/RouteIntentLowerer.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ActiveSourceSyntaxHygieneTest.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM36DedicatedCabinetSampleTest.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ConnectionIrLoweringTest.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/EngineeringConnectivityCompilationTest.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/PresentationConnectorCompilerTest.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/RouteIntentLowererTest.kt
- kernel/connection-model/src/main/kotlin/com/engineeringood/athena/connection/EngineeringConnectivityContracts.kt
- kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4
- kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt
- kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt
- kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageParserTest.kt
- kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationDocument.kt
- kernel/presentation-model/src/test/kotlin/com/engineeringood/athena/presentation/PresentationConnectorContractTest.kt
- kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/AthenaRouteEngine.kt
- kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/DrawingProfileCompiler.kt
- kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/RouteConstraintsAndFacts.kt
- kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/RouteIntentModels.kt
- kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/RouteQualityEvidence.kt
- kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/AthenaRouteEngineTest.kt
- kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/DrawingProfileCompilerTest.kt
- kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/RouteQualityEvidenceTest.kt

### Change Log

- 2026-08-01: Created story from M39 sprint plan, PRD, architecture spine, addendum, and Story 1.2 review outcome.
- 2026-08-01: Started development.
- 2026-08-01: Removed normal-source connection intent syntax and stale payload vocabulary; moved line/label ownership to relation/profile facts.
- 2026-08-01: Updated tests, active syntax fixtures, Tree-sitter generated artifacts, LSP payloads, GLSP/Theia adapters, and verification evidence.
