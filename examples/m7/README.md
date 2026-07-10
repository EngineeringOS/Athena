# `examples/m7`

English | [Chinese (Simplified)](README.zh-CN.md)

`examples/m7` documents the published proof corpus for the graphical projection milestone.

## Current proof fixture

M7 intentionally reuses the governed repository fixture under [`../m4/open-repository-proof/`](../m4/open-repository-proof/) instead of inventing a graphical-only repository format.

Use that fixture for the current M7 proof:

- `../m4/open-repository-proof/athena.yaml` - authored repository/package intent
- `../m4/open-repository-proof/athena.lock` - canonical derived lock
- `../m4/open-repository-proof/src/factory-line.athena` - source that drives the active graphical proof

## What M7 adds over the same fixture

- Graphical View beside the source editor
- graph-first split workbench posture
- pannable and zoomable infinite-canvas style viewport
- extension-owned `cabinet` and `wiring` projection mappings from `domain-electrical`

## Main usage guide

- [`docs/usages/m7-proof-usage.md`](../../docs/usages/m7-proof-usage.md)
