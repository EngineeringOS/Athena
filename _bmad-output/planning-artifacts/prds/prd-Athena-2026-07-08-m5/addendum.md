# M5 Addendum

This addendum captures useful M5 planning detail that informs architecture and epic shaping but is intentionally more implementation-shaped than the main PRD body.

## 1. Agreed Milestone Split

The active sequence is now:

- **M5** - repository and package graph
- **M6** - semantic SCM
- **M7** - graphical projection and visual workbench

This split is now recorded in `docs/roadmap/athena-milestone-roadmap.md`.

## 2. Why M5 Must Come Before M6

Semantic SCM above Git needs stable answers to:

- what a package is
- what a repository contract is
- what dependency meaning is
- what lock state means
- what constitutes semantic package change

Until those are frozen, semantic diff and review would be built on moving ground.

## 3. Why M5 Must Come Before Real Graphical Projection

Graphical projection is important, but it should consume stable package and repository meaning rather than help define it.

The important M5 implication is:

- package semantics are upstream
- SCM semantics are next
- graphical projection is downstream of both

The GLSP note from `draft/m4/002-glsp.md` remains relevant research, but it is not the M5 milestone contract.

## 4. Likely Architecture Concerns For The M5 Spine

The architecture phase should resolve at least these concerns:

### 4.1 Repository Contract Shape

- repository root responsibility
- package-local responsibility
- how `athena.yaml` and `athena.lock` relate
- what is repository-global versus package-local

### 4.2 Package Identity

- local identity rules
- whether version is mandatory in the first cut
- whether namespace or registry coordinates are deferred

### 4.3 Resolver Scope

- purely local dependencies first versus future-ready external coordinates
- deterministic ordering and failure policy
- lockfile update rules

### 4.4 Session Upgrade

- how the current M4 repository session upgrades into a repository graph session
- how the IDE sees repository/package state without becoming its authority

## 5. M5 Supporting Backlog That Should Not Replace The Core

These are valid items for M5 execution, but they should stay subordinate to the package-graph contract:

- minimal syntax highlighting for `.athena`
- semantic token groundwork
- hover / rename / formatting groundwork where directly useful
- frontend regression harness for Athena-owned Theia surfaces
- backend transport regression proof when version-sensitive IDE transport logic changes

## 6. Suggested Epic Spine

### Epic 1

Freeze the repository and package contract:

- `athena.yaml`
- `athena.lock`
- package identity
- repository/package layout rules

### Epic 2

Build the semantic package resolver:

- dependency resolution
- lock production
- deterministic graph state
- package diagnostics

### Epic 3

Expose package semantics in the IDE without breaking M4 boundaries:

- package-aware repository session state
- package-aware diagnostics and feedback
- narrow usability support such as basic highlighting if needed

## 7. Guardrail For Scope Control

When a proposed M5 feature appears, ask:

1. Does it freeze repository/package meaning?
2. Does it directly improve package-graph operability?
3. Is it actually SCM?
4. Is it actually graphical projection?

If the answer is:

- `1` -> M5 core
- `2` -> M5 supporting backlog
- `3` -> move to M6
- `4` -> move to M7
