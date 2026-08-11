---
title: Athena M44 Asset-First Editable Engineering Canvas
status: final
created: 2026-08-07
updated: 2026-08-07
---

# PRD: Athena M44 Asset-First Editable Engineering Canvas

## 0. Document Purpose

This PRD defines M44 product capabilities for a trustworthy, editable Athena engineering canvas. It feeds M44 architecture, UX contracts, epics, stories, tests, and closure evidence. The approved design input is `_bmad-output/planning-artifacts/m44/design.md`; technical mechanisms and rejected alternatives are recorded in this workspace's `addendum.md`.

M44 is milestone-local and breaking. It does not preserve retired renderer, semantic-macro, or presentation compatibility paths.

## 1. Vision

An engineer opening an Athena document sees recognizable IEC or vendor engineering elements, not placeholder boxes. Symbols have credible geometry, visible connection points, readable labels, predictable placement, and controllable line styles. The canvas is suitable for inspection and editing, not merely a debug projection.

The canvas remains a projection of engineering meaning. A user can select an element, align it, move it, change its representation, connect valid ports, or change presentation style. Each accepted action becomes a validated source change and survives recompilation and reopening. A rejected action explains the exact engineering problem and leaves the accepted document unchanged.

M44 establishes Athena's first complete engineering authoring transaction loop: library-backed
representations, traceable selection, classified edit operations, validated source transactions,
operation journal, and one canonical scene shared by Theia and export. It intentionally stops short of a
complete semantic navigator, AI copilot, engineering Pattern authoring system, or GPU renderer.

## 2. Target Users

### 2.1 Jobs To Be Done

- **Engineering author:** inspect a real engineering projection and make small layout or connection corrections without losing semantic traceability.
- **Design reviewer:** distinguish a real symbol, port, route, and label from a rendering artifact and follow each item back to its source.
- **Library maintainer:** publish reusable IEC/vendor elements with geometry, port contracts, version, digest, provenance, and license information.
- **Integrator:** consume one deterministic scene and export path without reimplementing layout or rendering rules.

### 2.2 Non-Users In M44

- Users seeking a full EPLAN file/database compatibility layer.
- Users seeking AI-authored engineering solutions or standards decisions.
- Users seeking PDF/report/BOM production.
- Users seeking full macro/pattern authoring and design automation.
- Users seeking CAD-scale 100,000-complex-element interactive performance.

### 2.3 Key User Journeys

**UJ-1. Maya opens a readable engineering sheet.** Maya opens the controlled M44 example in Theia. The project resolves its locked library dependencies, compiles one canonical scene, and shows actual IEC/vendor element geometry, labels, ports, routes, and clean sheet framing. She selects a symbol and can identify its semantic subject and source trace.

**UJ-2. Maya aligns and moves occurrences.** Maya selects two or more occurrences, chooses Align or Distribute, or drags one occurrence onto a grid anchor. The canvas previews the result, submits one operation on release, and refreshes from the validated source patch. The authored placement survives reopening; derived route geometry may be recomputed.

**UJ-3. Maya changes representation or part binding.** Maya chooses another compatible library symbol for the same engineering subject, or chooses a replacement Part. Representation changes update the locked symbol reference. Part changes trigger engineering validation. An incompatible choice is rejected with a correction and no partial scene.

**UJ-4. Maya reconnects a port.** Maya drags from a valid output port to a compatible input port. The product validates direction and flow/signal compatibility, persists a relationship edit only after validation, and republishes the scene. An invalid connection leaves the previous accepted scene intact.

**UJ-5. Nikhil adjusts presentation style.** Nikhil changes route width, dash, color, label typography, or port display. The change is previewed without altering engineering meaning. Solidifying the change writes the sheet style companion; a session-only override disappears when discarded.

**UJ-6. Nikhil diagnoses a broken library or source edit.** Nikhil opens a project with a missing dependency, mismatched SVG/metadata digest, stale edit revision, or invalid port. The product names the subject, problem, and correction, publishes no partial scene, and preserves the last accepted source state.

