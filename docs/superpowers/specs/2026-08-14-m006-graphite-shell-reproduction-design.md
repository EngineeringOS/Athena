# M006 Graphite Shell Reproduction Design

## Status

`APPROVED` on 2026-08-14. The user approved Graphite as Athena's primary
shell reference, Zed as the native GPUI implementation reference, and
QElectroTech as the source of the electrical editor-panel taxonomy.

Approval covers the shell-only milestone defined here. It does not pre-accept
the implementation, screenshots, interaction quality, or later QElectroTech
feature slices.

## Why

Athena's current web and GPUI shells are invented approximations. They contain
a static schematic preview, incomplete panel behavior, and visual decisions
that were not reproduced from the approved references. Continuing to attach
electrical behavior to that shell would make replacement progressively harder.

M006 reverses the order of work. Athena first reproduces Graphite's shell and
workspace behavior with inert electrical placeholders. Only after the shell is
visually and interactively certified may later milestones reconnect
QElectroTech behavior one bounded slice at a time.

## Reference Authority

| Concern | Authority | Evidence |
| --- | --- | --- |
| Shell hierarchy | Graphite | `reference/Graphite/frontend/src/components/window/MainWindow.svelte:22-35` composes title bar, recursive workspace, status bar, dialogs, and tooltips. |
| Recursive panels | Graphite | `reference/Graphite/frontend/src/components/window/PanelSubdivision.svelte:201-251` renders panel groups, document groups, alternating splits, and gutters. |
| Panel tabs and docking | Graphite | `reference/Graphite/frontend/src/components/window/Panel.svelte:387-478` renders tab groups, active tabs, close actions, body content, and docking ghosts. |
| Panel measurements | Graphite | `reference/Graphite/frontend/src/components/window/Panel.svelte:483-658` defines 6px panels, 28px tabs, 2px input radii, and docking feedback; `PanelSubdivision.svelte:255-292` defines 4px gutters. |
| Title and status bars | Graphite | `reference/Graphite/frontend/src/components/window/TitleBar.svelte:35-168` defines a 28px title/menu/window bar; `StatusBar.svelte:27-91` defines a 24px status bar. |
| Theme and typography | Graphite | `reference/Graphite/frontend/src/components/Editor.svelte:89-128` defines the neutral color ramp; `Editor.svelte:253-270` defines Source Sans Pro at 14px. |
| Default document priority | Graphite | `reference/Graphite/editor/src/messages/portfolio/utility_types.rs:376-428` gives the document 80% and a properties/layers column 20%. |
| Native workspace composition | Zed | `reference/zed/crates/workspace/src/workspace.rs:1372-1430` owns center, left/right/bottom docks, title item, and status bar; `workspace.rs:8991-9460` renders and resizes them in GPUI. |
| Rust/native-WASM architecture comparison | OpenCADStudio (supplemental) | `reference/OpenCADStudio/docs/native-vs-web.md:3-12,64-69` documents one Rust application's native/browser boundary and web platform adaptation. M006 may use toolkit-neutral library and architecture choices only as comparison evidence. Its `iced` UI/event/rendering patterns do not map to GPUI and must not be copied; Graphite remains shell authority and QElectroTech remains the electrical oracle. |
| Project panel content | QElectroTech | `reference/qelectrotech-doc/source/users/interface/panels/projects_panel.rst:7-31` defines projects, folios, title blocks, activation, ordering, filtering, and properties entry points. |
| Element-library content | QElectroTech | `reference/qelectrotech-doc/source/users/interface/panels/collections_panel.rst` defines the electrical element collection/search surface. |
| Contextual properties | QElectroTech | `reference/qelectrotech-doc/source/users/interface/panels/selection_properties_panel.rst:7-35` defines selection-specific electrical properties and cross references. |
| Canvas/tool vocabulary | QElectroTech | `reference/qelectrotech-doc/source/users/interface/toolbars.rst:69-139` defines navigation, grid, zoom, selection, conductor, element, and annotation commands. |
| Visual target | Graphite | User-provided `https://static.graphite.art/content/index/gui-demo-painted-dreams__4.avif`. |

