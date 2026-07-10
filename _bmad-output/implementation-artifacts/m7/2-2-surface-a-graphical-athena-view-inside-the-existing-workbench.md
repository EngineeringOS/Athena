# Story 2.2: Surface A Graphical Athena View Inside The Existing Workbench

Status: review

## Story

As an engineer,
I want Athena to open a real graphical view inside the current desktop workbench,
so that graphical projection becomes a product capability rather than an isolated demo.

## FR Traceability

- FR-3: surface graphical views in the existing Athena workbench
- FR-4: support graphical navigation and projection-oriented inspection
- FR-5: publish explicit read-only versus editable rules
- NFR-1: graphical surfaces remain downstream of canonical semantic, repository, package, and SCM meaning
- NFR-4: M7 extends the current Athena shell rather than replacing it

## Acceptance Criteria

1. Given the current Athena Theia product shell, when users open a supported graphical projection, then Athena presents that graphical view through existing additive product seams rather than a shell rewrite, and graphical views coexist with source, semantic inspection, repository, and semantic SCM surfaces.
2. Given graphical panel lifecycle is reviewed, when workbench ownership is checked, then Theia frontend and backend host panel lifecycle and presentation only, and runtime and `ide/lsp` remain the sole semantic and projection authorities.

## Tasks / Subtasks

- [x] Add the first graphical Athena workbench widget to `ide/theia-frontend`. (AC: 1, 2)
  - [x] Create one dedicated frontend widget for the graphical projection surface, with a straightforward name such as `AthenaGraphWorkbenchWidget`.
  - [x] Bind the widget in `athena-frontend-module.ts` and register a `WidgetFactory` entry without changing the existing shell ownership model.
  - [x] Keep the widget read-only and projection-consumption-only in this story; do not add direct edit or mutation behavior.
- [x] Surface the graphical view through the existing Athena workbench extension model. (AC: 1, 2)
  - [x] Add one new Athena command and workbench-extension registration entry so the graphical view can be opened from the existing Athena menu and layout flow.
  - [x] Make the graphical view coexist with source, repository graph, semantic inspection, and semantic SCM panels instead of replacing any of them.
  - [x] If startup placement is used, keep it additive and professional inside the current workbench layout.
- [x] Consume runtime-owned projection state through the existing adapter and LSP bridge only. (AC: 1, 2)
  - [x] Use `AthenaGraphAdapterService` and the existing typed projection-session request path as the only upstream source for graphical view state.
  - [x] Do not add direct `fetch('/athena/lsp/...')` calls, direct filesystem access, direct JVM calls, or direct `kernel/*` imports in the new graphical widget.
  - [x] Keep any view-local scene state disposable and rebuildable from the adapter output.
- [x] Deliver a real graphical panel presentation, not only raw JSON or debug text. (AC: 1)
  - [x] Render a clear graphical projection surface in the widget, such as an SVG or equivalent node/edge scene, from adapter output.
  - [x] Show meaningful empty, loading, unavailable, and error states for repository/session lifecycle transitions.
  - [x] Present supported view metadata and active-view context in the panel so the workbench surface is inspectable and not a black box.
- [x] Keep ownership and scope discipline explicit. (AC: 1, 2)
  - [x] Preserve `ide/lsp` and runtime as the only semantic/projection authorities for the IDE path.
  - [x] Keep graph-framework or graph-shape vocabulary in the adapter or widget presentation layer only; do not move it into Athena-owned kernel or LSP contracts.
  - [x] Do not add selection synchronization, review/history graph overlays, or edit behavior in this story; Stories `2.3` and `2.4` own those concerns.
- [x] Cover the first graphical workbench surface with focused proof and documentation. (AC: 1, 2)
  - [x] Add focused tests for any new pure view-model or rendering helper introduced for the widget.
  - [x] Keep the `integrations/graph-glsp` adapter verification green after the widget consumes it.
  - [x] Document the new graphical workbench surface in the relevant README or usage docs.

## Dev Notes

### Story Intent

