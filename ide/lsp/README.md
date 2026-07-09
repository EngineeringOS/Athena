# `ide/lsp`

English | [Chinese (Simplified)](README.zh-CN.md)

`ide/lsp` is Athena's semantic-service host for the IDE path.

## Responsibility

- repository-session authority for the IDE path
- the stdio Athena LSP server that embeds the existing JVM runtime stack
- `initialize`-time repository activation inside the LSP boundary
- the Athena-authored `textDocument/didOpen` semantic path for `.athena` source
- `textDocument/publishDiagnostics` sourced from Athena-owned parsing, semantic analysis, and validation
- `textDocument/completion`, `textDocument/documentSymbol`, `textDocument/definition`, and `textDocument/references` backed by Athena-owned document state
- version-aware tracked document state that rejects stale rollbacks during repeated editing
- additive semantic SCM request surfaces for baseline-driven review, commit-preparation, and package-history state
- future hover, rename, and richer workspace navigation in later stories

## Boundary

Story `2.4` extended this package from authoring transport into the first semantic SCM projection bridge. Story `3.3` widens that same additive bridge to include package evolution and release relevance. Theia may manage process lifecycle and transport, but semantic access must continue to flow through LSP methods here instead of direct calls into `kernel/*`.