## 3. Glossary

- **Engineering Entity** - A real engineering subject represented in Athena, such as a device or assembly.
- **Function** - A role or capability held by an Engineering Entity; it is not a drawing primitive.
- **Part** - A replaceable procurement or physical implementation bound to an Engineering Entity.
- **Symbol** - A graphical representation supplied by a library; it does not own engineering identity.
- **Representation Binding** - The stable link from an Engineering Function or Entity to a compatible
  library representation choice.
- **Representation Instance** - One sheet occurrence of a Representation Binding with placement, style,
  and trace.
- **Symbol Macro** - A reusable representation composition of symbols with exposed connection points and
  parameters; it does not create engineering relationships or engineering intelligence.
- **Element Macro** - A reusable representation element set; it is not an engineering solution Pattern.
- **Library Package** - A versioned, lock-resolved Athena package publishing engineering definitions, representation definitions, or both.
- **Representation Package** - A Library Package containing Symbols, SVG resources, and geometry metadata.
- **Engineering Package** - A Library Package containing Parts, engineering definitions, or validation-relevant metadata.
- **Port Contract** - Library metadata describing a connection point's stable key, direction, domain, signal/flow kind, and geometry anchor.
- **Relationship** - An engineering relationship between typed participants, such as supplies, controls, protects, or consumes.
- **Sheet** - A projection container that owns authored placement and presentation intent for one view.
- **Occurrence** - One projection instance of an Engineering Entity, Function, or Relationship on a Sheet.
- **Route** - The geometric representation of a Relationship on a Sheet.
- **Canonical Scene** - The renderer-neutral, traceable scene compiled from validated engineering, projection, spatial, and library facts.
- **Style Companion** - A colocated `*.sheet.style.athena` document containing persisted presentation styling.
- **Edit Operation** - A typed user intent submitted for server-side validation and source patching.
- **Presentation Edit Operation** - A safe operation that mutates Sheet or Style Companion authority:
  Move, Align, Distribute, Snap, or Set Style.
- **Representation Edit Operation** - A medium-risk operation that mutates representation binding while
  preserving engineering identity: Change Symbol.
- **Engineering Edit Operation** - A high-risk operation that mutates Engineering Reality and requires M42
  validation: Reconnect or Bind Part.
- **Operation Journal** - The accepted source-transaction history used for Undo/Redo, AI collaboration,
  audit, and future multi-user conflict handling.
- **Source Revision** - The revision/precondition that an Edit Operation must match before mutation.
- **Source Trace** - The stable link from a visible scene item to its semantic, projection, spatial, or library origin.

Source Revision is a structured compare-and-set token. It contains existing scene `inputRevision`,
project engineering-source digest, Sheet companion digest, Style Companion digest (or explicit absent
marker), `athena.lock` digest, admitted package/item digests, compiler/schema version, and source-root
identity. It is not a client timestamp. Any mismatch is stale-source conflict; formatting-only edits
still require a matching token. Successful publication returns a new token and one publication
correlation identifier.

## 4. Features

### 4.1 Library-Backed Engineering Elements

**Description:** Projects consume versioned IEC/vendor Library Packages through Athena's existing package and lock model. A library item is referenced by package identity and digest rather than copied into each project. The item pairs real graphical bytes with inspectable metadata. Realizes UJ-1, UJ-3, and UJ-6.

#### FR-1: Resolve locked Library Packages

The system must resolve project-declared Engineering and Representation Packages using the repository lock and admitted digest.

**Consequences (testable):**

- A valid lock resolves the same package version and digest deterministically on repeated compilation.
- Missing, incompatible, stale, or digest-mismatched dependencies produce a diagnostic naming the package and correction.
- A project references a library item by package/item identity; it does not require copying SVG bytes into the project source.

#### FR-2: Publish paired Symbol metadata

The system must admit a Symbol only when its graphical resource and metadata are both present and mutually admitted.

