---
status: done
baseline_commit: cbe65c3fcf0fe4f05e5edfa2eb98edbe4f3e237e
story_id: 2.2
epic: 2
title: Move Source Edit Planning Behind Backend Authority
---

# Story 2.2: Move Source Edit Planning Behind Backend Authority

## Status

Done

## Story

As an engineering author,
I want all Athena source edits planned and serialized by the backend,
so that frontend code cannot become a competing language authority.

## Acceptance Criteria

1. Backend authoring planning accepts eligible semantic entity, semantic relationship, and authored-layout intents and returns admitted `.athena` edits whose insertion/replacement spans derive from the parsed `SourceFileAst` and authored `SourceSpan`s, never frontend line scanning or parser-generator offsets.
2. Every planned edit carries the exact `AuthoringRevisionGuard` used for planning, affected semantic ids, source URI, offset-based replacement span, admitted text, optional selection span, and stable diagnostics; source mismatch returns `authoring.source.conflict` and no edit.
3. Entity creation uses the selected `EngineeringConceptTemplate` as the sole source of semantic type, default model, governed properties, and nested port declarations. No hard-coded concept-to-port/type table remains in the LSP path.
4. Relationship planning validates canonical endpoint compatibility and delegates admitted connection serialization to one backend serializer. Authored-layout planning reuses `AuthoredLayoutIntentSourceSerializer`; flat and grouped relationship semantics remain unchanged for Story 2.4.
5. Theia requests backend planning and applies returned edits only through `AthenaLspEditorBridgeService`; live frontend code contains no `.athena` serializer, source-brace scan, insertion-position calculation, `buildAthenaGraphLayoutSourceEdit`, or equivalent authoring authority.
6. Existing create, update, relationship, and layout behavior remains covered through backend/LSP integration tests; stale M22/M23 tests that assert frontend serialization are migrated to assert transport-only behavior.
7. M31-CL-002 and M31-CL-005 are closed or replaced by narrower owned entries, and the mandatory Polish/Purge Gate plus AC-to-evidence mapping are complete.

## Tasks/Subtasks

- [x] Add failing compiler/authoring tests for one typed backend source-edit plan contract, exact Revision Guard echo, AST-span insertion/replacement, source conflict, and deterministic output. (AC: 1,2)
- [x] Add failing planner tests for template-driven nested entity serialization, compatible relationship serialization, and compiler-owned authored-layout serialization. (AC: 3,4)
- [x] Add failing LSP/frontend tests proving preview/accept transport backend plans and Graphical View no longer computes source positions or serializes `.athena`. (AC: 5,6)
- [x] Implement the cohesive backend source-edit planning contract and planner by extending existing compiler/LSP boundaries; reuse authored AST, source spans, Revision Guard, concept templates, relationship validator, and `AuthoredLayoutIntentSourceSerializer`. (AC: 1,2,3,4)
- [x] Route entity create/update, semantic relationship, and authored-layout source impact through the backend authority; preserve editor-bridge application as transport only. (AC: 1,3,4,5,6)
- [x] Remove component-named/hard-coded planner functions and frontend layout serialization/insertion helpers; migrate stale tests without leaving compatibility aliases. (AC: 3,5,6)
- [x] Run focused tests and full compiler, authoring-model, runtime, LSP, and frontend suites sequentially. (AC: 1,2,3,4,5,6)
- [x] Complete mandatory polish/purge review, close cleanup ledger items, and record AC-to-evidence mapping. (AC: 7)

## Dev Notes

### Architecture Guardrails

- `.athena` remains canonical semantic persistence. A source edit plan is transient authoring evidence, not semantic truth and not a second document model.
- Keep one backend authority. Extend existing compiler/LSP authoring paths; do not introduce a second parser, source writer, source mutation service, layout serializer, or frontend planner.
- Plan from the checked-in Athena AST (`SourceFileAst`, `DeviceDeclaration`, nested ports, connection/layout declarations) and authored `SourceSpan`s. ANTLR internal offsets and Tree-sitter nodes are not edit authorities.
- Use `AuthoringRevisionGuard` from Stories 1.2/2.1. The planner must compare exact source URI/version/content digest before returning an eligible plan and must echo that guard in the plan.
- Reuse `EngineeringConceptTemplate`; concept id and template id are distinct. Do not infer one by stripping suffixes.
- Reuse `AuthoredLayoutIntentSourceSerializer` in `kernel/compiler`; delete the TypeScript equivalent after backend transport is proven.
- Reuse `ElectricalSemanticRelationshipCompatibilityValidator` and existing canonical endpoint lookup. Story 2.4 owns removal of `ConnectPortsIntent` and grouped/flat migration cleanup.
- `AthenaSourceMutationRuntimeService` remains the existing compiler/source evaluation seam. Integrate or explicitly preserve it; do not duplicate its semantic diff/validation behavior.
- The LSP may translate backend byte/character offsets to LSP line/character ranges. It may not decide semantic placement or serialize source text independently.
- Theia may carry user-entered semantic values, request preview/accept, display source impact, and apply returned edits. It may not inspect source braces or create mutation text.

