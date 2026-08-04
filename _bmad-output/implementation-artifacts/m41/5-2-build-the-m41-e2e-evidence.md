---
baseline_commit: f2245862b430c56aabdc4ef5bcdf97d587db3f81
---

# Story 5.2: Build The M41 E2E Evidence

Status: done

<!-- Note: Created through BMad create-story from milestone-local M41 artifacts. -->

## Story

As an engineer,
I want runtime proof to reject collapsed drawings,
so that screenshots demonstrate Spatial Reality rather than file existence.

## Acceptance Criteria

1. Given changed kernel, runtime, LSP, frontend, GLSP, or Theia product code, when final M41
   product proof runs, the affected kernel/runtime/LSP outputs, frontend bundle, and Theia product
   bundle are rebuilt first. Gradle verification commands run sequentially on Windows; Electron
   never consumes stale generated output. (GATE-4, GATE-6)

2. Given the active project `examples/m41/rolling-shutter` and active view `schematic`, when the
   runtime payload is collected in both desktop `1920x1080` and narrow `720x900` viewports, the
   proof contains typed Projection, Spatial, and Presentation data from one compilation session:
   the active Projection view and Sheet are identified, Spatial Sheet/Occurrence/Region/Construct/
   Anchor/Route/Lane/Grid Reference/quality facts are present, and Presentation occurrence and
   connector facts are present. No verifier constant, copied fixture snapshot, source-text scan,
   or second Projection-to-Spatial transformation supplies these facts. (FR-9, FR-10, FR-11.1,
   SM-1, SM-3, GATE-3)

3. The runtime proof compares actual identities and geometry, not counts alone: Spatial Route
   connection identities equal visible Projection Connection identities one-for-one; each Spatial
   Occurrence bound equals its Presentation occurrence bound; each Spatial Route point list equals
   its Presentation connector point list in order; endpoint Anchor ids and points remain exact;
   every route has one owning Sheet and Lane; and all expected M41 Golden coverage is present.
   (FR-9.1, FR-9.3, FR-9.4, FR-11.1, SM-1, SM-3)

4. The runtime quality proof reads the Spatial quality snapshot and checks exact Golden M41 values,
   including zero Occurrence overlap, zero Construct containment failures, zero Route/body
   intersections, zero twist, used-Lane count, peak Routes per Lane, Density, and Occupancy.
   It separately checks occupied horizontal width `>= 0.55` and vertical height `>= 0.45` of the
   Drawing Area. Label count, label pressure, PNG size, and Presentation-derived proxy metrics do
   not satisfy this acceptance criterion. (FR-12, SM-2, SM-C1, SM-C2)

5. The verifier samples rendered pixels inside the runtime Drawing Area for each viewport. It
   requires non-background body or Route pixels in at least three horizontal buckets and three
   vertical buckets, records bucket counts and Drawing Area bounds, and rejects a blank canvas,
   one-row/top-strip collapse, or pixels outside the governed Drawing Area. It also confirms the
   screenshot dimensions match the requested desktop and narrow Electron surfaces. (FR-11.2,
   SM-2, SM-4, GATE-5)

6. Negative proof tests fail with actionable failed-authority messages when runtime Spatial data is
   missing, Projection/Spatial/Presentation identities disagree, a Route is missing or duplicated,
   coordinates are repaired, quality is nonzero, pixel buckets are empty, or proof is satisfied
   only by a hardcoded renderer boolean or PNG byte threshold. Tests exercise exported verifier
   functions with synthetic invalid proof objects; they do not inspect verifier source text as the
   behavior under test. (FR-10.2, FR-10.4, FR-11.2, NFR-2, GATE-6)

7. Fresh screenshots are written only under
   `_bmad-output/implementation-artifacts/m41/screenshots/` as
   `m41-rolling-shutter-desktop-1920x1080.png` and `m41-rolling-shutter-narrow.png`. The final
   proof output records active source URI, view/Sheet identity, runtime counts, quality values,
   coordinate-preservation result, pixel-bucket result, and both screenshot paths. Human review of
   both screenshots confirms useful two-dimensional content without claiming M42 styling, M44
   readability optimization, M45 professional routing, or QElectroTech/EPLAN parity. (AD-23,
   AD-29, GATE-5, GATE-7)

