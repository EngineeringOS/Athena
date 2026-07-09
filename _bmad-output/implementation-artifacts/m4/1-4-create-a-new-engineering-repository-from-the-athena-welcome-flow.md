---
baseline_commit: a9b3a12593a428305bb342f3594226aff1120de2
---

# Story 1.4: Create A New Engineering Repository From The Athena Welcome Flow

Status: done

## Story

As an engineer,
I want Athena to create a new Engineering Repository from the product shell,
so that I can start new work without leaving the IDE or waiting for M5 repository contracts.

## Acceptance Criteria

1. Given the Athena welcome flow is visible, when I create a new Engineering Repository, then Athena creates the minimum repository bootstrap structure required for the M4 proof, and the new repository can be activated immediately as the active Repository Session.
2. Given the repository bootstrap is created in M4, when the created files and folders are reviewed, then the structure is intentionally light and suitable for current Athena source conventions, and it does not freeze final `athena.yaml`, `athena.lock`, or package-resolution rules that belong to M5.

## Tasks / Subtasks

- [x] Add a backend repository-bootstrap service for the M4 proof shape. (AC: 1, 2)
  - [x] Create one local bootstrapper that materializes a new repository root under a selected parent folder.
  - [x] Create only the temporary M4 shape `src/<project>.athena`.
  - [x] Reject invalid repository names and existing target directories instead of silently overwriting content.
- [x] Add a product-owned create flow in the Theia shell. (AC: 1)
  - [x] Add a `New Engineering Repository` command.
  - [x] Prompt for the parent folder and repository name from the Athena product shell.
  - [x] Open the created repository in the same window so the existing single-session activation path can attach immediately.
- [x] Keep M4 bootstrap deliberately light. (AC: 2)
  - [x] Seed only one minimal authored source file with `system <Name> { }`.
  - [x] Do not create final `athena.yaml`, `athena.lock`, or package-resolution contracts.
  - [x] Document the temporary bootstrap rule explicitly so M5 can replace it cleanly later.
- [x] Update implementation artifacts and usage docs. (AC: 1, 2)
  - [x] Update the `ide/*` READMEs with create-flow and bootstrap-shape ownership notes.
  - [x] Update `docs/usages/athena-workspace-summary.md` with a deterministic create-and-activate proof command.
  - [x] Update sprint tracking and record this story in the M4 implementation-artifacts folder.
- [x] Verify create-plus-activate behavior without claiming Story `1.5` is already closed. (AC: 1, 2)
  - [x] Run `Set-Location ide; yarn build`.
  - [x] Run a deterministic create-and-activate proof using the built backend bootstrapper plus the JVM session host and confirm `session-ready`.
  - [ ] Run an interactive manual welcome-flow proof in the desktop shell.

## Dev Notes

### Implementation Notes

- Repository creation stays product-owned at the command and welcome-flow layer, but physical filesystem bootstrap remains a backend responsibility.
- The created repository intentionally stays below the M5 contract line:
  - one repository root
  - one `src/` folder
  - one authored `.athena` file
- Activation reuses Story `1.3` exactly instead of introducing a second runtime bootstrap path.

### Files Changed

- `ide/theia-backend/src/node/athena-repository-bootstrapper.ts`
- `ide/theia-backend/src/node/athena-backend-contribution.ts`
- `ide/theia-backend/src/node/athena-backend-module.ts`
- `ide/theia-frontend/package.json`
- `ide/theia-frontend/src/browser/athena-repository-creation-service.ts`
- `ide/theia-frontend/src/browser/athena-product-contribution.ts`
- `ide/theia-frontend/src/browser/athena-home-widget.tsx`
- `ide/theia-frontend/src/browser/athena-frontend-module.ts`
- `ide/theia-frontend/src/browser/style/index.css`
- `ide/README.md`
- `ide/README.zh-CN.md`
- `ide/theia-backend/README.md`
- `ide/theia-backend/README.zh-CN.md`
- `ide/theia-frontend/README.md`
- `ide/theia-frontend/README.zh-CN.md`
- `examples/m4/README.md`
- `examples/m4/README.zh-CN.md`
- `docs/usages/athena-workspace-summary.md`

### Verification

- `Set-Location ide; yarn build`
- Deterministic create-and-activate proof command documented in `docs/usages/athena-workspace-summary.md`
  - proof result: created `Factory Line Alpha/src/factory-line-alpha.athena`
  - proof result: JVM host returned `session-ready` for the newly created repository

### Remaining Review Note

- The interactive desktop welcome-flow proof is still pending manual validation in the running Athena shell. Story `1.5` still owns final deterministic desktop bring-up closure.