Graphite is Apache-2.0 licensed at
`reference/Graphite/LICENSE.txt`. Any source-derived web files must retain the
required copyright/license notices, be marked as modified, and be accompanied
by the applicable Apache-2.0 attribution. Graphite names and trademarks are not
Athena product branding.

## Capabilities

### CAP-M006-001: Reproduced Graphite Window Hierarchy

Intent: replace the current invented shell with Graphite's title-bar,
recursive-workspace, status-bar, and floating-layer hierarchy.

Success: desktop and web show the same ordered regions and measured dimensions;
there is no second Athena-specific toolbar/page layout competing with the
Graphite hierarchy.

### CAP-M006-002: Recursive Docked Workspace

Intent: represent the workspace as backend-owned alternating split nodes and
panel groups with stable identities, sizes, tabs, and active-tab state.

Success: users can resize adjacent groups, activate and reorder tabs, move tabs
between groups, split a group at an edge, close/reopen non-document panels,
reset adjacent sizing, and enter/leave document-focus mode on desktop and web.
The same Rust layout transition tests drive both adapters.

### CAP-M006-003: Graphite Visual System

Intent: reproduce Graphite's density, neutral surfaces, typography, icon sizing,
panel curves, separators, hover/focus/active states, menus, tooltips, and docking
feedback rather than approximating them from memory.

Success: every shell token has a source citation or measured reference value;
desktop/web screenshots pass the metric, clipping, overlap, focus, and human
comparison gates defined below.

### CAP-M006-004: QElectroTech Electrical Panel Taxonomy

Intent: populate the reproduced shell with electrical roles while leaving
domain operations disconnected for this milestone.

Success: the default shell contains these inert surfaces:

- left upper group: `Project` and `Folios`;
- left lower group: `Elements` and `Title Blocks`;
- center document group: one `Folio 1` tab containing an empty electrical page;
- right upper group: `Selection Properties` and `Folio Properties`;
- right lower group: `Diagnostics` and `History`;
- compact electrical tool shelf and tool-options row around the document panel;
- status hints on the left and document/zoom/grid information on the right.

The center is an empty QElectroTech-style folio page with border, grid, and
title-block frame only. It must not contain hard-coded demonstration symbols,
wires, fake selection, or CSS-authored schematic content.

### CAP-M006-005: Equivalent Web and Native Shells

Intent: use Graphite's web shell source as the visual reference implementation
and Zed/gpui-component patterns to reproduce the same contract in native GPUI.

Success: the browser presentation contains no electrical domain state, and the
GPUI presentation contains no alternate layout model. Both consume the same
Rust shell tree and emit the same shell messages.

### CAP-M006-006: Narrow-Viewport Web Shell

Intent: keep the cloud editor operable on a narrow browser without pretending a
desktop dock arrangement fits unchanged.

Success: below the desktop breakpoint the document remains fully visible and
side groups open as Graphite-styled overlay panels from explicit panel buttons.
No fixed-width page or hidden-only control makes project, library, or properties
content unreachable.

## Architecture

### Shared Shell Contract

`athena-application` owns shell presentation state independently of the
electrical document:

```text
WorkspaceShell
  title_bar
  root: ShellNode
  status_bar
  floating_layers

ShellNode = Split { axis, children } | PanelGroup { id, tabs, active_tab }
SplitChild = { node, share }
PanelTab = { id, role, label, closeable }
```

Shell messages cover tab activation/reordering/movement, group splitting,
adjacent resizing/reset, panel close/reopen, document focus, overlay open/close,
and narrow-viewport panel presentation. Shell effects publish structural or
value-only changes. No shell message mutates projects, folios, symbols,
conductors, history, persistence, or selections.

