# `examples/m4`

English | [Chinese (Simplified)](README.zh-CN.md)

`examples/m4` holds Engineering Repository fixtures for the Athena Theia product proof.

## Current fixture

- `open-repository-proof/` - first openable Engineering Repository for Story `1.3`, now kept compatible with the governed repository contract used by the current desktop path
- `open-repository-proof/athena.yaml` - authored repository/package intent contract for the fixture
- `open-repository-proof/athena.lock` - canonical derived lock contract for the same repository
- `open-repository-proof/src/factory-line.athena` - the authored source resolved into the active runtime-backed session

New repositories created from the Athena welcome flow follow the same governed physical shape:

- `<repository-root>/athena.yaml`
- `<repository-root>/athena.lock`
- `<repository-root>/src/<project>.athena`
