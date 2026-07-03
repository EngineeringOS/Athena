# Athena Examples

`examples/` is reserved for M0 conformance artifacts.

Current seed:

- `m0/demo-cabinet.athena` - first valid parse example for Story `1.2`
- `m0/demo-cabinet.engineering-ir.txt` - published canonical `Engineering IR` expectation for Story `1.3`
- `m0/demo-cabinet.expectation.txt` - stable end-to-end expectation contract
- `m0/dual-drive-cabinet.athena` - second valid end-to-end example
- `m0/invalid-semantic-cabinet.athena` - first invalid semantic fixture for Story `1.4`
- `m0/invalid-direction-cabinet.athena` - invalid connection-direction example
- `m0/duplicate-identity-cabinet.athena` - duplicate declaration and ambiguity example
- `m0/quoted-properties-cabinet.athena` - invalid quoted symbolic-property example

Story `1.7` expands this into the M0 conformance set with stable expectation files, representative published artifacts, and automated end-to-end compiler-path checks.
Every `m0/*.athena` source is expected to have a matching `m0/*.expectation.txt` sidecar so the conformance suite cannot skip authored examples silently.