### Web Reproduction

The web implementation may directly adapt the minimal Graphite Svelte shell
primitives needed for M006: main window, title/status bars, recursive panel
subdivision, panel/tab chrome, layout rows/columns, menu/tooltip/dialog layers,
and primitive buttons/labels. Graphite editor, graph, raster/vector, portfolio,
document, tool, and storage logic must not be copied.

Adapted files live under an Athena-owned shell directory, carry Apache-2.0
attribution and modified-file notices, and consume Athena's serialized Rust
shell effects. Svelte is presentation code only; the WASM/application layer
remains state authority.

### Native Reproduction

The desktop implementation uses GPUI and `gpui-component`. It follows Zed's
native composition pattern for title region, center, left/right/bottom docks,
resize handles, focus, overlays, and status region while matching Graphite's
tokens and panel geometry. Zed source is an implementation reference; Athena
does not import Zed's workspace product logic.

### Default Layout

Graphite's 80:20 document/non-document split rule is applied recursively:

```text
Row
  20% Column [Project/Folios, Elements/Title Blocks]
  64% Column [Folio document]
  16% Column [Selection/Folio Properties, Diagnostics/History]
```

Each side column is initially split equally vertically, matching Graphite's
properties/layers default. Shares are backend state, not CSS-only constants.

## Reproduction Rules

1. Read and cite the original Graphite/Zed/QET file before implementing each
   shell component. A similar-looking invention without a source map fails.
2. Copy only shell/presentation code whose behavior is in M006. Remove imports,
   stores, messages, and UI for Graphite drawing/product logic.
3. Preserve Graphite's measured values unless a documented platform constraint
   requires a deviation. Deviations require a spec amendment and evidence.
4. Use Graphite's neutral ramp and Source Sans Pro typography; Athena may replace
   Graphite branding and accent semantics, but may not redesign the shell palette
   during M006.
5. Use repository icon assets or `gpui-component`/Lucide equivalents. No text
   glyphs stand in for standard toolbar or panel icons.
6. Controls without shell behavior are visibly disabled. They do not pretend to
   edit an electrical document.
7. Remove the existing hard-coded web schematic preview and any corresponding
   GPUI demonstration content.
8. Core Rust behavior and tests remain intact but are not expanded in M006.
9. Every touched core Rust file follows the module/public-contract documentation
   rules in `AGENTS.md`.

## Visual Contract

| Token/region | Required baseline |
| --- | --- |
| Title bar | 28px; menu left, draggable/flexible frame center, platform actions right. |
| Status bar | 24px; clipped hints left, separator/fade, compact information right. |
| Panel tab bar | 28px; 6px top corners on active tab with Graphite-style adjoining fillets. |
| Panel frame | 6px radius; near-black chrome; dark-gray body. |
| Split gutter | 4px; 2px hover shape; EW/NS cursor; delayed hover emphasis. |
| Inputs/buttons | 2px radius where Graphite uses rectangular controls. |
| Typography | Source Sans Pro, Arial fallback, 14px baseline, 1.0 line height. |
| Surfaces | Graphite ramp `#111`, `#222`, `#333`, `#444`, `#555` through `#eee`. |
| Document | Dominant center region; no decorative card around the canvas. |
| Floating layers | Menus, tooltips, dialogs, and docking ghosts render above the workspace without affecting split geometry. |

## Interaction Contract

- Pointer drag on a gutter resizes only its two adjacent children, enforces
  Graphite's 100px minimum panel size, and preserves their combined share;
  Escape or secondary-button abort restores the prior shares.
- Double-clicking a gutter within Graphite's 500ms window restores the 80:20
  ratio when exactly one
  adjacent subtree contains the document, otherwise 50:50.
- Tabs activate on click, reorder within a group, move across groups, and expose
  left/right/top/bottom/center docking targets with a visible ghost.
- Closing a non-document panel preserves a restorable position. Closing the
  only document placeholder is disabled in M006.
