# M35 Architecture Spine Divergence Review

## Verdict

**FREEZE BLOCKED.** The spine establishes the right owners, but twelve shared contracts remain
underspecified. Independent stories can obey every current AD and still produce incompatible models,
join keys, coordinate transforms, package snapshots, routes, traces, or future mutation commands.
These are architecture holes, not implementation details.

## D-1 - Physical contract inputs have no canonical resolution policy

**Story pair that can diverge while obeying AD-2, AD-3, and AD-27:**

- A physical-trait story maps `ResolvedPhysicalTraitDefinition` to a complete
  `PhysicalInstallationContract`, then uses project facts only for missing fields.
- A language/compiler story treats any explicit project installation block as replacement of the
  complete resolved trait contract.

Both consume only admitted sources named by AD-3. They can disagree on footprint, mounting type,
clearance, provenance, diagnostics, and the physical-contract digest for the same subject.

**Required AD fix:** Extend AD-3 with one kernel-owned pure
`PhysicalInstallationContractResolver`. It must emit exactly one
`ResolvedPhysicalInstallationContract` per authored installation occurrence using these rules:

1. Resolve each field independently.
2. An explicit project field overrides a resolved physical-trait field.
3. Multiple values at the same precedence are an ambiguity diagnostic, never first-wins.
4. Missing required fields fail before `PhysicalInstallationIR` construction.
5. Every resolved field carries typed source provenance.
6. The canonical ordered resolved record, not either input record, owns the contract digest.

## D-2 - The physical/representation join key has no defined shape or cardinality

**Story pair that can diverge while obeying AD-4 and AD-11:**

- A Physical IR story derives `MountedOccurrenceId` from the installation declaration and permits
  two mounted occurrences to reference the same canonical semantic subject.
- A representation story uses the existing semantic-subject/projection occurrence identity and
  constructs one Cabinet representation occurrence per semantic subject.

Both use stable typed ids. Cabinet composition still cannot perform a deterministic one-to-one join;
joining by semantic subject duplicates or drops occurrences, while joining by occurrence id fails
because the ids were independently derived.

**Required AD fix:** Add an `InstallationOccurrenceKey` owned by `physical-model` and derived only
from repository id, package-relative source unit, installation declaration id, and canonical semantic
subject. The compiler must pass the same key as occurrence context to both physical compilation and
Cabinet representation construction. For Cabinet v0, require exactly one mounted occurrence and one
resolved representation occurrence per key; missing or duplicate sides fail closed. Remove the phrase
"joins ... by stable semantic and occurrence identity" from AD-11 and name this single join contract.

## D-3 - Containment and mounting are collapsed into one ambiguous parent relation

**Story pair that can diverge while obeying AD-4:**

- A topology story makes a rail the sole parent/container of a mounted occurrence.
- A constraint story makes the enclosure the container and records the rail as a separate mounting
  surface.

Both satisfy "exactly one physical container or mounting surface," but produce incompatible graphs.
The first loses enclosure containment needed for fit; the second cannot use the seed's
`rail -> mountedOccurrence` traversal. FR-20 also permits duct/terminal zones as containers without
the spine defining whether they are legal mount targets.

**Required AD fix:** Replace the generic parent rule in AD-4 with a sealed v0 topology:

- `Enclosure` is contained by one `InstallationSpace`.
- `MountingSurface`, `Duct`, `TerminalGroup`, `RouteChannel`, and `ClearanceZone` are contained by one
  `Enclosure`.
- `Rail` is mounted on one `MountingSurface` and contained by that surface's enclosure.
- `MountedOccurrence` has exactly one `containerId: EnclosureId` and exactly one
  `mountTarget: MountingSurfaceId | RailId | TerminalGroupId`.
- A duct or route channel is never a mount target in v0.

Define allowed edges and cycle checks on these typed relations; do not expose a generic `parentId`.

## D-4 - The physical coordinate frame and orientation transform are unspecified

**Story pair that can diverge while obeying AD-5:**

- A constraint story uses a lower-left origin, positive Y upward, and rotates around footprint
  center.
- A Cabinet composition story uses an upper-left origin, positive Y downward, and rotates around the
  placement point.

Both use integer millimetres and an explicit compiled transform. Their containment, clearance,
terminal locations, and route segments still disagree.

**Required AD fix:** Add a canonical `PhysicalFrame2D v0` to AD-5:

- container interior origin is upper-left;
- positive X is right and positive Y is down;
- admitted orientations are `DEG_0`, `DEG_90`, `DEG_180`, and `DEG_270` clockwise;
- placement is the upper-left point of the post-orientation footprint AABB;
- intrinsic local points rotate around the unrotated footprint center and are translated so the
  rotated AABB minimum equals placement;
- nested transforms apply child-local, then parent-local, then Cabinet drawing transform.

The same functions must be used by constraint evaluation, route-anchor projection, composition, and
proof generation. AD-5 must also resolve the FR-18 conflict explicitly: Physical IR owns physical
extents in millimetres; only composition owns `GraphicBounds` in drawing units.

## D-5 - Mapping representation geometry into a physical footprint has no policy

