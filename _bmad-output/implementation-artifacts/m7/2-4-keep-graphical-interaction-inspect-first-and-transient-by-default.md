---
baseline_commit: adb0ae5d1956b13d8aa3df8774e102dfc09ef380
---

# Story 2.4: Keep Graphical Interaction Inspect-First And Transient By Default

Status: review

## Story

As a platform engineer,
I want M7 graphical interaction to stay inspect-first,
so that the first visual milestone proves useful interaction without widening into unrestricted graphical authoring.

## FR Traceability

- FR-4: support graphical navigation and projection-oriented inspection
- FR-5: publish explicit read-only versus editable rules
- FR-6: preserve deterministic projection refresh from the same underlying semantic state
- FR-7: prepare for later graphical interaction without locking final editing scope
- NFR-1: graphical surfaces remain downstream of canonical semantic, repository, package, and SCM meaning
- NFR-2: the same upstream semantic state and chosen view yield the same projection state
- NFR-5: later richer interaction must not collapse into unrestricted editing

## Acceptance Criteria

1. Given a graphical workbench view is open, when users select, focus, reveal, or navigate supported graphical elements, then those interactions behave as downstream UI actions over runtime-owned projection state, and they do not mutate semantic or persisted projection state privately.
2. Given unapproved frontend interaction occurs or a graphical client offers richer default behavior, when Athena refreshes projection state, then that unapproved state either snaps back or is discarded on refresh, and any persisted projection change must route through governed runtime commands.

## Tasks / Subtasks

- [x] Expose the existing governed projection command path to the frontend graph adapter. (AC: 1, 2)
  - [x] Add typed frontend request/response contracts for Athena-owned projection commands through the current LSP bridge.
  - [x] Reuse the existing runtime allowlist from Story `1.4`; do not invent a frontend-local graph command model.
  - [x] Keep graph adapter behavior translation-only and downstream of the Athena LSP bridge.
- [x] Keep supported graphical navigation governed and inspect-first. (AC: 1, 2)
  - [x] Route supported view switching through the governed runtime command path instead of local widget-only active-view mutation.
  - [x] Keep selection, focus, and reveal as downstream UI-only behavior.
  - [x] Surface inspect-first status clearly in the graphical workbench without introducing edit affordances.
- [x] Discard or snap back transient interaction state on projection refresh. (AC: 1, 2)
  - [x] Clear the active synchronized selection when a refreshed projection no longer contains the selected semantic id.
  - [x] Ensure graph-surface refresh or governed view switching rebuilds visible state from the latest runtime-owned diagram rather than preserving stale frontend graph state.
  - [x] Do not persist local drag, layout, or canvas mutation state anywhere in this story.
- [x] Cover the inspect-first transient behavior with focused proof and documentation. (AC: 1, 2)
  - [x] Add focused frontend tests for transient-selection reconciliation against refreshed diagrams.
  - [x] Keep existing LSP projection-command tests green while reusing the same governed command boundary.
  - [x] Update relevant README or usage docs to make the inspect-first / governed-command boundary explicit.

## Dev Notes

### Story Intent

- Story `2.4` closes Epic 2 by tightening the interaction boundary around the first graphical workbench surface.
- The success condition is not editing. It is proving that useful navigation remains downstream UI behavior while anything persistent still routes through governed runtime commands.
- Story `2.4` should reuse Story `1.4` command allowlisting, Story `2.2` panel hosting, and Story `2.3` synchronized semantic selection instead of inventing new interaction ownership.

### Architecture Guardrails

- Align to AD-30 by keeping graphical interaction downstream of Athena-owned transport rooted at `ide/lsp`. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#AD-30]
- Align to AD-31 by treating selection, focus, reveal, and navigation as allowed inspect-first UI behavior, while any meaningful persisted change remains governed by runtime commands. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#AD-31]
- Preserve Story `1.4`'s explicit command allowlist and do not expose a generic graph-framework action tunnel. [Source: _bmad-output/implementation-artifacts/m7/1-4-expose-typed-projection-queries-and-governed-commands-through-ide-lsp.md]

### Technical Requirements

- Reuse:
  - `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`
  - `ide/theia-frontend/src/browser/athena-graph-adapter-service.ts`
  - `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`
  - `ide/theia-frontend/src/browser/athena-semantic-selection-model.ts`
  - `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionRequestTest.kt`
- Keep any new frontend helper pure and disposable.
- Do not add edit gestures, drag interactions, local layout persistence, or renderer-owned command ids.

