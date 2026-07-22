---
status: done
baseline_commit: cbe65c3fcf0fe4f05e5edfa2eb98edbe4f3e237e
story_id: 2.4
epic: 2
title: Create Semantic Relationships And Retire Legacy Connect Ports
---

# Story 2.4: Create Semantic Relationships And Retire Legacy Connect Ports

## Status

Done

## Story

As a controls engineer,
I want to create one compatible semantic relationship from canonical terminals,
so that relationship truth is governed independently of route geometry and source formatting.

## Acceptance Criteria

1. Interaction/capability discovery admits relationship creation only for compatible canonical port or terminal subjects and produces one `SemanticRelationshipIntent`; no frontend, route, SVG, or DOM fact decides compatibility or canonical identity.
2. Governed relationship preview contains exact source and target semantic ids, relationship type, compatibility result, affected ids, exact revision-bound backend source edit, route preview facts, eligibility, and structured diagnostics. Route facts are transient downstream evidence and never relationship authority.
3. Reject and cancel decisions call neither Mutation Authority nor reprojection and leave source, canonical relationship collection, routing, and projection state unchanged.
4. Revision-current acceptance traverses the existing single-intent `SemanticAuthoringTransactionRuntime`, hands the validated backend source plan to existing Mutation Authority, recompiles successfully, and returns the expected canonical relationship plus reprojected route/occurrence evidence.
5. Equivalent flat and grouped `connect` source compile to the same flat canonical relationship identity and endpoints. A group name remains source provenance/organization only and never becomes relationship type, canonical hierarchy, or engineering truth.
6. Incompatible, unresolved, duplicate, self, malformed, or stale endpoint requests block before mutation with stable structured diagnostics; incompatible endpoints use `authoring.relationship.incompatible`, and every blocked path produces zero source mutation and zero reprojection calls.
7. `ConnectPortsIntent`, connect-ports-specific production transport names, compatibility conversion, duplicate tests, and stale documentation are removed after all consumers migrate. Typed relationship-removal intent, preview, dependency-impact/validation readiness remains, but no accepted removal UX is claimed.
8. Mandatory Polish/Purge Gate and AC-to-evidence mapping are complete, including closure of `M31-CL-003` and post-cleanup regression evidence.

## Tasks/Subtasks

- [x] Add failing authoring-model and interaction tests for compatible-subject capability discovery, one `SemanticRelationshipIntent`, typed relationship-removal readiness, and absence of `ConnectPortsIntent`. (AC: 1,7)
- [x] Add failing runtime tests for complete governed relationship preview evidence, route-evidence non-authority, reject/cancel no-op, validation order, and accepted mutation/reprojection handoff. (AC: 2,3,4)
- [x] Add failing compiler/domain tests for compatibility, stable diagnostics, exact backend source planning, and canonical equality of flat and grouped `connect` forms with provenance-only group identity. (AC: 2,5,6)
- [x] Add failing LSP/frontend transport tests proving relationship preview/decision uses generic semantic relationship names and typed backend evidence without frontend compatibility, identity, route, or source inference. (AC: 1,2,4,7)
- [x] Implement the cohesive governed relationship preview/validation orchestration by composing the existing capability registry, `SemanticRelationshipIntent`, transaction runtime, electrical compatibility validator, backend source planner, Mutation Authority, and route/reprojection facts. (AC: 1,2,3,4,6)
- [x] Route accepted relationship creation through the existing transaction and backend LSP authority while preserving exact preview-to-decision Revision Guard/source evidence and post-compile canonical relationship identity. (AC: 3,4)
- [x] Migrate every production/test/document consumer from `ConnectPortsIntent` and connect-ports transport names, delete compatibility conversion and obsolete files, and preserve typed relationship-removal preview/validation contracts only. (AC: 7)
- [x] Prove grouped and flat syntax canonical equivalence, provenance-only group names, incompatible/stale zero-write behavior, and route preview evidence without treating geometry as truth. (AC: 2,4,5,6)
- [x] Run focused tests and full authoring-model, compiler, electrical-extension, runtime, LSP, parser/language, and frontend suites sequentially. (AC: 1,2,3,4,5,6,7)
- [x] Complete mandatory deep polish/purge review, close `M31-CL-003`, scan for stale names/authorities/docs, rerun verification after cleanup, and record AC-to-evidence mapping. (AC: 8)

### Review Findings

