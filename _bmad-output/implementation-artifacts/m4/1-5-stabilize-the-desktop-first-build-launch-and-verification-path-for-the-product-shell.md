---
baseline_commit: a9b3a12593a428305bb342f3594226aff1120de2
---

# Story 1.5: Stabilize The Desktop-First Build, Launch, And Verification Path For The Product Shell

Status: done

## Story

As a platform owner,
I want the Athena Theia product to build and launch predictably on the current workstation,
so that M4 can close with a trustworthy desktop-first proof path.

## Acceptance Criteria

1. Given the Athena Theia product modules and repository flows are in place, when the documented desktop build and launch steps are executed on the current workstation, then the product builds and starts successfully through a deterministic local path, and the proof remains compatible with the current workstation constraints and Java 25 environment.
2. Given the desktop-first path is documented and verified, when the product is explained as an M4 milestone outcome, then it is clear that M4 has proven desktop-first product embodiment, and browser-first expansion remains deferred without blocking future Theia-based growth.

## Tasks / Subtasks

- [x] Remove the remaining Windows Java 25 ambiguity from the Theia desktop launch path. (AC: 1)
  - [x] Make the Electron wrapper resolve Java 25 automatically on Windows before Theia boots.
  - [x] Export the resolved Java home into the backend child-process environment used by `ide/lsp`.
  - [x] Keep the fallback behavior explicit when Java 25 cannot be resolved.
- [x] Add a deterministic desktop smoke verification path. (AC: 1)
  - [x] Add a `start:smoke` command that launches the real Electron entrypoint.
  - [x] Wait for a real window-ready signal instead of treating process spawn alone as success.
  - [x] Exit cleanly after the ready signal so the proof can run in automation and code review.
- [x] Update the M4 docs so the verified desktop path is explicit. (AC: 1, 2)
  - [x] Update `ide/README*` and `ide/theia-product/README*` with the final desktop command sequence.
  - [x] Update `README.md`, `DEV.md`, and `docs/usages/athena-workspace-summary.md` with the deterministic path.
  - [x] Extend `docs/compiler/java-25-build-and-launch-notes.md` to cover the Electron-hosted M4 JVM path.
- [x] Record the stabilized story outcome in the M4 implementation artifacts. (AC: 2)
  - [x] Create the Story `1.5` artifact under `_bmad-output/implementation-artifacts/m4/`.
  - [x] Update `sprint-status.yaml`.
- [x] Verify the final desktop-first path on the current workstation. (AC: 1, 2)
  - [x] Run `Set-Location ide; yarn build`.
  - [x] Run `Set-Location ide; yarn start:smoke`.
  - [x] Confirm the smoke proof resolved Java 25 and reached a real window-ready signal.

## Dev Notes

### Implementation Notes

- The core stabilization problem was not Theia itself. It was Windows process inheritance:
  - `java25.bat` can print success without modifying the parent PowerShell environment
  - a later Electron/backend child process can still inherit Java `19` unless Athena fixes it inside the product launcher
- The final M4 solution stays inside the product boundary:
  - `athena-electron-main.js` resolves and exports Java 25
  - `verify-athena-start.js` proves the real desktop entrypoint reaches window-ready state
- This story stabilizes the desktop-first product embodiment only. It does not add browser-first support or expand IDE semantics beyond the existing M4 scope.

### Files Changed

- `ide/package.json`
- `ide/theia-product/package.json`
- `ide/theia-product/scripts/athena-electron-main.js`
- `ide/theia-product/scripts/verify-athena-start.js`
- `ide/README.md`
- `ide/README.zh-CN.md`
- `ide/theia-product/README.md`
- `ide/theia-product/README.zh-CN.md`
- `README.md`
- `DEV.md`
- `docs/compiler/java-25-build-and-launch-notes.md`
- `docs/usages/athena-workspace-summary.md`

### Verification

- `Set-Location ide; yarn build`
- `Set-Location ide; yarn start:smoke`
  - result: `Athena desktop smoke start passed. ready=true javaHome=D:\Program Files\Java\openjdk-25.0.2`

### Remaining Review Note

- The deterministic desktop-first product path is now in place. Future stories can reuse `yarn start:smoke` for workstation-safe startup verification before layering on LSP or workbench features.
