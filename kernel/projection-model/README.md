# `:kernel:projection-model`

English | [Chinese (Simplified)](README.zh-CN.md)

`projection-model` is the M7 kernel boundary for compiler-derived graphical projection documents.

It sits downstream of:

- `:kernel:engineering-model`
- `:kernel:layout-model`
- `:kernel:geometry-model`

It stays upstream of:

- `:kernel:runtime`
- `:ide:lsp`
- `integrations/graph-*`
- desktop or web workbench surfaces

## Purpose

This module gives Athena one typed, renderer-neutral projection document shape that runtime, LSP, and downstream graph adapters can consume without rebuilding their own private models from geometry.

The boundary is intentionally small:

- layout-owned `ViewDefinition` stays the owner of view semantics
- projection documents keep canonical semantic identity
- projection nodes and connections keep inspectable references back to geometry-owned structure
- no Theia, GLSP, canvas, or frontend DTOs appear here

## Current Scope

The current M7 slice publishes:

- `ProjectionDocument`
- `ProjectionNode`
- `ProjectionConnection`
- projection-local ids and simple geometry-facing coordinates
- `ProjectionModelMarker`

It does not publish:

- runtime session lifecycle
- LSP payloads
- graph-framework protocol objects
- renderer-owned mutable state

## Rule

If a downstream surface needs graphical projection data, it should consume `:kernel:projection-model` output from compiler/runtime instead of deriving a second authority from raw geometry.
