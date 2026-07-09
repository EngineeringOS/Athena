# `ide/theia-backend`

English | [Chinese (Simplified)](README.zh-CN.md)

`ide/theia-backend` is the active Athena Theia backend contribution package.

## Responsibility

- product startup wiring
- path handling and repository-session activation orchestration
- Engineering Repository bootstrap on the local filesystem
- process orchestration for the JVM LSP host
- transport bridge for Athena-namespaced and standard LSP methods
- published diagnostics capture and relay for the frontend marker surfaces
- request relay for completion, document symbols, definition, and references
- version-carrying notification relay that stays inspectable during repeated editing
- backend-side Theia contribution registration

## Boundary

This package owns product-process concerns, not semantic truth. It may select paths, expose transport endpoints, and manage the JVM host lifecycle, but semantic authority still lives in `ide/lsp`.
