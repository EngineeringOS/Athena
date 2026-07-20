---
baseline_commit: c7fda3877a76fd8db52a65510e1b123aed4304f1
---

# Story 3.1: Compact Route Labels With Hover And Selection Detail

Status: done

## Story

As an engineer,
I want route labels to stay compact while detailed semantic identities remain available on demand,
so that the canvas stays readable without losing engineering traceability.

## Acceptance Criteria

1. Given a route connects verbose semantic endpoints, when the sheet is rendered, then the visible
   route label uses compact display text.
2. Full semantic endpoint identity remains available through hover, selection, inspector, or proof
   payload.
3. Given a route is selected, the inspector can show canonical source identity, endpoint identity,
   route quality, and document projection context from facts rather than DOM label scanning.

## Tasks / Subtasks

- [x] Keep verbose route labels out of the default canvas (AC: 1)
  - [x] Route labels are deferred/selection-only unless explicitly visible as compact facts.
  - [x] M27 smoke reports `visibleRouteLabelCount: 0` and `visibleVerboseRouteLabelCount: 0`.
- [x] Preserve on-demand semantic detail (AC: 2, 3)
  - [x] Route SVG elements retain `<title>` and structured data attributes for hover/proof
        disclosure.
  - [x] Graph route inspection resolves route quality and endpoint identities from model facts.
  - [x] Product smoke captures full `routeStates` with semantic ids, anchors, route points, and
        quality.
- [x] Preserve renderer boundary (AC: 3)
  - [x] Theia reads transported route facts; it does not infer route meaning from DOM labels.

## Dev Notes

- This story is satisfied by Story 1.3 compact-label work plus existing route-inspection/proof
  payload behavior.
- No `.athena` syntax is introduced.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- 2026-07-20: Closed after M27 smoke proved no default visible verbose route labels and structured
  route proof remained available.

### Completion Notes List

- Fresh M27 smoke active first sheet: `visibleRouteLabelCount: 0`,
  `deferredRouteLabelCount: 2`, `visibleVerboseRouteLabelCount: 0`.
- Fresh route proof includes semantic route ids, terminal anchor ids, route point lists, terminal
  anchor status, orthogonal status, and route quality.

### File List

- `_bmad-output/implementation-artifacts/m27/3-1-compact-route-labels-with-hover-and-selection-detail.md`

## Change Log

- 2026-07-20: Created and closed Story 3.1 from existing implementation and fresh proof evidence.

## Verification

- `yarn --cwd ide/theia-frontend test` - passed earlier in M27 graph-view closeout, 133/133 tests.
- `yarn --cwd ide build` - passed during Story 2.3 closeout.
- `yarn --cwd ide start:smoke:m27` - passed during Story 2.3 closeout.
