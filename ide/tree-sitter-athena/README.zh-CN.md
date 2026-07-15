# `:ide/tree-sitter-athena`

[English](README.md) | 简体中文

Athena 的 Tree-sitter 语法包，**仅用于语法 UX**（AD-107）。

## 边界

- 负责：增量语法树、高亮查询、残缺源码下的编辑器可用结构。
- 不负责：语义诊断、`Engineering IR`、包语义，或任何编译器真值。
- 语义诊断仍走编译器/LSP 路径（`ide/lsp` → `AthenaCompiler`）。

## 验证

```bash
yarn --cwd ide/tree-sitter-athena test
```