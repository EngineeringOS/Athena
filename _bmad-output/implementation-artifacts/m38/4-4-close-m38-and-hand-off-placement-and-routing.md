---
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 4.4: Close M38 And Hand Off Placement And Routing

Status: done

## Story

As a milestone owner,
I want verified M38 records and narrow future handoffs,
so that M39 and M40 improve quality without weakening attachment truth.

## Acceptance Criteria

1. M38 sprint status, screenshot index, usage handoff, and retrospective cite only fresh evidence from M38.
2. Architecture audit confirms one `RepresentationDefinition`, one `GraphicOccurrence`, one strict visible `PresentationConnector`, one atomic Presentation Document, and paint-only Theia/SVG consumers.
3. M39 handoff may replace only current placement production while preserving occurrence transform and attachment authority.
4. M40 handoff may replace only route-candidate production while preserving endpoint validation and strict connector lowering.
5. M38 closure explicitly says placement and routing quality are not professional-grade yet.
6. No story, epic, command, artifact, screenshot, or retrospective item is marked complete without fresh verification evidence.
7. Sequential compiler, LSP, GLSP, frontend, Theia product smoke, source-set hygiene, encoding, and diff checks pass.

## Tasks / Subtasks

- [x] Create M38 closure artifacts (AC: 1, 5, 6)
  - [x] Add or update an M38 usage/handoff note under `_bmad-output/implementation-artifacts/m38`.
  - [x] Add or update an M38 screenshot index listing the three verified screenshots.
  - [x] Add an M38 retrospective with what passed, what remains weak, and which claims are forbidden.
- [x] Freeze architecture handoff boundaries (AC: 2, 3, 4, 5)
  - [x] Record M39 handoff: placement/composition quality only; must preserve `GraphicOccurrence` exact Anchor authority.
  - [x] Record M40 handoff: route quality only; must preserve strict connector endpoint equality and source trace.
  - [x] State Theia remains renderer and no Java2D/second renderer enters closure.
  - [x] State no new normal-source layout/routing/paint grammar was accepted in M38.
- [x] Audit active authority path (AC: 2, 6)
  - [x] Search active production code for forbidden source-set names, milestone production names, `V0`/`V1`, renderer repair, endpoint fallback, and public route authority.
  - [x] Confirm active Theia and SVG paths consume Presentation Document facts, not independent engineering inference.
  - [x] If stale active code or docs are found, delete/refactor directly; no compatibility shim.
- [x] Verify closure evidence (AC: 7)
  - [x] Run M38 compiler sample test.
  - [x] Run M38 LSP smoke test.
  - [x] Run graph GLSP tests.
  - [x] Run Theia frontend tests.
  - [x] Run LSP install distribution.
  - [x] Run Theia product build.
  - [x] Run M38 Electron smoke.
  - [x] Run source-set hygiene audit.
  - [x] Run encoding audit.
  - [x] Run `git diff --check`.

## Dev Notes

This is M38 closure, not M39/M40 implementation.

Hard rules:

- Stay in M38. Do not touch M37, M39, M40 implementation artifacts except handoff references inside M38 closure docs.
- Athena source remains SSOT.
- Theia is render layer. No Java2D, no second renderer, no compiler-side font engine.
- M38 makes trust claim only: exact attachment, complete trace, one visible Presentation Connector path.
- M38 must not claim professional placement or routing beauty. Current screenshot still shows poor route/layout taste; record this honestly as M39/M40 work.
- No compatibility shims. Pre-public stale path gets deleted or aligned to current architecture.
- SVG geometry refs only; SVG must not own Port/signal/component facts.
- Normal `.athena` source stays human-first, concrete, AI-friendly, K.I.S.S. No layout/routing/paint grammar expansion in closure.

Previous story intelligence:

- Story 4.3 passed M38 product smoke after carrying connector `sourceSpan` from `PresentationConnector` through LSP, GLSP, Theia model, and DOM evidence.
- Electron native screenshot can fail with `Current display surface not available for capture`; smoke now falls back to renderer-side SVG rasterization using browser APIs.
- Verified screenshot paths:
  - `_bmad-output/implementation-artifacts/m38/screenshots/m38-professional-control-drawing-desktop-1920x1080.png`
  - `_bmad-output/implementation-artifacts/m38/screenshots/m38-professional-control-drawing-desktop-1280x900.png`
  - `_bmad-output/implementation-artifacts/m38/screenshots/m38-professional-control-drawing-narrow.png`

Relevant artifacts:

- `_bmad-output/implementation-artifacts/m38/epics.md`
- `_bmad-output/implementation-artifacts/m38/sprint-status.yaml`
- `_bmad-output/implementation-artifacts/m38/4-3-prove-exact-connections-end-to-end.md`
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-31-m38/prd.md`
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-31-m38/addendum.md`
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-31-m38/ARCHITECTURE-SPINE.md`
- `examples/m38/professional-control-drawing`
- `ide/theia-product/scripts/verify-athena-m38-professional-control-drawing.js`

## Testing Requirements

Run sequentially:

```powershell
.\gradlew.bat --no-daemon --console=plain -q :kernel:compiler:test --tests com.engineeringood.athena.compiler.DedicatedM38ProfessionalDrawingSampleTest
.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaM38DedicatedProfessionalControlDrawingSmokeTest
yarn --cwd integrations/graph-glsp test
yarn --cwd ide/theia-frontend test
.\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist
yarn --cwd ide/theia-product build
yarn --cwd ide/theia-product start:smoke:m38
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
git diff --check
```

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-08-01: Story created after Story 4.3 moved to review.
- 2026-08-01: Dev started from sprint order after Story 4.4 creation.
- 2026-08-01: Added M38 usage handoff, screenshot index, architecture audit, and retrospective.
- 2026-08-01: Verified M38 closure sequentially through compiler, LSP, GLSP, frontend, product build, Electron smoke, hygiene, encoding, and diff checks.

### Completion Notes

- M38 closure artifacts cite fresh M38-only evidence and screenshot paths.
- Closure records M39 placement/composition handoff and M40 routing-quality handoff without weakening M38 attachment authority.
- Architecture audit confirms Theia and SVG consume Presentation Document facts, with no Java2D, second renderer, renderer repair, or new normal-source drawing grammar.
- M38 closure is honest: attachment and trace are proven; professional placement and routing quality remain future work.

### File List

- `_bmad-output/implementation-artifacts/m38/4-4-close-m38-and-hand-off-placement-and-routing.md`
- `_bmad-output/implementation-artifacts/m38/usage-handoff.md`
- `_bmad-output/implementation-artifacts/m38/screenshot-index.md`
- `_bmad-output/implementation-artifacts/m38/architecture-audit.md`
- `_bmad-output/implementation-artifacts/m38/retrospective.md`

### Change Log

- 2026-08-01: Created Story 4.4 context and marked ready for dev.
- 2026-08-01: Started Story 4.4 implementation.
- 2026-08-01: Completed M38 closure docs, audit, handoff, and verification; moved story to review.

## Verification Evidence

```powershell
.\gradlew.bat --no-daemon --console=plain -q :kernel:compiler:test --tests com.engineeringood.athena.compiler.DedicatedM38ProfessionalDrawingSampleTest
.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaM38DedicatedProfessionalControlDrawingSmokeTest
yarn --cwd integrations/graph-glsp test
yarn --cwd ide/theia-frontend test
.\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist
yarn --cwd ide/theia-product build
yarn --cwd ide/theia-product start:smoke:m38
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
git diff --check
```
