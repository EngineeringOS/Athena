---
title: M43 Presentation Replacement Ledger
status: final
updated: 2026-08-06
---

# M43 Presentation Replacement Ledger

Canonical `AthenaDiagramScene` activation removes the current raw Projection/Spatial presentation
path in the same story. This is deletion scope, not compatibility or migration scope.

## Prior Implementation Disposition

Replanning does not mean repository-wide rollback. Classify current M43 implementation before edit:

| Current implementation | Disposition | Required proof |
| --- | --- | --- |
| `SheetCompanionLanguage.kt` and parser tests | **ADOPT/EXTEND** | New syntax, positive multiple-of-4 `cell: N`, spans, diagnostics, and contract vectors pass. |
| `SheetCompanionProjectionPlacementMapper.kt` and tests | **ADOPT/REFACTOR** | Stable occurrence identity, A1/micro lowering, locks, provenance, and shared vectors pass. |
| `GridAlignedSpatialLayoutCompiler.kt` and tests | **ADOPT/REFACTOR** | Determinism, bounds, overlap, missing-companion behavior, and M41 Spatial invariants pass. |
| Sheet Companion LSP parsing, symbols, and diagnostics | **ADOPT/REFACTOR** | Same-basename discovery and `UNAVAILABLE` publication behavior pass without raw paint DTOs. |
| Raw Projection/Spatial LSP payloads and `athena/projectionSession` | **DELETE/REPLACE** | `AthenaScenePublication` schema and new methods pass; absence gate is clean. |
| Split DOM SVG plus Canvas presentation widget | **DELETE/REPLACE** | One Konva adapter owns paint, hit testing, selection, and transforms. |
| Old M43 proof scripts, selectors, JSON, and screenshots | **RETIRE/RECREATE** | New scene/status/command E2E and M43-local screenshots pass. |

New story implementation must cite adopted files and passing prior tests in its Dev Notes. It must not
rewrite aligned code merely because the story number changed. It also must not keep violating code
merely because an earlier story reached `review`.

| Active path | Current authority leak | Required replacement | Closure evidence |
| --- | --- | --- | --- |
| `ide/lsp/.../AthenaProjectionPayloads.kt` | Hand-written Projection/Spatial paint DTOs | Delete presentation DTO file. Publish generated/validated `AthenaScenePublication` only. | No production reference to any `AthenaProjection*Payload` or `AthenaSpatial*Payload`. |
| `ide/lsp/.../AthenaProjectionSessionProtocol.kt` | Reconstructs raw nodes, sheets, bounds, labels, anchors, and routes for frontend | Delete presentation session mapper. Runtime scene compiler owns complete scene. | No `toProjectionSessionPayload`, `toProjectionPayload`, or paint mapper remains. |
| `AthenaLanguageServer.projectionSession` and JSON method `athena/projectionSession` | Second presentation transport authority | Replace with `athena/diagramScene`; add generated `athena/diagramConnectOptions` and `athena/applyDiagramCommand` contracts. | Protocol test rejects old method and validates new schema/version. |
| Hand-written `AthenaProjectionDocumentPayload` and `AthenaProjectionSessionDocumentPayload` in `athena-lsp-editor-bridge-service.ts` | Duplicate Kotlin/TypeScript wire contract | Delete types and `requestProjectionSession`; import generated scene/publication/command types and expose new requests. | TypeScript build has no raw Projection/Spatial paint type. |
| `athena-presentation-widget.tsx` Canvas route paint plus DOM SVG occurrence paint | Two transform/hit owners; frontend derives frame, rows, labels, and bounds | Replace with Theia host plus one `KonvaDiagramAdapter`; host consumes validated scene and commands only. Remove old file when replacement name activates. | Adapter contract, selection, trace, resize, drag, and port-command tests pass; no `<svg>` paint layer. |
| `athena-presentation__canvas`, `__svg`, `__coordinate-*`, and `__occurrence*` CSS | CSS-coupled second scene and proof API | Delete old selectors. Keep only host, viewport, status, toolbar/accessibility, and adapter container styles. | CSS/text search absence gate plus narrow-viewport screenshot. |
| Frontend calculations of `columnWidth`, `rowHeight`, `rowLabel`, frame geometry, and occurrence labels | Renderer recreates compiler-owned presentation facts | Delete. Coordinate Decorations and labels arrive in scene. Viewport computes only scene-to-CSS transform. | Unit test feeds scene with non-default frame and proves exact paint without recalculation. |
| `ide/theia-product/scripts/athena-m43-proof-main.js` selectors for `__svg`, `__canvas`, DOM occurrences | Old implementation shape is treated as product contract | Rewrite around scene revision/status, Konva hit selection, canvas nonblank pixel sampling, command transcript, and Electron screenshots. | Proof fails when workspace is absent, scene stale, canvas blank, trace wrong, or command not persisted. |
| Current M43 screenshot/output expectations based on split layers or title table | Stale visual acceptance | Replace with clean border, no interior grid/table, max canvas, desktop/narrow screenshots, and scene/SVG digest evidence. | New evidence only under `_bmad-output/implementation-artifacts/m43`; old failed proof not reused. |
| Transitive Ajv and absent Konva declarations | Runtime depends on hoisting or undeclared library | Add exact direct `ajv: "8.20.0"` and `konva: "10.3.0"` to `@engineeringood/athena-theia-frontend`. | Manifest and lockfile exact-version tests. |

## Absence Gate

Closure search must find none of these active presentation contracts:

```text
athena/projectionSession
requestProjectionSession
AthenaProjectionSessionPayload
AthenaProjectionDocumentPayload
AthenaSpatialSheetPayload
athena-presentation__svg
athena-presentation__canvas
paintCanvas(
Spatial Reality unavailable.
```

A raw Projection query may survive only when a named non-presentation consumer is documented and it
does not expose Spatial geometry, paint facts, or renderer data. No alias, shim, fallback request,
dual widget, or old proof selector is allowed.
