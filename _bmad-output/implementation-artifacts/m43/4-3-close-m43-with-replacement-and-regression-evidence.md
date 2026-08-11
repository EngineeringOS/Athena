---
baseline_commit: 9681345816d2659abf7a70b68151eacfef0944c2
---

# Story 4.3: Close M43 With Replacement And Regression Evidence

Status: done

## Story

As a maintainer,
I can verify M43 closure from deterministic commands,
so stale presentation authority, compatibility paths, and unproven product claims cannot survive
in active Athena code.

## Acceptance Criteria

1. The active source tree passes the M43 replacement absence gate:
   no raw `athena/projectionSession` presentation transport, `requestProjectionSession`,
   `AthenaProjection*Payload` paint DTO, split DOM SVG occurrence layer, `paintCanvas(` fallback,
   duplicate live renderer, or compatibility reader remains under `kernel`, `ide`, `apps`,
   `extensions`, `integrations`, `contracts`, or active `examples/m43`. Historical BMad records,
   the replacement ledger, and negative tests may mention retired names as evidence.
2. M43 source-set hygiene is clean:
   no milestone/demo/proof/sample helper in production `src/main`, no renderer dependency outside
   the sole `KonvaDiagramAdapter` boundary, no copied `reference/` source, no M0-M42 example or
   fallback selected by active product scripts, and no stale generated frontend bundle used for proof.
3. The active M43 contract corpus is complete and referenced by evidence:
   scene/publication/command schemas, generated TypeScript validators, grid/trace/asset/render/scale
   vectors, rolling-shutter source and Sheet Companion, product proof, screenshots, and scale
   benchmark all exist under their governed paths and agree on scene identity, revision, counts,
   trace, viewport, and gate decisions.
4. Sequential closure verification passes from fresh rebuilt surfaces:
   frontend contract check/build/tests, product build, product proof at desktop and narrow viewports,
   scale harness, affected `:ide:lsp:test`, root `test`, encoding audit, and source-set hygiene
   audit. Gradle invocations run one at a time on Windows.
5. Closure records are complete and truthful:
   every M43 story file has checked tasks, a complete Dev Agent Record and File List, status
   `review` or `done` backed by passing evidence; `sprint-status.yaml` lists exact story order and
   current statuses; a closure note records commands, artifact paths, known residual risk, and
   explicit deferred work (PDF/export, alternate renderer, AI, ELK/layout solver, collaboration).
6. No compatibility layer is added. Active M43 product uses only `AthenaDiagramScene`,
   `AthenaScenePublication`, typed diagram commands, and one `KonvaDiagramAdapter`; old M0-M42
   examples and proofs are not reopened or used as regression authority.

## Tasks / Subtasks

- [x] Run active replacement and source-set absence audit (AC: 1, 2, 6)
  - [x] Search only active production/runtime/example paths using the exact ledger absence terms;
    classify each hit as active violation, required negative test, architecture/history evidence,
    or allowed M42 knowledge code.
  - [x] Delete or refactor active stale transport, widget selector, proof selector, fallback, alias,
    duplicate dependency, or milestone-named production class; do not delete historical BMad records
    or the M43 replacement ledger.
  - [x] Re-run source-set hygiene and dependency/import boundary checks after cleanup.
- [x] Reconcile M43 contract and evidence corpus (AC: 3, 5)
  - [x] Validate `contracts/presentation/v1/manifest.json` references every required schema/profile/
    vector and that generated frontend contracts are current.
  - [x] Validate rolling-shutter source/Sheet Companion, product proof, desktop/narrow screenshots,
    scale benchmark, scene digest, trace IDs, counts, and revisions are internally consistent.
  - [x] Write `_bmad-output/implementation-artifacts/m43/M43-CLOSURE.md` with deterministic commands,
    artifact paths, gate metrics, replacement-audit result, residual risk, and deferred M44+ work.
- [x] Run complete sequential closure proof (AC: 4)
  - [x] Run frontend contracts, frontend build/tests, product build, product proof, and scale
    harness from their owning package directories after rebuilding affected bundles.
  - [x] Run `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test`, then root `test`, sequentially.
  - [x] Run encoding and source-set hygiene audits; retain exact passing outputs in the closure log.
- [x] Complete BMad records and sprint closure (AC: 5, 6)
  - [x] Re-scan all M43 story tasks and records; update only allowed story record sections and
    statuses, preserving baseline commits and historical evidence.
  - [x] Update M43 sprint status in numeric order; mark this story `review` only after every gate
    passes, then record epic closure/retrospective outcome without fabricating `done`.

## Dev Notes

### Authority And Scope

- `AthenaDiagramScene` is the only public Presentation Reality scene. `AthenaScenePublication` is
  the only publication state machine. `DiagramAuthoringService` remains semantic command owner.
- `KonvaDiagramAdapter` is the only live paint/hit owner and the only production Konva import
  boundary. CSS may size the host and show transient focus/selection but may not recreate geometry.
- `AthenaProjectionPolicyCompiler` and other M42 knowledge/validation code are not retired
  presentation transport; keep them when active callers prove they belong to M42 authority.
- Historical `_bmad-output` records, `.retired` story copies, architecture reviews, and
  `LEGACY-REPLACEMENT.md` may contain old names as deletion evidence. Absence audit must scope active
  product paths, not erase history.

### Required Closure Paths

