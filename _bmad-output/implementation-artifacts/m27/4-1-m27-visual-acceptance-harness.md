---
baseline_commit: c7fda3877a76fd8db52a65510e1b123aed4304f1
---

# Story 4.1: M27 Visual Acceptance Harness

Status: done

## Story

As an Athena maintainer,
I want visual acceptance coverage for the M27 sheet proof,
so that professional sheet quality can be protected from regression.

## Acceptance Criteria

1. Given the M27 sample project is served in the Theia proof environment, acceptance capture
   produces screenshot or equivalent visual artifacts for the professional sheet surface.
2. The visual evidence verifies sheet surface, frame, zones, title block metadata, compact labels,
   and ordered routes through tolerant regression evidence rather than pixel-perfect QElectroTech
   matching.
3. Evidence distinguishes QElectroTech-inspired quality references from Athena-authored facts.

## Completion Notes List

- M27 smoke launches the Theia product against `examples/m27/sample-project`.
- Smoke captures `_bmad-output/implementation-artifacts/m27/proofs/m27-graph-workbench-smoke.png`.
- Smoke asserts sheet frame, grid, transparent control docks, sheet selector, visual centering,
  compact labels, route geometry, and screenshot path.
- QElectroTech remains reference-only and has no runtime/import authority.

## File List

- `_bmad-output/implementation-artifacts/m27/4-1-m27-visual-acceptance-harness.md`
- `_bmad-output/implementation-artifacts/m27/proofs/m27-graph-workbench-smoke.png`
- `ide/theia-product/scripts/verify-athena-m27-sample-project.js`
- `ide/theia-product/scripts/athena-electron-open-workspace-main.js`

## Verification

- `yarn --cwd ide build` - passed during Story 2.3 closeout.
- `yarn --cwd ide start:smoke:m27` - passed during Story 2.3 closeout.