**Consequences (testable):**

- Exactly one canonical SVG resource and one canonical metadata descriptor are admitted for each
  Symbol item. Descriptor digest uses canonical UTF-8 JSON with sorted object keys, explicit units,
  and normalized numbers.
- Required metadata: schema version, item identity, SVG resource digest, coordinate frame/units,
  center/hotspot, anchors, label zones, and stable connection-point keys. Optional metadata is
  namespaced vendor data; unknown unnamespaced fields reject package admission.
- Library metadata supplies keyed geometry and a compatibility envelope only. Engineering source
  remains authoritative for Port identity, direction, domain, flow kind, and semantic properties.
  A mismatch produces `Library port metadata conflicts with engineering port ...`; library data
  cannot mutate Engineering Ports.
- Missing, surplus, or digest-mismatched resource/metadata pairs block Canonical Scene publication
  with a diagnostic naming Symbol item and correction.
- Every admitted Symbol and Part trace carries package identity, item identity, version, admitted
  digest, provenance, and license origin, with stable primary and related navigation.

#### FR-3: Support representation and implementation references

The system must allow an Engineering Entity to reference a Symbol, a Part, or a coordinated Symbol/Part library pair.

**Consequences (testable):**

- One Engineering Entity may own multiple Functions. Each Function occurrence has its own stable
  occurrence identity and may bind one compatible Symbol representation through a Representation Binding;
  an Entity-level representation may also exist. Source Trace resolves Function before Entity when both
  are present.
- A Part binds to Entity implementation and may provide declared mapping to Function
  representations. Valid Part replacement preserves Entity, Function, Occurrence, and Relationship
  identities; only implementation reference, affected validation, and library trace change.
- Changing a Symbol reference changes representation while preserving all engineering identities.
- Symbol Macro and Element Macro references are read-only, package-resolved representation items with
  metadata, ports, provenance, and digest. They may not create breaker/contactor/overload solutions,
  engineering relationships, or validation facts; that belongs to future Pattern authority.

### 4.2 Canonical Scene and Professional Rendering

**Description:** The compiler publishes one validated Canonical Scene. The Theia canvas, SVG output, and PNG output consume that scene and the same locked library bytes. Real element geometry replaces placeholders. Realizes UJ-1 and UJ-6.

#### FR-4: Publish traceable scene occurrences

The system must publish each visible Occurrence with semantic identity, library item reference, resolved ports/labels, placement, style reference, and Source Trace.

**Consequences (testable):**

- Every selectable Occurrence has `semanticId`, `occurrenceId`, and `traceId`.
- Every connectable port resolves from a semantic connection-point key to a library geometry anchor.
- Invalid upstream semantic, projection, spatial, or library facts publish an unavailable state, never a partial scene.
- The active M44 fixture proves one Entity with at least two Function/Symbol occurrences and one
  cross-domain capability relationship. Visual proof remains electrical-first; the cross-domain case
  proves contracts without adding a second renderer.
- A Relationship carries typed kind, endpoint identities, domain, flow kind, and direction. Route
  paint cannot substitute for Relationship truth. Terminal, Cable, and Wire are later physical
  projections, not M44 semantic authorities.

#### FR-5: Render real element geometry

The canvas must render the admitted SVG geometry, not placeholder rectangles or renderer-invented symbols.

**Consequences (testable):**

- The active M44 example visibly contains real IEC/vendor symbols, ports, labels, and routes.
- Symbol geometry remains legible at the supported fit-to-page and zoom ranges.
- SVG export is canonical UTF-8 output with exact byte determinism for identical inputs. PNG export
  is a pinned Electron/Chromium raster proof; evidence records OS, Electron/Chromium version,
  viewport, DPR, fonts, and color profile, and repeated captures pass a declared pixel tolerance.
- Theia exposes `Export SVG` and `Export PNG` commands. User-selected output paths receive the
  Canonical Scene result; M44 ships no PDF, report, BOM, or manufacturing export command.

