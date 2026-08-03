---
story_key: 5-3-expose-computed-source-and-route-evidence
epic: m37-e5
requirements: [FR-33, FR-34]
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 5.3: Expose Computed Source And Route Evidence

Status: review

## Story

As an engineer inspecting the M37 drawing,
I want every visual fact traced to its governing source and compiler decision,
so that humans and AI can explain and correct the engineering result.

## Acceptance Criteria

1. Every compiled M37 drawing occurrence, route, route label, junction/crossing marker, and sheet structure fact exposes trace evidence to applicable semantic subject, contract/interface/port, anchor, Connection Intent, lane, RouteFact, RouteLabelFact, presentation class, external evidence mapping, Projection Policy, package resource, source span, and compiler snapshot.
2. Compiler proof fields for endpoint attachment, component/label clearance, fallback absence, crossing semantics, renderer purity, and source authority are computed from compiled facts and diagnostics only.
3. Removing or corrupting a required source, route, label, class, lane, or proof fact causes the matching evidence/proof gate to fail with typed diagnostics before presentation success.
4. LSP/runtime/Theia-facing payloads carry Athena facts only; no raw planner object, SVG markup, XML payload, DOM node, renderer-inferred endpoint, or success constant crosses the protocol.
5. Focused trace/protocol/negative tests plus source-set hygiene, encoding audit, and `git diff --check` pass sequentially.

## Tasks / Subtasks

- [x] Add RED M37 trace and proof tests (AC: 1, 2, 3, 4)
  - [x] Compile `examples/m37/professional-control-drawing` and assert occurrence, route, label, marker, sheet, policy, and package-resource trace fields.
  - [x] Assert all proof fields are derived from evidence and diagnostics, not hardcoded constants.
  - [x] Add negative fixture or mutation path that corrupts one required route/source proof fact and fails the corresponding gate.
  - [x] Assert normalized protocol payloads contain Athena IDs/source spans/snapshots only.
- [x] Extend compiler evidence model and presentation payloads (AC: 1, 2, 4)
  - [x] Add trace payload model for professional drawing evidence if existing structures cannot express all required facts cleanly.
  - [x] Populate trace from compiled `PresentationGraphicOccurrence`, `RouteFactSnapshot`, `RouteLabelFact`, `DrawingProfileCompiler`, projection policy selection, and external evidence mappings.
  - [x] Keep planner graph/proposal and SVG/XML/raw markup out of transport.
  - [x] Preserve renderer purity: renderer consumes compiled Graphic Primitive IR and route facts only.
- [x] Add protocol/runtime/LSP evidence exposure (AC: 1, 4)
  - [x] Locate existing LSP/presentation payload mappers and extend current payload shape by responsibility, not milestone name.
  - [x] Ensure every route payload can explain endpoint, intent, lane, quality, class, label, and source provenance.
  - [x] Ensure source spans and compiler snapshots are stable and deterministic.
- [x] Add fail-closed evidence validation (AC: 2, 3)
  - [x] Convert missing/corrupt trace into typed diagnostics before accepted presentation output.
  - [x] Add tests for missing Connection Intent influence, missing line class, missing label evidence, missing package resource trace, and raw markup leakage.
  - [x] Do not add compatibility shims, fallback payloads, fake proof booleans, or milestone-named production classes.
- [x] Verify and record gates (AC: 5)
  - [x] Run focused M37 trace/protocol tests.
  - [x] Run `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`.
  - [x] Run relevant LSP/runtime focused tests when touched.
  - [x] Run source-set hygiene audit, encoding audit, and `git diff --check`.
  - [x] Update Dev Agent Record, File List, Change Log, story status, and sprint status only after evidence exists.

## Dev Notes

### Authority And Scope

- Athena source remains SSOT. Trace payloads may explain compiled facts; they must not become another authority path.
- This story is not the screenshot E2E. Story 5.4 owns desktop/narrow screenshots.
- Do not expose raw SVG/XML/DOM/planner-native objects. M37 evidence must be structured Athena IDs, source spans, package resource IDs, diagnostics, and compiler snapshots.
- Pre-public rule applies: if an old payload shape cannot carry current evidence safely, refactor it directly instead of adding compatibility wrappers.

