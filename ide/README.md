# `ide`

English | [Chinese (Simplified)](README.zh-CN.md)

The `ide` group is Athena's primary desktop-first product path. It now hosts the runnable Eclipse Theia based Athena shell, the M5 repository seams, the current M6 semantic SCM plus package-history workbench projections, and the first M7 graphical workbench surface.

## Scope

This group is structural plus runnable.

It currently establishes:

- the physical home for the Athena Theia product path
- the ownership split between product, frontend, backend, and LSP
- the local Yarn workspace used to build and launch the Theia desktop shell
- the branded Athena home surface inside the shell
- the Engineering Repository open flow and single-session activation path
- the governed M5 Engineering Repository create flow
- the stdio Athena LSP server embedded in the JVM runtime stack
- the `.athena` editor-open path routed through LSP instead of direct semantic imports
- the Athena-owned diagnostics path from JVM parsing and validation into editor and Problems surfaces
- the Athena-owned completion, document-symbol, definition, and references path through the same LSP boundary
- the Athena-owned `.athena` syntax highlighting path, including token-family colors documented in [docs/usages/ide-syntax-highlighting.md](../docs/usages/ide-syntax-highlighting.md)
- the repeated-edit stability path that keeps diagnostics and navigation aligned with the latest in-memory Athena state
- the additive Athena semantic SCM workbench panel that now projects baseline-driven review, commit-preparation, package evolution, and release relevance through the existing backend plus LSP bridge
- the additive Athena graphical workbench panel that now projects runtime-owned node and relationship views through the graph adapter boundary without moving projection authority into the frontend
- the first synchronized semantic-selection path that now keeps graphical selection, source reveal, semantic inspection, and semantic SCM context aligned through canonical semantic ids
- the first inspect-first graphical interaction path that now keeps active-view switching governed by the Athena runtime command allowlist and discards stale transient selection on projection refresh

It does not yet deliver:

- full hover, rename, formatting, and richer multi-file language tooling coverage through Athena LSP
- richer review overlays or richer governed graphical interaction

## Packages

- `ide/theia-product/` -> [theia-product/](theia-product/README.md)
- `ide/theia-frontend/` -> [theia-frontend/](theia-frontend/README.md)
- `ide/theia-backend/` -> [theia-backend/](theia-backend/README.md)
- `ide/lsp/` -> [lsp/](lsp/README.md)

## Commands

```powershell
Set-Location ide
yarn install
yarn build
yarn start:smoke
yarn start
```

## Boundary

`ide/` owns product-shell structure and Theia integration. It must not absorb kernel semantic authority. The current IDE path reaches repository activation, authored-source open, diagnostics, a first serious authoring/navigation baseline through `ide/lsp`, and additive semantic SCM plus package-history projection through the same backend/LSP path, while `apps/cli`, `apps/desktop-viewer`, and `ui/compose-workbench` remain secondary proof shells during M4-M6.

Current M5 bootstrap shape for newly created repositories:

- `<repository-root>/athena.yaml`
- `<repository-root>/athena.lock`
- `<repository-root>/src/<project>.athena`
- one primary package bootstrap with explicit `src` ownership
- no predeclared extra dependency graph at creation time; canonical package resolution is derived later through compiler/runtime authority
