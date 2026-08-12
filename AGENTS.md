# Athena Rewrite Guardrails

## Product Direction

- Athena is a clean-room Rust rewrite inspired by QElectroTech's user-facing
  schematic-authoring capabilities, workflows, and interaction quality.
- The product domain is electrical schematics only. Generic diagramming is not
  a first-class scope for this rewrite.
- `reference/qelectrotech-source-mirror` and `reference/qelectrotech-doc` are
  behavior references only. Do not preserve or reuse their C++/Qt architecture,
  XML file formats, user interface, or implementation details.
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
  than cloning its UI.
- Use Graphite as an editor-shell reference: keep document state, tools,
  viewport/canvas, commands, rendering, and platform wrappers as separate
  layers. Graphite is an architectural reference, not a product dependency.

## Current First Milestone

- Deliver the core schematic-authoring loop: projects and sheets, symbol library
  placement, connections, selection and transforms, pan/zoom/snap,
  undo/redo, local save/reopen, and proof through both desktop and WASM shells.