### Testing Requirements

- Minimum verification:
  - `yarn workspace @engineeringood/athena-theia-frontend test`
  - `yarn --cwd ide build`
  - `yarn --cwd ide start:smoke`
  - `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaProjectionRequestTest"`
- Wider regression before completion:
  - `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain test"`

## Story Completion Status

- Status: review

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- story created from M7 epic, PRD, architecture, Stories `1.4`, `2.2`, and `2.3`
- red phase: `yarn workspace @engineeringood/athena-theia-frontend test`
- green phase: `yarn workspace @engineeringood/athena-theia-frontend test`
- focused governed-command regression: `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaProjectionRequestTest"`
- workbench build verification: `yarn --cwd ide build`
- desktop smoke verification: `yarn --cwd ide start:smoke`
- repository hygiene verification: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
- full JVM regression verification: `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain test"`

### Completion Notes List

- Extended the frontend Athena LSP bridge with typed projection-command request/response payloads and reused the existing runtime-governed `switch-active-view` allowlist instead of introducing a graph-local command model.
- Extended the graph adapter boundary so supported view switching stays translation-only and downstream of Athena-owned LSP projection commands.
- Updated the graphical workbench surface so supported-view navigation is explicitly inspect-first, routes through governed runtime commands, and does not mutate local active-view state privately.
- Added transient-selection reconciliation so refreshed projection diagrams discard stale selection when the selected semantic id is no longer present after governed view switching or projection refresh.
- Updated IDE-facing docs to make the inspect-first / governed-command interaction boundary explicit.
- Verified the story with focused frontend tests, focused projection-command regression, full Theia build, desktop smoke launch, encoding audit, and full Java 25 regression tests.

### File List

- _bmad-output/implementation-artifacts/m7/2-4-keep-graphical-interaction-inspect-first-and-transient-by-default.md
- _bmad-output/implementation-artifacts/m7/sprint-status.yaml
- docs/usages/athena-workspace-summary.md
- ide/README.md
- ide/README.zh-CN.md
- ide/theia-frontend/README.md
- ide/theia-frontend/README.zh-CN.md
- ide/theia-frontend/scripts/athena-semantic-selection-model.test.mjs
- ide/theia-frontend/src/browser/athena-graph-adapter-service.ts
- ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx
- ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts
- ide/theia-frontend/src/browser/athena-semantic-selection-model.ts
- ide/theia-frontend/src/browser/style/index.css
- ide/theia-frontend/lib/browser/athena-graph-adapter-service.d.ts
- ide/theia-frontend/lib/browser/athena-graph-adapter-service.d.ts.map
- ide/theia-frontend/lib/browser/athena-graph-adapter-service.js
- ide/theia-frontend/lib/browser/athena-graph-adapter-service.js.map
- ide/theia-frontend/lib/browser/athena-graph-workbench-widget.d.ts
- ide/theia-frontend/lib/browser/athena-graph-workbench-widget.d.ts.map
- ide/theia-frontend/lib/browser/athena-graph-workbench-widget.js
- ide/theia-frontend/lib/browser/athena-graph-workbench-widget.js.map
- ide/theia-frontend/lib/browser/athena-lsp-editor-bridge-service.d.ts
- ide/theia-frontend/lib/browser/athena-lsp-editor-bridge-service.d.ts.map
- ide/theia-frontend/lib/browser/athena-lsp-editor-bridge-service.js
- ide/theia-frontend/lib/browser/athena-lsp-editor-bridge-service.js.map
- ide/theia-frontend/lib/browser/athena-semantic-selection-model.d.ts
- ide/theia-frontend/lib/browser/athena-semantic-selection-model.d.ts.map
- ide/theia-frontend/lib/browser/athena-semantic-selection-model.js
- ide/theia-frontend/lib/browser/athena-semantic-selection-model.js.map
- ide/theia-frontend/tsconfig.tsbuildinfo
- ide/theia-product/lib/frontend/bundle.css
- ide/theia-product/lib/frontend/bundle.css.map
- ide/theia-product/lib/frontend/bundle.js
- ide/theia-product/lib/frontend/bundle.js.map

### Change Log

- 2026-07-10: Created Story `2.4` for governed active-view switching and transient interaction reset.
- 2026-07-10: Added governed active-view switching through Athena-owned projection commands, transient-selection discard on refresh, documentation updates, and full verification on Java 25.
