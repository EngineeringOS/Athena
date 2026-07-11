# `examples/m9`

English | [Chinese (Simplified)](README.zh-CN.md)

`examples/m9` documents the published proof corpus for the first engineering-knowledge runtime milestone.

## Current proof fixtures

- `motor-derived-context.athena` - smallest derived-context seed for the first electrical proof slice
- `motor-impact-before.athena` - baseline state for the governed impact and sufficiency proof
- `motor-impact-after.athena` - changed state for the governed impact and sufficiency proof

## What M9 adds over canonical semantic structure

- derived engineering context above canonical `Engineering IR`
- capability facts promoted through one fixed electrical knowledge pack
- deterministic electrical sufficiency diagnostics
- deterministic engineering-impact consequences for governed before/after change
- reuse of existing Problems, semantic inspection, source-mutation review, and semantic SCM surfaces

## Main usage guide

- [`docs/usages/m9-proof-usage.md`](../../docs/usages/m9-proof-usage.md)
