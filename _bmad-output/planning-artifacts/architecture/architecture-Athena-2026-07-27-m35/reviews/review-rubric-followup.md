# M35 Architecture Spine Rubric Follow-Up

## Gate Verdict

**CHANGES REQUIRED - FREEZE BLOCKED.** The update resolves the architecture direction and most of
the original rubric findings. Three high-severity divergence points remain: the dependency graph is
not yet compatible with the retained brownfield paths, aggregate resource-limit authority is not
pinned, and lock materialization has no complete operational contract.

The deterministic spine lint passes with zero findings.

## Evidence Reviewed

- Updated `ARCHITECTURE-SPINE.md`.
- Prior `reviews/review-rubric.md`.
- BMAD good-spine checklist from the architecture reviewer gate.
- Current Gradle project dependencies for the affected kernel and IDE modules.
- Current `AthenaRepositoryLockMaterializer` write behavior.
- Current `AthenaSvgGraphicBodySupport` safety-limit ownership.

## Prior Finding Verification

| Prior finding | Status | Follow-up judgment |
| --- | --- | --- |
| H1 - active Layout/Geometry migration undefined | **Resolved** | AD-18 fixes one M35 Cabinet path, preserves the M34 Control Drawing path, and requires old Cabinet producer deletion after zero-caller proof. |
| H2 - dependency direction contradicted Gradle | **Partially resolved - blocker** | The compiler/presentation arrow is corrected and routing is explicitly excluded, but the diagram omits retained dependencies and one required new dependency. See B1. |
| H3 - physical contract precedence undefined | **Resolved** | AD-3 defines field-level project-over-trait precedence, same-level ambiguity failure, required-field failure, provenance, and digest ownership. |
| H4 - physical geometry semantics undefined | **Mostly resolved** | AD-5 and AD-6 fix frame, orientation, rotation, rectangle contact, clearance inflation, and shared transforms. Authored `ClearanceZone` behavior remains underspecified; see M1. |
| H5 - routing semantics undefined | **Mostly resolved** | AD-12 fixes owners, route vocabulary, orthogonal segments, channel containment, endpoint binding, and blocking diagnostics. Endpoint-stub construction remains underspecified; see M2. |
| H6 - inherited SVG safety/bounds/visual gates dropped | **Partially resolved - blocker** | M34 AD-12..AD-15 are now inherited and AD-10/AD-16 preserve their boundaries. New package/repository aggregate budgets still have no fixed owner or values; see B2. |
| M1 - planning questions disappeared | **Resolved** | `Resolved Planning Questions` fixes the resource syntax and mandatory visual checklist. |
| M2 - trace ownership creates orchestration dependency | **Resolved** | AD-13 uses typed ids, spans, and digests only; it forbids source bytes and snapshot objects. Presentation does not import compiler/runtime authorities. |
| M3 - descriptor/lock extension ownership incomplete | **Open - blocker** | Snapshot digest cycles are fixed, but materialization mode, atomic write, stale-lock behavior, and failure recovery remain open. See B3. |
| L1 - toolchain ranges are not pins | **Open - low** | The stack says versions are ratified, but does not explicitly make checked-in package-manager lockfiles the CI/E2E executable-graph authority. |

## Freeze Blockers

### B1 - Dependency direction is still not an implementable brownfield contract

**Severity: High**

The `Dependency Direction` diagram is presented as the module graph, but it omits dependencies that
must remain while AD-18 keeps the M34 Control Drawing path alive:

- current `drawing-composition` depends on `engineering-model`, `projection-model`, and
  `representation-model`; the diagram omits the first two;
- current `presentation-model` depends on `document-projection-model`, `layout-model`,
  `routing-model`, and `representation-model`; the diagram omits the first three;
- AD-7..AD-9 and the structural seed make `repository-model` the owner of snapshot ids while
  `package-runtime` constructs the admitted aggregate, but the diagram has no
  `repository-model -> package-runtime` edge.

This leaves two incompatible readings: either stories must remove dependencies still required by
the retained M34 path, or the diagram describes only M35 additions without saying so.

**Required disposition:** Mark the diagram explicitly as a complete target graph or an M35 delta.
For a complete graph, include retained brownfield edges and the repository/package-runtime edge. For
a delta, list all new M35 edges and state that existing M34 dependencies remain until their owning
projection is retired. Every new edge must have one owning AD.

### B2 - Aggregate resource safety policy is named but not fixed

**Severity: High**

AD-8 requires per-file, per-package, and repository caps for bytes, DOM depth, elements, reference
expansion, path segments, primitives, and work units. The spine does not assign the canonical limit
policy owner, fix the M35 values, or state whether descriptors/packages may override them.

Brownfield code currently fixes only part of the policy in compiler internals:

```text
MAX_SVG_BYTES = 262144
MAX_ELEMENTS = 512
MAX_DEPTH = 32
MAX_EMITTED_PRIMITIVES = 256
```

M35 adds package count, aggregate bytes, reference expansion, path-segment, and work-unit limits.
Independent resource-admission stories can choose different values and still satisfy AD-8.

