# M35 Final Divergence Gate

## Verdict

**FAIL - FREEZE BLOCKED.** N-1, N-3, N-4, N-5, N-6, and N-9 are closed. N-2, N-7, N-8,
and N-10 remain incomplete because their published source and evaluator contracts cannot construct or
validate all facts required by the spine. Six exact blockers remain.

## N-1 Through N-10

| Gate | Result | Evidence |
| --- | --- | --- |
| N-1 closed physical contract | **CLOSED** | AD-3 and the addendum publish required fields, scalar/set merge granularity, ordering, units, validation, provenance, and digest ownership for `PhysicalInstallationContractV0`. |
| N-2 mount frames | **NOT CLOSED** | Surface and terminal-group frames are fixed, but rail local X and normal polarity are not. The normative rail syntax declares only `horizontal|vertical`; it declares neither direction nor normal. Independent implementations can mirror vertical/horizontal rail-mounted occurrences. |
| N-3 visual matrix | **CLOSED** | AD-11 publishes the seven-step matrix order and requires 0/90/180/270 body-and-anchor golden tests. |
| N-4 lexical resources | **CLOSED** | AD-9 fixes source-unit lexical ids, same-file references, source-directory-relative paths, duplicate behavior, and no raw dependency resource access. PRD FR-5 through FR-8 agree. |
| N-5 validated lock digest | **CLOSED** | AD-7/AD-9 define `RepositoryLockV2`, `ValidatedLockStateDigest`, canonical facts, unlocked sentinel, validate/update modes, and exclusion from resource/snapshot identity. PRD FR-6/FR-10 agree. |
| N-6 SVG `use` cardinality | **CLOSED** | AD-10 indexes ids before expansion and requires a referenced id to materialize exactly once; zero and multiple instances fail closed. |
| N-7 deterministic routing | **NOT CLOSED** | AD-12 fixes ordering, lanes, transitions, bends, stubs, and failure behavior, but required channel geometry cannot be authored or derived, and route declarations cannot name an existing individual engineering connection. |
| N-8 duct/channel relation | **NOT CLOSED** | AD-4 makes every channel belong to one duct, requires its rectangle to fit the wall-inset interior, and forbids implicit derivation. The normative `channel` form has no `at` or `size`, so no channel rectangle exists to validate or route through. |
| N-9 no `ClearanceZone` v0 | **CLOSED** | `ClearanceZone` is absent from active topology, IR, grammar, and PRD vocabulary; shared keep-out zones are explicitly deferred. |
| N-10 normative grammar | **NOT CLOSED** | A normative surface and accepted example now exist, but two required references are not representable: rail frame polarity and a stable source name for an individual engineering connection. |

## Exact Blockers

### B-1 - Rail frame polarity is undefined

AD-5 says rail local X follows its declared direction and local Y follows its declared normal. The
normative grammar declares only rail orientation. For a horizontal rail, X may run left-to-right or
right-to-left; for a vertical rail, it may run top-to-bottom or bottom-to-top, with two possible
normals in either case. This changes placement, collision, anchors, and routing.

**Required closure:** Define fixed v0 polarity, for example horizontal X = enclosure +X and normal =
+Y; vertical X = enclosure +Y and normal = +X, or add explicit direction/normal syntax and type rules.

### B-2 - `RouteChannel` requires geometry that source cannot provide

AD-4 requires a channel rectangle and forbids deriving it from duct geometry. The normative form is:

```text
channel <id> in <duct-id> orientation <horizontal|vertical> lanes <positive-int> margin <length>
```

It contains no position or extent. The compiler therefore cannot prove duct containment, adjacency,
lane coordinates, or deterministic transition points without violating the no-inference rule.

**Required closure:** Add target-local `at` and `size` to channel syntax and AST, or define one
canonical derivation from the wall-inset duct interior and remove the no-inference rule.

### B-3 - A route cannot identify an authored individual connection

The normative example uses `route MainSupplyConnection`, but current Athena connections are unnamed:
`connect A.p -> B.q`, and grouped connections only name the authoring group. Current lowering derives
each connection id from endpoints plus duplicate ordinal (`connection:A.p->B.q[#n]`). The group name
does not identify an individual connection, and no source alias maps `MainSupplyConnection` to that
derived id.

**Required closure:** Define a typed, stable, authorable connection reference. Either add an
individual connection alias/id syntax, or make `route` reference canonical endpoint identity plus an
explicit duplicate discriminator. Group ids must remain non-semantic grouping only.

### B-4 - Required depth has no fit rule

The contract and enclosure syntax require depth, but AD-6 evaluates only 2D `PhysicalRect` fit and
four-sided clearance. A component deeper than its enclosure can either pass or fail depending on the
story implementation.

**Required closure:** Define usable enclosure depth and a deterministic depth-fit check, including
orientation effects if depth can rotate, or remove depth from the required M35 contract and grammar.

### B-5 - Legal mount targets do not share a complete fit/mounting contract

Mounted occurrences may target MountingSurface, Rail, or TerminalGroup. Only Rail declares a
mounting type; Surface and TerminalGroup expose no accepted mounting-type set. AD-6 also checks the
inflated footprint against the enclosure, not against mount-target extent. A rail occurrence may sit
beyond rail length while remaining inside the enclosure.

**Required closure:** Publish per-target compatibility and fit rules: accepted mounting types for
each target kind, surface/terminal-group bounds checks, rail along-axis interval checks, and the
allowed normal-axis placement rule.

### B-6 - Actual orientation membership is not a required evaluation

The source declares both `orientation` and `allowed-orientations`, but no AD explicitly requires the
selected orientation to be a member of the resolved allowed set. Implementations can accept or reject
the same occurrence while satisfying the current rectangle rules.

**Required closure:** Add `orientation in allowedOrientations` as a fail-closed typed constraint with
a stable diagnostic before Physical IR construction/composition.

## Final Gate

M35 may freeze only after B-1 through B-6 are resolved consistently in the spine, PRD/addendum, and
normative syntax contract. No reviewed planning artifact was modified by this gate; only this review
report was added.
