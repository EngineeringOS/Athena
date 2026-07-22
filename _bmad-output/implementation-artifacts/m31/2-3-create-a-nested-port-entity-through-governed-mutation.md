---
status: done
baseline_commit: cbe65c3fcf0fe4f05e5edfa2eb98edbe4f3e237e
story_id: 2.3
epic: 2
title: Create A Nested-Port Entity Through Governed Mutation
---

# Story 2.3: Create A Nested-Port Entity Through Governed Mutation

## Status

Done

## Story

As a controls engineer,
I want to create one semantic device from a governed concept,
so that Athena writes valid compact source and projects the new engineering entity.

## Acceptance Criteria

1. Governed creation preview for `electrical.motor.ac.default` exposes canonical tag, semantic type, model, all nested port names/directions/signals, affected semantic ids, exact revision-bound backend source edit, composition target, resolved M30 representation id, eligibility, and structured diagnostics.
2. Reject and cancel decisions call neither Mutation Authority nor reprojection and leave source, canonical model, representation, composition, and projection state unchanged.
3. Revision-current acceptance runs through the existing `SemanticAuthoringTransactionRuntime` validation order and `SemanticAuthoringMutationAuthority`, applies the validated backend source plan, recompiles successfully, and returns the created entity/port identities plus reprojected occurrence ids.
4. Accepted source contains the device and its ports only in compact nested syntax. It contains no legacy top-level `port Device.name` declarations, parses through ANTLR4, and lowers to one canonical entity with the exact nested ports after reopen/recompile.
5. Duplicate canonical tag, missing concept template, mismatched concept/template identity, invalid nested-port anatomy, unresolved representation, or unsatisfied composition target blocks preview/accept with stable authority/stage diagnostics and produces no fallback entity or generic representation success.
6. Existing LSP create-entity preview/decision flow consumes the governed preview/source plan and preserves current editor transport behavior; no frontend semantic/source authority is added.
7. Mandatory Polish/Purge Gate and AC-to-evidence mapping are complete.

## Tasks/Subtasks

- [x] Add failing authoring/runtime tests for complete governed entity-creation preview evidence, eligibility, reject/cancel no-op, and transaction acceptance handoff. (AC: 1,2,3)
- [x] Add failing compiler/LSP integration tests proving accepted motor source uses compact nested ports, recompiles to exact canonical ids, and carries representation/composition/reprojection evidence. (AC: 3,4,6)
- [x] Add failing tests for duplicate tag, missing/mismatched template, invalid nested anatomy, unresolved representation, unsatisfied composition, and stale acceptance diagnostics with zero mutation calls. (AC: 5)
- [x] Implement a cohesive governed entity-creation preview/validation service by composing existing capability evidence, transaction/runtime, concept template, backend source planner, M30 representation policy/binding facts, and composition target facts. (AC: 1,3,5)
- [x] Route accepted creation through the existing transaction mutation/reprojection authorities and LSP transport; preserve exact source-plan/revision evidence. (AC: 2,3,4,6)
- [x] Prove canonical compile/reopen identity and nested-port lowering without legacy top-level declarations or renderer fallback. (AC: 3,4,5)
- [x] Run focused tests and full authoring-model, compiler, electrical-extension, runtime, LSP, representation, and frontend suites sequentially. (AC: 1,2,3,4,5,6)
- [x] Complete mandatory polish/purge review and record AC-to-evidence mapping. (AC: 7)

### Review Findings

