# Athena M46 Retrospective

Date: 2026-08-12

## Delivered Usage

Engineers write semantic Connection and Net intent in Athena source. Athena validates Port roles and electrical
requirements, compiles one Connection IR, projects page-specific route geometry, then paints a professional
engineering document. A route move changes Page presentation only; reconnect changes Engineering Reality through
one accepted source transaction.

## What Worked

- Keeping `EngineeringConnection`/`EngineeringNet`, Connection IR, route planning, and `SceneConnection` separate
  prevented line geometry from becoming hidden electrical truth.
- Direct local packages supplied SVG geometry and Port anchors without importing `.elmt`, HTML, or XML runtime authority.
- One real M46 example exercised package resolution, multi-page Folio, typed operations, reopen, export, and scale.
- Product verification caught stale build and workspace failures better than unit tests alone.
- Fixed screen-space line widths, tiny topology markers, white page interior, and invisible hit geometry moved output
  toward IEC document grammar.

## What Failed First

- Single Sheet Companion assumptions broke multi-page staging: an active-page override erased other page grid facts.
  Correction: load/apply every Folio Page Companion and replace only matching page data.
- Sheet mapping later erased route constraints from a prior Page. Correction: retain existing constraints when a Page
  does not author a replacement.
- Undo/Redo scene validation omitted active Page identity. Correction: compile with affected Page and matching style.
- Export verifier retained M45 constant `10` while current M46 canonical route geometry contains 11 orthogonal
  segments. Correction: inspect generated SVG before changing proof; update M46-only expected segment count.
- Stale Theia layout generated a preview-widget warning; clean workspace product proof still passed. Startup/session
  noise remains recorded rather than declared resolved.
- A stale compiler-owned `athena.lock` created `Presentation Internal error` in the LSP because source revision
  validation rejected resolver output. Correction: materialize lock through compiler authority; never hand-edit it.
- Concrete Sheet editors displayed a nested Folio bar. Correction: page controls are a `.folio.athena` concern only;
  `.sheet.athena` opens one standalone canvas editor. Regression test added at the widget boundary.

## Architectural Laws Kept

- Source owns engineering meaning; packages own admitted geometry and anchor references only.
- `ConnectionProjection` and Route Plan are disposable derived facts. `SceneConnection` is sole paint contract.
- Folio orders Page projections. Each Page opens as its own normal editor canvas tab; no nested canvas page authority.
- `athena.lock` is resolver output. Generated lock state is verified before diagnosing LSP, grammar, Tree-sitter, or
  paint failures.
- Reconnect is Engineering edit; route adjustment is Presentation edit. Both use full source revision transactions.
- Renderer, Konva, DOM, SVG, viewport, and mouse state never validate or create connectivity.
- No compatibility aliases, deprecated route paths, or old generic relationship lowering.

## M47 Carry-Forward

- Resolve clean-start RPC notification/layout-restore warning before expanding editor workflows.
- Add product automation that can inspect the exact active concrete editor widget, not only the generic Presentation
  automation seam. Current frontend widget regression test proves the nested-tab rule; generic proof cannot inspect an
  inactive widget safely.
- Reduce editor chrome into a small purposeful command set; preserve style controls that work and remove only proven
  redundant controls.
- Continue visual comparison against engineering reference before declaring document quality. Use realistic multi-page
  professional examples, never toy fixtures.
- Treat cross-page interruption/reference as current M46 behavior. Add Smart Connect only with a new semantic,
  transaction, and user interaction contract.
- Consider external route/layout engines only as disposable planner adapters after measured need; they cannot own
  Connection IR or page truth.

## Closure Finding

No unresolved critical or high implementation finding blocks M46 closure. One non-blocking startup/log warning remains
tracked for M47.
