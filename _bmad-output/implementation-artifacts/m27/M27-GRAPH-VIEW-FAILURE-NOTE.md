# M27 Graph View Failure Note

Date: 2026-07-20

Status: fixed after follow-up debugging, then corrected after a second DOM/SVG inspection. Keep this note as the incident record and prevention checklist.

## Final Lesson From The Second Round

The first follow-up fix still carried a wrong assumption: it treated the governed A3 sheet frame as the thing the live SVG should fit. The user inspected the actual DOM/SVG and found the real regression:

- The live `<svg>` used `viewBox="0 0 1680 1188"`.
- The visible engineering elements occupied only a small area near the top-left.
- Duplicate/off-sheet elements were still present outside the visible frame.
- Some terminal labels were emitted twice.

The corrected rule is:

1. **Component size is exactly the rendered component/route content size from Presentation IR and route facts. Do not guess it and do not hard-code it.**
2. **A3/A4 publication sizes belong to sheet chrome/metadata only. They must not become the active SVG scene viewBox for compact component projections.**
3. **The live SVG viewBox must be computed from the active, filtered scene bounds and centered in the main canvas.**
4. **Support/catalog/reference projection subjects must not leak into the active sheet as off-screen duplicate render elements.**
5. **Visual fixes require live DOM/SVG evidence plus screenshot inspection. Unit tests that only prove data exists are not enough.**

This distinction is now mandatory for M27:

```text
publication sheet frame != live SVG scene bounds != component representation bounds != DOM viewport bounds
```

## Why This Note Exists

The M27 graph-view work was not verified carefully enough before I reported progress. The earlier checks were too focused on DOM/data proof and did not validate the actual visible graph view as a user would see it.

The user then observed serious UI regressions:

- Component UI appears far too large.
- Layout appears broken.
- The bottom-right fit/center button does not behave correctly.
- The graph-view output does not match the intended professional sheet/frame/linework target.

I did not complete a reliable independent verification of those latest user-observed regressions before stopping. They must be treated as open critical issues.

## What Was Verified

These were directly observed during the debugging loop:

1. The previous M27 smoke was insufficient.
   - It passed while only proving that graph facts/DOM existed.
   - It did not prove that the sheet was visually usable.

2. A stricter visual smoke exposed real graph-view problems.
   - The sheet initially collapsed into a thin horizontal band.
   - Example failing visual proof:
     - viewport around `890 x 488`
     - sheet around `844 x 68`
     - canvas around `843 x 68`
   - Routes were orthogonal and avoided bodies, but the sheet surface was visually unusable.

3. The document sheet selector path was broken/incomplete.
   - The UI sent document sheet IDs.
   - The runtime switch path originally only understood view IDs.
   - This meant sheet switching could fail or not update the active projection sheet as intended.

4. The smoke harness became flaky.
   - Sometimes it failed to open Graphical View.
   - Sometimes it sampled the sheet while the graph was still refreshing.
   - Sometimes it reached all-sheet proof but sheet 3 had no rendered route/terminal facts.

5. The M27 sample/project projection appears structurally questionable.
   - `01-workspace-semantic-source.athena` contains the power/control system.
   - `02-field-assets-not-a-sheet.athena` contains field assets under a separate system.
   - The document projection expected a field-wiring sheet, but the active rendered graph did not reliably show field routes/terminals for sheet 3.
   - This may be a sample design problem, projection membership problem, or active-source/workspace aggregation problem. It was not resolved.

## What I Changed During The Investigation

These changes were made before the stop. They need review before keeping or reverting.

### Runtime / Kernel

- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt`
  - Added runtime active projection sheet state.

- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt`
  - Changed projection switching to accept document sheet IDs as sheet switches.
  - Added active-sheet scoping for projection/presentation before building the runtime snapshot.

- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSupport.kt`
  - Made sheet layout choose the active sheet rather than always the first sheet.

- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSessionTest.kt`
  - Added regression coverage for switching a document sheet without treating source files as pages.

- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationDocument.kt`
  - Added `scopedToProjectionMembership(...)` to filter presentation facts by projection-owned membership.

### Theia Graph View / Smoke

- `ide/theia-frontend/src/browser/athena-graph-workbench-edge-layer.tsx`
  - Added `data-athena-route-points` so smoke can inspect actual rendered route geometry.

- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`
  - Added projection-publication fallback sizing for A3/A4 sheet surfaces.
  - This was intended to prevent sheet height collapse, but may have contributed to scale/fit regressions and must be reviewed.

- `ide/theia-frontend/scripts/athena-graph-workbench-model.test.mjs`
  - Added regression coverage for using governed projection publication sheet size instead of collapsed content bounds.

- `ide/theia-product/scripts/athena-electron-open-workspace-main.js`
  - Added screenshot capture.
  - Added all-sheet visual proof collection.
  - Added route geometry checks.
  - Changed smoke window size to `1920 x 1080`.
  - Added broader Graphical View opening fallback.
  - Added waits for rendered sheet content after sheet switching.

- `ide/theia-product/scripts/verify-athena-m27-sample-project.js`
  - Added assertions for screenshot path, visual proof, and all-sheet visual proof.

- `ide/theia-frontend/scripts/athena-m27-sample-project.test.mjs`
  - Added static checks that M27 smoke is wired to the stricter proof functions.

## Verification Actually Run

Commands that passed during the loop:

- `.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaRuntimeProjectionSessionTest`
- `yarn --cwd ide/theia-frontend test`
- `yarn --cwd ide build`

Commands that did not pass reliably:

- `yarn --cwd ide start:smoke:m27`

Observed smoke failures included:

- Sheet collapsed to a thin band.
- Graphical View quick action/opening timeout.
- Visual viewport threshold failure at smaller actual viewport.
- All-sheet proof failure for `documentation/sheet/03-field-wiring-and-terminal-transition` because route/terminal facts were absent or not rendered.
- Timeout waiting for rendered sheet content on sheet 3.

## Current Blockers

1. Actual graph-view visual quality is not acceptable.
   - User reports component UI is too big and layout is broken.
   - User reports fit/center button does not work.
   - This needs direct visual inspection before any story can be marked done.

2. The smoke harness is not yet trustworthy.
   - It now catches more real issues, but also has race/opening problems.
   - It must be stabilized separately from product changes.

3. Active sheet / document projection behavior is not fully proven.
   - Sheet 3 does not reliably render route/terminal facts.
   - Root cause is not proven.

4. Projection-publication sizing may be the wrong level of fix.
   - I changed frontend fallback sizing to A3/A4 dimensions.
   - This may be architecturally acceptable, but it may also worsen scale/fit behavior.
   - It needs review before keeping.

5. Fit-to-view behavior likely uses the wrong bounds.
   - Current suspicion: the fit button may fit `sceneBounds` from content rather than the governed sheet frame, or may not update correctly after sheet-size changes.
   - This is not yet verified in code.

## Mistakes Made

1. I accepted data/DOM smoke as proof of visual acceptance.
   - That was wrong for M27.
   - M27 is explicitly about visible sheet/frame/linework quality.

2. I mixed product fixes, sample questions, runtime fixes, and smoke harness fixes in one loop.
   - This made debugging noisy and hard to review.

3. I kept iterating after the investigation had already shown multiple layers were unstable.
   - The right move was to stop earlier and produce this failure note.

4. I did not checkpoint with a screenshot review before continuing.
   - For visual stories, screenshots must be treated as first-class evidence.

5. I did not sufficiently protect the user-facing graph-view experience.
   - The resulting UI may now look worse, not better.

## Recommended Review Order

1. Inspect current Graphical View manually and capture screenshots before touching code.
2. Decide whether to keep or revert the A3/A4 sheet-size fallback in `athena-graph-workbench-model.ts`.
3. Verify the fit/center button behavior against:
   - content bounds
   - sheet bounds
   - viewport bounds
4. Verify whether sheet 3 should contain field routes in the M27 sample.
5. Separate product bugs from smoke harness bugs.
6. Only after that, decide the next implementation step.

## Do Not Claim

Resolved follow-up, 2026-07-20:

1. Fit/center root cause found.
   - `sceneBounds` used content/off-frame reference bounds even when the graph had a governed A3 sheet frame.
   - The bottom-right fit button therefore fit hidden/off-sheet content instead of the visible sheet.
   - Fix: governed sheet projections now use the sheet frame as the fit bounds.

2. Oversized / tiny-top-left component symptom fixed in the second round.
   - The earlier A3 frame fit made compact component projections appear as tiny content inside a large empty sheet.
   - Fix: the active SVG `viewBox` now comes from tight scene bounds after filtering active projection content.
   - A3 frame data remains available as `sheetChrome.frame`, but the SVG scene and DOM sheet surface use `model.canvas` content dimensions.

3. Sheet 3 root cause found.
   - The M27 sample split field wiring into a separate system/file while the runtime projected one active source path.
   - Fix: the active proof system now includes power, control, and field wiring subjects. The second source remains a non-page support source with distinct spare identities.

4. Smoke harness root cause found.
   - Absolute `800 x 500` sheet-size checks were impossible for fitted A3 inside some Theia panes.
   - Screenshot capture raced with Theia loading/splash rendering and initially captured a spinner.
   - Fix: visual proof now uses viewport-relative fitted-sheet coverage, waits for routed graph DOM, captures the graph workbench webContents rectangle, and guards against loading spinners.

5. New regression coverage added.
   - Runtime test proves M27 field sheet renders field subjects from the active semantic source.
   - Frontend model tests prove the SVG viewBox is tight, does not fall back to `0 0 1680 1188`, and ignores off-frame/overflow reference content.
   - Density contract tests prove the DOM sheet surface is sized from `model.canvas`, not from publication frame metadata.
   - Smoke proof inspects the live DOM SVG viewBox, all three document sheets, route geometry, and screenshot evidence.

Final verification run after the fix:

- `.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaRuntimeProjectionSessionTest.m27*`
- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaM27SampleProjectCompilerTest`
- `yarn --cwd ide/theia-frontend test`
- `yarn --cwd ide build`
- `yarn --cwd ide start:smoke:m27`
- Screenshot inspected: `_bmad-output/implementation-artifacts/m27/proofs/m27-graph-workbench-smoke.png`

Second-round verification evidence:

- Active first-sheet live SVG viewBox: `0 36 624 124`
- Active first-sheet live viewBox dimensions: `624 x 124`
- Active first-sheet center deltas in usable canvas: `0, 0`
- Active first-sheet route count: `2`
- Active first-sheet node-box count: `6`
- Explicit regression guard: live SVG must not be `0 0 1680 1188`

## Do Not Claim Without This Evidence

Do not claim any of the following without an up-to-date smoke run plus screenshot inspection:

- M27 graph view is visually accepted.
- Sheet/frame/linework fidelity is done.
- All M27 stories are complete.
- The smoke test proves the user-visible graph view is correct.
- The current changes are safe to commit.

## Third-Round Lesson: Chrome Borders And Sheet Navigation

Date: 2026-07-20

User-visible symptoms:

1. The graph view showed two gray rectangle borders around compact component content.
   - `border-a` was the `.athena-graph-workbench__sheet` wrapper border/shadow.
   - `border-b` was the `.athena-graph-workbench__sheet-frame` metadata/chrome border/shadow.
   - These were not semantic component geometry. They were HTML helper/chrome layers.

2. The component background looked boxed instead of transparent with the canvas board.
   - Component SVG geometry was already transparent.
   - The visible box came from wrapper chrome, not the component renderer.

3. Switching document sheets could leave non-first sheets off center.
   - The graph workbench reused stale viewport dimensions during sheet switches.
   - Auto-fit now refreshes the live viewport element size before computing the fit transform.

4. Switching from document sheets to cabinet/wiring could make the sheet selector disappear.
   - The selector was rendered only from the current projection model.
   - Cabinet/wiring views may not publish document sheets, but the user's navigation control still needs a way back to the document projection.
   - The widget now remembers the last governed document sheet selector and keeps it visible across non-document projection modes.

Design rule recorded:

- Normal graph canvas state must not show helper rectangles, publication frames, or wrapper borders.
- Hover, focus, selection, and DnD may reveal a dotted helper affordance.
- Dense industrial UI must preserve visual focus on engineering symbols and routes, not container chrome.
- Sheet/frame metadata can stay in the DOM for proof and future publication/export, but it must not become visible canvas noise by default.

Regression evidence added:

- Frontend density contract asserts:
  - `.athena-graph-workbench__sheet` has `border: 0` and `box-shadow: none`.
  - `.athena-graph-workbench__sheet-frame` has `border: 0` and `box-shadow: none`.
  - `.athena-graph-workbench__node-hitbox` is transparent normally.
  - hover/focus on graph elements shows a dotted hitbox affordance.
  - graph workbench caches the last document sheet selector.
  - auto-fit refreshes live viewport size before fitting.

- M27 Electron smoke now asserts:
  - live computed sheet and sheet-frame border widths are all `0px`.
  - live computed sheet and sheet-frame box shadows are `none`.
  - live sheet and sheet-frame backgrounds are transparent.
  - all three document sheets are centered after switching.
  - sheet selector stays visible after switching to `cabinet`.
  - the selector can restore `documentation/sheet/01-power-distribution`.

Latest passing verification evidence:

- `yarn --cwd ide/theia-frontend test --test-name-pattern density`
- `yarn --cwd ide build`
- `yarn --cwd ide start:smoke:m27`

Live smoke proof highlights:

- `sheetChromeVisualProof.sheetBorder*Width`: all `0px`
- `sheetChromeVisualProof.sheetFrameBorder*Width`: all `0px`
- `sheetChromeVisualProof.sheetBoxShadow`: `none`
- `sheetChromeVisualProof.sheetFrameBoxShadow`: `none`
- `allSheetVisualProof[*].visualProof.sheetCenterDeltaX`: `0`
- `allSheetVisualProof[*].visualProof.sheetCenterDeltaY`: `0`
- `sheetSelectorPersistenceProof.selectorVisibleAfterViewSwitch`: `true`
- `sheetSelectorPersistenceProof.alternateViewId`: `cabinet`
- `sheetSelectorPersistenceProof.restoredSheetViewId`: `documentation/sheet/01-power-distribution`

## Final Verified Recap: Source Files Are Not Sheet Views

Date: 2026-07-20

User question:

```text
we have two '.athena' files why sheet list has 3 items?
```

Confirmed answer:

- The M27 sample has two source files:
  - `examples/m27/sample-project/src/01-workspace-semantic-source.athena`
  - `examples/m27/sample-project/src/02-field-assets-not-a-sheet.athena`
- The sheet selector lists generated documentation projection sheets, not `.athena` source files.
- The compiler intentionally creates three documentation sheets:
  - `documentation/sheet/01-power-distribution`
  - `documentation/sheet/02-control-and-plc-logic`
  - `documentation/sheet/03-field-wiring-and-terminal-transition`
- Runtime tests assert those exact three generated sheet IDs.

Architectural lesson:

```text
.athena source file count != projection sheet count
```

Athena keeps `.athena` as semantic source of truth. Document sheets are downstream projection views
derived from the workspace semantic graph. A single source file may generate multiple sheets, and
multiple source files may contribute to one sheet.

Fresh smoke verification after this clarification:

- command: `yarn --cwd ide start:smoke:m27`
- result: passed
- first-sheet live SVG viewBox: `0 12 678 148`
- first-sheet center delta: `1,0`
- sheet selector option count: `3`
- selector texts:
  - `1 - Power Distribution (power_distribution)`
  - `2 - Control And PLC Logic (control_logic)`
  - `3 - Field Wiring And Terminal Transition (field_wiring)`
- sheet/frame borders: all `0px`
- sheet/frame box shadows: `none`
- sheet/frame backgrounds: transparent
- selector remains visible after switching to `cabinet`
- selector restores `documentation/sheet/01-power-distribution`

Do not "fix" the 3-item selector by mapping sheets to source files. If the UI is unclear, improve
the label to communicate "generated document projection sheets."
