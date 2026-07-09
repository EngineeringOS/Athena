# M6 Addendum

This addendum captures useful M6 planning detail that informs architecture and epic shaping but is intentionally more implementation-shaped than the main PRD body.

## 1. Agreed Milestone Split

The active sequence is now:

- **M5** - repository and package graph
- **M6** - semantic SCM
- **M7** - graphical projection and visual workbench

This split is already recorded in `docs/roadmap/athena-milestone-roadmap.md`.

## 2. Why M6 Must Come After M5

Semantic SCM above vendor storage needs stable answers to:

- what a repository means
- what a package means
- what dependency and lock meaning are
- what package identity is
- what constitutes semantic repository/package change

M5 now provides that foundation.

The key M6 implication is:

- repository/package meaning is upstream
- semantic change/review/history is next
- graphical projection stays downstream of both

## 3. Why M6 Must Stay Above Vendor Storage Mechanics

The user-facing Athena model should stay vendor-neutral.

Athena operators should reason about:

- semantic baseline
- semantic diff
- semantic review
- commit intent
- publish-oriented history

They should not need Git-first vocabulary to understand Athena's semantic model.

That means:

- `repository-model` remains clean and vendor-neutral
- any SCM-facing abstraction should depend on M5 contracts from above
- Git can be the first practical adapter, but not the semantic authority

## 4. Relationship To M1 Runtime Diff And History

M1 already proved:

- runtime-owned commands
- semantic diff
- history and replay

M6 should not ignore that work.

Instead, M6 should generalize the M1 change idea from:

- in-memory runtime command and project state

to:

- repository/package baseline versus current semantic state
- review and commit preparation over governed repository meaning

The architecture phase should decide what can be reused directly and what needs a new M6 boundary.

## 5. Likely Architecture Concerns For The M6 Spine

The architecture phase should resolve at least these concerns:

### 5.1 Semantic SCM Boundary Shape

- whether the first explicit boundary is `scm-model`, `semantic-scm`, or equivalent
- how it depends on M5 repository/package contracts
- how it stays separate from vendor adapters

### 5.2 Baseline Model

- working tree versus committed baseline
- how baseline identity is represented without leaking vendor terms into the core semantic model
- deterministic failure behavior when baseline resolution fails

### 5.3 Semantic Change Model

- stable change category taxonomy
- package-aware versus engineering-aware changes
- validation and contract consequence modeling

### 5.4 Commit Preparation Model

- what a structured intent-commit result looks like
- what is inspectable model versus UI projection
- how adapter handoff remains downstream

### 5.5 Review And History Model

- summary shape for semantic review
- package/version impact modeling
- how publish-oriented history remains narrow in M6

### 5.6 IDE Consumption Path

- how semantic SCM output reaches runtime, LSP, and Theia surfaces
- how additive workbench composition continues from M4 and M5
- how to avoid frontend-owned SCM semantics

## 6. Suggested Epic Spine

### Epic 1

Freeze the semantic SCM foundation:

- semantic SCM boundary
- baseline model
- semantic change model
- deterministic semantic diff

### Epic 2

Build commit and review semantics:

- commit intent preparation
- semantic review summaries
- validation and contract impact explanation

### Epic 3

Add publish-oriented semantic history and product consumption:

- package-aware history surfaces
- release/publish relevance modeling
- additive runtime/LSP/IDE exposure

## 7. Relationship To Theia SCM

Theia's SCM support is a good product-edge host abstraction, not a good semantic core.

The M6 rule should be:

- **yes, refer to Theia SCM** for workbench contribution shape
- **no, do not couple Athena semantic SCM to Theia SCM types**

Practical implication:

- Athena should define its own semantic SCM boundary first
- a Git or other vendor adapter should sit below that semantic boundary
- a Theia adapter should sit beside the product shell and map Athena semantic SCM output into Theia SCM providers, resource groups, actions, and views

This preserves the current architecture rule:

- Athena semantic meaning remains owned by JVM-side semantic layers
- Theia remains the downstream IDE/workbench host
- replacing or extending the workbench later does not force a rewrite of Athena semantic SCM contracts

Provider-specific Theia actions and SCM view wiring are still useful, but they should remain presentation integration rather than semantic ownership.

### Recommended Boundary Split

1. `kernel` or semantic core
   - baseline model
   - semantic diff model
   - review summary model
   - commit intent model
   - publish-oriented history model
2. vendor adapter layer
   - Git-backed baseline loading and commit execution
   - no semantic authority, only substrate access
3. product adapter layer
   - Theia SCM provider bridge
   - SCM view wiring
   - Athena-specific commands and decorations

This is the anti-corruption-layer approach: Athena learns from Theia's SCM integration model without letting Theia define Athena's semantic SCM language.

## 8. Likely Supporting Backlog That Should Not Replace The Core

These are valid items for M6 execution, but they should stay subordinate to semantic SCM meaning:

- richer read-only semantic inspection tied to change review
- focused frontend regression harness for Athena-owned Theia SCM surfaces
- focused backend/LSP regression proof when semantic SCM payloads evolve
- narrow authoring affordances that directly support review or commit preparation

## 9. Guardrail For Scope Control

When a proposed M6 feature appears, ask:

1. Does it freeze semantic history/review meaning?
2. Does it directly improve semantic SCM operability?
3. Is it actually repository/package contract work?
4. Is it actually graphical projection?

If the answer is:

- `1` -> M6 core
- `2` -> M6 supporting backlog
- `3` -> move back to M5 only if the contract is genuinely incomplete
- `4` -> move to M7
