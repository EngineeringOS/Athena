# M004 Graphite Architecture Inventory

## Evidence Policy

Graphite is the architecture and shell reference, not an Athena dependency.
Each ID describes a boundary Athena may reproduce in its own Rust modules.
`EVIDENCED` certifies only the cited Graphite responsibility. It does not mean
Athena implements that responsibility, and it does not transfer Graphite's
image/vector/node-graph product behavior.

The line-level companion is
[`../research/2026-08-14-m004-graphite-editor-architecture-inventory.md`](../research/2026-08-14-m004-graphite-editor-architecture-inventory.md).

| ID | Graphite responsibility to reproduce | Primary evidence | Reference status |
| --- | --- | --- | --- |
| GRA-APP-001 | A top-level `Editor` owns a `Dispatcher`; typed input messages return typed frontend effects. | `reference/Graphite/editor/src/application.rs:8-30`; `reference/Graphite/editor/src/application.rs:49-57` | `EVIDENCED` |
| GRA-DISPATCH-001 | The dispatcher owns ordered message queues and specialized handlers, and coalesces defined deferred work. | `reference/Graphite/editor/src/dispatcher.rs:12-41`; `reference/Graphite/editor/src/dispatcher.rs:105-180`; `reference/Graphite/editor/src/dispatcher.rs:347-350` | `EVIDENCED` |
| GRA-MSG-001 | UI actions cross the editor boundary as typed message families and handler contexts, not arbitrary platform calls into document methods. | `reference/Graphite/editor/src/messages/message.rs:4-54`; `reference/Graphite/editor/src/dispatcher.rs:175-240` | `EVIDENCED` |
| GRA-FRONTEND-001 | Core effects return typed `FrontendMessage` values for platform rendering and services. | `reference/Graphite/editor/src/messages/frontend/frontend_message.rs:23-45`; `reference/Graphite/editor/src/dispatcher.rs:215-217`; `reference/Graphite/editor/src/application.rs:49-53` | `EVIDENCED` |
| GRA-LAYOUT-001 | Backend-owned widget layouts use stable targets and widget IDs, callbacks, structural diffs, and typed layout effects. | `reference/Graphite/editor/src/messages/layout/layout_message.rs:4-32`; `reference/Graphite/editor/src/messages/layout/layout_message_handler.rs:15-79`; `reference/Graphite/editor/src/messages/layout/layout_message_handler.rs:469-518` | `EVIDENCED` |
| GRA-PANEL-001 | Workspace panel presence, tabs, splits, focus mode, restoration, and resize state are owned by a recursive panel-layout model. | `reference/Graphite/editor/src/messages/portfolio/utility_types.rs:38-133`; `reference/Graphite/frontend/src/components/window/PanelSubdivision.svelte:54-89`; `reference/Graphite/frontend/src/components/window/PanelSubdivision.svelte:134-184` | `EVIDENCED` |
| GRA-PORTFOLIO-001 | A portfolio owner controls document lifetime, ordering, active identity, persistence coordination, and workspace layout independently of platform views. | `reference/Graphite/editor/src/messages/portfolio/portfolio_message_handler.rs:47-107`; `reference/Graphite/editor/src/messages/portfolio/portfolio_message.rs:10-105` | `EVIDENCED` |
| GRA-DOC-001 | Each document owns persistent data plus document-scoped navigation, overlays, properties, and presentation/control state. | `reference/Graphite/editor/src/messages/portfolio/document/document_message_handler.rs:73-120`; `reference/Graphite/editor/src/messages/portfolio/document/document_message_handler.rs:130-136` | `EVIDENCED` |
| GRA-TOOL-001 | The tool handler owns active-tool lifecycle and routes tool messages into explicit state machines with abort, overlay, hint, cursor, and transaction behavior. | `reference/Graphite/editor/src/messages/tool/tool_message_handler.rs:98-180`; `reference/Graphite/editor/src/messages/tool/tool_messages/artboard_tool.rs:81-102`; `reference/Graphite/editor/src/messages/tool/tool_messages/artboard_tool.rs:295-315` | `EVIDENCED` |
| GRA-INPUT-001 | Raw platform events become normalized input messages before semantic mapping and tool/document dispatch. | `reference/Graphite/editor/src/messages/input_preprocessor/input_preprocessor_message_handler.rs:8-125`; `reference/Graphite/editor/src/messages/input_mapper/input_mapper_message_handler.rs:8-52` | `EVIDENCED` |
| GRA-VIEWPORT-001 | Platform viewport bounds/device scale are distinct from document pan/zoom presentation state. | `reference/Graphite/editor/src/messages/viewport/viewport_message_handler.rs:6-123`; `reference/Graphite/editor/src/messages/portfolio/document/navigation/navigation_message_handler.rs:17-74` | `EVIDENCED` |
| GRA-OVERLAY-001 | Transient interaction visuals use a dedicated document overlay lifecycle and are not persisted document data. | `reference/Graphite/editor/src/messages/portfolio/document/overlays/overlays_message_handler.rs:4-99`; `reference/Graphite/editor/src/messages/tool/utility_types.rs:155-205` | `EVIDENCED` |
| GRA-PROPS-001 | Contextual properties are backend-owned, document-scoped layouts refreshed through messages and targeted panel diffs. | `reference/Graphite/editor/src/messages/portfolio/document/properties_panel/properties_panel_message_handler.rs:9-67`; `reference/Graphite/frontend/src/stores/portfolio.ts:139-168` | `EVIDENCED` |
| GRA-HISTORY-001 | Gesture history uses explicit start/end/commit/cancel/abort transaction messages coordinated with document updates and persistence. | `reference/Graphite/editor/src/messages/portfolio/document/document_message.rs:203-214`; `reference/Graphite/editor/src/messages/portfolio/document/document_message_handler.rs:1341-1387` | `EVIDENCED` |
| GRA-WASM-001 | The browser wrapper owns a persistent Rust editor, maps JS calls to typed messages, serializes frontend effects, and keeps the frontend as an effect subscriber. | `reference/Graphite/frontend/wrapper/src/editor_wrapper.rs:31-141`; `reference/Graphite/frontend/wrapper/src/editor_commands.rs:1-39`; `reference/Graphite/frontend/src/subscriptions-router.ts:1-99` | `EVIDENCED` |
| GRA-PLATFORM-001 | Native and browser adapters preserve the same editor/frontend message semantics while intercepting platform-specific effects at the adapter boundary. | `reference/Graphite/desktop/wrapper/src/message_dispatcher.rs:9-76`; `reference/Graphite/desktop/wrapper/src/intercept_frontend_message.rs:8-89`; `reference/Graphite/frontend/wrapper/src/native_communication.rs:1-98` | `EVIDENCED` |
| GRA-UX-001 | The shell hierarchy is title bar, recursive docked workspace, status bar, and floating layers; panels support tabs, lifecycle, movement, docking, and resizing. | `reference/Graphite/frontend/src/components/window/MainWindow.svelte:22-35`; `reference/Graphite/frontend/src/components/window/Panel.svelte:16-170`; `reference/Graphite/frontend/src/components/window/PanelSubdivision.svelte:54-184` | `EVIDENCED` for structure; visual fidelity remains `UNKNOWN` |

