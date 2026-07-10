# `integrations`

English | [Chinese (Simplified)](README.zh-CN.md)

The `integrations` group contains vendor-specific substrate adapters that Athena can consume without turning vendor APIs into semantic authorities.

## Modules

- `:integrations:scm-git` -> [`scm-git/`](scm-git/README.md)
- `node: graph-glsp` -> [`graph-glsp/`](graph-glsp/README.md)

## Boundary

Integration modules may resolve vendor references, talk to vendor processes, or translate substrate state into Athena-owned contracts. They must not redefine semantic change, review, commit-intent, projection authority, or history meaning already owned by Athena kernel and IDE boundaries.

Graph-framework dependencies belong under `integrations/graph-*` only. `ide/theia-*` may consume those adapters, but it must not become the owner of graph-framework protocol or rendering vocabulary.
