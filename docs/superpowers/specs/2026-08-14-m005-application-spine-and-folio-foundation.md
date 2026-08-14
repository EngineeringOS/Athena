# M005 Application Spine and Folio Foundation

## Status

`APPROVED` for implementation on 2026-08-14 by explicit user direction to
continue until the plan is complete. Approval authorizes M005 only; it does not
pre-accept its runtime, visual, or certification gates.

## Why

Athena needs one small, bounded electrical workflow implemented through the
evidenced Graphite-derived application boundary. The current prototype exposes
document methods directly from desktop and WASM shells, so adding more features
would deepen an architecture already classified `REWRITE`.

## Evidence Scope

M005 may cite only:

- QET-PROJ-001, QET-PERSIST-001, QET-FOLIO-001/002,
  QET-TITLE-001/002, QET-VAR-001, QET-HISTORY-001, QET-PLATE-001;
- GRA-APP-001, GRA-DISPATCH-001, GRA-MSG-001, GRA-FRONTEND-001,
  GRA-LAYOUT-001, GRA-PANEL-001, GRA-PORTFOLIO-001, GRA-DOC-001,
  GRA-HISTORY-001, GRA-PROPS-001, GRA-WASM-001, GRA-PLATFORM-001, and
  GRA-UX-001.

No other QET or Graphite behavior is implied.

## Requirement Traceability

`EVIDENCED` rows preserve a reference outcome. `PROPOSED` rows are Athena
design decisions constrained by cited evidence and `AGENTS.md`; they require
user approval and later verification.

