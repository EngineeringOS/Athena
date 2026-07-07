# `kernel/plugins`

English | [Chinese (Simplified)](README.zh-CN.md)

The `kernel/plugins` subgroup contains Athena's plugin infrastructure modules. These modules keep the extension-facing SPI separate from host-owned discovery, approval, lifecycle, and approved-inventory governance.

## Modules

- `:kernel:plugins:plugin-api` -> [`plugin-api/`](plugin-api/README.md)
- `:kernel:plugins:plugin-host` -> [`plugin-host/`](plugin-host/README.md)

## Boundary

`plugin-api` is the stable public contract surface that extensions compile against. `plugin-host` is the host-owned governance layer that discovers, approves, inventories, and exposes installed plugins to compiler and runtime services.

