# M35 Architecture Freeze Decision

## Verdict

**PASS - ARCHITECTURE MAY FREEZE.** F-1 through F-5 are closed consistently across the architecture
spine, PRD, and normative addendum. The combined contracts expose no fresh freeze-blocking ownership,
identity, coordinate, routing, constraint, or source-grammar divergence.

## F-1 Through F-5

| Gate | Result | Evidence |
| --- | --- | --- |
| F-1 right-handed rail frame | **CLOSED** | AD-5 defines horizontal basis `(+X,+Y)` and vertical basis `(+Y,-X)`, requires determinant `+1`, forbids target-frame mirroring, and keeps the shared transform owner. |
| F-2 parent containment | **CLOSED** | AD-6 requires MountingSurface, Duct, and TerminalGroup rectangles inside Enclosure and each complete oriented Rail interval inside its MountingSurface, with invalid input blocking composition. |
| F-3 exact lane formula | **CLOSED** | AD-12 defines `U = S - 2M`, lane centre `M + ((2i + 1) * U) / (2N)`, exact reduced rational millimetres, stable assignment order, capacity, and odd/non-divisible golden tests. |
| F-4 same-duct adjacency | **CLOSED** | AD-12 derives one adjacency only for same-Duct, disjoint-interior channels sharing one positive trimmed boundary segment; it defines the exact midpoint and rejects cross-duct, corner, overlap, gap, and multiple-intersection cases. |
| F-5 source scope and alias migration | **CLOSED** | AD-15 and the addendum make `installation cabinet` a system member, `route` an installation member, Cabinet the only v0 kind, aliases mandatory for every connection, same-source lookup mandatory, and alias-free grammar/AST/fixtures deleted without a compatibility parser. The accepted proof is a complete source unit. |

## Fresh Divergence Attack

- Physical frames remain rigid and non-mirroring through Cabinet visual transformation.
- Supporting topology and mounted occurrences have separate, deterministic containment checks.
- Channel geometry, lane geometry, adjacency, connection identity, and route intent now form one closed input chain.
- Exact rational lane coordinates avoid platform-dependent rounding before drawing transformation.
- Alias identity is independent from endpoints and group names while remaining source-unit scoped.
- Grouped connection authoring preserves individual engineering connection identity.
- The system/installation/route declaration scopes are explicit and consistent with the accepted proof.
- Unreleased alias-free syntax has one migration/deletion owner and no parallel parser path.
- Cabinet remains the only M35 installation kind and visible product surface.
- No new renderer, SVG, package, ECS, or mutation authority is introduced by these fixes.

No fresh freeze blocker was found. No reviewed planning artifact was modified; only this requested
review report was added.
