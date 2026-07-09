---
baseline_commit: a9b3a12593a428305bb342f3594226aff1120de2
---

# Story 3.2: Route Core Athena Commands And Views Through Explicit Product Boundaries

Status: done

## Story

As an engineer,
I want core Athena commands and views to be deliberately wired into the product shell,
so that repository actions and workbench behavior remain Athena-owned rather than raw upstream defaults.

## Acceptance Criteria

1. Given the workbench baseline exists, when I use core product actions such as open repository, create repository, or reveal diagnostic and semantic views, then those actions are available through explicit Athena command and menu entry points, and the view composition is controlled by Athena rather than by uncurated default shell behavior.
2. Given Theia frontend and backend ownership is split, when command and view wiring is reviewed, then presentation and panel behavior remain in frontend ownership, and product startup, path handling, and process orchestration remain in backend ownership.

## Tasks / Subtasks

- [x] Keep repository actions Athena-owned in the product contribution. (AC: 1, 2)
  - [x] Preserve explicit Athena commands for creating and opening Engineering Repositories.
  - [x] Keep the wiring in frontend product contribution code instead of leaking into backend process code.
- [x] Add Athena-owned reveal commands for current workbench surfaces. (AC: 1, 2)
  - [x] Reveal the repository navigator through an Athena command.
  - [x] Reveal Problems through an Athena command.
  - [x] Reveal Output through an Athena command.
- [x] Wire explicit Athena menu entry points. (AC: 1, 2)
  - [x] Keep File actions under Athena-owned menu entries.
  - [x] Add an Athena submenu under `View` for current workbench surfaces.
- [x] Add product-owned quick actions on Athena Home. (AC: 1)
  - [x] Route Athena Home, Repository Navigator, Problems, and Output through Athena commands.
- [x] Verify the desktop shell still builds and starts sequentially on Windows. (AC: 1, 2)
  - [x] Run `Set-Location ide; yarn workspace @engineeringood/athena-theia-frontend build`.
  - [x] Run `Set-Location ide; yarn verify:desktop`.

## Dev Notes

### Implementation Notes

- Story 3.2 stays inside frontend product composition.
- No semantic inspection widget was introduced here; Story `3.3` remains the place where read-only semantic inspection will actually appear.
- The current Athena-owned workbench surface set is:
  - Athena Home
  - Repository Navigator
  - Problems
  - Output

### Files Changed

- `ide/theia-frontend/src/browser/athena-product-contribution.ts`
- `ide/theia-frontend/src/browser/athena-home-widget.tsx`

### Verification

- `Set-Location ide; yarn workspace @engineeringood/athena-theia-frontend build`
- `Set-Location ide; yarn verify:desktop`

### Remaining Review Note

- Story `3.3` should attach a real Athena semantic inspection surface to the same command and view boundary instead of inventing a second shell path.
