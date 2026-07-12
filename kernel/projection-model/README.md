# `:kernel:projection-model`

English | [Chinese (Simplified)](README.zh-CN.md)

`projection-model` is the M7 kernel boundary for compiler-derived graphical projection documents.

As of M11, it also carries:

- first governed electrical sheet model
- first governed electrical notation-pack model
- first repeated-reference and cross-reference contract

Both layers stay projection-owned and downstream of canonical engineering meaning.

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
- layout-owned `ViewDefinition` may now also carry governed projection-family contracts for downstream electrical workbench depth
- projection documents keep canonical semantic identity
- projection sheets keep stable downstream identity, ordering, and navigation semantics
- projection sheet subjects point back to canonical semantic identity without turning sheets into truth
- projection notation packs keep symbol choice, label policy, and marker tokens inspectable without turning notation into truth
- projection cross references keep repeated references inspectable without inventing view-local alias identities
- projection nodes and connections keep inspectable references back to geometry-owned structure
- no Theia, GLSP, canvas, or frontend DTOs appear here

## Current Scope

The current M7 slice publishes:

- `ProjectionDocument`
- `ProjectionNode`
- `ProjectionConnection`
- `ProjectionSheet`
- `ProjectionSheetSubject`
- `ProjectionNotationPack`
- `ProjectionNotationSubject`
- `ProjectionCrossReference`
- projection-local ids and simple geometry-facing coordinates
- `ProjectionModelMarker`

It does not publish:

- runtime session lifecycle
- LSP payloads
- graph-framework protocol objects
- renderer-owned mutable state

## Rule

If a downstream surface needs graphical projection data, it should consume `:kernel:projection-model` output from compiler/runtime instead of deriving a second authority from raw geometry.

If a downstream surface needs page or sheet navigation, it should consume the governed sheet
contract here instead of inventing frontend-local page truth.

If a downstream surface needs symbol choice, label behavior, or presentation markers, it should
consume the governed notation-pack contract here instead of hardcoding renderer-local notation.

If a downstream surface needs repeated-reference or cross-reference state, it should consume the
canonical-identity-first contract here instead of inventing renderer-local alias maps.