#### FR-6: Keep sheet presentation clean

The canvas must show the sheet frame and required coordinate annotations while keeping the drawing area visually clean.

**Consequences (testable):**

- Row letters and column numbers occupy the outer ruler cells.
- Internal grid lines remain hidden by default and can be enabled only as an interaction aid.
- No bottom table or debug decoration appears in the active M44 drawing area unless explicitly requested by a separate projection fact.

### 4.3 Style and Presentation Control

**Description:** Presentation style is configurable outside Engineering Reality. Defaults come from the asset/library and sheet style; session overrides are transient until explicitly solidified. Realizes UJ-5.

#### FR-7: Apply style cascade

The system must resolve asset defaults, Sheet/project defaults, role/route styles, occurrence overrides, and transient selection overlays in a deterministic order.

**Consequences (testable):**

- A later style layer overrides only fields it declares.
- Style changes do not alter semantic identity, capability, port direction, Relationship meaning, or Part binding.
- Repeated compilation of the same source and style companions produces identical resolved styles.
- For `rolling-shutter.sheet.athena`, style discovery checks same-basename
  `rolling-shutter.sheet.style.athena`. Missing companion means defaults only; more than one matching
  companion is an error. Style syntax is owned by the Sheet companion contract, and its digest
  participates in Source Revision, scene identity, Source Trace, and diagnostics.

#### FR-8: Persist or discard style edits explicitly

The user must be able to preview a style edit, discard it, or solidify it into the Style Companion.

**Consequences (testable):**

- Discarded session overrides do not change project files or subsequent compilation.
- Solidified styles are represented in `*.sheet.style.athena` and survive reopening.
- Line width, dash, cap/join, colors, labels, route markers, and port display can be independently controlled where supported by the style contract.

### 4.4 Editable Operations and Source Round-Trip

**Description:** Canvas editing is an operation-driven workflow. The frontend previews intent; the server validates and writes the smallest authoritative source patch; the scene is regenerated. Realizes UJ-2, UJ-3, UJ-4, UJ-5, and UJ-6.

#### FR-9: Submit typed Edit Operations

The canvas must submit typed operations for Move, Align, Distribute, Snap, Reconnect, Change Symbol, Bind Part, and Set Style.

**Consequences (testable):**

- Each operation includes target identity, Source Revision, and Source Trace context.
- Each operation declares its authority class:
  `PresentationEditOperation`, `RepresentationEditOperation`, or `EngineeringEditOperation`.
- Presentation operations write only Sheet/Style authority; Representation operations write only
  representation binding authority; Engineering operations write Engineering Reality and require
  Relationship, Capability, and Flow validation from the M42 validation path.
- Drag preview never writes source; one completed interaction produces one operation.
- The frontend cannot apply an operation by mutating Canonical Scene authority directly.

#### FR-10: Validate and atomically apply operations

The server must validate operation authority and apply a source patch atomically only when compilation and validation succeed.

**Consequences (testable):**

- Placement operations update `*.sheet.athena`; style operations update the same-basename
  `*.sheet.style.athena`; relationship/port edits update engineering source; Symbol/Part edits update
  declared project binding file(s). Each operation declares its exact writable file set. Coordinated
  Symbol/Part edits are one multi-file transaction.
- The server compare-and-sets every declared input digest against Source Revision, stages all patches,
  compiles and validates staged workspace, then atomically publishes all files or rolls back all files.
  No partial mutation or scene is observable.
- Successful operation emits one publication correlation identifier, one new Source Revision, and one
  Canonical Scene. A stale revision, invalid port direction, incompatible flow/signal kind, missing
  target, or failed compile produces no file mutation.
- No operation may use a shortcut path around its authority class. Reconnect and Bind Part must run
  engineering validation; Change Symbol must preserve Engineering Entity, Function, Relationship, and Port
  identity; Presentation operations must not touch Engineering Reality.

