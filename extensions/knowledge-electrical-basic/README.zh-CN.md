# `extensions/knowledge-electrical-basic`

[English](README.md) | 简体中文

`extensions/knowledge-electrical-basic` 是 Athena 当前第一份受治理的 M9 knowledge pack。它是目录形式的评审工件，不是 Gradle 代码模块。这个 pack 通过现有 compiler knowledge-package 接缝贡献固定的 electrical capability promotion 语义，以及第一条固定 sufficiency rule slice。

## 职责

- 在 `athena-knowledge.properties` 中发布第一份固定 knowledge-pack manifest。
- 贡献第一批受治理的 capability promotion 语义 payload。
- 贡献第一条受治理的 constraint-slice payload。
- 保持 M9 proof 足够窄：只覆盖 electrical，不引入 vendor catalog、standards platform 或终端用户 rule authoring。

## 当前范围

- 将 `FULL_LOAD_CURRENT` 提升为：
  - `REQUIRED_PROTECTION_CURRENT`
  - `REQUIRED_CABLE_CURRENT`
  - `REQUIRED_RELAY_SIZING_CURRENT`
- 使用 `payload/capability-semantics.properties` 中固定的 multiplier 和 rounding 语义。
- 使用 `payload/constraint-slice.properties` 评估固定的充分性检查：
  - protection sufficiency
  - cable sufficiency
  - relay sufficiency

## 边界

这个 pack 本身不直接执行代码，不拥有 compiler 或 runtime 的执行模型，也不提供 renderer 或 IDE 行为。Athena 的 kernel/compiler 代码负责加载该 pack、解释 payload，并输出 typed capability facts、constraint-evaluation results 与 sufficiency diagnostics。
