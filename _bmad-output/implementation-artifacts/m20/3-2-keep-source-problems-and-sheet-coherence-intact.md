---
baseline_commit: b76b2da
---

# Story 3.2: Keep source, Problems, and sheet coherence intact

Status: review

## Story

As an engineer,
I want reveal and selection to keep working while presentation is improved,
so that the IDE workflow remains coherent.

## Acceptance Criteria

1. Given a source span or diagnostic tied to a canonical subject, when I trigger reveal, the same canonical subject is highlighted in the sheet.
2. Source and Problems still navigate to the same canonical subject after M20 presentation changes.
3. Presentation polish does not break cross-surface identity or occurrence aliases.
4. No frontend-local semantic inference is introduced.
5. No cabinet preview, repository/import ecosystem, or final layout-stack decision is introduced.

## Tasks / Subtasks

- [x] Preserve the canonical reveal path (AC: 1, 2, 3)
  - [x] Reuse the M19 selection/reveal model and extend only where M20 payloads require it.
  - [x] Keep source, Problems, and sheet synchronized through canonical ids.
- [x] Guard against frontend semantic drift (AC: 4, 5)
  - [x] Do not derive identity from DOM text or canvas state.
  - [x] Keep frontend payloads read-only.
- [x] Add regression tests (AC: 1, 2, 3, 4)
  - [x] Cover reveal from source and Problems.
  - [x] Cover repeated selection after the new presentation fields land.

## Dev Notes

### Current State

- M19 already has canonical selection and reveal behavior through `AthenaSemanticSelectionService` and the related model helpers.
- M20 should not rebuild identity logic; it should keep the coherence path intact while presentation becomes richer.

### Architectural Guardrails

- Follow M20 AD-1 and AD-5.
- Canonical subject identity remains the shared currency across views.
- The renderer and frontend remain downstream of governed projection data.

### Project Structure Notes

- Likely update targets:
  - `ide/theia-frontend/src/browser/athena-semantic-selection-model.ts`
  - `ide/theia-frontend/src/browser/athena-semantic-selection-service.ts`
  - `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`
  - `ide/theia-frontend/scripts/*selection*.test.mjs`
- Avoid touching selection logic that already passes M19 unless M20 payloads require it.

### Testing Requirements

- Add tests before changing selection behavior.
- Keep selection/reveal fixtures small and governed.
- Run frontend tests and encoding audit after updates.

### References

- [Source: `_bmad-output/implementation-artifacts/m20/epics.md` - Epic 3, Story 3.2]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-16-m20/prd.md` - FR-6]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-16-m20/ARCHITECTURE-SPINE.md` - AD-1, AD-5]
- [Source: `_bmad-output/implementation-artifacts/m19/2-1-round-trip-selection-through-canonical-identity.md`]

## Dev Agent Record

### Agent Model Used

GPT-5

### Debug Log References

- RED: `node --test ide/theia-frontend/scripts/athena-semantic-selection-model.test.mjs` failed because presentation-owned occurrences were ignored by reveal resolution.
- GREEN: `node --test ide/theia-frontend/scripts/athena-semantic-selection-model.test.mjs` passed after extending the projection carrier to read governed presentation occurrences and connectors first.
- Verification: `yarn test` from `ide/theia-frontend` passed all 64 frontend scripted checks.

### Completion Notes List

- Source and Problems reveal now round-trip through canonical ids even when the sheet is published from M20 presentation occurrences/connectors.
- The resolver still falls back to graph nodes and edges when presentation occurrences are not present.
- No DOM, text, or canvas-local identity inference was introduced.

### File List

- `_bmad-output/implementation-artifacts/m20/3-2-keep-source-problems-and-sheet-coherence-intact.md`
- `ide/theia-frontend/src/browser/athena-semantic-selection-model.ts`
- `ide/theia-frontend/scripts/athena-semantic-selection-model.test.mjs`

## Change Log

- 2026-07-17: Extended canonical reveal resolution to include governed M20 presentation occurrences and connectors.