- [x] [Review][Patch] Route production LSP acceptance through the stored `SemanticAuthoringTransactionRuntime`, validation, Mutation Authority, compile, and reprojection path. [ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt:660]
- [x] [Review][Patch] Consume registry-discovered entity capability evidence instead of constructing empty evidence in the LSP adapter. [ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringSourceEditProtocol.kt:165]
- [x] [Review][Patch] Preserve the exact admitted entity tag; do not normalize underscores into a different canonical identity. [kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/BackendAuthoringSourceEditPlanner.kt:237]
- [x] [Review][Patch] Make preview decisions single-use so repeated or concurrent accepts cannot emit the same edit twice. [kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAuthoringSessionRuntimeService.kt:154]
- [x] [Review][Patch] Recheck the returned Revision Guard immediately before the frontend applies an editor edit. [ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts:1111]
- [x] [Review][Patch] Do not report committed/reprojected until the authoritative source mutation has actually succeeded; client-side edit failure must leave the transaction uncommitted. [ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaGovernedAuthoringExecution.kt:189]
- [x] [Review][Patch] Preserve stale/blocked transaction lifecycle in preview status instead of marking every requested accept as accepted. [kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAuthoringSessionRuntimeService.kt:160]
- [x] [Review][Patch] Make governed Revision Guard evidence mandatory at the editor boundary and return structured failure for malformed decisions. [ide/theia-frontend/src/browser/athena-authoring-revision-guard.ts:14]
- [x] [Review][Patch] Permit reject/cancel without active mutation authorities and defer source-plan application until Revision Guard validation passes. [kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAuthoringSessionRuntimeService.kt:136]
- [x] [Review][Patch] Carry the actual Engineering Concept Template id through the component catalog instead of substituting the concept id. [ide/theia-frontend/src/browser/athena-component-panel-widget.tsx:153]
- [x] [Review][Patch] Bind accepted entity-update edits to the preview Revision Guard rather than replanning against an unreviewed current document. [ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaEntityUpdateSourceEditProtocol.kt:31]
- [x] [Review][Patch] Fail authored-layout graph commands when backend planning fails instead of returning an accepted command with no durable source edit. [ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt:514]
- [x] [Review][Patch] Preflight authored-layout source planning before runtime graph placement mutation so a rejected source plan leaves projection placement unchanged. [ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt:505]

## Dev Notes

### Architecture Guardrails

- Compose Stories 1.1-1.3 and 2.1-2.2. Do not create a second capability registry, transaction runtime, Revision Guard, mutation authority, source planner, template model, representation binder, or lifecycle table.
- The canonical mutation is accepted `.athena` semantic source. Transaction and preview are transient audit/runtime state; representation/composition/projection evidence is re-derived and never mutated directly.
- Use `SemanticAuthoringTransactionFactory` and `SemanticAuthoringTransactionRuntime`; acceptance must traverse all nine validation stages. Tests must assert mutation/reprojection call counts.
- Use `BackendAuthoringSourceEditPlanner` and its parser gate. Do not reconstruct source text or insertion spans in runtime, LSP, or frontend.
- Use domain-owned `electricalEngineeringConceptTemplates()` and preserve separate `conceptTemplateId`/`conceptId` validation.
- Resolve representation through M30 policy/binding facts. A missing representation is `authoring.representation.unresolved`, not a generic box.
- Resolve composition target through policy/facts. Missing target is `authoring.composition.unsatisfied`, not arbitrary coordinates.
- Duplicate tag checks use authored AST/canonical ids before planning. No automatic rename during governed acceptance: the requested customer tag is either available or blocked. The lower-level planner's deterministic unique-name support must not hide duplicate-intent diagnostics.
- Keep LSP/Theia transport-only. The frontend applies a returned editor edit and does not infer ports, representation, composition, or identity.

### Existing Code To Extend

- `kernel/authoring-model/.../AuthoringPreviewModels.kt`: add a focused frontend-independent entity creation evidence contract or cohesive preview extension; avoid untyped maps.
- `kernel/runtime/.../SemanticAuthoringTransactionRuntime.kt`: reuse unchanged where possible; add orchestration beside it only if needed to construct the governed preview and authorities.
- `kernel/compiler/.../BackendAuthoringSourceEditPlanner.kt`: use the exact plan; add explicit duplicate-name rejection support if needed rather than silently accepting a renamed entity.
- `extensions/domain-electrical/.../ElectricalEngineeringConceptTemplates.kt`: M31 motor template is authoritative semantic anatomy.
- M30 representation policy/binding code in `kernel/representation-model`, `kernel/compiler`, and electrical extension: query existing resolved representation identity; do not duplicate symbol selection.
- `ide/lsp/.../AthenaAuthoringProtocol.kt`, `AthenaAuthoringSourceEditProtocol.kt`, and `AthenaLanguageServer.kt`: transport the governed evidence and accepted plan without constructing semantics.
- `ide/lsp/.../AthenaAuthoringRequestTest.kt`: extend the repository-backed create flow to motor nested ports and post-change canonical inspection.

### Suggested Contract Shape

```text
GovernedEntityCreationPreviewEvidence
  tag / semanticType / model
  nestedPorts
  affectedSemanticIds
  sourceEditPlan + RevisionGuard
  representationId
  compositionTarget
  eligibility + diagnostics

GovernedEntityCreationPreviewService
  intent + capability evidence + template + current model/projection facts
  -> preview evidence or structured blocked result
```

