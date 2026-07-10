# `examples/m4`

[English](README.md) | 简体中文

`examples/m4` 保存 Athena Theia 产品证明所需的 Engineering Repository 样例。

## 当前样例

- `open-repository-proof/` - Story `1.3` 的第一个可打开仓库样例，现已保持与当前桌面路径使用的受治理仓库合同兼容
- `open-repository-proof/athena.yaml` - 该样例的作者仓库/包意图合同
- `open-repository-proof/athena.lock` - 同一仓库的规范派生 lock 合同
- `open-repository-proof/src/factory-line.athena` - 会被解析为活动 runtime session 的作者输入源文件

Athena welcome flow 新建仓库时也遵循同样的受治理物理结构：

- `<repository-root>/athena.yaml`
- `<repository-root>/athena.lock`
- `<repository-root>/src/<project>.athena`
