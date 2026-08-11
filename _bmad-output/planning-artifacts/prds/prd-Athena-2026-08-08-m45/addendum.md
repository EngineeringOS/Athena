# M45 Addendum

## EPLAN Lessons Applied

EPLAN Symbol Libraries, Parts Databases, Macro Projects, Macro Navigator, Variants, and Placeholder
Objects are useful product lessons. M45 adopts their user-visible separation while replacing desktop
folders with direct project-local packages and deterministic lock resolution.

```text
EPLAN Symbol Library -> Athena Symbol package items
EPLAN Parts Database  -> Athena Part package items
EPLAN Symbol Macro    -> Athena Symbol Macro composition
EPLAN Window Macro    -> Athena Element/Page Macro composition
EPLAN Variant         -> Athena named Macro Variant
EPLAN Placeholder     -> Athena typed Placeholder
```

## Reference Material Rule

SVG geometry may be curated and copied into a package with digest, license, and provenance. Other files
under `reference/` are human/developer reference only. Athena does not parse, convert, package, or execute
them. No external reference folder is a compiler or runtime dependency. All package metadata is authored
directly in Athena-native contracts.

## Package Path Rule

Use this exact shape:

```text
packages/<package-name>/package.yaml
packages/<package-name>/symbols/
packages/<package-name>/elements/
packages/<package-name>/parts/
packages/<package-name>/macros/
```

Do not introduce `packages/registry`, `packages/representation`, or `packages/engineering` as registry
layers. Those are package item kinds, not package roots.

## Semantic/Representation Rule

Port meaning stays in Engineering source and typed package contracts. Port geometry maps a stable key to
an SVG anchor. A composite Element may expose public ports and internal child composition, but graphic
composition cannot silently create engineering relationships.

## Common Package Item Contract

All item kinds share identity, item version, digest, provenance, license, admission state, and lineage.
Kind-specific contracts carry only their distinct payload. This prevents separate Symbol, Element, Part,
Macro, Variant, and Placeholder code paths from duplicating supply-chain metadata.

## Binding Rule

Function representation is explicit:

```text
Function -> FunctionRepresentationBinding -> Element -> Symbol
```

The binding records projection choice, optional Variant, Placeholder values, constraints, package trace,
and provenance. Multiple projections may bind one Function without changing semantic identity.

## Macro and Variant Boundary

Macro is representation reuse. Pattern is engineering-solution generation. Variant changes presentation
or compatible composition only; it cannot change engineering identity. Placeholder performs typed
substitution only; it cannot reason, recommend, or select an engineering solution.

## Provenance Lineage

Package provenance records normalized logical source locator, source digest, license, and admitted Package
Item digest. Usage trace records Function binding and Canonical Scene occurrence. A composed view joins
both through stable identities. A single provenance string is insufficient.

## Golden Page Qualification

The reference screenshot is a visual target, not a claim that every illustrative relationship is a
production-certified circuit. M45 acceptance requires complete definitions and deterministic traces;
standards validation findings remain explicit and inspectable.