Keep representation/composition fields as ids/facts, never primitives, coordinates, SVG, bounds, or
renderer objects.

### Testing Requirements

- Follow RED-GREEN-REFACTOR and record expected RED failures.
- Use the real electrical motor template and exact expected ports `up`, `down`, and `status`.
- Prove source contains nested declarations and explicitly does not match top-level `port ShutterMotorM31.*` syntax.
- Parse planned source with `AthenaLanguageParser`; compile it with current electrical domain; inspect canonical component/port ids.
- Prove rejection, cancellation, every blocked validation, and stale acceptance call mutation/reprojection zero times.
- Prove successful acceptance calls each authority once and preserves mutation id, committed revision, affected ids, and occurrence ids.
- Run Gradle commands strictly sequentially on Windows. Run full frontend tests because LSP payload shape is consumed by Theia.
- Run CodeGraph authority review, `git diff --check`, and encoding audit after cleanup.

### Previous Story Intelligence

- Story 2.2 introduced exact revision-bound, AST-placed, parser-validated backend source plans and removed all frontend layout serialization/insertion authority.
- The planner currently supports deterministic unique names for low-level planning. This story must enforce duplicate requested tag as a governed validation failure before planning.
- Electrical templates now cover PLC, power supply, and M31 motor; the motor template owns `up`, `down`, and `status` nested ports.
- `AthenaSourceMutationRuntimeService` remains a distinct downstream dirty-source semantic diff evaluator, not a source planner.
- Full frontend regression count is 166 after Story 2.2.

### Scope Boundaries

- One accepted motor/entity creation proof only; no customer-facing catalog/dialog polish (Story 4.1).
- No accepted entity update/removal, cascade delete, bulk creation, macro insertion, or undo/redo.
- No relationship creation or `ConnectPortsIntent` removal (Story 2.4).
- No new `.athena`, ANTLR4, or Tree-sitter syntax.
- No direct document/sheet/layout/route/geometry mutation and no generic renderer fallback.

### References

- `_bmad-output/implementation-artifacts/m31/epics.md` - Epic 2, Story 2.3 and FR-7/8/9/25.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m31/ARCHITECTURE-SPINE.md` - AD-3, AD-5, AD-6, AD-9, AD-10, AD-18.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m31/AUTHORING-CONTRACT.md` - Engineering Concept Template, Validation Order, Mutation Handoff, Product Proof Contract.
- `_bmad-output/implementation-artifacts/m31/2-2-move-source-edit-planning-behind-backend-authority.md` - source authority and verification evidence.

## Dev Agent Record

### Debug Log

- RED: runtime test compilation failed because governed preview/result/evidence contracts did not exist.
- RED: compiler tests failed because entity plans reported only the component id and omitted nested-port ids.
- RED: LSP test compilation failed because governed entity evidence was absent from the preview payload.
- RED: negative matrix proved duplicate template ids were silently collapsed before the template registry was changed to fail closed.
- RED: stale LSP acceptance returned an editor edit until a revision-bound transport gate was added.
- GREEN/REFACTOR: all focused failures passed after implementation; the 343-line mixed runtime file was split into models and orchestration, false `up` to `U1` terminal binding was removed, and template support identity was centralized in the electrical adapter.

### Completion Notes

