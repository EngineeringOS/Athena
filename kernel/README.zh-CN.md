# `kernel`

[English](README.md) | 简体中文

`kernel` 分组承载 Athena 的语义骨架。这些模块拥有作者语言边界、规范工程模型、显式投影边界、通用校验、编译器编排、runtime 托管与确定性的下游渲染能力。

## 模块

- `:kernel:language` -> [`language/`](language/README.zh-CN.md)
- `kernel/plugins` 子分组 -> [`plugins/`](plugins/README.zh-CN.md)
- `:kernel:plugins:plugin-api` -> [`plugins/plugin-api/`](plugins/plugin-api/README.zh-CN.md)
- `:kernel:plugins:plugin-host` -> [`plugins/plugin-host/`](plugins/plugin-host/README.zh-CN.md)
- `:kernel:repository-model` -> [`repository-model/`](repository-model/README.zh-CN.md)
- `:kernel:semantic-scm` -> [`semantic-scm/`](semantic-scm/README.zh-CN.md)
- `:kernel:engineering-model` -> [`engineering-model/`](engineering-model/README.zh-CN.md)
- `:kernel:layout-model` -> [`layout-model/`](layout-model/README.zh-CN.md)
- `:kernel:geometry-model` -> [`geometry-model/`](geometry-model/README.zh-CN.md)
- `:kernel:projection-model` -> [`projection-model/`](projection-model/README.zh-CN.md)
- `:kernel:validation` -> [`validation/`](validation/README.zh-CN.md)
- `:kernel:compiler` -> [`compiler/`](compiler/README.zh-CN.md)
- `:kernel:runtime` -> [`runtime/`](runtime/README.zh-CN.md)
- `:kernel:svg-renderer` -> [`svg-renderer/`](svg-renderer/README.zh-CN.md)

## 边界

Kernel 拥有语义主权。扩展、UI 与应用可以依赖它，但不应重新定义作者输入、规范模型，或编译器与 runtime 的所有权规则。

- `:kernel:repository-model` 是 M5 的 repository/package 合同边界。
- `:kernel:semantic-scm` 是位于 repository/package 之上的 M6 semantic SCM 边界。
- `:kernel:projection-model` 是 M7 的渲染器中立图形投影边界，位于 geometry 之上、runtime 与 LSP 之下。
- `:kernel:plugins:plugin-api` 是面向扩展的稳定公共 SPI。
- `:kernel:plugins:plugin-host` 拥有宿主插件的来源、审批与已批准库存治理，并与编译器、runtime 编排保持分离。
