# Athena Examples

`examples/` holds small architecture-proof corpora for milestone validation.

## `m0/`

- `demo-cabinet.athena` - first valid parse example for Story `1.2`
- `demo-cabinet.engineering-ir.txt` - published canonical `Engineering IR` expectation for Story `1.3`
- `demo-cabinet.expectation.txt` - stable end-to-end expectation contract
- `dual-drive-cabinet.athena` - second valid end-to-end example
- `invalid-semantic-cabinet.athena` - first invalid semantic fixture for Story `1.4`
- `invalid-direction-cabinet.athena` - invalid connection-direction example
- `duplicate-identity-cabinet.athena` - duplicate declaration and ambiguity example
- `quoted-properties-cabinet.athena` - invalid quoted symbolic-property example

Every `m0/*.athena` source is expected to have a matching `m0/*.expectation.txt` sidecar so the conformance suite cannot skip authored examples silently.

## `m2/`

- `demo-cabinet.athena` - shared semantic seed for the first synchronized `cabinet` and `wiring` projection proof
- `demo-cabinet.expectation.txt` - published artifact map for the geometry-backed backend chain
- `demo-cabinet.cabinet.svg` - expected SVG backend output for the `cabinet` view
- `demo-cabinet.wiring.svg` - expected SVG backend output for the `wiring` view
- `operator-proof.athena` - desktop operator-proof seed for the runtime-owned multi-view workflow

`m2/` stays intentionally small. It proves the explicit `Engineering IR -> Layout IR -> Geometry IR -> backend` chain and the runtime-owned desktop operator workflow without turning milestone fixtures into a broad documentation or export package.
