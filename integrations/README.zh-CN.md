# `integrations`

[English](README.md) | 简体中文

`integrations` 分组承载 Athena 的供应商底层适配器。这些模块可以接入 Git 等外部基座能力，但不能把供应商 API 变成 Athena 的语义主权。

## 模块

- `:integrations:scm-git` -> [`scm-git/`](scm-git/README.zh-CN.md)
- `node: graph-glsp` -> [`graph-glsp/`](graph-glsp/README.zh-CN.md)

## 边界

集成模块可以解析供应商引用、调用供应商进程，或把底层状态翻译成 Athena 自己的合同；但它们不能重新定义已经由 Athena kernel 与 IDE 边界拥有的 semantic change、review、commit intent、projection authority 或 history 语义。

图形框架依赖只能落在 `integrations/graph-*` 下。`ide/theia-*` 可以消费这些适配器，但不能成为 graph-framework protocol 或 rendering vocabulary 的拥有者。
