# `graph-glsp`

English | [Chinese (Simplified)](README.zh-CN.md)

`graph-glsp` is the first translation-only graph adapter package for Athena.

## Purpose

This package is the only allowed home for the first GLSP-class graph framework boundary in the current workspace.

It exists to:

- consume Athena-owned projection-session payloads from the existing IDE bridge
- translate those payloads into disposable GLSP-shaped graph data
- keep graph-framework vocabulary outside `kernel/`, `ide/lsp`, and the Theia product packages

It must not:

- become a semantic authority
- redefine semantic identity
- persist local graph state as truth
- call the JVM, filesystem, or Athena LSP transport directly

## Current Scope

The current M7 implementation keeps this package intentionally narrow:

- package boundary and build/test proof
- GLSP-shaped translation model
- deterministic translation from Athena projection-session payloads
- active render-contribution and surface-mapping transport for downstream workbench consumption

This package still does not own visible workbench behavior, frontend session lifecycle, or semantic mutation.

## M7 Technology Position

For M7, Athena uses GLSP-shaped vocabulary as the adapter boundary, not as a full diagram-runtime commitment.

That means:

- this package stays the only allowed home for graph-framework-shaped transport models
- the current graphical proof remains translation-only and inspectable
- fuller GLSP-class editor runtime integration is a later architectural choice, not hidden M7 scope

## Verification

From the repo root:

```powershell
yarn --cwd integrations/graph-glsp install
yarn --cwd integrations/graph-glsp build
yarn --cwd integrations/graph-glsp test
```
