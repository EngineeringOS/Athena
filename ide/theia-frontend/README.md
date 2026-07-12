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
- additive AI reasoning actions and proposal-decision surfaces inside semantic inspection and semantic SCM panels
- the first read-only graphical workbench surface that consumes adapter-owned projection diagrams through the existing Athena LSP bridge
- one transient frontend semantic-selection seam that synchronizes graph selection, source reveal, semantic inspection, and semantic SCM highlighting through canonical semantic ids
- one governed inspect-first interaction slice where active-view switching routes through Athena-owned projection commands and stale transient selection is discarded on refresh
- reuse of Theia AI foundation packages for generic product capabilities while Athena semantic truth still stays behind `ide/lsp`

## Boundary

This package owns presentation, not semantic authority. It must stay downstream of `ide/lsp` and must not call `kernel/*` directly.
