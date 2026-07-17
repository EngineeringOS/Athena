---
baseline_commit: 8dd23ddb8f1618ee3cef45d5c556766567434f52
---

# Story 3.3: Keep labels and cross-references readable

Status: done

## Story

As an engineer,
I want labels, terminal names, device names, and cross-references to stay readable,
so that layout intelligence improves engineering communication instead of only moving shapes.

## Acceptance Criteria

1. Given the M21 acceptance sheet, when label and cross-reference facts are produced, then they remain tied to canonical subjects, occurrences, and route or endpoint identities where applicable.
2. Given labels are placed for subjects and primary routes, when label facts are inspected, then labels avoid obvious overlap with their own subject anchors and primary route segments.
3. Given repeated runs on the same governed input, when label and cross-reference facts are produced, then label identity, anchor relation, placement, and ordering remain deterministic.
4. Given the M21 acceptance scenario, when label facts are inspected, then a reviewer can identify power source, protection, controller, terminals, and primary load path without reading implementation code.
5. Given M21 scope, when implementation is reviewed, then it does not implement frontend-owned label placement, manual pixel editing, cabinet authoring, physical routing, desktop-viewer behavior, AI layout, or final layout-stack selection.
6. Given the M21 proof baseline, when checks run, then existing layout-engine, routing-model, sample-project, graph-workbench, and Theia smoke proofs still pass.

## Tasks / Subtasks

- [x] Introduce schematic label and cross-reference facts (AC: 1, 3, 4, 5)
  - [x] Add a narrow Athena-owned label contract for label ids, label kinds, anchor refs, placements, and readable text.
  - [x] Preserve canonical subject, occurrence, snapshot, endpoint, or route identity as appropriate.
  - [x] Keep label facts separate from renderer state, frontend DOM/CSS, manual pixel edits, adapter output, and physical routing vocabulary.
- [x] Add deterministic label placement rules (AC: 2, 3, 4)
  - [x] Place subject labels relative to governed subject anchors without overlapping the anchor point.
  - [x] Place route labels relative to route segments without overlapping the segment line in the acceptance fixture.
  - [x] Emit deterministic ordering with explicit tie-breakers.
- [x] Add focused tests (AC: 1, 2, 3, 4, 5)
  - [x] Prove device, terminal, route, and cross-reference labels preserve identity.
  - [x] Prove label placements avoid their own anchor point and primary route segment in the acceptance fixture.
  - [x] Prove repeated runs produce identical label facts.
  - [x] Prove frontend-owned placement, manual pixel editing, cabinet, physical routing, desktop-viewer, AI layout, and final stack terms stay out of implementation contracts.
- [x] Validate and update story status (AC: 1, 2, 3, 4, 5, 6)
  - [x] Run `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test`.
  - [x] Run `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test`.
  - [x] Run `node --test ide/theia-frontend/scripts/athena-m21-sample-project.test.mjs`.
  - [x] Run `node --test ide/theia-frontend/scripts/athena-m21-graph-workbench-visual-proof.test.mjs`.
  - [x] Run `yarn workspace @engineeringood/athena-theia-product start:smoke:m21` from `ide/`.
  - [x] Run the encoding audit after touching docs or text assets.
  - [x] Update this story's Dev Agent Record and File List.

## Dev Notes

### Current State

- Story 3.1 added explicit schematic region facts in `:kernel:layout-engine`.
- Story 3.2 added `:kernel:routing-model` with schematic endpoint, route, lane, segment, request, snapshot, route fact, result, and rule-based route strategy contracts.
- There is no dedicated label module. Because M21 label readability depends on subject anchors and schematic route facts, Story 3.3 may extend `:kernel:routing-model` with label/cross-reference facts if that keeps the contract cohesive.

### Architectural Guardrails

