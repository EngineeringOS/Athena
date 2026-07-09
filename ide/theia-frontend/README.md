# `ide/theia-frontend`

English | [Chinese (Simplified)](README.zh-CN.md)

`ide/theia-frontend` is the active Athena workbench presentation package on top of Theia.

## Responsibility

- workbench layout and panel composition
- Athena-owned commands, menus, and views
- Athena Home presentation, launch framing, and repository-session status
- create/open Engineering Repository flows from product-owned command entry points
- `.athena` editor-open routing through Athena LSP
- Athena LSP diagnostics projection into editor and Problems surfaces
- Monaco completion, document symbols, definition, and references providers bridged to Athena LSP
- serialized document synchronization so repeated edits reach Athena LSP before follow-up language requests
- editor-adjacent semantic inspection surfaces

## Boundary

This package owns presentation, not semantic authority. It must stay downstream of `ide/lsp` and must not call `kernel/*` directly.
