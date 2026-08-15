# Athena Rewrite Guardrails

## Product Direction

- Athena is a source-based Rust product fork using Graphite's complete
  Apache-2.0 codebase as its initial editor and shell implementation.
- The product domain is electrical schematics only. Generic diagramming is not
  a first-class scope for this rewrite.
- `reference/qelectrotech-source-mirror` and `reference/qelectrotech-doc` are
  behavior references only. Do not preserve or reuse their C++/Qt architecture,
  XML file formats, user interface, or implementation details.
- Treat the reference set as two explicit eras. QElectroTech source and user
  docs are the old-world electrical feature, workflow, and terminology
  inventory. `reference/zed`, `reference/gpui-component`, and
  `reference/Graphite` are the new-age foundation references for Rust module
  boundaries, editor-shell composition, component behavior, interaction
  semantics, rendering/presentation separation, and desktop-first UX. Graphite
  is copied as the working product base; its UI and interaction remain intact
  while its graphics-domain functions are replaced one by one with Athena's
  electrical-schematic logic. QElectroTech, Zed, gpui-component, and
  OpenCADStudio remain evidence sources and are not source-copy donors.
- Preserve Graphite's Apache-2.0 license, copyright, repository revision, and
  modification notices. Never describe copied Graphite code as clean-room or
  Athena-authored. Do not selectively redraw or approximate the Graphite shell
  before the unmodified imported baseline runs and is visually certified.
- Legacy Theia/Electron code under `ide/` is out of scope. Do not extend,
  integrate with, or use it as an architectural constraint for the rewrite.

## Target Architecture

- Import Graphite revision `461ddbc8726c587a8abc536cab301b0b2206a54c` as the
  initial root Rust workspace, including its pinned lockfiles and build tools.
  Update dependencies only after the unchanged baseline is reproducible.
- Keep Athena's electrical core platform-neutral and compile it for native and
  `wasm32-unknown-unknown` when it is integrated into the Graphite base.
- The core owns the domain model, validation, geometry, deterministic commands,
  undo/redo, persistence schema/migrations, rendering scene data, and sync
  contracts. It must not depend on UI, filesystem, browser, or cloud APIs.
- The imported Graphite Svelte/WASM frontend and desktop wrapper are the initial
  shared product shell. Do not maintain a parallel hand-built GPUI/Svelte shell.
  Zed, GPUI, and `gpui-component` remain optional later references, not M007
  runtime constraints.
- Keep electrical behavior in Rust. Svelte maps browser/desktop-wrapper events
  and renders Graphite frontend messages; it must not become a second electrical
  state authority.
- Provide local-first persistence first. Design commands and document identities
  for later cloud projects, sharing, realtime collaboration, history, and
  conflict resolution from the outset.

## Design Rules

- Prefer a newly designed, versioned serde-backed document format; do not retain
  QElectroTech XML compatibility unless the product direction explicitly changes.
- Keep domain state, command/editing state, presentation state, and platform
  adapters separated.
- Treat desktop and web as equivalent product targets: shared domain behavior
  and interaction semantics, with platform-specific UI and service adapters.
- Use QElectroTech as the electrical-schematic feature inventory and behavior
  oracle, then redesign for ergonomic, composable, testable workflows rather
  than cloning its UI. When legacy behavior conflicts with modern interaction
  quality, preserve the electrical outcome and redesign the interaction.
- Use Graphite as the imported editor-shell implementation and Zed as a
  supplemental architecture reference: keep document
  state, tools, viewport/canvas, commands, rendering, and platform wrappers as
  separate layers, with explicit state ownership and composable interactions.
  Existing Graphite source retains its original provenance. New Athena code
  must be clearly separated from unchanged or modified Graphite code.
- Use `reference/OpenCADStudio` only as a supplemental evidence source for Rust
  libraries, module design, architectural boundaries, and native/WASM
  engineering patterns. It does not replace Graphite as shell authority or
  QElectroTech as the electrical behavior oracle. Do not copy its CAD feature
  set, command semantics, UI, file formats, 2D/3D product behavior, ribbon, or
  other generic-CAD assumptions. Its `iced` widget, event, subscription,
  pane-grid, and renderer patterns are toolkit-specific and must not enter the
  GPUI desktop adapter. Athena remains an EPLAN/QElectroTech-class
  electrical-schematic product only; Zed, GPUI, and `gpui-component` remain the
  native implementation authorities.
- For shell work, mirror Graphite's proven separation of document state,
  message/tool routing, viewport/canvas, overlays, panels, and platform
  wrappers first. Then fill those boundaries with Athena's electrical
  schematic kernel and QElectroTech-derived feature outcomes.
- Graphite is the visual-layout and panel-system reference: application shell,
  docked panel geometry, document viewport, tool shelf, panel lifecycle, and
  modern interaction affordances follow Graphite. QElectroTech is the
  electrical panel-plate reference: panel content, electrical categories,
  typed fields, property groups, report inputs, and user workflows must expose
  the QElectroTech-equivalent domain meaning inside the Graphite-style layout.
  Do not copy QElectroTech's Qt visual chrome; do not fill Graphite-like panels
  with invented generic fields.
- Every core Rust file touched in a milestone must be self-documenting: include
  a module-level `//!` summary, document public types/functions that define a
  cross-crate contract, and add short comments before non-obvious state,
  geometry, validation, persistence, or platform-boundary logic. Do not add
  comments that merely restate the code.

## Current First Milestone

- Deliver the core schematic-authoring loop: projects and sheets, symbol library
  placement, connections, selection and transforms, pan/zoom/snap,
  undo/redo, local save/reopen, and proof through both desktop and WASM shells.

## Execution Discipline

- Use a strict development flow for all non-trivial work:
  \`spec -> plan -> implementation -> verification\`.
- Do not jump straight into implementation for new milestones, parity slices,
  or architectural changes. First write/update a spec in
  \`docs/superpowers/specs/\`, then write/update a plan in
  \`docs/superpowers/plans/\`, then implement, then record verification evidence
  in \`docs/superpowers/verification/\` when the slice warrants it.
- When a task is only partially completed, update the relevant plan checkboxes
  precisely. Do not mark work done unless the repository state and verification
  output prove it.
- Before claiming a milestone or slice is complete, run fresh verification in
  the current workspace and record any intentionally skipped items explicitly.

## Milestone Naming

- All spec and plan documents must use sortable milestone-prefixed names:
  \`YYYY-MM-DD-mNNN-slug.md\`.
- \`mNNN\` is a zero-padded milestone identifier such as \`m001\`, \`m002\`, \`m010\`.
- The same milestone prefix should be reused across related spec, plan, and
  verification artifacts for the same slice whenever practical.
- Prefer creating a new milestone document over overloading an old one when the
  scope changes materially.