- Story `2.2` is the first visible graphical workbench delivery story for M7.
- The success condition is "Athena now has a real graphical workbench panel inside the existing Theia shell" rather than "the graph adapter exists."
- Story `2.2` must stop before synchronized selection, semantic SCM visual overlays, or inspect-first interaction policy details beyond read-only hosting.
- Story `2.3` owns synchronized graphical selection with source, semantic inspection, and semantic SCM context.
- Story `2.4` owns inspect-first interaction discipline and transient-vs-governed graphical behavior.

### Architecture Guardrails

- Align to AD-30 by feeding the workbench view from runtime-owned projection sessions through Athena-owned transport rooted at `ide/lsp`. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#AD-30]
- Align to AD-31 by keeping the graphical surface inspect-first and preventing private frontend mutation from becoming practical authority. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#AD-31]
- Align to inherited AD-18 and AD-23 by extending the existing Athena shell through additive workbench seams rather than replacing workbench composition or creating a frontend semantic core. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#Inherited-Invariants]
- Preserve the Story `2.1` outcome: graph-framework or graph-shape translation remains behind `integrations/graph-*`, and `ide/theia-*` consumes that boundary downstream. [Source: _bmad-output/implementation-artifacts/m7/2-1-introduce-the-graph-adapter-boundary-under-integrations-graph.md]

### Technical Requirements

- Reuse the current proven frontend seams instead of inventing new ones:
  - `ide/theia-frontend/src/browser/athena-frontend-module.ts` already owns widget binding and workbench-facing service registration.
  - `ide/theia-frontend/src/browser/athena-workbench-extensions.ts` already owns additive Athena workbench commands, menu placement, and startup layout metadata.
  - `ide/theia-frontend/src/browser/athena-product-contribution.ts` already reveals registered Athena workbench extensions and should remain the central layout orchestrator.
  - `ide/theia-frontend/src/browser/athena-graph-adapter-service.ts` already provides the downstream adapter-consumption seam over the typed Athena LSP bridge.
- The new graphical widget must consume Athena projection state through `AthenaGraphAdapterService` only.
- If additional frontend-local helper models are introduced, they must stay presentation-only and disposable.
- Keep all new core TypeScript classes documented clearly with concise comments where structure is not self-evident.

### Architecture Compliance

- The story is only successful if the ownership line stays clear:
  - runtime and `ide/lsp` remain semantic/projection authorities
  - `integrations/graph-glsp` remains the translation boundary
  - `ide/theia-frontend` hosts workbench-visible lifecycle and presentation only
- Prevent these failure modes:
  - direct widget calls to Athena LSP HTTP endpoints
  - a frontend-owned second projection-session cache that becomes practical authority
  - replacing the current workbench-extension registry with custom one-off panel startup logic
  - widening the story into selection synchronization, review overlays, or editing

### Library / Framework Requirements

- Use the repo-approved stack already frozen locally:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
  - Node `22+`
  - Yarn `1.22.22`
  - Eclipse Theia `1.73.1`
- Do not add graph-framework dependencies to `ide/lsp`, `kernel/runtime`, or any kernel module in this story.
- Prefer the current adapter output and frontend-native rendering over introducing a broader framework dependency unless the existing package already requires it.

### File Structure Requirements

- Expected new files:
  - `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`
  - one focused pure helper file and one focused test file if render/view-model logic needs isolation
- Expected update files:
  - `ide/theia-frontend/src/browser/athena-frontend-module.ts`
  - `ide/theia-frontend/src/browser/athena-workbench-extensions.ts`
  - `ide/theia-frontend/src/browser/style/index.css`
  - `ide/theia-frontend/package.json` if a test script is added
  - `ide/README.md` or `ide/theia-frontend/README.md` and the workspace summary if the new surface needs documentation
- Files whose current behavior and ownership must be preserved:
  - [`ide/theia-frontend/src/browser/athena-product-contribution.ts`](../../../ide/theia-frontend/src/browser/athena-product-contribution.ts)
    - remains the workbench layout and reveal orchestrator
  - [`ide/theia-frontend/src/browser/athena-workbench-extensions.ts`](../../../ide/theia-frontend/src/browser/athena-workbench-extensions.ts)
    - remains the single additive registry for Athena workbench surfaces
  - [`ide/theia-frontend/src/browser/athena-graph-adapter-service.ts`](../../../ide/theia-frontend/src/browser/athena-graph-adapter-service.ts)
    - remains the only frontend seam for consuming graph adapter output
  - [`ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`](../../../ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts)
    - remains the typed Athena LSP bridge and should not be bypassed

