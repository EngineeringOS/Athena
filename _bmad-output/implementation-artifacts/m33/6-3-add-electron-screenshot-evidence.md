---
story_id: 6.3
story_key: 6-3-add-electron-screenshot-evidence
epic: 6
status: review
baseline_commit: 0311ad67c08779db3d1def271d0ac948a4683128
created: '2026-07-23'
updated: '2026-07-23'
---

# Story 6.3: Add Electron Screenshot Evidence

## Status

Review

## Story

As a reviewer, I want screenshot evidence across real IDE viewports.

## Acceptance Criteria

- Electron proof captures desktop and narrower viewport screenshots.
- Screenshots support qualitative professional review and are paired with structured proof.
- Canvas is nonblank, framed correctly, and toolbar controls do not overlap.
- AC-to-evidence and polish/purge notes are recorded before review.

## Tasks/Subtasks

- [x] Add a RED contract that requires M33 desktop and narrow Cabinet screenshots.
- [x] Add viewport-sized Electron launch support for screenshot smoke runs.
- [x] Capture desktop and narrow Cabinet screenshots and pair each with structured proof.
- [x] Prevent Graph reveal from splitting beside an active source editor during product evidence capture.
- [x] Assert nonblank canvas, frame/viewBox evidence, and viewport-bounded controls.
- [x] Complete AC-to-evidence mapping, three-layer adversarial review, and polish/purge.

## Dev Notes

- Bind to M33 AD-10.
- Rebuild frontend and product bundles before screenshots when Theia-visible behavior changes.

## Testing

- Electron/Playwright screenshot smoke.
- Pixel/DOM checks for nonblank canvas and viewport-bounded controls.

## Evidence Plan

- Screenshot paths and structured proof linked in story.

## Polish And Purge

Delete stale screenshots or move them into milestone-specific evidence folders.

## Dev Agent Record

### Implementation Plan

- Extend the M33 smoke verifier to run two real Electron viewports: desktop and narrow.
- Add launcher window-size environment controls so screenshot dimensions are deliberate, not implicit.
- Keep source diagnostic activation, but ensure Graph opens as the primary main-area surface instead of a split pane.
- Store screenshots under the M33 evidence folder with viewport-specific names.

### Debug Log

- RED: product smoke contract failed because M33 only emitted `m33-cabinet-product-smoke.png`.
- RED: product contribution contract failed because `revealGraphWorkbench` opened Graph with `mode: 'open-to-right'` when a source editor was active.
- GREEN: verifier now captures `m33-cabinet-product-smoke-desktop.png` and `m33-cabinet-product-smoke-narrow.png`.
- GREEN: launcher accepts `ATHENA_ELECTRON_SMOKE_WINDOW_WIDTH` and `ATHENA_ELECTRON_SMOKE_WINDOW_HEIGHT`.
- GREEN: Graph reveal now adds the Cabinet Workbench to the configured main area without source-editor split.

### Completion Notes

- Desktop screenshot: `_bmad-output/implementation-artifacts/m33/screenshots/m33-cabinet-product-smoke-desktop.png`.
- Narrow screenshot: `_bmad-output/implementation-artifacts/m33/screenshots/m33-cabinet-product-smoke-narrow.png`.
- Structured proof includes `screenshotEvidenceProof` with viewport name, window size, PNG dimensions, active Cabinet view, canvas/frame facts, and control overlap checks.
- Stale single-viewport screenshot `m33-cabinet-product-smoke.png` was removed from the evidence folder.

### Three-Layer Adversarial Review

- Blind Hunter: screenshot evidence is not accepted alone; each PNG is paired with structured Cabinet proof.
- Edge Case Hunter: desktop and narrow runs prove different viewport widths, nonblank drawing content, frame/viewBox presence, and panel/frontmost control reachability.
- Acceptance Auditor: all ACs map to real commands and concrete screenshot paths below.

### AC-To-Evidence

- AC1: `yarn start:smoke:m33` creates both desktop and narrow PNG files.
- AC2: `ATHENA_M33_PROFESSIONAL_DRAWING_PROOF` includes `screenshotEvidenceProof.screenshots[]` entries for both images.
- AC3: `assertScreenshotEvidenceProof` rejects blank canvas, missing frame/viewBox evidence, and toolbar/panel overlap.
- AC4: this Dev Agent Record records evidence and polish/purge before review.

### Verification

- `node --test scripts\athena-m33-product-smoke-contract.test.mjs` passed: 4/4.
- `yarn build` from `ide` passed.
- `yarn start:smoke:m33` from `ide/theia-product` passed with desktop and narrow screenshots.

## File List

- `ide/theia-frontend/scripts/athena-m33-product-smoke-contract.test.mjs`
- `ide/theia-frontend/src/browser/athena-product-contribution.ts`
- `ide/theia-product/scripts/athena-electron-open-workspace-main.js`
- `ide/theia-product/scripts/verify-athena-m33-sample-project.js`
- `_bmad-output/implementation-artifacts/m33/screenshots/m33-cabinet-product-smoke-desktop.png`
- `_bmad-output/implementation-artifacts/m33/screenshots/m33-cabinet-product-smoke-narrow.png`
- `_bmad-output/implementation-artifacts/m33/screenshots/m33-cabinet-product-smoke.png` (deleted stale evidence)

## Change Log

- 2026-07-23: Added dual-viewport Electron screenshot evidence, structured screenshot proof, viewport-sized launcher controls, and no-split Cabinet Graph reveal.
