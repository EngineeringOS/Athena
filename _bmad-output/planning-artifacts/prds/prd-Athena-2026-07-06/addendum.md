# Athena M2 Addendum

## Purpose

This addendum captures context that matters to M2 planning but should not overload the main PRD narrative.

## Milestone Renumbering Note

The older draft in `draft/M2/001-draft.md` described a milestone ladder where "M2" meant the first end-to-end compiler pipeline and "M3" meant the first extension proof.

That draft is now behind the repository state.

Current completed reality is:

- M0 = compiler proof
- M1 = runtime, graph, command, viewer, and plugin-hosting proof

Because of that, the next PRD should use `M2` for the next real manifesto gap rather than skipping directly to `M3`.

## Why Layout and Geometry Are the Right Next Step

Athena now has:

- canonical semantic truth
- runtime ownership
- graph projection
- command mutation
- history and diff
- a desktop viewer proof

What it still does not have is the manifesto's explicit downstream split between:

- `Engineering IR`
- `Layout IR`
- `Geometry IR`

That makes layout and geometry the next high-leverage milestone because they protect the semantic architecture before the Studio surface gets richer.

## Planning Direction Chosen

The M2 PRD therefore assumes:

- desktop remains the primary proof surface
- the first goal is synchronized multi-view projection, not a full editor
- layout and geometry stay downstream of semantics
- at least one current backend or viewer path must consume geometry as geometry

## Candidate Proof Pairs for the First Two Views

Options to decide during architecture and epic breakdown:

- cabinet + wiring
- cabinet + functional
- wiring + functional

Selection criteria should be:

- strongest demonstration of "same semantics, different view intent"
- manageable implementation breadth for one milestone
- direct usefulness for later Studio growth

## Candidate Structural Module Direction

This PRD intentionally leaves module naming open, but likely directions include:

- one new kernel module for layout and one for geometry
- or one projection-focused kernel group that still exposes explicit `Layout IR` and `Geometry IR` boundaries

The architecture phase should decide the exact split.
