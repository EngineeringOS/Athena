---
story_id: 5.2
story_key: 5-2-make-cabinet-the-only-product-surface
epic: 5
status: done
baseline_commit: 0311ad67c08779db3d1def271d0ac948a4683128
created: '2026-07-23'
updated: '2026-07-23'
---

# Story 5.2: Make Cabinet The Only Product Surface

## Status

Done

## Story

As a customer demo user, I want one focused Cabinet surface so Athena can deliver one professional
layout instead of three incomplete views.

## Acceptance Criteria

1. The normal Workbench exposes exactly one product surface named `Cabinet`, backed by the exact
   backend projection id `cabinet`, and Cabinet is the active/default M33 customer surface.
2. Documentation, Schematic, and Wiring do not appear as peer product controls, normal toolbar
   labels, or M33 visual-acceptance targets; their protocol ids may remain hidden for compatibility.
3. Cabinet active state and Cabinet-owned navigation/layout controls remain stable across graph
   refreshes without caching Documentation sheet controls or Presentation Profile UI state.
4. The change does not alter semantic, package, representation, composition, or mutation authority.
5. AC-to-evidence mapping, adversarial review, and mandatory deep polish/purge are recorded before
   review.

## Tasks/Subtasks

- [x] Add RED pure-model tests proving only exact `cabinet` resolves to the `Cabinet` product
  surface, input order and backend labels do not matter, and missing Cabinet fails closed. (AC: 1,2)
- [x] Add RED Workbench source/DOM tests proving exactly one Cabinet product control and forbidding
  Documentation/Schematic/Wiring product labels and abandoned cross-view profile controls. (AC: 1..3)
- [x] Purge interrupted multi-view Story 5.2 code, tests, Electron proof, and docs without reverting
  valid earlier M33 work. (AC: 2,3,5)
- [x] Replace the superseded `Professional Schematic` product identity with a typed Cabinet product
  surface backed only by `cabinet`; preserve hidden adapter compatibility. (AC: 1,2,4)
- [x] Make the product session activate Cabinet deterministically and prove refresh does not require
  switching through Documentation or Wiring. (AC: 1,3)
- [x] Update focused M32/M33 and Electron proof contracts to assert Cabinet-only customer taxonomy,
  while keeping raw compatibility ids out of visible controls. (AC: 1..4)
- [x] Run focused frontend tests, frontend regression, sequential Gradle regression, encoding audit,
  syntax checks, and diff checks. (AC: 1..5)
- [x] Complete adversarial review, AC-to-evidence mapping, and deep polish/purge; update the cleanup
  ledger with every retained hidden compatibility path. (AC: 5)

## Dev Notes

- Bind to the approved sprint change proposal, M33 FR-32..FR-37, AD-5, and corrected AD-9.
- Product surface identity is `cabinet`; visible label is `Cabinet`; backing projection id is exact
  `cabinet`. Do not relabel another active projection as Cabinet.
- Documentation and Schematic/Wiring are not deleted from backend protocols in this story. They are
  hidden compatibility only and must not consume customer-facing UX or screenshot scope.
- Remove the interrupted profile-inspection and cross-view selector-persistence implementation.
  That work was never accepted and conflicts with Cabinet-only scope.
- This story establishes taxonomy and activation. Epic 6 owns package-backed Cabinet symbols,
  professional composition, derived bounds, and screenshot quality.
- Do not redesign Create Device or inspection controls; Stories 5.3 and 5.4 own them.

## Architecture Compliance

- The product-surface adapter remains in `ide/theia-frontend` and maps an existing backend id. It
  does not create semantic or drawing authority.
- Rendering output is independent of toolbar labels and product-surface state.
- No UI may directly mutate Presentation IR, representation occurrences, package resolution,
  composition facts, route geometry, or `.athena` source.

## Testing

- Pure resolver: exact Cabinet selection, order independence, backend-label independence, active
  state fidelity, and fail-closed behavior.
- Source/DOM: one Cabinet surface, zero peer Documentation/Schematic/Wiring controls, no abandoned
  profile selector/inspection, and no cross-view sheet cache.
- Electron: active projection is `cabinet`, visible surface id/label are exact, no peer product
  controls exist, and a graph refresh stays on Cabinet.
- Run `yarn test` in `ide/theia-frontend`, rebuild `ide/theia-product`, run Electron proof, run full
  sequential `gradlew test`, encoding audit, Node syntax checks, and `git diff --check`.

## Evidence Plan

