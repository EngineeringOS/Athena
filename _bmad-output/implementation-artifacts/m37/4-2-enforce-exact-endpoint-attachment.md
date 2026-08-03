---
story_key: 4-2-enforce-exact-endpoint-attachment
epic: m37-e4
requirements: [FR-19]
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 4.2: Enforce Exact Endpoint Attachment

Status: review

## Story

As an engineer reviewing a compiled drawing,
I want every engineering line attached to a valid endpoint fact,
so that no random line start or end can be mistaken for a real connection.

## Acceptance Criteria

1. Visible engineering connection lines resolve both start and end exactly to an Anchor, terminal, bus, junction, or sheet reference fact.
2. First and last rendered route segment coordinates equal the validated endpoint facts after projection transforms.
3. Unresolved, ambiguous, center/fallback, detached, or body-interior endpoints emit blocking diagnostics and no accepted route attachment.
4. Loose lines are permitted only when authored and typed as graphical annotation rather than engineering connectivity.
5. Renderer receives compiled endpoint attachment facts only and cannot synthesize, snap, or repair endpoints.
6. Focused endpoint, transform, negative, and rendering-adapter tests if touched, source-set hygiene, encoding audit, and `git diff --check` pass sequentially.

## Tasks / Subtasks

- [x] Add RED endpoint attachment tests (AC: 1, 2, 3, 4, 5)
  - [x] Add valid route endpoint test matching first/last segment coordinates to source/target anchors.
  - [x] Add projection transform test proving transformed endpoints stay exact.
  - [x] Add unresolved, ambiguous, fallback, detached, and body-interior negative tests.
  - [x] Add graphical annotation exception test.
  - [x] Add payload hygiene test proving renderer receives attachment facts only.
- [x] Implement endpoint attachment validator (AC: 1, 2, 3, 4)
  - [x] Use responsibility-named types only; no `Proof`, `Demo`, `Sample`, milestone names, `V0`/`V1`, XML, SVG metadata, renderer authority, or compatibility shims.
  - [x] Validate endpoints from route facts and explicit endpoint candidates, not component centers or rendered geometry.
  - [x] Keep annotation lines typed and separate from engineering connectivity.
- [x] Implement diagnostics and normalized payload (AC: 3, 5)
  - [x] Emit typed diagnostics for unresolved, ambiguous, fallback, detached, and body-interior endpoints.
  - [x] Normalize accepted endpoint facts to renderer-safe Athena payload.
  - [x] Prevent success when any engineering route has invalid endpoint attachment.
- [x] Verify and record gates (AC: 6)
  - [x] Run focused endpoint attachment tests.
  - [x] Run `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test`.
  - [x] Run `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test` if compiler touched.
  - [x] Run source-set hygiene audit, encoding audit, and `git diff --check`.
  - [x] Update Dev Agent Record, File List, Change Log, story status, and sprint status only after evidence exists.

## Dev Notes

### Authority And Scope

- Endpoint attachment is compiled evidence. Renderer does not snap, infer, repair, or classify line endpoints.
- Engineering connectivity must attach to authored/compiled facts: Anchor, terminal, bus, junction, or sheet reference.
- Component center, fallback anchor, loose geometry, route body intersection, SVG id, or DOM coordinate cannot become endpoint authority.
- Graphical annotation line is allowed only when explicitly typed as annotation and excluded from engineering connectivity validation.
- No compatibility shims. Athena is pre-public.

### Current Code Intelligence

- `RouteFact` already carries `source`, `target`, and `segments`; route engine starts/ends at terminal anchor grid points.
- `DrawingProfileCompiler.kt` now normalizes line style evidence but does not validate attachment.
- `RouteQualityEvidence.kt` blocks route quality success for unresolved quality evidence, not endpoint exactness.
- Keep this story focused in `routing-model` unless compiler/presentation adapter integration becomes necessary.

### Expected Diagnostics

- `drawing.endpoint.unresolved`
- `drawing.endpoint.ambiguous`
- `drawing.endpoint.fallback`
- `drawing.endpoint.detached`
- `drawing.endpoint.body-interior`
- `drawing.endpoint.protocol-authority.invalid`

### TDD And Verification

- RED first: focused endpoint attachment test fails before implementation.
- Required sequential commands:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test --tests com.engineeringood.athena.routing.EndpointAttachmentValidatorTest`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test` if compiler touched
  - `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  - `git diff --check`

### References

- [Source: `_bmad-output/implementation-artifacts/m37/epics.md` - Story 4.2]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-30-m37/prd.md` - FR-19]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-30-m37/addendum.md` - Non-Negotiable Source Principle, M37 Visual Quality Checklist]
- [Source: `_bmad-output/implementation-artifacts/m37/4-1-compile-drawing-profiles-and-line-classes.md` - Line class evidence]

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test --tests com.engineeringood.athena.routing.EndpointAttachmentValidatorTest` failed before implementation with unresolved endpoint attachment validator symbols.
- GREEN: focused `EndpointAttachmentValidatorTest` passed after adding exact endpoint attachment validator.
- Regression: `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test` passed.
- Hygiene: `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1` passed.
- Encoding: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.
- Whitespace: `git diff --check` passed with existing line-ending warnings only.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Added endpoint attachment facts, projection transform, annotation exception, validator, diagnostics, and renderer-safe payload.
- Engineering endpoints now validate exact source/target attachment against first/last route segment coordinates.
- Validator blocks unresolved, ambiguous, fallback, detached, and body-interior endpoints.
- Graphical annotation lines stay separate from engineering connectivity.
- No compiler module touched; `:kernel:compiler:test` not required for this story.

### File List

- _bmad-output/implementation-artifacts/m37/4-2-enforce-exact-endpoint-attachment.md
- _bmad-output/implementation-artifacts/m37/sprint-status.yaml
- kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/EndpointAttachmentValidator.kt
- kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/EndpointAttachmentValidatorTest.kt

## Change Log

- 2026-07-30: Created implementation-ready Story 4.2 from finalized M37 PRD, addendum, epics, and Story 4.1 line class learnings.
- 2026-07-30: Implemented exact endpoint attachment validation, diagnostics, annotation exception, normalized payload, and routing tests.