#### FR-11: Preserve history at operation granularity

The system must provide Undo/Redo for accepted canvas operations.

**Consequences (testable):**

- Undo restores source and scene state before one operation, not one pointer-move event.
- Redo reapplies the same validated operation when its preconditions remain valid.
- If Redo preconditions fail, Redo remains available, source remains unchanged, and one actionable
  stale/conflict diagnostic is emitted; failed Redo is not recorded as a new operation.
- Reopening the project after an operation reproduces the accepted result.
- Undo/Redo reads the Operation Journal of accepted source transactions. It never reverses raw mouse
  events or mutates Konva/canvas state directly.

### 4.5 Theia Canvas Integration

**Description:** Theia remains the shell. The frontend owns view interaction and renderer lifecycle; LSP/kernel own engineering decisions. Selection identifiers are shared so future semantic navigation and inspection can connect without making M44 a full workspace milestone. Realizes UJ-1 through UJ-6.

#### FR-12: Keep frontend free of engineering authority

The frontend must not resolve Relationships, choose valid ports, assemble source text, choose Parts, or run engineering validation.

**Consequences (testable):**

- Invalid operation tests fail in the server/kernel path, not through duplicated frontend rules.
- Canvas selection exposes `semanticId`, `occurrenceId`, and `traceId` to host services.
- Renderer replacement does not change source, operation, or scene contracts.

#### FR-13: Provide a bounded renderer backend

The system must isolate the interactive renderer behind a Renderer Backend contract and ship Konva Canvas2D for M44.

**Consequences (testable):**

- Asset cache, viewport culling, route/label layers, and interaction overlays do not require semantic recompilation on pan/zoom.
- Konva node tree is disposable adapter state, not scene model. Deleting or replacing Konva cannot change
  `AthenaDiagramScene`, Source Revision, Edit Operation, Operation Journal, or source authority.
- Future WebGL/WebGPU replacement can implement the backend contract without changing source or Edit Operations.
- Export paths consume the same scene and locked assets rather than a second interactive model.

### 4.6 Diagnostics and Product Evidence

**Description:** Failures are explicit, source-linked, and actionable. M44 closure is based on the active example and product screenshots, not only unit tests. Realizes UJ-6.

#### FR-14: Explain rejected publication and edits

The system must report the exact subject, problem, and correction when a library, scene, or Edit Operation cannot be accepted.

**Consequences (testable):**

- Diagnostics identify missing library dependencies, asset/metadata mismatch, unresolved ports, invalid direction/flow, stale revision, or compile failure.
- No diagnostic requires the user to understand internal renderer or transport codes to correct the source.
- The last accepted scene remains visible or an explicit unavailable state is shown; guessed legacy fallback is forbidden.

#### FR-15: Produce reproducible M44 evidence

The project must produce reproducible tests, screenshots, and export comparisons for the active M44 example.

**Consequences (testable):**

- Evidence is stored under `_bmad-output/implementation-artifacts/m44/`.
- The evidence includes visible real symbols, ports, labels, routes, source trace, at least one successful edit, and at least one rejected edit.
- The full fixed input tuple (project engineering source, Sheet and Style Companions, `athena.lock`,
  admitted library/resource bytes and metadata digests, source-root identity, compiler/schema/profile
  versions) produces stable scene digests and exact SVG bytes across repeated runs. Pinned PNG captures
  pass recorded pixel tolerance.

## 5. Non-Goals (Explicit)

- Full EPLAN or QElectroTech file/database compatibility.
- Runtime dependency on `reference/elements` or `reference/elements_contrib`.
- A new package manager separate from Athena's existing package/lock model.
- Copying reference SVG catalogs into every project.
- Full Symbol Macro or Element Macro authoring, parameter automation, or engineering Pattern governance.
- AI engineering decisions or AI-authored source mutation.
- Semantic Navigator, Folio, multi-perspective workspace, or complete Inspector product.
- PDF, report, BOM, manufacturing, simulation, or lifecycle outputs.
- WebGPU migration or a second interactive renderer.
- Compatibility shims for retired M0-M43 renderer and semantic-macro behavior.

