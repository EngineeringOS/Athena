# `ide/theia-backend`

[English](README.md) | 简体中文

`ide/theia-backend` 是当前 Athena Theia 后端贡献包。

## Responsibility

- 产品启动接线
- 路径处理与 repository-session 激活编排
- 本地文件系统上的 Engineering Repository bootstrap
- JVM LSP host 的进程编排
- Athena 命名空间与标准 LSP 方法的传输桥接
- 已发布 diagnostics 的捕获与向前端 relay
- completion、document symbols、definition、references 的请求 relay
- 带版本信息的 notification relay，方便排查重复编辑时的状态连续性
- backend-side Theia contribution registration

## Boundary

这个包拥有产品进程相关职责，但不拥有语义真相。它可以选择路径、暴露传输端点、管理 JVM host 生命周期，并把 LSP 结果向前端 relay；但语义主权仍然位于 `ide/lsp`。