| Requirement | Observable outcome | Evidence path/line | Athena owner | Status |
| --- | --- | --- | --- | --- |
| REQ-M005-001 Typed application boundary | One typed input produces an ordered list of typed effects through a narrow editor facade. | GRA-APP-001: `reference/Graphite/editor/src/application.rs:8-57`; GRA-DISPATCH-001: `reference/Graphite/editor/src/dispatcher.rs:12-41`; GRA-MSG-001: `reference/Graphite/editor/src/messages/message.rs:4-54`; GRA-FRONTEND-001: `reference/Graphite/editor/src/messages/frontend/frontend_message.rs:23-45` | `athena-application` | `EVIDENCED` -> `PROPOSED` Athena mapping |
| REQ-M005-002 Project and folio ownership | A project owns explicit ordered folios with stable identity and one active work context. | QET-PROJ-001: `reference/qelectrotech-doc/source/users/project/what_is.rst:7-20`; QET-FOLIO-001: `reference/qelectrotech-doc/source/users/folio/what_is.rst:7-16`; GRA-PORTFOLIO-001: `reference/Graphite/editor/src/messages/portfolio/portfolio_message_handler.rs:47-107`; GRA-DOC-001: `reference/Graphite/editor/src/messages/portfolio/document/document_message_handler.rs:73-136` | `athena-domain`, `athena-application` | `EVIDENCED` -> `PROPOSED` Athena mapping |
| REQ-M005-003 New-folio inheritance | A new folio receives project title-block defaults without later default edits rewriting existing folios. | QET-FOLIO-002: `reference/qelectrotech-doc/source/users/project/properties/new_folio/folio.rst:7-45`; `reference/qelectrotech-source-mirror/sources/qetproject.cpp:1322-1332` | `athena-domain` | `EVIDENCED`; copy-on-create is `PROPOSED` |
| REQ-M005-004 Title-block fields | The folio plate exposes template/placement plus Title, Author, Date, File, Folio, Plant, Location, Rev index, Page Num, and custom variables. | QET-TITLE-001/002: `reference/qelectrotech-doc/source/users/folio/properties/folio_title_block.rst:7-84`; QET-PLATE-001: `reference/qelectrotech-doc/source/users/folio/properties/index.rst:1` | `athena-domain`, `athena-application` layout handler | `EVIDENCED` |
| REQ-M005-005 Variable scopes and resolution | Project/folio variables can be inserted into title text and resolve deterministically; missing references remain visible with diagnostics. | QET-VAR-001: `reference/qelectrotech-doc/source/users/annex/variables.rst:9-78`; `reference/qelectrotech-source-mirror/sources/autoNum/assignvariables.cpp:375-398`; redesign rule: `AGENTS.md:60-63` | `athena-domain` | Variable behavior `EVIDENCED`; typed AST/diagnostic policy `PROPOSED` |
| REQ-M005-006 Undo/redo and save checkpoint | Folio and title-block edits enter shared undo/redo history and return to equal persisted state after undo/redo sequences. Save requests carry a request ID and document revision. Matching adapter success establishes that revision as the clean checkpoint: history at or before it is discarded, while later edits remain undoable and dirty. Failure or stale/mismatched results change neither history nor dirty state. | QET-HISTORY-001: `reference/qelectrotech-doc/source/users/interface/panels/undo_panel.rst:9-22`; GRA-HISTORY-001: `reference/Graphite/editor/src/messages/portfolio/document/document_message_handler.rs:1341-1387`; core ownership: `AGENTS.md:35-39` | Save checkpoint behavior `EVIDENCED`; revision-bound async mapping `PROPOSED` |
| REQ-M005-007 Backend-owned plates | Project/folio panel structure, stable widget identity, context refresh, and callbacks are Rust-owned. | GRA-LAYOUT-001: `reference/Graphite/editor/src/messages/layout/layout_message_handler.rs:15-79`; GRA-PANEL-001: `reference/Graphite/editor/src/messages/portfolio/utility_types.rs:38-133`; GRA-PROPS-001: `reference/Graphite/editor/src/messages/portfolio/document/properties_panel/properties_panel_message_handler.rs:9-67`; shell/plate rule: `AGENTS.md:69-80` | application layout handler; GPUI/WASM views consume effects | `EVIDENCED` -> `PROPOSED` electrical mapping |
| REQ-M005-008 Save/reopen | Create/edit/save/close/reopen preserves project identity, folio order, title-block values, variables, and active context. | QET-PERSIST-001: `reference/qelectrotech-doc/source/users/project/save_project.rst:7-49`; `reference/qelectrotech-doc/source/users/project/close_project.rst:7-81`; GRA-PORTFOLIO-001: `reference/Graphite/editor/src/messages/portfolio/portfolio_message_handler.rs:47-107`; local-first rule: `AGENTS.md:48-50` | `athena-format`, portfolio handler, platform storage adapters | Workflow `EVIDENCED`; serde schema `PROPOSED` |
| REQ-M005-009 Platform equivalence | GPUI and browser adapters execute the same semantic message fixture and consume the same effect variants. | GRA-WASM-001: `reference/Graphite/frontend/wrapper/src/editor_wrapper.rs:31-141`; GRA-PLATFORM-001: `reference/Graphite/desktop/wrapper/src/message_dispatcher.rs:9-76`; target rule: `AGENTS.md:35-47`; `AGENTS.md:58-59` | `athena-desktop`, `athena-web-core`, `web/` | `EVIDENCED` -> `PROPOSED` GPUI/WASM mapping |
| REQ-M005-010 Versioned model | Stable IDs, explicit order, validation, versioned serde bytes, and platform-free core boundaries prepare local documents for later cloud identity/sync. | `AGENTS.md:35-39`; `AGENTS.md:48-59` | `athena-domain`, `athena-format` | `PROPOSED` Athena design constraint |
| REQ-M005-011 Shell proof | Screenshots show Graphite's title/workspace/status hierarchy filled with QET project/folio plate groups, without claiming full visual fidelity. | GRA-UX-001: `reference/Graphite/frontend/src/components/window/MainWindow.svelte:22-35`; QET project panel image: `reference/qelectrotech-doc/source/_external/_images/en/qet_panel/qet_panel_projects.png`; QET title fields image: `reference/qelectrotech-doc/source/_external/_images/en/qet_folios/qet_folio_prop_title_block_main.png`; user Graphite reference: `https://static.graphite.art/content/index/gui-demo-painted-dreams__4.avif` | GPUI/browser shell adapters and M005 verification | Structure `EVIDENCED`; Athena visual outcome `PROPOSED` |