- Follow M21 AD-1, AD-4, AD-7, AD-8, AD-9, AD-10, and AD-11.
- Label facts are governed layout/readability facts consumed downstream. Theia and renderer may paint them but may not own label identity or engineering placement meaning.
- Labels and cross-references must carry canonical identities needed for future source, outline, Problems, and sheet coherence.
- Do not introduce frontend-owned label placement, manual pixel editing, cabinet authoring, physical routing, desktop-viewer behavior, AI layout, or final layout-stack selection.

### Implementation Guidance

Likely update targets:

- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/SchematicRoutingModel.kt` or a new cohesive label file in the same package.
- `kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/SchematicRoutingModelTest.kt` or a new label-focused test file.
- `kernel/routing-model/README.md`.
- this story file and `sprint-status.yaml`.

Reasonable contract names include:

- `SchematicLabelId`
- `SchematicLabelKind`
- `SchematicLabelAnchor`
- `SchematicLabelPlacement`
- `SchematicLabelFact`
- `SchematicCrossReferenceFact`
- `RuleBasedSchematicLabelStrategy`

Names may vary if the implementation stays clear, deterministic, and schematic-only.

### Previous Story Intelligence

- Story 3.1 review found missing exact coverage and invalid geometry checks; apply the same rigor to labels.
- Story 3.2 review found zero-length route edge cases; label placement must avoid degenerate assumptions around anchors and route segments.
- Keep deterministic tie-breakers explicit.
- Keep the M21 graph-workbench smoke proof intact; Story 3.3 must not change accepted M20/M21 canvas behavior.

### Testing Requirements

Run checks sequentially on Windows:

- `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test`
- `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test`
- `node --test ide/theia-frontend/scripts/athena-m21-sample-project.test.mjs`
- `node --test ide/theia-frontend/scripts/athena-m21-graph-workbench-visual-proof.test.mjs`
- `yarn workspace @engineeringood/athena-theia-product start:smoke:m21`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Red phase: `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test` failed before implementation with unresolved label contract and strategy symbols.
- Green phase:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test`
- Review follow-up: added duplicate label-id rejection to preserve label identity.
- Final verification after review fix:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test`
  - `node --test ide/theia-frontend/scripts/athena-m21-sample-project.test.mjs`
  - `node --test ide/theia-frontend/scripts/athena-m21-graph-workbench-visual-proof.test.mjs`
  - `yarn workspace @engineeringood/athena-theia-product start:smoke:m21` from `ide/`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

### Completion Notes List

- Added schematic label id, kind, anchor, placement, fact, snapshot, result, and strategy contracts in `:kernel:routing-model`.
- Implemented deterministic label placement for subject anchors and route segments.
- Preserved subject, occurrence, endpoint, and route identity in label anchors.
- Added tests for device, terminal, route, and cross-reference labels; anchor and segment avoidance; deterministic repeats; acceptance readability; and deferred-scope boundaries.
- Resolved review finding by rejecting duplicate label ids.

### File List

- `_bmad-output/implementation-artifacts/m21/3-3-keep-labels-and-cross-references-readable.md`
- `_bmad-output/implementation-artifacts/m21/sprint-status.yaml`
- `kernel/routing-model/README.md`
- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/SchematicLabelModel.kt`
- `kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/SchematicLabelModelTest.kt`

## Change Log

- 2026-07-17: Created M21 Story 3.3 for schematic label and cross-reference readability.
- 2026-07-17: Implemented schematic label and cross-reference fact contracts and deterministic placement.
- 2026-07-17: Addressed review finding and marked Story 3.3 done after final verification.

## Senior Developer Review (AI)

### Review Outcome

Approved after fix.

### Findings Addressed

- [x] Label snapshots could contain duplicate label ids, weakening label identity; fixed by rejecting duplicate label ids before fact derivation.

### Verification

- `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test` passed.
- `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test` passed.
- `node --test ide/theia-frontend/scripts/athena-m21-sample-project.test.mjs` passed.
- `node --test ide/theia-frontend/scripts/athena-m21-graph-workbench-visual-proof.test.mjs` passed.
- `yarn workspace @engineeringood/athena-theia-product start:smoke:m21` passed from `ide/`.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.
