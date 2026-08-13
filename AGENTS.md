# Athena Rewrite Guardrails

## Product Direction

- Athena is a clean-room Rust rewrite inspired by QElectroTech's user-facing
  schematic-authoring capabilities, workflows, and interaction quality.
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
  semantics, rendering/presentation separation, and desktop-first UX. Never
  let the old-world implementation dictate the new-world architecture or UI.
- Prefer learning from and reproducing proven reference patterns when that is
  the fastest path: inspect the reference code and behavior, record the
  evidence, then implement the applicable architecture and interaction in
  Athena's own Rust modules. Copying a proven design pattern is encouraged;
  copying source files, implementation code, dependencies, or unrelated
  product assumptions is not.
- Legacy Theia/Electron code under `ide/` is out of scope. Do not extend,
  integrate with, or use it as an architectural constraint for the rewrite.

## Target Architecture

- This is a greenfield project. At implementation time, select the latest
  stable Rust toolchain and latest compatible stable releases of GPUI,
  `gpui-component`, and supporting crates. Do not copy stale versions or lock
  files from the reference checkouts without checking their current release and
  compatibility status.
- Build a Rust workspace with a platform-neutral core compiled for native and
  `wasm32-unknown-unknown` targets.
- The core owns the domain model, validation, geometry, deterministic commands,
  undo/redo, persistence schema/migrations, rendering scene data, and sync
  contracts. It must not depend on UI, filesystem, browser, or cloud APIs.
- The desktop application uses GPUI; `reference/gpui-component` may be used for
  desktop UI components.
- The browser is a distinct WASM frontend over the same Rust core. Do not assume
  GPUI or `gpui-component` compiles to, or is the UI toolkit for, the web.
- For the MVP, keep the browser layer deliberately small: HTML/JavaScript may
  host the WASM module, map browser events and platform APIs, and provide the
  minimum shell needed to render the editor. Do not move schematic behavior or
  duplicate application state into TypeScript/JavaScript.
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
- Use Zed and Graphite as the primary editor-shell references: keep document
  state, tools, viewport/canvas, commands, rendering, and platform wrappers as
  separate layers, with explicit state ownership and composable interactions.
  Use `gpui-component` for native component patterns where it fits. These are
  architectural and UX references, not product dependencies or source to copy.
- For shell work, mirror Graphite's proven separation of document state,
  message/tool routing, viewport/canvas, overlays, panels, and platform
  wrappers first. Then fill those boundaries with Athena's electrical
  schematic kernel and QElectroTech-derived feature outcomes.
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
