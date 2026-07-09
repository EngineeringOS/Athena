# `:integrations:scm-git`

English | [Chinese (Simplified)](README.zh-CN.md)

The `:integrations:scm-git` module seeds Athena's first vendor-specific SCM substrate adapter. It is allowed to speak Git internally, but it must translate that substrate state into Athena-owned semantic baseline contracts exposed by `:kernel:semantic-scm`.

## Responsibilities

- Implement the first baseline-loading adapter behind Athena's vendor-neutral semantic SCM seam.
- Keep vendor-specific reference resolution out of `kernel/`.
- Reuse compiler-owned repository publication authority when materializing one baseline snapshot.

## Boundary

This module is not the semantic authority for diffs, reviews, commit intent, or history. It only loads substrate state and hands Athena-owned semantic snapshots back to the core.
