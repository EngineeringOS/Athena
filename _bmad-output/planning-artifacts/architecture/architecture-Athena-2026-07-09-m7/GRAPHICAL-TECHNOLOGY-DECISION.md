# Athena M7 Graphical Technology Decision

## Decision

Athena keeps the current M7 graphical path as:

- JVM-first projection authority in `kernel/runtime`
- typed transport through `ide/lsp`
- translation-only graph adapter in `integrations/graph-glsp`
- Athena-owned Theia workbench rendering in `ide/theia-frontend`

Athena does **not** adopt a full GLSP client/server runtime as the M7 implementation center.

Instead, M7 freezes a narrower decision:

- GLSP-shaped vocabulary remains useful as the adapter boundary
- the current proof stays translation-only and inspectable
- richer GLSP-class editor integration remains a post-M7 option, not a hidden M7 dependency

## Why This Is The Right M7 Decision

### 1. JVM-First Authority

M7 requires graphical state to stay downstream of Athena-owned semantic and projection authority.

The current path satisfies that:

- canonical semantic state remains in `Engineering IR`
- runtime owns active projection sessions
- the frontend only consumes typed projection payloads

Moving to a fuller client/server diagram stack inside M7 would add technology breadth without improving semantic authority preservation.

### 2. Theia Product-Shell Fit

Athena already has a real Theia product shell and repository/LSP session lifecycle.

The current path integrates directly with that shell:

- `AthenaGraphWorkbenchWidget` is a first-class workbench surface
- Graphical View opens beside the active editor
- source, inspection, problems, and graphical review stay in one product composition model

That proves product fit without replacing the current shell architecture.

### 3. Translation-Only Adapter Discipline

M7 required an explicit graph boundary rather than ad hoc frontend rendering code.

The current path provides that:

- `integrations/graph-glsp` owns GLSP-shaped vocabulary only
- the adapter translates Athena projection payloads into disposable graph data
- it does not call the JVM, filesystem, or repository services directly

This keeps graph-framework vocabulary downstream and replaceable.

### 4. Inspectability

The current path stays easy to inspect:

- runtime projection payloads are typed
- render contributions and active surface mappings are visible in transport
- the graph workbench can expose selection, diagnostics, and active mapping context

That is better aligned with M7 than a deeper framework integration that hides more state behind framework internals.

### 5. Deterministic Refresh

M7 required deterministic refresh from the same upstream state.

The current implementation preserves that:

- runtime projection sessions cache and invalidate deterministically
- view switches stay runtime-owned
- frontend refresh rebuilds from typed payloads instead of mutating local diagram truth

This is the correct behavior to freeze before richer graphical editing is attempted.

## What Athena Chose Not To Do In M7

Athena intentionally did **not** make M7 depend on:

- full GLSP server integration
- notation-pack editing
- renderer-owned persistence
- unrestricted graphical editing
- a second projection authority in the frontend

Those would widen the milestone before the current downstream boundary is fully proven.

## Consequences

### Positive

- M7 ends with a credible, working graphical direction
- later framework swaps remain possible behind the adapter seam
- domain-specific renderer mappings now enter through extensions instead of kernel forks
- the workbench already proves a graph-first engineering posture

### Negative

- the current graph surface is still a proof implementation, not the final editor stack
- there is not yet a real GLSP client/editor lifecycle in production use
- bidirectional code/graph editing remains deferred

## Follow-On Recommendation

Post-M7 milestones may evaluate two safe next steps:

1. Keep the current Theia workbench posture and deepen the adapter/workbench path for governed edit actions.
2. Introduce fuller GLSP-class editor runtime only if it clearly improves governed bidirectional behavior without weakening Athena authority boundaries.

The key rule is unchanged:

> any future diagram framework must remain downstream of Athena semantic, projection, and command authority.