## Capabilities

### CAP-M005-001: Typed Application Spine

Intent: platform adapters submit typed messages to a narrow `AthenaEditor`,
which delegates through one deterministic dispatcher and returns typed frontend
effects.

Success: desktop and WASM have no direct mutation path into project, folio, or
title-block state; identical message sequences produce identical state and
semantic effects.

Evidence: GRA-APP-001, GRA-DISPATCH-001, GRA-MSG-001, GRA-FRONTEND-001,
GRA-WASM-001, GRA-PLATFORM-001.

### CAP-M005-002: Project and Ordered Folios

Intent: create one electrical project, add folios, activate a folio, and move a
folio to a deterministic position in the project order.

Success: every folio has a stable identity; order is explicit and survives
save/reopen without being inferred from map-key order or title-block text.

Evidence: QET-PROJ-001, QET-FOLIO-001, QET-HISTORY-001,
GRA-PORTFOLIO-001, GRA-DOC-001, GRA-HISTORY-001.

### CAP-M005-003: Inherited Title-Block Data and Variables

Intent: new folios inherit project title-block defaults, and users can edit
folio title-block fields plus project/folio custom variables through contextual
electrical property plates.

Success: the active folio resolves project and folio variables deterministically
for title, author, date, file label, folio label, plant, location, revision, and
the user-configured Page Num field; edits survive save/reopen.

Evidence: QET-FOLIO-002, QET-TITLE-001/002, QET-VAR-001, QET-PLATE-001,
GRA-LAYOUT-001, GRA-PANEL-001, GRA-PROPS-001, GRA-DOC-001.

### CAP-M005-004: Shared Save and Reopen Contract

Intent: serialize the same versioned Rust-owned project document to bytes and
reopen it through platform-specific local persistence adapters.

Success: a canonical desktop-created fixture reopens in WASM and a canonical
WASM-created fixture reopens on desktop with equal project identity, folio
order, active folio, title-block values, variables, and resolved display data.

Evidence: QET-PROJ-001, QET-PERSIST-001, GRA-PORTFOLIO-001, GRA-DOC-001,
GRA-FRONTEND-001, GRA-WASM-001, GRA-PLATFORM-001.

### CAP-M005-005: Backend-Owned Project/Folio Plates

Intent: the application owns the project/folio panel model and emits targeted
layout updates; GPUI and browser views render controls and send typed callbacks.

Success: panel visibility, active context, field identity, validation errors,
and commits are driven by Rust messages/effects; JavaScript and GPUI do not own
a duplicate form state machine.

Evidence: QET-TITLE-002, QET-VAR-001, QET-PLATE-001, GRA-LAYOUT-001,
GRA-PANEL-001, GRA-PROPS-001, GRA-FRONTEND-001, GRA-UX-001.

## Field-Level Contract

### Project Document

| Field | Rule |
| --- | --- |
| `project_id` | Stable generated identity; serialized. |
| `name` | Non-empty user-visible project name; serialized. |
| `folio_order` | Ordered unique folio IDs; serialized as the sole ordering authority. |
| `folios` | Folio map keyed by stable identity; every ordered ID must exist exactly once. |
| `folio_defaults` | Default title-block values and default folio variables copied into a newly created folio. Later edits do not rewrite existing folios. |
| `variables` | Project-scoped ordered name/value map; keys are trimmed, non-empty, unique, and contain no control characters. |

### Folio

| Field | Rule |
| --- | --- |
| `folio_id` | Stable generated identity; serialized. |
| `label` | Non-empty user-visible folio label. |
| `title_block` | Folio-owned copy of inherited title-block data. |
| `variables` | Folio-scoped ordered name/value map with the same key rules as project variables. |
| `schematic` | Existing platform-neutral symbol/wire/junction/annotation payload, preserved under the folio without a second page identity. M005 does not expand that payload. |

