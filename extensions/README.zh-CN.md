# `extensions`

[English](README.md) | 简体中文

`extensions` 分组包含通过核心契约接入 kernel 的领域扩展包。扩展可以贡献领域行为，但不能拥有语义主权。

## 模块

- `:extensions:domain-electrical` -> [`domain-electrical/`](domain-electrical/README.zh-CN.md)

## 边界

扩展可以通过批准的契约增加 lowering、校验、command 与 view，但不能替代规范工程模型、compiler pass 顺序或 runtime 所有权。
