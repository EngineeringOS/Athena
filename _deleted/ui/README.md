# `ui`

English | [Chinese (Simplified)](README.zh-CN.md)

The `ui` group contains reusable interface infrastructure above the kernel. It is where Athena hosts workbench and viewer primitives without moving engineering semantics into the presentation layer.

## Modules

- `:ui:compose-workbench` -> [`compose-workbench/`](compose-workbench/README.md)

## Boundary

UI modules can compose, inspect, and project kernel-owned state. They must not become a second semantic authority.
