---
baseline_commit: adb0ae5d1956b13d8aa3df8774e102dfc09ef380
---

# Story 3.2: Route Electrical View Definitions And Renderer Mappings Through `extensions/domain-electrical`

Status: review

## Story

As a domain platform engineer,  
I want electrical projection contributions to enter through the extension layer,  
so that M7 can prove the first domain projection path without hard-coding domain view logic into kernel contracts.

## Acceptance Criteria

1. Given the existing `extensions/domain-electrical` proof domain, when Athena adds the first domain-specific projection contributions, then electrical view definitions and renderer mappings enter through extension-owned contribution paths, and kernel projection contracts remain renderer-neutral and domain-neutral.
2. Given later domains are considered, when extension boundaries are reviewed, then the same contribution model remains usable for future mechanical, process, or other view families, and M7 does not require kernel contract rewrites to support them.

## Tasks / Subtasks

- [x] Extend the stable render contribution contract with typed downstream surface-mapping metadata. (AC: 1, 2)
  - [x] Add `AthenaRenderSurface` and `AthenaRenderSurfaceMapping` to the plugin API.
  - [x] Keep the mapping contract inspectable and downstream-only instead of turning it into semantic authority.
- [x] Publish the first graphical electrical renderer mappings from `extensions/domain-electrical`. (AC: 1, 2)
  - [x] Keep the existing `cabinet` and `wiring` view definitions extension-owned.
  - [x] Add cabinet and wiring surface tokens for the `graph-workbench` target without removing the existing `svg` target.
- [x] Route the active graphical mapping set through runtime and `ide/lsp`. (AC: 1)
  - [x] Attach active graphical render contributions to runtime projection-ready snapshots.
  - [x] Expose those mappings through the typed LSP projection payload.
- [x] Keep the graph adapter and workbench downstream of the new mapping seam. (AC: 1, 2)
  - [x] Thread active render contributions through `integrations/graph-glsp`.
  - [x] Apply the mapped surface tokens in the Theia graph workbench through CSS variables.
- [x] Cover the new seam with regression checks. (AC: 1, 2)
  - [x] Extend plugin API, plugin host, electrical plugin, runtime projection, LSP, GLSP, and frontend pure-model tests.
  - [x] Re-run the touched JVM and TypeScript verification paths sequentially on Windows.

## Dev Notes

### Implementation Notes

- Story `3.2` stayed intentionally narrow.
- The kernel still does not own electrical styling, notation, or frontend-specific view logic.
- The new contract proves view-level downstream surface mappings only:
  - `canvas`
  - `node`
  - `edge`
- Those mappings are published by the electrical extension, filtered by view plus renderer target, and transported to the graphical workbench as inspectable metadata.

### Architectural Guardrails Kept

- Canonical semantic authority remains `Engineering IR`.
- Supported view definitions remain extension-owned metadata.
- Renderer mappings remain downstream presentation metadata and do not modify layout, geometry, or canonical identity.
- The graph workbench only consumes transport payloads and applies CSS variables; it does not become a semantic authority.

### Verification

- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:plugins:plugin-api:test :kernel:plugins:plugin-host:test :extensions:domain-electrical:test :kernel:compiler:test :kernel:runtime:test :ide:lsp:test"`
- `yarn --cwd integrations/graph-glsp test`
- `yarn --cwd ide/theia-frontend test`
- `yarn --cwd ide build`
- `yarn --cwd ide start:smoke`

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Completion Notes List

- Added typed downstream surface-mapping metadata to the stable render contribution SPI.
- Updated the electrical extension to publish cabinet and wiring graph-workbench mappings while preserving the existing `svg` path.
- Routed active graphical render contributions through runtime projection snapshots, `ide/lsp`, the GLSP adapter, and the Theia workbench.
- Applied extension-owned surface tokens to the graph workbench through CSS variables so `cabinet` and `wiring` now render with distinct downstream presentation profiles.
- Verified the touched JVM, adapter, frontend, IDE build, and desktop smoke paths sequentially.

### File List

- `_bmad-output/implementation-artifacts/m7/3-2-route-electrical-view-definitions-and-renderer-mappings-through-extensions-domain-electrical.md`
- `_bmad-output/implementation-artifacts/m7/sprint-status.yaml`
- `kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaContributionModel.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerRenderingModel.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionProtocol.kt`
- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt`
- `integrations/graph-glsp/src/athena-glsp-diagram-model.ts`
- `integrations/graph-glsp/src/athena-glsp-projection-adapter.ts`
- `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`
- `ide/theia-frontend/src/browser/style/index.css`
- `kernel/plugins/plugin-api/src/test/kotlin/com/engineeringood/athena/plugin/PluginApiContributionContractTest.kt`
- `kernel/plugins/plugin-host/src/test/kotlin/com/engineeringood/athena/plugin/host/AthenaPluginContractTest.kt`
- `extensions/domain-electrical/src/test/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPluginTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServicesTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSessionTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionRequestTest.kt`
- `integrations/graph-glsp/test/athena-graph-glsp-adapter.test.mjs`
- `ide/theia-frontend/scripts/athena-graph-workbench-model.test.mjs`

### Change Log

- 2026-07-10: Implemented M7 Story `3.2` by introducing extension-owned graphical surface mappings and routing them through the existing runtime, LSP, adapter, and workbench seams.
