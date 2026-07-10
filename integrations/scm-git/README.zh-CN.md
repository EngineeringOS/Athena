# `:integrations:scm-git`

[English](README.md) | 简体中文

`:integrations:scm-git` 模块是 Athena 第一个面向供应商底层的 SCM 适配器种子。它可以在模块内部使用 Git 语义，但必须把底层状态翻译成 `:kernel:semantic-scm` 暴露的 Athena 自有语义 baseline 合同。

## 职责

- 在 Athena 的 vendor-neutral semantic SCM seam 后面实现第一个 baseline-loading adapter。
- 把 vendor-specific reference resolution 保持在 `kernel/` 之外。
- 在 materialize baseline snapshot 时复用 compiler-owned repository publication authority。

## 边界

该模块不是 diff、review、commit intent 或 history 的语义主权层。它只负责加载底层状态，并把 Athena 自有的语义 snapshot 交还给核心层。
