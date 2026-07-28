---
baseline_commit: 14ad49515e95473328472db843722f7200fc1e91
---

# Story 4.3: Run Falsifiable Cabinet Product E2E

Status: in-progress

## Story

As a product owner,
I want fresh automated proof of the real IDE and Cabinet renderer,
so that milestone completion is based on observable behavior rather than claims or stale screenshots.

## Acceptance Criteria

1. Given a clean rebuilt IDE product and the dedicated M35 sample, when Electron E2E runs at 1920x1080, 1280x900, and one narrow viewport, then each run opens the real Cabinet surface, captures a fresh screenshot, and proves nonblank canvas pixels, and screenshots visibly satisfy every structural professional Cabinet checklist item.
2. Given compiler/LSP/product structured evidence, when the smoke gate evaluates it, then it proves zero diagnostics, fallback components, XML authority, raw SVG/HTML transport, unbound anchors, off-channel routes, required body intersections, missing trace, clipping, overflow, and off-canvas required content.
3. Given a blank, mocked, stale, hidden, or hardcoded result, when the E2E verifier runs, then the gate fails even if screenshot files exist, and package/resource/physical/route/trace proof must match the rendered sample identities.
4. Given all Gradle and Node verification required by M35, when the acceptance sequence runs on Windows, then Gradle commands run strictly sequentially and every process is awaited to completion, and commands, timestamps, screenshots, proof payloads, and results are recorded in the story evidence.
5. Given all previous acceptance criteria are green, when the mandatory story gate runs, then RED/GREEN commands, AC-to-evidence mapping, and three-layer review are recorded, and stale screenshots, generated snapshots, temporary workspaces, dead smoke scripts, XML paths, and compatibility names are purged.

## Tasks / Subtasks

- [ ] Add RED E2E/smoke tests for real Cabinet product proof (AC: 1..3)
- [ ] Implement automated rebuilt IDE launch and screenshot capture for required viewports (AC: 1, 4)
- [ ] Implement structured proof checks for diagnostics, authority, route, trace, and visual bounds (AC: 2..3)
- [ ] Polish/purge and evidence gate (AC: 5)

## Dev Notes

E2E must use the real built IDE and dedicated M35 sample. Screenshots are evidence only when paired with structured proof and nonblank pixel checks. Do not accept mocks, stale screenshots, hidden backgrounds, or hardcoded pass payloads.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

### Completion Notes List

### File List

- `_bmad-output/implementation-artifacts/m35/4-3-run-falsifiable-cabinet-product-e2e.md`

### Change Log

- 2026-07-28 - Created Story 4.3 implementation guide.