## Tasks / Subtasks

- [x] Task 1: Prove runtime authority is missing or insufficient with RED tests (AC: 2, 3, 6)
  - [x] Add runtime/LSP contract tests that compile the active M41 source once and expose typed
        Projection, Spatial, and Presentation facts from the same active view/Sheet.
  - [x] Add a failing verifier contract test for missing Spatial payload, route identity mismatch,
        and repaired Presentation coordinates.
  - [x] Preserve existing projection-session behavior and do not introduce a second compiler call.

- [x] Task 2: Carry one compiler-backed Spatial contract through runtime and LSP (AC: 2, 3, 4)
  - [x] Extend the runtime-ready projection snapshot with the retained `SpatialDocument` selected
        from `CompilerCompilationSuccess` for the active Projection view.
  - [x] Add small typed, immutable LSP payload models for the required Projection identity,
        Spatial per-Sheet facts, exact geometry, route endpoint/Lane/trace identity, Grid Reference,
        and quality values. Keep payload mapping projection/runtime-owned; do not make LSP or GLSP
        an engineering authority.
  - [x] Map payloads deterministically through `AthenaProjectionSessionProtocol`, preserve source
        identity and integer coordinates exactly, and fail closed when active Projection/Spatial/
        Presentation data is unavailable.
  - [x] Add focused tests for one-Sheet M41 counts, route coverage, quality values, and immutable
        payload collection behavior.

- [x] Task 3: Preserve runtime facts through GLSP and frontend model (AC: 2, 3, 5)
  - [x] Extend the disposable GLSP source/diagram contract with the typed runtime proof payload;
        normalize only by defensive copying and never recompute geometry.
  - [x] Expose the payload through the Graph Workbench automation snapshot or an equivalent
        product-owned proof hook. Keep normal UI behavior unchanged; M41 does not add labels,
        styling controls, export, or a second renderer.
  - [x] Add frontend/GLSP tests proving Projection ids, Spatial bounds/routes, Presentation
        occurrence bounds, and ordered route points survive translation byte-for-byte.

- [x] Task 4: Replace weak Electron proof with falsifiable runtime and pixel proof (AC: 3-7)
  - [x] Update `ide/theia-product/scripts/athena-electron-open-workspace-main.js` to collect
        runtime payload proof, Drawing Area bounds, occupied width/height, and three-by-three
        horizontal/vertical rendered-pixel buckets for each requested viewport.
  - [x] Update `verify-athena-m41-product-proof.js` to assert runtime identities, exact counts,
        quality, coordinate preservation, pixel buckets, fresh screenshot dimensions, source path,
        active view, and failed-authority diagnostics. Remove PNG-byte-only acceptance and all
        hardcoded paint-only truth claims.
  - [x] Keep screenshot capture scoped to the visible Control Drawing workbench, write only to the
        M41 artifact directory, and report exact proof JSON through the existing sentinel.
  - [x] Replace script-text assertions in `athena-m41-product-proof-contract.test.mjs` with
        behavior tests against exported verifier helpers, including invalid-proof cases.

- [x] Task 5: Rebuild every affected product surface and run E2E (AC: 1, 5, 7)
  - [x] Run focused runtime/LSP/frontend contract tests first.
  - [x] Run Gradle verification sequentially, including affected runtime/LSP tests and
        `:ide:lsp:installDist`; never overlap Gradle invocations on Windows.
  - [x] Rebuild frontend and Theia product bundles with the repository Yarn commands before
        Electron. Do not rely on stale `lib`, `build`, or generated frontend output.
  - [x] Run `yarn --cwd ide start:smoke:m41` and retain its exact proof JSON, fresh desktop/narrow
        screenshots, and failure output if any gate fails.

