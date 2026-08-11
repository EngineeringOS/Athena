---
title: M43 Upstream Adoption Ledger
status: final
updated: 2026-08-06
---

# M43 Upstream Adoption Ledger

This ledger records what Athena learns from diagram and rendering projects. `reference/` remains
read-only study evidence. No source subtree is imported into production. A runtime dependency is
allowed only when its adapter boundary, license, tests, and upgrade policy are explicit.

## Disposition

- **ADOPT:** use the named library in M43 behind the architecture boundary.
- **ADAPT:** use a durable idea or a future adapter shape; do not add the upstream runtime now.
- **REJECT:** do not use the upstream model, renderer, or dependency as Athena authority.

| Upstream | Evidence | License | Disposition | Athena boundary and lesson |
| --- | --- | --- | --- | --- |
| Konva | `10.3.0`; local `reference/konva` commit `914acaf8bc00431ac68e98505d4d8eea1e5ca6bf` | MIT | **ADOPT** | Exact direct frontend dependency and first live adapter only. Use Stage/Layer/Group/Shape, hit canvas, delegated events, caching, and drag-layer isolation. No Konva type crosses scene/compiler contracts. Adapter contract, source trace, resize, selection, asset, and scale tests gate upgrades. |
| PixiJS | `8.19.0`; local `reference/pixijs` commit `1d90a20c62433ba68dff78466e06ee372a5a5232` | MIT | **ADAPT** | Learn batching, culling, render backends, and federated events for a future high-density adapter. No M43 dependency. Add only after measured Konva budget failure and reuse exactly the same `AthenaDiagramScene` and command contracts. |
| Excalidraw | local `reference/excalidraw` commit `e4ab626739f5f163c5eca56190f615643218b61c`; registry `@excalidraw/excalidraw 0.18.1` | MIT | **ADAPT** | Learn `Scene`/`AppState` separation, snapping, memoized rendering, viewport culling, and tool state. Reject its whiteboard document schema, shape ownership, and UI shell as Athena truth. No dependency. Re-check interaction tests when adding tools. |
| tldraw | registry `5.3.0`; upstream `tldraw/tldraw` HEAD `6850000c4a2ad6d4865e8b8b7ead9270b63c6a27` | `LicenseRef-tldraw`, custom source-available; production use prohibited without separate trial/commercial license and license-key compliance | **ADAPT** | Learn editor/store separation, selection, handles, and collaborative interaction ergonomics. Reject its document store, shape schema, collaboration authority, telemetry/license-key surface, and runtime adoption. No dependency or copied code. |
| diagrams.net / draw.io | upstream `jgraph/drawio` HEAD `3b4210678207f3880888969ee53c9fe4580104ab` | Apache-2.0 project license | **ADAPT** | Learn mature view/model separation, import/export boundary, and large-document ergonomics. Reject mxGraph/draw.io model, DOM/format authority, and vendor workflow as Athena kernel truth. No dependency. |
| Structurizr | local `reference/structurizr` commit `9ff16634c3b8574584262ae8545510bbb1d1b4bd` | Apache-2.0 | **ADAPT** | Learn semantic workspace model versus `ElementView` layout separation and deterministic views. Reject its DSL, C4 semantics, and renderer contracts as Athena dependencies. Ledger and scene tests preserve the separation. |
| Eclipse GLSP | local `reference/glsp-examples` commit `9b5cc2fbc2ada931cbc42ef23741330d198b9102` | EPL-2.0 with secondary GPL-2.0-only WITH Classpath-exception-2.0; some example material MIT | **ADAPT** | Learn protocolized diagram operations and server/client separation. Reject GLSP server model, operation stack, and dependency for M43 because compiler plus source command service already owns Athena truth. Revisit only for a deliberate remote/editor integration. |
| ELK / elkjs | registry `0.12.0` | EPL-2.0 OR GPL-3.0-or-later | **ADAPT** | Learn explicit layered layout service boundaries. Reject live renderer layout and implicit mutation. Revisit as an optional compiler-owned Spatial service with license review; no M43 dependency. |
| QElectroTech | pinned object `6d7b38c7a17bc21ee55f34b1319dbc5842c7bae5`; inspect only through `git show <commit>:<path>` because current mirror worktree is dirty | GPL-2.0-or-later | **ADAPT** | Learn user-familiar plot frame, row/column border, and clean document conventions only. Reject source code, database shape, proprietary semantics, and GPL runtime linkage. Dirty worktree files are not evidence. |
| EPLAN study material | `draft/20260803-confuse/FULL-DISCUSS.md` and `dir-where-we-go-and-learn-from-eplan-study.md` | User research, no code dependency | **ADAPT** | Learn that engineering meaning and document presentation are separate, coordinates are human references, and professional pages stay readable. Reject proprietary file/database compatibility as M43 scope. |

## Upgrade Gate

Any dependency or adapter change must record:

1. exact package version and lockfile diff;
2. license and transitive-license check;
3. scene-schema and source-command boundary check;
4. deterministic SVG, selection/trace, resize, asset-safety, and screenshot tests;
5. measured scale result against current named-hardware baseline;
6. clean-reference check, or immutable `git show <commit>:<path>` evidence when a mirror is dirty;
7. removal proof for any retired adapter or fallback path.

No upstream project may add a second engineering model, second layout authority, second renderer
truth, compatibility shim, or network-backed asset source.

Existing Theia distribution continues its normal third-party notice process for
`EPL-2.0 OR GPL-2.0-only WITH Classpath-exception-2.0`; M43 adds no new Theia license choice.
