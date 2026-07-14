---
baseline_commit: c04b3eb
---

# Story 2.1: Publish The First Governed Component Panel

Status: done

## Story

As an engineer,  
I want Athena to show available components in a dedicated component panel,  
so that I can author from governed component knowledge without writing DSL directly.

## FR Traceability

- FR-3: Athena can publish a governed component palette from active component-knowledge packs
- NFR-2: workbench surfaces remain consumers of platform-owned authoring contracts
- NFR-3: available components, parts, ports, and traits derive from active M14 component knowledge rather than frontend hardcoding
- NFR-6: the first proof stays narrow, Siemens-first, and electrical only

## Acceptance Criteria

1. Given one governed repository is open, when the Athena component panel is displayed, then available components are loaded from active component-knowledge packs instead of from hardcoded frontend lists.
2. Given the first proof slice is electrical only, when categories are reviewed, then the panel can group the first proof set by narrow categories such as PLC, power supply, motor, and contactor.

## Tasks / Subtasks

- [x] Publish one Athena component panel widget in the left workbench sidebar. (AC: 1, 2)
  - [x] Add a dedicated Theia widget for the governed component panel.
  - [x] Register the widget in the frontend module and workbench extension list.
  - [x] Keep the panel styling dense, theme-aware, and aligned with existing Athena side panels.
- [x] Publish one frontend transport seam for available components. (AC: 1)
  - [x] Add typed frontend transport payloads for component-knowledge-backed available components.
  - [x] Request component knowledge through Athena-owned LSP transport instead of frontend-local catalogs.
  - [x] Keep story `2.1` read-only; no insertion or mutation execution yet.
- [x] Group the first governed component set by narrow engineering categories. (AC: 2)
  - [x] Derive categories from resolved component knowledge.
  - [x] Keep the first grouping narrow to PLC, power supply, motor, contactor, protection, and fallback categories.
  - [x] Avoid hardcoded vendor catalogs or freeform generic graph palettes.
- [x] Add focused verification. (AC: 1, 2)
  - [x] Add one frontend contract test for deterministic category grouping.
  - [x] Run targeted frontend build or test commands sequentially.
  - [x] Run required encoding audit after touching docs or text assets.

## Dev Notes

### Story Intent

- Story `2.1` proves Athena can expose governed authorable components as a product surface.
- Story `2.1` is not insertion yet; that belongs to Story `2.2`.
- The panel must consume M14 component knowledge and M15 runtime or transport seams, not invent a second frontend catalog truth.

### Architecture Guardrails

- Keep the panel as a thin Theia consumer.
- Do not bypass Athena LSP or M14 component knowledge.
- Do not emit direct graph creation, source edits, or mutation commits in this story.
- Preserve space for later guided placement preview through the existing authoring seam.

### File Structure Requirements

- Expected update files likely include:
  - `ide/theia-frontend/src/browser/...`
  - `ide/theia-frontend/scripts/...`
  - `ide/theia-frontend/package.json` only if verification wiring changes are needed
- Explicit non-goals:
  - no insertion execution yet
  - no graph node creation yet
  - no preview acceptance UI yet
  - no inspector editing yet

### References

- [Source: _bmad-output/planning-artifacts/epics-M15-2026-07-13.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-13-m15/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-13-m15/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-13-m15/ARCHITECTURE-SPINE.md]

## Story Completion Status

- Status: done
- Completion note: Athena now publishes one read-only governed component panel from active M14 component knowledge through Athena-owned runtime and LSP seams, with deterministic narrow grouping and verified frontend/backend contracts.

## Implementation Notes

- Extended component-knowledge runtime output with `availableComponents` derived from active plugin knowledge contributions instead of frontend-local catalogs.
- Published typed LSP and frontend payloads for governed available components and implementations.
- Added one left-sidebar Theia component panel widget that refreshes from repository and editor context without performing insertion yet.
- Added deterministic narrow grouping for the first proof slice: PLC, power supply, motor, contactor, protection, and fallback.

## Verification

- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaComponentKnowledgeRuntimeServiceTest"`
- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaComponentKnowledgeRequestTest"`
- `yarn build`
- `node --test scripts/athena-component-panel-model.test.mjs`
- `powershell -ExecutionPolicy Bypass -File .\\tools\\encoding-audit.ps1`
