---
baseline_commit: a9b3a12593a428305bb342f3594226aff1120de2
---

# Story 3.1: Compose A Professional Multi-Panel Athena Workbench

Status: done

## Story

As an engineer,
I want Athena to provide a serious multi-panel workbench layout,
so that I can work in an IDE-shaped environment rather than a bare editor window.

## Acceptance Criteria

1. Given the Athena Theia product is running with an active Repository Session, when the main workbench is shown, then it includes an editor area plus repository navigation and diagnostics-facing panels, and the structure reads as a professional IDE baseline rather than a temporary demo shell.
2. Given the workbench baseline is composed, when future product growth is considered, then the layout leaves room for semantic inspection, console visibility, and later engineering-facing panes, and the M4 composition leaves room for later graphical semantic-projection surfaces under the same shell.
3. Given the workbench baseline is composed, when future product growth is considered, then the M4 composition does not hard-code a throwaway shell that would need replacement in M5 or M6.

## Tasks / Subtasks

- [x] Compose the first explicit Athena workbench frame. (AC: 1, 2, 3)
  - [x] Keep Athena Home in the main area.
  - [x] Dock repository navigation in the left panel.
  - [x] Dock Problems and Output in the bottom panel.
- [x] Keep the implementation inside frontend product composition only. (AC: 2, 3)
  - [x] Reuse Theia widget composition rather than inventing a parallel shell.
  - [x] Keep semantic authority unchanged in Athena LSP and runtime.
- [x] Refresh visible product framing copy. (AC: 1, 2)
  - [x] Update Athena Home proof bullets to reflect the new workbench zones.
- [x] Verify the desktop shell still builds and starts sequentially on Windows with Java 25 available for the product path. (AC: 1, 2, 3)
  - [x] Run `Set-Location ide; yarn verify:desktop`.

## Dev Notes

### Implementation Notes

- The first Epic 3 workbench step stays deliberately narrow:
  - main area: Athena Home and editors
  - left area: repository navigation
  - bottom area: Problems and Output
- No new semantic service, repository contract, or inspection model was introduced.
- The composition is additive on top of standard Theia widgets so later Epic 3 stories can layer Athena-owned commands and inspection panes without replacing the shell.

### Files Changed

- `ide/theia-frontend/src/browser/athena-product-contribution.ts`
- `ide/theia-frontend/src/browser/athena-home-widget.tsx`

### Verification

- `Set-Location ide; yarn verify:desktop`

### Remaining Review Note

- Story `3.2` should route Athena-owned workbench commands and view reveals through explicit product entry points rather than relying only on layout composition.
