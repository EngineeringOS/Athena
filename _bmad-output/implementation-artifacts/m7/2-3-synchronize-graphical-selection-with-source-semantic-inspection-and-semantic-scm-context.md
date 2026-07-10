---
baseline_commit: adb0ae5d1956b13d8aa3df8774e102dfc09ef380
---

# Story 2.3: Synchronize Graphical Selection With Source, Semantic Inspection, And Semantic SCM Context

Status: review

## Story

As an engineer or reviewer,
I want graphical selections to stay coherent with textual and semantic context,
so that I can move between source, inspection, review, and graphical understanding without semantic drift.

## FR Traceability

- FR-4: support graphical navigation and projection-oriented inspection
- FR-5: publish explicit read-only versus editable rules
- FR-6: preserve deterministic graphical refresh from the same underlying semantic state
- FR-7: prepare for later interactive graphical work without locking final editing scope
- NFR-1: graphical surfaces remain downstream of canonical semantic, repository, package, and SCM meaning
- NFR-2: the same repository and semantic state produce the same synchronized inspection outcome
- NFR-3: projection boundary output remains inspectable

## Acceptance Criteria

1. Given source, semantic inspection, semantic SCM, and a graphical view are open, when users select or focus supported graphical elements, then Athena synchronizes that selection with the relevant canonical source and semantic context, and the same repository and semantic state produce the same synchronized inspection outcome.
2. Given later visual review and history work are planned, when synchronization boundaries are reviewed, then existing M6 semantic identity and history vocabulary are reused, and the graphical path does not invent renderer-specific change identities.

## Tasks / Subtasks

- [x] Extend the Athena-owned semantic inspection payload with source anchors for canonical semantic identities. (AC: 1, 2)
  - [x] Add typed source-range payload data for inspected components, ports, and connections under `ide/lsp`.
  - [x] Derive those ranges from the existing AST/navigation information instead of inventing frontend-local guesses.
  - [x] Keep the inspection payload keyed by canonical semantic ids already used in runtime projection and M6 semantic SCM.
- [x] Introduce one shared frontend semantic-selection seam for M7 workbench synchronization. (AC: 1, 2)
  - [x] Add a dedicated browser service under `ide/theia-frontend` that owns only transient selected semantic context, not semantic authority.
  - [x] Resolve source reveal/highlight from the active Athena editor through the typed Athena LSP inspection path.
  - [x] Keep the service disposable and rebuildable from runtime plus editor state.
- [x] Synchronize graphical and semantic-inspection surfaces around canonical semantic ids. (AC: 1, 2)
  - [x] Make the graphical workbench surface publish selection/focus from supported nodes and relationships using their canonical semantic ids.
  - [x] Highlight the active selection in the graphical workbench and semantic inspection surfaces without introducing renderer-owned identities.
  - [x] Allow semantic inspection rows to reuse the same selection seam so the workbench can move coherently between the two surfaces.
- [x] Synchronize semantic SCM context using M6 subject-identity vocabulary. (AC: 1, 2)
  - [x] Reuse `subjectIdentity` and `factReferences.subjectIdentity` from the existing semantic SCM payloads to highlight relevant review or commit entries for the active semantic selection.
  - [x] Do not introduce graph-only review ids, renderer change ids, or a new SCM selection model.
  - [x] Keep package-history state package-scoped unless the current M6 payload already exposes a subject-level semantic match.
- [x] Keep the interaction inspect-first and source-of-truth boundaries explicit. (AC: 1, 2)
  - [x] Do not add persisted graphical editing, local graph mutation, or graph-owned semantic caches in this story.
  - [x] Keep runtime and `ide/lsp` as the only semantic/projection authorities; frontend selection state is transient workbench context only.
  - [x] Keep Story `2.4` scope intact by limiting this story to synchronized selection, reveal, and highlight behavior.
- [x] Cover the synchronization slice with focused proof and documentation. (AC: 1, 2)
  - [x] Add focused tests for any new pure selection-matching or source-anchor helper introduced in `ide/theia-frontend`.
  - [x] Add or extend focused `:ide:lsp` tests for the new typed semantic inspection source-range payload.
  - [x] Update relevant README or usage docs to describe the first synchronized graphical-selection flow.

