---
title: M33 Sprint Change Proposal - Cabinet-Only Product Focus
status: approved
created: '2026-07-23'
approved_by: Aaron
---

# M33 Sprint Change Proposal - Cabinet-Only Product Focus

## 1. Issue Summary

Story 5.1 exposed a `wiring`-backed Professional Schematic surface, and the first Story 5.2 draft
attempted to keep sheet controls stable while switching among Cabinet, Documentation, and Wiring.
This repeated the product problem M33 is intended to solve: three incomplete peer experiences split
implementation and visual-quality effort, so none reaches customer-demo quality.

The approved correction is explicit: **M33 has one customer-facing product surface, Cabinet.**
Documentation and Schematic/Wiring remain internal compatibility projections only. They receive no
M33 product-toolbar placement, visual polish, or screenshot acceptance.

Evidence:

- The user rejected the three-view product model and directed M33 to focus only on Cabinet.
- Story 5.1 currently labels `wiring` as the primary surface, which conflicts with that direction.
- The interrupted Story 5.2 adds multi-view selector persistence and profile inspection, increasing
  complexity without improving the Cabinet demo.
- Existing Electron proof shows projection switching works, but does not prove professional Cabinet
  layout or package-backed M33 drawing integration.

## 2. Impact Analysis

### Epic Impact

- Epics 1-4 remain valid. Their generic drawing contracts, symbols, renderer adapter, and composition
  facts become the upstream implementation used by Cabinet.
- Story 5.1 remains historical completed work, but its Professional Schematic product decision is
  superseded by this approved correction.
- Story 5.2 is replaced by a Cabinet-only surface story. Its abandoned cross-view selector/profile
  work must be purged.
- Stories 5.3 and 5.4 apply only to Cabinet customer UX.
- Epic 6 sample, structured smoke, Electron E2E, and screenshots target Cabinet only.
- Epic 7 visual review and debt purge target Cabinet only. Documentation/Wiring compatibility is
  ledgered, not polished.

### Artifact Impact

- PRD FR-32..FR-37, core acceptance, success metrics, and open questions require Cabinet-only text.
- Architecture AD-9 must name Cabinet as the only product surface.
- Epics and sprint tracking must replace Story 5.2 and rescope Epics 5-7.
- Workbench tests and Electron proof must stop treating hidden projection switching as product UX.

### Technical Impact

- The visible product-surface contract binds to backend projection id `cabinet` and displays
  `Cabinet`.
- Raw `documentation`, `schematic`, and `wiring` ids remain adapter/protocol compatibility only.
- Cabinet must consume M33 package-backed symbol and composition output; generic boxes and legacy
  hard-coded sheet sizing fail acceptance.
- No backend projection deletion is required in M33.

## 3. Recommended Approach

Use a direct sprint adjustment with narrowed MVP scope.

- **Selected:** replace the interrupted Story 5.2, rescope remaining stories, preserve compatible
  backend views internally.
- **Rejected:** polish all three views. This has high effort and directly conflicts with the product
  focus decision.
- **Rejected:** destructive rollback of all Story 5.1 work. The single-surface adapter and hidden-id
  proof are reusable; only the selected product identity must change.

Effort is medium and risk is medium. The change reduces product scope while increasing the required
quality bar for Cabinet.

## 4. Detailed Changes

### PRD

- Replace Professional Schematic as primary surface with Cabinet.
- Make Documentation and Schematic/Wiring internal compatibility projections.
- Replace cross-view sheet-navigation acceptance with stable Cabinet navigation and layout controls.
- Require all customer-facing smoke and screenshots to activate and prove Cabinet.

### Architecture

- Change AD-9 to Cabinet-only product focus.
- Preserve Workbench/drawing-engine authority separation.
- Treat hidden projections as compatibility transport, never peer product modes.

### Stories

- Story 5.1: retain `done`, add a supersession note.
- Story 5.2: replace with `Make Cabinet The Only Product Surface`.
- Story 5.3: Create Device must work on Cabinet or leave the Cabinet demo.
- Story 5.4: only Cabinet customer controls remain visible.
- Epic 6: integrate package-backed symbols, composition, bounds, and proof into Cabinet.
- Epic 7: purge Cabinet toy rendering and ledger non-Cabinet compatibility.

## 5. Implementation Plan

1. Purge the interrupted multi-view Story 5.2 implementation and test.
2. Implement the Cabinet product-surface model and focused RED/GREEN tests.
3. Resolve Cabinet Create Device behavior and hide Cabinet debug/proof clutter.
4. Integrate M33 symbols and composition into the live Cabinet projection.
5. Run frontend regression, sequential Gradle verification, Electron E2E, DOM proof, and desktop/
   narrow Cabinet screenshots.
6. Complete adversarial review, polish/purge, cleanup ledger, and retrospective.

## 6. Approval And Handoff

Aaron approved the Cabinet-only direction in the triggering request. The M33 implementation agent
owns story execution; architecture and product documents in this proposal are binding for all
remaining M33 work.
