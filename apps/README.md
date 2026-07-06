# `apps`

English | [Chinese (Simplified)](README.zh-CN.md)

The `apps` group contains concrete entry points for users and operators. Apps assemble kernel, extensions, and UI modules into executable surfaces.

## Modules

- `:apps:cli` -> [`cli/`](cli/README.md)
- `:apps:desktop-viewer` -> [`desktop-viewer/`](desktop-viewer/README.md)

## Boundary

Apps should stay thin. They launch surfaces, wire dependencies, and present results, but they should not absorb reusable UI primitives or semantic ownership that belongs lower in the stack.
