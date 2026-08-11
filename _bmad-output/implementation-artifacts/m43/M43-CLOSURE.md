---
milestone: M43
title: Presentation and Rendering Reality
status: done
closed: 2026-08-06
---

# M43 Closure

## Verdict

M43 active product path is closed. Presentation Reality is published as one validated
`AthenaDiagramScene`, rendered and hit-tested by one `KonvaDiagramAdapter`, and proven against the
active `examples/m43/rolling-shutter` project plus the normative 100000-element fixture.

No compatibility path was added. Historical M43 records, `.retired` records, architecture reviews,
and `LEGACY-REPLACEMENT.md` remain evidence and are not active runtime authority.

## Replacement Audit

Audit scope:

```text
kernel/
ide/
apps/
extensions/
integrations/
contracts/
examples/m43/
```

Ledger terms checked:

```text
athena/projectionSession
requestProjectionSession
AthenaProjectionSessionPayload
AthenaProjectionDocumentPayload
AthenaSpatialSheetPayload
athena-presentation__svg
athena-presentation__canvas
paintCanvas(
```

Result: no active production transport, DTO, split SVG occurrence layer, fallback renderer, or
compatibility reader matched. Remaining matches are allowed negative tests or architecture/history
evidence. `AthenaProjectionPolicyCompiler` and `helper:experimental-elk` belong to existing M42
knowledge/layout code and are not M43 presentation transport or runtime renderer dependencies.

Dependency/import result:

```text
@engineeringood/athena-theia-frontend
  konva: 10.3.0 (exact direct dependency)
  ajv: 8.20.0 (exact direct dependency)
  production Konva import count: 1
  Pixi/WebGPU/WebGL/GLSP/ELK/tldraw/Excalidraw/draw.io runtime dependency: 0
```

## Contract And Product Evidence

- Contract manifest: `contracts/presentation/v1/manifest.json` references 16 artifacts; all exist.
- Active source: `examples/m43/rolling-shutter/src/com/engineeringood/m43/rollingshutter/rolling-shutter.athena`
- Sheet Companion: `examples/m43/rolling-shutter/src/com/engineeringood/m43/rollingshutter/rolling-shutter.sheet.athena`
- Product proof: `_bmad-output/implementation-artifacts/m43/m43-product-proof.json`
- Desktop screenshot: `_bmad-output/implementation-artifacts/m43/m43-rolling-shutter-desktop-1920x1080.png`
- Narrow screenshot: `_bmad-output/implementation-artifacts/m43/m43-rolling-shutter-mobile-720x900.png`
- Scale evidence: `_bmad-output/implementation-artifacts/m43/m43-scale-benchmark.json`

Product proof reports READY projection/spatial reality, one scene digest shared by desktop and
narrow captures, 8 occurrences, 7 routes, nonblank canvases, one source editor, and stable selected
occurrence/trace identity at both viewports.

Center-panel corrections are included in fresh proof: bottom Problems/Output startup ranking is
removed, the presentation host consumes the full center editor panel, page fit preserves aspect
ratio and top-anchors the page, and coordinate rulers remain the only visible grid decoration.
Selection proof samples rendered Konva content pixels, sends trusted Electron input, and waits for
the source editor trace surface before accepting evidence.

Scale evidence reports:

```text
scene elements:       100000
visible elements:     5000 (5%)
first stable paint:   331.6 ms
incremental heap:     30.95 MiB
interaction p95:      22.1 ms
selection p95:        26.9 ms
stable revision:      true
identity/trace/errors: 0
```

## Fresh Sequential Verification

All commands ran after rebuilding affected frontend/product bundles. Gradle commands ran one at a
time on Windows.

```text
PASS yarn workspace @engineeringood/athena-theia-frontend contracts:check
PASS yarn workspace @engineeringood/athena-theia-frontend build
PASS yarn workspace @engineeringood/athena-theia-frontend test (27/27)
PASS yarn workspace @engineeringood/athena-theia-product build
PASS yarn verify:m43-proof
PASS yarn verify:m43-scale
PASS .\gradlew.bat --no-daemon --console=plain :ide:lsp:test
PASS .\gradlew.bat --no-daemon --console=plain test
PASS tools/encoding-audit.ps1
PASS tools/source-set-hygiene-audit.ps1
```

## Residual Risk And Deferred Work

- Konva scale qualification is measured on the recorded local reference hardware only; other
  hardware requires a fresh same-scene run.
- PDF/print/report/BOM export remains outside M43. Deterministic SVG is the renderer-neutral oracle;
  PNG is Electron proof only.
- Pixi/WebGPU/WebGL, ELK/live auto-layout, AI mutation/explanation, collaboration, and full EPLAN
  compatibility remain deferred. Any future renderer must consume unchanged scene/command contracts
  and pass the same evidence gates.
- Retrospective: `_bmad-output/implementation-artifacts/m43/epic-4-retro-2026-08-06.md`.
