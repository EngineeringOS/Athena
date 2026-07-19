---
status: review
epic: 4
story: 4.1
title: Render route facts as terminal anchor schematic wires
baseline_commit: e4b243ab9441d585e79c2d8effceb1a6692a3be2
---

# Story 4.1: Render route facts as terminal-anchor schematic wires

As a reviewer, I want Theia to render route facts from terminal anchors, so that wires visually
attach to ports/terminals instead of component centers.

## Acceptance Criteria

- Graphical View renders wires from route facts when route facts are present.
- Wires begin and end at terminal anchors.
- Route segments are orthogonal and grid-aligned.
- The accepted M24 proof has no renderer-side center-to-center fallback.
- DOM/canvas smoke checks can distinguish terminal-anchor routes from old graph-edge routes.

## References

- `../../planning-artifacts/prds/prd-Athena-2026-07-18-m24/prd.md`
- `../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m24/ARCHITECTURE-SPINE.md`

## Notes

This is the user-visible heart of M24. Do not finish it with only model tests.

## Tasks/Subtasks

- [x] Derive terminal-anchor route facts into presentation documents for schematic wires.
- [x] Serialize route-fact-backed presentation connectors through the LSP payload boundary.
- [x] Verify Theia graph workbench rendering consumes terminal-anchor route points instead of legacy graph edges.
- [x] Add focused compiler, LSP, and frontend smoke coverage for terminal-anchor route rendering.

## Dev Agent Record

### Debug Log

- Added a focused compiler test proving presentation derivation publishes route facts and route-backed connectors.
- Verified the compiler route derivation path with `:kernel:compiler:test --tests "com.engineeringood.athena.compiler.PresentationModelDeriverTest.presentation derivation publishes terminal anchor route facts for rendered schematic wires"`.
- Found the LSP presentation payload still serialized raw `connectors`; changed it to serialize `connectorsForRendering()` so route facts reach Theia.
- Added an LSP projection session guard proving serialized connectors carry terminal anchor ids, port semantic ids, orthogonal grid route points, and do not match legacy connection endpoints.
- Strengthened the Theia graph workbench model smoke to reject old fallback route points and assert terminal metadata.

### Completion Notes

- Story 4.1 is implemented inside kernel/compiler, LSP, and Theia frontend only.
- No route syntax was introduced.
- No desktop-viewer or deprecated KMP/Compose desktop module was touched.
- Route facts now reach the IDE as terminal-anchor presentation connectors when available, preserving renderer paint-only behavior.

### Verification

- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests "com.engineeringood.athena.compiler.PresentationModelDeriverTest.presentation derivation publishes terminal anchor route facts for rendered schematic wires"`
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests "com.engineeringood.athena.ide.lsp.AthenaProjectionRequestTest.projection session payload renders terminal anchor route facts instead of legacy graph edges"`
- `yarn --cwd ide/theia-frontend test`
- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests "com.engineeringood.athena.compiler.PresentationModelDeriverTest"`
- `.\gradlew.bat --no-daemon --console=plain :kernel:presentation-model:test`
- `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test`
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests "com.engineeringood.athena.ide.lsp.AthenaProjectionRequestTest"`

## File List

- `kernel/compiler/build.gradle.kts`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/PresentationModelDeriver.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/PresentationModelDeriverTest.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationSessionProtocol.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionRequestTest.kt`
- `ide/theia-frontend/scripts/athena-graph-workbench-model.test.mjs`

## Change Log

- 2026-07-19: Implemented terminal-anchor route fact rendering path from compiler presentation derivation through LSP payload and Theia workbench smoke coverage.

## Status

review
