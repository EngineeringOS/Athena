# `:apps:desktop-viewer`

English | [Chinese (Simplified)](README.zh-CN.md)

The `:apps:desktop-viewer` module is Athena's desktop Compose application entry point. It assembles the shared Compose workbench, runtime host, and electrical domain plugin into one desktop surface for viewing and command-backed interaction proofs.

## Responsibilities

- Launch the desktop Compose entry point.
- Bootstrap the runtime-managed `operator-proof` desktop viewer session from `examples/m2/operator-proof.athena`.
- Bind the shared workbench UI to runtime-owned project and projection-session state.
- Request active-view switching through `Athena Runtime` instead of holding desktop-local projection truth.
- Keep canonical semantic inspection visible while the operator switches between supported views.
- Provide desktop smoke verification for Java 25 launch behavior and the scripted M2 operator proof.

## Dependencies

- `:ui:compose-workbench`
- `:kernel:runtime`
- `:extensions:domain-electrical`

## Boundaries

This module is an application shell. It should not own engineering semantics, canonical project state, projection derivation rules, or reusable workbench primitives that belong in lower grouped modules. Selection and view switching may be surfaced here, but projection truth stays runtime-owned.

## Default Proof

The default desktop bootstrap opens the `operator-proof` seed from `examples/m2/operator-proof.athena`. That file starts without authored connections so the desktop proof can show a runtime-owned command creating `connection:PLC1.out->M1.in` while preserving canonical selection across `cabinet` and `wiring`.

## Verification

```bash
./gradlew :apps:desktop-viewer:test
./gradlew :apps:desktop-viewer:bootstrapSmoke
./gradlew :apps:desktop-viewer:operatorProofSmoke
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :apps:desktop-viewer:test
java25; .\gradlew.bat :apps:desktop-viewer:bootstrapSmoke
java25; .\gradlew.bat :apps:desktop-viewer:operatorProofSmoke
```
