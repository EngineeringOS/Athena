# `examples/m8`

`examples/m8` documents the published proof corpus for the unified semantic mutation milestone.

## Current proof fixture

M8 intentionally reuses the governed repository fixture under [`../m4/open-repository-proof/`](../m4/open-repository-proof/) instead of inventing a mutation-only repository shape.

Use that fixture for the current M8 proof:

- `../m4/open-repository-proof/athena.yaml` - authored repository and package intent
- `../m4/open-repository-proof/athena.lock` - canonical derived lock for the governed repository
- `../m4/open-repository-proof/src/factory-line.athena` - source file that drives the semantic and graph mutation proof

## What M8 adds over the same fixture

- source-originated mutation evaluation normalized into the same runtime mutation-result model used by graph-originated changes
- one real graph semantic mutation path through `connect-ports`
- one real graph projection mutation path through governed cabinet placement
- one shared semantic review model for accepted source and graph mutations
- one canonical reveal path across source, graph, and semantic SCM surfaces

## Main usage guide

- [`docs/usages/m8-proof-usage.md`](../../docs/usages/m8-proof-usage.md)