- [x] Task 6: Complete BMad records and quality gates (AC: 1-7)
  - [x] Run repository `test`, frontend tests/build, source-set hygiene, encoding audit, and
        `git diff --check` sequentially after all edits.
  - [x] Inspect both screenshots and confirm active M41 source, visible two-dimensional body/Route
        content, no top-strip collapse, and no UI overlap; record paths and observations.
  - [x] Complete Tasks, Debug Log, Completion Notes, File List, Change Log, review findings, and
        sprint status from actual command output before moving to review/done.

### Review Follow-ups (AI)

- [x] [AI-Review][High] Replace all-non-background pixel classification with native rendered
      component-body/Route isolation. Grid lines, Drawing Area chrome, and labels must not satisfy
      horizontal/vertical buckets or occupied-span ratios. Require typed classification evidence,
      add RED/GREEN verifier behavior tests, rebuild product, and regenerate both screenshots.
      (AC: 4, 5, 6, 7)

### Review Findings

- [x] [Review][Patch][Medium] Reject malformed runtime geometry types instead of coercing numeric
      strings or nulls during Spatial/Presentation comparison; add RED/GREEN behavior cases for
      matching malformed bounds and Route points.
      [ide/theia-product/scripts/verify-athena-m41-product-proof.js:282]
- [x] [Review][Patch][High] Fail closed unless Presentation and Spatial both contain the exact
      active Sheet; never fall back to another same-view Sheet.
      [kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt:132]
- [x] [Review][Patch][High] Export Projection component ids by typed `component` node kind and
      export `activeSheetId` independently from Spatial facts.
      [ide/theia-frontend/src/browser/athena-product-contribution.ts:320]
- [x] [Review][Patch][High] Require unique, exact Presentation Occurrence identities, including
      `occurrenceId`, `semanticId`, and full Spatial set coverage.
      [ide/theia-product/scripts/verify-athena-m41-product-proof.js:191]
- [x] [Review][Patch][High] Require unique, exact Presentation connector Route identities and
      unique Spatial Projection Connection coverage.
      [ide/theia-product/scripts/verify-athena-m41-product-proof.js:202]
- [x] [Review][Patch][Medium] Compare used-Lane count, not total Lane count, with the Golden
      baseline.
      [ide/theia-product/scripts/verify-athena-m41-product-proof.js:182]
- [x] [Review][Patch][High] Remove proof-time screenshot resizing and prove the actual Electron
      viewport and native capture match each requested surface.
      [ide/theia-product/scripts/athena-electron-open-workspace-main.js:1671]
- [x] [Review][Patch][High] Validate Projection, Spatial, Presentation, active view, and active
      Sheet authority independently for both desktop and narrow runs.
      [ide/theia-product/scripts/verify-athena-m41-product-proof.js:118]
- [x] [Review][Patch][High] Require Drawing Area/sample bounds and prove sampled bitmap bounds map
      to the runtime-governed Drawing Area.
      [ide/theia-product/scripts/verify-athena-m41-product-proof.js:343]
- [x] [Review][Patch][Medium] Wait for child `close`, not `exit`, before consuming final buffered
      proof sentinels.
      [ide/theia-product/scripts/verify-athena-m41-product-proof.js:94]
- [x] [Review][Patch][Medium] Reject duplicate Spatial Anchor ids and any Route listed by more
      than one Lane, regardless of the Lane's declared id.
      [ide/theia-product/scripts/verify-athena-m41-product-proof.js:216]
- [x] [Review][Patch][Medium] Require every Golden quality metric and finite typed runtime quality
      values; never coerce missing values to zero.
      [ide/theia-product/scripts/verify-athena-m41-product-proof.js:240]
- [x] [Review][Patch][Medium] Reject non-finite, negative, or out-of-range pixel counts and ratios.
      [ide/theia-product/scripts/verify-athena-m41-product-proof.js:349]
- [x] [Review][Patch][Low] Create the M41 proof artifact parent directory before writing proof
      JSON.
      [ide/theia-product/scripts/verify-athena-m41-product-proof.js:28]
