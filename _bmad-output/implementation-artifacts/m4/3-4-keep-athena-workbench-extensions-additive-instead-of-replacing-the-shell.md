---
baseline_commit: a9b3a12593a428305bb342f3594226aff1120de2
---

# Story 3.4: Keep Athena Workbench Extensions Additive Instead Of Replacing The Shell

Status: done

## Story

As a platform engineer,
I want Athena-specific workbench panels and commands to attach additively to the Theia shell,
so that later milestone capabilities can be introduced without replacing the core IDE structure.

## Acceptance Criteria

1. Given the Athena workbench already includes repository navigation, diagnostics, and semantic inspection surfaces, when Athena-specific panels, commands, or menu entries are added in M4, then they attach through explicit Athena-owned extension points in the product shell, and they do not require the main workbench structure to be rewritten to host them.
2. Given the workbench contains Athena-specific additions, when the shell composition is reviewed, then the product still reads as one coherent Theia-based IDE with one governed semantic path, and the workbench remains downstream of kernel and runtime authority.
3. Given M4 workbench extension points are in place, when future milestones such as M5 repository/package features, M6 semantic SCM surfaces, or later graphical semantic-projection surfaces are considered, then those future capabilities have a clear place to attach inside the same product shell, and M4 does not force a browser-first redesign or a replacement of the core IDE architecture.

## Tasks / Subtasks

- [x] Create one Athena-owned additive workbench extension registry. (AC: 1, 2, 3)
  - [x] Define canonical Athena commands for additive workbench panels in one frontend-owned module.
  - [x] Define canonical widget attachment metadata for area, menu order, quick action label, and startup docking.
- [x] Refactor the frontend shell composition to consume the additive registry. (AC: 1, 2, 3)
  - [x] Register reveal commands from the registry rather than hard-coding each panel separately.
  - [x] Register Athena `View` submenu entries from the same registry.
  - [x] Compose startup panel docking from the same registry so new panels can attach without shell rewrites.
- [x] Refactor Athena Home to consume the same registry for product-owned workbench actions. (AC: 1, 2, 3)
  - [x] Render panel quick actions from the additive registry.
  - [x] Explain the additive extension-point contract in the home surface copy.
- [x] Clean the semantic inspection panel text rendering so the additive surface remains production-legible. (AC: 2)
  - [x] Replace corrupted separator and connection glyph text with ASCII-safe strings.
- [x] Verify sequentially on Windows. (AC: 1, 2, 3)
  - [x] Run `Set-Location ide; yarn workspace @engineeringood/athena-theia-frontend build`.
  - [x] Run `Set-Location ide; yarn verify:desktop`.

## Dev Notes

### Implementation Notes

- `athena-workbench-extensions.ts` is the new Athena-owned attachment seam for additive frontend views in M4.
- The registry currently governs repository navigation, Problems, Output, and Semantic Inspection.
- `Athena Home` stays the main surface, but the add-on workbench panels now attach through the same explicit product-owned metadata path.
- The refactor keeps frontend ownership in Theia while leaving semantic authority in the JVM runtime behind `ide/lsp`.

### Files Changed

- `ide/theia-frontend/src/browser/athena-workbench-extensions.ts`
- `ide/theia-frontend/src/browser/athena-product-contribution.ts`
- `ide/theia-frontend/src/browser/athena-home-widget.tsx`
- `ide/theia-frontend/src/browser/athena-semantic-inspection-widget.tsx`

### Verification

- `Set-Location ide; yarn workspace @engineeringood/athena-theia-frontend build`
- `Set-Location ide; yarn verify:desktop`
