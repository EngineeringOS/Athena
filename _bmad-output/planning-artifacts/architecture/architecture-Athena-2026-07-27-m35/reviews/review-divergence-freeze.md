# M35 Architecture Freeze Divergence Recheck

## Verdict

**FAIL - FREEZE BLOCKED.** Authored channel rectangles, individual connection aliases, exact depth
fit, target-specific occurrence fit/mount compatibility, and orientation membership are closed.
Rail polarity is deterministic but introduces a reflection. The source grammar remains incomplete at
its declaration-scope and migration boundary. Five exact freeze blockers remain.

## Requested Recheck

| Contract | Result | Evidence |
| --- | --- | --- |
| Fixed rail polarity | **DEFINED, BUT BLOCKED** | AD-5 fixes horizontal and vertical bases, but the vertical basis swaps enclosure axes and has determinant `-1`; AD-11 therefore mirrors representation geometry when applying the target-to-enclosure transform. |
| Authored channel rectangles | **CLOSED** | AD-4 and the normative grammar require duct-interior-local channel `at` and `size`, orientation, lanes, and margin; implicit derivation remains forbidden. |
| Individual connection aliases | **CLOSED** | AD-12 defines source-unit-unique aliases and `EngineeringConnectionId = (SourceUnitId, connectionAlias)`; grouped and ungrouped normative forms expose aliases and group ids remain non-semantic. |
| Exact depth fit | **CLOSED** | AD-6 fixes usable enclosure depth, forbids in-plane depth rotation/Z offsets, and requires occurrence depth `<=` enclosure depth. |
| Target-specific fit/mount compatibility | **CLOSED FOR OCCURRENCES** | AD-6 defines surface/terminal bounds fit, rail along-axis fit with zero normal offset, and exact accepted mounting-type checks. Parent physical-object containment remains open under F-2. |
| Orientation membership | **CLOSED** | AD-6 explicitly requires selected orientation membership in `allowedOrientations` before composition. |
| Source grammar consistency | **NOT CLOSED** | Normative forms exist, but declaration scope, admitted installation kind, and migration/retention of existing unaliased `connect` forms are not specified. The accepted proof is not a complete compilable source unit. |

## Exact Freeze Blockers

### F-1 - Vertical rail target frames mirror mounted representations

AD-5 defines vertical rail local `+X = enclosure +Y` and local `+Y = enclosure +X`. This transform
has determinant `-1`, so it is a reflection rather than a 90-degree rigid rotation. AD-11 applies the
target-to-enclosure transform to body, anchors, labels, and hotspots; vertical-rail occurrences can
therefore be mirrored even when their authored orientation does not request mirroring.

**Required closure:** Use a right-handed rigid basis, for example local `+X = enclosure +Y` and local
`+Y = enclosure -X`, or explicitly separate rail placement coordinates from visual orientation and
forbid target-frame reflection. Add vertical-rail body/anchor golden tests.

### F-2 - Parent physical objects have no containment validation

The evaluator validates mounted occurrences against enclosure and target bounds, and validates a
channel against its duct. It does not require MountingSurface, Duct, or TerminalGroup bounds to fit
their Enclosure, nor Rail placement/length to fit its MountingSurface. Independent stories may accept
or reject the same physical topology, and an accepted duct or rail can lie outside the Cabinet.

**Required closure:** Add fail-closed containment rules for MountingSurface, Duct, and TerminalGroup
inside Enclosure and for Rail's complete oriented interval inside MountingSurface, with stable
source-spanned diagnostics.

### F-3 - Lane centreline coordinates are not deterministic

RouteChannel now has rectangle, orientation, lane count, and margin. AD-12 fixes lane allocation
order but never defines the lane centreline positions within the cross-axis span. Implementations can
pack from the margin, divide into equal cells, or use arbitrary pitch and all still obey the current
ordering rule. Non-divisible integer-millimetre spans also have no rounding/rational policy.

**Required closure:** Publish one v0 lane-position formula, minimum usable span rule, and exact
numeric/rounding representation. Golden tests must cover odd/non-divisible spans and full capacity.

### F-4 - "Shared passable boundary" has no topology contract

AD-12 routes adjacent channels through a shared passable-boundary centre, but neither the IR nor
source grammar defines passability or a channel-to-channel adjacency edge. Since channels live inside
wall-inset duct interiors, channels in separate ducts normally do not share a geometric boundary.
One compiler may infer touching rectangles; another may require overlap or permit a gap.

**Required closure:** Define typed `RouteChannelAdjacency` with authored or uniquely derived entry/exit
geometry, or restrict v0 adjacency to one explicit geometric condition. Define whether cross-duct
transitions are supported and fail every other case deterministically.

### F-5 - The normative source surface does not close declaration scope or connect migration

Current Athena `connect` declarations are system-scoped. The addendum's accepted proof places
`connect` and `installation` without a containing `system`, does not state whether `installation` and
`route` are system members or top-level declarations, and leaves `<kind>` unconstrained despite M35
supporting only Cabinet. It also adds aliased `connect` forms without deciding whether existing
unaliased grouped/ungrouped forms remain legal for unrouted connections or are migrated and deleted.

**Required closure:** Publish one complete compilable source-unit example and state:

- exact declaration scope for `installation` and `route`;
- the closed M35 installation kind set (`cabinet` if it is the only kind);
- whether aliases are mandatory for all connections or only routed connections;
- whether old unaliased forms remain, or the required fixture/example migration and deletion gate.

ANTLR4, AST, Tree-sitter, formatter, LSP, and parser-parity corpus must consume that same decision.

## Freeze Gate

M35 may freeze after F-1 through F-5 are closed. The previously requested channel, alias, depth,
occurrence-fit, mount-compatibility, and orientation contracts do not need reopening. No reviewed
planning artifact was modified; only this requested review report was added.
