---
story_id: 5.3
story_key: 5-3-resolve-create-device-demo-behavior
epic: 5
status: done
baseline_commit: 0311ad67c08779db3d1def271d0ac948a4683128
created: '2026-07-23'
updated: '2026-07-23'
---

# Story 5.3: Resolve Create Device Demo Behavior

## Story

As a customer demo user, I want Create Device to either work or not distract me.

## Status

Done

## Acceptance Criteria

- Create Device performs governed semantic entity creation with visible graph refresh, or is
  removed/disabled with explicit rationale.
- No dead placeholder panel remains in the customer demo.
- If kept, source planning and serialization remain backend authority.
- AC-to-evidence and polish/purge notes are recorded before review.

## Dev Notes

- Bind to M33 AD-5 and M31 authoring authority.
- Do not create source text in Theia.

## Testing

- If enabled: authoring transaction test plus Electron visible-result proof.
- If disabled: DOM proof for absent/disabled control and rationale doc.

## Evidence Plan

- Proof states which path was chosen and why.

## Polish And Purge

Remove dead popover/panel tests if control is removed; update if kept.

## Dev Agent Record

### Debug Log References

- RED: focused frontend contract failed because graph-first authoring proof did not assert Cabinet
  remained active after the created occurrence appeared.
- GREEN: `node --test scripts/athena-m32-cabinet-first-authoring-ux.test.mjs` passed `4/4`.
- Existing real Electron proof already covers panel reachability, backend preview/accept, governed source
  persistence, projected occurrence, nested ports, and reopen persistence; Story 5.3 adds exact Cabinet
  continuity to that proof and will be rerun with the consolidated M33 product gate.

### Completion Notes List

- Chosen path: keep Create Device because it is a working governed authoring flow, not a placeholder.
- Theia submits typed preview and decision requests; it does not plan or serialize `.athena` text.
- Product proof now requires the created semantic occurrence to be visible while exact `cabinet` remains
  active, in addition to persisted source and reopen evidence.
- Deep polish/purge removed the dead placeholder message and fails closed by omitting the Add action
  when governed system identity or concept capability is unavailable. The remaining panel and tests
  have live product and Electron callers.

### File List

- `_bmad-output/implementation-artifacts/m33/5-3-resolve-create-device-demo-behavior.md`
- `_bmad-output/implementation-artifacts/m33/cleanup-ledger.md`
- `_bmad-output/implementation-artifacts/m33/sprint-status.yaml`
- `ide/theia-frontend/scripts/athena-m32-cabinet-first-authoring-ux.test.mjs`
- `ide/theia-frontend/scripts/athena-m32-create-entity-panel.test.mjs`
- `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`
- `ide/theia-product/scripts/athena-electron-open-workspace-main.js`
- `ide/theia-product/scripts/verify-athena-m32-sample-project.js`

## Change Log

- 2026-07-23: Retained the working governed Create Device flow and added exact Cabinet continuity to
  graph-first creation proof.
- 2026-07-23: Purged the unavailable-capability placeholder path and aligned the older M32 panel
  contract with fail-closed M33 behavior.
