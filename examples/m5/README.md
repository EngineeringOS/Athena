# `examples/m5`

English | [Chinese (Simplified)](README.zh-CN.md)

`examples/m5` holds governed repository fixtures for the completed repository/package graph proof.

## Current Fixture

- `repository-graph-proof/` - governed repository root with one primary package
- `repository-graph-proof/athena.yaml` - authored repository/package intent contract
- `repository-graph-proof/athena.lock` - canonical derived lock rendered in stable order
- `repository-graph-proof/src/root.athena` - primary package source
## Proof Intent

This fixture proves that Athena can resolve the minimal canonical package graph for one governed repository root while keeping:

- `athena.yaml` as authored intent
- `athena.lock` as compiler-owned derived state
- package identity explicit and inspectable
- repository validation and lock interpretation deterministic
