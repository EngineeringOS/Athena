# M004 Athena Prototype Gap Audit

## Classification Rules

- `RETAIN`: platform-neutral behavior that has a mapped future owner.
- `EXTRACT`: useful behavior exists but sits behind the wrong boundary.
- `REWRITE`: behavior/UI exists but conflicts with the evidenced target architecture.
- `DELETE`: prototype-only content with no approved future role.
- `UNDECIDED`: evidence is insufficient to classify safely.

| Current module | Current role | Classification | Evidence and required action |
| --- | --- | --- | --- |
| `rust/crates/domain` | Project, sheets, symbols, wires, basic fields. | `EXTRACT` | Required evidence: QET-PROJ-001, QET-FOLIO-001/002, QET-TITLE-001/002, QET-VAR-001, QET-ELEM-001/002, QET-TERM-001, QET-COND-001/002/003, and GRA-DOC-001. Preserve only platform-neutral invariants after field-by-field mapping; current sheets and generic fields are not the target contract. |
| `rust/crates/editor` | `EditorSession`, commands, history, selection, interaction. | `EXTRACT` | Required evidence: QET-HISTORY-001, GRA-APP-001, GRA-DISPATCH-001, GRA-MSG-001, GRA-DOC-001, GRA-TOOL-001, GRA-INPUT-001, and GRA-HISTORY-001. Extract deterministic command/history behavior under document handlers; `EditorSession` cannot remain the platform-facing application facade. |
| `rust/crates/render` | Scene projection and hit regions. | `EXTRACT` | Required evidence: QET-ELEM-001, QET-TERM-001, QET-COND-001/002/003, GRA-VIEWPORT-001, GRA-OVERLAY-001, and GRA-FRONTEND-001. Retain scene/hit concepts only behind document projection and transient overlay outputs. |
| `rust/crates/library` | Small built-in symbol catalog. | `REWRITE` | Required evidence: QET-LIB-001/002, QET-ELEM-001, GRA-PANEL-001, and GRA-PROPS-001. Replace the flat built-in catalog with evidenced built-in/user/project collection semantics before feature expansion. |
| `rust/crates/format` | Serde persistence snapshot. | `UNDECIDED` | Required evidence: QET-PROJ-001, QET-PERSIST-001, QET-FOLIO-001/002, QET-TITLE-001/002, QET-VAR-001, GRA-PORTFOLIO-001, GRA-DOC-001, and GRA-HISTORY-001. The versioned serde envelope may survive, but its schema and ownership cannot be approved before the project/folio slice is specified. |
| `rust/crates/geometry` | Basic geometric primitives. | `RETAIN` | Required evidence: QET-ELEM-001, QET-TERM-001, QET-COND-001, and GRA-VIEWPORT-001. Retain as platform-neutral support; no claim is made that current geometry covers QET routing or potential semantics. |
| `rust/crates/web-core` | WASM facade directly exposing editor methods. | `REWRITE` | Required evidence: GRA-APP-001, GRA-DISPATCH-001, GRA-MSG-001, GRA-FRONTEND-001, GRA-INPUT-001, and GRA-WASM-001. Replace direct methods with a persistent Rust editor plus typed input/effect protocol. |
| `rust/crates/desktop/src/app.rs` | Desktop adapter exposing direct session calls. | `REWRITE` | Required evidence: GRA-APP-001, GRA-DISPATCH-001, GRA-MSG-001, GRA-FRONTEND-001, GRA-INPUT-001, and GRA-PLATFORM-001. GPUI must adapt platform events/effects without owning duplicate editor state. |
| `rust/crates/desktop/src/panels.rs` | Large imperative GPUI shell. | `REWRITE` | Required evidence: GRA-LAYOUT-001, GRA-PANEL-001, GRA-PROPS-001, GRA-UX-001, and QET-PLATE-001. Replace the monolith with backend-owned panel layout/lifecycle and QET-evidenced electrical plate content. No more visual patching is authorized here. |
| `rust/crates/desktop/src/canvas.rs` | GPUI projection of the current scene. | `EXTRACT` | Required evidence: GRA-FRONTEND-001, GRA-VIEWPORT-001, and GRA-OVERLAY-001. Preserve useful native rendering only after it consumes typed scene/overlay effects instead of reaching through shell-owned editor state. |
| `rust/crates/desktop/src/input.rs` | Desktop-local active tool and canvas input interpretation. | `REWRITE` | Required evidence: GRA-INPUT-001, GRA-MSG-001, and GRA-TOOL-001. Native code may normalize events and perform adapter hit delivery, but active-tool state and semantic gestures must move behind the shared dispatcher. |
| `rust/crates/desktop/src/storage.rs` | Native filesystem snapshot adapter. | `EXTRACT` | Required evidence: QET-PERSIST-001, GRA-FRONTEND-001, GRA-PORTFOLIO-001, and GRA-PLATFORM-001. Keep filesystem behavior only as an adapter consuming save/open effects; it may not own project lifecycle. |
| `rust/crates/desktop/src/lib.rs`, `rust/crates/desktop/src/main.rs` | Native startup and shell composition. | `REWRITE` | Required evidence: GRA-APP-001, GRA-FRONTEND-001, and GRA-PLATFORM-001. Startup must construct the shared application and GPUI effect adapter rather than the prototype facade. |
| `web/bootstrap.js` | Direct DOM event handlers and direct `WebEditor` calls. | `REWRITE` | Required evidence: GRA-INPUT-001, GRA-MSG-001, GRA-FRONTEND-001, and GRA-WASM-001. It may host WASM, normalize browser events, route effects, and call platform APIs only. |
| `web/index.html`, `web/styles.css` | Prototype shell. | `REWRITE` | Required evidence: GRA-PANEL-001, GRA-UX-001, and QET-PLATE-001. Treat these as a temporary harness until a measured shell hierarchy, electrical panel taxonomy, and visual gate are specified. |
| `web/tests` | Prototype browser workflow assertions. | `REWRITE` | Required evidence: the exact QET/GRA IDs authorized by each future slice. Replace tests that assert prototype DOM or direct methods with real typed-message workflow, persistence, and visual evidence. |
| `web/pkg`, `rust/web/pkg` | Generated WASM bindings and artifacts. | `DELETE` | Required evidence: GRA-WASM-001. Remove stale generated bindings from the architecture authority and regenerate them from the approved `athena-web-core` protocol during implementation. |
| M001-M003 docs/verification | Prototype claims and evidence. | `RETAIN` | Required evidence: M004 baseline claim rules. Preserve as history, but M001-M003 do not prove QET parity, Graphite architecture, cross-platform product equivalence, or professional visual quality. |