**Story pair that can diverge while obeying AD-5 and AD-11:**

- A vendor-element story uniformly scales and centers intrinsic `GraphicBounds` inside the physical
  footprint.
- A Cabinet story stretches X and Y independently to fill the footprint.

Neither infers the physical footprint from visual geometry, so both obey the ADs. They produce
different body bounds and terminal-anchor positions, making route intersection proof incompatible.

**Required AD fix:** Add one compiler-owned `CabinetVisualTransform` contract to AD-11. For v0 it
must map intrinsic representation bounds into the post-orientation physical footprint with uniform
aspect-preserving scale and centering, then apply the canonical physical-to-drawing transform.
Degenerate intrinsic bounds fail closed. The same transform instance/id must project body geometry,
anchors, labels, hotspots, bounds, routes, and trace evidence; no consumer may recalculate it.

## D-6 - Package coordinate, source-unit identity, and portable path equality are undefined

**Story pair that can diverge while obeying AD-7 through AD-9:**

- A repository story builds `PackageResourceKey` from the `athena.yaml` primary/dependency package
  coordinate and a repository-relative source path.
- A representation story builds it from the source `package` namespace, declaration version, and a
  namespace-relative path.

Both can claim exact package id/version and owning source-unit identity. They will disagree for
dependencies, multiple source roots, and representation definition versions. Windows also permits
case variants that can collapse on disk but hash as distinct logical paths.

**Required AD fix:** Amend AD-7/AD-9 with these canonical value types and ownership:

- `ResolvedPackageCoordinate` comes only from the existing resolved `athena.yaml` dependency graph.
- `AthenaNamespace` comes only from the compiled source declaration and is not a package coordinate.
- `SourceUnitId` is `(ResolvedPackageCoordinate, normalized package-root-relative path)`.
- `PackageResourceKey` is `(SourceUnitId, resourceDeclarationId)`.
- Element/symbol version is never package version.
- Portable paths use `/`, reject `.`/`..`, normalize Unicode to NFC, and fail admission on
  case-folded or normalization collisions within one package snapshot.

## D-7 - Snapshot identity and generated lock evidence form a digest cycle

**Story pair that can diverge while obeying AD-7, AD-9, and the admission sequence:**

- A snapshot story includes the digest of the existing `athena.lock` in `snapshotId`, then writes a
  new lock containing admitted resource hashes.
- A lock story computes the snapshot first, writes the lock from it, and excludes the lock digest
  from snapshot identity to avoid self-reference.

The current spine requires snapshot/cache identity to contain the lock digest while also declaring
the lock derived from the snapshot. The first implementation changes identity on the next compile;
the second violates the literal cache convention.

**Required AD fix:** Split the identities in AD-7/AD-9:

1. `AdmittedPackageSnapshotDigest` hashes the normalized descriptor, resolved coordinates, sorted
   staged source/resource hashes, and compiler/schema version; it never hashes generated lock bytes.
2. `athena.lock` records that snapshot digest plus resolved coordinates and resource hashes.
3. In locked mode, an existing lock is validated as an input constraint against the newly computed
   snapshot; its byte digest is evidence, not part of snapshot identity.
4. A higher-level compilation fingerprint may include the validated lock schema/state, but cannot
   make the snapshot depend on its generated output.

## D-8 - Two-phase admission does not define the aggregate snapshot boundary

**Story pair that can diverge while obeying AD-8:**

- A package-runtime story freezes one snapshot per representation root/dependency and applies file
  budgets per root.
- A compiler story freezes one repository-wide snapshot and applies budgets and duplicate checks
  across all roots.

Both perform secure two-phase capture and use immutable snapshots. They admit different projects
when roots overlap, one file is reachable through two package roots, or equal declaration identities
exist in separate dependency coordinates.

**Required AD fix:** Define one `ResolvedRepositoryPackageSnapshot` containing an ordered map from
`ResolvedPackageCoordinate` to exactly one `AdmittedPackageSnapshot`. Reject overlapping package
roots and any physical file admitted by more than one coordinate. Apply safety budgets both per
package and across the repository snapshot. Package-local declaration uniqueness is scoped by
coordinate; exported identity collisions are resolved only by the existing dependency resolver.
Compiler, binding, resource loading, lock generation, and proof must consume this same aggregate
snapshot handle.

## D-9 - SVG geometry-reference lookup has two legal but incompatible namespaces

**Story pair that can diverge while obeying AD-10:**

- An SVG frontend story resolves `geometryRef svg.L1` against XML `id`, selecting the first match and
  preserving `<use>` as a reference.
- A representation story resolves it against `data-athena-geometry-ref`, requires uniqueness, and
  expands `<use>` before applying accumulated transforms.

Both use only the two hint forms allowed by AD-10. They can attach proof to different nodes and
compute different transformed geometry.

**Required AD fix:** Add a single `SvgGeometryNodeIndex` contract to AD-10. A referenceable node must
have an XML `id`; an optional `data-athena-geometry-ref`, when present, must equal that id. IDs are
unique across the admitted SVG after safe `<use>` expansion, or package admission fails. Lookup uses
that one key namespace and returns the node plus its accumulated transform in the lowered primitive
tree. Geometry references are proof/attachment hints only: they never supply anchor coordinates,
roles, direction, signal, compatibility, or physical size.

