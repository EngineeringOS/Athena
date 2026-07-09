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

## `m3/`

- `electrical-proof.athena` - electrical-only hosted proof for the stable M3 SPI
- `electrical-proof.expectation.txt` - approved-plugin, view, and render contract for the electrical-only path
- `dummy-proof.athena` - dummy-only hosted proof for the synthetic second domain
- `dummy-proof.expectation.txt` - hosted proof contract for the dummy-only no-global-view path
- `dual-domain-proof.athena` - mixed hosted proof showing electrical and dummy domains can coexist
- `dual-domain-proof.expectation.txt` - approved-plugin and render contract for the combined hosted path

`m3/` stays proof-focused. It publishes only the minimum corpus needed to show that external domains can participate through the same stable hosted contracts and to seed the later zero/one/multi-plugin verification matrix.

## `m4/`

- `open-repository-proof/` - first M4 Engineering Repository fixture for the Theia repository-open flow
- `open-repository-proof/src/factory-line.athena` - the single authored source resolved by the temporary M4 repository rule

`m4/` publishes repository-root fixtures instead of loose standalone source files. The current M4 rule intentionally stays narrow: one repository opens only when Athena can resolve exactly one `.athena` source, preferring `src/` when it exists.

## `m5/`

- `repository-graph-proof/` - governed repository/package graph fixture for the completed M5 proof
- `repository-graph-proof/athena.yaml` - authored repository/package intent contract
- `repository-graph-proof/athena.lock` - canonical derived lock contract in stable rendered form
- `repository-graph-proof/src/root.athena` - primary package authored source

`m5/` publishes governed repository-root fixtures instead of standalone source files. The published operator fixture stays intentionally minimal: one primary package, one canonical lock, and one valid repository root. Wider local-first dependency resolution remains proven primarily by focused compiler tests.
