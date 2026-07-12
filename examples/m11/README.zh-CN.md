# `examples/m11`

[English](README.md) | 简体中文

`examples/m11` 发布 Athena 第一份更高密度的电气 ECAD 证明仓库。

## 当前夹具

- `dense-electrical-proof/` - 面向 M11 严肃电气工作台证明的受治理仓库夹具
- `dense-electrical-proof/athena.yaml` - 编写态仓库与主包意图
- `dense-electrical-proof/athena.lock` - 同一仓库的规范派生锁文件
- `dense-electrical-proof/src/assembly-line.athena` - 高密度电气源文件，包含超过 10 个组件、超过 20 条连接、sheet 感知文档输出，以及 repeated-reference 压力

## 它证明什么

- 一个 canonical source 可以同时驱动 `cabinet`、`wiring`、`schematic`、`documentation` 四个电气家族视图。
- `documentation` 投影可以发布 repeated reference 与 cross-reference 元数据，同时不制造新的语义身份。
- 现有 runtime、LSP、graph adapter 与 workbench 路径可以在同一受治理仓库形态下承载更高密度的电气输出。