- [x] [Review][Patch] Route production LSP acceptance through the stored `SemanticAuthoringTransactionRuntime`, validation, Mutation Authority, compile, and reprojection path. [ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt:660]
- [x] [Review][Patch] Consume registry-discovered relationship capability evidence instead of constructing empty evidence in the LSP adapter. [ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticRelationshipSourceEditProtocol.kt:55]
- [x] [Review][Patch] Return structured blocked diagnostics for unresolved or malformed relationship requests instead of `null`, `require`, or `error` escapes. [ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticRelationshipSourceEditProtocol.kt:40]
- [x] [Review][Patch] Resolve authored endpoint paths from the validated canonical ids and reject mismatched caller paths. [kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/GovernedRelationshipPreviewService.kt:52]
- [x] [Review][Patch] Enforce the relationship persistence target URI against the backend source document. [kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/GovernedRelationshipPreviewService.kt:70]
- [x] [Review][Patch] Make preview decisions single-use and recheck the returned Revision Guard immediately before the editor edit is applied. [kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAuthoringSessionRuntimeService.kt:154]
- [x] [Review][Patch] Preserve failure authority in relationship evidence instead of labeling every blocked preview incompatible. [kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/GovernedRelationshipPreviewService.kt:120]
- [x] [Review][Defer] Replace Graphical View's broad `port:` candidate affordance with registry-discovered compatible target evidence in Story 4.2. [ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx:1545] - deferred because Story 2.4 explicitly excludes graphical candidate UX; backend compatibility remains authoritative.
- [x] [Review][Patch] Do not report committed/reprojected until the authoritative relationship source mutation has actually succeeded. [ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaGovernedAuthoringExecution.kt:189]
- [x] [Review][Patch] Bind relationship capability evidence to both canonical endpoints, active source context/origin, and domain compatibility requirements. [ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaGovernedAuthoringExecution.kt:58]
- [x] [Review][Patch] Omit unavailable route preview or derive it from real downstream endpoint-anchor facts; do not manufacture partial route evidence. [ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticRelationshipSourceEditProtocol.kt:41]
- [x] [Review][Patch] Preserve stale/blocked lifecycle, mandatory Revision Guard enforcement, reject/cancel no-op behavior, and structured malformed-decision diagnostics across generic relationship transport. [kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAuthoringSessionRuntimeService.kt:136]
- [x] [Review][Patch] Make target capability evidence null handling explicit before validating relationship target kind and source context. [kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/GovernedRelationshipPreviewService.kt:49]

## Dev Notes

### Architecture Guardrails

- Compose the existing M29 capability/interaction model and M31 transaction/source-planning work. Do not create a second capability registry, transaction runtime, Revision Guard, relationship ontology, compatibility validator, source planner, mutation authority, route compiler, or lifecycle table.
- Canonical relationship truth is semantic and flat. Endpoints are canonical port/terminal ids; compatibility comes from domain facts. Group ids, route points, anchors, Presentation IR, SVG, DOM, and frontend state cannot define relationship identity or meaning.
- Use `SemanticRelationshipIntent` directly through the complete authoring flow. `ConnectPortsIntent` is a migration artifact owned by `M31-CL-003`; remove it only after every production and test caller has moved.
- Use `SemanticAuthoringTransactionFactory` and `SemanticAuthoringTransactionRuntime`. Acceptance must preserve the fixed nine-stage validation order and exact preview evidence. Reject, cancel, blocked, and stale paths call mutation/reprojection zero times.
- Use `BackendAuthoringSourceEditPlanner` for admitted `connect` text, AST placement, parser gate, affected ids, and Revision Guard. Runtime, LSP, and Theia must not serialize `.athena` or calculate insertion spans.
- Reuse the electrical domain's canonical compatibility validator. Do not infer compatibility from visual proximity, route availability, direction strings in TypeScript, or selected frontend tools.
- Route preview facts are optional downstream evidence. Missing or failed route preview cannot silently redefine or manufacture a relationship; accepted semantic mutation is followed by normal downstream re-derivation.
- Preserve relationship-removal contract readiness as typed intent/preview/validation and dependency-impact evidence. Accepted relationship removal UX and cascade behavior remain outside this story.

### Existing Code To Extend Or Replace

