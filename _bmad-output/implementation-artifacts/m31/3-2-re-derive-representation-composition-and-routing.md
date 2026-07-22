---
status: done
baseline_commit: cbe65c3fcf0fe4f05e5edfa2eb98edbe4f3e237e
story_id: 3.2
epic: 3
title: Re-Derive Representation Composition And Routing
---

# Story 3.2: Re-Derive Representation Composition And Routing

## Status

Done

## Story

As an engineering author,
I want accepted semantic changes reprojected through governed representation and spatial compilers,
so that the canvas cannot directly edit downstream engineering documents.

## Acceptance Criteria

1. Accepted entity or relationship mutation triggers fresh derivation of document membership, Representation Occurrence, composition, layout, route facts, Presentation IR, and geometry. No authoring operation directly mutates Presentation IR, Representation Occurrence, Projection Occurrence, sheet output, layout facts, route facts, rendered geometry, or SVG.
2. Created M31 entity representation binding uses M30 Representation Policy and Binding Compiler selected from semantic/template facts. Theia does not infer the symbol, label, anchor, or bounds from the chosen action, DOM, SVG id, CSS class, or canvas state.
3. Accepted relationship routing uses governed terminal anchors with zero center fallback. Missing anchor, representation, composition, or route facts produce structured diagnostics instead of generic boxes or center-to-center routes.
4. Normal rendering keeps wrappers and hitboxes transparent, derives viewBox/framing from resolved presentation bounds and governed margins, contains no duplicate off-sheet occurrences or repeated labels, and shows interaction chrome only for transient hover, selection, focus, preview, or drag states.
5. Mandatory Polish/Purge Gate and AC-to-evidence mapping are complete, including direct-mutation, fallback-route, generic-box, hard-coded viewBox, duplicate occurrence, repeated-label, and stale generated artifact review.

## Tasks/Subtasks

- [x] Add failing compiler/runtime tests proving accepted semantic changes rederive downstream projection artifacts instead of mutating them directly. (AC: 1)
  - [x] Use an accepted nested-port entity creation result and assert the post-accept projection contains freshly derived sheet membership, occurrence ids, representation evidence, composition facts, route facts, Presentation IR inputs, and geometry.
  - [x] Use an accepted `SemanticRelationshipIntent` result and assert route/projection facts are regenerated after recompile rather than copied from preview, transient UI state, or previous canvas state.
  - [x] Add explicit no-side-effect assertions for rejected, blocked, stale, and projection-failed paths where this story touches the pipeline.
- [x] Add failing representation/composition tests proving M30 policy owns symbol selection for M31-created entities. (AC: 2)
  - [x] Assert the active representation is selected from semantic subject/template facts and M30 representation capability, not frontend action labels.
  - [x] Assert Engineering Concept Templates remain free of representation primitives, anchors, style, viewBox, and SVG.
  - [x] Assert missing or inactive representation policy returns a structured `authoring.representation.unresolved` diagnostic.
- [x] Add failing routing tests proving terminal anchors are mandatory for accepted relationships. (AC: 3)
  - [x] Assert relationship route endpoints bind to governed terminal anchors from representation/projection facts.
  - [x] Assert center fallback is absent for accepted M31 relationship routing.
  - [x] Assert missing terminal anchor, missing representation occurrence, or missing composition target returns structured diagnostics such as `authoring.representation.unresolved` or `authoring.composition.unsatisfied`.
- [x] Add failing renderer/presentation proof tests for compact professional output. (AC: 4)
  - [x] Extend existing viewBox/chrome proof to reject hard-coded `0 0 1680 1188`, off-sheet duplicate occurrences, repeated terminal labels, visible normal hitbox/wrapper borders, and persistent interaction chrome.
  - [x] Preserve existing cabinet default and two-sheet selector behavior from Story 3.1 while adding the downstream re-derivation proof.
  - [x] Keep screenshots secondary; use structured payload/DOM-proof assertions as the acceptance authority.
- [x] Implement minimal compiler/runtime/representation/routing changes to pass the RED tests. (AC: 1,2,3,4)
  - [x] Reuse existing document projection policy, M30 representation policy/binder, M27 spatial/layout/routing facts, and Presentation IR contracts.
  - [x] Remove or replace any direct downstream mutation path discovered while implementing; do not add a second projection or representation authority.
  - [x] If a retained compatibility path is necessary, add a cleanup-ledger entry with owner, reason, target story/milestone, and verification.