## Athena Target Spine

The next authorized implementation must introduce this relationship before
further product or shell work:

```text
PlatformEvent
  -> AthenaInputMessage
  -> AthenaEditor
  -> AthenaDispatcher
  -> Portfolio/Document/Layout handlers
  -> AthenaFrontendMessage
  -> GPUI or WASM adapter
```

Contextual electrical plates follow a second mapped path:

```text
electrical selection/document state
  -> QET-evidenced plate model
  -> backend-owned panel layout/diff
  -> AthenaFrontendMessage
  -> target-specific GPUI/browser view
  -> typed callback message
  -> AthenaDispatcher
```

## Non-transferable Graphite Scope

The following are explicitly excluded unless a later milestone separately
approves them:

- node graphs, raster/vector artwork models, artboards, filters, and color tools;
- Graphite file compatibility and image-rendering behavior;
- Svelte as Athena's desktop UI or Graphite's embedded-web desktop wrapper;
- `web_sys` or native renderer objects inside Athena's platform-neutral core;
- Graphite's exact panel taxonomy, canvas tilt, and generic vector properties.

## Unknown / Not Yet Certified

- Full initialization ordering, asynchronous scheduling, and retry behavior.
- Workspace-layout persistence migration and malformed-layout recovery.
- Complete widget validation, focus, accessibility, keyboard, and commit rules.
- Native/web overlay DPI and coordinate equivalence.
- History storage format, crash recovery, branching, and memory limits.
- Exact visual geometry, density, colors, icons, focus/hover states, responsive
  behavior, and interaction timing.

These unknowns block any future task that depends on their exact behavior. They
must not be resolved by intuition.