- [x] [Review][Patch][Medium] Replace pre-reopen verification notes with fresh RED/GREEN, rebuild,
      E2E, audit, screenshot-inspection, and completion records before status changes.
      [_bmad-output/implementation-artifacts/m41/5-2-build-the-m41-e2e-evidence.md:276]
- [x] [Review][Patch][High] Derive the active Spatial Sheet only from the compiler-backed
      `spatialReality.proof.sheets`; reject detached or disagreeing `activeSheet` copies.
      [ide/theia-product/scripts/verify-athena-m41-product-proof.js:143]
- [x] [Review][Patch][High] Measure occupied width and height from compiled Spatial Occurrence and
      Route geometry and enforce the AC4 `0.55`/`0.45` gates independently from rendered pixels.
      [ide/theia-product/scripts/verify-athena-m41-product-proof.js:301]
- [x] [Review][Patch][Medium] Require unique typed Lane facts and an exact one-Lane Route
      partition, including nonblank ids, orientation, coordinate, and membership.
      [ide/theia-product/scripts/verify-athena-m41-product-proof.js:217]
- [x] [Review][Patch][Medium] Reject empty Construct membership and incomplete typed Anchor facts,
      including missing Anchor points, with actionable failed-authority diagnostics.
      [ide/theia-product/scripts/verify-athena-m41-product-proof.js:267]
- [x] [Review][Patch][Medium] Bind the Golden baseline to the exact active M41 source bytes using
      `fixture.source.path` and `fixture.source.sha256`.
      [ide/theia-product/scripts/verify-athena-m41-product-proof.js:430]
- [x] [Review][Patch][Medium] Require both persisted screenshot paths to equal the milestone-local
      M41 artifact paths for their viewport names.
      [ide/theia-product/scripts/verify-athena-m41-product-proof.js:483]
- [x] [Review][Patch][High] Carry rendered Component bounds and Route point geometry from the DOM
      proof and require exact preservation of Presentation geometry, not identity lists alone.
      [ide/theia-product/scripts/athena-electron-open-workspace-main.js:1625]
- [x] [Review][Patch][Medium] Reject duplicate rendered Component and Route identities before
      normalization can hide duplicate DOM targets.
      [ide/theia-product/scripts/athena-electron-open-workspace-main.js:1663]
- [x] [Review][Patch][Medium] Exclude non-canvas overlays from isolated body/Route pixel capture so
      dialogs and notifications cannot satisfy pixel buckets.
      [ide/theia-product/scripts/athena-electron-open-workspace-main.js:1785]
- [x] [Review][Patch][Medium] Reject a governed Drawing Area clipped outside the actual Electron
      viewport before bitmap-coordinate clamping.
      [ide/theia-product/scripts/athena-electron-open-workspace-main.js:1907]
- [x] [Review][Patch][Medium] Prove render identity and geometry stay stable between the retained
      screenshot and isolated pixel capture.
      [ide/theia-product/scripts/athena-electron-open-workspace-main.js:1677]
- [x] [Review][Patch][Medium] Scope exported Projection Region and Construct ids to the active Sheet
      instead of flattening all Projection sheets.
      [kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt:191]
- [x] [Review][Patch][Medium] Persist explicit validated runtime-count, coordinate-preservation,
      Spatial-span, and pixel-bucket result summaries required by AC7.
      [ide/theia-product/scripts/verify-athena-m41-product-proof.js:130]

## Dev Notes

### Authority And Scope

- Authority chain remains Athena source -> Engineering -> Projection -> Spatial -> Presentation ->
  Theia paint. Spatial owns all geometry; Presentation and Theia preserve it.
- `ProjectionSpatialCompiler` is the only Projection-to-Spatial orchestration entry. Story 5.2
  consumes the retained `CompilerCompilationSuccess.spatialDocuments`; it must never call a second
  transformation, derive a synthetic Projection, or read Golden constants into runtime proof.
- M41 proves coherent geometry and basic routes only. M42 owns labels/styling/visibility/grid
  chrome, M43 owns rendering/export, M44 owns readability optimization, and M45 owns professional
  route optimization/multi-Sheet continuation.

### Current Implementation Reality

