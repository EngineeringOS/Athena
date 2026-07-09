# Athena Milestone Roadmap

## Purpose

This note records the current milestone sequence after the completed M5 proof.

Its job is to keep the next milestone boundaries explicit so later planning does not drift back to older draft-era meanings.

## Authority

This roadmap follows the completed M5 PRD and M5 architecture spine.

That means:

- M5 is now complete as the repository/package graph milestone
- M6 is the semantic SCM milestone
- graphical projection remains later than both

Older draft notes that assigned M5 or M6 to UI, AI, or GLSP research should now be treated as exploratory background, not as the active delivery contract.

## Completed Milestones

| Milestone | Status | Proven outcome |
| --- | --- | --- |
| M0 | done | DSL -> AST -> Engineering IR -> validation -> SVG |
| M1 | done | runtime-owned workspace, graph, command, history, diff, plugin-hosted execution |
| M2 | done | Layout IR, Geometry IR, projection sessions, multi-view desktop proof |
| M3 | done | stable plugin API, hosted plugin platform, external proof domains |
| M4 | done | Theia desktop shell, repository session, Athena LSP, professional workbench, semantic inspection |
| M5 | done | governed repository contract, deterministic package graph, canonical lock, repository graph session, package-aware IDE operation |

## Next Milestone Sequence

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

M6 comes after M5 freezes repository and package meaning.

Core objective:

> Lift source-control and review behavior from raw file mechanics into semantic repository and package meaning without replacing Git.

Core scope:

- a VCS-neutral semantic SCM boundary that sits above vendor storage mechanics
- semantic diff
- intent commit flows
- semantic review flows
- publish-oriented semantic history
- package-aware change analysis

What M6 is not:

- basic package contract work that belongs to M5
- graphical projection milestone
- AI-first milestone

### M7 - Graphical Projection And Visual Workbench

M7 is the first milestone where real graphical projection should become the center.

Core objective:

> Add a governed graphical projection path under the same semantic authority model proven by M0 to M6.

Core scope:

- projection protocol and server boundary
- read-only and later interactive graphical views
- layout/presentation state kept downstream of canonical semantic state
- future GLSP-class or equivalent graph architecture if still the best fit when M7 begins

What M7 is not:

- replacing the LSP path
- moving engineering truth into canvas state
- bypassing repository/package or semantic SCM contracts

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

## Immediate Recommendation

The next planning cycle should start with M6 semantic SCM planning on top of the completed M5 repository/package graph foundation.