**Required disposition:** Name one lower-level limit-policy owner, pin all M35 default limits, forbid
package-authored overrides in M35, and require the same policy instance for admission, diagnostics,
proof, and tests. Limits may later become configuration only through an explicit governed boundary.

### B3 - Lock materialization lacks an operational contract

**Severity: High**

AD-7 and AD-9 correctly remove the digest cycle and distinguish evidence from authority. They do not
state:

- which module owns canonical lock schema values versus lock materialization;
- locked/validate mode versus update/materialize mode behavior;
- whether a stale lock blocks compilation or is silently rewritten;
- atomic replacement and cleanup behavior after write failure;
- the diagnostic emitted for stale, missing, or schema-incompatible lock state.

Current brownfield `AthenaRepositoryLockMaterializer.materialize` calls `Files.writeString` directly
at `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryLockMaterializer.kt:40`,
so the claimed reproducibility contract is not yet ratified by existing behavior.

**Required disposition:** Pin `repository-model` as schema/value owner and `compiler` as the sole lock
materializer. Locked mode must fail closed on stale/missing/incompatible state; explicit update mode
must write canonical bytes through same-directory temporary output plus atomic replacement, with no
partial lock accepted after failure. Stable diagnostics and tests must cover each state.

## Remaining Non-Blocking Findings

### M1 - `ClearanceZone` has topology but no evaluation semantics

AD-4 includes `ClearanceZone`, while AD-6 defines only per-occurrence inflated clearance. The spine
does not say whether a zone is forbidden occupancy, which subjects it constrains, whether contact is
allowed, or whether it may overlap rails, ducts, route channels, or other zones.

**Disposition:** Define its V0 predicate or remove it from V0 IR and defer it. Do not leave a typed IR
node with story-defined semantics.

### M2 - Endpoint-stub routing remains ambiguous

AD-12 exempts "explicit endpoint stubs" from channel containment but does not define their endpoint,
maximum extent, orthogonal construction, or obstacle-intersection rule. Route compilation and proof
can therefore disagree while both claim compliance.

**Disposition:** Fix a deterministic V0 stub rule or require authored stub facts. General route
optimization may remain deferred.

### L1 - Executable toolchain pin authority is implicit

The stack is repository-compatible, but `Node.js >=22` remains a range and the spine does not state
that checked-in Gradle/npm lock state is the CI/E2E reproducibility authority.

## Dependency And Operational Coverage

| Dimension | Result | Judgment |
| --- | --- | --- |
| Authority/dependency direction | **Fail** | Core ownership is strong, but the module graph is neither a complete target nor a labelled M35 delta. |
| Brownfield migration | **Pass** | AD-18 prevents parallel Cabinet pipelines and protects the retained M34 projection. |
| Package/resource security | **Partial** | Fail-closed capture, no-follow containment, no external I/O, immutable snapshots, and normalized SVG lowering are covered; aggregate limit policy is not. |
| Reproducibility/cache identity | **Pass** | AD-9 removes the snapshot/lock digest cycle and fixes canonical identity inputs. |
| Lock lifecycle/recovery | **Fail** | Mode, stale-state, atomic write, and failure recovery are not fixed. |
| Runtime/environment | **Pass** | Offline local execution, no network, deterministic outputs, Windows sequential Gradle verification, and fresh Electron E2E evidence are explicit. No infrastructure/provider decision is needed for this local milestone. |
| Diagnostics/observability | **Pass** | Stable source-spanned diagnostics and structured package/physical/routing/trace proof are required. |
| Product acceptance | **Pass** | One Cabinet surface, three viewport gates, nonblank canvas proof, and structural/professional visual checks are enforceable. |

## Good-Spine Checklist

| Check | Result |
| --- | --- |
| Fixes the real divergence points for M35 stories | **Fail** - dependency, resource-limit, and lock-operation choices remain divergent. |
| Every AD is enforceable and prevents its stated divergence | **Partial** - AD-7..AD-9 need operational completion; AD-4/AD-12 have small semantic gaps. |
| Deferred contains unsafe open decisions | **Partial** - solver/ECS/view expansion are correctly deferred, but clearance-zone and endpoint-stub semantics are neither fixed nor deferred. |
| Named technology is verified-current | **Pass** - repository versions remain ratified; executable pin authority is only a low clarification. |
| Ratifies the brownfield codebase | **Partial** - Cabinet migration is fixed, but dependency and lock-write reality still diverge. |
| Covers PRD capabilities | **Pass** - all capability areas are represented with enforceable gates apart from the operational gaps above. |
| Preserves inherited architecture | **Pass** - relevant M14/M34 authority, SVG, bounds, binding, and visual gates are explicit. |
| Covers the owned operational/environmental envelope | **Partial** - local/offline/E2E behavior is complete; resource limits and lock recovery are not. |

## Gate Summary

```text
Critical: 0
High:     3
Medium:   2
Low:      1
Verdict:  CHANGES REQUIRED - FREEZE BLOCKED
```

No change to the M35 direction is required. Freeze after the three blockers are incorporated and a
final deterministic lint plus dependency-focused rubric pass succeeds.