- `AthenaRuntimeProjectionSession.buildProjectionSnapshot` currently selects Projection and
  Presentation from `CompilerCompilationSuccess`, builds `AthenaRuntimeProjectionReadySnapshot`,
  and omits retained Spatial. Add the smallest typed runtime field needed for proof.
- `AthenaProjectionReadyPayload` and `AthenaProjectionSessionProtocol` are the LSP boundary.
  `integrations/graph-glsp/src/athena-glsp-projection-source.ts` and
  `athena-glsp-projection-adapter.ts` are translation-only; preserve payload values without
  recomputation.
- `AthenaGraphWorkbenchWidget`, `AthenaGraphWorkbenchModel`, and
  `AthenaProductContribution` already expose product automation hooks. Extend the proof hook with
  runtime facts rather than scraping source text or inventing a test-only compiler path.
- `athena-electron-open-workspace-main.js` currently checks DOM route markers and PNG headers but
  accepts hardcoded `paintOnlyRenderer` values and never asserts route/pixel proof. The verifier
  currently accepts any PNG over 1024 bytes; this is explicitly invalid for M41.

### Required Runtime Proof Shape

- Projection identity: active view, active Sheet, visible Projection occurrence ids, construct and
  region ids, and visible Connection ids.
- Spatial identity: Sheet extent/Drawing Area, Occurrence bounds, Region/Construct membership and
  bounds, Anchor ids/points, Route ids/Connection ids/ordered points/source-target Anchor ids,
  owning Sheet, Lane membership, Grid Reference cells, and quality snapshot.
- Presentation identity: occurrence ids/bounds, connector route ids/ordered route points, endpoint
  Anchor ids/points, and drawing canvas/Drawing Area bounds.
- Verifier compares sets and ordered lists by stable typed identity. A matching count with wrong
  identity is failure. Any failed authority returns a plain failed-authority name plus exact
  subject/problem/correction in the proof error.

### Pixel And Viewport Proof

- Use actual rendered Control Drawing pixels in the Electron renderer or captured workbench image;
  do not infer pixels from DOM node count, SVG text, PNG byte length, or screenshot dimensions.
- Define Drawing Area relative to the governed Sheet/canvas bounds, divide it into three equal
  horizontal and three equal vertical buckets, and require non-background body/Route pixels in
  every bucket for both viewports. Record counts, bounds, and background classification.
- Desktop and narrow use the same source and active view, with viewport-specific screenshot paths.
  Repeated runs must remain deterministic enough for exact runtime facts while allowing raster
  anti-aliasing differences in pixel counts.

### Testing And Commands

- Kotlin/JUnit focused tests must use actual M41 bytes and the real runtime/LSP session; synthetic
  payload objects are only for negative verifier behavior tests.
- Frontend/GLSP tests use the existing Node test runner and TypeScript build. Keep new proof helpers
  exportable and unit-testable without launching Electron.
- Expected sequential command families: focused Gradle tests; affected Gradle tests; `:ide:lsp:installDist`;
  frontend build/tests; Theia product build; repository `test`; `yarn --cwd ide start:smoke:m41`;
  source-set hygiene; encoding audit; `git diff --check`. Use repository-defined scripts and do
  not run Gradle commands concurrently.

### Previous Story Intelligence

- Story 5.1 retains immutable validated Spatial from the actual compiler and fail-closes both
  Spatial and Spatial-derived Presentation publication on transformation/canonical conflicts.
- Story 5.1 compiler suite passed 520 tests with zero failures/errors/skips; repository `test`
  passed with 148 actionable tasks; source-set hygiene, encoding audit, and `git diff --check`
  passed. Preserve these gates after runtime/LSP changes.
- Story 5.1 fixture source and baseline remain byte-stable. Do not rewrite
  `examples/m41/rolling-shutter/src/com/engineeringood/m41/rollingshutter/01-rolling-shutter-spatial.athena`,
  `athena.lock`, or the M41 baseline while implementing proof.
- The prior adversarial audit found product proof accepted blank/top-strip output, hardcoded
  renderer booleans, unused route proof, and PNG-only screenshots. Story 5.2 exists to close those
  exact gaps; do not preserve weak checks for compatibility.