## Dev Notes

### Story Intent

- Story `2.3` is the first cross-surface synchronization story for M7.
- The success condition is not broad visual interactivity. It is coherent semantic selection across the existing source, inspection, SCM, and graphical workbench surfaces.
- Story `2.3` must reuse canonical semantic ids already proven in M6 and already emitted by runtime projection payloads.
- Story `2.4` still owns the broader inspect-first interaction policy, transient snap-back behavior, and any explicit governed-mutation affordances.

### Architecture Guardrails

- Align to AD-30 by keeping graphical selection downstream of runtime-owned projection sessions and Athena-owned transport rooted at `ide/lsp`. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#AD-30]
- Align to AD-31 by treating selection, focus, reveal, and panel synchronization as allowed inspect-first UI behavior while keeping any meaningful mutation outside this story. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#AD-31]
- Preserve inherited AD-23 by keeping Theia-hosted surfaces downstream bridges rather than semantic cores. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#Inherited-Invariants]
- Reuse the M6 semantic SCM subject-identity vocabulary instead of introducing graphical review identities. [Source: _bmad-output/implementation-artifacts/m6/2-4-expose-review-and-commit-semantics-through-runtime-lsp-and-existing-ide-seams.md]

### Technical Requirements

- Reuse the current proven seams:
  - `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`
  - `ide/theia-frontend/src/browser/athena-semantic-inspection-widget.tsx`
  - `ide/theia-frontend/src/browser/athena-semantic-scm-widget.tsx`
  - `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt`
- The graphical workbench must publish selection using canonical semantic ids already carried by the projection payload and the graph adapter output.
- Source synchronization should rely on typed source ranges from Athena-owned inspection payloads, not frontend string matching or DOM heuristics.
- If a new frontend service is introduced, keep it strictly presentation-scoped and transient.
- Keep all new core TypeScript and Kotlin classes documented clearly with concise comments/KDoc.

### Architecture Compliance

- The story is only successful if the ownership line stays clear:
  - runtime projection and semantic SCM keep semantic identity authority
  - `ide/lsp` keeps typed transport and typed source anchors
  - the frontend keeps only transient selected-semantic context and visual highlighting
- Prevent these failure modes:
  - source reveal guessed from rendered node labels instead of semantic ids
  - graph-only selection ids or change ids that do not map back to canonical semantic identity
  - frontend-owned SCM or inspection caches that become practical authority
  - widening into edit commands, drag behavior, or persisted view mutation

### Library / Framework Requirements

- Use the repo-approved stack already frozen locally:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
  - Node `22+`
  - Yarn `1.22.22`
  - Eclipse Theia `1.73.1`
- Reuse the existing Theia editor APIs for reveal and decorations instead of adding another editor layer.
- Do not add new graphical framework dependencies for synchronization.

### File Structure Requirements

- Expected update files:
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt`
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt` only if the existing request surface needs additive payload wiring
  - `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/` focused inspection request tests
  - `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`
  - `ide/theia-frontend/src/browser/athena-semantic-inspection-widget.tsx`
  - `ide/theia-frontend/src/browser/athena-semantic-scm-widget.tsx`
  - `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`
  - one or more focused frontend helper/service files under `ide/theia-frontend/src/browser/`
  - `ide/theia-frontend/src/browser/style/index.css`
- Files whose current ownership must be preserved:
  - [`ide/theia-frontend/src/browser/athena-graph-adapter-service.ts`](../../../ide/theia-frontend/src/browser/athena-graph-adapter-service.ts)
    - remains translation-consumption only and must not become selection authority
  - [`ide/theia-frontend/src/browser/athena-repository-session-service.ts`](../../../ide/theia-frontend/src/browser/athena-repository-session-service.ts)
    - remains repository session lifecycle state, not semantic selection state
  - [`ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`](../../../ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts)
    - remains the typed Athena LSP bridge for IDE consumers

