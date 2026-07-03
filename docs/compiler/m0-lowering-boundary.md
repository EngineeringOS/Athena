# M0 Lowering Boundary

## Purpose

Story `1.3` establishes the first canonical semantic boundary in Athena M0:

- `language/` owns authored text, tokenization, parsing, and the syntax-only AST.
- `compiler/` owns the deterministic lowering pass from AST into canonical `Engineering IR`.
- `ir/` owns the canonical semantic model consumed by later validation, rule execution, and rendering passes.

After Story `2.3`, the lowering pass is still compiler-owned, but the first Electrical/Runtime declaration mapping is contributed by the active domain plugin rather than hard-coded in the core.

The lowering pass is intentionally not a validator. It structures authored meaning into canonical semantic objects while preserving unresolved semantic references for later phases.

## Boundary Rules

- AST is syntax authority only. It is not a second semantic substrate.
- `Engineering IR` is semantic authority for downstream passes.
- Approved domain plugins may translate authored domain declarations into lowering blueprints.
- The compiler may normalize those contributions into canonical IDs, references, and typed property values.
- Lowering must not emit semantic legality judgments such as unresolved-reference errors, type errors, or connection-compatibility errors.
- Layout, geometry, page information, and renderer-specific fields are excluded from the IR.

## First IR Shape

The first canonical IR document is intentionally small and typed:

- `EngineeringIrDocument`
- `EngineeringSystem`
- `EngineeringComponent`
- `EngineeringPort`
- `EngineeringConnection`
- `EngineeringReference`
- `EngineeringProperty`
- `EngineeringPropertyValue`

This vocabulary is sufficient for the M0 Electrical/Runtime wedge without hard-coding renderer concerns or prematurely specializing the permanent semantic core.

## Identity Rules

Stable semantic identity is derived from authored meaning rather than source positions:

- systems: `system:<system-name>`
- components: `component:<component-name>`
- ports: `port:<qualified-port-name>`
- connections: `connection:<from-qualified-name>-><to-qualified-name>`

These IDs are deterministic across repeated compilations of semantically unchanged source and are suitable for downstream pass references.

## Provenance Rules

Canonical semantic objects preserve authored provenance using half-open source ranges:

- `start` points at the first authored character
- `end` points immediately after the last authored character

The current lowering pass carries provenance on:

- the system object
- each component
- each port
- each connection

This keeps canonical semantic objects traceable back to authored source without making source position the semantic authority.

## Unresolved References

Lowering preserves unresolved semantic references structurally:

- `EngineeringReference.authoredPath` always preserves the authored semantic path
- `EngineeringReference.resolvedIdentity` is populated only when the target is already present in the lowered document

This allows Story `1.4` to perform semantic validation against a well-formed canonical IR instead of against syntax trees.

## Story 2.3 Ownership Shift

Story `2.3` keeps these compiler-owned responsibilities in the lowering pass:

- stable identity generation
- duplicate ordinal handling
- authored-reference preservation
- owner and endpoint resolution
- canonical `Engineering IR` assembly

The active domain plugin now owns the first M0 declaration-to-blueprint mapping for:

- `device`
- `port`
- `connect`

That is the current proof that domain mapping can move out of the core without moving semantic authority out of the compiler.
