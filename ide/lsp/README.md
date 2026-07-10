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
- additive projection-session request surfaces for runtime-owned graphical state inspection
- one explicit governed projection-command allowlist for inspect-first graphical interaction, currently limited to active-view switching
- future hover, rename, and richer workspace navigation in later stories

## Boundary

Story `2.4` extended this package from authoring transport into the first semantic SCM projection bridge. Story `3.3` widens that same additive bridge to include package evolution and release relevance. M7 Story `1.4` adds typed projection-session queries plus one governed projection-command seam over the runtime-owned projection session.

Theia may manage process lifecycle and transport, but semantic or projection access must continue to flow through LSP methods here instead of direct calls into `kernel/*`.

The current M7 projection boundary is intentionally narrow:

- `athena/projectionSession` returns runtime-owned supported views, active view state, inspectable ready or unavailable projection payloads, and the published command allowlist
- unavailable projection payloads preserve underlying runtime diagnostics, including stable codes and provenance when the upstream failure exposes them
- `athena/projectionCommand` accepts only Athena-allowlisted projection actions instead of exposing a generic runtime tunnel
- hosted plugin commands, graph-framework commands, and arbitrary frontend-local actions are not public transport contracts here
