---
story_key: 5-4-prove-m37-end-to-end-with-screenshots
epic: m37-e5
requirements: [FR-32, FR-34, NFR-3, NFR-7]
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 5.4: Prove M37 End To End With Screenshots

Status: review

## Story

As a milestone evaluator,
I want repeatable structural and visual E2E evidence,
so that professional rendering is demonstrated rather than claimed.

## Acceptance Criteria

1. Freshly rebuilt kernel, LSP, Theia frontend, and Theia product launch the dedicated `examples/m37/professional-control-drawing` project into the professional connection drawing surface.
2. Electron E2E captures desktop and narrow screenshots under `_bmad-output/implementation-artifacts/m37/screenshots`, proves PNG validity, nonblank canvas, responsive viewport differences, and active source-backed product surface.
3. Structural E2E assertions prove every accepted FR-32 gate: zero loose endpoints, fallback anchors, route/body intersections, ambiguous crossings, label/body overlaps, label/title-block overlaps, and unclassified visible routes.
4. Structural E2E assertions prove complete FR-33 trace from Story 5.3: occurrences, routes, labels, markers, sheet structures, proof inputs, package resources, source spans, route intent, lane, presentation class, and compiler snapshot.
5. A post-ready compile-to-presentation refresh completes within 10 seconds on the supported workstation and deterministic reruns preserve semantic snapshot, route snapshot, trace counts, and screenshot dimensions within declared tolerance.
6. E2E fails when required endpoint, line class, intent, lane, label bound, marker, trace, or proof evidence is missing or corrupted.
7. Electron E2E, focused frontend/product contract tests, LSP `installDist`, Theia build, source-set hygiene audit, encoding audit, and `git diff --check` pass sequentially.

## Tasks / Subtasks

- [x] Add RED M37 product E2E contract tests (AC: 1, 2, 3, 4, 6)
  - [x] Add frontend/product contract test proving `ide/package.json` and `ide/theia-product/package.json` expose dedicated M37 start/smoke scripts.
  - [x] Add verifier-source contract test proving the M37 verifier targets only `examples/m37/professional-control-drawing`, expected source path, M37 screenshot artifact directory, professional drawing surface, and no M36/M35 sample.
  - [x] Assert verifier source checks FR-32 zero-defect gates and FR-33 trace fields, not screenshot appearance alone.
  - [x] Assert verifier source rejects raw XML/SVG markup authority, DOM/planner objects, fake proof booleans, and stale frontend bundle assumptions.
- [x] Implement dedicated M37 Electron verifier (AC: 1, 2, 3, 4, 5, 6)
  - [x] Add `ide/theia-product/scripts/verify-athena-m37-professional-control-drawing.js` following the M36 verifier pattern but with M37 names, paths, and professional drawing expectations.
  - [x] Launch `examples/m37/professional-control-drawing` with the professional connection drawing active view/surface, not Cabinet or an older sample.
  - [x] Capture at least desktop `1920x1080`, desktop `1280x900`, and narrow `720x900` screenshots into `_bmad-output/implementation-artifacts/m37/screenshots`.
  - [x] Parse `ATHENA_GRAPH_WORKBENCH_PROOF` and build one M37 proof payload with product surface, screenshot, zero-defect, route, trace, source-diagnostic, authority, and timing sections.
  - [x] Measure refresh duration after IDE readiness and fail if compile-to-presentation exceeds 10 seconds.
- [x] Wire scripts and package contract (AC: 1, 7)
  - [x] Add `start:m37` and `start:smoke:m37` scripts in the correct package files.
  - [x] Keep existing M34/M35/M36 scripts untouched except where shared stale assumptions block M37.
  - [x] Do not add production `src/main` proof/demo/sample classes or compatibility shims.
- [x] Prove renderer and authority boundaries in E2E (AC: 3, 4, 6)
  - [x] Assert professional drawing active view/surface, sheet frame/grid/title evidence, visible item count, route count, route labels, and Graphic Primitive authority.
  - [x] Assert every route payload has endpoint anchors, source/target ports, route intent, lane id, route label ids, quality, presentation class evidence, source span, and compiler snapshot.
  - [x] Assert every occurrence has semantic subject, package id, definition id, binding rule id, package resource ids, anchor ids, labels, and Graphic Primitive authority.
  - [x] Assert no XML, QET HTML/element, raw SVG markup, DOM node, renderer-inferred endpoint, planner-native object, or fallback authority appears in accepted proof.