## 6. MVP Scope

### 6.1 In Scope

- Existing Athena package/lock integration for library-backed Symbol and Part references.
- A representative IEC/vendor library fixture with SVG and normalized metadata.
- Canonical Scene asset references, port contracts, label zones, anchors, provenance, and digests.
- Real SVG-based Konva rendering with clean frame/rulers, routes, labels, ports, selection, and viewport culling.
- External Style Companion plus session override behavior.
- Move, Align, Distribute, Snap, Reconnect, Change Symbol, Bind Part, Set Style operations with explicit
  Presentation/Representation/Engineering authority classes.
- Atomic source round-trip and operation-level Undo/Redo.
- Operation Journal for all accepted M44 operations.
- Theia integration, source trace, diagnostics, screenshot, export, and regression proof.

Delivery order is explicit, but closure includes the complete authoring transaction loop:

- **Must slice:** locked package; one real Symbol; one Entity with two Function/Symbol occurrences;
  traceable ports and one cross-domain relationship; clean render; Move and Align; one rejected edit;
  exact SVG and pinned PNG evidence.
- **Completion slice:** Distribute, Snap, Set Style, Change Symbol, Reconnect, Bind Part, Undo, Redo,
  operation journal, performance transcript, restart/reopen proof, and all operation transcripts.
- **Deferred slice:** macro authoring, engineering Pattern authoring, broader library marketplace, and
  non-canvas lifecycle projections. No M44 operation is deferred.

### 6.2 Out of Scope for MVP

- Remote public library registry and publishing workflow; M44 consumes admitted repository packages. [ASSUMPTION: registry UX is a later package-platform milestone.]
- Full macro expansion, design automation, and engineering Pattern authoring. M44 representation macro
  references are read-only and cannot create engineering intelligence. [ASSUMPTION: macro authoring is not required to prove current canvas quality.]
- Universal 60 FPS for complex 100,000-element documents. M44 uses a checked-in benchmark profile:
  Windows 11, 1920x1080 viewport, DPR 1, 60 Hz display, pinned Electron/Chromium, and 300 real
  occurrences. After 3 seconds warm-up, p95 pan/zoom frame time must be <=20 ms (>=50 FPS), p95
  drag-preview latency <=100 ms, and 60-second heap growth <=20 MB. Transcript records CPU/GPU, OS,
  browser, fonts, and fixture digest; outside-profile results are exploratory only.

## 7. Cross-Cutting NFRs

### 7.1 Determinism and Integrity

- Same source, lock, library bytes, and style companions produce the same Canonical Scene identity,
  resolved asset references, and canonical SVG bytes. Pinned PNG repeats pass recorded pixel tolerance.
- No operation partially applies source or publishes a partial scene.
- Library bytes are admitted by digest and provenance/license is inspectable.

### 7.2 Responsiveness

- Pan and zoom do not recompile semantic or spatial authority.
- Local interaction redraws only affected viewport/layers where possible.
- On the checked-in benchmark profile, p95 pan/zoom frame time is <=20 ms, p95 move-preview latency
  <=100 ms, and 60-second heap growth <=20 MB. Failed budget blocks performance proof but never permits
  hiding real geometry or dropping source trace.

### 7.3 Usability and Accessibility

- Symbols, ports, labels, and selection states remain visually distinguishable at fit-to-page scale.
- Diagnostics identify user-facing subject/problem/correction in plain engineering language.
- Keyboard focus and operation commands remain reachable through Theia command surfaces.

### 7.4 Maintainability

- Frontend contains no duplicate engineering rules.
- Renderer backend, scene contract, operation contract, and package contract are independently testable.
- Production source contains no M44 demo/proof class and no legacy compatibility path.

## 8. Success Metrics

**Primary**

