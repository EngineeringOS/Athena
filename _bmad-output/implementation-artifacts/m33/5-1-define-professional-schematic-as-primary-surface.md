---
story_id: 5.1
story_key: 5-1-define-professional-schematic-as-primary-surface
epic: 5
status: done
baseline_commit: 0311ad67c08779db3d1def271d0ac948a4683128
created: '2026-07-23'
updated: '2026-07-23'
---

# Story 5.1: Define Professional Schematic As Primary Surface

## Status

Done

> Superseded product decision: the approved 2026-07-23 sprint correction makes Cabinet the only
> customer-facing M33 surface. This story remains Done as historical implementation evidence; its
> `Professional Schematic` selection is replaced by Story 5.2.

## Story

As a customer demo user, I want one clear primary graph surface so I am not confused by internal
projection ids.

## Acceptance Criteria

- Normal toolbar exposes Professional Schematic as primary surface.
- Raw ids such as `cabinet`, `documentation`, and `schematic` do not appear as confusing peer
  product buttons.
- Internal ids may remain in transport/proof payloads only.
- AC-to-evidence and polish/purge notes are recorded before review.

## Tasks/Subtasks

- [x] Add RED model tests proving the product surface resolves the internal `wiring` projection to
  the stable customer label `Professional Schematic`, regardless of backend display text or view
  order. (AC: 1,3)
- [x] Add RED Workbench source/DOM contract tests proving the normal toolbar renders exactly one
  primary product-surface control and does not render `cabinet`, `documentation`, `schematic`, or
  `wiring` as peer customer labels. (AC: 1,2)
- [x] Add a typed Workbench product-surface model that owns customer taxonomy while preserving the
  backing projection id only for adapter switching and structured proof. (AC: 1..3)
- [x] Replace the M32 Cabinet-first toolbar selection with the M33 Professional Schematic control;
  do not change backend projection identities or drawing-engine output. (AC: 1..3)
- [x] Update superseded M32 taxonomy assertions without weakening compatibility coverage for
  contextual Documentation navigation or secondary Cabinet transport. (AC: 2,3)
- [x] Add/extend Electron proof collection so it asserts the visible `Professional Schematic`
  control and one-primary-surface count from the real Workbench DOM. (AC: 1,2)
- [x] Run focused frontend tests, complete frontend regression, full repository regression,
  encoding audit, and diff checks with Gradle commands strictly sequential. (AC: 1..4)
- [x] Complete adversarial review, AC-to-evidence mapping, and mandatory deep polish/purge. (AC: 4)

## Dev Notes

- Bind to M33 AD-5 and AD-9, FR-32..FR-34, and SM-6.
- `Professional Schematic` is a product-surface identity owned by the Theia Workbench adapter. It
  is not a new backend projection id, source-language concept, representation profile, or drawing
  engine authority.
- Current product transport exposes internal `wiring`, `cabinet`, and `documentation` projection
  ids. The M33 primary surface must bind to `wiring`; accept legacy `schematic` only as an explicit
  compatibility candidate. Never relabel an arbitrary active/Cabinet view as a professional
  schematic.
- Selection must be deterministic and independent of supported-view order or backend displayName.
  If no schematic-capable backing view exists, return no primary-surface control rather than lie.
- Cabinet remains available through backend/adapter compatibility for later secondary UX work, but
  Story 5.1 does not add a second peer button or a new view menu.
- Visible copy, title, and aria-label use `Professional Schematic`. Internal backing ids may appear
  only in typed transport, event closures, or machine proof attributes; never as normal button
  text.
- Do not change sheet navigation, Create Device behavior, debug controls, default backend view,
  renderer, package binding, composition, SVG, or source mutation in this story.

## Architecture Compliance

- Workbench taxonomy lives in `ide/theia-frontend`; no dependency is added to kernel drawing,
  package, representation, or composition modules.
- Backend `viewId` remains the switching contract. Product-surface identity is a frontend adapter
  projection over it and must not rewrite the payload.
- Drawing output must be byte-for-byte independent of toolbar state and labels.
- Keep M32 contextual Documentation behavior and adapter-level Cabinet support until later stories
  explicitly remove or relocate them.

## File Structure Requirements

- Add the typed product-surface contract and pure resolver beside the existing Workbench model in
  `athena-graph-workbench-model.ts`.
- Render the resolved surface only in `athena-graph-workbench-widget.tsx`.
- Add a focused M33 contract test under `ide/theia-frontend/scripts`; adjust only directly
  superseded M32 taxonomy assertions and Electron proof code.
- Do not add a generic navigation framework or product taxonomy module for one surface.

## Testing

- Model test: `wiring` wins over active `cabinet`, input order does not matter, backend labels are
  ignored, internal `schematic` is accepted only as compatibility, and no candidate fails closed.
- DOM/source contract: one `Professional Schematic` primary control; no raw projection label is
  rendered as a peer control; no Cabinet-first selector remains.
- Electron proof: visible primary label, one primary control, backing projection evidence, and no
  raw peer labels.
- Preserve contextual Documentation, sheet navigation, Create Device, relationship authoring, and
  existing M32 smoke assertions not superseded by FR-32..FR-34.