- Replacement ledger: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-05-m43/LEGACY-REPLACEMENT.md`
- Contract pack: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-05-m43/M43-CONTRACT-PACK.md`
- Active corpus: `contracts/presentation/v1/`
- Active product fixture: `examples/m43/rolling-shutter/`
- M43 implementation evidence: `_bmad-output/implementation-artifacts/m43/`
- Frontend adapter: `ide/theia-frontend/src/browser/diagram/konva-diagram-adapter.ts`
- Product proof scripts: `ide/theia-product/scripts/verify-athena-m43-product-proof.js`,
  `ide/theia-product/scripts/verify-athena-m43-scale.js`

### Closure Rules

- Do not reopen review stories or reset them to development solely to rewrite prose. Only a concrete
  failing acceptance gate creates new implementation work.
- Do not use M0-M42 examples, screenshots, generated bundles, fallback readers, or compatibility
  aliases as proof. M43 evidence must use `examples/m43/rolling-shutter` and current rebuilt output.
- Do not add PixiJS, WebGPU, WebGL, GLSP, ELK, tldraw, Excalidraw, draw.io, or another renderer in
  closure. Their adoption ledger entries remain study/deferred decisions.
- Do not add PDF, print export, report, BOM, AI mutation, collaboration, or automatic layout scope.
- Do not alter committed contract fixtures to make a gate pass. A failed gate stays visible with
  measured corrective action.

### Required Sequential Commands

```powershell
Set-Location ide
yarn workspace @engineeringood/athena-theia-frontend contracts:check
yarn workspace @engineeringood/athena-theia-frontend build
yarn workspace @engineeringood/athena-theia-frontend test
yarn workspace @engineeringood/athena-theia-product build
Set-Location theia-product
yarn verify:m43-proof
yarn verify:m43-scale
Set-Location ../..
.\gradlew.bat --no-daemon --console=plain :ide:lsp:test
.\gradlew.bat --no-daemon --console=plain test
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
```

Run every Gradle command sequentially. Product proof and scale harness run only after the frontend
and product bundles are rebuilt.

### References

- [Source: `_bmad-output/planning-artifacts/m43/epics.md`#Epic 4 - Product Proof, Scale, And Closure]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-05-m43/prd.md`#4.3 Closure And Hygiene]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-05-m43/ARCHITECTURE-SPINE.md`#AD-17 Verification Covers Authority, Interaction, Pixels, And Scale]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-05-m43/ARCHITECTURE-SPINE.md`#AD-20 Replaced Presentation Contracts Die With Activation]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-05-m43/LEGACY-REPLACEMENT.md`#Absence Gate]
- [Source: `_bmad-output/implementation-artifacts/m43/4-2-qualify-100000-element-interactive-scale.md`]

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

1. Active-path legacy audit found no raw M43 presentation transport, split SVG layer, fallback
   renderer, or compatibility reader. Remaining retired-name matches are negative tests and
   architecture/history evidence; M42 knowledge/layout code was classified and preserved.
2. Contract manifest references all 16 required artifacts. Rolling-shutter source, Sheet Companion,
   product proof, desktop/narrow screenshots, and scale evidence agree on current scene/revision
   and trace identity.
3. Closure verification ran after frontend/product rebuild. Frontend tests passed `27/27`; product
   proof passed at desktop and narrow viewports; scale passed with 100000 total, 5000 visible,
   first stable paint `331.6ms`, interaction p95 `22.1ms`, selection p95 `26.9ms`, and zero
   identity/trace/error failures.
4. Both `:ide:lsp:test` and root `test` passed sequentially. Encoding and source-set hygiene audits
   passed after closure documentation was written.
### Implementation Plan

1. Audit active paths against `LEGACY-REPLACEMENT.md`, preserving historical evidence and valid M42
   knowledge code.
2. Reconcile contract corpus, active M43 source/evidence, scene/revision/trace identities, and
   product/scale artifacts.
3. Rebuild frontend/product and run every closure command sequentially.
4. Write `M43-CLOSURE.md`, complete story records, update sprint status, and close Epic 4 after
   retrospective evidence.
### Completion Notes List

- Active replacement absence gate passed; no raw presentation transport, duplicate renderer, fallback,
  or compatibility reader remains in active M43 paths.
- Contract manifest is complete: 16/16 referenced artifacts exist. Active M43 source and Sheet
  Companion remain colocated and are the only product proof authority.
- `M43-CLOSURE.md` records commands, exact evidence paths, scale metrics, replacement audit,
  residual risk, and deferred scope.
- Fresh sequential verification passed: frontend contracts/build/tests (`27/27`), product build,
  desktop/narrow product proof, scale harness, `:ide:lsp:test`, root `test`, encoding audit, and
  source-set hygiene audit.
### File List

- `_bmad-output/implementation-artifacts/m43/4-3-close-m43-with-replacement-and-regression-evidence.md`
- `_bmad-output/implementation-artifacts/m43/M43-CLOSURE.md`
- `_bmad-output/implementation-artifacts/m43/sprint-status.yaml`
- `_bmad-output/implementation-artifacts/m43/m43-product-proof.json`
- `_bmad-output/implementation-artifacts/m43/m43-scale-benchmark.json`
- `_bmad-output/implementation-artifacts/m43/m43-rolling-shutter-desktop-1920x1080.png`
- `_bmad-output/implementation-artifacts/m43/m43-rolling-shutter-mobile-720x900.png`
- `ide/theia-product/scripts/athena-m43-proof-main.js`
### Change Log

- 2026-08-06: Created through BMad create-story workflow; status `ready-for-dev`.
- 2026-08-06: Completed active replacement audit, contract/evidence reconciliation, sequential
  closure verification, and M43 closure note; status `done`.
