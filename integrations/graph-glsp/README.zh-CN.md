# `graph-glsp`

[English](README.md) | 简体中文

`graph-glsp` 是 Athena 当前第一个 translation-only 图形适配包。

## 目标

这个包是当前工作区里第一条 GLSP-class 图形框架边界唯一允许落地的位置。

它负责：

- 消费来自现有 IDE bridge 的 Athena-owned projection-session payload
- 把这些 payload 翻译成可丢弃的 GLSP-shaped graph data
- 把图形框架词汇隔离在 `kernel/`、`ide/lsp` 和 Theia 产品包之外

它不能：

- 成为语义主权边界
- 重新定义 semantic identity
- 把本地图状态持久化为真值
- 直接调用 JVM、文件系统或 Athena LSP transport

## 当前范围

当前 M7 实现仍然把这个包保持在很窄的范围内：

- 包边界与 build/test proof
- GLSP-shaped translation model
- 从 Athena projection-session payload 到图形模型的确定性翻译
- active render contribution 与 surface mapping 的下游传输

这个包仍然不拥有可见 workbench 行为、前端 session lifecycle 或语义变更。

## M7 技术定位

对 M7 来说，Athena 采用的是 GLSP-shaped 词汇作为适配边界，而不是立即承诺完整的图编辑 runtime。

这意味着：

- 图框架形状化 transport model 只能放在这个包里
- 当前图形证明依然是 translation-only 且可检查的
- 更完整的 GLSP-class editor runtime 集成属于后续架构决策，而不是隐藏的 M7 范围

## 验证

在仓库根目录执行：

```powershell
yarn --cwd integrations/graph-glsp install
yarn --cwd integrations/graph-glsp build
yarn --cwd integrations/graph-glsp test
```
