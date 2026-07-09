# `examples/m4`

[English](README.md) | 简体中文

`examples/m4` 保存 Athena Theia 产品证明所需的 Engineering Repository 样例。

## 当前样例

- `open-repository-proof/` - Story `1.3` 的第一个可打开仓库样例
- `open-repository-proof/src/factory-line.athena` - 会被解析为活动 runtime session 的作者输入源文件

Athena welcome flow 新建仓库时也遵循同样的轻量物理结构：

- `<repository-root>/src/<project>.athena`

## 当前 M4 仓库规则

M4 还不会冻结最终的 manifest、lockfile 或 package graph 合同。

在这个里程碑里，Athena 只有在仓库能够解析出且仅解析出一个 `.athena` 作者源文件时才会打开它。如果存在 `src/` 目录，就优先从那里搜索。
