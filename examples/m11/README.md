# `examples/m11`

English | [Chinese (Simplified)](README.zh-CN.md)

`examples/m11` publishes the first denser electrical ECAD proof repository for Athena.

## Current fixture

- `dense-electrical-proof/` - governed repository fixture for the serious M11 electrical workbench proof
- `dense-electrical-proof/athena.yaml` - authored repository and primary-package intent
- `dense-electrical-proof/athena.lock` - canonical derived lock state for the same repository
- `dense-electrical-proof/src/assembly-line.athena` - dense electrical source with more than 10 components, more than 20 connections, sheet-aware documentation output, and repeated-reference pressure

## What It Proves

- One canonical source can feed `cabinet`, `wiring`, `schematic`, and `documentation` electrical families.
- Documentation projection can publish repeated references and cross-reference metadata without creating new semantic identities.
- The current runtime, LSP, graph adapter, and workbench path can carry denser electrical output from the same governed repository shape introduced earlier.
