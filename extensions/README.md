# `extensions`

English | [Chinese (Simplified)](README.zh-CN.md)

The `extensions` group contains domain-specific packs that plug into the kernel through core-owned contracts. Extensions may contribute domain behavior, but they do not own semantic sovereignty.

## Modules

- `:extensions:domain-electrical` -> [`domain-electrical/`](domain-electrical/README.md)
- `:extensions:domain-dummy` -> [`domain-dummy/`](domain-dummy/README.md)

## Boundary

Extensions can add lowering, validation, commands, and views through approved contracts. They must not replace the canonical engineering model, compiler pass ordering, or runtime ownership.