The classifications apply recursively to each listed crate or directory unless
a more specific file row overrides the group. This is an ownership-boundary
audit, not a claim that every source line was semantically certified.

## Current Implementation Evidence

| Audit group | Current local evidence | Classification basis |
| --- | --- | --- |
| `rust/crates/domain` | `rust/crates/domain/src/project.rs:42`; `rust/crates/domain/src/sheet.rs:47`; `rust/crates/domain/src/symbol.rs:8`; `rust/crates/domain/src/electrical.rs:42` | Platform-neutral aggregate exists, but uses prototype sheet/generic field semantics. |
| `rust/crates/editor` | `rust/crates/editor/src/session.rs:32`; `rust/crates/editor/src/command.rs:50`; `rust/crates/editor/src/history.rs:11`; `rust/crates/editor/src/interaction.rs:98` | Useful commands/history are concentrated in `EditorSession`, which is also exposed as an application facade. |
| `rust/crates/render` | `rust/crates/render/src/scene.rs:29`; `rust/crates/render/src/scene.rs:147`; `rust/crates/render/src/hit_test.rs:13` | Scene and hit data are platform-neutral enough to extract behind effects/overlays. |
| `rust/crates/library` | `rust/crates/library/src/catalog.rs:95`; `rust/crates/library/src/search.rs:7` | One flat built-in catalog lacks QET collection ownership and metadata. |
| `rust/crates/format` | `rust/crates/format/src/envelope.rs:7-23`; `rust/crates/format/src/migrations.rs:4`; `rust/crates/format/src/snapshot.rs:5-22` | A versioned serde envelope exists, but its current schema is prototype authority only. |
| `rust/crates/geometry` | `rust/crates/geometry/src/lib.rs:1-15`; `rust/crates/geometry/src/point.rs:5` | Deterministic platform-neutral geometry has a clear support role. |
| `rust/crates/web-core` | `rust/crates/web-core/src/lib.rs:34`; `rust/crates/web-core/src/lib.rs:383-409`; `rust/crates/web-core/src/lib.rs:535-692` | WASM exports direct mutation, snapshot, and rendering methods instead of one typed message/effect boundary. |
| `rust/crates/desktop/src/app.rs` | `rust/crates/desktop/src/app.rs:25`; `rust/crates/desktop/src/app.rs:100-177`; `rust/crates/desktop/src/app.rs:517-525` | Desktop directly owns and mutates the editing facade and filesystem workflow. |
| `rust/crates/desktop/src/panels.rs` | `rust/crates/desktop/src/panels.rs:20`; `rust/crates/desktop/src/panels.rs:68-85`; `rust/crates/desktop/src/panels.rs:200-230` | One GPUI type owns shell composition and directly reads/writes editor fields. |
| `rust/crates/desktop/src/canvas.rs` | `rust/crates/desktop/src/canvas.rs:12` | Native scene projection is useful, but currently consumes shell-provided scene state directly. |
| `rust/crates/desktop/src/input.rs` | `rust/crates/desktop/src/input.rs:7-18`; `rust/crates/desktop/src/input.rs:29` | Active tool and hit interpretation are desktop-local rather than shared typed input/tool state. |
| `rust/crates/desktop/src/storage.rs` | `rust/crates/desktop/src/storage.rs:12` | Filesystem persistence is already adapter-shaped but is called from the direct facade. |
| `rust/crates/desktop/src/lib.rs`, `main.rs` | `rust/crates/desktop/src/lib.rs:1-7`; `rust/crates/desktop/src/main.rs:1-6` | Startup selects the prototype app/shell rather than a shared application/effect adapter. |
| `web/bootstrap.js` | `web/bootstrap.js:1`; `web/bootstrap.js:159`; `web/bootstrap.js:174`; `web/bootstrap.js:253` | JavaScript imports `WebEditor`, creates it, loads snapshots, and directly mutates fields. |
| `web/index.html`, `web/styles.css` | `web/index.html:1`; `web/styles.css:1` | Static prototype shell has no backend-owned panel model or measured Graphite visual evidence. |
| `web/tests` | `web/tests/authoring-mvp.test.js:3`; `web/tests/authoring-mvp.test.js:41` | Tests assert prototype direct workflows and unsupported professional-workbench wording. |
| `web/pkg`, `rust/web/pkg` | `web/pkg/athena_web_core.js:1`; `rust/web/pkg/athena_web_core.js:1` | Generated bindings expose the current direct facade and must follow the approved protocol rather than define it. |
| M001-M003 docs/verification | `docs/superpowers/specs/2026-08-13-m001-rust-authoring-mvp.md:1`; `docs/superpowers/specs/2026-08-14-m003-graphite-professional-shell.md:1` | Historical milestone artifacts describe prototypes and retain no authority over M004/M005 claims. |

## Current Product Claim

The only supported claim is: **Athena contains a Rust/WASM schematic editing
prototype with basic symbol, wire, selection, transform, persistence, and
shell experiments.**

It is not a QElectroTech functional mirror, not a Graphite architecture port,
and not a professionally certified editor shell.

## Architecture Authorization Boundary

No module classified `REWRITE` may receive more feature/UI work. The next
implementation milestone must first specify and create the Graphite-derived
application/dispatcher/message/frontend spine, with one electrical vertical
slice chosen from `qet-electrical-inventory.md`.

## First Authorized Dependency Set

M005 may depend only on QET-PROJ-001, QET-PERSIST-001,
QET-FOLIO-001/002, QET-TITLE-001/002, QET-VAR-001, QET-HISTORY-001,
QET-PLATE-001, GRA-APP-001, GRA-DISPATCH-001, GRA-MSG-001,
GRA-FRONTEND-001, GRA-LAYOUT-001, GRA-PANEL-001, GRA-PORTFOLIO-001,
GRA-DOC-001, GRA-HISTORY-001, GRA-PROPS-001, GRA-WASM-001,
GRA-PLATFORM-001, and GRA-UX-001. Everything else remains outside that
vertical slice.
