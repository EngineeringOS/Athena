# `kernel`

English | [Chinese (Simplified)](README.zh-CN.md)

The `kernel` group contains Athena's semantic backbone. These modules own the authored language boundary, the canonical engineering model, generic validation, compiler orchestration, runtime hosting, and deterministic downstream rendering.

## Modules

- `:kernel:language` -> [`language/`](language/README.md)
- `:kernel:engineering-model` -> [`engineering-model/`](engineering-model/README.md)
- `:kernel:validation` -> [`validation/`](validation/README.md)
- `:kernel:compiler` -> [`compiler/`](compiler/README.md)
- `:kernel:runtime` -> [`runtime/`](runtime/README.md)
- `:kernel:svg-renderer` -> [`svg-renderer/`](svg-renderer/README.md)

## Boundary

The kernel owns semantic authority. Extensions, UI, and apps may depend on it, but they should not redefine the authored source, the canonical model, or the compiler/runtime ownership rules.
