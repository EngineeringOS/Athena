# `examples/m12`

English | [Chinese (Simplified)](README.zh-CN.md)

`examples/m12` publishes the renderer-hardening proof corpus for Athena.

## Current Fixture

- `renderer-benchmark-proof/` - governed repository fixture for the M12 electrical renderer benchmark
- `renderer-benchmark-proof/athena.yaml` - authored repository and primary-package intent
- `renderer-benchmark-proof/athena.lock` - canonical derived lock state for the same repository
- `renderer-benchmark-proof/src/expansion-line.athena` - larger electrical source used to validate connection readability, viewport behavior, and cross-reference reveal beyond the M11 baseline

## What It Proves

- Athena can carry a materially larger electrical scene than the M11 baseline without changing semantic authority.
- The graph workbench can still fit, pan, zoom, and reveal canonical electrical subjects on the larger fixture.
- Repeated-reference navigation and related-subject reveal remain downstream of canonical semantic ids.