### Existing Code To Extend Or Replace

- `kernel/compiler/.../AuthoredLayoutIntentSourceSerializer.kt`: existing admitted layout serializer. General backend planning must call this instead of reproducing its grammar in TypeScript.
- `kernel/runtime/.../AthenaSourceMutationRuntimeService.kt`: existing source evaluation and semantic-diff seam recorded as M31-CL-002. Reuse its validation consequences where applicable.
- `ide/lsp/.../AthenaAuthoringSourceEditProtocol.kt`: currently performs component-named create planning, scans `lastIndexOf('}')`, and hard-codes concept ports/types. Replace with generic plan-to-LSP transport mapping.
- `ide/lsp/.../AthenaUpdateComponentSourceEditProtocol.kt`: AST-span-aware update logic is valuable, but its component naming and separate authority must be folded into the cohesive planner rather than cosmetically renamed.
- `ide/lsp/.../AthenaConnectPortsSourceEditProtocol.kt`: preserve canonical endpoint validation while routing source text construction through the backend planner.
- `ide/lsp/.../AthenaLanguageServer.kt`: current preview/decision switches call separate source-edit helpers. Route through one backend planning boundary and add authored-layout request/response transport.
- `ide/theia-frontend/.../athena-graph-workbench-model.ts`: remove `serializeAthenaGraphAuthoredLayoutIntent`, statement serialization, source-brace insertion resolution, indentation helper, and `buildAthenaGraphLayoutSourceEdit` after tests prove backend ownership.
- `ide/theia-frontend/.../athena-graph-workbench-widget.tsx`: `acceptLayoutMutationPreview` currently reads editor text and computes insertion location. Replace with an asynchronous backend request followed by `applyAuthoringSourceEdit` only.
- M22/M23 source-edit tests currently assert the obsolete frontend serializer. Migrate them to enforce no frontend serialization and backend-owned transport while preserving accepted user behavior.

### Planning Contract Shape

```text
BackendSourceEditPlanningRequest
  intent
  revisionGuard
  exact source text + SourceFileAst
  concept template / semantic document as required

BackendSourceEditPlan
  revisionGuard
  sourceUri
  replacement offsets
  admittedText
  selection offsets?
  affectedSemanticIds
  diagnostics
  eligibility
```

Keep the core plan offset-based and frontend-independent. Convert offsets to LSP positions only in
`ide/lsp`. Do not place LSP4J, Theia, editor, DOM, SVG, or browser types in kernel contracts.

### Testing Requirements

- Follow RED-GREEN-REFACTOR and record expected RED failures.
- Prove exact deterministic text and exact Revision Guard equality, not only non-null fields.
- Prove a changed source digest/version returns no plan and `authoring.source.conflict`.
- Prove entity output contains nested `port` declarations inside the device and no legacy top-level `port Device.name` declarations.
- Prove layout output is produced by the existing compiler serializer and parses successfully with `AthenaLanguageParser` after insertion.
- Prove frontend production files contain no admitted-source keywords/templates or insertion scanners after migration.
- Preserve create/update/relationship acceptance and active editor behavior through LSP integration tests.
- Run all Gradle commands strictly sequentially on Windows. Run the full frontend `yarn test`, `git diff --check`, and encoding audit after cleanup.

### Previous Story Intelligence

- Story 2.1 introduced generic entity intents and domain-owned semantic templates. It intentionally ledgered M31-CL-004 for Story 4.1 and M31-CL-005 for this story.
- Backend LSP now derives Revision Guard from the tracked document or canonical source; Theia no longer computes it.
- `conceptTemplateId` and `conceptId` must remain separate.
- Story 2.1 full verification found and repaired unrelated M29/M30 frontend test wiring, so full frontend regression is mandatory rather than assuming focused protocol tests are enough.
- Epic 1 established fixed validation order and `authoring.source.conflict`/stale no-write semantics; source planning must fit that transaction stage rather than bypass it.

### Scope Boundaries

- No new `.athena` syntax, parser grammar, Tree-sitter grammar, or external dependency.
- No accepted customer entity creation persistence beyond routing existing authoring decisions through the backend plan; Story 2.3 proves nested-port creation end to end.
- No removal/cascade mutation and no relationship-removal acceptance.
- No `ConnectPortsIntent` deletion or grouped-connect migration cleanup; Story 2.4 owns those after all consumers migrate.
- No frontend transaction dialog, component catalog redesign, sheet policy, representation/layout algorithm, or renderer changes.
- No direct mutation of Presentation IR, representation/projection occurrences, sheets, routes, or geometry.

### References

- `_bmad-output/implementation-artifacts/m31/epics.md` - Epic 2, Story 2.2 and FR-9/33/43.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m31/ARCHITECTURE-SPINE.md` - AD-3, AD-4, AD-5, module boundaries.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m31/AUTHORING-CONTRACT.md` - Revision Guard, Validation Order, Mutation Handoff, Frontend Adapter Contract.
- `_bmad-output/implementation-artifacts/m31/2-1-generalize-semantic-entity-authoring-contracts.md` - generic intent/template and cleanup intelligence.
- `_bmad-output/implementation-artifacts/m31/cleanup-ledger.md` - M31-CL-002 and M31-CL-005.

