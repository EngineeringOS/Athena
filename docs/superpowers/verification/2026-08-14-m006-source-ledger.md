# M006 Shell Source, License, and Dependency Ledger

## Status

`ACTIVE` as of 2026-08-15. This ledger is the source-reproduction gate for
M006. `PLANNED` rows must be changed to `ADAPTED`, `REIMPLEMENTED`, or `REMOVED`
when the corresponding file lands. A shell file without a row fails the gate.

## Reference Revisions

| Reference | Revision | Permitted role | Source-copy policy |
| --- | --- | --- | --- |
| Graphite | `461ddbc8726c587a8abc536cab301b0b2206a54c` | Primary shell hierarchy, panel behavior, geometry, and visual tokens | Minimal web shell adaptation permitted under Apache-2.0 with per-file modified notices and `web/src/shell/NOTICE.md` |
| Zed | `cdf33ac25e3c9b65294d22a027e50eca6e8ab53a` | GPUI workspace composition evidence | No source copied; Athena reimplements the applicable composition against its own shell contract |
| gpui-component | `46576ae7aa63ab9db9923e5d8c32228ed330a259` | Native GPUI component/API evidence | Use as a dependency/reference under Apache-2.0; no reference source copied into Athena |
| QElectroTech docs/source | workspace checkout | Electrical panel names and behavior inventory | No Qt/C++/XML/UI source copied; inert M006 labels only |
| OpenCADStudio | `2c08b92267a82156d6ecd05bd79788b2ed6fa49f` | Supplemental Rust library and native/WASM architecture comparison | GPL-3.0 source is not copied. CAD features, UI, commands, formats, and all `iced` widget/event/rendering code are excluded |

Graphite's checkout contains `reference/Graphite/LICENSE.txt` (Apache License
2.0). Zed and gpui-component include Apache-2.0 terms, but M006 copies neither
reference implementation. OpenCADStudio is GPL-3.0 and is evidence only.

## Measured Evidence

| Contract | Exact evidence | Accepted M006 value |
| --- | --- | --- |
| Window order | Graphite `MainWindow.svelte:22-35` | title bar, recursive workspace, status bar, then floating dialogs/tooltips |
| Recursive groups/splits | Graphite `PanelSubdivision.svelte:201-251` | document/panel groups and alternating recursive splits |
| Gutter | Graphite `PanelSubdivision.svelte:254-272` | 4px allocation, 2px radius, EW/NS cursor, delayed hover |
| Panel/tab/docking | Graphite `Panel.svelte:387-480` | tab activation/reorder/close body and five docking ghosts |
| Panel geometry | Graphite `Panel.svelte:483-658` | 6px panel radius, 28px tabs, 2px inputs, active fillets, insertion/docking feedback |
| Title bar | Graphite `TitleBar.svelte:41-168` | menu, flexible draggable frame, platform actions; M006 baseline height 28px |
| Status bar | Graphite `StatusBar.svelte:33-91` | 24px, clipped hints, separator/fade, compact information |
| Neutral ramp | Graphite `Editor.svelte:89-120` | `#111` through `#eee` plus black/white endpoints |
| Typography | Graphite `Editor.svelte:256-267` | Source Sans Pro/Arial, 14px, line-height 1 |
| Native composition | Zed `workspace.rs:1372-1430,8997-9460` | center, docks, title item, status, modal/toast layers, resize/focus composition |
| Project/Folio roles | QET `projects_panel.rst:7-31` | Project, Folios, Title Blocks, activation/order/property entry points |
| Element library | QET `collections_panel.rst:7-28` | Elements collections and search surface |
| Selection plate | QET `selection_properties_panel.rst:7-35` | contextual electrical properties and cross-reference surface |
| Tool vocabulary | QET `toolbars.rst:69-139` | selection/navigation/grid/zoom/conductor/element/annotation labels; disabled in M006 |
| Architecture comparison | OpenCADStudio `docs/native-vs-web.md:3-20,52-70` | platform paths versus browser bytes/overlays only; the documented `iced` implementation is not portable to GPUI |

## Athena File Map

`Removed dependencies` names reference-product state that must not cross into
Athena. `License treatment` is mandatory before a row can leave `PLANNED`.