### Project View State

`active_folio_id` is application/document presentation state. It is stored in
the local envelope so save/reopen restores the work context, but it is not part
of the electrical project aggregate and does not participate in semantic
project equality.

### Title Block

The first slice stores `template_id`, `placement` (`Bottom` or `Right`),
`title`, `author`, `date_text`, `file_label`, `folio_label`, `plant`,
`location`, `revision`, and `page_number`. Folio custom variables are edited in
the same QET-evidenced property plate but are stored only in `Folio.variables`;
the title block stores typed references to them, not a duplicate variable map.
`page_number` is a stored title-block template value because QET-TITLE-002
identifies the field but M005 does not include automatic numbering-pattern
semantics. Template editing/duplication and title-block geometry are outside
M005; `template_id` is an opaque stable reference to the built-in initial
template.

Athena stores variable-bearing title text as typed segments rather than making
an ad hoc string grammar part of the persistence contract:

```text
TemplateText = [Literal | VariableReference]
VariableReference = Project(name) | Folio(name)
```

Standard title-block fields remain direct typed properties; M005 does not allow
one field to recursively reference another. The property plate inserts custom
project/folio references through a scoped variable picker. A
future textual formula syntax may serialize to the same AST, but M005 does not
invent or certify that syntax. Missing references produce typed diagnostics and
remain visibly unresolved; they are never silently erased.

## State Ownership

| Owner | Responsibility |
| --- | --- |
| `athena-domain` | Project, folio, title-block values, variables, invariants, and deterministic resolution. |
| `athena-format` | Versioned serde envelope, migration entrypoint, canonical bytes, and validation on decode. |
| `athena-editor` | Document-scoped command application and history for folio/title-block edits. |
| new `athena-application` | `AthenaEditor`, dispatcher, portfolio/document/layout handlers, message queues, and frontend effects. |
| `athena-desktop` | GPUI event/effect adapter, local file picker/storage, and native views. |
| `athena-web-core` | WASM wrapper around the persistent `AthenaEditor`; typed message serialization and effect callback. |
| `web/` | Minimal event/effect router and DOM rendering; no project or form authority. |

## Typed Protocol

### Input Messages

`AthenaMessage` has `Portfolio`, `Document`, and `Layout` families.

- Portfolio: create project, request open, open bytes, request save, report save
  result, close project. Save request/result messages carry the same request ID,
  project identity, and document revision.
- Document: add folio, activate folio, move folio, update project name/defaults,
  update project variable, update folio label/title-block field/variable.
- Layout: request workspace layout, request project plate, request folio plate,
  commit a stable widget ID/value pair.

### Frontend Effects

- project/document opened or closed;
- active folio and dirty-state changed;
- workspace panel layout updated;
- targeted project/folio plate layout updated;
- project outline/folio order updated;
- save bytes requested from the adapter, including request ID, project identity,
  document revision, and immutable bytes for that revision;
- open-file bytes requested from the adapter;
- validation diagnostics updated;
- fatal/non-fatal operation error displayed.

Effects describe semantic UI/service work. They do not contain GPUI elements,
DOM nodes, filesystem handles, browser objects, or cloud APIs.

## Required Workflow

1. Start desktop or browser shell and create `Main Distribution`.
2. The project creates one initial folio from project title-block defaults.
3. Add two folios, rename them, and move the third folio to the second position.
4. Set project variables `plant=PLANT-A` and `designer=A. Engineer`.
5. On the active folio, set title, insert the project `designer` reference into
   author through the variable picker, set location/revision, and add one folio
   custom variable and Page Num value; observe the resolved title-block display.
6. Save through the platform adapter, close the project, and reopen the bytes.
7. Verify identity, folio order, active folio, inherited/edited values,
   variables, resolved display, and the revision-bound save checkpoint. With no
   intervening edit, success leaves empty undo/redo stacks and a clean document;
   an edit made while save is pending remains undoable and dirty after success.
