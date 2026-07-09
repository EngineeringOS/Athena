# `examples/m4`

English | [Chinese (Simplified)](README.zh-CN.md)

`examples/m4` holds Engineering Repository fixtures for the Athena Theia product proof.

## Current fixture

- `open-repository-proof/` - first openable Engineering Repository for Story `1.3`
- `open-repository-proof/src/factory-line.athena` - the authored source resolved into the active runtime-backed session

New repositories created from the Athena welcome flow follow the same light physical shape:

- `<repository-root>/src/<project>.athena`

## Temporary M4 repository rule

M4 does not freeze the final manifest, lockfile, or package-graph contracts yet.

For this milestone, Athena opens a repository only when it resolves exactly one authored `.athena` source. If a `src/` directory exists, it is searched first.
