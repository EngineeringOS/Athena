# M0 Conformance Suite

## Purpose

Story `1.7` turns the example set under `examples/m0/` into the explicit end-to-end proof contract for Athena M0.

Each conformance example is:

- authored in the DSL
- executed through the standard `AthenaCompiler` entry path
- checked against a stable expectation file
- treated as architecture-governing proof input, not disposable sample content

## Inventory

Current M0 suite size: `6`

- `demo-cabinet`
- `dual-drive-cabinet`
- `duplicate-identity-cabinet`
- `invalid-direction-cabinet`
- `invalid-semantic-cabinet`
- `quoted-properties-cabinet`

This stays inside the required `5-10` example range.

## Expectation Format

Each example has a sibling `.expectation.txt` file using a minimal `key=value` format.
Every `.athena` source in `examples/m0/` must have a matching `.expectation.txt` sidecar so the suite cannot silently skip authored inputs.

Current fields:

- `status` - `valid` or `semantic-invalid`
- `components` - expected lowered component count
- `ports` - expected lowered port count
- `connections` - expected lowered connection count
- `svg` - `emitted` or `blocked`
- `diagnostics` - comma-separated semantic rule IDs for invalid examples
- `published_ir` - optional exact IR artifact file for representative valid examples
- `published_svg` - optional exact SVG artifact file for representative valid examples

This keeps expectations publishable beside the DSL sources without introducing parser dependencies just for fixture metadata.

## Coverage

The suite deliberately covers:

- valid canonical compilation and SVG emission
- declaration duplication and ambiguity
- invalid connection direction
- unresolved references, missing types, and signal incompatibility
- invalid quoted symbolic properties

Together, the suite covers valid and invalid cases for declarations, ports, references, types, and connections.

## Automation

`kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M0ConformanceExamplesTest.kt` runs every example through `AthenaCompiler.compile()` and asserts:

- inventory is stable and remains within `5-10`
- component, port, and connection counts match expectations
- semantic success or invalidity matches expectations
- diagnostic rule IDs match expectations
- rendering is emitted or blocked as expected
- published IR and SVG artifacts still match when present

## Published Artifacts

Representative valid exact artifacts currently remain published for:

- `demo-cabinet.engineering-ir.txt`
- `demo-cabinet.svg`

Other examples still contribute stable expected outcomes through counts, diagnostics, and render behavior.