8. Execute the same semantic message fixture against native and WASM targets
   and compare canonical state/effect traces.

## Persistence Rules

- The core accepts and returns bytes; it never opens paths or browser handles.
- The document envelope contains a schema version and rejects unsupported future
  versions, malformed identities, missing ordered folios, duplicate order IDs,
  and invalid active-folio references. Title-block Page Num is serialized as a
  field; folio order remains a separate ordered identity list.
- M001-M003 prototype schema versions 1 and 2 are rejected as
  `UnsupportedPrototypeSchema`; M005 is a greenfield schema 3 boundary and does
  not silently reinterpret prototype sheets as certified folios.
- Encoding is deterministic for equal semantic state.
- Native save uses an adapter-owned atomic replacement strategy.
- Browser save/open uses a small JavaScript adapter around browser file APIs;
  bytes and editor state remain Rust-owned.
- Save bytes generation does not mutate history. The request effect carries
  `SaveRequestId`, project identity, and `DocumentRevision`; the adapter result
  must echo them. A matching success establishes only that revision as the
  clean checkpoint, discards history at or before it, clears redo state derived
  from the pre-save timeline, and preserves later undo transactions. Dirty state
  is then `current_revision != saved_revision`. Cancellation, failure, or a
  stale/mismatched result preserves history and dirty state and emits a typed
  diagnostic. A matching success, cancellation, or failure terminates and
  releases the active request so the user can retry. A stale/mismatched result
  never clears the actual active request. At most one save request per project
  is active in M005.
- Cloud project storage, collaboration, autosave policy, and conflict resolution
  remain future adapter/protocol work.

## Verification Gates

| Gate | M005 evidence required |
| --- | --- |
| Reference | Every requirement cites only the allowed QET/GRA IDs. |
| Mapping | Every field, message, handler, effect, and adapter has one named Rust owner. |
| Architecture | Static dependency checks and tests prove adapters cannot directly mutate domain/document state. |
| Behavior | Desktop and browser complete the required workflow through real messages/effects. |
| Persistence | Cross-platform fixtures round-trip with canonical semantic equality. |
| Cross-platform | Native and `wasm32-unknown-unknown` builds pass; semantic traces match. |
| Visual | Screenshots prove the Graphite-evidenced hierarchy and the QET-evidenced project/folio plate groups. This does not certify full professional visual fidelity. |
| Adversarial | Tests reject duplicate state, direct adapter mutation, invalid order, unresolved callback IDs, malformed variables, and unsupported schema versions. |
| User | User reviews the running desktop/browser workflow and explicitly accepts it. |

## Constraints

- No QElectroTech XML, Qt/C++ architecture, or Graphite source code is copied.
- No generic diagram, vector, image, node-graph, conductor, symbol-placement, or
  report feature is added in M005.
- JavaScript remains a minimal WASM/platform adapter.
- GPUI and browser shells implement equivalent semantics without sharing UI
  toolkit code.
- Every touched core Rust file receives module documentation and contract-level
  public API documentation under `AGENTS.md`.
- At implementation time, dependency versions must be rechecked against the
  latest stable Rust and latest compatible stable crate releases.

## Non-goals

- QElectroTech parity for any full inventory ID.
- A complete Graphite docking system or visual clone.
- Title-block template editing, geometry, logos, or migration from QET XML.
- Conductors, elements, terminals, numbering patterns, reports, printing, cloud
  sync, collaboration, or multi-project workspaces.
- Calling the result `complete`, `mirrored`, `parity`, or `professional`.

## Success Signal

M005 may be called `USER-ACCEPTED` only after the required project/folio/title-
block workflow runs through the typed application spine on desktop and web,
fresh verification evidence is recorded, and the user accepts the demonstrated
result. Until then its status remains no higher than the strongest proven gate.