### Project Structure Notes

- Expected production updates: `kernel/runtime`, `ide/lsp`, `integrations/graph-glsp`, and
  `ide/theia-frontend` payload/model/proof hooks; `ide/theia-product/scripts` for Electron proof.
- Runtime and LSP models should remain small typed clusters. Keep compiler authority in kernel;
  keep browser/GLSP layers translation-only; avoid one giant mixed-responsibility file.
- Proof helpers and screenshot artifacts belong in test/scripts and
  `_bmad-output/implementation-artifacts/m41/screenshots`, never production `src/main`.
- No new dependencies unless existing repository configuration cannot support real pixel sampling;
  prefer Electron/native image or browser canvas APIs already available.

### References

- [Source: `_bmad-output/implementation-artifacts/m41/epics.md#Story-5.2-Build-The-M41-E2E-Evidence`]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-03-m41/prd.md#FR-11-Preserve-Spatial-Authority-Through-Presentation-And-Theia`]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-03-m41/prd.md#Success-Metrics-And-Closure-Gates`]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-03-m41/addendum.md#Confirmed-Decisions`]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-03-m41/ARCHITECTURE-SPINE.md#AD-26-ADOPTED---Complete-Spatial-Validation-Is-The-Presentation-Gate`]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-03-m41/ARCHITECTURE-SPINE.md#AD-29-ADOPTED---Presentation-And-Product-Proof-Preserve-Runtime-Geometry`]
- [Source: `_bmad-output/implementation-artifacts/m41/m41-delivery-audit-2026-08-03.md`]
- [Source: `_bmad-output/implementation-artifacts/m41/5-1-build-the-dedicated-m41-example.md`]
- [Source: `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt`]
- [Source: `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionModels.kt`]
- [Source: `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionPayloads.kt`]
- [Source: `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionSessionProtocol.kt`]
- [Source: `integrations/graph-glsp/src/athena-glsp-projection-source.ts`]
- [Source: `integrations/graph-glsp/src/athena-glsp-projection-adapter.ts`]
- [Source: `ide/theia-frontend/src/browser/athena-product-contribution.ts`]
- [Source: `ide/theia-product/scripts/athena-electron-open-workspace-main.js`]
- [Source: `ide/theia-product/scripts/verify-athena-m41-product-proof.js`]

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Implementation Plan

- First add failing runtime/LSP/verifier tests for missing Spatial authority and weak product proof.
- Carry retained compiler Spatial facts through runtime, typed LSP, GLSP, and frontend automation.
- Replace Electron verifier's PNG-only/boolean checks with identity, quality, coordinate, and pixel
  bucket assertions for desktop and narrow screenshots.
- Rebuild every affected surface, run sequential gates, inspect screenshots, review findings, and
  update M41 sprint records.

### Debug Log References

- RED contract tests proved missing Spatial authority, route identity drift, repaired Presentation
  bounds/points/endpoints, non-Golden quality, and empty pixel buckets fail with named authorities.
- Runtime and LSP focused tests passed after carrying compiler-retained Spatial facts through the
  same Projection session; no second Projection-to-Spatial transformation was added.
- First Electron run exposed stdout chunk splitting on the large widget payload; buffered sentinel
  parsing fixed false JSON truncation. Next run exposed label ids mixed with occurrence ids; product
  proof now filters label nodes by identity.
- Native Electron capture returned DPI-scaled PNGs (3000x1904 and 1440x1800). `setContentSize` and
  native resize now produce exact requested 1920x1080 and 720x900 screenshots.
- Source-set hygiene initially rejected production `*Proof` model/file names. Renamed runtime/LSP
  authority models to `SpatialFacts` and rebuilt downstream surfaces without compatibility shims.
- Sequential verification passed: runtime/LSP tests, `:ide:lsp:installDist`, `yarn --cwd ide build`,
  GLSP tests, 219 frontend tests, product verifier behavior tests, repository `test` (148 tasks),
  source-set hygiene, encoding audit, and `git diff --check`.