### Current Code Intelligence

- Story 5.2 made `AthenaProfessionalDrawingCompiler` compute zero-defect evidence from route/profile/endpoint/label diagnostics and produce successful presentation only when gates pass.
- `AthenaProfessionalDrawingEvidence` currently holds boolean proof gates. Story 5.3 should add or attach structured trace behind those booleans so humans/AI can inspect why each proof is true.
- Route-level facts already contain `connectionId`, `routeIntentId`, `laneAssignment`, `qualityMetrics`, `source`, `target`, `segments`, `labels`, `quality`, `intentInfluence`, `compilerSnapshotId`, and provenance.
- Occurrences already carry `semanticSubjectId`, `physicalComponentId`, `functionId`, `packageId`, `definitionId`, `bindingRuleId`, terminal bindings, labels, Graphic Primitive document, and source provenance.
- Existing protocol surfaces to inspect before editing include `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationPayloads.kt`, `AthenaPresentationSessionProtocol.kt`, and presentation model normalization under `kernel/presentation-model`.

### Previous Story Intelligence

- Story 5.2 found real computed failures: `motor_down` body intersection and `motor_up`/`motor_down` label collision. Fixes were made upstream in route planning and label placement, not renderer repair.
- Full `:kernel:compiler:test`, source-set hygiene audit, encoding audit, and `git diff --check` passed after Story 5.2.
- Stale M34 professional drawing tests were aligned to current pre-1.0 rules: old samples can be regression inputs, but M37 owns current professional proof.

### Expected Diagnostics

- `drawing.trace.missing`
- `drawing.trace.source-missing`
- `drawing.trace.route-fact-missing`
- `drawing.trace.label-fact-missing`
- `drawing.trace.presentation-class-missing`
- `drawing.trace.package-resource-missing`
- `drawing.trace.raw-authority`
- `drawing.proof.constant-forbidden`

### TDD And Verification

- RED first: add a focused M37 evidence/protocol test that fails on missing structured trace or raw-authority leakage before implementation.
- Required sequential commands:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.M37ProfessionalDrawingTraceEvidenceTest`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
  - LSP/runtime focused tests when protocol/runtime files are touched
  - `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  - `git diff --check`

### References

- [Source: `_bmad-output/implementation-artifacts/m37/epics.md` - Story 5.3]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-30-m37/prd.md` - FR-33, FR-34]
- [Source: `_bmad-output/implementation-artifacts/m37/5-2-compile-the-professional-drawing-surface.md` - computed drawing gates]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaProfessionalDrawingCompiler.kt` - professional drawing compile/evidence flow]
- [Source: `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/AthenaRouteEngine.kt` - route facts and lane/intent evidence]
- [Source: `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/RouteLabelPlacementCompiler.kt` - route label bounds/collision evidence]
- [Source: `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationPayloads.kt` - LSP/presentation payload boundary]

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.M37ProfessionalDrawingTraceEvidenceTest` - passed.
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaPresentationRouteFactPayloadTest` - red first, then passed.
- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test` - passed.
- `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1` - passed.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` - passed.
- `git diff --check` - passed with existing line-ending warnings only.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Added structured professional drawing trace evidence for occurrences, routes, route labels, route markers, sheet structures, proof inputs, source spans, and forbidden authority findings.
- Trace proof gates are now derived from compiled facts and diagnostics, with fail-closed diagnostics for missing route/source/label/class/resource/proof evidence.
- LSP route payloads now expose Athena-owned route intent, lane, label, channel, compiler snapshot, quality, and source span facts without raw planner or markup authority.

### File List

- _bmad-output/implementation-artifacts/m37/5-3-expose-computed-source-and-route-evidence.md
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationPayloads.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationSessionProtocol.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationTracePayloads.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationRouteFactPayloadTest.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaProfessionalDrawingCompiler.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaProfessionalDrawingModels.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M37ProfessionalDrawingTraceEvidenceTest.kt

## Change Log

- 2026-07-31: Created implementation-ready Story 5.3 from finalized M37 PRD, epics, and Story 5.2 learnings.
- 2026-07-31: Implemented computed professional drawing trace evidence, fail-closed trace validation, and LSP route evidence payloads.