- AC1: resolver tests plus Electron exact label, product id, backing id, and active id.
- AC2: source/DOM forbidden-peer checks and Electron visible-control inventory.
- AC3: Electron Cabinet refresh proof and source scan showing no cross-view selector/profile cache.
- AC4: dependency/source scan and unchanged backend authority tests.
- AC5: story record, adversarial findings, regression logs, and cleanup ledger.

## Polish And Purge

Audit `Professional Schematic` names, wiring-first selection, cross-view sheet caches, profile
inspection helpers, obsolete Story 5.2 tests, M32 taxonomy assumptions, Electron hidden-view product
proof, CSS selectors, and planning artifacts. Remove code made dead by Cabinet-only scope; ledger
only compatibility that still has a real caller and verification path.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: focused M32/M33 tests failed on missing Cabinet activation policy, real refresh hook, stale-request
  guard, exact Electron surface assertions, one-argument selector contract, and obsolete Documentation
  round-trip proof.
- GREEN: focused Cabinet/selector/Workbench suite passed `50/50` after implementation.
- Regression: `yarn test` passed `212/212` in `ide/theia-frontend`.
- Repository regression: sequential `.\gradlew.bat --no-daemon --console=plain test` passed with
  `151 actionable tasks` and `BUILD SUCCESSFUL`.
- Product build: `yarn build` in `ide/theia-product` completed all browser, node, and Electron builds
  with zero errors.
- Electron E2E: `yarn start:smoke:m32` passed default activation, real refresh, Create Device temporary
  repository mutation, and reopen persistence.
- Quality: encoding audit, Node syntax checks, and `git diff --check` passed; only repository line-ending
  notices were emitted.

### Completion Notes List

- AC1: exact `cabinet` is the only product-surface identity and exact `Cabinet` is its stable label;
  Electron reported one visible, backing, and active id, all `cabinet`.
- AC2: Documentation, Schematic, and Wiring have no peer product controls. Explicit historical M26/M27
  smoke requests may still activate Documentation through the hidden compatibility API and collect its
  current facts without entering that view from the M33 product flow.
- AC3: a public product smoke hook now executes the real diagram refresh. Monotonic request tokens prevent
  stale async responses from replacing newer state, and a refreshed compatibility view is corrected to
  Cabinet when Cabinet is available.
- AC4: all changes remain in Workbench adaptation, tests, and smoke orchestration; semantic, package,
  representation, composition, and mutation authorities are unchanged.
- AC5: adversarial review found eight valid issues: fake refresh, Documentation E2E scope leakage, stale
  selector-cache API, forced default smoke view, incomplete exact-id assertions, missing exact active-id
  assertion, stale async overwrite, and refresh returning a compatibility view. All eight were resolved.
  Cabinet-absent fallback was rejected because this story intentionally fails closed.
- Deep polish/purge removed the abandoned profile state, cross-view selector resurrection, Documentation
  round-trip proof, and reprojection naming. Historical Story 5.1 text remains as explicitly superseded
  evidence; hidden compatibility transport remains only for older explicit smoke paths.
- Visual inspection is intentionally not claimed as professional Cabinet completion: the M32 screenshot
  still shows legacy box rendering, sparse composition, and label collisions. Epic 6 and Story 7.2 remain
  responsible for migrating the live Cabinet drawing path and passing the visual quality gate.

### File List

- `_bmad-output/implementation-artifacts/m33/5-2-make-cabinet-the-only-product-surface.md`
- `_bmad-output/implementation-artifacts/m33/cleanup-ledger.md`
- `_bmad-output/implementation-artifacts/m33/sprint-status.yaml`
- `_bmad-output/implementation-artifacts/m32/screenshots/m32-graph-workbench-smoke.png`
- `ide/theia-frontend/scripts/athena-graph-workbench-model.test.mjs`
- `ide/theia-frontend/scripts/athena-m31-controls-lifecycle-diagnostics.test.mjs`
- `ide/theia-frontend/scripts/athena-m32-cabinet-first-authoring-ux.test.mjs`
- `ide/theia-frontend/scripts/athena-m32-graph-view-taxonomy.test.mjs`
- `ide/theia-frontend/scripts/athena-m33-workbench-primary-surface.test.mjs`
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`
- `ide/theia-frontend/src/browser/athena-product-contribution.ts`
- `ide/theia-product/scripts/athena-electron-open-workspace-main.js`
- `ide/theia-product/scripts/verify-athena-m32-sample-project.js`

## Change Log

- 2026-07-23: Replaced the interrupted multi-view selector/profile story after approval of the
  Cabinet-only M33 sprint correction.
- 2026-07-23: Completed Cabinet-only taxonomy, deterministic activation, real refresh, stale-response
  protection, compatibility containment, adversarial review, cleanup, and Electron proof.
