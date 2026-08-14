# M004 Athena Prototype Gap Audit

## Classification Rules

- `RETAIN`: platform-neutral behavior that has a mapped future owner.
- `EXTRACT`: useful behavior exists but sits behind the wrong boundary.
- `REWRITE`: behavior/UI exists but conflicts with certified architecture.
- `DELETE`: prototype-only content with no approved future role.
- `UNDECIDED`: evidence is insufficient to classify safely.

| Current module | Current role | Classification | Evidence and required action |
| --- | --- | --- | --- |
| `rust/crates/domain` | Project, sheets, symbols, wires, basic fields. | `EXTRACT` | Useful base for QET-FOLIO-001/QET-ELEM-001/QET-COND-001, but lacks typed electrical families, title blocks, variables, cross-references, terminals, and report semantics. Do not extend until QET mapping exists. |
| `rust/crates/editor` | `EditorSession`, commands, history, selection, interaction. | `EXTRACT` | Useful command/history foundation; must become a document handler beneath GRA-APP/DISPATCH/MSG, rather than the public application boundary. |
| `rust/crates/render` | Scene projection and hit regions. | `EXTRACT` | Can become document projection/overlay implementation after GRA-VIEWPORT/OVERLAY mapping. |
| `rust/crates/library` | Small built-in symbol catalog. | `REWRITE` | Does not represent QET collection hierarchy, project/user libraries, metadata, or element editor scope. |
| `rust/crates/format` | Serde persistence snapshot. | `UNDECIDED` | Local-first intent is valid, but schema cannot be certified before QET project/folio semantic mapping. |
| `rust/crates/geometry` | Basic geometric primitives. | `RETAIN` | Platform-neutral support code; retain unless future geometry constraints prove insufficient. |
| `rust/crates/web-core` | WASM facade directly exposing editor methods. | `REWRITE` | Violates GRA-MSG-001/GRA-FRONTEND-001/GRA-WASM-001. Replace direct method facade with typed input/output protocol. |
| `rust/crates/desktop/src/app.rs` | Desktop adapter exposing direct session calls. | `REWRITE` | Must become a GPUI adapter over Athena messages/frontend responses. |
| `rust/crates/desktop/src/panels.rs` | Large imperative GPUI shell. | `REWRITE` | Violates GRA-LAYOUT-001/GRA-PANEL-001/GRA-PROPS-001. No more shell styling here before panel/layout architecture exists. |
| `web/bootstrap.js` | Direct DOM event handlers and direct `WebEditor` calls. | `REWRITE` | Violates GRA-MSG-001/GRA-WASM-001; platform adapter must consume frontend messages. |
| `web/index.html`, `web/styles.css` | Prototype shell. | `REWRITE` | No Graphite screenshot/layout certification exists. Treat as temporary harness only. |
| M001-M003 docs/verification | Prototype claims and evidence. | `RETAIN-AS-HISTORY` | Do not erase history. Future docs must explicitly state these milestones are not parity/professional certification. |

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