- `kernel/authoring-model/.../AuthoringIntentModels.kt`: remove `ConnectPortsIntent`; retain/complete generic `SemanticRelationshipIntent` and typed removal intent.
- `kernel/authoring-model/.../InteractionAuthoringMapping.kt`: map relationship action intent directly to `SemanticRelationshipIntent` without compatibility conversion.
- `kernel/interaction-model/.../SemanticCapabilityRegistry.kt` and `AuthoringCapabilities.kt`: discover relationship actions from canonical semantic facts and active projection occurrences.
- `kernel/runtime/.../AthenaAuthoringSessionRuntimeService.kt` and `SemanticAuthoringTransactionRuntime.kt`: migrate relationship preview/decision to the one governed transaction flow; add focused orchestration/models only when cohesion requires it.
- `kernel/compiler/.../BackendAuthoringSourceEditPlanner.kt`: keep one serializer/planner for semantic relationships and exact source evidence.
- Electrical-domain compatibility code: reuse canonical endpoint lookup and `ElectricalSemanticRelationshipCompatibilityValidator`; keep stable `authoring.relationship.incompatible` diagnostics.
- `ide/lsp/.../AthenaConnectPortsSourceEditProtocol.kt`: migrate valuable behavior to generic relationship protocol/authoring protocol and delete the legacy-named file after all callers move.
- `ide/lsp/.../AthenaAuthoringProtocol.kt`, `AthenaGraphCommandIntentProtocol.kt`, and `AthenaLanguageServer.kt`: transport generic relationship intent/preview/decision only.
- `ide/theia-frontend/.../athena-authoring-protocol.ts`, graph command protocol/model/widget, and relationship authoring model: carry typed generic payloads and display returned evidence only; do not decide compatibility or construct source.
- Existing parser/compiler grouped-connect tests: extend them to assert canonical equality and provenance-only group behavior; no grammar change is required.

### Suggested Contract Shape

```text
GovernedRelationshipPreviewEvidence
  sourceSubjectId / targetSubjectId
  relationshipType
  compatibility
  affectedSemanticIds
  sourceEditPlan + RevisionGuard
  routePreviewFacts
  eligibility + diagnostics

SemanticRelationshipIntent
  canonical endpoints + relationship type + provenance
  -> single-intent transaction
  -> validated backend source plan
  -> Mutation Authority
  -> canonical relationship
  -> re-derived route/projection evidence
```

Keep group names in source provenance only. Keep route points, anchors, bounds, and renderer objects out of the intent and canonical relationship contract.

### Testing Requirements

- Follow RED-GREEN-REFACTOR in task order and record each observed RED failure before production edits.
- Assert exact canonical source/target ids, relationship type, Revision Guard, source edit, diagnostics, affected ids, lifecycle, mutation/reprojection call counts, and returned relationship/occurrence ids.
- Compile equivalent flat and grouped fixtures through the real ANTLR4 parser/compiler and compare canonical relationship collections while separately checking group provenance.
- Cover incompatible direction/signal/media, unresolved endpoint, duplicate relationship, same endpoint, malformed intent, source conflict, and stale preview. Every blocked case must assert unchanged source and zero authorities called.
- Assert route preview is derived from terminal anchors where available but removing/changing preview geometry does not alter compatibility or canonical relationship identity.
- Use code scans and compile failures to prove no `ConnectPortsIntent`, connect-ports transport type/function/file, or compatibility converter remains after migration.
- Run Gradle commands strictly sequentially on Windows. Run the full frontend `yarn test`, language/parser regression needed by grouped syntax, `git diff --check`, and encoding audit after cleanup.

### Previous Story Intelligence

- Story 2.2 established one AST-aware, parser-admitted, revision-bound backend source planner and kept grouped/flat relationship semantics unchanged for this story.
- Story 2.3 proved one exact preview-to-decision evidence flow through the nine-stage transaction runtime and added stale LSP acceptance protection. Reuse that shape rather than creating a relationship-only lifecycle.
- The current frontend still has an M28 relationship mode/preview model. Preserve useful transient UX state, but generic backend preview is authoritative for compatibility, source impact, and acceptance eligibility.
- `M31-CL-003` explicitly requires complete migration and deletion in this story. A compatibility alias is not completion.
- Full frontend regression count was 168 after Story 2.3; the final count may change only through intentional test removal/addition and must be recorded.

### Scope Boundaries

- No new `.athena`, ANTLR4, or Tree-sitter syntax. Flat and grouped `connect` forms already exist and are only being proven equivalent.
- No accepted relationship removal UX, cascade delete, entity removal, multi-intent transaction, bulk wiring, autorouting algorithm, undo/redo, or AI planning.
- No direct mutation of route facts, Presentation IR, representation/projection occurrences, sheet membership, layout, bounds, or SVG.
- No graphical catalog/dialog/sheet workflow polish; Epic 4 owns customer-facing graphical transaction UX.
- No renderer fallback or route geometry as semantic success.

### References