- **SM-1:** 100% of active M44 example occurrences render admitted IEC/vendor geometry; no placeholder rectangle is used for an engineering element. Validates FR-2, FR-4, FR-5.
- **SM-2:** 100% of accepted Move, Align, Distribute, Snap, Set Style, Change Symbol, Reconnect,
  Bind Part, Undo, and Redo proof operations round-trip through source and reopen to the accepted result.
  Validates FR-9 through FR-11.
- **SM-3:** 100% of invalid dependency, asset, port, revision, and compile cases leave source and last accepted scene unchanged while producing actionable diagnostics. Validates FR-1, FR-2, FR-10, FR-14.

**Secondary**

- **SM-4:** Repeated compile runs produce identical scene digests and exact SVG bytes; pinned PNG captures pass recorded pixel tolerance. Validates FR-4, FR-13, FR-15.
- **SM-5:** M44 screenshots show clean frame/rulers, readable labels, visible ports, independently styled routes, and no debug/bottom-table clutter. Validates FR-5 through FR-8 and FR-15.
- **SM-6:** Frontend operation tests contain no engineering-validation duplicate; invalid operation acceptance is covered by LSP/kernel tests. Validates FR-12 and maintainability NFRs.
- **SM-7:** Every accepted operation has an Operation Journal entry proving authority class, declared
  preconditions, writable files, accepted patch, staged compile result, publication correlation id,
  identity preservation, reopen behavior, Undo behavior, Redo behavior, and rejection path before M44
  closure. Validates FR-9 through FR-11.

**Counter-metrics**

- **SM-C1:** Do not optimize raw element count by hiding or replacing real geometry, labels, or ports in normal M44 mode. This prevents scale metrics from degrading engineering readability.
- **SM-C2:** Do not reduce source trace, validation, or atomicity to improve interaction latency. This prevents apparent speed from becoming silent engineering corruption.

## 9. Open Questions

1. Which package registry/transport should a future milestone use for remote public library publishing? M44 consumes repository-admitted packages and does not block on this choice.
2. Which normalized metadata document syntax should library authors use for Symbol/Part item descriptors?
   **Resolved for M44:** UTF-8 `symbol.yaml` package descriptors, parsed into canonical JSON for
   digesting. Schema is `athena-symbol-v1`; unknown unnamespaced fields reject admission, namespaced
   vendor extensions are preserved. No alternate descriptor syntax or compatibility parser is allowed.

Structured EPLAN-style `=Function +Location -Device` addressing is deferred. M44 uses typed Entity,
Function, Part, Symbol, Occurrence, and Relationship identities; labels cannot substitute for identity.
Terminal, Cable, Wire, BOM, report, manufacturing, and lifecycle projections remain later milestones.
M44 consumes Function/capability facts already resolved by M42; it does not select Functions from a
library Symbol.

## 10. Golden Authoring Loop

M44 closes only when one active example proves:

1. Initial sheet contains PLC, Contactor, and Motor using real admitted symbols.
2. User moves Contactor.
3. User aligns symbols.
4. User changes one Symbol representation.
5. User reconnects one valid Port.
6. User attempts one invalid connection and receives no mutation.
7. User replaces one Part through engineering validation.
8. User runs Undo.
9. User runs Redo.
10. User restarts/reopens and sees same accepted identities and result.

## 11. Assumptions Index

- Section 6.2: M44 does not include a remote public library registry UX.
- Section 6.2: M44 proves direct library element consumption; full macro authoring/automation is later.
- Section 6.2: M44 macro references are read-only package references only.
- Section 6.2: M44 is electrical-first visual proof with one cross-domain relationship; no second renderer.
- Section 6.2 and NFR 7.2: benchmark profile and numeric performance budgets are fixed in this PRD.
- Section 9: operation transaction shape and performance profile are architecture-freeze constraints;
  metadata encoding is fixed to `symbol.yaml` / `athena-symbol-v1`.
- Section 10: complete operation loop is required for M44 closure; no operation category is deferred.
