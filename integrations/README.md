# `integrations`

English | [Chinese (Simplified)](README.zh-CN.md)

The `integrations` group contains vendor-specific substrate adapters that Athena can consume without turning vendor APIs into semantic authorities.

## Modules

- `:integrations:scm-git` -> [`scm-git/`](scm-git/README.md)

## Boundary

Integration modules may resolve vendor references, talk to vendor processes, or translate substrate state into Athena-owned contracts. They must not redefine semantic change, review, commit-intent, or history meaning already owned by `kernel/semantic-scm`.
