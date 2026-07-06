# `:ui:compose-workbench`

English | [Chinese (Simplified)](README.zh-CN.md)

The `:ui:compose-workbench` module contains shared Compose workbench infrastructure for Athena. It owns shell state, viewer stage composition, theme wiring, and interaction infrastructure such as selection, pan, and zoom, while leaving engineering semantics in runtime and model layers.

## Responsibilities

- Publish reusable Compose shell and viewer scaffolding.
- Keep workbench state domain-neutral and disposable.
- Host selection, viewport, and interaction state for semantic viewing.
- Support shared UI infrastructure used by desktop-first app surfaces.

## Main Types

- `AthenaComposeShell`
- `AthenaComposeShellState`
- `AthenaSemanticViewerStage`
- `AthenaSemanticViewerInteractionState`
- `ComposeRuntimeModuleMarker`

## Boundaries

This module does not own canonical engineering meaning, command execution, compiler orchestration, or domain rule enforcement. It is UI infrastructure only.

## Verification

```bash
./gradlew :ui:compose-workbench:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :ui:compose-workbench:test
```
