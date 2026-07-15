# M18 Closeout Boundaries

M18 proves package-aware authored meaning through Athena-owned compiler and LSP authority. It does
not expand Athena into a package ecosystem, broad language redesign, desktop viewer, or alternate
frontend semantic engine.

## Proven In M18

- `kernel/language` parses the supported package/import syntax slice through the compiler parser
  boundary.
- `kernel/compiler` builds a compiler-owned project semantic graph from governed repository state.
- Imports resolve through admitted `athena.yaml` and `athena.lock` package graph state.
- Cross-source-unit and cross-package references link through semantic graph declarations and
  bindings.
- Linked package-aware meaning lowers through the canonical Engineering IR path without AST paste
  or source include behavior.
- `ide/lsp` projects package-aware diagnostics, definition, references, and document symbols from
  compiler-owned state.
- `ide/tree-sitter-athena` mirrors package/import syntax for syntax UX only.
- `examples/m18/` contains local proof fixtures for syntax, linking/lowering, and repository-backed
  closeout behavior.

## Deferred Or Excluded

- Remote registry resolution, cloud package resolution, package marketplace behavior, and package publish flows are deferred to later ecosystem milestones.
- Full export/visibility semantics, broad authored-language redesign, and broad declaration-family
  expansion are deferred to later language milestones.
- Multi-root sessions and package-local manifest redesign are deferred to later repository/workspace
  milestones.
- Frontend-owned semantic resolution is excluded. The Theia IDE may render compiler/LSP results but
  must not become import, package, or symbol authority.
- `apps/desktop-viewer`, desktop-viewer canvas behavior, and Kotlin Compose UI work are excluded
  from M18.
- Tree-sitter semantic diagnostics, package resolution, and symbol linking are excluded.

## Future Growth Rule

Future package-aware growth must extend the compiler-owned project semantic graph foundation or a
compiler/LSP projection derived from it. New package transport, publishing, marketplace, visibility,
workspace, or frontend UX features should reference this foundation instead of introducing a
parallel package model or frontend-local semantic resolver.

## Validation

Run:

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\m18-scope-boundary-audit.ps1
```

The audit checks that M18 implementation artifacts and proof fixtures avoid absolute workspace
paths, keep required boundary language present, and do not introduce proof fixture paths that imply
registry, marketplace, publish, multi-root, desktop-viewer, Kotlin Compose, or frontend-owned
semantic resolution scope.