## Dev Agent Record

### Debug Log

- RED compiler contract: focused compilation failed because the compiler had no authoring dependency and no backend source-plan types.
- GREEN planner: create, relationship, layout, stale-source, AST-boundary, and deterministic plan tests passed after adding the compiler-owned planner.
- RED parser gate: malformed generated source initially returned a plan; the focused test failed until production planning parsed proposed source and emitted `authoring.source.invalid`.
- RED frontend ownership: tests failed because layout intent was not transported and TypeScript still serialized source/scanned the closing brace.
- GREEN transport: focused M22/M23/M31 tests passed after graph command payloads carried structured layout intent and backend source edits.
- Regression: full authoring-model, compiler, electrical extension, runtime, and LSP tests passed sequentially; full frontend suite passed 166/166.

### Completion Notes

- Added one compiler-owned offset-based source-edit planning contract with exact Revision Guard echo, AST system-boundary placement, parser validation, affected ids, selection offsets, and structured rejection diagnostics.
- Entity creation now resolves domain-owned Engineering Concept Templates and serializes nested ports; hard-coded LSP concept/type/port tables were deleted.
- Relationship planning keeps canonical electrical compatibility validation and delegates admitted text/insertion to the backend planner.
- Authored-layout planning reuses `AuthoredLayoutIntentSourceSerializer`; Theia sends structured intent, displays returned source impact, and applies only `preview.sourceEdit` through the editor bridge.
- Renamed the entity update source protocol and component-specific API/local terms while preserving its proven AST-span rewrite behavior and adding Revision Guard transport.
- AC evidence: AC-1/2 `BackendAuthoringSourceEditPlannerTest`; AC-3 entity/template tests plus LSP create regression; AC-4 relationship/layout planner and LSP tests; AC-5 `athena-m31-backend-source-authority.test.mjs`; AC-6 full LSP/frontend regressions and migrated M22/M23 tests; AC-7 CodeGraph review, no-old-name/no-frontend-serializer scans, resolved ledger entries, diff check, and encoding audit.
- Polish/Purge: removed frontend layout serializer/insertion helpers and stale tests, removed old component planner names/file, updated LSP docs, resolved M31-CL-002/005, and retained `AthenaSourceMutationRuntimeService` only as the distinct downstream dirty-source semantic-diff evaluator.

### Completion Notes

## File List

- `_bmad-output/implementation-artifacts/m31/2-2-move-source-edit-planning-behind-backend-authority.md`
- `_bmad-output/implementation-artifacts/m31/cleanup-ledger.md`
- `_bmad-output/implementation-artifacts/m31/sprint-status.yaml`
- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalEngineeringConceptTemplates.kt`
- `ide/lsp/README.md`
- `ide/lsp/README.zh-CN.md`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringSourceEditProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaConnectPortsSourceEditProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaEntityUpdateSourceEditProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaGraphCommandIntentProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaUpdateComponentSourceEditProtocol.kt` (deleted)
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringRequestTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionRequestTest.kt`
- `ide/theia-frontend/scripts/athena-m22-layout-mutation-preview.test.mjs`
- `ide/theia-frontend/scripts/athena-m22-layout-source-edit.test.mjs`
- `ide/theia-frontend/scripts/athena-m23-ide-behavior-preservation.test.mjs`
- `ide/theia-frontend/scripts/athena-m23-layout-source-edit.test.mjs`
- `ide/theia-frontend/scripts/athena-m31-backend-source-authority.test.mjs`
- `ide/theia-frontend/src/browser/athena-authoring-protocol.ts`
- `ide/theia-frontend/src/browser/athena-graph-adapter-service.ts`
- `ide/theia-frontend/src/browser/athena-graph-command-intent-protocol.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`
- `kernel/authoring-model/src/main/kotlin/com/engineeringood/athena/authoring/AuthoringTransactionModels.kt`
- `kernel/compiler/build.gradle.kts`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/BackendAuthoringSourceEditPlanner.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/BackendAuthoringSourceEditPlannerTest.kt`

## Change Log

- 2026-07-21: Ultimate context engine analysis completed; comprehensive developer guide created.
- 2026-07-22: Implemented and verified backend-only source planning for entity creation, relationships, and authored layout; purged frontend serialization authority.

## Mandatory Final Polish/Purge Gate

- Review touched and adjacent compiler serializer, AST/span, runtime evaluator, LSP planner/transport, frontend model/widget, tests, docs, and compatibility paths.
- Remove stale artifacts or ledger them with owner, reason, target milestone, and verification.
- Confirm no frontend `.athena` serializer/insertion scanner, hard-coded concept port/type table, duplicate backend planner, or unowned compatibility path remains.
- Re-run final verification after cleanup.