### Testing Requirements

- Minimum story verification:
  - `yarn workspace @engineeringood/athena-theia-frontend test`
  - `yarn --cwd ide build`
  - `yarn --cwd ide start:smoke`
  - `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :ide:lsp:test"`
- Recommended wider regression:
  - `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain test"`
- Keep Gradle and Node verification sequential on Windows. Do not overlap build, test, or run commands in parallel shells.

### Current Code State To Preserve

- Story `2.2` already hosts the first visible `Graphical View` panel and keeps it read-only.
- `AthenaGraphWorkbenchWidget` already consumes the adapter-owned diagram and should stay downstream of that seam.
- `AthenaSemanticInspectionWidget` and `AthenaSemanticScmWidget` already consume typed Athena LSP payloads and should remain additive surfaces rather than local semantic authorities.
- `AthenaLanguageFeatures.semanticInspection()` already owns the server-side semantic inspection payload and already has access to AST span data through the navigation index.
- The current graph adapter already preserves canonical semantic ids as diagram node and edge ids, which is the correct selection identity for this story.

### Previous Story Intelligence

- Story `1.4` already established additive typed projection transport under `ide/lsp`, so this story should extend typed payloads rather than creating another request family.
- Story `2.1` already established the translation-only graph adapter boundary.
- Story `2.2` already established the real graphical workbench panel but explicitly deferred selection synchronization to this story.
- M6 already established `subjectIdentity` and `factReferences` vocabulary inside semantic SCM payloads; reuse that vocabulary here.

### References

- [Source: _bmad-output/planning-artifacts/epics-M7-2026-07-09.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-09-m7/prd.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/implementation-artifacts/m7/1-4-expose-typed-projection-queries-and-governed-commands-through-ide-lsp.md]
- [Source: _bmad-output/implementation-artifacts/m7/2-1-introduce-the-graph-adapter-boundary-under-integrations-graph.md]
- [Source: _bmad-output/implementation-artifacts/m7/2-2-surface-a-graphical-athena-view-inside-the-existing-workbench.md]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticScmProtocol.kt]
- [Source: ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx]
- [Source: ide/theia-frontend/src/browser/athena-semantic-inspection-widget.tsx]
- [Source: ide/theia-frontend/src/browser/athena-semantic-scm-widget.tsx]
- [Source: ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts]
- [Source: integrations/graph-glsp/src/athena-glsp-diagram-model.ts]

## Story Completion Status

- Status: review

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- story created from M7 epic, PRD, architecture, Story `1.4`, Story `2.1`, and Story `2.2`
- red phase: `yarn workspace @engineeringood/athena-theia-frontend test`
- red phase: `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaSemanticInspectionTest"`
- green phase: `yarn workspace @engineeringood/athena-theia-frontend test`
- green phase: `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaSemanticInspectionTest"`
- workbench build verification: `yarn --cwd ide build`
- desktop smoke verification: `yarn --cwd ide start:smoke`
- repository hygiene verification: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
- full JVM regression verification: `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain test"`

### Completion Notes List

- Extended the Athena semantic inspection payload with typed declaration `sourceRange` data for components, ports, and connections so canonical semantic ids can map back to authored source deterministically.
- Added a transient `AthenaSemanticSelectionService` plus pure selection helpers in `ide/theia-frontend`, keeping synchronized workbench selection downstream of Athena LSP and the active editor instead of creating a frontend semantic authority.
- Updated the graphical workbench panel to publish canonical semantic selection from nodes and relationships, highlight the active graph element, and surface the currently selected semantic object inside the panel.
- Updated the semantic inspection panel so component, port, and connection rows reuse the same selection seam and reflect the active synchronized semantic selection state.
- Updated the semantic SCM panel to reuse M6 `subjectIdentity` and `factReferences.subjectIdentity` vocabulary for selection-aware review and commit highlighting rather than inventing renderer-specific review ids.
- Documented the new synchronized graphical-selection flow in the IDE-facing README files and workspace summary.
- Verified the story with focused frontend and LSP tests, full Theia build, desktop smoke launch, encoding audit, and full Java 25 regression tests.

