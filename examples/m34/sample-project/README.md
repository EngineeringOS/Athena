# Athena M34 Sample Project

This sample is the M34 Cabinet representation fixture.

- `src/01-native-cabinet-proof.athena` owns project engineering truth.
- `packages/representation/athena/iec/epic1-native-elements.athena` owns reusable native Symbol/Element visual contracts.
- `packages/representation/athena/vendor/epic2-svg-elements.athena` owns reusable complex Symbol/Element visual contracts and references package-local `./vendor-drive.svg`.
- `packages/representation/athena/generated/generated-drive.athena` proves importer/AI output enters only as canonical Athena source and references package-local `./generated-drive.svg`.
- No XML, QET runtime schema, raw SVG transport, or package manifest authority is used in this fixture.

Story 2.1 adds the separate `graphic svg "..."` path for complex geometry.
Story 2.4 proves generated assets use the same path without adding a runtime importer.
