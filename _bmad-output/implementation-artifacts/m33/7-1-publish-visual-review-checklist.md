---
story_id: 7.1
story_key: 7-1-publish-visual-review-checklist
epic: 7
status: review
baseline_commit: 0311ad67c08779db3d1def271d0ac948a4683128
created: '2026-07-23'
updated: '2026-07-23'
---

# Story 7.1: Publish Visual Review Checklist

## Status

Review

## Story

As a reviewer, I want a visual checklist so M33 does not rely on vague taste.

## Acceptance Criteria

- Checklist compares M33 qualitatively against professional engineering drawing references without
  copying proprietary assets.
- Checklist states standards are reference anchors, not compliance claims.
- Checklist covers symbol quality, density, sheet frame, labels, routes, toolbar, and screenshots.
- AC-to-evidence and polish/purge notes are recorded before review.

## Tasks/Subtasks

- [x] Add a RED checklist contract test.
- [x] Publish the M33 Cabinet visual review checklist.
- [x] State reference anchors without compliance claims.
- [x] Cover symbol quality, density, sheet frame, labels, routes, toolbar, and screenshots.
- [x] Run documentation contract validation.
- [x] Complete AC-to-evidence mapping, three-layer review, and polish/purge notes.

## Dev Notes

- Bind to M33 AD-10.
- Keep claims precise; no IEC compliance language unless audited.

## Testing

- Documentation review and link checks.
- Optional checklist-to-proof mapping test if format supports it.

## Evidence Plan

- Checklist path recorded in closeout evidence.

## Polish And Purge

Remove vague visual claims from docs or move them to explicit qualitative review.

## Dev Agent Record

### Implementation Plan

- Add a small documentation contract test so the checklist stays explicit.
- Publish a Cabinet-only visual checklist tied to M33 screenshot and structured proof names.
- Avoid IEC/EPLAN/QET equivalence claims.

### Debug Log

- RED: `node --test scripts\athena-m33-visual-review-checklist.test.mjs` failed because the checklist file did not exist.
- GREEN: added `_bmad-output/implementation-artifacts/m33/visual-review-checklist.md`.

### Completion Notes

- Checklist path: `_bmad-output/implementation-artifacts/m33/visual-review-checklist.md`.
- It uses QElectroTech as qualitative reference anchor only and explicitly rejects compliance/equivalence claims.
- It maps visual review areas to structured proof payloads.

### Three-Layer Adversarial Review

- Blind Hunter: no proprietary asset copy path is introduced.
- Edge Case Hunter: checklist covers screenshots, toolbar, routes, labels, sheet frame, density, and symbol quality.
- Acceptance Auditor: checklist and test cover every AC.

### AC-To-Evidence

- AC1: `Reference Anchors` section compares qualitatively and bans copying proprietary/QET assets into runtime.
- AC2: checklist states references are anchors, not compliance claims.
- AC3: sections cover Symbol Quality, Drawing Density, Sheet Frame, Labels, Routes, Toolbar, and Screenshot Evidence.
- AC4: this Dev Agent Record records evidence and polish/purge before review.

### Verification

- `node --test scripts\athena-m33-visual-review-checklist.test.mjs` passed: 2/2.

## File List

- `_bmad-output/implementation-artifacts/m33/visual-review-checklist.md`
- `ide/theia-frontend/scripts/athena-m33-visual-review-checklist.test.mjs`

## Change Log

- 2026-07-23: Published M33 Cabinet visual review checklist and contract test.
