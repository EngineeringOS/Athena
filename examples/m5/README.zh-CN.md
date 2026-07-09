# `examples/m5`

[English](README.md) | 中文（简体）

`examples/m5` 保存已完成的仓库与包图证明所需的受治理仓库样例。

## 当前样例

- `repository-graph-proof/` - 一个包含主包的受治理仓库根
- `repository-graph-proof/athena.yaml` - 作者意图的仓库/包 manifest 契约
- `repository-graph-proof/athena.lock` - 以稳定顺序渲染的派生 lock 契约
- `repository-graph-proof/src/root.athena` - 主包源文件
## 证明意图

这个样例用于证明 Athena 可以围绕一个受治理仓库根解析出最小规范 package graph，并保持：

- `athena.yaml` 仍然是作者意图
- `athena.lock` 仍然是 compiler 权威派生状态
- package 身份明确且可检查
- 仓库校验与 lock 解释保持确定性