- Implemented typed governed creation evidence for canonical tag, semantic type, model, exact nested ports, affected ids, exact source edit/Revision Guard, M30 representation id, composition target, occurrence ids, eligibility, and diagnostics.
- Added `GovernedEntityCreationPreviewService` without adding a second transaction runtime, mutation authority, source planner, or representation binder. Accept/reject/cancel tests use the existing nine-stage `SemanticAuthoringTransactionRuntime` and assert authority call counts.
- Added the electrical-domain projection adapter. It loads the bundled M30 native library, resolves `iec.motor.compact` through `RepresentationBindingCompiler`, derives `composition:alignment_group` through `SchematicCompositionIntentCompiler`, and fails closed without renderer fallback.
- Backend source plans now include component and nested-port semantic ids. Accepted motor source is parser-admitted compact nested syntax and recompiles to `component:ShutterMotorM31` plus exact `up`, `down`, and `status` port ids.
- LSP preview and decision transport consume the same stored source evidence. Stale acceptance returns `authoring.preview.stale` with no source edit; blocked previews cannot be accepted. Theia carries typed evidence only and owns no planner or representation inference.
- AC evidence: AC1 `GovernedEntityCreationPreviewServiceTest.motor preview...` and LSP motor assertions; AC2 reject/cancel authority-count test; AC3 fixed-order acceptance test and LSP accepted flow; AC4 compiler nested-port identity test and LSP recompile/inspection assertions; AC5 negative matrix plus stale LSP test; AC6 full `AthenaAuthoringRequestTest` and frontend transport test; AC7 CodeGraph authority review, cleanup ledger M31-CL-006/M31-CL-007, encoding audit, scans, and post-cleanup regression.
- Final verification: `:kernel:authoring-model:test`, `:kernel:compiler:test`, `:extensions:domain-electrical:test`, `:kernel:runtime:test`, `:kernel:representation-model:test`, `:ide:lsp:test`, and frontend `yarn test` (168/168) all passed sequentially.
- Review follow-up completion: routed governed LSP acceptance through authoritative workspace mutation, added connected-editor rejection proof with versioned `TextDocumentEdit`, preserved blocked/stale lifecycle and reject/cancel no-op behavior, enforced frontend/editor Revision Guard checks, bound update edits to the preview/intent Revision Guard, and rejected authored layout commands when backend source planning produces no durable edit.
- Final review patch: added a focused LSP regression proving failed authored layout source planning does not mutate graph placement overrides, then moved source planning ahead of runtime placement mutation.
- Post-review verification: focused RED/GREEN tests for LSP relationship diagnostics, connected editor rejection, stale update edit suppression, and authored-layout source planning failure; full sequential regression passed for `:kernel:authoring-model:test`, `:kernel:interaction-model:test`, `:kernel:compiler:test`, `:extensions:domain-electrical:test`, `:kernel:runtime:test`, `:kernel:representation-model:test`, `:ide:lsp:test`, `:kernel:language:test`, frontend `yarn test` (162/162), encoding audit, `git diff --check`, CodeGraph authority pass, and stale-name scans.

## File List

- `_bmad-output/implementation-artifacts/m31/2-3-create-a-nested-port-entity-through-governed-mutation.md`
- `_bmad-output/implementation-artifacts/m31/cleanup-ledger.md`
- `_bmad-output/implementation-artifacts/m31/sprint-status.yaml`
- `extensions/domain-electrical/build.gradle.kts`
- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalEntityCreationProjectionAuthority.kt`
- `ide/lsp/build.gradle.kts`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringSourceEditProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringWorkspaceMutation.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaEntityUpdateSourceEditProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaGovernedAuthoringExecution.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringRequestTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionRequestTest.kt`
- `ide/theia-frontend/scripts/athena-m31-governed-entity-preview.test.mjs`
- `ide/theia-frontend/src/browser/athena-authoring-protocol.ts`
- `kernel/authoring-model/src/main/kotlin/com/engineeringood/athena/authoring/AuthoringPreviewModels.kt`
- `kernel/authoring-model/src/main/kotlin/com/engineeringood/athena/authoring/AuthoringTransactionModels.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/BackendAuthoringSourceEditPlanner.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/BackendAuthoringSourceEditPlannerTest.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/NativeRepresentationLibraryLoader.kt`
- `kernel/runtime/build.gradle.kts`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAuthoringSessionRuntimeService.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/GovernedEntityCreationModels.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/GovernedEntityCreationPreviewService.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/GovernedEntityCreationPreviewServiceTest.kt`

## Change Log

- 2026-07-22: Ultimate context engine analysis completed; comprehensive developer guide created.
- 2026-07-22: Implemented governed nested-port motor creation, exact revision-bound source evidence, M30 representation/composition resolution, stale acceptance protection, typed LSP/Theia transport, and mandatory deep cleanup.
- 2026-07-22: BMAD adversarial review requested changes for production transaction execution, registry-issued capability evidence, exact entity identity, single-use decisions, and apply-time Revision Guard enforcement.
- 2026-07-22: Addressed BMAD review findings for authoritative LSP mutation, connected editor rejection, update Revision Guard binding, authored layout rejection on source-plan failure, lifecycle preservation, and post-review regression evidence.

## Mandatory Final Polish/Purge Gate

- Review touched and adjacent preview, transaction, capability, template, planner, mutation, representation, composition, LSP, frontend transport, test, and documentation paths.
- Remove stale artifacts or ledger them with owner, reason, target milestone, and verification.
- Confirm no duplicate preview/runtime authority, silent unique-name fallback, legacy top-level port output, generic representation fallback, or unowned compatibility path remains.
- Re-run final verification after cleanup.
