# M3 Addendum

This addendum captures technical direction that informs M3 architecture and implementation planning but is intentionally too mechanism-heavy for the PRD body.

## 1. Agreed Direction

- M3 may refactor kernel contracts and compiler structure once to establish a stable extensibility boundary.
- After that boundary is set, adding another proof domain should be extension work rather than fresh kernel domain work.
- The first proof host remains the current JVM-first ServiceLoader path.
- Future plugin loading may grow toward local-directory and remote-URL installation and dynamic lifecycle, but M3 does not need to deliver that capability.
- The existing `domain-electrical` extension is the correct base for the first real proof domain.

## 2. Kernel-Minimal Semantics Direction

The kernel should stay small. For M3, that means:

- keep the parser and authored source model generic
- keep canonical semantic ownership generic
- let plugins declare domain schema and domain rules
- let plugins contribute validation and rendering behavior
- let plugins participate in the pass pipeline through governed contribution points

This direction avoids turning the parser into a plugin-defined language host too early, while still keeping domain meaning outside kernel-owned domain code.

## 3. Recommended Interpretation Boundary

The preferred M3 split is:

- **kernel-owned**
  - generic DSL structure
  - AST ownership
  - canonical semantic contracts
  - pass orchestration
  - runtime orchestration
  - projection and backend orchestration
- **plugin-owned**
  - domain schema
  - domain-specific semantic interpretation
  - domain validation rules
  - domain rendering contribution
  - optional domain-specific passes within approved contribution points

This is the smallest practical boundary that still proves platform extensibility.

## 4. Compiler Pipeline Intent

M3 should move the compiler toward an explicit pass pipeline. The exact class structure is architecture work, but the milestone should support a model close to:

```text
parse
-> lower
-> semantic enrichment
-> validation
-> projection/backend preparation
-> backend rendering
```

Plugin participation should happen through stable contribution points, not through ad hoc compiler branching.

## 5. Electrical Proof Posture

The electrical proof should stay intentionally small:

- `Motor`
- `Lamp`
- `Switch`
- `Wire`

The goal is not electrical product breadth. The goal is proving that a real domain can live outside kernel-owned domain code.

## 6. Dummy Proof Posture

The dummy plugin should remain deliberately synthetic. Its job is to prove:

- the SPI is generic
- the test matrix is real
- the kernel is not secretly electrical-shaped

## 7. M3 Output Workspace

All M3 planning artifacts for this cycle should live under:

- [`_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-07/`](.)

This keeps M3 planning isolated from the previously completed M1 and M2 planning runs.