| Athena file | Authority and exact range | Status | Removed dependencies | License treatment |
| --- | --- | --- | --- | --- |
| `rust/crates/application/src/shell_model.rs` | Graphite `PanelSubdivision.svelte:201-251`; Graphite editor `utility_types.rs:376-428` | REIMPLEMENTED clean-room Rust contract; default tree and invariants covered by `shell_model_contracts.rs` | Graphite portfolio/documents/stores; all platform APIs | Athena-authored; citations in module docs, no copied source |
| `rust/crates/application/src/shell.rs` | Graphite `PanelSubdivision.svelte:1-199`; `Panel.svelte:1-386` behavior oracle | REIMPLEMENTED clean-room state machine; six transition tests cover tab, split, resize, reset, close/reopen, focus, overlay, pruning, and invalid IDs | DOM events, Graphite messages, drawing state | Athena-authored; no copied source |
| `rust/crates/application/src/plate.rs` | Existing Athena M005 plate logic; QET panel docs above | REIMPLEMENTED by moving the existing plate handler intact and removing the obsolete fixed workspace; five M005 plate tests pass | obsolete fixed three-column workspace | Existing Athena source; no third-party copy |
| `web/src/App.svelte` | Graphite `Editor.svelte:76`; `MainWindow.svelte:22-35` | REIMPLEMENTED shell-only Svelte mount over the Rust effect store | Graphite editor/product stores | Athena-authored composition; no copied source |
| `web/src/lib/athena.js` | Athena WASM protocol; OpenCADStudio `native-vs-web.md:52-62` comparison only | REIMPLEMENTED persistent WASM shell dispatcher | filesystem paths and `iced` application state | Athena-authored; no copied GPL source |
| `web/src/lib/shell-store.svelte.js` | Athena `ShellEffect` contract | REIMPLEMENTED effect reducer with no topology authority | project/folio/symbol/conductor state | Athena-authored; no copied source |
| `web/src/shell/MainWindow.svelte` | Graphite `MainWindow.svelte:22-35` | ADAPTED title/workspace/status/floating hierarchy | Graphite portfolio/dialog/product stores | Apache-2.0 derived-file notice plus `NOTICE.md`; marked modified |
| `web/src/shell/TitleBar.svelte` | Graphite `TitleBar.svelte:41-168` | ADAPTED 28px menu/frame/action composition | Graphite subscriptions/editor window commands/branding | Apache-2.0 derived-file notice plus `NOTICE.md`; marked modified |
| `web/src/shell/StatusBar.svelte` | Graphite `StatusBar.svelte:33-91` | ADAPTED 24px clipped hints/info composition | Graphite widget subscriptions | Apache-2.0 derived-file notice plus `NOTICE.md`; marked modified |
| `web/src/shell/PanelSubdivision.svelte` | Graphite `PanelSubdivision.svelte:201-292` | ADAPTED recursive split/group projection | portfolio documents/editor messages | Apache-2.0 derived-file notice plus `NOTICE.md`; marked modified |
| `web/src/shell/PanelGroup.svelte` | Graphite `Panel.svelte:387-658` | ADAPTED tabs, keyboard traversal, close/reorder/move/split, and docking feedback | panel component registry and drawing panels | Apache-2.0 derived-file notice plus `NOTICE.md`; marked modified |
| `web/src/shell/Gutter.svelte` | Graphite `PanelSubdivision.svelte:1-199,254-272` | ADAPTED pointer resize, abort, commit, and reset behavior over Rust messages | Graphite backend bridge | Apache-2.0 derived-file notice plus `NOTICE.md`; marked modified |
| `web/src/shell/FloatingLayers.svelte` | Graphite `MainWindow.svelte:30-35`; `Panel.svelte:471-479` | ADAPTED final stacking layer placeholder for Rust overlay state | Graphite dialogs/tooltips/product state | Apache-2.0 derived-file notice plus `NOTICE.md`; marked modified |
| `web/src/shell/shell.css` | Graphite `Panel.svelte:483-658`; `PanelSubdivision.svelte:254-292`; `Editor.svelte:89-120,256-267` | ADAPTED measured ramp, typography, panel, tab, gutter, title, and status tokens | Graphite drawing/product selectors | Apache-2.0 derived-file notice plus `NOTICE.md`; marked modified |
| `web/src/electrical/PanelContent.svelte` | QET panel RST files above | REIMPLEMENTED nine role-specific inert surfaces over Rust active-tab state | Qt widgets and active electrical commands | Athena-authored from behavior docs; no C++/Qt copy |
| `web/src/electrical/ToolShelf.svelte` | QET `toolbars.rst:69-139` | REIMPLEMENTED disabled selection/move/conductor/element/annotation controls | QET icons/actions and all editing behavior | Athena-authored labels with `@lucide/svelte` icons |
| `web/src/electrical/ToolOptions.svelte` | QET `toolbars.rst:69-139` | REIMPLEMENTED disabled grid/zoom/conductor options | QET toolbar implementation | Athena-authored; no copied source |
| `web/src/electrical/EmptyFolio.svelte` | M006 spec empty-folio contract | REIMPLEMENTED border/grid/unfilled-title-block page only | demo symbols/wires/selections | Athena-authored; no copied source |
| `rust/crates/desktop/src/shell/tokens.rs` | Graphite `Editor.svelte:89-120,256-267`; panel measurements above | PLANNED clean-room GPUI tokens | CSS/DOM and Graphite product colors | Athena-authored constants with citations; no copied source |
| `rust/crates/desktop/src/shell/title_bar.rs` | Zed `workspace.rs:1372-1430,8997-9460`; Graphite title metrics | PLANNED clean-room GPUI view | Zed project/editor/collaboration logic | Athena-authored; no Zed source copied |
| `rust/crates/desktop/src/shell/status_bar.rs` | Zed workspace status composition; Graphite `StatusBar.svelte:33-91` | PLANNED clean-room GPUI view | Zed language/terminal/collaboration status | Athena-authored; no source copied |
| `rust/crates/desktop/src/shell/subdivision.rs` | Zed dock render/resize ranges; Graphite recursive split behavior | PLANNED clean-room GPUI view | Zed persistence/project/pane products | Athena-authored; no source copied |
| `rust/crates/desktop/src/shell/panel_group.rs` | Zed pane/dock composition; Graphite `Panel.svelte:387-658` | PLANNED clean-room GPUI view | Zed editor items and Graphite DOM | Athena-authored; no source copied |
| `rust/crates/desktop/src/shell/electrical.rs` | QET panel/tool RST files; M006 empty-folio contract | PLANNED inert GPUI content | Qt UI and active document commands | Athena-authored using gpui-component APIs |
| `rust/crates/desktop/src/shell/mod.rs` | Zed `workspace.rs:1372-1430,8997-9460` | PLANNED clean-room GPUI composition | Zed workspace product state | Athena-authored; no source copied |