- Run `yarn test` in `ide/theia-frontend`, full sequential `gradlew test`, encoding audit, and
  `git diff --check`.

## Evidence Plan

- AC1: pure resolver assertions plus DOM/Electron visible label and single-control count.
- AC2: source/DOM forbidden-peer-label assertions and updated superseded M32 taxonomy contract.
- AC3: exact backing projection id preservation in resolver/proof without payload mutation.
- AC4: story record, cleanup ledger, RED/GREEN log, regressions, and adversarial review.

## Polish And Purge

Audit old graph-view taxonomy tests, Cabinet-first selection logic, view label helpers, DOM proof
attributes, and docs for contradictory customer labels. Remove only code made dead by this story;
ledger active secondary/debug compatibility with owner, reason, target, and verification.

## Previous Story Intelligence

- M32 intentionally made Cabinet the only visible projection and added source-scanning tests plus
  Electron proof around `resolveVisibleProjectionViews`. Those expectations are now superseded by
  M33 FR-32..FR-34 and must be migrated explicitly, not silently bypassed.
- Documentation controls are contextual and sheet selector persistence is separately governed;
  Story 5.1 must not regress or redesign them because Story 5.2 owns stabilization.
- Epic 4 proved drawing composition facts but the live Workbench does not consume them yet. A
  product label is not evidence that professional rendering is integrated.

## Git Intelligence

- Baseline remains M32 commit `0311ad6`; all M33 work is intentionally uncommitted as one milestone
  change until requested.
- Never stage `.tools`, generated Theia bundles, build outputs, or reference/QET sources.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: all four initial focused checks failed because the product-surface resolver, single-surface
  DOM, and Electron proof fields did not exist.
- GREEN: typed `wiring`/legacy-`schematic` resolution and the one visible product control passed the
  focused model and source contracts.
- ADVERSARIAL: review found three dead generic-view helpers and hidden projection smoke still
  inferred state from visible buttons; both were replaced by explicit adapter switching and an
  active-projection proof attribute.
- E2E DEBUG: first Electron run proved the product bundle was stale; rebuilding Theia exposed a
  second defect where active hidden views were still inferred from removed buttons. The next run
  exposed an embedded-template regex escaping defect (`/s+/` instead of `/\\s+/`). Focused tests
  now guard both failures.
- E2E: M32 Electron product smoke passed after a clean product rebuild. DOM proof reported one
  `professional-schematic` surface, exact label `Professional Schematic`, backing view `wiring`,
  hidden Documentation activation, Cabinet restoration, and zero source diagnostics.
- REGRESSION: frontend `yarn test` passed 210 tests before final documentation closeout; full
  sequential Gradle passed 151 actionable tasks. Final reruns are recorded after closeout.

### Completion Notes List

- Added deterministic Workbench-owned product taxonomy; it never relabels Cabinet as schematic and
  fails closed when neither `wiring` nor explicit legacy `schematic` exists.
- Replaced Cabinet-first peer view rendering with one icon-and-text Professional Schematic control.
  Internal projection ids remain only in adapter switching and machine proof attributes.
- Preserved hidden Cabinet and Documentation compatibility through the smoke adapter and explicit
  active-view proof instead of visible-button inference.
- AC-to-evidence: AC1 is covered by resolver, source contract, and live DOM label/count proof; AC2
  by no-peer source assertions and Electron global-toolbar proof; AC3 by exact backing-view proof
  plus payload-preservation tests; AC4 by this record, cleanup ledger, adversarial fixes, encoding,
  diff, frontend, Gradle, and Electron evidence.
- Polish/purge removed `resolveVisibleProjectionViews`, `abbreviateViewLabel`, `viewIconClass`,
  `viewAriaLabel`, and the stale `--view` CSS. Active hidden compatibility is separately ledgered
  for Stories 5.2/5.4; no drawing, package, composition, renderer, or source authority changed.
- Final acceptance review: PASS for AC1-AC4.

### File List

- `_bmad-output/implementation-artifacts/m33/5-1-define-professional-schematic-as-primary-surface.md`
- `_bmad-output/implementation-artifacts/m33/cleanup-ledger.md`
- `_bmad-output/implementation-artifacts/m33/sprint-status.yaml`
- `ide/theia-frontend/scripts/athena-ide-density-contract.test.mjs`
- `ide/theia-frontend/scripts/athena-m32-graph-view-taxonomy.test.mjs`
- `ide/theia-frontend/scripts/athena-m33-workbench-primary-surface.test.mjs`
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`
- `ide/theia-frontend/src/browser/style/index.css`
- `ide/theia-product/scripts/athena-electron-open-workspace-main.js`
- `ide/theia-product/scripts/verify-athena-m32-sample-project.js`

## Change Log

- 2026-07-23: Expanded BMAD story context with deterministic `wiring`-backed product taxonomy,
  fail-closed behavior, M32 compatibility migration, Electron proof, and Workbench/drawing boundary.
- 2026-07-23: Implemented and E2E-proved the single Professional Schematic product surface, explicit
  hidden-view proof, compatibility switching, and stale generic-view purge.