- [x] Run final verification and record screenshot evidence (AC: 7)
  - [x] Run focused frontend/product contract tests.
  - [x] Run `.\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist`.
  - [x] Run `yarn --cwd ide build`.
  - [x] Run `yarn --cwd ide start:smoke:m37`.
  - [x] Run source-set hygiene audit, encoding audit, and `git diff --check`.
  - [x] Record screenshot paths, proof summary, timing, command results, File List, Change Log, story status, and sprint status only after evidence exists.

## Dev Notes

### Authority And Scope

- Athena source is SSOT. E2E proof reads compiled Athena facts and screenshots; it must not infer engineering success from pixel appearance.
- This story owns screenshot-backed proof only for the dedicated M37 professional connection drawing. Do not reuse M36 Cabinet screenshots or M34/M35 samples.
- The renderer remains paint-only. If a route, line class, endpoint, label, trace, or proof field is missing, fix upstream compiler/payload/source behavior.
- Pre-public rule applies: delete/refactor stale assumptions directly. No compatibility wrappers, fallback proof, old surface aliases, or fake constants.

### Current Code Intelligence

- Existing E2E verifier pattern: `ide/theia-product/scripts/verify-athena-m36-connectivity-cabinet.js`.
- Existing frontend contract pattern: `ide/theia-frontend/scripts/athena-m36-product-smoke-contract.test.mjs`.
- Existing Theia launch helper: `ide/theia-product/scripts/athena-electron-open-workspace-main.js`.
- Product package scripts live in `ide/theia-product/package.json`; top-level IDE workspace scripts live in `ide/package.json`.
- Story 5.2 made `AthenaProfessionalDrawingCompiler` produce computed zero-defect evidence for endpoints, label clearance, fallback absence, route/body intersections, line classification, renderer purity, and route facts.
- Story 5.3 added structured trace on `AthenaProfessionalDrawingEvidence` and enriched LSP route payloads with route intent, lane id, route label ids, selected channels, compiler snapshot, source span, and quality.

### E2E Proof Shape

- Screenshot paths:
  - `_bmad-output/implementation-artifacts/m37/screenshots/m37-professional-control-drawing-desktop-1920x1080.png`
  - `_bmad-output/implementation-artifacts/m37/screenshots/m37-professional-control-drawing-desktop-1280x900.png`
  - `_bmad-output/implementation-artifacts/m37/screenshots/m37-professional-control-drawing-narrow.png`
- Product proof sentinel should be dedicated, for example `ATHENA_M37_PROFESSIONAL_CONTROL_DRAWING_PRODUCT_PROOF=`.
- Required source: `src/com/engineeringood/m37/professionalcontroldrawing/01-professional-control-drawing.athena`.
- Expected active product surface: professional connection drawing/control drawing, not Cabinet.
- E2E proof must include structural evidence and PNG checks. Screenshot visual inspection supports review but is not authority.

### Previous Story Intelligence

- Story 5.2 found real route/body and label collision defects; fixes belonged in route planning and label placement, not renderer repair.
- Story 5.3 proved trace evidence from compiled facts and diagnostics, then added LSP route payload facts. Reuse those fields; do not create a parallel E2E evidence schema with invented data.
- M36 retrospective found stale frontend bundles can make Electron E2E lie. Rebuild LSP and full IDE/Theia frontend before launching Electron.

### Required Commands

- Focused frontend/product contract test, likely:
  - `yarn --cwd ide/theia-frontend test --test-name-pattern` is not available; use the repo's Node test pattern or run the specific `.mjs` test with `node --test` after build.
