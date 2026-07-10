# Athena Milestone Roadmap

## Purpose

This note records the current milestone sequence after the completed M7 proof.

Its job is to keep the next milestone boundaries explicit so later planning does not drift back to older draft-era meanings.

## Authority

This roadmap follows the completed M7 PRD and M7 architecture spine.

That means:

- M5 is complete as the repository/package graph milestone
- M6 is complete as the semantic SCM milestone
- M7 is complete as the first graphical projection and visual workbench milestone
- the next milestone is not frozen in this note

Older draft notes that assigned M5, M6, or M7 to UI-only, AI-first, or unresolved framework research should now be treated as exploratory background, not as the active delivery contract.

## Completed Milestones

| Milestone | Status | Proven outcome |
| --- | --- | --- |
| M0 | done | DSL -> AST -> Engineering IR -> validation -> SVG |
| M1 | done | runtime-owned workspace, graph, command, history, diff, plugin-hosted execution |
| M2 | done | Layout IR, Geometry IR, projection sessions, multi-view desktop proof |
| M3 | done | stable plugin API, hosted plugin platform, external proof domains |
| M4 | done | Theia desktop shell, repository session, Athena LSP, professional workbench, semantic inspection |
| M5 | done | governed repository contract, deterministic package graph, canonical lock, repository graph session, package-aware IDE operation |
| M6 | done | semantic baseline, semantic diff, review/commit/history, runtime/LSP/Theia semantic SCM panel |
| M7 | done | projection model, runtime-owned projection sessions, graph adapter, graph-first workbench, first renderer proof |

## Milestone Notes

### M5 - Repository And Package Graph

M5 is complete.

Proven outcome:

- one canonical repository-root `athena.yaml`
- one canonical derived `athena.lock`
- one VCS-neutral `:kernel:repository-model` boundary
- deterministic local-first package resolution and lock validation
- one runtime-owned `RepositoryGraphSession` per product window
- Athena IDE package diagnostics, repository graph feedback, and narrow `.athena` editor hardening

Supporting backlog carried forward after M5:

- later semantic token work
- hover / rename / formatting beyond the narrow M5 slice
- parser-evolution watchpoint for the Athena language front-end if the current hand-rolled parser becomes a bottleneck
- frontend regression harness for Athena-owned Theia surfaces
- backend transport regression proof when version-sensitive IDE transport logic changes

What M5 did not become:

- semantic SCM
- graphical projection milestone
- UI-polish-only milestone
- Git abstraction or source-control vendor abstraction inside `repository-model`

### M6 - Semantic SCM

M6 is complete.

Proven outcome:

- a VCS-neutral semantic SCM boundary above vendor storage mechanics
- semantic diff, review, commit-intent, and publish-oriented history flows
- runtime-owned semantic SCM state exposed through Athena LSP
- Theia semantic SCM inspection as a downstream product surface

### M7 - Graphical Projection And Visual Workbench

M7 is complete.

Proven outcome:

- dedicated projection protocol and server boundary under runtime and `ide/lsp`
- graph-first split workbench with source and graphical view visible together
- infinite-canvas-style diagram surface and professional engineering-workbench density
- inspect-first graphical review and navigation with transient frontend state by default
- layout and presentation state kept downstream of canonical semantic state
- explicit first graphical technology path recorded instead of left open-ended

What M7 did not become:

- a replacement for the Athena LSP path
- canvas-owned engineering truth
- a bypass around repository/package or semantic SCM contracts
- full bidirectional graphical authoring
- final QElectroTech/EPLAN-class domain depth in one step

Current architecture decision record:

- [`_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/GRAPHICAL-TECHNOLOGY-DECISION.md`](../../_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/GRAPHICAL-TECHNOLOGY-DECISION.md)

## Practical Backlog Placement

| Concern | Belongs to |
| --- | --- |
| syntax highlighting | M5 supporting backlog |
| semantic tokens | M5 supporting backlog |
| hover / rename / formatting | M5 supporting backlog |
| parser stack evolution (`hand-rolled` -> `ANTLR` / `tree-sitter` / other) | kernel-language watchpoint; plan explicitly when language complexity or tooling needs justify it, not by default as M6 or M7 core |
| richer read-only semantic inspection | M5 or M6 supporting backlog |
| SCM abstraction above Git/vendor storage | M6 core |
| semantic diff / semantic review | M6 core |
| real diagram / graph canvas / GLSP-class projection | M7 core |
| governed bidirectional code/graph edit-review | post-M7 milestone |
| QElectroTech/EPLAN-class domain workbench depth | post-M7 milestone |

## Cross-Cutting Technical Watchpoints

### Parser Evolution

Athena currently uses a deliberately small hand-rolled parser, which is the correct fit for the completed M0 proof and the current M5 editor hardening.

That does **not** mean the parser choice is frozen forever.

If future language growth requires stronger grammar tooling, better incremental parsing, richer editor tooling, or broader syntax-error recovery, the parser stack should be reviewed explicitly as a **kernel-language decision**.

Important boundary:

- this is **not automatically M6**, because semantic SCM is downstream of language meaning
- this is **not automatically M7**, because graphical projection is also downstream of language meaning
- it should be planned as a dedicated language-front-end hardening decision when there is concrete pressure from DSL complexity, IDE tooling, or compiler maintenance cost

## Planning Rule

When a backlog item is discussed, ask:

1. Does it freeze repository/package meaning?
2. Does it freeze semantic history/review meaning?
3. Does it introduce graphical projection?

If the answer is:

- `1` -> M5
- `2` -> M6
- `3` -> M7

If it is only IDE usability polish, it should attach to the current nearest milestone as supporting backlog, not replace the milestone core.

## Post-M7 Carry-Forward

The next milestone should build on the completed M7 proof instead of reopening it.

Primary carry-forward items:

- governed bidirectional code/graph edit and review
- graph actions routed back through Athena commands and semantic authority
- richer domain workbench behavior that can grow toward electrical and other industrial projections without fragmenting the core model
- notation, symbol-pack, and renderer depth beyond the first relationship-forward proof