- `_bmad-output/implementation-artifacts/m31/epics.md` - Epic 2, Story 2.4 and FR-13..FR-18/FR-44.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m31/prd.md` - relationship authoring requirements and NFR-1/2/11.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m31/addendum.md` - Relationship Serialization, failure taxonomy, and authority migration.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m31/ARCHITECTURE-SPINE.md` - AD-3, AD-4, AD-8, AD-10, AD-13, AD-16, AD-17, AD-18.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m31/AUTHORING-CONTRACT.md` - validation order, relationship rules, mutation handoff, frontend contract, and product proof.
- `_bmad-output/implementation-artifacts/m31/2-2-move-source-edit-planning-behind-backend-authority.md` - backend source authority and relationship planner context.
- `_bmad-output/implementation-artifacts/m31/2-3-create-a-nested-port-entity-through-governed-mutation.md` - governed preview/decision precedent.
- `_bmad-output/implementation-artifacts/m31/cleanup-ledger.md` - `M31-CL-003` removal obligation.

## Dev Agent Record

### Debug Log

- RED: authoring-model tests failed because `RemoveSemanticRelationshipIntent` did not exist and the legacy `ConnectPortsIntent` contract remained.
- RED: capability discovery admitted relationship actions without the required canonical endpoint-kind evidence.
- RED: runtime tests failed because governed relationship evidence, route-preview separation, and `authoring.relationship.incompatible` did not exist.
- RED: frontend migration tests found legacy transport names, local compatibility inference, and a direct graph-command mutation bypass.
- RED: LSP tests exposed legacy request fields and M28 smoke assumptions that invalid previews could still be accepted.
- GREEN/REFACTOR: generic relationship intent/transport, governed backend preview evidence, Revision Guard preservation, canonical grouped/flat proof, and zero-write blocked paths passed focused and full suites.
- POLISH/PURGE: removed the dead frontend relationship-authoring model and eight authority-duplicating tests, deleted the unused legacy diagnostic, consolidated duplicate LSP bypass coverage, corrected active docs, closed `M31-CL-003`, and ledgered the separate Compose migration as `M31-CL-008` for Story 4.2.
- VERIFICATION FINDING: the full language suite exposed a stale facade allow-list for the already-public `ConnectionGroupDeclaration`; the allow-list was corrected and all 52 language tests then passed.

### Completion Notes

- Implemented `SemanticRelationshipIntent` as the only active authoring contract and added typed `RemoveSemanticRelationshipIntent` preview/validation readiness without claiming accepted removal UX.
- Added `GovernedRelationshipPreviewService` with exact canonical endpoints, relationship type, compatibility, affected ids, revision-bound backend source evidence, optional route evidence, eligibility, and stable diagnostics. Route facts remain transient and non-authoritative.
- Reused the electrical compatibility validator, single backend source planner, transaction factory/runtime, Mutation Authority contract, and reprojection authority. Reject, cancel, incompatible, malformed, duplicate, self, unresolved, and stale paths are covered by zero-write/zero-reprojection assertions.
- Migrated LSP and Theia to `semantic-relationship`, preserved exact preview evidence through acceptance, and removed the immediate graph-command relationship mutation bypass and frontend-local source/compatibility authority.
- Proved real ANTLR4 flat/grouped `connect` compilation yields identical canonical relationship ids/endpoints while group names remain source organization/provenance only.
- AC evidence: AC1 authoring/interaction contract tests and generic frontend request; AC2 governed preview runtime/LSP assertions; AC3 reject/cancel authority-count tests; AC4 nine-stage runtime acceptance plus repository-backed LSP edit/recompile/route assertions; AC5 `AthenaGroupedConnectLoweringTest`; AC6 runtime negative matrix and stale LSP gate; AC7 migration source scan and deleted legacy files/model; AC8 cleanup ledger, CodeGraph authority audit, encoding audit, `git diff --check`, and post-cleanup regression.
- Final sequential verification passed: `:kernel:authoring-model:test`, `:kernel:interaction-model:test`, `:kernel:compiler:test`, `:extensions:domain-electrical:test`, `:kernel:runtime:test`, `:kernel:representation-model:test`, `:ide:lsp:test`, `:kernel:language:test` (52 tests), and frontend `yarn test` (160/160).
- Review follow-up completion: LSP relationship capability discovery now binds registered endpoint evidence without preempting runtime semantic diagnostics; incompatible/self/duplicate/source-conflict cases preserve their structured relationship authority. Governed acceptance uses authoritative workspace mutation, connected editor rejection leaves lifecycle blocked with no reprojection, route preview remains optional/null unless derived, malformed decisions return structured unavailable payloads, and reject/cancel/stale paths stay zero-write.
- Final review patch: made relationship target capability evidence null handling explicit before target-kind/source-context checks and recompiled the runtime/LSP path.
- Post-review verification: focused RED/GREEN tests for relationship diagnostic preservation and connected editor rejection; full sequential regression passed for `:kernel:authoring-model:test`, `:kernel:interaction-model:test`, `:kernel:compiler:test`, `:extensions:domain-electrical:test`, `:kernel:runtime:test`, `:kernel:representation-model:test`, `:ide:lsp:test`, `:kernel:language:test`, frontend `yarn test` (162/162), encoding audit, `git diff --check`, CodeGraph authority pass, and legacy-name scans.

