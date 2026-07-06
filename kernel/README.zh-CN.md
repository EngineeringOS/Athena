# `kernel`

[English](README.md) | 简体中文

`kernel` 分组包含 Athena 的语义骨干模块。这些模块拥有作者语言边界、规范工程模型、通用校验、编译器编排、runtime 托管，以及确定性的下游渲染能力。

## 模块

- `:kernel:language` -> [`language/`](language/README.zh-CN.md)
- `:kernel:engineering-model` -> [`engineering-model/`](engineering-model/README.zh-CN.md)
- `:kernel:validation` -> [`validation/`](validation/README.zh-CN.md)
- `:kernel:compiler` -> [`compiler/`](compiler/README.zh-CN.md)
- `:kernel:runtime` -> [`runtime/`](runtime/README.zh-CN.md)
- `:kernel:svg-renderer` -> [`svg-renderer/`](svg-renderer/README.zh-CN.md)

## 边界

Kernel 拥有语义主权。扩展、UI 与应用可以依赖它，但不应重新定义作者真源、规范模型，或打破 compiler/runtime 的所有权规则。