### Testing Requirements

- Minimum story verification:
  - `yarn --cwd integrations/graph-glsp test`
  - `yarn workspace @engineeringood/athena-theia-frontend test` if a frontend test script is added
  - `yarn --cwd ide build`
  - `yarn --cwd ide start:smoke`
- Recommended focused regression:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test"` if any Kotlin/LSP contract changes are required
- Required proof checks:
  - the graphical panel is visibly hosted by the current Athena workbench composition path
  - the widget renders a real node/edge projection surface from adapter output
  - empty/loading/error/unavailable states remain intelligible
  - semantic and projection authority remain outside the frontend
- Keep Gradle and Node verification sequential on Windows. Do not overlap build, test, or run commands in parallel shells.

### Current Code State To Preserve

- `ide/theia-frontend` already ships three Athena-owned workbench surfaces: repository graph, semantic inspection, and semantic SCM.
- `ATHENA_WORKBENCH_EXTENSIONS` already defines command, menu order, panel area, and startup rank for additive workbench surfaces.
- `AthenaProductContribution` already opens and reveals registered workbench surfaces from that extension registry.
- `AthenaGraphAdapterService` now exists but is not yet attached to any visible workbench surface.
- The current style system is already intentional and panel-specific inside `ide/theia-frontend/src/browser/style/index.css`; the new surface should extend that language instead of resetting it.

### Previous Story Intelligence

- Story `2.1` established the `integrations/graph-glsp` package and the downstream `AthenaGraphAdapterService` seam.
- Story `2.1` also already exposed typed `requestProjectionSession()` support through `AthenaLspEditorBridgeService`.
- The practical implication for `2.2` is that panel delivery should stay entirely in `ide/theia-frontend`; there is no reason to add a second transport or backend semantic path here.

### Git Intelligence Summary

- Recent milestone baseline:
  - `adb0ae5 Complete M4-M6 IDE, repository, and semantic SCM milestones`
  - `a9b3a12 Complete M3 hosted extensibility proof`
  - `1339722 feat(m3): establish kernel plugin api boundary`
  - `dfc0234 add reference`
  - `87b1342 Complete M2 projection proof and normalize milestone artifacts`
- Practical implication:
  - keep the new surface grouped under the existing Theia frontend product path
  - preserve the additive workbench pattern already used in M4 through M6

### Latest Technical Information

- No extra web research is required for this story.
- The stack and version constraints that matter are already frozen locally.

### Project Structure Notes

- No `project-context.md` file exists in the repository.
- `.codegraph/` exists and should continue to be used first when locating or understanding code areas.
- This story sits at the seam between:
  - `integrations/graph-glsp`
  - `ide/theia-frontend`
  - existing `ide/lsp` projection transport
- Naming should stay straightforward:
  - widget: `AthenaGraphWorkbenchWidget`
  - command label: `Reveal Graphical View` or similarly direct Athena-first naming

### References

- [Source: _bmad-output/planning-artifacts/epics-M7-2026-07-09.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-09-m7/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-09-m7/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/implementation-artifacts/m7/2-1-introduce-the-graph-adapter-boundary-under-integrations-graph.md]
- [Source: ide/theia-frontend/src/browser/athena-product-contribution.ts]
- [Source: ide/theia-frontend/src/browser/athena-workbench-extensions.ts]
- [Source: ide/theia-frontend/src/browser/athena-repository-graph-widget.tsx]
- [Source: ide/theia-frontend/src/browser/athena-semantic-inspection-widget.tsx]
- [Source: ide/theia-frontend/src/browser/athena-semantic-scm-widget.tsx]
- [Source: ide/theia-frontend/src/browser/athena-graph-adapter-service.ts]
- [Source: ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts]
- [Source: ide/theia-frontend/src/browser/style/index.css]

## Story Completion Status