- Document-focus mode hides non-document groups without destroying their tree
  position and restores them exactly.
- Focus rings, hover, selected, disabled, and keyboard traversal states are
  visible and consistent across web and GPUI.
- At narrow widths, panel overlay open/close and focus return are keyboard and
  touch operable; the electrical page fits or scrolls coherently without
  clipping toolbar/status content.

## Verification Gates

### Source Reproduction Gate

Create a component ledger mapping every Athena shell file to exact Graphite or
Zed source ranges, copied/adapted status, removed product dependencies, and
license treatment. Any shell component without evidence is `FAILED`, not
"inspired".

### Automated Contract Gate

- Rust tests replay one shell-message fixture through native and WASM adapters
  and compare the complete layout tree and semantic effects.
- Property tests cover resize conservation, minimum size, abort restoration,
  tab identity/order, split/prune normalization, close/reopen, and focus-mode
  restoration.
- Web tests cover pointer and keyboard resize, tab activation/reorder, panel
  overlay behavior, focus return, disabled electrical commands, and zero
  console/page errors.
- GPUI tests exercise the same shell operations through rendered controls.

### Visual Gate

Capture fresh:

- Graphite reference shell at 1440x900 where the local checkout can run;
- Athena web at 1440x900 and 390x844;
- Athena desktop at 1440x900.

Record exact bounds for title/status bars, panel/tab/gutter geometry, default
shares, typography, and control sizes. Use side-by-side and overlay comparisons
for density, hierarchy, spacing, surfaces, active/hover/focus states, clipping,
overlap, and text fit. Platform font rasterization may differ; hierarchy and
metrics may not.

The gate fails if any required region is missing, any placeholder appears
functional when it is not, mobile controls are unreachable, desktop/web use
different layout authority, or the result is merely described as
Graphite-like without captured comparison evidence.

### User Gate

The shell cannot be called `reproduced`, `professional`, or accepted until the
user reviews the running desktop and web shells and explicitly accepts the
visual result. Passing tests alone is insufficient.

## Constraints

- Preserve the existing Rust domain/application/persistence work; M006 replaces
  shell presentation and shell-layout behavior only.
- Desktop remains GPUI with `gpui-component`; browser remains a distinct WASM
  frontend over the same Rust application contracts.
- Svelte/JavaScript owns DOM rendering, focus adapters, and browser platform
  APIs only. It does not own electrical state.
- Graphite code is copied only where Apache-2.0 obligations are satisfied and
  only for the shell subset. Graphite product logic and branding are excluded.
- QElectroTech supplies electrical surface names and future behavior, not Qt
  architecture, UI styling, XML, or source code.
- OpenCADStudio supplies supplemental evidence for Rust libraries, module
  design, architecture, and native/WASM boundaries only. Its CAD commands,
  feature set, UI, file formats, 2D/3D product behavior, ribbon, and generic-CAD
  assumptions are excluded. Its `iced` widget, subscription, pane-grid,
  renderer, and event-loop implementation are also excluded from Athena's GPUI
  desktop adapter.
- No Theia/Electron code participates.

## Non-goals

- Adding or reconnecting symbol placement, conductors, selection, transforms,
  title-block editing, save/open, undo/redo, or other QElectroTech operations.
- Copying Graphite's graph, raster/vector, layer, node, portfolio, document,
  storage, collaboration, or tool state machines.
- Copying Zed's editor, project, language, terminal, collaboration, or workspace
  persistence product logic.
- Redesigning the shell beyond documented Graphite-to-electrical substitutions.
- Claiming QElectroTech functional parity.

## Success Signal

M006 succeeds only when Athena web and desktop render and operate the certified
Graphite shell contract, show only inert QElectroTech electrical surfaces, pass
fresh automated and visual gates, retain source/license traceability, and
receive explicit user visual acceptance. The next milestone may then reconnect
exactly one QElectroTech behavior slice.
