---
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 4.3: Prove Exact Connections End To End

Status: done

## Story

As a milestone evaluator,
I want structural and visual evidence from a fresh M38 build,
so that drawing trust is demonstrated rather than claimed.

## Acceptance Criteria

1. Fresh kernel, LSP, graph GLSP, Theia frontend, and Theia product builds run before Electron proof.
2. Structural proof for `examples/m38/professional-control-drawing` reports zero detached endpoints, fallback endpoints, renderer repairs, inferred topology, missing line facts, and incomplete traces.
3. Every visible route endpoint exactly equals its placed Anchor point.
4. Endpoint inspection proof reaches the exact Athena source span.
5. Post-ready refresh completes within 10 seconds on supported workstation.
6. 1920x1080, 1280x900, and narrow Electron screenshots are stored under `_bmad-output/implementation-artifacts/m38/screenshots`.
7. Sequential root/affected runtime/LSP/frontend/IDE/Electron/hygiene/encoding/diff checks pass.

## Tasks / Subtasks

- [x] Add M38 product E2E wiring (AC: 1, 6)
  - [x] Add `start:m38` and `start:smoke:m38` package scripts.
  - [x] Add M38 Electron verifier using only `examples/m38/professional-control-drawing`.
  - [x] Add frontend/product contract test proving verifier points at M38 and screenshot paths are M38.
- [x] Add structural proof assertions (AC: 2, 3, 4, 5)
  - [x] Assert connector endpoints equal supplied Anchor endpoint points.
  - [x] Assert all route states contain Connection, Port, binding, occurrence, Anchor, line class, label, source span, and compiler snapshot evidence.
  - [x] Assert no fallback representation, center fallback endpoint, renderer inference, inferred topology, or incomplete trace.
  - [x] Assert compile-to-presentation refresh is under 10 seconds.
- [x] Capture visual evidence (AC: 6)
  - [x] Generate desktop 1920x1080 screenshot.
  - [x] Generate desktop 1280x900 screenshot.
  - [x] Generate narrow screenshot.
  - [x] Store screenshots under `_bmad-output/implementation-artifacts/m38/screenshots`.
- [x] Verify sequentially (AC: 7)
  - [x] Run affected Gradle tests sequentially.
  - [x] Run graph GLSP and Theia frontend tests/build sequentially.
  - [x] Run LSP install distribution before Electron proof.
  - [x] Run M38 Electron smoke.
  - [x] Run source-set hygiene audit.
  - [x] Run encoding audit.
  - [x] Run `git diff --check`.

## Dev Notes

This story proves M38. It must not reuse M37/M36 example paths, screenshots, proof sentinel names, or
product scripts except as mechanical pattern reference.

Hard rules:

- Stay in M38.
- Theia is render layer. No Java2D.
- Use `examples/m38/professional-control-drawing` only.
- Store screenshots in `_bmad-output/implementation-artifacts/m38/screenshots`.
- No compatibility shim. If old verifier assumptions conflict with M38, update to current M38 evidence.
- M38 proves trust, not professional route/layout beauty.

Useful current files:

- `ide/theia-product/scripts/verify-athena-m37-professional-control-drawing.js` is the nearest smoke pattern.
- `ide/theia-frontend/scripts/athena-m37-professional-control-drawing-product-e2e-contract.test.mjs` is the nearest contract test pattern.
- `ide/theia-product/scripts/athena-electron-open-workspace-main.js` emits graph workbench proof and screenshot sentinels.
- `examples/m38/professional-control-drawing` was created and tested in Story 4.1.

Testing requirements:

```powershell
.\gradlew.bat --no-daemon --console=plain -q :kernel:compiler:test --tests com.engineeringood.athena.compiler.DedicatedM38ProfessionalDrawingSampleTest
.\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist
yarn --cwd integrations/graph-glsp test
yarn --cwd ide/theia-frontend test
yarn --cwd ide/theia-product build
yarn --cwd ide/theia-product start:smoke:m38
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
git diff --check
```

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-08-01: Story created from M38 sprint order after Story 4.2 reached review.
- 2026-08-01: Dev started from sprint order.
- 2026-08-01: Fixed Theia product screenshot capture by falling back to renderer-side SVG rasterization when Electron native capture is unavailable.
- 2026-08-01: Carried connector source spans from compiler `PresentationConnector` through LSP, GLSP, Theia model, and rendered route evidence.
- 2026-08-01: Verified M38 product smoke after fresh LSP install and Theia product build.

### Completion Notes

- M38 exact-connection E2E proof passes on dedicated M38 example.
- Every visible route now carries source span evidence in DOM route proof.
- Screenshot evidence captured at 1920x1080, 1280x900, and narrow viewports.
- Native Electron screenshot can fail on unavailable display surface; smoke now exports the Theia SVG canvas through renderer-side browser APIs as fallback.

### File List

- `_bmad-output/implementation-artifacts/m38/4-3-prove-exact-connections-end-to-end.md`
- `_bmad-output/implementation-artifacts/m38/screenshots/m38-professional-control-drawing-desktop-1920x1080.png`
- `_bmad-output/implementation-artifacts/m38/screenshots/m38-professional-control-drawing-desktop-1280x900.png`
- `_bmad-output/implementation-artifacts/m38/screenshots/m38-professional-control-drawing-narrow.png`
- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationOccurrenceModels.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/PresentationConnectorCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/PresentationModelDeriver.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationPayloads.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationSessionProtocol.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationConnectorPayloadTest.kt`
- `integrations/graph-glsp/src/athena-glsp-projection-source.ts`
- `integrations/graph-glsp/src/athena-glsp-projection-adapter.ts`
- `integrations/graph-glsp/test/athena-graph-glsp-adapter.test.mjs`
- `ide/theia-frontend/src/browser/athena-graph-presentation-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-edge-layer.tsx`
- `ide/theia-product/scripts/athena-electron-open-workspace-main.js`

### Change Log

- 2026-08-01: Created Story 4.3 context and marked ready for dev.
- 2026-08-01: Started Story 4.3 implementation.
- 2026-08-01: Completed Story 4.3 exact-connection E2E proof and moved to review.

## Verification Evidence

```powershell
.\gradlew.bat --no-daemon --console=plain -q :kernel:compiler:test --tests com.engineeringood.athena.compiler.DedicatedM38ProfessionalDrawingSampleTest
.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaPresentationConnectorPayloadTest --tests com.engineeringood.athena.ide.lsp.AthenaM38DedicatedProfessionalControlDrawingSmokeTest
.\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist
yarn --cwd integrations/graph-glsp test
yarn --cwd ide/theia-frontend test
yarn --cwd ide/theia-product build
yarn --cwd ide/theia-product start:smoke:m38
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
git diff --check
```