- Status: review
- Completion note: Added the first visible `Graphical View` panel to the existing Athena workbench, kept it downstream of the graph adapter and Athena LSP bridge, and verified it through focused frontend, desktop smoke, encoding, and full JVM regression checks.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- M7 sprint status, epic breakdown, PRD, addendum, and architecture spine review
- Story `2.1` review and outcome analysis
- CodeGraph exploration of current Theia widget and workbench seams
- live review of `athena-product-contribution.ts`, `athena-workbench-extensions.ts`, existing Athena widgets, and frontend style host
- red phase: `yarn workspace @engineeringood/athena-theia-frontend test` failed because `athena-graph-workbench-model.js` did not exist yet
- green phase: `yarn workspace @engineeringood/athena-theia-frontend test`
- adapter regression: `yarn --cwd integrations/graph-glsp test`
- workbench build verification: `yarn --cwd ide build`
- desktop smoke verification: `yarn --cwd ide start:smoke`
- repository hygiene verification: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
- full JVM regression verification: `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain test"`

### Completion Notes List

- Added the first visible `AthenaGraphWorkbenchWidget` panel under `ide/theia-frontend` and bound it through the existing widget factory and workbench extension registry.
- Introduced one pure `athena-graph-workbench-model` helper plus focused frontend tests so the SVG scene, status tones, and unavailable-state behavior are verified without UI-only guesswork.
- Reused the existing `AthenaGraphAdapterService` seam so the new panel consumes only adapter-owned projection diagrams and stays downstream of runtime plus `ide/lsp`.
- Rendered a real read-only SVG node/relationship surface together with active-view metadata, supported-view pills, diagnostics, and empty/loading/error states.
- Updated the IDE-facing README and workspace summary docs so the repository no longer claims there is no graphical projection tooling in the current workbench.
- Verified the story with frontend tests, adapter regression tests, full Theia build, desktop smoke launch, encoding audit, and a full Java 25 Gradle regression run.

### File List

- _bmad-output/implementation-artifacts/m7/2-2-surface-a-graphical-athena-view-inside-the-existing-workbench.md
- _bmad-output/implementation-artifacts/m7/sprint-status.yaml
- docs/usages/athena-workspace-summary.md
- ide/README.md
- ide/README.zh-CN.md
- ide/theia-frontend/README.md
- ide/theia-frontend/README.zh-CN.md
- ide/theia-frontend/package.json
- ide/theia-frontend/scripts/athena-graph-workbench-model.test.mjs
- ide/theia-frontend/src/browser/athena-frontend-module.ts
- ide/theia-frontend/src/browser/athena-graph-workbench-model.ts
- ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx
- ide/theia-frontend/src/browser/athena-workbench-extensions.ts
- ide/theia-frontend/src/browser/style/index.css
- ide/theia-frontend/lib/browser/athena-frontend-module.d.ts.map
- ide/theia-frontend/lib/browser/athena-frontend-module.js
- ide/theia-frontend/lib/browser/athena-frontend-module.js.map
- ide/theia-frontend/lib/browser/athena-graph-workbench-model.d.ts
- ide/theia-frontend/lib/browser/athena-graph-workbench-model.d.ts.map
- ide/theia-frontend/lib/browser/athena-graph-workbench-model.js
- ide/theia-frontend/lib/browser/athena-graph-workbench-model.js.map
- ide/theia-frontend/lib/browser/athena-graph-workbench-widget.d.ts
- ide/theia-frontend/lib/browser/athena-graph-workbench-widget.d.ts.map
- ide/theia-frontend/lib/browser/athena-graph-workbench-widget.js
- ide/theia-frontend/lib/browser/athena-graph-workbench-widget.js.map
- ide/theia-frontend/lib/browser/athena-workbench-extensions.d.ts
- ide/theia-frontend/lib/browser/athena-workbench-extensions.d.ts.map
- ide/theia-frontend/lib/browser/athena-workbench-extensions.js
- ide/theia-frontend/lib/browser/athena-workbench-extensions.js.map
- ide/theia-frontend/tsconfig.tsbuildinfo
- ide/theia-product/lib/frontend/bundle.js
- ide/theia-product/lib/frontend/bundle.js.map

### Change Log

- 2026-07-10: Added the first visible graphical Athena workbench panel, wired it through the existing Theia extension registry, documented the new surface, and verified it with focused frontend, desktop smoke, encoding, and full JVM regression checks.