## D-10 - Constraint geometry and boundary semantics are not reproducible

**Story pair that can diverge while obeying AD-6:**

- An evaluator story checks post-orientation axis-aligned footprint rectangles, treats touching
  edges as valid, and inflates each rectangle by its own clearance.
- A composition story checks oriented body bounds, treats edge contact as collision, and sums both
  occurrences' clearances before comparison.

Both can reasonably claim footprint containment, positive-area collision, and required-clearance
checking. They produce different diagnostics and acceptance results.

**Required AD fix:** Define `PhysicalRect` and evaluation semantics in AD-6:

- all v0 footprints and container interiors are closed-axis-aligned rectangles in canonical
  container-local millimetres after orientation;
- footprint collision means intersection width `> 0` and height `> 0`; edge contact alone is not a
  footprint collision;
- clearance is a four-sided non-negative value that inflates its owner's footprint;
- two occurrences violate clearance when either owner's inflated rectangle has positive-area
  intersection with the other's footprint;
- container fit compares the inflated rectangle against the container's interior rectangle;
- compatibility and mounting use exact typed-id set membership, never string matching;
- renderer/representation body bounds are excluded from all physical checks.

## D-11 - Routing ownership is internally contradictory and permits two route IRs

**Story pair that can diverge while obeying different current AD clauses:**

- A Physical IR story follows AD-4 and the Physical Contract Seed by storing final routed-connection
  segments in `PhysicalInstallationIR`.
- A Cabinet story follows AD-12 and waits for bound representation terminal anchors before deriving
  final segments during composition.

The first cannot obtain representation anchors because AD-2 forbids `physical-model` from depending
on representation. The second creates final route facts outside the IR that AD-4 says owns them.
Both are directly supported by the current spine.

**Required AD fix:** Split routing stages explicitly:

- `PhysicalInstallationIR` owns only `RouteChannelTopology`, physical obstacles, and
  `PhysicalRouteIntent` keyed by canonical `EngineeringConnectionId`; it owns no final terminal
  anchor coordinates or composed segments.
- One `CabinetRoutingCompiler` in `drawing-composition` consumes engineering connections,
  `PhysicalInstallationIR`, bound representation anchors, and the shared `CabinetVisualTransform`.
- It emits one typed `CabinetRouteFact` set with ordered channel traversal, transformed endpoint
  bindings, ordered segments, intersection proof, and source provenance.
- Grouped `connect` syntax does not merge engineering connection identity; each connection retains a
  stable id even when route channels are shared.
- Remove final `routedConnection` from the Physical Contract Seed and name `CabinetRouteFact` in the
  capability/dependency map.

## D-12 - Trace shape, coverage, and mutation authority are not closed

**Story pair that can diverge while obeying AD-13 and AD-14:**

- A transport story attaches one `GraphicOccurrenceTrace` only to top-level mounted-device groups;
  child primitives inherit it implicitly, while rails, ducts, clearance zones, and routes have no
  semantic subject and therefore no trace.
- An interaction story duplicates full trace chains on every primitive and invents synthetic
  semantic subject ids for installation-only objects so selection always resolves.

Both can claim every composed "occurrence" has one trace and that selection uses the subject index.
Their payload shapes, trace cardinality, source reveal, and future editing capabilities are
incompatible. AD-14 then permits separate teams to introduce a semantic move intent and a
representation move intent for the same drag because it names no physical-installation action.

**Required AD fix:** Amend AD-13/AD-14 with one normalized contract:

- `GraphicOccurrenceId` is distinct from `GraphicPrimitiveId`.
- `GraphicOccurrenceSubject` is a sealed union of `MountedSemanticSubject`,
  `InstallationDeclarationSubject`, and `EngineeringConnectionSubject`; no synthetic semantic ids.
- Transport carries one versioned trace table keyed by `GraphicOccurrenceId`; each selectable
  primitive carries only that occurrence id. Decorative nonselectable primitives are explicitly
  marked and excluded from the completeness gate.
- Trace entries contain typed references/digests, not embedded package snapshots or source bytes,
  and declaration references are role-labelled and canonically ordered.
- Future `MoveMountedOccurrence` targets the authoritative installation declaration;
  `ChangeRepresentationBinding` targets binding/profile source; engineering-subject replacement
  targets governed engineering source. All use the single M31 `AuthoringTransaction` and
  revision-bound preview path. A generic graphic/representation move intent is forbidden.

## Required Freeze Gate

The spine is ready to freeze only when the twelve fixes above are incorporated into ADs and their
seed contracts, and contract tests are assigned before independent story implementation begins. At
minimum, the architecture must publish canonical shared types for contract resolution, installation
occurrence joins, physical topology, coordinate/visual transforms, package/resource identity,
snapshot/lock identity, SVG node lookup, constraint rectangles, staged route facts, and normalized
trace transport.
