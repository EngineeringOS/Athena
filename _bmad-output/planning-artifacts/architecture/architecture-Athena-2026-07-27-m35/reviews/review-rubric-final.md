# M35 Architecture Rubric Final Gate

## Verdict

**CHANGES REQUIRED - FREEZE BLOCKED.** Prior blockers B2 and B3 are resolved. B1 remains partially
open, and review of the updated PRD/addendum exposes two additional high-severity contract gaps.

Deterministic spine lint: **PASS**, zero findings.

## Required Verification

| Item | Result | Judgment |
| --- | --- | --- |
| B1 - dependency graph | **FAIL** | The missing M34 edges and new `repository-model -> package-runtime` edge were added, but the graph now claims to be complete while still omitting direct current and M35-required dependencies. |
| B2 - resource-limit owner and values | **PASS** | AD-8 assigns immutable `PackageAdmissionLimitsV1` to `package-model`, fixes all per-file/package/repository values and work-unit charging, forbids package overrides, and requires one policy across admission, lowering, diagnostics, proof, and tests. |
| B3 - lock modes and atomic materialization | **PASS** | AD-7/AD-9 assign schema/codec to `repository-model`, validator/materializer to `compiler`, define read-only validate and explicit update modes, stable stale/missing/schema diagnostics, same-directory temporary output, atomic replacement, cleanup, revalidation, and fail-closed write behavior. The brownfield ledger explicitly replaces the current direct `Files.writeString` path. |

## Freeze Blockers

### B1 - The dependency diagram's completeness claim is false

**Severity: High**

The spine states that the diagram is the "complete M35 target graph for touched kernel/IDE modules"
at `ARCHITECTURE-SPINE.md:390`. It still omits direct dependencies required by either current Gradle
reality or its own M35 rules, including:

- `package-model -> compiler`: current Gradle already has this edge, and AD-8 requires compiler SVG
  lowering to consume `PackageAdmissionLimitsV1` owned by `package-model`;
- `engineering-model -> compiler` and `representation-model -> compiler`: both remain current direct
  dependencies and are used by the retained compiler path;
- `repository-model -> LSP`: current Gradle has the edge and AD-7 says LSP consumes the one repository
  loader result;
- other existing direct compiler/LSP edges, although both modules are shown as part of the claimed
  complete graph.

The new edges in the Brownfield Contract Ledger are coherent, including
`repository-model -> package-runtime`, `physical-model -> drawing-composition`, and the typed
presentation references. The failure is the diagram's declared scope, not those decisions.

**Required fix:** Either include every direct dependency for every displayed module, or relabel the
diagram as the **M35-relevant dependency subgraph**, state that unrelated existing Gradle edges remain
authoritative and unchanged, and ensure every direct edge required by AD-7..AD-13 appears. Do not
leave a partial graph labelled complete.

### N1 - Terminal occurrence ownership conflicts across the planning inputs

**Severity: High**

The addendum defines a distinct `TerminalOccurrence` under `TerminalGroup` at `addendum.md:200` and
requires terminal ordering at `addendum.md:231`. The spine's closed `PhysicalInstallationIR v0`
vocabulary in AD-4 does not include `TerminalOccurrence` or a terminal-ordering fact. Instead, it
allows the general `MountedOccurrence` to target `TerminalGroup`.

Two stories can therefore implement incompatible models:

- a dedicated terminal occurrence type owned by the group; or
- ordinary mounted occurrences targeting a terminal group, with no specified ordering owner.

This directly affects Physical IR, Cabinet composition, routing-anchor ownership, trace, and the
terminal-strip acceptance proof.

**Required fix:** Choose one contract. The smaller V0 is to make terminals normal
`MountedOccurrence`s targeting `TerminalGroup`, with the group owning a deterministic ordered list of
their `InstallationOccurrenceKey`s. Otherwise add and fully define `TerminalOccurrence` in AD-4,
identity, trace, constraints, routing, and dependency ownership. Reconcile the PRD/addendum/spine.

### N2 - Physical fit validation is incomplete for the contract it requires

**Severity: High**

AD-3 and PRD FR-26 require width, height, and **depth**, a selected orientation, allowed
orientations, and an explicit mount target. AD-6 validates only a 2D inflated rectangle against the
enclosure interior. It does not define:

- whether and how occurrence depth must fit enclosure depth;
- whether selected orientation must be a member of `allowedOrientations`;
- target-specific fit for `MountingSurface`, `Rail`, and `TerminalGroup`;
- for a rail, which footprint span must remain within rail length;
- for a surface or terminal group, whether the inflated footprint must fit target bounds or only the
  enclosure.

As written, an occurrence can satisfy the stated evaluator while extending beyond its mount target
or exceeding enclosure depth. Different Physical Contract and constraint stories can make different
choices and still claim compliance.

**Required fix:** Define one V0 fit predicate per mount-target kind and a depth rule. At minimum,
selected orientation must be allowed; depth must fit the enclosure's usable depth; surface and
terminal-group placements must fit their target bounds; rail placements must fit the rail's authored
length along its local axis and match mounting type. Explicitly state whether clearance participates
in each target-bound test.

## Good-Spine Checklist

| Check | Result | Judgment |
| --- | --- | --- |
| Fixes real divergence points for stories | **Fail** | Dependency scope, terminal occurrence ownership, and target/depth fit still permit incompatible implementations. |
| AD rules are enforceable and prevent stated divergence | **Partial** | B2/B3 are now fully enforceable; AD-4 and AD-6 remain incomplete. |
| Deferred/open decisions are safe | **Partial** | Shared `ClearanceZone` is correctly deferred, but terminal identity/order and mount-target fit are active requirements, not deferred work. |
| Named technology is verified-current | **Pass** | Declared constraints, executable versions, Gradle wrapper, npm locks, Electron, Node, and tree-sitter authority are explicit. |
| Brownfield fit | **Partial** | The ledger accurately marks existing/extend/replace contracts, but the dependency diagram contradicts its claim of completeness. |
| PRD/addendum capability coverage | **Fail** | Terminal-strip composition and Physical Contract fit semantics are not singular across the three inputs. |
| Inherited architecture | **Pass** | M14/M34 authority, binding, SVG hardening, normalization, bounds, visual gates, and superseded SVG metadata rules are explicit. |
| Operational/environmental coverage | **Pass** | Resource limits, lock lifecycle/recovery, offline execution, deterministic cache identity, Windows sequential verification, and fresh Electron E2E gates are fixed. |

## What Now Passes

- One active M35 Cabinet composition path with the M34 Control Drawing retained only for regression.
- Physical/representation occurrence join and deterministic visual transform.
- Fixed deterministic routing algorithm, channel ownership, lane allocation, endpoint stubs, and
  route proof.
- Package/resource SSOT, logical identity, immutable snapshots, fixed admission budgets, and safe SVG
  lowering.
- Lock-independent snapshot identity and governed lock lifecycle.
- Typed trace, source reveal, future mutation boundary, grammar parity, and single-surface E2E proof.
- ECS, Auto Layout AI, extra product surfaces, network registry, and graphic mutation remain safely
  deferred.

## Gate Summary

```text
Critical: 0
High:     3
Medium:   0
Low:      0
Verdict:  CHANGES REQUIRED - FREEZE BLOCKED
```

No architecture-direction change is needed. Freeze after B1, N1, and N2 are reconciled and the
deterministic lint plus final rubric pass both succeed.
