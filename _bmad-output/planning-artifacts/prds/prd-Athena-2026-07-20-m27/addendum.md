# M27 Addendum: Semantic Spatial Compiler Boundary

## Source Discussion

This addendum preserves the architectural depth from:

```text
draft/m27/Big-layout-discuss.md
```

The discussion clarified that M27 must not treat linework and auto-connection as simple renderer
polish. Athena needs a governed spatial-intent boundary so professional routing remains explainable,
deterministic, and compatible with future 2D, 3D, report, manufacturing, maintenance, and AI-facing
projections.

## Core Architectural Correction

Avoid this framing:

```text
Engineering Semantic Kernel
  -> Spatial Kernel
  -> Renderer
```

The term `Spatial Kernel` is too close to traditional CAD architecture and risks pulling Athena
toward a hidden OpenCascade/Parasolid-style geometry authority.

Prefer:

```text
Engineering Semantic Model
  -> Semantic Spatial Compiler
  -> Projection Routing
  -> Route Geometry Facts
  -> Presentation IR
  -> Renderer
```

or:

```text
Engineering Meaning
  -> Semantic Spatial Intent
  -> Projection-Specific Geometry
```

Athena should remain meaning-first:

```text
Meaning -> Spatial Intent -> Geometry
```

not:

```text
Geometry -> Meaning
```

## Three Routing Levels

M27 should distinguish:

1. **Semantic Connection**
   - Component/port identities
   - Signal role
   - Engineering constraints
   - No geometry

2. **Semantic Spatial Intent**
   - Preferred direction
   - Terminal orientation
   - Separation
   - Grouping
   - Lane preference
   - Component avoidance
   - Still no raw canvas truth

3. **Projection Routing**
   - Adapter or built-in route engine
   - Orthogonal points
   - Bends
   - Anchors
   - Route quality state
   - Paint-ready route geometry facts

## Backend Boundary

ELK, libavoid, yFiles, or a custom router may become useful later, but they belong below an
Athena-owned adapter boundary:

```text
Semantic Spatial Intent
  -> Routing Backend Adapter SPI
      -> Athena v0 router
      -> future ELK/libavoid/yFiles
  -> Normalized Athena Route Geometry Facts
```

External tools must not own:

- semantic connection meaning
- terminal identity
- layout or route authority
- source mutation
- document occurrence identity
- persisted engineering truth

## GLSP/Theia Position

GLSP should be treated as a projection interaction framework, not as a semantic authority.

```text
Semantic Projection
  -> GLSP Model Adapter
  -> Sprotty/Theia Rendering And Interaction
```

Theia may select, inspect, preview, navigate, and paint. It must not infer engineering truth from DOM
geometry, SVG segments, canvas scans, or visual route breaks.

## M27 Scope Consequence

M27 should prove the first narrow 2D electrical schematic version of this boundary:

- professional sheet frame and linework quality
- semantic spatial intent contracts
- spatial constraint priority, confidence, and source attribution
- route quality facts
- semantic connection preview, with source mutation acceptance deferred to M28 unless an existing
  governed mutation path is reused without scope expansion
- no component-crossing accepted routes
- no center fallback in accepted proof
- no backend hard dependency

M27 should not attempt general CAD, 3D, cabinet, harness, or standards-complete routing.

## Refactor And Cleanup Rule

Because Athena is not live yet, M27 may break internal APIs when that strengthens the long-term
architecture. The safety rule is not downstream compatibility at all costs. The safety rule is
authority-chain preservation plus final cleanup.

Acceptable M27 refactor:

- clarifies layout versus routing ownership
- moves duplicated routing/layout decisions into semantic spatial intent or projection routing
- removes stale helper paths after the new path is verified
- updates tests and samples to match the new architecture

Unacceptable M27 refactor:

- rewrites broad compiler, LSP, Theia, or Presentation IR ownership without need
- introduces a CAD geometry kernel
- leaves old docs claiming unsupported behavior
- keeps obsolete code paths that confuse future stories

Final M27 completion must include a purge pass for stale code, docs, screenshots, sample references,
and design claims. Anything intentionally retained must have a current owner, reason, and deferred
milestone.

## Constraint Priority

M27 spatial intent should include enough metadata to explain conflicts and support future AI
optimization:

```text
SpatialIntent {
  subject
  relation
  priority
  confidence
  source
}
```

The initial hierarchy should be:

- hard constraints: semantic connectivity, terminal attachment, component-body avoidance in accepted
  proof
- strong preferences: terminal-side entry, route separation, lane grouping, route ordering
- soft preferences: shortest path, symmetry, visual balance, compact bend count

## Visual Acceptance Thresholds

M27 screenshot checks should be visual-regression guards, not pixel-perfect matching against
QElectroTech or any other tool. Structured acceptance should come from DOM and proof payloads:

- sheet frame exists
- title block exists
- route count and terminal-anchor count are valid
- center fallback count is zero in the accepted proof
- component crossing count is zero in the accepted proof
- verbose semantic route labels are not always visible
- route quality states are present and deterministic
