# `examples/m9`

[English](README.md) | 简体中文

`examples/m9` 记录第一个 engineering-knowledge runtime milestone 的已发布 proof corpus。

## 当前 proof fixture

- `motor-derived-context.athena` - 第一条 electrical proof slice 的最小 derived-context seed
- `motor-impact-before.athena` - governed impact 与 sufficiency proof 的 baseline state
- `motor-impact-after.athena` - governed impact 与 sufficiency proof 的 changed state

## M9 相比 canonical semantic structure 新增了什么

- 位于 canonical `Engineering IR` 之上的 derived engineering context
- 通过固定 electrical knowledge pack 提升出的 capability facts
- deterministic electrical sufficiency diagnostics
- 面向 governed before/after change 的 deterministic engineering-impact consequence
- 复用现有 Problems、semantic inspection、source-mutation review 与 semantic SCM surface

## 主要使用说明

- [`docs/usages/m9-proof-usage.md`](../../docs/usages/m9-proof-usage.md)