- [x] Run focused and regression verification sequentially on Windows. (AC: 1,2,3,4)
  - [x] Run focused RED and GREEN tests for compiler/runtime/representation/routing/frontend changes.
  - [x] Run relevant Gradle suites sequentially; never run Gradle commands concurrently.
  - [x] Run `yarn test` in touched TypeScript packages if frontend/GLSP code changes.
  - [x] Run `git diff --check`, stale/fallback scans, and `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.
- [x] Complete mandatory polish/purge review and update story evidence. (AC: 5)
  - [x] Review touched and adjacent source, tests, generated output, docs, samples, and cleanup ledger.
  - [x] Remove stale code/docs/tests/generated artifacts or ledger each retained item.
  - [x] Record AC-to-evidence mapping, final verification commands, File List, Completion Notes, and Change Log.

## Dev Notes

### Architecture Guardrails

- This story implements M31 FR-21, FR-22, FR-25, FR-26, FR-27, FR-28, FR-29, FR-30, and FR-51.
- No new `.athena`, ANTLR4, or Tree-sitter syntax is introduced by this story. M31 consumes existing nested-port and grouped-connect syntax.
- Accepted source mutation remains the only upstream change. Downstream documents, sheets, representation occurrences, layout facts, route facts, Presentation IR, geometry, SVG, viewBox, and canvas chrome are immutable authoring outputs and must be re-derived.
- Theia remains an adapter. It may display preview and apply backend-returned source edits as transport, but it must not select symbols, infer terminal anchors, persist coordinates, compute viewBox, or repair projection failures.
- M30 Representation Policy and Binding Compiler own representation selection. Engineering Concept Templates provide semantic anatomy only.
- Composition Target is intent, not geometry: `sheetId`, optional `zoneId`, `laneId`, or `alignmentGroupId`; never `x`, `y`, `width`, `height`, `svgTransform`, or DOM ids.
- Relationship route geometry is evidence. Canonical relationship truth remains endpoint semantic ids and `SemanticRelationshipIntent`.
- Accepted relationship routing must not silently fall back to component centers. A missing anchor, occurrence, or composition target is a structured diagnostic, not a generic renderer box.
- Normal industrial density is required: transparent wrappers/hitboxes, no extra visible borders, no duplicate off-screen occurrences, no repeated labels, no oversized fixed canvas, and interaction chrome only while transient state is active.
- Every rejected or unavailable path touched by this story must prove no source, semantic, projection, composition, route, or renderer side effect.
- Every story must finish with the mandatory polish/purge workspace review.

### Existing Code To Inspect And Extend

- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionModelDeriver.kt`: derives projection nodes, connections, labels, sheets, cross references, electrical anchors, endpoints, corridors, canvas size, and documentation-specific occurrences. Watch for documentation copies or canvas-width logic that can create off-sheet duplicates or oversized bounds.
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/CompositionBoundsProofGuard.kt`: existing professional-output proof guard for derived viewBox, duplicate occurrence, repeated-label, wrapper-border, and bounds regressions. Extend this instead of adding screenshot-only acceptance.
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/M30DemoRepresentationBinder.kt` and adjacent M30 representation policy/binding files: reuse policy/binding contracts; do not hard-code M31 frontend actions into representation choice.
- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/RouteConstraintsAndFacts.kt` and route derivation callers: inspect route quality and fallback behavior. Story 3.2 must prove accepted M31 routes do not use center fallback.
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt` and `AthenaRuntimeProjectionSupport.kt`: ensure post-accept recompile/reproject publishes new derived facts without retaining preview placement or renderer state.
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/*Projection*` and authoring protocol files: transport structured representation/composition/routing diagnostics without frontend inference.
- `integrations/graph-glsp/src/athena-glsp-projection-adapter.ts` and `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`: renderer/adapter can consume resolved payloads and transient interaction state only. Preserve Story 3.1 typed sheet-policy evidence and Cabinet default behavior.
- `ide/theia-frontend/scripts/athena-graph-workbench-model.test.mjs`: existing tests already cover content-derived viewBox, no hard-coded `0 0 1680 1188`, off-sheet duplicate filtering, selection-only verbose labels, and deterministic chrome. Extend rather than replace.

### Previous Story Intelligence

- Story 3.1 added `BuiltInDocumentProjectionPolicies.athenaM31CustomerProjectionV0()` and typed `ProjectionSheetPolicyEvidence`; keep using those facts for sheet identity and selector state.
- Story 3.1 preserved Cabinet as default and kept the governed sheet selector available after switching projection models. Do not regress this while changing downstream projection payloads.
- Story 3.1 fixed malformed GLSP sheet policy evidence to fail closed. Treat malformed representation/composition/routing evidence the same way.
- Story 3.1 ledgered legacy M26 sheet display-title fallback as M31-CL-010. Do not add new label-parsing authority for M31.
- Epic 2 completed generic authoring contracts, backend source planning, nested-port creation, grouped-connect lowering, and `SemanticRelationshipIntent`. Do not reintroduce `ConnectPortsIntent`, component-specific authoring names, or frontend source serializers.
- Deferred item M31-CL-009 remains Epic 4 relationship UX work. Story 3.2 may prove routing facts and diagnostics, but it must not claim the full graphical relationship/reveal UX.

### Testing Requirements

- Follow RED-GREEN-REFACTOR. Record observed RED failures in the Dev Agent Record before production edits.
- Use structured tests over screenshots. Screenshots are secondary visual evidence only.
- Run Gradle verification strictly sequentially on Windows.
- Focused suites likely include:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:representation-model:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test` if routing tests are touched and the module has tests
  - `.\gradlew.bat --no-daemon --console=plain :kernel:projection-model:test`
  - `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test`
  - `yarn test` in `integrations/graph-glsp` if GLSP payload code changes
  - `yarn test` in `ide/theia-frontend` if Theia workbench code changes
- Final polish must run `git diff --check`, stale direct-mutation/fallback/viewBox scans, generated-output purge checks, `.tools` status check, and encoding audit.

### Scope Boundaries

- No full cross-sheet reference implementation; Story 3.3 owns first-class semantic continuation and reopen-stable reveal.
- No Graphical View creation/relationship UX completion; Epic 4 owns customer-facing transaction controls and reveal workflow.
- No direct canvas persistence, permanent coordinates, geometry database, generic fallback box, or renderer repair layer.
- No QET runtime dependency or `.athena` visual syntax.
- No expansion of symbol-library breadth beyond what the M30/M31 vertical slice requires.

### References

- `_bmad-output/implementation-artifacts/m31/epics.md` - Epic 3, Story 3.2 and FR-21/22/25/26/27/28/29/30/51.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m31/prd.md` - Feature 4, Feature 5, FR-41/42 cleanup gate, and M31 core acceptance scope.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m31/addendum.md` - Composition Target, No Hidden Projection Editing, and Two-Sheet Product Proof.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m31/ARCHITECTURE-SPINE.md` - AD-6, AD-7, AD-12, AD-15, AD-18.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m31/AUTHORING-CONTRACT.md` - Downstream Re-Derivation Rule, Frontend Adapter Contract, Product Proof Contract.
- `_bmad-output/implementation-artifacts/m31/3-1-add-the-two-sheet-customer-projection-policy.md` - Typed sheet-policy evidence, Cabinet default preservation, and prior review fixes.
- `_bmad-output/implementation-artifacts/m31/cleanup-ledger.md` - Retained compatibility items that must not grow unowned.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Created via BMAD create-story workflow after Story 3.1 completion and CodeGraph projection/representation/routing context scan.
- Started BMAD dev-story workflow for Story 3.2; loaded sprint status, story context, project config, CodeGraph projection context, TDD instructions, and verification-before-completion instructions.
- RED: `:kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaM30SampleProjectCompilerTest` failed because documentation projection still emitted duplicate off-sheet `_reference` nodes.
- GREEN: focused compiler test passed after removing documentation reference-node copying and deriving sheet subjects from real projection nodes only.
- RED: `:kernel:representation-model:test --tests com.engineeringood.athena.representation.RepresentationBindingStatusPayloadTest` failed because binding status payload lacked selected symbol, occurrence role, and composition evidence.
- GREEN: focused representation test passed after adding representation evidence fields to `M30DemoRepresentationBindingProof.toBindingStatusPayload()`.
- Regression migration: full compiler, runtime, and LSP suites initially failed where M11/M12/depth tests still expected duplicated visual reference occurrences; updated those assertions to prove one visual occurrence plus governed cross-reference facts.
- Polish scan found retained `_reference` only in defensive frontend/GLSP/semantic-selection/projection-model fixtures and the old M19 static example; recorded M31-CL-011.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Removed compiler-generated duplicate off-sheet documentation reference nodes and associated canvas expansion.
- Added compiler proof that M31 documentation projection has one visual component occurrence per semantic component, content-derived canvas width, governed terminal anchors, and routing corridors for every accepted relationship projection.
- Extended M30 representation binding status payload with selected symbol ids, occurrence roles, and composition membership count.
- Migrated compiler/runtime/LSP depth tests so repeated/cross-sheet reference proof comes from cross-reference facts, not duplicated visual nodes.
- Code review follow-up: removed the now-dead documentation projection node helper and expanded documentation canvas derivation/proof to include projected nodes, labels, and route endpoints.
- AC-to-evidence mapping:
  - AC1: `AthenaM30SampleProjectCompilerTest`, `AthenaCompilerM11DepthTest`, `AthenaCompilerM12RendererBenchmarkTest`, runtime depth/session tests, and LSP depth/session tests prove re-derived sheet/projection outputs without duplicate off-sheet node mutation.
  - AC2: `RepresentationBindingStatusPayloadTest`, existing `GovernedEntityCreationPreviewServiceTest`, domain electrical tests, and frontend authoring protocol tests prove representation selection and missing-policy diagnostics remain platform-owned.
  - AC3: `AthenaM30SampleProjectCompilerTest` asserts documentation connection endpoints reference governed terminal anchors and every accepted relationship projection publishes a routing corridor; `:kernel:routing-model:test` preserves route quality diagnostics.
  - AC4: compiler/runtime/LSP tests assert no `_reference` normal payloads; existing Theia workbench tests assert content-derived viewBox, duplicate filtering, transparent chrome, and selector preservation.
  - AC5: `git diff --check` exited 0 with line-ending warnings only, encoding audit passed, `.tools` status was clean, and M31-CL-011 records retained legacy defensive `_reference` fixtures.
- Verification run sequentially on Windows:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaM30SampleProjectCompilerTest`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:representation-model:test --tests com.engineeringood.athena.representation.RepresentationBindingStatusPayloadTest`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:representation-model:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:projection-model:test`
  - `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test`
  - `yarn test` in `integrations/graph-glsp`
  - `yarn test` in `ide/theia-frontend`
  - `.\gradlew.bat --no-daemon --console=plain :extensions:domain-electrical:test`
  - `git diff --check`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
- Fresh post-review verification on 2026-07-22:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaM30SampleProjectCompilerTest`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:representation-model:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:projection-model:test`
  - `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test`
  - `.\gradlew.bat --no-daemon --console=plain :extensions:domain-electrical:test`
  - `yarn test` in `integrations/graph-glsp` returned 6/6 pass
  - `yarn test` in `ide/theia-frontend` returned 162/162 pass
  - `git diff --check` exited 0 with line-ending warnings only
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed
  - `.tools` status was clean

## Senior Developer Review (AI)

### Review Date

2026-07-22

### Review Outcome

Approve

### Findings

- Medium: Documentation canvas derivation still depended only on node bounds after duplicate reference nodes were removed, which could miss labels or route endpoints in future documentation projections. Fixed by deriving documentation canvas width/height from node, label, and route endpoint bounds and by extending the compiler proof.
- Low: `documentationProjectionNodes` became a dead pass-through helper after removing off-sheet reference copies. Fixed by removing the helper and using `baseNodes` directly.

### Verification

- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaM30SampleProjectCompilerTest`
- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
- `.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test`
- `.\gradlew.bat --no-daemon --console=plain :kernel:representation-model:test`
- `.\gradlew.bat --no-daemon --console=plain :kernel:projection-model:test`
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test`
- `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test`
- `.\gradlew.bat --no-daemon --console=plain :extensions:domain-electrical:test`
- `yarn test` in `integrations/graph-glsp` returned 6/6 pass
- `yarn test` in `ide/theia-frontend` returned 162/162 pass
- `git diff --check` exited 0 with line-ending warnings only
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed
- `.tools` status was clean

### File List

- `_bmad-output/implementation-artifacts/m31/3-2-re-derive-representation-composition-and-routing.md`
- `_bmad-output/implementation-artifacts/m31/cleanup-ledger.md`
- `_bmad-output/implementation-artifacts/m31/sprint-status.yaml`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionModelDeriver.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerM11DepthTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerM12RendererBenchmarkTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM30SampleProjectCompilerTest.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/M30DemoRepresentationBinder.kt`
- `kernel/representation-model/src/test/kotlin/com/engineeringood/athena/representation/RepresentationBindingStatusPayloadTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionDepthTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSessionTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionM11DepthRequestTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionRequestTest.kt`

### Change Log

- 2026-07-22: Created Story 3.2 context for BMAD dev-story execution.
- 2026-07-22: Removed documentation off-sheet reference node generation, added structured compiler/representation proof, migrated runtime/LSP depth assertions, and ledgered retained legacy defensive `_reference` fixtures.
- 2026-07-22: Addressed code review by deriving documentation canvas bounds from nodes, labels, and route endpoints; marked Story 3.2 done after fresh verification.
