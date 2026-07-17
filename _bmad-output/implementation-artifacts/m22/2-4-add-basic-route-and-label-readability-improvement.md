---
baseline_commit: 0b43cbe
---

# Story 2.4: Add Basic Route and Label Readability Improvement

Status: done

## Story

As an engineer,
I want basic routes and labels to avoid obvious conflicts,
so that M22 improves schematic communication without entering advanced routing or standards-specific labeling scope.

## Acceptance Criteria

1. Given the M22 optimized sample sheet, when basic route and label facts are produced, then routes use basic schematic edge routing only.
2. Given labels are placed, when the layout is inspected, then labels avoid obvious overlap with their own subject and primary routes.
3. Given M22 scope is reviewed, when route and label behavior is described, then no implementation claims physical routing, advanced electrical routing intelligence, or standards-specific label generation.

## Tasks / Subtasks

- [x] Add basic schematic route readability coverage (AC: 1, 3)
  - [x] Add failing tests for route-lane preference producing basic schematic route facts.
  - [x] Keep route behavior schematic topology only.
- [x] Add basic label readability coverage (AC: 2, 3)
  - [x] Add failing tests for labels avoiding subject bounds and primary route segments.
  - [x] Keep label behavior non-standards-specific.
- [x] Publish acceptance evidence hooks (AC: 3)
  - [x] Update M22 acceptance/usage docs with route and label evidence.
  - [x] Add or update static validation.
- [x] Run validation (AC: 1, 2, 3)
  - [x] Run routing-model tests.
  - [x] Run M22 static proof tests affected by docs.
  - [x] Run encoding audit.

## Dev Notes

### Current State

- M21 introduced basic schematic routing and label contracts.
- M22 must only improve obvious readability; it must not claim advanced routing, physical routing, or standards-specific label generation.

### Guardrails

- Do not add physical routing.
- Do not add advanced electrical routing intelligence.
- Do not add standards-specific label generation.
- Do not persist route or label hints in this story.

### Testing Requirements

- `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test`
- M22 static proof tests affected by documentation updates
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

### References

- [Source: `_bmad-output/implementation-artifacts/m22/epics.md` - Epic 2, Story 2.4]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-17-m22/ARCHITECTURE-SPINE.md` - AD-7, AD-8]
- [Source: `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing`]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test` failed first because route lane preference and subject label bounds did not exist.
- `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test` passed after adding route-lane preference and subject-bound label placement support.
- `node --test ide/theia-frontend/scripts/athena-m22-layout-acceptance-checklist.test.mjs` passed after adding route/label evidence to the checklist.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Added optional schematic route-lane preference to route requests, supporting horizontal-first and vertical-first orthogonal route facts.
- Added subject-bounds-aware label placement so subject labels can move outside their component body.
- Documented route/label readability evidence in the M22 acceptance checklist and usage doc.

### File List

- `_bmad-output/implementation-artifacts/m22/2-4-add-basic-route-and-label-readability-improvement.md`
- `_bmad-output/implementation-artifacts/m22/sprint-status.yaml`
- `docs/usages/m22-proof-usage.md`
- `examples/m22/sample-project/M22-LAYOUT-ACCEPTANCE.md`
- `ide/theia-frontend/scripts/athena-m22-layout-acceptance-checklist.test.mjs`
- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/SchematicLabelModel.kt`
- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/SchematicRoutingModel.kt`
- `kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/SchematicLabelModelTest.kt`
- `kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/SchematicRoutingModelTest.kt`

## Change Log

- 2026-07-18: Created M22 Story 2.4 with basic schematic route and label readability requirements.
- 2026-07-18: Added basic route-lane preference, subject-bound label placement, docs, and tests.