### File List

- _bmad-output/implementation-artifacts/m7/2-3-synchronize-graphical-selection-with-source-semantic-inspection-and-semantic-scm-context.md
- _bmad-output/implementation-artifacts/m7/sprint-status.yaml
- docs/usages/athena-workspace-summary.md
- ide/README.md
- ide/README.zh-CN.md
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticInspectionTest.kt
- ide/theia-frontend/README.md
- ide/theia-frontend/README.zh-CN.md
- ide/theia-frontend/scripts/athena-semantic-selection-model.test.mjs
- ide/theia-frontend/src/browser/athena-frontend-module.ts
- ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx
- ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts
- ide/theia-frontend/src/browser/athena-semantic-inspection-widget.tsx
- ide/theia-frontend/src/browser/athena-semantic-scm-widget.tsx
- ide/theia-frontend/src/browser/athena-semantic-selection-model.ts
- ide/theia-frontend/src/browser/athena-semantic-selection-service.ts
- ide/theia-frontend/src/browser/style/index.css
- ide/theia-frontend/lib/browser/athena-frontend-module.d.ts.map
- ide/theia-frontend/lib/browser/athena-frontend-module.js
- ide/theia-frontend/lib/browser/athena-frontend-module.js.map
- ide/theia-frontend/lib/browser/athena-graph-workbench-widget.d.ts
- ide/theia-frontend/lib/browser/athena-graph-workbench-widget.d.ts.map
- ide/theia-frontend/lib/browser/athena-graph-workbench-widget.js
- ide/theia-frontend/lib/browser/athena-graph-workbench-widget.js.map
- ide/theia-frontend/lib/browser/athena-lsp-editor-bridge-service.d.ts
- ide/theia-frontend/lib/browser/athena-lsp-editor-bridge-service.d.ts.map
- ide/theia-frontend/lib/browser/athena-lsp-editor-bridge-service.js
- ide/theia-frontend/lib/browser/athena-lsp-editor-bridge-service.js.map
- ide/theia-frontend/lib/browser/athena-semantic-inspection-widget.d.ts
- ide/theia-frontend/lib/browser/athena-semantic-inspection-widget.d.ts.map
- ide/theia-frontend/lib/browser/athena-semantic-inspection-widget.js
- ide/theia-frontend/lib/browser/athena-semantic-inspection-widget.js.map
- ide/theia-frontend/lib/browser/athena-semantic-scm-widget.d.ts
- ide/theia-frontend/lib/browser/athena-semantic-scm-widget.d.ts.map
- ide/theia-frontend/lib/browser/athena-semantic-scm-widget.js
- ide/theia-frontend/lib/browser/athena-semantic-scm-widget.js.map
- ide/theia-frontend/lib/browser/athena-semantic-selection-model.d.ts
- ide/theia-frontend/lib/browser/athena-semantic-selection-model.d.ts.map
- ide/theia-frontend/lib/browser/athena-semantic-selection-model.js
- ide/theia-frontend/lib/browser/athena-semantic-selection-model.js.map
- ide/theia-frontend/lib/browser/athena-semantic-selection-service.d.ts
- ide/theia-frontend/lib/browser/athena-semantic-selection-service.d.ts.map
- ide/theia-frontend/lib/browser/athena-semantic-selection-service.js
- ide/theia-frontend/lib/browser/athena-semantic-selection-service.js.map
- ide/theia-frontend/tsconfig.tsbuildinfo
- ide/theia-product/lib/frontend/bundle.css
- ide/theia-product/lib/frontend/bundle.css.map
- ide/theia-product/lib/frontend/bundle.js
- ide/theia-product/lib/frontend/bundle.js.map

### Change Log

- 2026-07-10: Created Story `2.3` with focused guardrails for canonical semantic-id-based graphical selection synchronization.
- 2026-07-10: Added typed semantic inspection source anchors, transient cross-surface semantic selection, graphical/source/inspection synchronization, semantic SCM highlighting, documentation updates, and full verification on Java 25.
