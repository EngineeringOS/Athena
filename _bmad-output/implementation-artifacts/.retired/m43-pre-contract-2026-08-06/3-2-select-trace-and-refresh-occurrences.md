# Story 3.2: Select Trace And Refresh Occurrences

Status: review

## Story

As an engineer,
I want selecting a visible occurrence to reveal its source and refresh atomically after edits,
so that the document remains inspectable and trustworthy.

## Acceptance Criteria

1. SVG and Canvas hit regions use the same Projection occurrence identity and selection state.
2. Selecting an occurrence uses the existing semantic selection service to reveal source; no new
   source-navigation authority is created.
3. Source edits publish a complete replacement Presentation Reality revision; mixed old/new layers
   are not observable.
4. Invalid edits publish diagnostics and no guessed or stale geometry.
5. Focused frontend tests and `yarn build` pass.

## Tasks / Subtasks

- [x] Task 1: Wire document surface selection to semantic source trace (AC: 1-2).
- [x] Task 2: Add atomic refresh and invalid-state handling on document changes (AC: 3-4).
- [x] Task 3: Add focused tests and build frontend (AC: 5).

## Dev Notes

- Reuse `AthenaSemanticSelectionService.selectSemanticId`; it already resolves source ranges through
  LSP and decorates the active editor.
- Keep `data-occurrence-id` on SVG and use the same ID for Canvas hit testing. Canvas must not infer
  relationships; hit regions can be compiler-published occurrence rectangles.
- Replace payload as one immutable object before calling `update()`. Clear prior payload when LSP
  reports unavailable or diagnostics. Do not retain previous geometry as fallback.
- Use existing editor/repository listeners. No compatibility path, duplicate renderer, or renderer
  geometry derivation.

### References

- [Source: _bmad-output/planning-artifacts/m43/epics.md#Story 3-2 - Select Trace And Refresh Occurrences]
- [Source: ide/theia-frontend/src/browser/athena-semantic-selection-service.ts]
- [Source: ide/theia-frontend/src/browser/athena-presentation-widget.tsx]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `yarn test` passed (build plus four frontend tests).

### Completion Notes List

- Added SVG occurrence selection with shared occurrence IDs and `AthenaSemanticSelectionService` trace.
- Added editor/session refresh listeners with monotonic revision token; stale responses ignored.
- Invalid/unavailable payload replaces prior state and renders diagnostics, never guessed geometry.

### File List

- `ide/theia-frontend/src/browser/athena-presentation-widget.tsx`
- `ide/theia-frontend/src/browser/style/index.css`

### Change Log

- 2026-08-05: Added source trace selection and atomic Presentation Reality refresh.
