# `examples/m12`

[English](README.md) | 简体中文

`examples/m12` 发布 Athena 的 renderer hardening 证明语料。

## 当前夹具

- `renderer-benchmark-proof/` - 面向 M12 电气 renderer benchmark 的受治理仓库夹具
- `renderer-benchmark-proof/athena.yaml` - 编写态仓库与主包意图
- `renderer-benchmark-proof/athena.lock` - 同一仓库的规范派生锁文件
- `renderer-benchmark-proof/src/expansion-line.athena` - 比 M11 baseline 更大的电气源文件，用于验证连接可读性、viewport 行为，以及 repeated-reference reveal

## 它证明什么

- Athena 可以在不改变语义权威边界的前提下承载比 M11 baseline 更大的电气场景。
- graph workbench 在更大的夹具上仍然可以完成 fit、pan、zoom 与 canonical 电气主题 reveal。
- repeated-reference 导航与 related-subject reveal 依然以下游 canonical semantic id 为锚点。
