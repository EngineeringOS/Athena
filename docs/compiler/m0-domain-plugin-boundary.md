# M0 Real Domain Plugin Boundary

## Purpose

Story `2.3` turns the first Electrical/Runtime plugin from a manifest-only proof object into a real semantic contributor.

The goal is to prove that:

- the compiler still owns pass order and semantic authority
- the first domain wedge is executable through a real plugin
- Electrical/Runtime semantics no longer live as hard-coded M0 domain meaning in the core

## What The Core Still Owns

The core still owns:

- plugin discovery and approval
- pass ordering
- canonical `Engineering IR`
- stable identity generation
- provenance propagation
- authored-reference preservation
- duplicate and reference validation
- semantic continuation policy

The plugin does not get its own pass pipeline, semantic model, or renderer authority.

## What The Electrical/Runtime Plugin Owns

The real domain plugin now owns:

- mapping M0 `device`, `port`, and `connect` declarations into compiler-owned lowering blueprints
- Electrical/Runtime device type semantics such as `PLC` and `Motor`
- Electrical/Runtime direction semantics such as `in` and `out`
- connection direction legality
- signal compatibility checks
- symbolic-property validity for domain-owned properties such as `type` and `direction`

Those semantics execute only through the core-owned `DOMAIN_SEMANTICS` extension point.

## Lowering Boundary

The compiler still runs the declared `LOWER` pass.

Inside that pass:

- active approved domain plugins produce lowering blueprints
- the compiler assigns stable identities
- the compiler resolves authored references where possible
- the compiler assembles canonical `Engineering IR`

This keeps identity and canonical assembly core-owned while moving domain mapping logic out of hard-coded core implementation.

## Validation Boundary

The compiler still runs one declared `VALIDATE` pass.

Inside that pass:

- `semantics-core` performs generic duplicate and reference validation
- active approved domain plugins add domain-owned diagnostics
- the compiler merges diagnostics and computes continuation policy

This keeps semantic policy centralized while making domain rule ownership explicit.

## Plugin-Absent Behavior

If no approved domain plugin is active for authored domain declarations:

- parsing still succeeds
- pass reporting still succeeds
- the lowered document remains structurally well-formed
- semantic validation emits a stable domain-unavailable diagnostic
- downstream rendering is blocked by normal continuation policy

That is the architectural proof that semantics are attached, not hard-wired.

## Non-Goals

Story `2.3` still does not include:

- plugin-defined pass scheduling
- renderer plugins taking semantic ownership
- remote plugin distribution
- hot loading
- standards, importer, or exporter activation
- rule-engine replacement
- new M0 syntax or IR redesign

Those remain later work.
