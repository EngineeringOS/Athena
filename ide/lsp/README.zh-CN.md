# `ide/lsp`

[English](README.md) | 简体中文

`ide/lsp` 是 Athena 在 IDE 路径上的语义服务宿主。

## 职责

- IDE 路径上的 repository-session authority
- 嵌入现有 JVM runtime stack 的 stdio Athena LSP server
- 在 `initialize` 阶段于 LSP 边界内完成仓库激活
- 面向 `.athena` authored source 的 `textDocument/didOpen` 语义路径
- 来自 Athena 自有解析、语义分析与校验的 `textDocument/publishDiagnostics`
- 由 Athena 自有文档状态驱动的 `textDocument/completion`、`textDocument/documentSymbol`、`textDocument/definition` 与 `textDocument/references`
- 具备版本感知的 tracked document state，在重复编辑时拒绝陈旧回滚
- 面向 baseline-driven review、commit-preparation 与 package-history state 的增量语义 SCM 请求表面
- 后续故事中的 hover、rename 与更丰富的 workspace navigation

## 边界

Story `2.4` 把这个包从 authoring transport 扩展为第一条 semantic SCM projection bridge。Story `3.3` 再沿用同一条增量桥接路径，把 package evolution 与 release relevance 也投影到现有产品边界。Theia 可以负责进程生命周期与传输，但语义访问必须继续经由这里的 LSP 方法流动，而不是直接调用 `kernel/*`。
