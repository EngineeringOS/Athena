---
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 6.2: Build Dedicated M39 Product Proof

Status: review

## Story

As a milestone owner,
I want a dedicated M39 example and E2E proof,
so that M39 is judged on its own architecture.

## Acceptance Criteria

1. A dedicated M39 example exists under `examples/m39` and does not reuse M36, M37, or M38 example paths as proof authority.
2. The M39 example uses human-first relation syntax, with `to` preferred and no normal-source `intent` blocks.
3. The example compiles through Engineering Reality, Projection Reality, Spatial Reality, and Presentation Reality.
4. Kernel, LSP, Theia frontend, graph-glsp, and product verifier surfaces are rebuilt or tested before screenshot capture.
5. The product proof opens the M39 example in Theia and verifies the active product surface is paint-only Presentation output.
6. Screenshots are captured under `_bmad-output/implementation-artifacts/m39/screenshots`.
7. Proof artifacts cite only M39 paths and honestly report M39 as architecture proof, not professional drawing parity.
8. Focused product tests, full regression, source-set hygiene audit, encoding audit, and `git diff --check` pass.

## Tasks / Subtasks

- [x] Establish failing M39 proof tests first (AC: 1-8)
  - [x] Add product/frontend tests proving M39 scripts point to `examples/m39`.
  - [x] Add tests proving verifier writes screenshots under M39 artifacts.
  - [x] Add tests proving proof code does not cite M36, M37, or M38 examples.
  - [x] Add compiler or sample test proving M39 source uses `to` and no `intent` block.
- [x] Create dedicated M39 example (AC: 1-3, 7)
  - [x] Add `examples/m39` package descriptor and lock.
  - [x] Add a concise source file that exercises Engineering, Projection, Spatial, and Presentation chain.
  - [x] Keep names human-first and avoid `ProfessionalControlDrawing`, vague `Evidence`, milestone-named classes, `V0`/`V1`, and compatibility wording in active code.
  - [x] Do not copy stale M37/M38 source as authority; only use prior scripts as implementation reference when needed.
- [x] Wire M39 product proof path (AC: 4-7)
  - [x] Add M39 Theia/product scripts.
  - [x] Add M39 verifier that opens `examples/m39`, checks Presentation paint facts, and captures screenshots.
  - [x] Keep verifier honest: prove nonblank paint-only output and reality-chain presence, not professional routing quality.
  - [x] Delete/refactor stale incompatible proof wiring touched by this story; no migration shims.
- [x] Run E2E proof and capture screenshots (AC: 4-8)
  - [x] Run focused kernel/compiler, LSP, graph-glsp, Theia frontend, and product tests sequentially.
  - [x] Rebuild/install LSP host and frontend/product bundle as required by product proof.
  - [x] Run the M39 product verifier and capture screenshots under `_bmad-output/implementation-artifacts/m39/screenshots`.
  - [x] Run broader `gradlew test`.
  - [x] Run source-set hygiene audit, encoding audit, and `git diff --check`.
  - [x] Update this story File List, Completion Notes, Change Log, and sprint status.

## Dev Notes

### Scope Boundary

This story proves:

```text
M39 source -> Engineering Reality -> Projection Reality -> Spatial Reality -> Presentation Reality -> Theia/SVG paint-only surface
```

It does not claim EPLAN/QET visual quality. If screenshot still shows visual debt, record it for Story 6.3 and M40+.

### Current Code Intelligence

Use CodeGraph before editing code. Active areas likely touched:

- `examples/m39/**`
- `ide/package.json`
- `ide/theia-product/package.json`
- `ide/theia-product/scripts/verify-athena-m39-product-proof.js`
- `ide/theia-frontend/scripts/*m39*.test.mjs`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/*M39*Test.kt`

Existing M37/M38 verifier scripts may be used only as code-shape reference. They must not remain proof authority for M39.

### Architecture Requirements

- M39 proof must cite only M39 paths. [Source: `_bmad-output/implementation-artifacts/m39/epics.md` - Story 6.2]
- Theia and SVG export are paint-only. [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-01-m39/ARCHITECTURE-SPINE.md` - AD-7]
- Human names are architecture. [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-01-m39/ARCHITECTURE-SPINE.md` - AD-8]
- Athena is pre-1.0. Delete stale incompatible code, docs, examples, and tests directly. [Source: `AGENTS.md` - Pre-1.0 Architecture Rule]

### Previous Story Intelligence

Story 6.1 completed the paint-only path:

- SVG export consumes `PresentationPaintPlan`.
- LSP publication requires `paintPlan`.
- graph-glsp and Theia preserve route points and use Presentation visibility/order.
- Older active producers attach real paint plans.

Story 4.3 captured honest spatial quality baseline. Use those facts to report debt, not to claim professional visual quality.

### Testing Requirements

Use TDD. Write failing tests before production/example wiring.

Focused checks:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
.\gradlew.bat --no-daemon --console=plain :ide:lsp:test
cd integrations/graph-glsp; yarn test
cd ide/theia-frontend; yarn test
cd ide; yarn test
```

Product proof checks:

```powershell
.\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist
cd ide; yarn build
cd ide/theia-product; yarn start:smoke:m39
```

Regression and hygiene:

```powershell
.\gradlew.bat --no-daemon --console=plain test
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
git diff --check
```

### References

- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-01-m39/prd.md` - FR-44 through FR-46]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-01-m39/ARCHITECTURE-SPINE.md` - AD-7, AD-8]
- [Source: `_bmad-output/implementation-artifacts/m39/epics.md` - M39-E6 Story 6.2]
- [Source: `_bmad-output/implementation-artifacts/m39/6-1-keep-theia-and-svg-export-paint-only.md` - paint-only renderer handoff]
- [Source: `AGENTS.md` - Source-Set Hygiene Rule, Pre-1.0 Architecture Rule, Build Verification Rule, E2E Proof Rule]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Created after Story 6.1 entered review.
- Started development from sprint status after story creation.
- 2026-08-02 08:40: Completed story execution: verified example and proof tests, rebuilt LSP/frontend/product surfaces, ran M39 product verifier (3 screenshots captured), ran full regression and hygiene audits, marked story review.

### Completion Notes List

- Dedicated M39 example created under `examples/m39/reality-product-proof` with package descriptor, lock, package-local representation material, and a concise source exercising Engineering, Projection, Spatial, and Presentation with `to` relations and no `intent` blocks.
- Compiler proof test `DedicatedM39ProductProofTest` verifies source-first authoring, repository contract/lock validity, clean semantic compilation, representation resolution, and Presentation paint-plan presence; it passes.
- Frontend contract test `athena-m39-product-proof-contract.test.mjs` proves `start:m39`/`start:smoke:m39` wiring, verifier screenshot paths, reality-chain paint-only checks, and the absence of M36/M37/M38 citations; it passes.
- Product verifier `verify-athena-m39-product-proof.js` opens `examples/m39` in Electron at 1920x1080, 1280x900, and 720x900, proves the Control Drawing paint-only surface backed by schematic projection, and captures three non-blank PNG screenshots under `_bmad-output/implementation-artifacts/m39/screenshots`.
- E2E proof output is honest: it reports reality-chain presence and paint-only renderer behavior (no endpoint snapping, rerouting, domain inference, or presentation repair) plus measured spatial debt (28 label collisions, no route body intersections), not professional drawing parity.
- Stale M35 contract assertion `governed Cabinet render proof` was rewritten to the current product-neutral `governed product render proof` wording in the shared Electron smoke script.
- Verification run sequentially: `:kernel:compiler:test`, `:ide:lsp:test`, graph-glsp `yarn test`, theia-frontend `yarn test` (232 tests), theia-backend `yarn test` (12 tests), `:ide:lsp:installDist`, `ide yarn build`, `yarn start:smoke:m39`, full `gradlew test`, source-set hygiene audit, encoding audit, and `git diff --check` — all green.

### File List

- examples/m39/reality-product-proof/athena.yaml
- examples/m39/reality-product-proof/athena.lock
- examples/m39/reality-product-proof/src/com/engineeringood/m39/realityproductproof/01-reality-product-proof.athena
- examples/m39/reality-product-proof/packages/representation/com/engineeringood/m39/realityproductproof/bindings.athena
- examples/m39/reality-product-proof/packages/representation/com/engineeringood/m39/realityproductproof/drawing-profile.athena
- examples/m39/reality-product-proof/packages/representation/com/engineeringood/m39/realityproductproof/elements.athena
- examples/m39/reality-product-proof/.athena/snapshots/material-resolution/** (generated resolution snapshots)
- ide/theia-frontend/scripts/athena-m39-product-proof-contract.test.mjs
- ide/theia-frontend/scripts/athena-m35-cabinet-product-e2e-contract.test.mjs
- ide/theia-product/scripts/verify-athena-m39-product-proof.js
- ide/theia-product/scripts/athena-electron-open-workspace-main.js
- ide/package.json
- ide/theia-product/package.json
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/DedicatedM39ProductProofTest.kt
- _bmad-output/implementation-artifacts/m39/screenshots/m39-reality-product-proof-desktop-1920x1080.png
- _bmad-output/implementation-artifacts/m39/screenshots/m39-reality-product-proof-desktop-1280x900.png
- _bmad-output/implementation-artifacts/m39/screenshots/m39-reality-product-proof-narrow.png
- _bmad-output/implementation-artifacts/m39/6-2-build-dedicated-m39-product-proof.md
- _bmad-output/implementation-artifacts/m39/sprint-status.yaml

### Change Log

- 2026-08-02: Created story from corrected M39 PRD, architecture spine, sprint plan, and Story 6.1 output.
- 2026-08-02: Started development.
- 2026-08-02: Completed M39 example, proof tests, product verifier wiring, E2E screenshots, and full verification; updated File List, Completion Notes, and sprint status to review.
