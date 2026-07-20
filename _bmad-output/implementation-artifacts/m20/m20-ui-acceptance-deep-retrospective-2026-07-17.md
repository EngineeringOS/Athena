# M20 UI Acceptance Deep Retrospective

Updated: 2026-07-17

## Purpose

This retrospective records the failure mode found after the M20 stories were implemented: the
milestone had executable artifacts, but the user-visible IDE proof was not initially acceptable. The
lesson is not only about CSS. It is about making Athena milestones prove their value through the
actual Theia workflow a customer will see.

## What Happened

M20 implemented the engineering presentation foundation: sheet composition, representation families,
layout facts, dense drawing rules, viewport behavior, canonical reveal, acceptance fixtures, and
boundary tests. That work was real, but the first customer-facing explanation and UI proof were weak.

The sample project was not clearly presented as the IDE entry point. Several examples were `.mjs`
fixtures or test drivers, which made the feature look like an internal proof instead of a usable
engineering workflow. The Theia canvas also carried stale UI decisions: `Cabinet Main` information
appeared as sheet/canvas content, bottom controls looked like a separate panel, and component fills
masked the coordinate grid.

The accepted M20 surface is now different:

- the grid is the main canvas coordinate surface
- sheet and component bodies stay transparent over the grid
- `Cabinet Main` appears only in the top information-icon popover
- the popover closes when the user clicks outside it
- top and bottom control containers are transparent overlays
- bottom controls remain canvas controls, not sheet publication content
- outline navigation keeps the same `.athena` editor tab
- the M20 sample project opens as a normal Athena project in the Theia IDE

## Root Causes

### Claims Outran IDE Evidence

The implementation was verified with focused tests before it was proven through the actual IDE
surface. For a presentation milestone, passing scripts is not enough. The visible path must be opened,
inspected, and checked as a product workflow before calling it done.

### The Example Boundary Was Wrong For Users

M20 examples included executable fixtures, but the user expected a full sample project that could be
opened in the IDE. A customer does not write `.mjs` files to prove a sheet workflow. The project needs
real `.athena` sources and an obvious launch command.

### UI Content Had Multiple Render Paths

`Cabinet Main` existed in more than one conceptual place: sheet publication content, bottom
information content, and canvas controls. Moving one path did not remove the others. The correct fix
required tracing all render paths and deciding which layer owned each piece of information.

### The Visual Contract Was Not Precise Enough

The early contract allowed a parent dock to keep panel styling while only the inner controls were
transparent. That still looked wrong. The accepted contract is more exact: the top and bottom parent
containers are transparent and borderless; only the icon buttons may carry button styling.

### The Grid Was Treated Like Decoration

The grid initially behaved like sheet chrome. In an engineering canvas, the grid is a coordinate
surface used for spatial reasoning. It belongs behind the complete stage, not inside individual
components or as decorative sheet content.

### Inline SVG Fills Overrode CSS Intent

CSS transparency was not enough because presentation primitives could still emit fill values that
masked the grid. The presentation-node renderer had to make M20 primitive fills transparent so the
coordinate surface remained visible.

### Build Artifacts Matter In Theia

Fixing TypeScript source does not guarantee the running Theia product uses it. The product bundle and
generated JavaScript must be rebuilt and checked. The Monaco cursor crash also showed that runtime
behavior can come from precompiled package output, not only local source.

## Accepted Invariants

These rules must carry into M21 and later presentation work:

- The Theia IDE is the user-facing proof surface unless a story explicitly says otherwise.
- A presentation milestone is not done until a normal sample project demonstrates it in the IDE.
- Scripts can support the proof, but they are not the proof.
- Canvas controls are workbench chrome, not sheet publication semantics.
- Sheet publication semantics stay in the model; optional inspection details stay in a popover or
  inspector surface.
- The stage grid is the coordinate authority for the canvas.
- Sheet and component bodies must not hide the coordinate grid unless a future story explicitly
  introduces a governed drawing mode that requires opaque regions.
- Negative UI assertions are required for removed or forbidden elements.
- Source, bundle, and runtime logs must all be checked before declaring an IDE fix complete.

## What Was Cleaned Up

- Updated M20 usage docs so `Cabinet Main` is described as a top information-icon popover, not a
  bottom dock.
- Documented the accepted transparent-overlay and grid behavior in the M20 example README.
- Marked all M20 story files as `Status: done`.
- Aligned `m20/sprint-status.yaml` with the completed story state.
- Kept stale canvas classes guarded by negative density-contract assertions:
  `sheet-grid`, `graph-workbench__grid`, `sheet-title-block`, `sheet-cross-reference-marker`,
  `floating-panel`, and `overlay-toggle`.

## Verification Evidence

The latest accepted UI cleanup was verified with:

```powershell
node --test ide/theia-frontend/scripts/athena-ide-density-contract.test.mjs
yarn workspace @engineeringood/athena-theia-frontend build
yarn workspace @engineeringood/athena-theia-product build
yarn start:smoke
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
```

The IDE was also launched with:

```powershell
Set-Location ide
yarn start:m20
```

The runtime log showed the M20 sample project opened from `examples/m20/sample-project` with zero
diagnostics for the opened `.athena` file during that run.

## Residual Risks

- The Theia backend log still contains an unrelated startup RPC error:
  `TypeError: this.target[method] is not a function`. It did not block the accepted M20 UI proof, but
  it should be investigated separately before it becomes normal background noise.
- The Monaco cursor crash was fixed in `node_modules` source and compiled output. That is fragile.
  A persistent patch or upstream override should be added if this dependency is regenerated.
- The UI proof still relies mostly on source/static contract tests and manual IDE launch. M21 should
  add screenshot or Playwright-style evidence for the graph workbench so visual regressions are
  caught before user review.
- BMad's `resolve_config.py --key agents` failed in this Windows console because the output could not
  be encoded as GBK. Workflow tooling should force UTF-8 output for Windows shells.

## Preventive Actions

- Every future presentation story must name the visible IDE proof path in its acceptance criteria.
- Every sample project must be openable as a normal Athena project with real `.athena` source files.
- Before moving visible UI content, search for all render paths and all tests that preserve the old
  behavior.
- Add negative tests for removed UI. A stale class or stale label is a regression, not harmless CSS.
- Treat documentation as part of the product proof. Usage docs must match the current UI before a
  milestone is closed.
- Rebuild the frontend package and product bundle after Theia UI changes.
- Check runtime logs after launch and explicitly separate blocking product errors from unrelated
  background errors.

## Next Milestone Preparation

M21 should start from a stricter rule: presentation quality work begins with the visible IDE workflow.
Kernel contracts and test fixtures remain necessary, but the first acceptance question is whether an
engineer can open the sample project, inspect the source, see the schematic, navigate the outline, and
trust the rendered surface without needing implementation knowledge.
