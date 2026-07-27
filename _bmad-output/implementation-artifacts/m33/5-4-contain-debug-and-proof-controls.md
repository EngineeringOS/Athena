---
story_id: 5.4
story_key: 5-4-contain-debug-and-proof-controls
epic: 5
status: done
baseline_commit: 0311ad67c08779db3d1def271d0ac948a4683128
created: '2026-07-23'
updated: '2026-07-23'
---

# Story 5.4: Contain Debug And Proof Controls

## Status

Done

## Story

As a customer demo user, I want debug controls hidden from normal canvas flow.

## Acceptance Criteria

- Debug/proof controls live under inspection affordances.
- Normal toolbar no longer fills with line-like buttons or internal proof toggles.
- Inspection remains available for developer evidence without harming demo density.
- AC-to-evidence and polish/purge notes are recorded before review.

## Tasks/Subtasks

- [x] Add a RED Workbench contract for the maximum normal toolbar command count, the single
  inspection affordance, allowed text labels, and forbidden internal proof controls.
- [x] Keep normal Cabinet commands compact and route engineering evidence through the existing
  inspection popover and structured product payload.
- [x] Extend the Electron smoke payload with measured toolbar density facts.
- [x] Rebuild the Theia product and run the real Electron Cabinet proof.
- [x] Run the complete frontend regression suite.
- [x] Complete adversarial review, AC-to-evidence mapping, and deep polish/purge.

## Dev Notes

- Bind to M33 AD-5, AD-9, AD-10.
- Keep evidence available in structured payloads.

## Testing

- Frontend DOM tests for toolbar count/labels.
- Electron screenshot proof for normal toolbar density.

## Evidence Plan

- Screenshot and DOM proof compare normal toolbar before/after.

## Polish And Purge

Remove stale debug buttons and update graph-view taxonomy contract if needed.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: the M33 Workbench test required a bounded normal toolbar, one inspection control, no proof
  controls, and only the `Cabinet` text label.
- Focused regression passed `17/17` after aligning the stale M32 Add-button contract with Story 5.3
  fail-closed capability behavior.
- Full frontend regression passed `214/214` after product TypeScript compilation.
- Product rebuild completed browser, node, and Electron builds with zero errors.
- Real Electron `yarn start:smoke:m32` reported `normalToolbarButtonCount: 4`,
  `textButtonLabels: ["Cabinet"]`, `inspectionButtonCount: 1`, and
  `internalProofControlCount: 0`.

### Completion Notes List

- AC1: normal Cabinet chrome exposes one information/inspection affordance; structured proof remains
  in the smoke payload rather than appearing as toolbar controls.
- AC2: the measured normal toolbar contains four compact commands and no line-like debug or proof
  toggles.
- AC3: developer evidence remains available through the inspection popover and deterministic
  Electron payload without consuming customer canvas space.
- AC4: adversarial review found one stale M32 test that contradicted the accepted M33 Create Device
  capability gate. The old test was corrected; no production authority was changed.
- Deep polish/purge confirmed there are no normal-flow DOM controls marked as debug, proof, route
  inspection, or package inspection. Hidden compatibility projections remain outside toolbar scope.
- This story proves Workbench control density only. It does not claim professional Cabinet drawing
  quality; Epic 6 still owns live M33 representation/composition integration and screenshot proof.

### File List

- `_bmad-output/implementation-artifacts/m33/5-3-resolve-create-device-demo-behavior.md`
- `_bmad-output/implementation-artifacts/m33/5-4-contain-debug-and-proof-controls.md`
- `_bmad-output/implementation-artifacts/m33/cleanup-ledger.md`
- `_bmad-output/implementation-artifacts/m33/sprint-status.yaml`
- `_bmad-output/implementation-artifacts/m32/screenshots/m32-graph-workbench-smoke.png`
- `ide/theia-frontend/scripts/athena-ide-density-contract.test.mjs`
- `ide/theia-frontend/scripts/athena-m32-create-entity-panel.test.mjs`
- `ide/theia-frontend/scripts/athena-m33-workbench-primary-surface.test.mjs`
- `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`
- `ide/theia-product/scripts/athena-electron-open-workspace-main.js`
- `ide/theia-product/scripts/verify-athena-m32-sample-project.js`

## Change Log

- 2026-07-23: Contained internal evidence behind inspection, bounded normal Cabinet toolbar density,
  added measured Electron proof, and completed deep cleanup review.
