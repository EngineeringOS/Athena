# Athena M46 Closure

Date: 2026-08-12
Status: verified closure
Example: `examples/m46/rolling-shutter`

## Verified Scope

- M46 source owns typed binary Engineering Connections and multi-endpoint Engineering Nets.
- Compiler publishes canonical Connection IR, deterministic Connection Projections, typed orthogonal Route Plans,
  and renderer-neutral `SceneConnection`.
- Local package resolution supplies admitted representation geometry and Port anchors; SVG geometry never becomes
  engineering meaning.
- Theia accepts typed connect/reconnect/route intent through server transactions, validates, journals, recompiles,
  supports Undo/Redo, and reopens accepted state.
- Folio orders independent Power and CPU/Control Page Companions. Each Page publishes one independent Scene editor.

## Requirement Evidence

| Requirement | Current proof |
| --- | --- |
| FR1-FR8 | Affected Kotlin model/compiler/LSP suites; M46 source and `M46RollingShutterConnectionProjectTest` |
| FR9-FR12 | Connection projection/route/topology compiler tests; frontend SceneConnection tests; SVG proof |
| FR13-FR15 | Connection read-model/frontend transaction tests; author/reopen operation transcript |
| FR16 | M46 example, desktop/narrow/reopened screenshots, SVG/PNG export proof |
| NFR1 | repeated SVG/PNG byte equality in `exports/m46-export-proof.json` |
| NFR2 | invalid direction and stale journal rejection in author/reopen proof |
| NFR3 | one-pixel non-scaling route/frame export checks; frontend paint tests |
| NFR4 | 1,000-connection measured evidence in `performance/m46-connection-performance.json` |
| NFR5 | Connection IR/Route Plan/Scene contract tests; no DOM/Konva fields in kernel contract |
| NFR6 | source-set hygiene, active executable-root scan, no compatibility route contract |
| NFR7 | sequential rebuild, workspace-root activation, READY LSP/Scene proof, screenshots, exports, audits |
| UX-DR1-UX-DR6 | renderer/frontend contract tests plus current desktop/narrow product captures |

## Current Artifacts

- SVG: `exports/m46-rolling-shutter.svg`
  `sha256:879dea71487695aae4f3cedf44d4ce2e01e9de467e780b7dd6c53adf043e09d7`
- PNG: `exports/m46-rolling-shutter.png`
  `sha256:cc4d832ef40b19813d79ef4fe0089923ef874f6a240f48bd2542e340859ebc66`
- Export proof: `exports/m46-export-proof.json`
- Performance proof: `performance/m46-connection-performance.json`
- Transaction/reopen proof: `operation-transcripts/4-3-author-reopen-product-proof.json`
- Screenshots: `screenshots/m46-authoring-*.png`

## Verification

All passed on 2026-08-12:

- Complete affected Gradle suites sequentially, including `:ide:lsp:test` and `:ide:lsp:installDist`.
- Tree-sitter generate/test/WASM: 23 successful parses, 0 failures.
- Frontend contracts and tests: 88 passed.
- Full Theia/Electron product rebuild.
- `verify:m46-performance`, `verify:m46-authoring`, `verify:m46-export`.
- Encoding audit, source-set hygiene audit, and `git diff --check`.

## Residual Risks

- Clean Theia start produced a non-blocking RPC notification/layout-restore warning while product workspace activation,
  LSP `READY`, zero source diagnostics, and M46 product automation completed. Keep it visible for M47 startup cleanup.
- M46 proves IEC-like visual grammar and deterministic rendering. Pixel-perfect vendor drawing parity, cross-page
  Smart Connect authoring, harness fabrication, and 3D routing remain outside scope.
