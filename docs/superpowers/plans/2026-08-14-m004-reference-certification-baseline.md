# M004 Reference Certification Baseline Plan

> **For agentic workers:** This plan is research and governance only. It does not authorize product-code edits. A checked item means evidence was captured, not that Athena implements the referenced feature.

**Goal:** Establish a primary-source-evidenced QElectroTech behavior inventory, a Graphite architecture/shell inventory, an Athena prototype gap audit, and gates that prevent unsupported implementation claims.

**Architecture:** QElectroTech supplies electrical behavior and property-plate semantics. Graphite supplies application dispatch, messages, viewport/overlays, workspace/panel layout, frontend protocol, and modern shell interaction. Athena must map both references before any vertical product slice can be implemented.

**Tech Stack:** Markdown evidence registry; local QElectroTech user documentation/source; local Graphite source; current Athena Rust workspace.

---

### Task 1: Freeze Unsupported Claims

**Files:**
- Modify: `AGENTS.md`
- Create: `docs/superpowers/specs/2026-08-14-m004-reference-certification-baseline.md`

- [x] Record that M001-M003 are prototype history, not QElectroTech parity or Graphite professional-shell certification.
- [x] Define approved certification vocabulary and prohibit unsupported `complete`, `parity`, `mirrored`, and `professional` claims.
- [x] Define nine gates: reference, mapping, architecture, behavior, persistence, cross-platform, visual, adversarial, and user acceptance.

### Task 2: Build QElectroTech Behavior Evidence

**Files:**
- Create: `docs/superpowers/research/2026-08-14-m004-qelectrotech-behavior-inventory.md`
- Create: `docs/superpowers/specs/2026-08-14-m004-qet-electrical-inventory.md`

- [x] Inventory project, folio, title-block, library, element, terminal, conductor, numbering, variables, reports, and export behavior with local primary-source evidence.
- [x] Inventory project persistence and undo/redo behavior, and assign explicit `UNRESEARCHED` IDs to remaining schema, editor, print, UI, preference, drawing, and basic-object families.
- [x] Record confirmed limits, documentation/source conflicts, and unknown areas without resolving them by inference.
- [x] Define QET-PLATE-001: QElectroTech determines electrical panel content while Graphite determines panel layout/lifecycle.
- [x] Capture field-level minimum groups for project, folio/title-block, element, and conductor plates.

### Task 3: Build Graphite Architecture Evidence

**Files:**
- Create: `docs/superpowers/research/2026-08-14-m004-graphite-editor-architecture-inventory.md`
- Create: `docs/superpowers/specs/2026-08-14-m004-graphite-architecture-inventory.md`

- [x] Trace `Editor -> Dispatcher -> Message handlers -> FrontendMessage` from Graphite source with line-level evidence.
- [x] Trace Graphite workspace panel layout, layout targets/widget callbacks, document portfolio, tool handler/FSM, viewport/PTZ, overlay, properties-panel, input, history, and WASM/frontend boundaries.
- [x] Mark image/vector/node-graph behavior explicitly non-transferable unless separately approved.
- [x] Define a Graphite-derived Athena target spine without implementing it.

### Task 4: Audit Athena Against Both References

**Files:**
- Create: `docs/superpowers/specs/2026-08-14-m004-athena-prototype-gap-audit.md`

- [x] Classify each current Athena crate and frontend/desktop entry point as `RETAIN`, `EXTRACT`, `REWRITE`, `DELETE`, or `UNDECIDED`.
- [x] Prohibit additional feature or style work in modules classified `REWRITE` until the Graphite-derived application spine is specified.
- [x] Add exact QET/GRA ID dependencies to every retained/extracted boundary after Task 3 evidence is complete.

### Task 5: Produce the First Authorized Vertical-Slice Proposal

**Files:**
- Create: `docs/superpowers/specs/2026-08-14-m005-application-spine-and-folio-foundation.md`
- Create: `docs/superpowers/plans/2026-08-14-m005-application-spine-and-folio-foundation.md`

- [x] Scope exactly one workflow: project -> ordered folio -> title-block variables -> save/reopen, routed through the Graphite-derived application/message/frontend boundary.
- [x] Cite only the reviewed M005 set: QET-PROJ-001, QET-PERSIST-001, QET-FOLIO-001/002, QET-TITLE-001/002, QET-VAR-001, QET-HISTORY-001, QET-PLATE-001, GRA-APP-001, GRA-DISPATCH-001, GRA-MSG-001, GRA-FRONTEND-001, GRA-LAYOUT-001, GRA-PANEL-001, GRA-PORTFOLIO-001, GRA-DOC-001, GRA-HISTORY-001, GRA-PROPS-001, GRA-WASM-001, GRA-PLATFORM-001, and GRA-UX-001.
- [x] Define field-level model, messages, handler state ownership, frontend outputs, desktop/web workflow, persistence rules, reference screenshots, and every required gate before code is permitted.
- [ ] Submit M005 specification for user approval. Do not write implementation code before approval.

### Task 6: M004 Verification and Review

**Files:**
- Create: `docs/superpowers/verification/2026-08-14-m004-reference-certification-baseline.md`
- Modify: `docs/superpowers/plans/2026-08-14-m004-reference-certification-baseline.md`

- [x] Verify every QET and GRA row has primary-source evidence or `UNKNOWN` status.
- [x] Verify every current Athena crate and shell/platform entrypoint or generated-artifact group has an explicit audit classification and no unsupported product claim remains in new M004 docs.
- [x] Record `CONFIRMED`, `DEDUCED`, `UNKNOWN`, `FAILED`, and `SKIPPED` separately.
- [x] Commit/push the evidence package for review. User acceptance remains a separate unchecked gate.