- Earlier adversarial review reopened authority and verifier gaps; each finding remained tracked
  until its RED/GREEN behavior case and affected gate passed. No premature clean-review claim is
  used as completion evidence.
- Review continuation RED: rewritten per-viewport verifier contract initially failed 26 of 27
  behavior cases; runtime exact-authority tests initially failed on missing selectors; both failures
  were observed before production fixes. A later frontend gate exposed an old single-proof negative
  fixture failing at `viewport` instead of intended `spatial` authority.
- Review continuation GREEN: runtime exact Projection/Spatial/Presentation selection tests passed;
  product verifier passed 27 of 27 behavior cases; corrected frontend negative fixture passed its
  focused 2-case suite and the complete frontend suite passed 219 of 219 tests.
- Fresh sequential gates passed on 2026-08-04: `:kernel:runtime:test`, `:ide:lsp:test`,
  `:ide:lsp:installDist`, GLSP 9 of 9 tests, full `yarn --cwd ide build` with zero Theia browser/node/
  Electron errors, post-build M41 product contract 27 of 27, repository `test` with 148 actionable
  tasks, source-set hygiene, encoding audit, and `git diff --check`.
- Fresh post-build Electron E2E passed independently for desktop `1920x1080` and narrow `720x900`.
  Both native PNG dimensions equal requested viewport dimensions; both use isolated rendered
  component-body/Route classification, governed Drawing Area bounds, and nonempty 3-by-3 buckets.
- Screenshot inspection confirmed two-dimensional component/Route distribution and no top-strip
  collapse in both captures. Current route-label text remains visually dense; this is recorded as
  the explicit M42 label/readability boundary, not claimed as M41 visual polish.
- Final verifier expansion first produced 29 passing and 24 failing cases. After strict typed
  geometry, full Projection/Spatial/Presentation identity, Route terminal/semantic/Lane,
  cross-viewport authority, exact quality, rendered-id, and pixel-accounting checks were added,
  the product contract passed 55 of 55 cases.
- Final repository regression passed on 2026-08-04: `gradlew test` completed 148 actionable tasks
  with `BUILD SUCCESSFUL`; source-set hygiene, encoding audit, and `git diff --check` passed
  sequentially. `git diff --check` reported only existing line-ending conversion warnings.
- Final adversarial continuation RED passed 55 existing cases and failed all 12 new falsification
  cases. GREEN passed 67 of 67 after binding proof to exact source bytes, one compiler-backed Sheet,
  typed Lane/Anchor/Construct facts, Spatial occupied span, exact DOM paint geometry, unclipped and
  stable isolated capture, required screenshot paths, and persisted validation summaries.
- Fresh rebuilt Electron E2E passed both viewports. Persisted Spatial occupied span is width
  `848/1120 = 0.7571428571` and height `592/640 = 0.925`; coordinate-preservation summaries are true,
  pixel buckets are nonempty, and runtime counts remain exact in both viewports.

### Completion Notes List

- Runtime snapshot retains immutable Spatial facts from the actual compiler session and fails closed
  when active Spatial authority is unavailable.
- Typed LSP, GLSP, and Graph Workbench payloads preserve Projection ids, Sheet extent/Drawing Area,
  Occurrence bounds, Anchor points, ordered Route points, Lane ownership, Grid References, and exact
  quality values without recomputation.
- Verifier enforces Golden counts/geometry/quality, Projection-Spatial-Presentation identity and
  coordinate equality, one Sheet/Lane owner per Route, native Drawing Area pixel buckets, and exact
  viewport screenshot dimensions. PNG byte size and hardcoded renderer flags are non-authoritative.
- Product proof passed against `examples/m41/rolling-shutter`, active view `schematic`, Sheet
  `schematic/sheet/S1`: 8 Occurrences, 3 Regions, 7 Constructs, 16 Anchors, 9 Routes, 7 used
  Lanes, 15 Grid References, zero overlap/containment/intersection/twist, 3 crossings, peak lane 2,
  Density `8/716800`, Occupancy `25600/716800`.