## File List

- `_bmad-output/implementation-artifacts/m31/2-4-create-semantic-relationships-and-retire-legacy-connect-ports.md`
- `_bmad-output/implementation-artifacts/m31/cleanup-ledger.md`
- `_bmad-output/implementation-artifacts/m31/sprint-status.yaml`
- `docs/usages/athena-workspace-summary.md`
- `docs/usages/m29-proof-usage.md`
- `ide/lsp/README.md`
- `ide/lsp/README.zh-CN.md`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringWorkspaceMutation.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaGovernedAuthoringExecution.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaGraphCommandIntentProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticRelationshipSourceEditProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaConnectPortsSourceEditProtocol.kt` (deleted)
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringRequestTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionRequestTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaM28ProductAuthoringSmokeTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionRequestTest.kt`
- `ide/theia-frontend/src/browser/athena-authoring-protocol.ts`
- `ide/theia-frontend/src/browser/athena-graph-adapter-service.ts`
- `ide/theia-frontend/src/browser/athena-graph-command-intent-protocol.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`
- `ide/theia-frontend/src/browser/athena-relationship-authoring-model.ts` (deleted)
- `ide/theia-frontend/scripts/athena-m28-product-smoke-wiring.test.mjs`
- `ide/theia-frontend/scripts/athena-m28-relationship-authoring-model.test.mjs` (deleted)
- `ide/theia-frontend/scripts/athena-m31-semantic-relationship-migration.test.mjs`
- `kernel/authoring-model/README.md`
- `kernel/authoring-model/README.zh-CN.md`
- `kernel/authoring-model/src/main/kotlin/com/engineeringood/athena/authoring/AuthoringIntentModels.kt`
- `kernel/authoring-model/src/main/kotlin/com/engineeringood/athena/authoring/AuthoringPreviewModels.kt`
- `kernel/authoring-model/src/main/kotlin/com/engineeringood/athena/authoring/InteractionAuthoringMapping.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaGroupedConnectLoweringTest.kt`
- `kernel/interaction-model/src/main/kotlin/com/engineeringood/athena/interaction/InteractionModels.kt`
- `kernel/interaction-model/src/main/kotlin/com/engineeringood/athena/interaction/SemanticCapabilityRegistry.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/LanguageFacadeBoundaryTest.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAuthoringSessionRuntimeService.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/GovernedRelationshipModels.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/GovernedRelationshipPreviewService.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/GovernedRelationshipPreviewServiceTest.kt`
- `ui/compose-workbench/src/commonMain/kotlin/com/engineeringood/athena/composeruntime/AthenaComposeShellState.kt`

## Change Log

- 2026-07-21: Ultimate context engine analysis completed; comprehensive developer guide created.
- 2026-07-22: Implemented governed semantic relationship preview/acceptance, removed legacy relationship authoring authorities and names, proved grouped/flat canonical equivalence, completed deep purge, and recorded fresh regression evidence.
- 2026-07-22: BMAD adversarial review requested changes for production transaction execution, registry-issued capabilities, structured failures, canonical path/URI binding, single-use decisions, apply-time Revision Guard enforcement, and accurate blocked evidence.
- 2026-07-22: Addressed BMAD review findings for relationship capability evidence, structured diagnostics, authoritative mutation/reprojection lifecycle, route-preview non-authority, malformed decisions, and post-review regression evidence.

## Mandatory Final Polish/Purge Gate

- Review touched and adjacent intent, capability, interaction, compatibility, transaction, planner, mutation, routing, LSP, frontend transport, test, and documentation paths.
- Remove stale artifacts or ledger them with owner, reason, target milestone, and verification; `M31-CL-003` must be resolved in this story, not deferred again.
- Confirm no `ConnectPortsIntent`, connect-ports transport naming, compatibility adapter, duplicate relationship authority, frontend semantic/source inference, group-as-truth, or route-as-truth remains.
- Preserve only typed relationship-removal preview/validation readiness and state clearly that accepted removal UX is deferred.
- Re-run all final verification after cleanup and record AC-to-evidence mapping.
