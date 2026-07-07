# `kernel`

[English](README.md) | 简体中文

`kernel` 分组承载 Athena 的语义骨架。这些模块拥有作者语言边界、稳定插件 API、规范工程模型、通用校验、编译器编排、runtime 宿主能力，以及确定性的下游渲染编排。

## 模块

- `:kernel:language` -> [`language/`](language/README.zh-CN.md)
- `:kernel:plugin-api` -> [`plugin-api/`](plugin-api/README.zh-CN.md)
- `:kernel:engineering-model` -> [`engineering-model/`](engineering-model/README.zh-CN.md)
- `:kernel:layout-model` -> [`layout-model/`](layout-model/README.zh-CN.md)
- `:kernel:geometry-model` -> [`geometry-model/`](geometry-model/README.zh-CN.md)
- `:kernel:validation` -> [`validation/`](validation/README.zh-CN.md)
- `:kernel:compiler` -> [`compiler/`](compiler/README.zh-CN.md)
- `:kernel:runtime` -> [`runtime/`](runtime/README.zh-CN.md)
- `:kernel:svg-renderer` -> [`svg-renderer/`](svg-renderer/README.zh-CN.md)

## 边界

Kernel 拥有语义主权。扩展、UI 与应用可以依赖它，但不应重新定义作者源码、规范模型，或编译器与 runtime 的所有权规则。`:kernel:plugin-api` 是专门的扩展侧公共 SPI 边界；编译器与 runtime 的宿主逻辑仍然保持分离。