- Human screenshot review confirms distributed two-dimensional sheet content in both viewports with
  no top-strip collapse. Labels remain visually dense; M42 labels, M44 readability, and M45 routing
  parity remain deferred and are not claimed by this story.
- Final verifier behavior suite passes 55 of 55 cases, including malformed typed geometry,
  one-Sheet coverage, exact identity and route-terminal preservation, cross-viewport runtime
  equality, and internally consistent rendered-pixel evidence.
- Final review continuation expands verifier behavior coverage to 67 of 67 passing cases and proves
  source-hash binding, compiled Spatial span, DOM geometry preservation, unclipped capture, stable
  render state, required artifact paths, and explicit validated proof summaries.

### File List

- `_bmad-output/implementation-artifacts/m41/m41-product-proof.json`
- `_bmad-output/implementation-artifacts/m41/5-2-build-the-m41-e2e-evidence.md`
- `_bmad-output/implementation-artifacts/m41/sprint-status.yaml`
- `_bmad-output/implementation-artifacts/m41/screenshots/m41-rolling-shutter-desktop-1920x1080.png`
- `_bmad-output/implementation-artifacts/m41/screenshots/m41-rolling-shutter-narrow.png`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeSpatialFacts.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeSpatialFactsMapper.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionModels.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeM41SpatialProofTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionAuthoritySelectionTest.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaSpatialFactsPayloads.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionPayloads.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionSessionProtocol.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaM41SpatialPayloadTest.kt`
- `integrations/graph-glsp/src/athena-glsp-projection-source.ts`
- `integrations/graph-glsp/src/athena-glsp-projection-adapter.ts`
- `integrations/graph-glsp/src/athena-glsp-graph-model.ts`
- `integrations/graph-glsp/test/athena-graph-glsp-adapter.test.mjs`
- `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-edge-layer.tsx`
- `ide/theia-frontend/src/browser/athena-product-contribution.ts`
- `ide/theia-product/package.json`
- `ide/theia-product/scripts/athena-electron-open-workspace-main.js`
- `ide/theia-product/scripts/verify-athena-m41-product-proof.js`
- `ide/theia-product/scripts/athena-m41-product-proof-contract.test.mjs`
- `ide/theia-frontend/scripts/athena-m41-product-proof-contract.test.mjs`

### Change Log

- 2026-08-04: Implemented runtime/LSP/GLSP/frontend Spatial facts transport and falsifiable
  verifier behavior tests; moved Story 5.2 to review after focused gates.
- 2026-08-04: Fixed Electron sentinel buffering, label identity filtering, DPI-normalized screenshots,
  Golden ratio parsing, and production source-set hygiene names; product E2E passed in both viewports.
- 2026-08-04: Recorded proof JSON, screenshot paths, exact Golden facts, command evidence, and human
  screenshot observations; all acceptance tasks checked.
- 2026-08-04: Completed review with no unresolved findings and moved Story 5.2 to done.
- 2026-08-04: Completion audit reopened Story 5.2 after proving generic non-background sampling
  counted grid/chrome pixels as body/Route evidence; added high-severity BMad review follow-up.
- 2026-08-04: Closed all reopened review findings with exact active-Sheet selection, independent
  desktop/narrow authority, native captures, isolated body/Route pixels, complete identity/quality/
  bounds validation, fresh full gates, and honest screenshot observations; moved to review.
- 2026-08-04: Expanded final product proof from 29/53 RED to 55/55 GREEN, reran repository `test`
  plus hygiene/encoding/diff gates sequentially, completed records, and returned Story 5.2 to
  review.
- 2026-08-04: Resolved final three-layer review with 12 RED-to-GREEN verifier cases plus active-Sheet
  runtime scoping; rebuilt product E2E, passed 67 product, 219 frontend, 9 GLSP, and repository 148
  task gates, then returned Story 5.2 to review.
- 2026-08-04: Adversarial actions complete with 13 patches resolved, one duplicate-authority
  verifier proposal dismissed, no unresolved findings, and Story 5.2 moved to done.
