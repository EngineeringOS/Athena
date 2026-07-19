---
status: ready-for-dev
baseline_commit: b195399ea8ba56f120948427e5f63d55cc8fec5f
epic: 3
story: 3.1
title: Render presentation primitives and schematic symbol anatomy
---

# Story 3.1: Render presentation primitives and schematic symbol anatomy

## Story

As an IDE user,
I want components to render as governed engineering representations,
So that the sheet no longer looks like generic graph boxes.

## Acceptance Criteria

- Supported symbols render from Presentation IR primitives, bounds, and hotspots.
- Generic fallback styling is absent from the accepted proof.
- Renderer code remains paint-only.
- Rendering remains inside the Theia IDE frontend only.

## Tasks/Subtasks

- [x] Locate Graphical View rendering code using CodeGraph.
- [x] Render M25 primitives from Presentation IR facts.
- [x] Add fallback-free accepted-proof DOM markers.
- [x] Verify no desktop-viewer/KMP/Compose frontend files are touched.

## Dev Notes

- Governed by AD-1, AD-2, AD-7, AD-9.

## Dev Agent Record

### Debug Log

- 2026-07-19: Used CodeGraph to locate the active Theia Graph Workbench renderer, presentation model resolver, LSP projection payload mapper, and graph-glsp adapter boundary.
- 2026-07-19: Red phase confirmed with `yarn --cwd integrations/graph-glsp test`; adapter failed because `representationFacts` were dropped from the projection payload.
- 2026-07-19: Green phase passed with `yarn --cwd integrations/graph-glsp test`.
- 2026-07-19: Theia model/rendering regression passed with `yarn --cwd ide/theia-frontend test --test-name-pattern "M25|representation|Presentation IR|route inspection|ready graphical"`.
- 2026-07-19: Kotlin LSP red phase failed on missing `:kernel:representation-model` dependency from `:ide:lsp`; fixed with an explicit dependency.
- 2026-07-19: LSP verification passed with `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test`.

### Completion Notes

- Added typed M25 representation facts to the LSP Presentation payload and graph-glsp transport.
- Theia Graph Workbench now resolves M25 representation anatomy into the existing paint-only presentation command stream.
- Representation anatomy is scaled using upstream projection placement; the frontend does not create semantic meaning or persist canvas state.
- Added SVG DOM markers for governed representation rendering: `data-athena-representation-fact`, `data-athena-representation-id`, and `data-athena-render-fallback="false"`.
- Verified changed files do not include desktop-viewer, Compose, or deprecated KMP frontend modules.

### File List

- `ide/lsp/build.gradle.kts`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationPayloads.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationSessionProtocol.kt`
- `ide/theia-frontend/scripts/athena-graph-workbench-model.test.mjs`
- `ide/theia-frontend/src/browser/athena-graph-presentation-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`
- `integrations/graph-glsp/lib/athena-glsp-projection-adapter.js`
- `integrations/graph-glsp/lib/athena-glsp-projection-adapter.js.map`
- `integrations/graph-glsp/lib/athena-glsp-projection-source.d.ts`
- `integrations/graph-glsp/lib/athena-glsp-projection-source.d.ts.map`
- `integrations/graph-glsp/src/athena-glsp-projection-adapter.ts`
- `integrations/graph-glsp/src/athena-glsp-projection-source.ts`
- `integrations/graph-glsp/test/athena-graph-glsp-adapter.test.mjs`

## Change Log

- 2026-07-19: Implemented M25 representation fact transport and Theia governed symbol rendering support.

## Status

review