- Required sequential verification:
  - `.\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist`
  - `yarn --cwd ide build`
  - `yarn --cwd ide start:smoke:m37`
  - `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  - `git diff --check`

### References

- [Source: `_bmad-output/implementation-artifacts/m37/epics.md` - Story 5.4]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-30-m37/prd.md` - FR-32, FR-34, NFR-3, NFR-7]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-30-m37/addendum.md` - M37 Visual Quality Checklist]
- [Source: `_bmad-output/implementation-artifacts/m37/5-2-compile-the-professional-drawing-surface.md` - computed surface gates]
- [Source: `_bmad-output/implementation-artifacts/m37/5-3-expose-computed-source-and-route-evidence.md` - trace and protocol evidence]
- [Source: `_bmad-output/implementation-artifacts/m36/retrospective.md` - stale frontend bundle lesson]
- [Source: `ide/theia-product/scripts/verify-athena-m36-connectivity-cabinet.js` - Electron verifier pattern]
- [Source: `ide/theia-frontend/scripts/athena-m36-product-smoke-contract.test.mjs` - verifier contract pattern]

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- 2026-07-31: `node --test ide/theia-frontend/scripts/athena-m37-professional-control-drawing-product-e2e-contract.test.mjs` passed.
- 2026-07-31: `.\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist` passed.
- 2026-07-31: `yarn --cwd ide build` passed and rebuilt Theia frontend/product plus LSP runtime.
- 2026-07-31: `yarn --cwd ide start:smoke:m37` passed, emitted M37 professional Control Drawing proof, and captured screenshots.
- 2026-07-31: `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1` passed.
- 2026-07-31: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.
- 2026-07-31: `git diff --check` passed after removing one trailing whitespace line in the product launch script.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Added dedicated M37 product E2E contract coverage for M37-only scripts, sample path, screenshot artifact path, FR-32 zero-defect gates, FR-33 trace fields, and authority rejection rules.
- Added dedicated M37 Electron smoke verifier for `examples/m37/professional-control-drawing`; verifier proves professional Control Drawing surface, screenshots, route/occurrence trace, renderer purity, zero defects, and compile-to-presentation timing.
- Fixed stale LSP dirty-document projection override so M37 product smoke uses compiled projection session payload instead of Cabinet-specific override behavior.
- Tightened route label placement and frontend rendering to use compiler-provided label origins and avoid terminal/body/title collisions.
- Final smoke proof: workspace `examples/m37/professional-control-drawing`, active surface `Control Drawing`, backing view `schematic`, 11 routes, 10 graphic occurrences, 28 presentation terminals, refresh 53 ms, zero defect gates passed.
- Screenshot evidence:
  - `_bmad-output/implementation-artifacts/m37/screenshots/m37-professional-control-drawing-desktop-1920x1080.png`
  - `_bmad-output/implementation-artifacts/m37/screenshots/m37-professional-control-drawing-desktop-1280x900.png`
  - `_bmad-output/implementation-artifacts/m37/screenshots/m37-professional-control-drawing-narrow.png`

### File List

- _bmad-output/implementation-artifacts/m37/5-4-prove-m37-end-to-end-with-screenshots.md
- _bmad-output/implementation-artifacts/m37/screenshots/m37-professional-control-drawing-desktop-1920x1080.png
- _bmad-output/implementation-artifacts/m37/screenshots/m37-professional-control-drawing-desktop-1280x900.png
- _bmad-output/implementation-artifacts/m37/screenshots/m37-professional-control-drawing-narrow.png
- examples/m37/professional-control-drawing/packages/representation/com/engineeringood/m37/professional/m37-surface-elements.athena
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionSessionProtocol.kt
- ide/package.json
- ide/theia-frontend/scripts/athena-m37-professional-control-drawing-product-e2e-contract.test.mjs
- ide/theia-frontend/src/browser/athena-graph-workbench-model.ts
- ide/theia-product/package.json
- ide/theia-product/scripts/athena-electron-open-workspace-main.js
- ide/theia-product/scripts/verify-athena-m37-professional-control-drawing.js
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaProfessionalDrawingCompiler.kt
- kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationDocument.kt
- kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/RouteLabelPlacementCompiler.kt

## Change Log

- 2026-07-31: Created implementation-ready Story 5.4 from finalized M37 PRD, epics, addendum, M36 E2E verifier pattern, and Stories 5.2/5.3 learnings.
- 2026-07-31: Implemented dedicated M37 product E2E proof, rebuilt IDE/LSP surfaces, captured screenshot evidence, fixed route label placement defects, and moved story to review after all final gates passed.
