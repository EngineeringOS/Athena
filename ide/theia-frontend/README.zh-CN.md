# `ide/theia-frontend`

[English](README.md) | 简体中文

`ide/theia-frontend` 是 Athena 在 Theia 之上的当前工作台展示包。

## Responsibility

- workbench layout 与 panel composition
- Athena 自有 commands、menus、views
- Athena Home 展示、启动框架与 repository-session 状态
- 通过产品自有命令入口触发 create/open Engineering Repository 流
- 通过 Athena LSP 路由 `.athena` editor open 与 change 事件
- 将 Athena LSP diagnostics 投影到 editor 与 Problems surface
- 通过 Monaco provider 把 completion、document symbols、definition、references 桥接到 Athena LSP
- 串行化文档同步，让重复编辑后的后续语言请求先等 Athena LSP 接收最新内容
- editor-adjacent semantic inspection surfaces

## Boundary

这个包拥有展示职责，但不拥有语义主权。它必须保持在 `ide/lsp` 下游，不能直接调用 `kernel/*`，也不能发明独立的 diagnostics 或 language engine。
