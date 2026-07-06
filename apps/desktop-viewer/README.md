# `:apps:desktop-viewer`

English | [Chinese (Simplified)](README.zh-CN.md)

The `:apps:desktop-viewer` module is Athena's desktop Compose application entry point. It assembles the shared Compose workbench, runtime host, and electrical domain plugin into one desktop surface for viewing and command-backed interaction proofs.

## Responsibilities

- Launch the desktop Compose entry point.
- Bootstrap the first runtime-managed desktop viewer session.
- Bind the shared workbench UI to runtime-owned project state.
- Provide desktop smoke verification for Java 25 launch behavior.

## Dependencies

- `:ui:compose-workbench`
- `:kernel:runtime`
- `:extensions:domain-electrical`

## Boundaries

This module is an application shell. It should not own engineering semantics, canonical project state, or reusable workbench primitives that belong in lower grouped modules.

## Verification

```bash
./gradlew :apps:desktop-viewer:test
./gradlew :apps:desktop-viewer:bootstrapSmoke
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :apps:desktop-viewer:test
java25; .\gradlew.bat :apps:desktop-viewer:bootstrapSmoke
```