## Web Dependency Audit

Queried from npm on 2026-08-15 with Node `24.15.0` and npm `11.2.0`:

| Package | Exact version | License | Compatibility/evidence |
| --- | --- | --- | --- |
| `svelte` | `5.56.9` | MIT | current stable queried version |
| `vite` | `8.2.1` | MIT | Node `^20.19.0 || >=22.12.0`; current Node qualifies |
| `@sveltejs/vite-plugin-svelte` | `7.3.0` | MIT | peers: Vite 8 and Svelte `^5.46.4` |
| `@lucide/svelte` | `1.31.0` | ISC | peer: Svelte 5 |
| `@playwright/test` | `1.62.1` | Apache-2.0 | existing browser test dependency |

The initially queried `lucide-svelte@1.0.1` package is deprecated by its
publisher with the instruction to use `@lucide/svelte`; it was installed only
long enough for npm to expose that warning, then removed before Task 1 closed.

Rust toolchain: `rustc 1.97.1 (8bab26f4f 2026-07-14)`, Cargo `1.97.1`.
OpenCADStudio dependency versions and lock choices are not copied.

## Boundary Result

`cargo metadata --format-version 1 --no-deps` must continue to show:

- domain, format, editor, and application have no GPUI, browser, filesystem,
  Svelte, JavaScript, cloud, OpenCADStudio, or `iced` dependency;
- desktop is the only GPUI/`gpui-component` adapter;
- web-core is the only `wasm-bindgen` adapter;
- npm dependencies are presentation/build/test tooling only.

Observed from `rust/` on 2026-08-15:

```text
athena-application: athena-domain, athena-editor, athena-format, serde, serde_json, thiserror, uuid
athena-desktop: athena-application, athena-domain, gpui, gpui-component, uuid, windows-sys
athena-domain: serde, serde_json, thiserror, uuid
athena-format: athena-domain, serde, serde_json, thiserror, uuid
athena-web-core: athena-application, athena-domain, athena-editor, js-sys, serde, serde_json, uuid, wasm-bindgen, wasm-bindgen-test
```

`npm ls --depth=0` resolved the five exact packages in the Web Dependency Audit
with exit `0`. The first combined audit attempt invoked Cargo from the repository
root and failed because Athena's Cargo workspace is under `rust/`; the corrected
command above ran from `rust/` and exited `0`.

The pre-M006 `.gitignore` excluded `package-lock.json`; Task 1 removed that rule
and added the root lockfile so exact transitive Web dependencies are reviewable
and reproducible.
