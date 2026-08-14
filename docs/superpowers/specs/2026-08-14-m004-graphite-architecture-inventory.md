# M004 Graphite Architecture Inventory

## Evidence Policy

Graphite is the architecture and shell reference, not an Athena dependency.
Each ID describes a boundary Athena must reproduce in its own Rust modules;
generic image/vector features are not implied requirements.

| ID | Graphite responsibility to reproduce | Primary evidence | Athena status |
| --- | --- | --- | --- |
| GRA-APP-001 | A top-level Editor owns a Dispatcher and returns frontend responses from typed messages. | `reference/Graphite/editor/src/application.rs:8`; `reference/Graphite/editor/src/application.rs:49` | `UNRESEARCHED` |
| GRA-DISPATCH-001 | Dispatcher owns handlers and routes typed messages to layout, portfolio, menu, tool, and frontend boundaries. | `reference/Graphite/editor/src/dispatcher.rs:13`; `reference/Graphite/editor/src/dispatcher.rs:235`; `reference/Graphite/editor/src/dispatcher.rs:316` | `UNRESEARCHED` |
| GRA-MSG-001 | UI actions cross the core boundary as typed messages, not direct platform calls into arbitrary document methods. | `reference/Graphite/editor/src/dispatcher.rs:175` | `UNRESEARCHED` |
| GRA-FRONTEND-001 | Core effects return typed `FrontendMessage` values for platform rendering and services. | `reference/Graphite/editor/src/application.rs:49`; `reference/Graphite/editor/src/dispatcher.rs:492` | `UNRESEARCHED` |
| GRA-LAYOUT-001 | Layout is a first-class handler with stable targets, widget IDs, diffs, and callbacks. | `reference/Graphite/editor/src/messages/layout/layout_message_handler.rs:15`; `reference/Graphite/editor/src/messages/layout/layout_message_handler.rs:41` | `UNRESEARCHED` |
| GRA-PANEL-001 | Workspace panel presence/focus is owned by a panel layout model, not CSS-only booleans. | `reference/Graphite/editor/src/dispatcher.rs:268`; `reference/Graphite/editor/src/dispatcher.rs:270` | `UNRESEARCHED` |
| GRA-PORTFOLIO-001 | Portfolio owns active documents and document lifetime independently of platform views. | `reference/Graphite/editor/src/messages/portfolio/portfolio_message_handler.rs:1`; `reference/Graphite/editor/src/dispatcher.rs:317` | `UNRESEARCHED` |
| GRA-DOC-001 | Each document owns persistent data and document-scoped presentation/control state. | `reference/Graphite/editor/src/messages/portfolio/document/document_message_handler.rs:1` | `UNRESEARCHED` |
| GRA-TOOL-001 | Tool handler owns active tool choice and routes tool messages into tool-specific state machines. | `reference/Graphite/editor/src/messages/tool/tool_message_handler.rs:1`; `reference/Graphite/editor/src/messages/tool/tool_messages/select_tool.rs:1` | `PARTIAL` |
| GRA-VIEWPORT-001 | Viewport/PTZ is a dedicated message/state boundary, separate from document geometry. | `reference/Graphite/editor/src/messages/viewport/viewport_message_handler.rs:1` | `PARTIAL` |
| GRA-OVERLAY-001 | Transient overlays are produced by a dedicated document overlay handler and are not saved document data. | `reference/Graphite/editor/src/messages/portfolio/document/overlays/overlays_message_handler.rs:1` | `PARTIAL` |
| GRA-PROPS-001 | Properties are contextual, document-scoped panel layouts refreshed through messages. Athena's layout/lifecycle follows this, while the electrical property plate content must be mapped from QET-ELEM/QET-COND/QET-FOLIO/QET-TITLE/QET-REPORT IDs. | `reference/Graphite/editor/src/messages/portfolio/document/properties_panel/properties_panel_message_handler.rs:1`; `reference/Graphite/editor/src/dispatcher.rs:74` | `UNRESEARCHED` |
| GRA-INPUT-001 | Input mapping/preprocessing is explicit, platform-neutral, and precedes tool/document dispatch. | `reference/Graphite/editor/src/messages/input_mapper/input_mapper_message_handler.rs:1`; `reference/Graphite/editor/src/messages/input_preprocessor/input_preprocessor_message_handler.rs:1` | `UNRESEARCHED` |
| GRA-HISTORY-001 | History transactions are coordinated at message boundaries rather than only button-local state. | `reference/Graphite/editor/src/messages/color_picker/color_picker_message_handler.rs:163` | `PARTIAL` |
| GRA-WASM-001 | Web frontend is a platform adapter over core/frontend messages, not a duplicate editor state machine. | `reference/Graphite/frontend/src:1`; `reference/Graphite/editor/src/application.rs:49` | `UNRESEARCHED` |
| GRA-UX-001 | The modern shell has application/menu controls, icon-led tool affordances, document viewport, dockable panels, contextual properties, and status feedback. This requires screenshot and interaction evidence before visual certification. | User-provided Graphite visual reference: `https://static.graphite.art/content/index/gui-demo-painted-dreams__4.avif` | `UNRESEARCHED` |

## Non-transferable Graphite Scope

Node graphs, image filters, raster/vector artwork features, color tools, and
Graphite file compatibility are not Athena requirements unless separately
approved. Athena copies the editor architecture and shell patterns, then fills
them with electrical commands and QET behavior.

## Required Athena Direction

The next architecture plan must introduce an Athena application/dispatcher
spine before further shell restyling:

`AthenaEditor -> AthenaDispatcher -> AthenaMessage handlers -> AthenaFrontendMessage -> GPUI/WASM adapters`

The existing direct browser calls and monolithic native shell cannot be called
Graphite-mirrored while this spine is absent.
